// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans;

import java.beans.ConstructorProperties;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KFunction;
import kotlin.reflect.KParameter;
import kotlin.reflect.full.KClasses;
import kotlin.reflect.jvm.KCallablesJvm;
import kotlin.reflect.jvm.ReflectJvmMapping;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 用于 JavaBeans 的静态便捷方法：用于实例化 Bean、检查 Bean 属性类型、复制 Bean 属性等。
 *
 * <p>主要用于框架内部的内部使用，但在一定程度上也对应用程序类有用。请考虑以下第三方框架：
 * <a href="https://commons.apache.org/proper/commons-beanutils/">Apache Commons BeanUtils</a>，
 * <a href="https://github.com/ExpediaGroup/bull">BULL - Bean Utils Light Library</a>，
 * 或类似的其他第三方框架以获取更全面的 Bean 工具集。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Sebastien Deleuze
 */
public abstract class BeanUtils {

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private static final Set<Class<?>> unknownEditorTypes = Collections.newSetFromMap(new ConcurrentReferenceHashMap<>(64));

    private static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES = Map.of(boolean.class, false, byte.class, (byte) 0, short.class, (short) 0, int.class, 0, long.class, 0L, float.class, 0F, double.class, 0D, char.class, '\0');

    /**
     * 使用无参构造函数实例化类的便捷方法。
     * @param clazz 要实例化的类
     * @return 新实例
     * @throws BeanInstantiationException 如果无法实例化bean
     * @see Class#newInstance()
     * @deprecated 自Spring 5.0起已弃用，随JDK 9中 {@link Class#newInstance()} 的弃用而弃用
     */
    @Deprecated
    public static <T> T instantiate(Class<T> clazz) throws BeanInstantiationException {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new BeanInstantiationException(clazz, "Specified class is an interface");
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException ex) {
            throw new BeanInstantiationException(clazz, "Is it an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new BeanInstantiationException(clazz, "Is the constructor accessible?", ex);
        }
    }

