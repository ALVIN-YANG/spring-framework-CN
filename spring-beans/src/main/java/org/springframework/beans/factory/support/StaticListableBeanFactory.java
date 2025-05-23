// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.core.OrderComparator;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 静态的 {@link org.springframework.beans.factory.BeanFactory} 实现
 * 允许通过程序方式注册现有的单例实例。
 *
 * <p>不支持原型bean或别名。
 *
 * <p>作为实现 {@link org.springframework.beans.factory.ListableBeanFactory} 接口的一个简单示例，
 * 管理现有的bean实例，而不是基于bean定义创建新的实例，并且不实现任何扩展的SPI接口（例如，
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}）。
 *
 * <p>对于基于bean定义的完整工厂，请参考 {@link DefaultListableBeanFactory}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 06.01.2003
 * @see DefaultListableBeanFactory
 */
public class StaticListableBeanFactory implements ListableBeanFactory {

    /**
     * 将实体名称映射到实体实例的映射。
     */
    private final Map<String, Object> beans;

    /**
     * 创建一个常规的 {@code StaticListableBeanFactory}，将通过调用 {@link #addBean} 方法来填充单例bean实例。
     */
    public StaticListableBeanFactory() {
        this.beans = new LinkedHashMap<>();
    }

    /**
     * 创建一个包装给定 {@code Map} 的 {@code StaticListableBeanFactory}。
     * <p>注意，给定的 {@code Map} 可能已经预填充了bean；或者新的，仍然允许通过 {@link #addBean} 注册bean；或者使用 {@link java.util.Collections#emptyMap()} 创建一个虚拟工厂，该工厂强制对空集合的bean进行操作。
     * @param beans 一个用于存储此工厂bean的 {@code Map}，其中bean名称作为键，相应的单例对象作为值
     * @since 4.3
     */
    public StaticListableBeanFactory(Map<String, Object> beans) {
        Assert.notNull(beans, "Beans Map must not be null");
        this.beans = beans;
    }

    /**
     * 添加一个新的单例bean。
     * <p>将覆盖给定名称的任何现有实例。
     * @param name bean的名称
     * @param bean bean实例
     */
    public void addBean(String name, Object bean) {
        this.beans.put(name, bean);
    }

    // 很抱歉，您只提供了代码注释的分割线“---------------------------------------------------------------------”，没有提供实际的代码注释内容。请提供需要翻译的代码注释，我将为您翻译成中文。
    // BeanFactory接口的实现
    // 由于您提供的代码注释内容是空的（只有一个空行），我无法进行翻译。请提供实际的英文代码注释内容，我将为您翻译成中文。
    @Override
    public Object getBean(String name) throws BeansException {
        String beanName = BeanFactoryUtils.transformedBeanName(name);
        Object bean = this.beans.get(beanName);
        if (bean == null) {
            throw new NoSuchBeanDefinitionException(beanName, "Defined beans are [" + StringUtils.collectionToCommaDelimitedString(this.beans.keySet()) + "]");
        }
        // 不要让调用代码尝试取消引用
        // 如果该Bean不是一个工厂，则使用Bean工厂
        if (BeanFactoryUtils.isFactoryDereference(name) && !(bean instanceof FactoryBean)) {
            throw new BeanIsNotAFactoryException(beanName, bean.getClass());
        }
        if (bean instanceof FactoryBean<?> factoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
            try {
                Object exposedObject = factoryBean.getObject();
                if (exposedObject == null) {
                    throw new BeanCreationException(beanName, "FactoryBean exposed null object");
                }
                return exposedObject;
            } catch (Exception ex) {
                throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
            }
        } else {
            return bean;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, @Nullable Class<T> requiredType) throws BeansException {
        Object bean = getBean(name);
        if (requiredType != null && !requiredType.isInstance(bean)) {
            throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
        }
        return (T) bean;
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        if (!ObjectUtils.isEmpty(args)) {
            throw new UnsupportedOperationException("StaticListableBeanFactory does not support explicit bean creation arguments");
        }
        return getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        String[] beanNames = getBeanNamesForType(requiredType);
        if (beanNames.length == 1) {
            return getBean(beanNames[0], requiredType);
        } else if (beanNames.length > 1) {
            throw new NoUniqueBeanDefinitionException(requiredType, beanNames);
        } else {
            throw new NoSuchBeanDefinitionException(requiredType);
        }
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        if (!ObjectUtils.isEmpty(args)) {
            throw new UnsupportedOperationException("StaticListableBeanFactory does not support explicit bean creation arguments");
        }
        return getBean(requiredType);
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) throws BeansException {
        return getBeanProvider(ResolvableType.forRawClass(requiredType), true);
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
        return getBeanProvider(requiredType, true);
    }

    @Override
    public boolean containsBean(String name) {
        return this.beans.containsKey(name);
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        Object bean = getBean(name);
        // 如果是在 FactoryBean 的情况下，返回创建的对象的单例状态。
        if (bean instanceof FactoryBean<?> factoryBean) {
            return factoryBean.isSingleton();
        }
        return true;
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        Object bean = getBean(name);
        // 在 FactoryBean 的情况下，返回创建的对象的原型状态。
        return ((bean instanceof SmartFactoryBean<?> smartFactoryBean && smartFactoryBean.isPrototype()) || (bean instanceof FactoryBean<?> factoryBean && !factoryBean.isSingleton()));
    }

    @Override
    public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        Class<?> type = getType(name);
        return (type != null && typeToMatch.isAssignableFrom(type));
    }

