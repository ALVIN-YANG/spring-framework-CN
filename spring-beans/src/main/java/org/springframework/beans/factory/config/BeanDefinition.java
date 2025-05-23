// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证，了解管理许可权和限制的具体语言。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * BeanDefinition描述了一个具有属性值、构造函数参数值以及由具体实现提供的其他信息的bean实例。
 *
 * <p>这只是一个最小接口：主要目的是允许一个{@link BeanFactoryPostProcessor}进行反射并修改属性值和其他bean元数据。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 19.03.2004
 * @see ConfigurableListableBeanFactory#getBeanDefinition
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

    /**
     * 标准单例作用域的标识符：{@value}。
     * <p>注意，扩展的Bean工厂可能支持更多的作用域。
     * @see #setScope
     * @see ConfigurableBeanFactory#SCOPE_SINGLETON
     */
    String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;

    /**
     * 标准原型作用域的标识符：{@value}。
     * <p>注意，扩展的bean工厂可能支持更多的作用域。
     * @see #setScope
     * @see ConfigurableBeanFactory#SCOPE_PROTOTYPE
     */
    String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;

    /**
     * 角色提示表示一个 {@code BeanDefinition} 是应用程序的主要部分。通常对应于用户定义的Bean。
     */
    int ROLE_APPLICATION = 0;

    /**
     * 角色提示表明一个 {@code BeanDefinition} 是更大配置的一部分，通常是外部的
     * {@link org.springframework.beans.factory.parsing.ComponentDefinition}。
     * 被视为重要到需要在更仔细地查看特定的
     * {@link org.springframework.beans.factory.parsing.ComponentDefinition} 时意识到
     * 的 {@code SUPPORT} 实例，但在查看应用程序的整体配置时则不是。
     */
    int ROLE_SUPPORT = 1;

    /**
     * 角色提示表示一个 {@code BeanDefinition} 扮演的是完全的背景角色，对最终用户没有关联。此提示用于注册完全属于 {@link org.springframework.beans.factory.parsing.ComponentDefinition} 内部工作的一部分的 Bean。
     */
    int ROLE_INFRASTRUCTURE = 2;

    // 可修改的属性
    /**
     * 设置此bean定义的父定义的名称，如果有的话。
     */
    void setParentName(@Nullable String parentName);

    /**
     * 如果存在，则返回此 Bean 定义父定义的名称。
     */
    @Nullable
    String getParentName();

    /**
     * 指定此bean定义的bean类名称。
     * <p>类名称可以在bean工厂后处理期间进行修改，
     * 通常用解析后的变体替换原始类名称。
     * @see #setParentName
     * @see #setFactoryBeanName
     * @see #setFactoryMethodName
     */
    void setBeanClassName(@Nullable String beanClassName);

    /**
     * 返回此bean定义当前bean类的名称。
     * <p>请注意，这不一定是要在运行时使用的实际类名，在子定义覆盖/继承父定义的类名的情况下。这也可能是调用工厂方法时的类，或者如果是在工厂bean引用上调用方法，甚至可能是空的。因此，<i>不要</i>将其视为运行时的最终bean类型，而应仅将其用于解析目的，在单个bean定义级别上使用。
     * @see #getParentName()
     * @see #getFactoryBeanName()
     * @see #getFactoryMethodName()
     */
    @Nullable
    String getBeanClassName();

    /**
     * 覆盖此bean的目标作用域，指定一个新的作用域名称。
     * @see #SCOPE_SINGLETON
     * @see #SCOPE_PROTOTYPE
     */
    void setScope(@Nullable String scope);

    /**
     * 返回此bean的当前目标作用域的名称，
     * 或如果尚未知道，则返回{@code null}。
     */
    @Nullable
    String getScope();

    /**
     * 设置此 Bean 是否应该进行懒加载初始化。
     * <p>如果为 {@code false}，则 Bean 将由执行单例 eager 初始化的 Bean 工厂在启动时进行实例化。
     */
    void setLazyInit(boolean lazyInit);

    /**
     * 返回此Bean是否应该进行懒加载，即不在启动时立即实例化。仅适用于单例Bean。
     */
    boolean isLazyInit();

    /**
     * 设置此Bean所依赖的Bean的名称，确保这些Bean先被初始化。
     * Bean工厂将保证这些Bean先被初始化。
     */
    void setDependsOn(@Nullable String... dependsOn);

    /**
     * 返回此 Bean 依赖的 Bean 名称。
     */
    @Nullable
    String[] getDependsOn();

    /**
     * 设置此 Bean 是否是自动装配到其他 Bean 的候选者。
     * <p>请注意，此标志仅设计用于影响基于类型的自动装配。
     * 它不会影响通过名称显式引用的情况，即使指定的 Bean 未被标记为自动装配候选者，这些引用也会被解析。因此，尽管如此，如果名称匹配，通过名称的自动装配仍会注入一个 Bean。
     */
    void setAutowireCandidate(boolean autowireCandidate);

    /**
     * 返回此 Bean 是否是自动注入到其他 Bean 的候选者。
     */
    boolean isAutowireCandidate();

    /**
     * 设置此Bean是否为首选自动装配候选者。
     * <p>如果多个匹配候选者中恰好有一个Bean的此值设置为{@code true}，它将作为决断因素。
     */
    void setPrimary(boolean primary);

    /**
     * 返回此Bean是否是主要自动装配候选者。
     */
    boolean isPrimary();

    /**
     * 指定要使用的工厂bean（如果有的话）。
     * 这是调用指定工厂方法的bean的名称。
     * @see #setFactoryMethodName
     */
    void setFactoryBeanName(@Nullable String factoryBeanName);

    /**
     * 返回工厂bean的名称，如果有的话。
     */
    @Nullable
    String getFactoryBeanName();

    /**
     * 指定一个工厂方法（如果有）。此方法将使用构造函数参数被调用，如果没有指定参数，则不带参数调用。如果指定了工厂bean，该方法将在指定的工厂bean上调用，否则作为本地bean类的静态方法调用。
     * @see #setFactoryBeanName
     * @see #setBeanClassName
     */
    void setFactoryMethodName(@Nullable String factoryMethodName);

    /**
     * 返回一个工厂方法，如果有。
     */
    @Nullable
    String getFactoryMethodName();

    /**
     * 返回此 Bean 的构造器参数值。
     * <p>返回的实例可以在 Bean 工厂后处理期间进行修改。
     * @return 构造器参数值对象（绝不会为 {@code null}）
     */
    ConstructorArgumentValues getConstructorArgumentValues();

    /**
     * 如果此Bean定义了构造函数参数值，则返回。
     * @since 5.0.2
     */
    default boolean hasConstructorArgumentValues() {
        return !getConstructorArgumentValues().isEmpty();
    }

    /**
     * 返回应用于新实例的属性值。
     * <p>返回的实例可以在bean工厂的后处理过程中进行修改。
     * @return 可变属性值对象（从不为{@code null}）
     */
    MutablePropertyValues getPropertyValues();

    /**
     * 如果为该Bean定义了属性值，则返回。
     * @since 5.0.2
     */
    default boolean hasPropertyValues() {
        return !getPropertyValues().isEmpty();
    }

    /**
     * 设置初始化方法的名称。
     * @since 5.1
     */
    void setInitMethodName(@Nullable String initMethodName);

    /**
     * 返回初始化方法的名称。
     * @since 5.1
     */
    @Nullable
    String getInitMethodName();

    /**
     * 设置销毁方法的名称。
     * @since 5.1
     */
    void setDestroyMethodName(@Nullable String destroyMethodName);

    /**
     * 返回销毁方法的名称。
     * @since 5.1
     */
    @Nullable
    String getDestroyMethodName();

    /**
     * 设置此{@code BeanDefinition}的角色提示。角色提示为框架和工具提供了对特定{@code BeanDefinition}的角色和重要性的指示。
     * @since 5.1
     * @see #ROLE_APPLICATION
     * @see #ROLE_SUPPORT
     * @see #ROLE_INFRASTRUCTURE
     */
    void setRole(int role);

    /**
     * 获取此 {@code BeanDefinition} 的角色提示。角色提示为框架以及工具提供了关于特定 {@code BeanDefinition} 角色和重要性的指示。
     * @see #ROLE_APPLICATION
     * @see #ROLE_SUPPORT
     * @see #ROLE_INFRASTRUCTURE
     */
    int getRole();

    /**
     * 设置此Bean定义的可读描述。
     * @since 5.1
     */
    void setDescription(@Nullable String description);

    /**
     * 返回该 Bean 定义的易读描述。
     */
    @Nullable
    String getDescription();

    // 只读属性
    /**
     * 返回一个可解析的类型，针对这个bean定义，基于bean类或其他特定元数据。
     * <p>这通常在运行时合并的bean定义上完全解析，但在配置时定义实例上不一定如此。
     * @return 可解析的类型（可能是 {@link ResolvableType#NONE}）
     * @since 5.2
     * @see ConfigurableBeanFactory#getMergedBeanDefinition
     */
    ResolvableType getResolvableType();

    /**
     * 返回是否为<b>单例</b>，所有调用都返回一个单一、共享的实例
     * @see #SCOPE_SINGLETON
     */
    boolean isSingleton();

    /**
     * 返回此对象是否为<b>原型</b>，每次调用都返回一个独立的实例。
     * @since 3.0
     * @see #SCOPE_PROTOTYPE
     */
    boolean isPrototype();

    /**
     * 返回此bean是否为"抽象的"，即不是用来实例化的。
     */
    boolean isAbstract();

    /**
     * 返回此Bean定义所来源的资源描述（用于在出现错误时提供上下文）。
     */
    @Nullable
    String getResourceDescription();

    /**
     * 返回原始的 BeanDefinition，如果没有则返回 {@code null}。
     * <p>允许检索装饰过的 BeanDefinition（如果存在）。
     * <p>注意，此方法返回的是直接的原作者。通过遍历原作者链来找到用户定义的原始 BeanDefinition。
     */
    @Nullable
    BeanDefinition getOriginatingBeanDefinition();
}
