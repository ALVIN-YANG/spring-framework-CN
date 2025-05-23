// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 用于在Bean工厂实现中使用的辅助类，
 * 将包含在Bean定义对象中的值解析为应用于目标Bean实例的实际值。
 *
 * <p>在{@link AbstractBeanFactory}和一个普通的
 * {@link org.springframework.beans.factory.config.BeanDefinition}对象上操作。
 * 由{@link AbstractAutowireCapableBeanFactory}使用。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 1.2
 * @see AbstractAutowireCapableBeanFactory
 */
public class BeanDefinitionValueResolver {

    private final AbstractAutowireCapableBeanFactory beanFactory;

    private final String beanName;

    private final BeanDefinition beanDefinition;

    private final TypeConverter typeConverter;

    /**
     * 为给定的 BeanFactory 和 BeanDefinition 创建一个 BeanDefinitionValueResolver，
     * 使用给定的 {@link TypeConverter}。
     * @param beanFactory 要解析的 BeanFactory
     * @param beanName 我们正在处理的 bean 的名称
     * @param beanDefinition 我们正在处理的 bean 的 BeanDefinition
     * @param typeConverter 用于解析 TypedStringValues 的 TypeConverter
     */
    public BeanDefinitionValueResolver(AbstractAutowireCapableBeanFactory beanFactory, String beanName, BeanDefinition beanDefinition, TypeConverter typeConverter) {
        this.beanFactory = beanFactory;
        this.beanName = beanName;
        this.beanDefinition = beanDefinition;
        this.typeConverter = typeConverter;
    }

    /**
     * 为给定的 BeanFactory 和 BeanDefinition 创建一个 BeanDefinitionValueResolver，使用默认的 {@link TypeConverter}。
     * @param beanFactory 要解析的 BeanFactory
     * @param beanName 我们正在处理的 bean 的名称
     * @param beanDefinition 我们正在处理的 bean 的 BeanDefinition
     */
    public BeanDefinitionValueResolver(AbstractAutowireCapableBeanFactory beanFactory, String beanName, BeanDefinition beanDefinition) {
        this.beanFactory = beanFactory;
        this.beanName = beanName;
        this.beanDefinition = beanDefinition;
        BeanWrapper beanWrapper = new BeanWrapperImpl();
        beanFactory.initBeanWrapper(beanWrapper);
        this.typeConverter = beanWrapper;
    }

