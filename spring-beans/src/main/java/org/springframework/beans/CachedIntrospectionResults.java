// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

/**
 * 内部类，用于缓存 Java 类的 JavaBeans {@link java.beans.PropertyDescriptor} 信息。不打算直接由应用程序代码使用。
 *
 * <p>对于 Spring 在应用程序的类加载器（@link ClassLoader）内部缓存自己的 bean 描述符，而不是依赖于 JDK 的系统级 {@link BeanInfo} 缓存（为了避免在共享 JVM 中单个应用程序关闭时的泄漏）是必要的。
 *
 * <p>信息是静态缓存的，因此我们不需要为每个我们操作的 JavaBean 创建新的此类对象。因此，此类实现了工厂设计模式，使用私有构造函数和一个静态的 {@link #forClass(Class)} 工厂方法来获取实例。
 *
 * <p>请注意，为了使缓存有效，需要满足一些先决条件：优先选择 Spring jar 与应用程序类位于同一类加载器中的配置，这样无论在何种情况下都可以实现与应用程序生命周期的清洁缓存。
 *
 * <p>从 6.0 版本开始，Spring 的默认反射通过高效的方法反射遍历发现基本的 JavaBeans 属性。对于包括索引属性和所有 JDK 支持的定制器的完整 JavaBeans 反射，请配置一个包含以下内容的 {@code META-INF/spring.factories} 文件：
 * {@code org.springframework.beans.BeanInfoFactory=org.springframework.beans.StandardBeanInfoFactory}
 * 对于与 Spring 5.3 兼容的扩展反射，包括非 void 设置器方法：
 * {@code org.springframework.beans.BeanInfoFactory=org.springframework.beans.ExtendedBeanInfoFactory}
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 05 May 2001
 * @see #acceptClassLoader(ClassLoader)
 * @see #clearClassLoader(ClassLoader)
 * @see #forClass(Class)
 */
public final class CachedIntrospectionResults {

    private static final List<BeanInfoFactory> beanInfoFactories = SpringFactoriesLoader.loadFactories(BeanInfoFactory.class, CachedIntrospectionResults.class.getClassLoader());

    private static final SimpleBeanInfoFactory simpleBeanInfoFactory = new SimpleBeanInfoFactory();

    private static final Log logger = LogFactory.getLog(CachedIntrospectionResults.class);

    /**
     * 该 CachedIntrospectionResults 类将始终接受来自以下 ClassLoaders 的类，即使这些类不符合缓存安全的条件。
     */
    static final Set<ClassLoader> acceptedClassLoaders = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /**
     * 以 Class 为键，包含 CachedIntrospectionResults 的 Map，持有力度强。
     * 这种变体正在被用于缓存安全的 Bean 类。
     */
    static final ConcurrentMap<Class<?>, CachedIntrospectionResults> strongClassCache = new ConcurrentHashMap<>(64);

    /**
     * 以 Class 为键，包含缓存的 CachedIntrospectionResults 的 Map，采用软引用持有。
     * 此变体用于非缓存安全的 bean 类。
     */
    static final ConcurrentMap<Class<?>, CachedIntrospectionResults> softClassCache = new ConcurrentReferenceHashMap<>(64);

    /**
     * 接受给定的 ClassLoader 作为缓存安全的，即使其类在此 CachedIntrospectionResults 类中不符合缓存安全的条件。
     * <p>此配置方法仅在 Spring 类位于一个“公共”ClassLoader（例如系统 ClassLoader）的场景中相关，该 ClassLoader 的生命周期与应用程序无关。在这种情况下，CachedIntrospectionResults 默认不会缓存应用程序的任何类，因为这些类会在公共 ClassLoader 中造成泄漏。
     * <p>在应用程序启动时对任何 {@code acceptClassLoader} 的调用应与在应用程序关闭时对 {@link #clearClassLoader} 的调用相匹配。
     * @param classLoader 要接受的 ClassLoader
     */
    public static void acceptClassLoader(@Nullable ClassLoader classLoader) {
        if (classLoader != null) {
            acceptedClassLoaders.add(classLoader);
        }
    }