    @Override
    public boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        Class<?> type = getType(name);
        return (typeToMatch == null || (type != null && typeToMatch.isAssignableFrom(type)));
    }

    @Override
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return getType(name, true);
    }

    @Override
    public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        String beanName = BeanFactoryUtils.transformedBeanName(name);
        Object bean = this.beans.get(beanName);
        if (bean == null) {
            throw new NoSuchBeanDefinitionException(beanName, "Defined beans are [" + StringUtils.collectionToCommaDelimitedString(this.beans.keySet()) + "]");
        }
        if (bean instanceof FactoryBean<?> factoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
            // 如果它是一个FactoryBean，我们希望查看它所创建的对象，而不是工厂类。
            return factoryBean.getObjectType();
        }
        return bean.getClass();
    }

    @Override
    public String[] getAliases(String name) {
        return new String[0];
    }

    // 由于您提供的代码注释内容为空，我无法进行翻译。请提供具体的 Java 代码注释内容，我将为您翻译成中文。
    // ListableBeanFactory 接口的实现
    // 请提供需要翻译的 Java 代码注释内容，这样我才能进行准确的翻译。
    @Override
    public boolean containsBeanDefinition(String name) {
        return this.beans.containsKey(name);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beans.size();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return StringUtils.toStringArray(this.beans.keySet());
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
        return getBeanProvider(ResolvableType.forRawClass(requiredType), allowEagerInit);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {
        return new ObjectProvider<>() {

            @Override
            public T getObject() throws BeansException {
                String[] beanNames = getBeanNamesForType(requiredType);
                if (beanNames.length == 1) {
                    return (T) getBean(beanNames[0], requiredType);
                } else if (beanNames.length > 1) {
                    throw new NoUniqueBeanDefinitionException(requiredType, beanNames);
                } else {
                    throw new NoSuchBeanDefinitionException(requiredType);
                }
            }

            @Override
            public T getObject(Object... args) throws BeansException {
                String[] beanNames = getBeanNamesForType(requiredType);
                if (beanNames.length == 1) {
                    return (T) getBean(beanNames[0], args);
                } else if (beanNames.length > 1) {
                    throw new NoUniqueBeanDefinitionException(requiredType, beanNames);
                } else {
                    throw new NoSuchBeanDefinitionException(requiredType);
                }
            }

            @Override
            @Nullable
            public T getIfAvailable() throws BeansException {
                String[] beanNames = getBeanNamesForType(requiredType);
                if (beanNames.length == 1) {
                    return (T) getBean(beanNames[0]);
                } else if (beanNames.length > 1) {
                    throw new NoUniqueBeanDefinitionException(requiredType, beanNames);
                } else {
                    return null;
                }
            }

            @Override
            @Nullable
            public T getIfUnique() throws BeansException {
                String[] beanNames = getBeanNamesForType(requiredType);
                if (beanNames.length == 1) {
                    return (T) getBean(beanNames[0]);
                } else {
                    return null;
                }
            }

            @Override
            public Stream<T> stream() {
                return Arrays.stream(getBeanNamesForType(requiredType)).map(name -> (T) getBean(name));
            }

            @Override
            public Stream<T> orderedStream() {
                return stream().sorted(OrderComparator.INSTANCE);
            }
        };
    }

    @Override
    public String[] getBeanNamesForType(@Nullable ResolvableType type) {
        return getBeanNamesForType(type, true, true);
    }

    @Override
    public String[] getBeanNamesForType(@Nullable ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
        Class<?> resolved = (type != null ? type.resolve() : null);
        boolean isFactoryType = resolved != null && FactoryBean.class.isAssignableFrom(resolved);
        List<String> matches = new ArrayList<>();
        for (Map.Entry<String, Object> entry : this.beans.entrySet()) {
            String beanName = entry.getKey();
            Object beanInstance = entry.getValue();
            if (beanInstance instanceof FactoryBean<?> factoryBean && !isFactoryType) {
                Class<?> objectType = factoryBean.getObjectType();
                if ((includeNonSingletons || factoryBean.isSingleton()) && objectType != null && (type == null || type.isAssignableFrom(objectType))) {
                    matches.add(beanName);
                }
            } else {
                if (type == null || type.isInstance(beanInstance)) {
                    matches.add(beanName);
                }
            }
        }
        return StringUtils.toStringArray(matches);
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type) {
        return getBeanNamesForType(ResolvableType.forClass(type));
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        return getBeanNamesForType(ResolvableType.forClass(type), includeNonSingletons, allowEagerInit);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
        return getBeansOfType(type, true, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
        boolean isFactoryType = (type != null && FactoryBean.class.isAssignableFrom(type));
        Map<String, T> matches = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : this.beans.entrySet()) {
            String beanName = entry.getKey();
            Object beanInstance = entry.getValue();
            // 这个 Bean 是 FactoryBean 吗？
            if (beanInstance instanceof FactoryBean<?> factoryBean && !isFactoryType) {
                // 由FactoryBean创建的匹配对象。
                Class<?> objectType = factoryBean.getObjectType();
                if ((includeNonSingletons || factoryBean.isSingleton()) && objectType != null && (type == null || type.isAssignableFrom(objectType))) {
                    matches.put(beanName, getBean(beanName, type));
                }
            } else {
                if (type == null || type.isInstance(beanInstance)) {
                    // 如果匹配的类型是 FactoryBean，则返回 FactoryBean 本身。
                    // 否则，返回 Bean 实例。
                    if (isFactoryType) {
                        beanName = FACTORY_BEAN_PREFIX + beanName;
                    }
                    matches.put(beanName, (T) beanInstance);
                }
            }
        }
        return matches;
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        List<String> results = new ArrayList<>();
        for (String beanName : this.beans.keySet()) {
            if (findAnnotationOnBean(beanName, annotationType) != null) {
                results.add(beanName);
            }
        }
        return StringUtils.toStringArray(results);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
        Map<String, Object> results = new LinkedHashMap<>();
        for (String beanName : this.beans.keySet()) {
            if (findAnnotationOnBean(beanName, annotationType) != null) {
                results.put(beanName, getBean(beanName));
            }
        }
        return results;
    }

    @Override
    @Nullable
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
        return findAnnotationOnBean(beanName, annotationType, true);
    }

    @Override
    @Nullable
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        Class<?> beanType = getType(beanName, allowFactoryBeanInit);
        return (beanType != null ? AnnotatedElementUtils.findMergedAnnotation(beanType, annotationType) : null);
    }

    @Override
    public <A extends Annotation> Set<A> findAllAnnotationsOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        Class<?> beanType = getType(beanName, allowFactoryBeanInit);
        return (beanType != null ? AnnotatedElementUtils.findAllMergedAnnotations(beanType, annotationType) : Collections.emptySet());
    }
}
