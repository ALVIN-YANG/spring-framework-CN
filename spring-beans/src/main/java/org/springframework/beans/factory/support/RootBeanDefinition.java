// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 *  根级Bean定义表示在运行时支持特定Spring BeanFactory中Bean的<b>合并后的Bean定义</b>。它可能是由多个原始Bean定义创建的，这些定义相互继承，例如从XML声明中创建的{@link GenericBeanDefinition GenericBeanDefinitions}。根级Bean定义本质上是在运行时的一种'统一'的Bean定义视图。
 *
 * <p>根级Bean定义也可以用于<b>在配置阶段注册单个Bean定义</b>。这对于从工厂方法（例如，`@Bean`方法）和实例提供者（例如，lambda表达式）推导出的程序性定义尤其适用，这些方法提供了额外的类型元数据（参见{@link #setTargetType(ResolvableType)}/{@link #setResolvedFactoryMethod(Method)}）。
 *
 * <p>注意：从声明性来源（例如，XML定义）推导出的Bean定义的首选选择是灵活的{@link GenericBeanDefinition}变体。GenericBeanDefinition的优点在于它允许动态定义父级依赖关系，而不是'硬编码'作为根级Bean定义的角色，甚至支持在Bean后处理器阶段更改父级关系。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see GenericBeanDefinition
 * @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public class RootBeanDefinition extends AbstractBeanDefinition {

    @Nullable
    private BeanDefinitionHolder decoratedDefinition;

    @Nullable
    private AnnotatedElement qualifiedElement;

    /**
     * 确定定义是否需要重新合并。
     */
    volatile boolean stale;

    boolean allowCaching = true;

    boolean isFactoryMethodUnique;

    @Nullable
    volatile ResolvableType targetType;

    /**
     * 用于缓存给定bean定义确定的类的包可见字段。
     */
    @Nullable
    volatile Class<?> resolvedTargetType;

    /**
     * 用于缓存如果该Bean是工厂Bean的包可见字段。
     */
    @Nullable
    volatile Boolean isFactoryBean;

    /**
     * 用于缓存泛型类型工厂方法返回类型的包可见字段。
     */
    @Nullable
    volatile ResolvableType factoryMethodReturnType;

    /**
     * 包可见的字段，用于缓存一个独特的工厂方法候选对象以供反射使用。
     */
    @Nullable
    volatile Method factoryMethodToIntrospect;

    /**
     * 用于缓存已解析的销毁方法名称的字段（也适用于推断出的）。
     */
    @Nullable
    volatile String resolvedDestroyMethodName;

    /**
     * 以下四个构造函数字段的公共锁。
     */
    final Object constructorArgumentLock = new Object();

    /**
     * 用于缓存已解析构造函数或工厂方法的包可见字段。
     */
    @Nullable
    Executable resolvedConstructorOrFactoryMethod;

    /**
     * 包可见字段，用于标记构造函数参数已解析。
     */
    boolean constructorArgumentsResolved = false;

    /**
     * 用于缓存完全解析的构造函数参数的包可见字段。
     */
    @Nullable
    Object[] resolvedConstructorArguments;

    /**
     * 用于缓存部分准备好的构造函数参数的包可见字段。
     */
    @Nullable
    Object[] preparedConstructorArguments;

    /**
     * 以下两个后处理字段共享的常用锁。
     */
    final Object postProcessingLock = new Object();

    /**
     * 表示已应用了MergedBeanDefinitionPostProcessor的包可见字段。
     */
    boolean postProcessed = false;

    /**
     * 包可见的字段，表示已经启动了实例化前的后处理器。
     */
    @Nullable
    volatile Boolean beforeInstantiationResolved;

    @Nullable
    private Set<Member> externallyManagedConfigMembers;

    @Nullable
    private Set<String> externallyManagedInitMethods;

    @Nullable
    private Set<String> externallyManagedDestroyMethods;

    /**
     * 创建一个新的 RootBeanDefinition，通过其bean属性和配置方法进行配置。
     * @see #setBeanClass
     * @see #setScope
     * @see #setConstructorArgumentValues
     * @see #setPropertyValues
     */
    public RootBeanDefinition() {
    }

    /**
     * 创建一个新的 RootBeanDefinition 以用于单例。
     * @param beanClass 要实例化的 bean 的类
     * @see #setBeanClass
     */
    public RootBeanDefinition(@Nullable Class<?> beanClass) {
        setBeanClass(beanClass);
    }

    /**
     * 创建一个新的单例 RootBeanDefinition。
     * @param beanType 要实例化的 Bean 类型
     * @since 6.0
     * @see #setTargetType(ResolvableType)
     * @deprecated 6.0.11 版本后已弃用，建议使用额外的 {@link #setTargetType(ResolvableType)} 调用
     */
    @Deprecated(since = "6.0.11")
    public RootBeanDefinition(@Nullable ResolvableType beanType) {
        setTargetType(beanType);
    }

    /**
     * 为单例bean创建一个新的RootBeanDefinition，通过调用给定的供应商（可能是lambda表达式或方法引用）来构造每个实例。
     * @param beanClass 要实例化的bean的类
     * @param instanceSupplier 构造bean实例的供应商，作为替代声明性指定的工厂方法的选项
     * @since 5.0
     * @see #setInstanceSupplier
     */
    public <T> RootBeanDefinition(@Nullable Class<T> beanClass, @Nullable Supplier<T> instanceSupplier) {
        setBeanClass(beanClass);
        setInstanceSupplier(instanceSupplier);
    }

    /**
     * 创建一个新的 RootBeanDefinition 用于作用域的 bean，通过调用给定的供应者（可能是一个 lambda 表达式或方法引用）来构造每个实例。
     * @param beanClass 要实例化的 bean 的类
     * @param scope 对应作用域的名称
     * @param instanceSupplier 构造 bean 实例的供应者，作为声明性指定的工厂方法的替代
     * @since 5.0
     * @see #setInstanceSupplier
     */
    public <T> RootBeanDefinition(@Nullable Class<T> beanClass, String scope, @Nullable Supplier<T> instanceSupplier) {
        setBeanClass(beanClass);
        setScope(scope);
        setInstanceSupplier(instanceSupplier);
    }

    /**
     * 创建一个新的 RootBeanDefinition 用于单例，
     * 使用给定的自动装配模式。
     * @param beanClass 要实例化的 Bean 的类
     * @param autowireMode 按名称或类型自动装配，使用本接口中的常量
     * @param dependencyCheck 是否对对象执行依赖性检查
     * （在构造函数中自动装配不适用，因此那里被忽略）
     */
    public RootBeanDefinition(@Nullable Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
        setBeanClass(beanClass);
        setAutowireMode(autowireMode);
        if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
            setDependencyCheck(DEPENDENCY_CHECK_OBJECTS);
        }
    }

    /**
     * 创建一个新的 RootBeanDefinition 用于单例，
     * 提供构造函数参数和属性值。
     * @param beanClass 要实例化的 bean 的类
     * @param cargs 要应用的构造函数参数值
     * @param pvs 要应用的属性值
     */
    public RootBeanDefinition(@Nullable Class<?> beanClass, @Nullable ConstructorArgumentValues cargs, @Nullable MutablePropertyValues pvs) {
        super(cargs, pvs);
        setBeanClass(beanClass);
    }

    /**
     * 创建一个新的RootBeanDefinition，用于单例，
     * 提供构造函数参数和属性值。
     * <p>接收一个bean类名以避免bean类的提前加载。
     * @param beanClassName 要实例化的类的名称
     */
    public RootBeanDefinition(String beanClassName) {
        setBeanClassName(beanClassName);
    }

    /**
     * 创建一个新的用于单例的RootBeanDefinition，
     * 提供构造函数参数和属性值。
     * <p>接受一个要实例化的Bean类名以避免Bean类的提前加载。
     * @param beanClassName 要实例化的类的名称
     * @param cargs 应用到的构造函数参数值
     * @param pvs 应用到的属性值
     */
    public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        super(cargs, pvs);
        setBeanClassName(beanClassName);
    }

    /**
     * 创建一个新的 RootBeanDefinition，作为给定 bean 定义的深拷贝。
     * @param original 要从中复制的原始 bean 定义
     */
    public RootBeanDefinition(RootBeanDefinition original) {
        super(original);
        this.decoratedDefinition = original.decoratedDefinition;
        this.qualifiedElement = original.qualifiedElement;
        this.allowCaching = original.allowCaching;
        this.isFactoryMethodUnique = original.isFactoryMethodUnique;
        this.targetType = original.targetType;
        this.factoryMethodToIntrospect = original.factoryMethodToIntrospect;
    }

    /**
     * 创建一个新的RootBeanDefinition，作为给定bean定义的深度副本。
     * @param original 要从中复制的原始bean定义
     */
    RootBeanDefinition(BeanDefinition original) {
        super(original);
    }

    @Override
    public String getParentName() {
        return null;
    }

    @Override
    public void setParentName(@Nullable String parentName) {
        if (parentName != null) {
            throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
        }
    }

    /**
     * 注册一个被此 Bean 定义装饰的目标定义。
     */
    public void setDecoratedDefinition(@Nullable BeanDefinitionHolder decoratedDefinition) {
        this.decoratedDefinition = decoratedDefinition;
    }

    /**
     * 如果有任何，返回由这个Bean定义装饰的目标定义。
     */
    @Nullable
    public BeanDefinitionHolder getDecoratedDefinition() {
        return this.decoratedDefinition;
    }

    /**
     * 指定定义修饰符的 {@link AnnotatedElement}，
     * 以代替目标类或工厂方法使用。
     * @since 4.3.3
     * @see #setTargetType(ResolvableType)
     * @see #getResolvedFactoryMethod()
     */
    public void setQualifiedElement(@Nullable AnnotatedElement qualifiedElement) {
        this.qualifiedElement = qualifiedElement;
    }

    /**
     * 返回定义了限定符的 {@link AnnotatedElement}，如果有的话。
     * 否则，将检查工厂方法和目标类。
     * @since 4.3.3
     */
    @Nullable
    public AnnotatedElement getQualifiedElement() {
        return this.qualifiedElement;
    }

    /**
     * 如果事先已知，指定此bean定义包含泛型的目标类型。
     * @since 4.3.3
     */
    public void setTargetType(@Nullable ResolvableType targetType) {
        this.targetType = targetType;
    }

    /**
     * 指定此 Bean 定义的预期类型，如果提前已知。
     * @since 3.2.2
     */
    public void setTargetType(@Nullable Class<?> targetType) {
        this.targetType = (targetType != null ? ResolvableType.forClass(targetType) : null);
    }

    /**
     * 返回此 Bean 定义的目标类型，如果已知（要么预先指定，要么在首次实例化时解析）。
     * @since 3.2.2
     */
    @Nullable
    public Class<?> getTargetType() {
        if (this.resolvedTargetType != null) {
            return this.resolvedTargetType;
        }
        ResolvableType targetType = this.targetType;
        return (targetType != null ? targetType.resolve() : null);
    }

    /**
     * 返回此bean定义的{@link ResolvableType}，
     * 无论是从运行时缓存的类型信息，还是从配置时的
     * {@link #setTargetType(ResolvableType)}或{@link #setBeanClass(Class)}，
     * 同时还考虑已解析的工厂方法定义。
     * @since 5.1
     * @see #setTargetType(ResolvableType)
     * @see #setBeanClass(Class)
     * @see #setResolvedFactoryMethod(Method)
     */
    @Override
    public ResolvableType getResolvableType() {
        ResolvableType targetType = this.targetType;
        if (targetType != null) {
            return targetType;
        }
        ResolvableType returnType = this.factoryMethodReturnType;
        if (returnType != null) {
            return returnType;
        }
        Method factoryMethod = this.factoryMethodToIntrospect;
        if (factoryMethod != null) {
            return ResolvableType.forMethodReturnType(factoryMethod);
        }
        return super.getResolvableType();
    }

    /**
     * 确定用于默认构造的推荐构造函数，如果有。
     * 如果需要，构造函数参数将自动装配。
     * @return 一个或多个推荐构造函数，如果没有则返回 {@code null}
     * （在这种情况下，将调用常规的无参默认构造函数）
     * @since 5.1
     */
    @Nullable
    public Constructor<?>[] getPreferredConstructors() {
        return null;
    }

    /**
     * 指定一个工厂方法名称，该名称指向一个非重载方法。
     */
    public void setUniqueFactoryMethodName(String name) {
        Assert.hasText(name, "Factory method name must not be empty");
        setFactoryMethodName(name);
        this.isFactoryMethodUnique = true;
    }

    /**
     * 指定一个工厂方法名称，该名称引用一个重载方法。
     * @since 5.2
     */
    public void setNonUniqueFactoryMethodName(String name) {
        Assert.hasText(name, "Factory method name must not be empty");
        setFactoryMethodName(name);
        this.isFactoryMethodUnique = false;
    }

    /**
     * 检查给定的候选者是否符合工厂方法的要求。
     */
    public boolean isFactoryMethod(Method candidate) {
        return candidate.getName().equals(getFactoryMethodName());
    }

    /**
     * 为此 Bean 定义设置一个已解析的 Java 方法作为工厂方法。
     * @param method 已解析的工厂方法，或 {@code null} 以重置它
     * @since 5.2
     */
    public void setResolvedFactoryMethod(@Nullable Method method) {
        this.factoryMethodToIntrospect = method;
        if (method != null) {
            setUniqueFactoryMethodName(method.getName());
        }
    }

    /**
     * 返回可用的解析后的工厂方法作为 Java 方法对象，如果有的话。
     * @return 工厂方法，或者在找不到或尚未解析时返回 {@code null}
     */
    @Nullable
    public Method getResolvedFactoryMethod() {
        return this.factoryMethodToIntrospect;
    }

    @Override
    public void setInstanceSupplier(@Nullable Supplier<?> supplier) {
        super.setInstanceSupplier(supplier);
        Method factoryMethod = (supplier instanceof InstanceSupplier<?> instanceSupplier ? instanceSupplier.getFactoryMethod() : null);
        if (factoryMethod != null) {
            setResolvedFactoryMethod(factoryMethod);
        }
    }

    /**
     * 将此 Bean 定义标记为已后处理，
     * 即由 {@link MergedBeanDefinitionPostProcessor} 处理。
     * @since 6.0
     */
    public void markAsPostProcessed() {
        synchronized (this.postProcessingLock) {
            this.postProcessed = true;
        }
    }

    /**
     * 注册一个外部管理的配置方法或字段。
     */
    public void registerExternallyManagedConfigMember(Member configMember) {
        synchronized (this.postProcessingLock) {
            if (this.externallyManagedConfigMembers == null) {
                this.externallyManagedConfigMembers = new LinkedHashSet<>(1);
            }
            this.externallyManagedConfigMembers.add(configMember);
        }
    }

    /**
     * 判断给定的方法或字段是否为外部管理的配置成员。
     */
    public boolean isExternallyManagedConfigMember(Member configMember) {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedConfigMembers != null && this.externallyManagedConfigMembers.contains(configMember));
        }
    }

    /**
     * 获取所有外部管理的配置方法和字段（作为一个不可变的 Set）。
     * @since 5.3.11
     */
    public Set<Member> getExternallyManagedConfigMembers() {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedConfigMembers != null ? Collections.unmodifiableSet(new LinkedHashSet<>(this.externallyManagedConfigMembers)) : Collections.emptySet());
        }
    }

    /**
     * 注册一个外部管理的配置初始化方法 ——
     * 例如，一个使用JSR-250的{@code javax.annotation.PostConstruct}
     * 或Jakarta的{@link jakarta.annotation.PostConstruct}注解标注的方法。
     * <p>提供的{@code initMethod}可能是一个
     * {@linkplain Method#getName() 简单方法名}或一个
     * {@linkplain org.springframework.util.ClassUtils#getQualifiedMethodName(Method)
     * 完整方法名}，用于包私有和{@code private}方法。
     * 为了在类型层次结构中区分具有相同名称的多个包私有和{@code private}方法，需要使用完整方法名。
     */
    public void registerExternallyManagedInitMethod(String initMethod) {
        synchronized (this.postProcessingLock) {
            if (this.externallyManagedInitMethods == null) {
                this.externallyManagedInitMethods = new LinkedHashSet<>(1);
            }
            this.externallyManagedInitMethods.add(initMethod);
        }
    }

    /**
     * 判断给定的方法名是否表示一个外部管理的初始化方法。
     * <p>有关提供的 {@code initMethod} 格式的详细信息，请参阅 {@link #registerExternallyManagedInitMethod}。
     */
    public boolean isExternallyManagedInitMethod(String initMethod) {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedInitMethods != null && this.externallyManagedInitMethods.contains(initMethod));
        }
    }

    /**
     * 判断给定的方法名是否表示一个外部管理的初始化方法，无论方法可见性如何。
     * 与{@link #isExternallyManagedInitMethod(String)}不同，此方法如果存在一个已经通过
     * 使用有资格的方法名而非简单的方法名进行注册的、已被
     * {@linkplain #registerExternallyManagedInitMethod(String)}注册的、私有外部管理的初始化方法，
     * 也会返回{@code true}。
     * @since 5.3.17
     */
    boolean hasAnyExternallyManagedInitMethod(String initMethod) {
        synchronized (this.postProcessingLock) {
            if (isExternallyManagedInitMethod(initMethod)) {
                return true;
            }
            return hasAnyExternallyManagedMethod(this.externallyManagedInitMethods, initMethod);
        }
    }

    /**
     * 获取所有外部管理的初始化方法（作为一个不可变的 Set）。
     * <p>有关返回集中初始化方法的格式细节，请参阅 {@link #registerExternallyManagedInitMethod}。
     * @since 5.3.11
     */
    public Set<String> getExternallyManagedInitMethods() {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedInitMethods != null ? Collections.unmodifiableSet(new LinkedHashSet<>(this.externallyManagedInitMethods)) : Collections.emptySet());
        }
    }

    /**
     * 如果需要，解决推断的销毁方法。
     * @since 6.0
     */
    public void resolveDestroyMethodIfNecessary() {
        setDestroyMethodNames(DisposableBeanAdapter.inferDestroyMethodsIfNecessary(getResolvableType().toClass(), this));
    }

    /**
     * 注册一个外部管理的配置销毁方法 &mdash;
     * 例如，一个带有JSR-250的
     * {@link jakarta.annotation.PreDestroy} 注解的方法。
     * <p>提供的 `destroyMethod` 可能是非私有方法的
     * {@linkplain Method#getName() 简单方法名}，或者是
     * {@linkplain org.springframework.util.ClassUtils#getQualifiedMethodName(Method)
     * 限定方法名} 对于私有方法。对于类层次结构中具有相同名称的多个私有方法，限定名称是必要的，以便区分。
     */
    public void registerExternallyManagedDestroyMethod(String destroyMethod) {
        synchronized (this.postProcessingLock) {
            if (this.externallyManagedDestroyMethods == null) {
                this.externallyManagedDestroyMethods = new LinkedHashSet<>(1);
            }
            this.externallyManagedDestroyMethods.add(destroyMethod);
        }
    }

    /**
     * 判断给定的方法名是否表示一个外部管理的销毁方法。
     * 有关提供的 `destroyMethod` 格式的详细信息，请参阅 {@link #registerExternallyManagedDestroyMethod}。
     */
    public boolean isExternallyManagedDestroyMethod(String destroyMethod) {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedDestroyMethods != null && this.externallyManagedDestroyMethods.contains(destroyMethod));
        }
    }

    /**
     * 确定给定的方法名称是否指示一个外部管理销毁方法，无论方法可见性如何。
     * 与{@link #isExternallyManagedDestroyMethod(String)}不同，此方法如果存在一个已通过
     * {@linkplain #registerExternallyManagedDestroyMethod(String)}注册的、使用限定方法名而不是简单方法名的
     * 外部管理销毁方法（私有），也会返回{@code true}。
     * @since 5.3.17
     */
    boolean hasAnyExternallyManagedDestroyMethod(String destroyMethod) {
        synchronized (this.postProcessingLock) {
            if (isExternallyManagedDestroyMethod(destroyMethod)) {
                return true;
            }
            return hasAnyExternallyManagedMethod(this.externallyManagedDestroyMethods, destroyMethod);
        }
    }

    private static boolean hasAnyExternallyManagedMethod(Set<String> candidates, String methodName) {
        if (candidates != null) {
            for (String candidate : candidates) {
                int indexOfDot = candidate.lastIndexOf('.');
                if (indexOfDot > 0) {
                    String candidateMethodName = candidate.substring(indexOfDot + 1);
                    if (candidateMethodName.equals(methodName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取所有外部管理的销毁方法（作为一个不可变的Set）。
     * <p>有关返回集中销毁方法格式的详细信息，请参阅{@link #registerExternallyManagedDestroyMethod}。
     * @since 5.3.11
     */
    public Set<String> getExternallyManagedDestroyMethods() {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedDestroyMethods != null ? Collections.unmodifiableSet(new LinkedHashSet<>(this.externallyManagedDestroyMethods)) : Collections.emptySet());
        }
    }

    @Override
    public RootBeanDefinition cloneBeanDefinition() {
        return new RootBeanDefinition(this);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
    }

    @Override
    public String toString() {
        return "Root bean: " + super.toString();
    }
}