    /**
     * 给定一个属性值，返回一个值，如果需要的话，解析工厂中其他bean的引用。这个值可能是：
     * <li>一个BeanDefinition，这会导致创建相应的新的bean实例。单例标志和此类“内部bean”的名称总是被忽略：内部bean是匿名原型。
     * <li>一个RuntimeBeanReference，必须进行解析。
     * <li>一个ManagedList。这是一个特殊的集合，可能包含需要解析的RuntimeBeanReferences或Collections。
     * <li>一个ManagedSet。也可能包含需要解析的RuntimeBeanReferences或Collections。
     * <li>一个ManagedMap。在这种情况下，值可能是一个需要解析的RuntimeBeanReference或Collection。
     * <li>一个普通对象或null，在这种情况下，它将被保留不变。
     * @param argName 定义值的参数名称
     * @param value 要解析的值对象
     * @return 解析后的对象
     */
    @Nullable
    public Object resolveValueIfNecessary(Object argName, @Nullable Object value) {
        // 我们必须检查每个值，以确定它是否需要运行时引用
        // 将其解析为另一个 Bean。
        if (value instanceof RuntimeBeanReference ref) {
            return resolveReference(argName, ref);
        } else if (value instanceof RuntimeBeanNameReference ref) {
            String refName = ref.getBeanName();
            refName = String.valueOf(doEvaluate(refName));
            if (!this.beanFactory.containsBean(refName)) {
                throw new BeanDefinitionStoreException("Invalid bean name '" + refName + "' in bean reference for " + argName);
            }
            return refName;
        } else if (value instanceof BeanDefinitionHolder bdHolder) {
            // 解析 BeanDefinitionHolder：包含具有名称和别名的 BeanDefinition。
            return resolveInnerBean(bdHolder.getBeanName(), bdHolder.getBeanDefinition(), (name, mbd) -> resolveInnerBeanValue(argName, name, mbd));
        } else if (value instanceof BeanDefinition bd) {
            return resolveInnerBean(null, bd, (name, mbd) -> resolveInnerBeanValue(argName, name, mbd));
        } else if (value instanceof DependencyDescriptor dependencyDescriptor) {
            Set<String> autowiredBeanNames = new LinkedHashSet<>(2);
            Object result = this.beanFactory.resolveDependency(dependencyDescriptor, this.beanName, autowiredBeanNames, this.typeConverter);
            for (String autowiredBeanName : autowiredBeanNames) {
                if (this.beanFactory.containsBean(autowiredBeanName)) {
                    this.beanFactory.registerDependentBean(autowiredBeanName, this.beanName);
                }
            }
            return result;
        } else if (value instanceof ManagedArray managedArray) {
            // 可能需要解析包含的运行时引用。
            Class<?> elementType = managedArray.resolvedElementType;
            if (elementType == null) {
                String elementTypeName = managedArray.getElementTypeName();
                if (StringUtils.hasText(elementTypeName)) {
                    try {
                        elementType = ClassUtils.forName(elementTypeName, this.beanFactory.getBeanClassLoader());
                        managedArray.resolvedElementType = elementType;
                    } catch (Throwable ex) {
                        // 通过展示上下文来改进消息。
                        throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName, "Error resolving array type for " + argName, ex);
                    }
                } else {
                    elementType = Object.class;
                }
            }
            return resolveManagedArray(argName, (List<?>) value, elementType);
        } else if (value instanceof ManagedList<?> managedList) {
            // 可能需要解析包含的运行时引用。
            return resolveManagedList(argName, managedList);
        } else if (value instanceof ManagedSet<?> managedSet) {
            // 可能需要解析包含的运行时引用。
            return resolveManagedSet(argName, managedSet);
        } else if (value instanceof ManagedMap<?, ?> managedMap) {
            // 可能需要解析包含的运行时引用。
            return resolveManagedMap(argName, managedMap);
        } else if (value instanceof ManagedProperties original) {
            // Properties 原始 = 管理属性；
            Properties copy = new Properties();
            original.forEach((propKey, propValue) -> {
                if (propKey instanceof TypedStringValue typedStringValue) {
                    propKey = evaluate(typedStringValue);
                }
                if (propValue instanceof TypedStringValue typedStringValue) {
                    propValue = evaluate(typedStringValue);
                }
                if (propKey == null || propValue == null) {
                    throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName, "Error converting Properties key/value pair for " + argName + ": resolved to null");
                }
                copy.put(propKey, propValue);
            });
            return copy;
        } else if (value instanceof TypedStringValue typedStringValue) {
            // 在此处将值转换为目标类型。
            Object valueObject = evaluate(typedStringValue);
            try {
                Class<?> resolvedTargetType = resolveTargetType(typedStringValue);
                if (resolvedTargetType != null) {
                    return this.typeConverter.convertIfNecessary(valueObject, resolvedTargetType);
                } else {
                    return valueObject;
                }
            } catch (Throwable ex) {
                // 通过显示上下文来改进消息。
                throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName, "Error converting typed String value for " + argName, ex);
            }
        } else if (value instanceof NullBean) {
            return null;
        } else {
            return evaluate(value);
        }
    }

    /**
     * 解析内部Bean定义，并在其合并的Bean定义上调用指定的{@code resolver}。
     * @param innerBeanName 内部Bean名称（或{@code null}以分配一个）
     * @param innerBd 内部原始Bean定义
     * @param resolver 调用来解析的函数
     * @param <T> 解析的类型
     * @return 应用了{@code resolver}后的解析后的内部Bean
     * @since 6.0
     */
    public <T> T resolveInnerBean(@Nullable String innerBeanName, BeanDefinition innerBd, BiFunction<String, RootBeanDefinition, T> resolver) {
        String nameToUse = (innerBeanName != null ? innerBeanName : "(inner bean)" + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(innerBd));
        return resolver.apply(nameToUse, this.beanFactory.getMergedBeanDefinition(nameToUse, innerBd, this.beanDefinition));
    }

    /**
     * 如果需要，评估给定的值作为表达式。
     * @param value 候选值（可能是一个表达式）
     * @return 解析后的值
     */
    @Nullable
    protected Object evaluate(TypedStringValue value) {
        Object result = doEvaluate(value.getValue());
        if (!ObjectUtils.nullSafeEquals(result, value.getValue())) {
            value.setDynamic();
        }
        return result;
    }

    /**
     * 如果需要，评估给定的值作为表达式。
     * @param value 原始值（可能是一个表达式）
     * @return 如果需要，则返回解析后的值，否则返回原始值
     */
    @Nullable
    protected Object evaluate(@Nullable Object value) {
        if (value instanceof String str) {
            return doEvaluate(str);
        } else if (value instanceof String[] values) {
            boolean actuallyResolved = false;
            Object[] resolvedValues = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                String originalValue = values[i];
                Object resolvedValue = doEvaluate(originalValue);
                if (resolvedValue != originalValue) {
                    actuallyResolved = true;
                }
                resolvedValues[i] = resolvedValue;
            }
            return (actuallyResolved ? resolvedValues : values);
        } else {
            return value;
        }
    }

    /**
     * 评估给定的 String 值是否为表达式，并在必要时执行评估。
     * @param value 原始值（可能是一个表达式）
     * @return 如果需要，则返回解析后的值，否则返回原始的 String 值
     */
    @Nullable
    private Object doEvaluate(@Nullable String value) {
        return this.beanFactory.evaluateBeanDefinitionString(value, this.beanDefinition);
    }

    /**
     * 在给定的TypedStringValue中解析目标类型。
     * @param value 要解析的TypedStringValue
     * @return 解析后的目标类型（如果没有指定，则返回{@code null}）
     * @throws ClassNotFoundException 如果指定的类型无法解析
     * @see TypedStringValue#resolveTargetType
     */
    @Nullable
    protected Class<?> resolveTargetType(TypedStringValue value) throws ClassNotFoundException {
        if (value.hasTargetType()) {
            return value.getTargetType();
        }
        return value.resolveTargetType(this.beanFactory.getBeanClassLoader());
    }

    /**
     * 在工厂中解析对另一个bean的引用。
     */
    @Nullable
    private Object resolveReference(Object argName, RuntimeBeanReference ref) {
        try {
            Object bean;
            Class<?> beanType = ref.getBeanType();
            if (ref.isToParent()) {
                BeanFactory parent = this.beanFactory.getParentBeanFactory();
                if (parent == null) {
                    throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName, "Cannot resolve reference to bean " + ref + " in parent factory: no parent factory available");
                }
                if (beanType != null) {
                    bean = parent.getBean(beanType);
                } else {
                    bean = parent.getBean(String.valueOf(doEvaluate(ref.getBeanName())));
                }
            } else {
                String resolvedName;
                if (beanType != null) {
                    NamedBeanHolder<?> namedBean = this.beanFactory.resolveNamedBean(beanType);
                    bean = namedBean.getBeanInstance();
                    resolvedName = namedBean.getBeanName();
                } else {
                    resolvedName = String.valueOf(doEvaluate(ref.getBeanName()));
                    bean = this.beanFactory.getBean(resolvedName);
                }
                this.beanFactory.registerDependentBean(resolvedName, this.beanName);
            }
            if (bean instanceof NullBean) {
                bean = null;
            }
            return bean;
        } catch (BeansException ex) {
            throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName, "Cannot resolve reference to bean '" + ref.getBeanName() + "' while setting " + argName, ex);
        }
    }

    /**
     * 解决内部Bean的定义。
     * @param argName 内部Bean定义所对应的参数名称
     * @param innerBeanName 内部Bean的名称
     * @param mbd 内部Bean合并后的Bean定义
     * @return 解析后的内部Bean实例
     */
    @Nullable
    private Object resolveInnerBeanValue(Object argName, String innerBeanName, RootBeanDefinition mbd) {
        try {
            // 检查给定的 Bean 名称是否唯一。如果尚未唯一，
            // 增加计数器 - 将计数器增加直到名称唯一。
            String actualInnerBeanName = innerBeanName;
            if (mbd.isSingleton()) {
                actualInnerBeanName = adaptInnerBeanName(innerBeanName);
            }
            this.beanFactory.registerContainedBean(actualInnerBeanName, this.beanName);
            // 确保初始化内部Bean所依赖的Bean。
            String[] dependsOn = mbd.getDependsOn();
            if (dependsOn != null) {
                for (String dependsOnBean : dependsOn) {
                    this.beanFactory.registerDependentBean(dependsOnBean, actualInnerBeanName);
                    this.beanFactory.getBean(dependsOnBean);
                }
            }
            // 实际上现在创建内部Bean实例...
            Object innerBean = this.beanFactory.createBean(actualInnerBeanName, mbd, null);
            if (innerBean instanceof FactoryBean<?> factoryBean) {
                boolean synthetic = mbd.isSynthetic();
                innerBean = this.beanFactory.getObjectFromFactoryBean(factoryBean, actualInnerBeanName, !synthetic);
            }
            if (innerBean instanceof NullBean) {
                innerBean = null;
            }
            return innerBean;
        } catch (BeansException ex) {
            throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName, "Cannot create inner bean '" + innerBeanName + "' " + (mbd.getBeanClassName() != null ? "of type [" + mbd.getBeanClassName() + "] " : "") + "while setting " + argName, ex);
        }
    }

    /**
     * 检查给定的 Bean 名称是否唯一。如果不是唯一的，
     * 则添加一个计数器，增加计数器直到名称唯一。
     * @param innerBeanName 内部 Bean 的原始名称
     * @return 内部 Bean 适配后的名称
     */
    private String adaptInnerBeanName(String innerBeanName) {
        String actualInnerBeanName = innerBeanName;
        int counter = 0;
        String prefix = innerBeanName + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;
        while (this.beanFactory.isBeanNameInUse(actualInnerBeanName)) {
            counter++;
            actualInnerBeanName = prefix + counter;
        }
        return actualInnerBeanName;
    }

    /**
     * 对于管理数组中的每个元素，如有必要，则解析引用。
     */
    private Object resolveManagedArray(Object argName, List<?> ml, Class<?> elementType) {
        Object resolved = Array.newInstance(elementType, ml.size());
        for (int i = 0; i < ml.size(); i++) {
            Array.set(resolved, i, resolveValueIfNecessary(new KeyedArgName(argName, i), ml.get(i)));
        }
        return resolved;
    }

    /**
     * 对于管理列表中的每个元素，如有必要，则解析引用。
     */
    private List<?> resolveManagedList(Object argName, List<?> ml) {
        List<Object> resolved = new ArrayList<>(ml.size());
        for (int i = 0; i < ml.size(); i++) {
            resolved.add(resolveValueIfNecessary(new KeyedArgName(argName, i), ml.get(i)));
        }
        return resolved;
    }

    /**
     * 对于管理集合中的每个元素，如有必要，进行引用解析。
     */
    private Set<?> resolveManagedSet(Object argName, Set<?> ms) {
        Set<Object> resolved = new LinkedHashSet<>(ms.size());
        int i = 0;
        for (Object m : ms) {
            resolved.add(resolveValueIfNecessary(new KeyedArgName(argName, i), m));
            i++;
        }
        return resolved;
    }

    /**
     * 对于管理映射中的每个元素，如有必要，解析引用。
     */
    private Map<?, ?> resolveManagedMap(Object argName, Map<?, ?> mm) {
        Map<Object, Object> resolved = CollectionUtils.newLinkedHashMap(mm.size());
        mm.forEach((key, value) -> {
            Object resolvedKey = resolveValueIfNecessary(argName, key);
            Object resolvedValue = resolveValueIfNecessary(new KeyedArgName(argName, key), value);
            resolved.put(resolvedKey, resolvedValue);
        });
        return resolved;
    }

    /**
     * 用于延迟构建toString的持有者类。
     */
    private static class KeyedArgName {

        private final Object argName;

        private final Object key;

        public KeyedArgName(Object argName, Object key) {
            this.argName = argName;
            this.key = key;
        }

        @Override
        public String toString() {
            return this.argName + " with key " + BeanWrapper.PROPERTY_KEY_PREFIX + this.key + BeanWrapper.PROPERTY_KEY_SUFFIX;
        }
    }
}