    /**
     * 清除指定ClassLoader的反射缓存，移除该ClassLoader下所有类的反射结果，并从接受列表中移除ClassLoader（及其子类）。
     * @param classLoader 要清除缓存的目标ClassLoader
     */
    public static void clearClassLoader(@Nullable ClassLoader classLoader) {
        acceptedClassLoaders.removeIf(registeredLoader -> isUnderneathClassLoader(registeredLoader, classLoader));
        strongClassCache.keySet().removeIf(beanClass -> isUnderneathClassLoader(beanClass.getClassLoader(), classLoader));
        softClassCache.keySet().removeIf(beanClass -> isUnderneathClassLoader(beanClass.getClassLoader(), classLoader));
    }

    /**
     * 为给定的Bean类创建缓存的反射结果。
     * @param beanClass 要分析的Bean类
     * @return 相应的CachedIntrospectionResults
     * @throws BeansException 在反射失败的情况下抛出异常
     */
    static CachedIntrospectionResults forClass(Class<?> beanClass) throws BeansException {
        CachedIntrospectionResults results = strongClassCache.get(beanClass);
        if (results != null) {
            return results;
        }
        results = softClassCache.get(beanClass);
        if (results != null) {
            return results;
        }
        results = new CachedIntrospectionResults(beanClass);
        ConcurrentMap<Class<?>, CachedIntrospectionResults> classCacheToUse;
        if (ClassUtils.isCacheSafe(beanClass, CachedIntrospectionResults.class.getClassLoader()) || isClassLoaderAccepted(beanClass.getClassLoader())) {
            classCacheToUse = strongClassCache;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Not strongly caching class [" + beanClass.getName() + "] because it is not cache-safe");
            }
            classCacheToUse = softClassCache;
        }
        CachedIntrospectionResults existing = classCacheToUse.putIfAbsent(beanClass, results);
        return (existing != null ? existing : results);
    }

    /**
     * 检查此 CachedIntrospectionResults 类是否配置为接受给定的 ClassLoader。
     * @param classLoader 要检查的 ClassLoader
     * @return 是否接受给定的 ClassLoader
     * @see #acceptClassLoader
     */
    private static boolean isClassLoaderAccepted(ClassLoader classLoader) {
        for (ClassLoader acceptedLoader : acceptedClassLoaders) {
            if (isUnderneathClassLoader(classLoader, acceptedLoader)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查给定的类加载器是否位于给定的父类加载器之下，
     * 即父类加载器是否位于候选者的层次结构中。
     * @param candidate 需要检查的候选类加载器
     * @param parent 需要检查的父类加载器
     */
    private static boolean isUnderneathClassLoader(@Nullable ClassLoader candidate, @Nullable ClassLoader parent) {
        if (candidate == parent) {
            return true;
        }
        if (candidate == null) {
            return false;
        }
        ClassLoader classLoaderToCheck = candidate;
        while (classLoaderToCheck != null) {
            classLoaderToCheck = classLoaderToCheck.getParent();
            if (classLoaderToCheck == parent) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取给定目标类的 {@link BeanInfo} 描述符。
     * @param beanClass 要反射的目标类
     * @return 返回的结果 {@code BeanInfo} 描述符（从不为 {@code null}）
     * @throws IntrospectionException 从反射给定的 Bean 类时抛出
     */
    private static BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
        for (BeanInfoFactory beanInfoFactory : beanInfoFactories) {
            BeanInfo beanInfo = beanInfoFactory.getBeanInfo(beanClass);
            if (beanInfo != null) {
                return beanInfo;
            }
        }
        return simpleBeanInfoFactory.getBeanInfo(beanClass);
    }

    /**
     * 用于反射的 Bean 类的 BeanInfo 对象。
     */
    private final BeanInfo beanInfo;

    /**
     * 以属性名 String 为键的 PropertyDescriptor 对象。
     */
    private final Map<String, PropertyDescriptor> propertyDescriptors;

    /**
     * 以 PropertyDescriptor 为键的 TypeDescriptor 对象。
     */
    private final ConcurrentMap<PropertyDescriptor, TypeDescriptor> typeDescriptorCache;

    /**
     * 为指定的类创建一个新的 CachedIntrospectionResults 实例。
     * @param beanClass 要分析的 Bean 类
     * @throws BeansException 在内省失败的情况下抛出异常
     */
    private CachedIntrospectionResults(Class<?> beanClass) throws BeansException {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Getting BeanInfo for class [" + beanClass.getName() + "]");
            }
            this.beanInfo = getBeanInfo(beanClass);
            if (logger.isTraceEnabled()) {
                logger.trace("Caching PropertyDescriptors for class [" + beanClass.getName() + "]");
            }
            this.propertyDescriptors = new LinkedHashMap<>();
            Set<String> readMethodNames = new HashSet<>();
            // 这个调用很慢，所以我们只做一次。
            PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (Class.class == beanClass && !("name".equals(pd.getName()) || (pd.getName().endsWith("Name") && String.class == pd.getPropertyType()))) {
                    // 仅允许 Class 属性的所有名称变体
                    continue;
                }
                if (URL.class == beanClass && "content".equals(pd.getName())) {
                    // 仅允许 URL 属性自省，不允许内容解析
                    continue;
                }
                if (pd.getWriteMethod() == null && isInvalidReadOnlyPropertyType(pd.getPropertyType(), beanClass)) {
                    // 忽略只读属性，如ClassLoader - 没有必要将其绑定
                    continue;
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Found bean property '" + pd.getName() + "'" + (pd.getPropertyType() != null ? " of type [" + pd.getPropertyType().getName() + "]" : "") + (pd.getPropertyEditorClass() != null ? "; editor [" + pd.getPropertyEditorClass().getName() + "]" : ""));
                }
                pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
                this.propertyDescriptors.put(pd.getName(), pd);
                Method readMethod = pd.getReadMethod();
                if (readMethod != null) {
                    readMethodNames.add(readMethod.getName());
                }
            }
            // 显式检查实现接口中的setter/getter方法。
            // 特别是针对 Java 8 的默认方法...
            Class<?> currClass = beanClass;
            while (currClass != null && currClass != Object.class) {
                introspectInterfaces(beanClass, currClass, readMethodNames);
                currClass = currClass.getSuperclass();
            }
            // 检查是否存在无前缀的记录式访问器：例如 "lastName()"
            // - 访问器方法直接引用同名的实例字段
            // - Java 15 记录类组件访问器的相同约定
            introspectPlainAccessors(beanClass, readMethodNames);
            this.typeDescriptorCache = new ConcurrentReferenceHashMap<>();
        } catch (IntrospectionException ex) {
            throw new FatalBeanException("Failed to obtain BeanInfo for class [" + beanClass.getName() + "]", ex);
        }
    }

    private void introspectInterfaces(Class<?> beanClass, Class<?> currClass, Set<String> readMethodNames) throws IntrospectionException {
        for (Class<?> ifc : currClass.getInterfaces()) {
            if (!ClassUtils.isJavaLanguageInterface(ifc)) {
                for (PropertyDescriptor pd : getBeanInfo(ifc).getPropertyDescriptors()) {
                    PropertyDescriptor existingPd = this.propertyDescriptors.get(pd.getName());
                    if (existingPd == null || (existingPd.getReadMethod() == null && pd.getReadMethod() != null)) {
                        // GenericTypeAwarePropertyDescriptor 类可以宽容地解析 set* 写方法
                        // 与声明的读取方法相反，所以我们在这里更倾向于使用读取方法描述符。
                        pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
                        if (pd.getWriteMethod() == null && isInvalidReadOnlyPropertyType(pd.getPropertyType(), beanClass)) {
                            // 忽略只读属性，例如 ClassLoader - 没有必要将这些绑定
                            continue;
                        }
                        this.propertyDescriptors.put(pd.getName(), pd);
                        Method readMethod = pd.getReadMethod();
                        if (readMethod != null) {
                            readMethodNames.add(readMethod.getName());
                        }
                    }
                }
                introspectInterfaces(ifc, ifc, readMethodNames);
            }
        }
    }

    private void introspectPlainAccessors(Class<?> beanClass, Set<String> readMethodNames) throws IntrospectionException {
        for (Method method : beanClass.getMethods()) {
            if (!this.propertyDescriptors.containsKey(method.getName()) && !readMethodNames.contains(method.getName()) && isPlainAccessor(method)) {
                this.propertyDescriptors.put(method.getName(), new GenericTypeAwarePropertyDescriptor(beanClass, method.getName(), method, null, null));
                readMethodNames.add(method.getName());
            }
        }
    }

    private boolean isPlainAccessor(Method method) {
        if (Modifier.isStatic(method.getModifiers()) || method.getDeclaringClass() == Object.class || method.getDeclaringClass() == Class.class || method.getParameterCount() > 0 || method.getReturnType() == void.class || isInvalidReadOnlyPropertyType(method.getReturnType(), method.getDeclaringClass())) {
            return false;
        }
        try {
            // 访问器方法引用同名的实例字段？
            method.getDeclaringClass().getDeclaredField(method.getName());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isInvalidReadOnlyPropertyType(@Nullable Class<?> returnType, Class<?> beanClass) {
        return (returnType != null && (ClassLoader.class.isAssignableFrom(returnType) || ProtectionDomain.class.isAssignableFrom(returnType) || (AutoCloseable.class.isAssignableFrom(returnType) && !AutoCloseable.class.isAssignableFrom(beanClass))));
    }

    BeanInfo getBeanInfo() {
        return this.beanInfo;
    }

    Class<?> getBeanClass() {
        return this.beanInfo.getBeanDescriptor().getBeanClass();
    }

    @Nullable
    PropertyDescriptor getPropertyDescriptor(String name) {
        PropertyDescriptor pd = this.propertyDescriptors.get(name);
        if (pd == null && StringUtils.hasLength(name)) {
            // 与Property中的宽松回退检查相同
            pd = this.propertyDescriptors.get(StringUtils.uncapitalize(name));
            if (pd == null) {
                pd = this.propertyDescriptors.get(StringUtils.capitalize(name));
            }
        }
        return pd;
    }

    PropertyDescriptor[] getPropertyDescriptors() {
        return this.propertyDescriptors.values().toArray(PropertyDescriptorUtils.EMPTY_PROPERTY_DESCRIPTOR_ARRAY);
    }

    private PropertyDescriptor buildGenericTypeAwarePropertyDescriptor(Class<?> beanClass, PropertyDescriptor pd) {
        try {
            return new GenericTypeAwarePropertyDescriptor(beanClass, pd.getName(), pd.getReadMethod(), pd.getWriteMethod(), pd.getPropertyEditorClass());
        } catch (IntrospectionException ex) {
            throw new FatalBeanException("Failed to re-introspect class [" + beanClass.getName() + "]", ex);
        }
    }

    TypeDescriptor addTypeDescriptor(PropertyDescriptor pd, TypeDescriptor td) {
        TypeDescriptor existing = this.typeDescriptorCache.putIfAbsent(pd, td);
        return (existing != null ? existing : td);
    }

    @Nullable
    TypeDescriptor getTypeDescriptor(PropertyDescriptor pd) {
        return this.typeDescriptorCache.get(pd);
    }
}