    /**
     * 使用类的“primary”构造函数实例化类（对于 Kotlin 类，可能声明了默认参数）或其默认构造函数（对于常规 Java 类，期望标准无参设置）。
     * <p>注意，此方法尝试将构造函数设置为可访问的，如果给定的是非可访问的（即非公共的）构造函数。
     * @param clazz 要实例化的类
     * @return 新的实例
     * @throws BeanInstantiationException 如果无法实例化 Bean。
     * 原因可能特别表明如果没有找到主/默认构造函数，则会出现一个 {@link NoSuchMethodException}，或者在无法解析类定义的情况下（例如，由于运行时缺少依赖项），会出现一个 {@link NoClassDefFoundError} 或其他 {@link LinkageError}，或者构造函数调用本身抛出的异常。
     * @see Constructor#newInstance
     */
    public static <T> T instantiateClass(Class<T> clazz) throws BeanInstantiationException {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new BeanInstantiationException(clazz, "Specified class is an interface");
        }
        Constructor<T> ctor;
        try {
            ctor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
            ctor = findPrimaryConstructor(clazz);
            if (ctor == null) {
                throw new BeanInstantiationException(clazz, "No default constructor found", ex);
            }
        } catch (LinkageError err) {
            throw new BeanInstantiationException(clazz, "Unresolvable class definition", err);
        }
        return instantiateClass(ctor);
    }

    /**
     * 使用无参构造函数实例化一个类，并返回新的实例作为指定的可赋值类型。
     * <p>在需要实例化的类的类型（clazz）不可用，但所需的类型（assignableTo）已知的情况下非常有用。
     * <p>注意，如果提供了非可访问的（即非公共的）构造函数，此方法会尝试设置构造函数的可访问性。
     * @param clazz 要实例化的类
     * @param assignableTo clazz必须可赋值给的类型
     * @return 新的实例
     * @throws BeanInstantiationException 如果无法实例化bean
     * @see Constructor#newInstance
     */
    @SuppressWarnings("unchecked")
    public static <T> T instantiateClass(Class<?> clazz, Class<T> assignableTo) throws BeanInstantiationException {
        Assert.isAssignable(assignableTo, clazz);
        return (T) instantiateClass(clazz);
    }

    /**
     * 方便方法，用于使用给定的构造函数实例化一个类。
     * <p>请注意，此方法在给定的构造函数非可访问（即非公共）时尝试设置构造函数的可访问性，并支持具有可选参数和默认值的 Kotlin 类。
     * @param ctor 要实例化的构造函数
     * @param args 应用到构造函数的参数（对于未指定的参数，可以使用 {@code null}，支持 Kotlin 可选参数和 Java 原始类型）
     * @return 新实例
     * @throws BeanInstantiationException 如果无法实例化 Bean
     * @see Constructor#newInstance
     */
    public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws BeanInstantiationException {
        Assert.notNull(ctor, "Constructor must not be null");
        try {
            ReflectionUtils.makeAccessible(ctor);
            if (KotlinDetector.isKotlinReflectPresent() && KotlinDetector.isKotlinType(ctor.getDeclaringClass())) {
                return KotlinDelegate.instantiateClass(ctor, args);
            } else {
                int parameterCount = ctor.getParameterCount();
                Assert.isTrue(args.length <= parameterCount, "Can't specify more arguments than constructor parameters");
                if (parameterCount == 0) {
                    return ctor.newInstance();
                }
                Class<?>[] parameterTypes = ctor.getParameterTypes();
                Object[] argsWithDefaultValues = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        Class<?> parameterType = parameterTypes[i];
                        argsWithDefaultValues[i] = (parameterType.isPrimitive() ? DEFAULT_TYPE_VALUES.get(parameterType) : null);
                    } else {
                        argsWithDefaultValues[i] = args[i];
                    }
                }
                return ctor.newInstance(argsWithDefaultValues);
            }
        } catch (InstantiationException ex) {
            throw new BeanInstantiationException(ctor, "Is it an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new BeanInstantiationException(ctor, "Is the constructor accessible?", ex);
        } catch (IllegalArgumentException ex) {
            throw new BeanInstantiationException(ctor, "Illegal arguments for constructor", ex);
        } catch (InvocationTargetException ex) {
            throw new BeanInstantiationException(ctor, "Constructor threw exception", ex.getTargetException());
        }
    }

    /**
     * 返回一个可解析的构造函数，对于提供的类，可以是带有参数的主构造函数或单公共构造函数，或者是一个带有参数的单非公共构造函数，或者只是一个默认构造函数。调用者必须准备好解析返回构造函数的参数，如果有的话。
     * @param clazz 要检查的类
     * @throws IllegalStateException 如果找不到任何唯一的构造函数时抛出
     * @since 5.3
     * @see #findPrimaryConstructor
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getResolvableConstructor(Class<T> clazz) {
        Constructor<T> ctor = findPrimaryConstructor(clazz);
        if (ctor != null) {
            return ctor;
        }
        Constructor<?>[] ctors = clazz.getConstructors();
        if (ctors.length == 1) {
            // 一个单独的公共构造函数
            return (Constructor<T>) ctors[0];
        } else if (ctors.length == 0) {
            // 没有公开的构造函数 -> 检查非公开的
            ctors = clazz.getDeclaredConstructors();
            if (ctors.length == 1) {
                // 一个单独的非公共构造函数，例如来自一个非公共记录类型
                return (Constructor<T>) ctors[0];
            }
        }
        // 几个构造函数 -> 让我们尝试使用默认构造函数
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
            // 放弃...
        }
        // 没有任何唯一构造函数
        throw new IllegalStateException("No primary or single unique constructor found for " + clazz);
    }

    /**
     * 返回提供的类的初级构造函数。对于 Kotlin 类，这返回与 Kotlin 初级构造函数相对应的 Java 构造函数（如 Kotlin 规范中定义）。对于非 Kotlin 类，这仅简单地返回 {@code null}。
     * @param clazz 要检查的类
     * @since 5.0
     * @see <a href="https://kotlinlang.org/docs/reference/classes.html#constructors">Kotlin 文档</a>
     */
    @Nullable
    public static <T> Constructor<T> findPrimaryConstructor(Class<T> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        if (KotlinDetector.isKotlinReflectPresent() && KotlinDetector.isKotlinType(clazz)) {
            return KotlinDelegate.findPrimaryConstructor(clazz);
        }
        return null;
    }

    /**
     * 查找一个给定方法名和给定参数类型的方法，
     * 该方法在给定的类或其超类中声明。优先考虑公共方法，
     * 但也会返回受保护的、包访问或私有方法。
     * <p>首先检查 {@code Class.getMethod}，然后回退到
     * {@code findDeclaredMethod}。这允许在具有受限Java安全设置的环境中
     * 无问题地找到公共方法。
     * @param clazz 要检查的类
     * @param methodName 要查找的方法名称
     * @param paramTypes 要查找的方法的参数类型
     * @return 方法对象，或未找到时返回 {@code null}
     * @see Class#getMethod
     * @see #findDeclaredMethod
     */
    @Nullable
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return findDeclaredMethod(clazz, methodName, paramTypes);
        }
    }

    /**
     * 查找一个给定方法名和给定参数类型的、在给定类或其超类中声明的方法。将返回一个公共的、受保护的、包访问或私有的方法。
     * <p>检查 {@code Class.getDeclaredMethod}，并向上级联到所有超类。
     * @param clazz 要检查的类
     * @param methodName 要查找的方法的名称
     * @param paramTypes 要查找的方法的参数类型
     * @return 方法对象，或找不到时返回 {@code null}
     * @see Class#getDeclaredMethod
     */
    @Nullable
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            if (clazz.getSuperclass() != null) {
                return findDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
            }
            return null;
        }
    }

    /**
     * 查找一个给定方法名和最少参数的方法（最佳情况：无参数），
     * 该方法声明在给定的类或其超类中。优先选择公共方法，
     * 但也会返回受保护的、包访问或私有方法。
     * <p>首先检查 {@code Class.getMethods}，然后回退到
     * {@code findDeclaredMethodWithMinimalParameters}。这允许在具有受限Java安全设置的环境中
     * 无问题地找到公共方法。
     * @param clazz 要检查的类
     * @param methodName 要查找的方法名
     * @return 方法对象，或如果未找到则返回 {@code null}
     * @throws IllegalArgumentException 如果找到了给定名称的方法，但无法解析为具有最少参数的唯一方法
     * @see Class#getMethods
     * @see #findDeclaredMethodWithMinimalParameters
     */
    @Nullable
    public static Method findMethodWithMinimalParameters(Class<?> clazz, String methodName) throws IllegalArgumentException {
        Method targetMethod = findMethodWithMinimalParameters(clazz.getMethods(), methodName);
        if (targetMethod == null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz, methodName);
        }
        return targetMethod;
    }

    /**
     * 查找给定类或其超类中具有给定方法名和最少参数（最佳情况：无）的方法。
     * 将返回公共、受保护、包访问或私有方法。
     * <p>检查 {@code Class.getDeclaredMethods}，并逐级向上到所有超类。
     * @param clazz 要检查的类
     * @param methodName 要查找的方法名
     * @return 返回 Method 对象，或如果未找到则返回 {@code null}
     * @throws IllegalArgumentException 如果找到了给定名称的方法，但无法解析为具有最少参数的唯一方法
     * @see Class#getDeclaredMethods
     */
    @Nullable
    public static Method findDeclaredMethodWithMinimalParameters(Class<?> clazz, String methodName) throws IllegalArgumentException {
        Method targetMethod = findMethodWithMinimalParameters(clazz.getDeclaredMethods(), methodName);
        if (targetMethod == null && clazz.getSuperclass() != null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz.getSuperclass(), methodName);
        }
        return targetMethod;
    }

    /**
     * 在给定方法列表中查找具有给定方法名和最少参数（最佳情况：无参数）的方法。
     * @param methods 要检查的方法列表
     * @param methodName 要查找的方法名称
     * @return 返回 Method 对象，或如果未找到则返回 {@code null}
     * @throws IllegalArgumentException 如果找到了给定名称的方法，但无法解析为具有最少参数的唯一方法
     */
    @Nullable
    public static Method findMethodWithMinimalParameters(Method[] methods, String methodName) throws IllegalArgumentException {
        Method targetMethod = null;
        int numMethodsFoundWithCurrentMinimumArgs = 0;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                int numParams = method.getParameterCount();
                if (targetMethod == null || numParams < targetMethod.getParameterCount()) {
                    targetMethod = method;
                    numMethodsFoundWithCurrentMinimumArgs = 1;
                } else if (!method.isBridge() && targetMethod.getParameterCount() == numParams) {
                    if (targetMethod.isBridge()) {
                        // 优先使用常规方法而非桥接方法...
                        targetMethod = method;
                    } else {
                        // 具有相同长度的额外候选者
                        numMethodsFoundWithCurrentMinimumArgs++;
                    }
                }
            }
        }
        if (numMethodsFoundWithCurrentMinimumArgs > 1) {
            throw new IllegalArgumentException("Cannot resolve method '" + methodName + "' to a unique method. Attempted to resolve to overloaded method with " + "the least number of parameters but there were " + numMethodsFoundWithCurrentMinimumArgs + " candidates.");
        }
        return targetMethod;
    }

    /**
     * 解析方法签名，格式为 {@code methodName[([arg_list])]}，
     * 其中 {@code arg_list} 是一个可选的、逗号分隔的完全限定类型名称列表，并尝试将此签名与提供的 {@code Class} 进行解析。
     * <p>当不提供参数列表时（仅 {@code methodName}），将返回名称匹配且参数数量最少的那个方法。当提供参数类型列表时，仅返回名称和参数类型都匹配的方法。
     * <p>请注意，与 {@code methodName()} 的解析方式不同。签名 {@code methodName} 表示调用名为 {@code methodName} 且参数数量最少的方法，而 {@code methodName()} 表示调用名为 {@code methodName} 且恰好有 0 个参数的方法。
     * <p>如果找不到任何方法，则返回 {@code null}。
     * @param signature 方法签名的字符串表示
     * @param clazz 用于解析方法签名的类
     * @return 解析后的 Method 对象
     * @see #findMethod
     * @see #findMethodWithMinimalParameters
     */
    @Nullable
    public static Method resolveSignature(String signature, Class<?> clazz) {
        Assert.hasText(signature, "'signature' must not be empty");
        Assert.notNull(clazz, "Class must not be null");
        int startParen = signature.indexOf('(');
        int endParen = signature.indexOf(')');
        if (startParen > -1 && endParen == -1) {
            throw new IllegalArgumentException("Invalid method signature '" + signature + "': expected closing ')' for args list");
        } else if (startParen == -1 && endParen > -1) {
            throw new IllegalArgumentException("Invalid method signature '" + signature + "': expected opening '(' for args list");
        } else if (startParen == -1) {
            return findMethodWithMinimalParameters(clazz, signature);
        } else {
            String methodName = signature.substring(0, startParen);
            String[] parameterTypeNames = StringUtils.commaDelimitedListToStringArray(signature.substring(startParen + 1, endParen));
            Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
            for (int i = 0; i < parameterTypeNames.length; i++) {
                String parameterTypeName = parameterTypeNames[i].trim();
                try {
                    parameterTypes[i] = ClassUtils.forName(parameterTypeName, clazz.getClassLoader());
                } catch (Throwable ex) {
                    throw new IllegalArgumentException("Invalid method signature: unable to resolve type [" + parameterTypeName + "] for argument " + i + ". Root cause: " + ex);
                }
            }
            return findMethod(clazz, methodName, parameterTypes);
        }
    }

    /**
     * 获取给定类的 JavaBeans {@code PropertyDescriptor}。
     * @param clazz 要获取 PropertyDescriptors 的类
     * @return 给定类的 {@code PropertyDescriptor} 数组
     * @throws BeansException 如果 PropertyDescriptor 查找失败
     */
    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws BeansException {
        return CachedIntrospectionResults.forClass(clazz).getPropertyDescriptors();
    }

    /**
     * 获取给定属性的 JavaBeans {@code PropertyDescriptors}。
     * @param clazz 要获取 PropertyDescriptor 的 Class
     * @param propertyName 属性的名称
     * @return 对应的 PropertyDescriptor，如果没有则返回 {@code null}
     * @throws BeansException 如果 PropertyDescriptor 查找失败
     */
    @Nullable
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) throws BeansException {
        return CachedIntrospectionResults.forClass(clazz).getPropertyDescriptor(propertyName);
    }

    /**
     * 查找给定方法的 JavaBeans {@code PropertyDescriptor}，
     * 该方法可以是该 bean 属性的读取方法或写入方法。
     * @param method 要查找相应 PropertyDescriptor 的方法，
     * 通过检查其声明类进行反射
     * @return 相应的 PropertyDescriptor，如果没有则返回 {@code null}
     * @throws BeansException 如果 PropertyDescriptor 查找失败
     */
    @Nullable
    public static PropertyDescriptor findPropertyForMethod(Method method) throws BeansException {
        return findPropertyForMethod(method, method.getDeclaringClass());
    }

    /**
     * 为给定方法查找一个 JavaBeans 的 {@code PropertyDescriptor}，
     * 该方法可以是该 Bean 属性的读取方法或写入方法。
     * @param method 要查找对应 PropertyDescriptor 的方法
     * @param clazz 用于反查描述符的（最具体的）类
     * @return 对应的 PropertyDescriptor，如果没有则返回 {@code null}
     * @throws BeansException 如果 PropertyDescriptor 查找失败
     * @since 3.2.13
     */
    @Nullable
    public static PropertyDescriptor findPropertyForMethod(Method method, Class<?> clazz) throws BeansException {
        Assert.notNull(method, "Method must not be null");
        PropertyDescriptor[] pds = getPropertyDescriptors(clazz);
        for (PropertyDescriptor pd : pds) {
            if (method.equals(pd.getReadMethod()) || method.equals(pd.getWriteMethod())) {
                return pd;
            }
        }
        return null;
    }

    /**
     * 查找一个遵循 'Editor' 后缀约定的 JavaBeans PropertyEditor
     * （例如，"mypackage.MyDomainClass" &rarr; "mypackage.MyDomainClassEditor"）。
     * <p>与通过 {@link java.beans.PropertyEditorManager} 实现的标准 JavaBeans 约定兼容，
     * 但与后者注册的默认原始类型编辑器相隔离。
     * @param targetType 要查找编辑器的类型
     * @return 对应的编辑器，如果没有找到则返回 {@code null}
     */
    @Nullable
    public static PropertyEditor findEditorByConvention(@Nullable Class<?> targetType) {
        if (targetType == null || targetType.isArray() || unknownEditorTypes.contains(targetType)) {
            return null;
        }
        ClassLoader cl = targetType.getClassLoader();
        if (cl == null) {
            try {
                cl = ClassLoader.getSystemClassLoader();
                if (cl == null) {
                    return null;
                }
            } catch (Throwable ex) {
                // 例如，在 Google App Engine 上抛出 AccessControlException 异常。
                return null;
            }
        }
        String targetTypeName = targetType.getName();
        String editorName = targetTypeName + "Editor";
        try {
            Class<?> editorClass = cl.loadClass(editorName);
            if (editorClass != null) {
                if (!PropertyEditor.class.isAssignableFrom(editorClass)) {
                    unknownEditorTypes.add(targetType);
                    return null;
                }
                return (PropertyEditor) instantiateClass(editorClass);
            }
            // 不合规的 ClassLoader 返回了 null 而不是 ClassNotFoundException
            // - 回退到以下未知的编辑器类型注册
        } catch (ClassNotFoundException ex) {
            // 忽略 - 在下面回退到未知编辑器类型注册
        }
        unknownEditorTypes.add(targetType);
        return null;
    }

    /**
     * 确定给定属性从给定类/接口中的属性类型，如果可能的话。
     * @param propertyName bean属性的名称
     * @param beanClasses 要检查的类
     * @return 属性类型，或者在失败的情况下返回 {@code Object.class} 作为后备
     */
    public static Class<?> findPropertyType(String propertyName, @Nullable Class<?>... beanClasses) {
        if (beanClasses != null) {
            for (Class<?> beanClass : beanClasses) {
                PropertyDescriptor pd = getPropertyDescriptor(beanClass, propertyName);
                if (pd != null) {
                    return pd.getPropertyType();
                }
            }
        }
        return Object.class;
    }

    /**
     * 获取指定属性的write方法的新的MethodParameter对象。
     * @param pd 属性的PropertyDescriptor
     * @return 相应的MethodParameter对象
     */
    public static MethodParameter getWriteMethodParameter(PropertyDescriptor pd) {
        if (pd instanceof GenericTypeAwarePropertyDescriptor gpd) {
            return new MethodParameter(gpd.getWriteMethodParameter());
        } else {
            Method writeMethod = pd.getWriteMethod();
            Assert.state(writeMethod != null, "No write method available");
            return new MethodParameter(writeMethod, 0);
        }
    }

    /**
     * 确定给定构造函数所需的参数名称，
     * 考虑到 JavaBeans 的 {@link ConstructorProperties} 注解以及 Spring 的 {@link DefaultParameterNameDiscoverer}。
     * @param ctor 要查找参数名称的构造函数
     * @return 参数名称（与构造函数的参数数量匹配）
     * @throws IllegalStateException 如果无法解析参数名称
     * @since 5.3
     * @see ConstructorProperties
     * @see DefaultParameterNameDiscoverer
     */
    public static String[] getParameterNames(Constructor<?> ctor) {
        ConstructorProperties cp = ctor.getAnnotation(ConstructorProperties.class);
        String[] paramNames = (cp != null ? cp.value() : parameterNameDiscoverer.getParameterNames(ctor));
        Assert.state(paramNames != null, () -> "Cannot resolve parameter names for constructor " + ctor);
        Assert.state(paramNames.length == ctor.getParameterCount(), () -> "Invalid number of parameter names: " + paramNames.length + " for constructor " + ctor);
        return paramNames;
    }

    /**
     * 检查给定的类型是否表示一个“简单”属性：一个简单值类型或简单值类型的数组。
     * <p>有关“简单值类型”的定义，请参阅{@link #isSimpleValueType(Class)}。
     * <p>用于确定需要检查“简单”依赖关系的属性。
     * @param type 要检查的类型
     * @return 给定的类型是否表示一个“简单”属性
     * @see org.springframework.beans.factory.support.RootBeanDefinition#DEPENDENCY_CHECK_SIMPLE
     * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#checkDependencies
     * @see #isSimpleValueType(Class)
     */
    public static boolean isSimpleProperty(Class<?> type) {
        Assert.notNull(type, "'type' must not be null");
        return isSimpleValueType(type) || (type.isArray() && isSimpleValueType(type.getComponentType()));
    }

    /**
     *  检查给定的类型是否代表一个“简单”的值类型：原始数据类型或原始数据类型的包装类，枚举，String或其它CharSequence，Number，Date，Temporal，URI，URL，Locale或Class。
     * 	<p>{@code Void}和{@code void}不被认为是简单值类型。
     * 	@param type 要检查的类型
     * 	@return 给定的类型是否代表一个“简单”的值类型
     * 	@see #isSimpleProperty(Class)
     */
    public static boolean isSimpleValueType(Class<?> type) {
        return (Void.class != type && void.class != type && (ClassUtils.isPrimitiveOrWrapper(type) || Enum.class.isAssignableFrom(type) || CharSequence.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type) || URI.class == type || URL.class == type || Locale.class == type || Class.class == type));
    }

    /**
     * 将给定源 Bean 的属性值复制到目标 Bean 中。
     * <p>注意：源类和目标类不必匹配，甚至不必相互继承，只要属性匹配即可。源 Bean 暴露的任何属性，而目标 Bean 没有对应的属性，将被静默忽略。
     * <p>这是一个便利方法。对于更复杂的传输需求，请考虑使用完整的 {@link BeanWrapper}。
     * <p>从 Spring 框架 5.3 开始，此方法在匹配源对象和目标对象的属性时，会尊重泛型类型信息。
     * <p>以下表格提供了一个非详尽的示例，说明可以复制的源和目标属性类型以及无法复制的源和目标属性类型。
     * <table border="1">
     * <tr><th>源属性类型</th><th>目标属性类型</th><th>复制是否支持</th></tr>
     * <tr><td>{@code Integer}</td><td>{@code Integer}</td><td>是</td></tr>
     * <tr><td>{@code Integer}</td><td>{@code Number}</td><td>是</td></tr>
     * <tr><td>{@code List<Integer>}</td><td>{@code List<Integer>}</td><td>是</td></tr>
     * <tr><td>{@code List<?>}</td><td>{@code List<?>}</td><td>是</td></tr>
     * <tr><td>{@code List<Integer>}</td><td>{@code List<?>}</td><td>是</td></tr>
     * <tr><td>{@code List<Integer>}</td><td>{@code List<? extends Number>}</td><td>是</td></tr>
     * <tr><td>{@code String}</td><td>{@code Integer}</td><td>否</td></tr>
     * <tr><td>{@code Number}</td><td>{@code Integer}</td><td>否</td></tr>
     * <tr><td>{@code List<Integer>}</td><td>{@code List<Long>}</td><td>否</td></tr>
     * <tr><td>{@code List<Integer>}</td><td>{@code List<Number>}</td><td>否</td></tr>
     * </table>
     * @param source 源 Bean
     * @param target 目标 Bean
     * @throws BeansException 如果复制失败
     * @see BeanWrapper
     */
    public static void copyProperties(Object source, Object target) throws BeansException {
        copyProperties(source, target, null, (String[]) null);
    }

    /**
     * 将给定源bean的属性值复制到给定的目标bean中，仅设置在给定的"可编辑"类（或接口）中定义的属性。
     * <p>注意：源类和目标类不必匹配，甚至不必相互派生，只要属性匹配即可。源bean公开但目标bean未定义的任何bean属性都将被静默忽略。
     * <p>这是一个便利方法。对于更复杂的传输需求，请考虑使用完整的{@link BeanWrapper}。
     * <p>从Spring Framework 5.3开始，此方法在匹配源对象和目标对象中的属性时尊重泛型类型信息。有关详细信息，请参阅{@link #copyProperties(Object, Object)}的文档。
     * @param source 源bean
     * @param target 目标bean
     * @param editable 限制属性设置的类（或接口）
     * @throws BeansException 如果复制失败
     * @see BeanWrapper
     */
    public static void copyProperties(Object source, Object target, Class<?> editable) throws BeansException {
        copyProperties(source, target, editable, (String[]) null);
    }

    /**
     * 将给定源bean的属性值复制到给定目标bean中，
     * 忽略给定的"ignoreProperties"属性。
     * <p>注意：源类和目标类不必匹配，甚至不必相互派生，只要属性匹配即可。任何源bean公开但目标bean没有的bean属性将被静默忽略。
     * <p>这是一个便利方法。对于更复杂的传输需求，请考虑使用完整的{@link BeanWrapper}。
     * <p>自Spring Framework 5.3起，此方法在匹配源对象和目标对象的属性时尊重泛型类型信息。有关详细信息，请参阅{@link #copyProperties(Object, Object)}的文档。
     * @param source 源bean
     * @param target 目标bean
     * @param ignoreProperties 要忽略的属性名称数组
     * @throws BeansException 如果复制失败
     * @see BeanWrapper
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) throws BeansException {
        copyProperties(source, target, null, ignoreProperties);
    }

    /**
     * 将给定源bean的属性值复制到给定的目标bean中。
     * <p>注意：源类和目标类不必匹配，甚至不必相互派生，只要属性匹配即可。源bean公开但目标bean没有的任何bean属性都将被静默忽略。
     * <p>从Spring Framework 5.3开始，此方法在匹配源对象和目标对象的属性时尊重泛型类型信息。有关详细信息，请参阅{@link #copyProperties(Object, Object)}的文档。
     * @param source 源bean
     * @param target 目标bean
     * @param editable 限制属性设置的类（或接口）
     * @param ignoreProperties 要忽略的属性名数组
     * @throws BeansException 如果复制失败
     * @see BeanWrapper
     */
    private static void copyProperties(Object source, Object target, @Nullable Class<?> editable, @Nullable String... ignoreProperties) throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");
        Class<?> actualEditable = target.getClass();
        if (editable != null) {
            if (!editable.isInstance(target)) {
                throw new IllegalArgumentException("Target class [" + target.getClass().getName() + "] not assignable to editable class [" + editable.getName() + "]");
            }
            actualEditable = editable;
        }
        PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
        Set<String> ignoredProps = (ignoreProperties != null ? new HashSet<>(Arrays.asList(ignoreProperties)) : null);
        CachedIntrospectionResults sourceResults = (actualEditable != source.getClass() ? CachedIntrospectionResults.forClass(source.getClass()) : null);
        for (PropertyDescriptor targetPd : targetPds) {
            Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod != null && (ignoredProps == null || !ignoredProps.contains(targetPd.getName()))) {
                PropertyDescriptor sourcePd = (sourceResults != null ? sourceResults.getPropertyDescriptor(targetPd.getName()) : targetPd);
                if (sourcePd != null) {
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null) {
                        if (isAssignable(writeMethod, readMethod)) {
                            try {
                                ReflectionUtils.makeAccessible(readMethod);
                                Object value = readMethod.invoke(source);
                                ReflectionUtils.makeAccessible(writeMethod);
                                writeMethod.invoke(target, value);
                            } catch (Throwable ex) {
                                throw new FatalBeanException("Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isAssignable(Method writeMethod, Method readMethod) {
        Type paramType = writeMethod.getGenericParameterTypes()[0];
        if (paramType instanceof Class<?> clazz) {
            return ClassUtils.isAssignable(clazz, readMethod.getReturnType());
        } else if (paramType.equals(readMethod.getGenericReturnType())) {
            return true;
        } else {
            ResolvableType sourceType = ResolvableType.forMethodReturnType(readMethod);
            ResolvableType targetType = ResolvableType.forMethodParameter(writeMethod, 0);
            // 如果任一ResolvableType具有无法解析的泛型，则在可赋值检查中忽略泛型类型。
            return (sourceType.hasUnresolvableGenerics() || targetType.hasUnresolvableGenerics() ? ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType()) : targetType.isAssignableFrom(sourceType));
        }
    }

    /**
     * 内部类，用于避免在运行时对 Kotlin 的硬依赖。
     */
    private static class KotlinDelegate {

        /**
         * 获取与 Kotlin 主构造函数对应的 Java 构造函数（如果存在）。
         * @param clazz Kotlin 类的 {@link Class}
         * @see <a href="https://kotlinlang.org/docs/reference/classes.html#constructors">
         * https://kotlinlang.org/docs/reference/classes.html#constructors</a>
         */
        @Nullable
        public static <T> Constructor<T> findPrimaryConstructor(Class<T> clazz) {
            try {
                KFunction<T> primaryCtor = KClasses.getPrimaryConstructor(JvmClassMappingKt.getKotlinClass(clazz));
                if (primaryCtor == null) {
                    return null;
                }
                Constructor<T> constructor = ReflectJvmMapping.getJavaConstructor(primaryCtor);
                if (constructor == null) {
                    throw new IllegalStateException("Failed to find Java constructor for Kotlin primary constructor: " + clazz.getName());
                }
                return constructor;
            } catch (UnsupportedOperationException ex) {
                return null;
            }
        }

        /**
         * 使用提供的构造函数实例化一个 Kotlin 类。
         * @param ctor 要实例化的 Kotlin 类的构造函数
         * @param args 要应用的构造函数参数
         * （如果需要，可以使用 {@code null} 表示未指定的参数）
         */
        public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
            KFunction<T> kotlinConstructor = ReflectJvmMapping.getKotlinFunction(ctor);
            if (kotlinConstructor == null) {
                return ctor.newInstance(args);
            }
            if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers()))) {
                KCallablesJvm.setAccessible(kotlinConstructor, true);
            }
            List<KParameter> parameters = kotlinConstructor.getParameters();
            Assert.isTrue(args.length <= parameters.size(), "Number of provided arguments must be less than or equal to the number of constructor parameters");
            if (parameters.isEmpty()) {
                return kotlinConstructor.call();
            }
            Map<KParameter, Object> argParameters = CollectionUtils.newHashMap(parameters.size());
            for (int i = 0; i < args.length; i++) {
                if (!(parameters.get(i).isOptional() && args[i] == null)) {
                    argParameters.put(parameters.get(i), args[i]);
                }
            }
            return kotlinConstructor.callBy(argParameters);
        }
    }
}
