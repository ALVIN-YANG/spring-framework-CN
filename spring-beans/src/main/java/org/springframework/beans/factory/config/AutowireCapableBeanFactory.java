// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按 "原样" 分发，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的或不侵犯知识产权的保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.lang.Nullable;

/**
 * 扩展了 {@link org.springframework.beans.factory.BeanFactory} 接口，以供能够实现自动装配功能的 BeanFactory 实现，前提是它们希望为现有 Bean 实例公开此功能。
 *
 * <p>此 BeanFactory 子接口不应用于正常的应用程序代码：对于典型用例，请坚持使用 {@link org.springframework.beans.factory.BeanFactory} 或 {@link org.springframework.beans.factory.ListableBeanFactory}。
 *
 * <p>与其他框架的集成代码可以利用此接口来连接和填充 Spring 不控制其生命周期的现有 Bean 实例。这对于 WebWork 动作和 Tapestry 页面对象等特别有用。
 *
 * <p>请注意，此接口不由 {@link org.springframework.context.ApplicationContext} 面板实现，因为它几乎不被应用程序代码使用。尽管如此，它仍然可以从应用程序上下文中获取，通过 ApplicationContext 的 {@link org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()} 方法访问。
 *
 * <p>您还可以实现 {@link org.springframework.beans.factory.BeanFactoryAware} 接口，即使运行在 ApplicationContext 中也能公开内部 BeanFactory，以获取对 AutowireCapableBeanFactory 的访问权限：只需将传入的 BeanFactory 强制转换为 AutowireCapableBeanFactory 即可。
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
 */
public interface AutowireCapableBeanFactory extends BeanFactory {

    /**
     * 表示没有外部定义自动装配的常量。注意，BeanFactoryAware 等以及注解驱动的注入仍然会被应用。
     * @see #createBean
     * @see #autowire
     * @see #autowireBeanProperties
     */
    int AUTOWIRE_NO = 0;

    /**
     * 表示通过名称自动装配 bean 属性的常量
     * （适用于所有 bean 属性设置器）。
     * @see #createBean
     * @see #autowire
     * @see #autowireBeanProperties
     */
    int AUTOWIRE_BY_NAME = 1;

    /**
     * 表示通过类型自动装配bean属性的常量
     * (适用于所有bean属性设置器)。
     * @see #createBean
     * @see #autowire
     * @see #autowireBeanProperties
     */
    int AUTOWIRE_BY_TYPE = 2;

    /**
     * 表示自动装配最贪婪的满足条件的构造函数的常量（涉及解析适当的构造函数）。
     * @see #createBean
     * @see #autowire
     */
    int AUTOWIRE_CONSTRUCTOR = 3;

    /**
     * 常量，表示通过反射bean类来确定合适的自动装配策略。
     * @see #createBean
     * @see #autowire
     * 已废弃：自Spring 3.0起，如果您使用混合自动装配策略，请优先使用基于注解的自动装配，以更清晰地界定自动装配需求。
     */
    @Deprecated
    int AUTOWIRE_AUTODETECT = 4;

    /**
     * 用于初始化现有bean实例时的"原始实例"约定后缀：将附加到完全限定的bean类名上，例如"com.mypackage.MyClass.ORIGINAL"，以确保返回指定的实例，即不返回代理等。
     * @since 5.1
     * @see #initializeBean(Object, String)
     * @see #applyBeanPostProcessorsBeforeInitialization(Object, String)
     * @see #applyBeanPostProcessorsAfterInitialization(Object, String)
     */
    String ORIGINAL_INSTANCE_SUFFIX = ".ORIGINAL";

    // 很抱歉，您提供的代码注释内容为空。请提供具体的 Java 代码注释内容，以便我能够为您进行翻译。
    // 典型的创建和填充外部Bean实例的方法
    // 很抱歉，您只提供了代码注释的分隔符“-------------------------------------------------------------------------”，并没有提供实际的代码注释内容。请提供需要翻译的代码注释部分，以便我能够为您进行准确的翻译。
    /**
     * 完全创建给定类的全新Bean实例。
     * <p>执行Bean的完整初始化，包括所有适用的
     * {@link BeanPostProcessor BeanPostProcessors}。
     * <p>注意：此方法旨在创建一个全新的实例，填充注解的字段和方法，以及应用所有标准的Bean初始化回调。
     * 构造函数的解析基于Kotlin的主构造函数/单个公共构造函数/单个非公共构造函数，
     * 在模糊场景中会回退到默认构造函数，也受
     * {@link SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors}
     * 的影响（例如，用于注解驱动的构造函数选择）。
     * @param beanClass 要创建的Bean的类
     * @return 新的Bean实例
     * @throws BeansException 如果实例化或连接失败
     */
    <T> T createBean(Class<T> beanClass) throws BeansException;

    /**
     * 通过应用实例化后的回调和属性后处理（例如，用于注解驱动的注入）来填充给定的 bean 实例。
     * <p>注意：这本质上是为了填充注解的字段和方法，无论是新实例还是反序列化实例。它不涉及传统的按名称或按类型自动装配属性；使用 {@link #autowireBeanProperties} 来实现这些目的。
     * @param existingBean 已存在的 bean 实例
     * @throws BeansException 如果装配失败
     */
    void autowireBean(Object existingBean) throws BeansException;

    /**
     * 配置给定的原始Bean：自动装配Bean属性，应用Bean属性值，应用工厂回调，例如`setBeanName`和`setBeanFactory`，以及应用所有Bean后处理器（包括可能包装给定原始Bean的后处理器）。
     * 这实际上是`#initializeBean`提供功能的超集，完全应用了对应Bean定义中指定的配置。
     * <b>注意：此方法需要一个名为给定名称的Bean定义！</b>
     * @param existingBean 已存在的Bean实例
     * @param beanName Bean的名称，必要时将其传递给它（必须有一个该名称的Bean定义可用）
     * @return 要使用的Bean实例，可以是原始的或包装后的一个
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     * 如果没有带有给定名称的Bean定义
     * @throws BeansException 如果初始化失败
     * @see #initializeBean
     */
    Object configureBean(Object existingBean, String beanName) throws BeansException;

    // 很抱歉，您提供的内容“-------------------------------------------------------------------------”并不是有效的 Java 代码注释，因此无法进行翻译。如果您能提供实际的 Java 代码注释，我将很乐意帮您翻译成中文。
    // 专门的方法，用于对 Bean 生命周期进行细粒度控制
    // 很抱歉，您提供的内容“-------------------------------------------------------------------------”并不是Java代码注释内容，而是一系列短横线。如果您能提供具体的Java代码注释，我会很乐意帮您将其翻译成中文。
    /**
     * 完全创建给定类的新的bean实例，并使用指定的自动装配策略。此接口中定义的所有常量均在此处支持。
     * <p>执行bean的完整初始化，包括所有适用的 {@link BeanPostProcessor BeanPostProcessors}。这实际上是 {@link #autowire} 提供的功能的超集，增加了 {@link #initializeBean} 的行为。
     * @param beanClass 要创建的bean的类
     * @param autowireMode 按名称或类型自动装配，使用此接口中的常量
     * @param dependencyCheck 是否对对象执行依赖性检查（在自动装配构造函数时不可应用，因此在此处忽略）
     * @return 新的bean实例
     * @throws BeansException 如果实例化或连接失败
     * @see #AUTOWIRE_NO
     * @see #AUTOWIRE_BY_NAME
     * @see #AUTOWIRE_BY_TYPE
     * @see #AUTOWIRE_CONSTRUCTOR
     */
    Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

    /**
     * 使用指定的自动装配策略实例化给定类的新的Bean实例。此接口中定义的所有常量都受到支持。
     * 也可以使用{@code AUTOWIRE_NO}来调用，以便仅应用实例化前的回调（例如，用于基于注解的注入）。
     * <p>不会应用标准的{@link BeanPostProcessor BeanPostProcessors}回调或执行Bean的任何进一步初始化。此接口提供了针对这些目的的独立、细粒度的操作，例如，例如使用{@link #initializeBean}。
     * 然而，如果适用，则会应用{@link InstantiationAwareBeanPostProcessor}回调，以构造实例。
     * @param beanClass 要实例化的Bean的类
     * @param autowireMode 按名称或类型自动装配，使用此接口中的常量
     * @param dependencyCheck 是否对Bean实例中的对象引用执行依赖项检查（不适用于自动装配构造函数，因此在此处忽略）
     * @return 新的Bean实例
     * @throws BeansException 如果实例化或连接失败
     * @see #AUTOWIRE_NO
     * @see #AUTOWIRE_BY_NAME
     * @see #AUTOWIRE_BY_TYPE
     * @see #AUTOWIRE_CONSTRUCTOR
     * @see #AUTOWIRE_AUTODETECT
     * @see #initializeBean
     * @see #applyBeanPostProcessorsBeforeInitialization
     * @see #applyBeanPostProcessorsAfterInitialization
     */
    Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

    /**
     * 通过名称或类型自动装配给定bean实例的bean属性。
     * 也可以使用{@code AUTOWIRE_NO}来仅应用实例化后的回调（例如，用于注解驱动的注入）。
     * <p>不会应用标准的{@link BeanPostProcessor BeanPostProcessors}回调或对bean进行任何进一步的初始化。此接口为这些目的提供了独特、细粒度的操作，例如{@link #initializeBean}。然而，如果适用，会应用{@link InstantiationAwareBeanPostProcessor}回调。
     * @param existingBean 已存在的bean实例
     * @param autowireMode 通过名称或类型，使用此接口中的常量
     * @param dependencyCheck 是否对bean实例中的对象引用执行依赖性检查
     * @throws BeansException 如果装配失败
     * @see #AUTOWIRE_BY_NAME
     * @see #AUTOWIRE_BY_TYPE
     * @see #AUTOWIRE_NO
     */
    void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck) throws BeansException;

    /**
     * 将给定名称的bean定义的属性值应用到给定的bean实例上。bean定义可以定义一个完全自包含的bean，重用其属性值，或者只是为现有bean实例使用的属性值。
     * <p>此方法<i>不会</i>自动装配bean属性；它只是应用显式定义的属性值。使用{@link #autowireBeanProperties}方法来自动装配现有的bean实例。
     * <b>注意：</b>此方法需要一个具有给定名称的bean定义！
     * <p><i>不会</i>应用标准的{@link BeanPostProcessor BeanPostProcessors}回调或对bean进行任何进一步的初始化。此接口为这些目的提供了独特的、细粒度的操作，例如{@link #initializeBean}。然而，如果适用，会应用{@link InstantiationAwareBeanPostProcessor}回调。
     * @param existingBean 现有的bean实例
     * @param beanName 在bean工厂中bean定义的名称（必须存在具有该名称的bean定义）
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     * 如果没有具有给定名称的bean定义
     * @throws BeansException 如果应用属性值失败
     * @see #autowireBeanProperties
     */
    void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

    /**
     * 初始化给定的原始 Bean，应用工厂回调，例如 {@code setBeanName} 和 {@code setBeanFactory}，同时应用所有 Bean 后处理器（包括可能包装给定原始 Bean 的那些）。
     * <p>注意，给定的名称在 Bean 工厂中不必存在。传入的 Bean 名称将仅用于回调，而不会与已注册的 Bean 定义进行校验。
     * @param existingBean 已存在的 Bean 实例
     * @param beanName Bean 的名称，如有必要将传递给它（仅传递给 {@link BeanPostProcessor BeanPostProcessors}；
     * 可以遵循 {@link #ORIGINAL_INSTANCE_SUFFIX} 约定，以强制返回给定的实例，即没有代理等）
     * @return 要使用的 Bean 实例，原始实例或包装后的实例
     * @throws BeansException 如果初始化失败
     * @see #ORIGINAL_INSTANCE_SUFFIX
     */
    Object initializeBean(Object existingBean, String beanName) throws BeansException;

    /**
     * 将 {@link BeanPostProcessor BeanPostProcessors} 应用到给定的现有bean实例上，调用它们的 {@code postProcessBeforeInitialization} 方法。
     * 返回的bean实例可能是一个围绕原始实例的包装器。
     * @param existingBean 现有的bean实例
     * @param beanName bean的名称，如果需要可传递给它（仅传递给 {@link BeanPostProcessor BeanPostProcessors}；
     * 可以遵循 {@link #ORIGINAL_INSTANCE_SUFFIX} 规范，以确保返回指定的实例，即无代理等）
     * @return 要使用的bean实例，要么是原始的，要么是包装过的
     * @throws BeansException 如果任何后处理失败
     * @see BeanPostProcessor#postProcessBeforeInitialization
     * @see #ORIGINAL_INSTANCE_SUFFIX
     */
    Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException;

    /**
     * 将给定的现有bean实例应用 {@link BeanPostProcessor BeanPostProcessors}，调用它们的 `postProcessAfterInitialization` 方法。
     * 返回的bean实例可能是原始实例的包装器。
     * @param existingBean 现有的bean实例
     * @param beanName bean的名称，如有必要将其传递给它（仅传递给 {@link BeanPostProcessor BeanPostProcessors}；
     * 可以遵循 `#ORIGINAL_INSTANCE_SUFFIX` 规范，以确保返回给定实例，即没有代理等）
     * @return 要使用的bean实例，原始的或包装的
     * @throws BeansException 如果任何后处理失败
     * @see BeanPostProcessor#postProcessAfterInitialization
     * @see #ORIGINAL_INSTANCE_SUFFIX
     */
    Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException;

    /**
     * 销毁给定的Bean实例（通常来自{@link #createBean}），
     * 应用{@link org.springframework.beans.factory.DisposableBean}契约以及已注册的{@link DestructionAwareBeanPostProcessor DestructionAwareBeanPostProcessors}。
     * <p>在销毁过程中产生的任何异常都应该被捕获并记录，而不是传播给此方法调用者。
     * @param existingBean 要销毁的Bean实例
     */
    void destroyBean(Object existingBean);

    // 很抱歉，您只提供了代码注释中的分隔符（"-------------------------------------------------------------------------"），而没有提供实际的代码注释内容。请提供具体的代码注释文本，以便我能够进行翻译。
    // 代理方法用于解决注入点
    // 很抱歉，您只提供了代码注释的一部分 "-------------------------------------------------------------------------"。为了提供准确的翻译，我需要完整的代码注释内容。请提供完整的代码注释段落，以便我能够为您翻译。
    /**
     * 解析与给定对象类型唯一匹配的bean实例（如果存在），包括其bean名称。
     * <p>这实际上是 {@link #getBean(Class)} 的一个变体，它保留了匹配实例的bean名称。
     * @param requiredType 必须匹配的bean类型；可以是一个接口或超类
     * @return bean名称加bean实例
     * @throws NoSuchBeanDefinitionException 如果未找到匹配的bean
     * @throws NoUniqueBeanDefinitionException 如果找到多个匹配的bean
     * @throws BeansException 如果无法创建bean
     * @since 4.3.3
     * @see #getBean(Class)
     */
    <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException;

    /**
     * 为给定的bean名称解析bean实例，提供依赖描述符以供目标工厂方法使用。
     * <p>这实际上是 {@link #getBean(String, Class)} 的一个变体，它支持带有 {@link org.springframework.beans.factory.InjectionPoint} 参数的工厂方法。
     * @param name 要查找的bean的名称
     * @param descriptor 请求注入点的依赖描述符
     * @return 相应的bean实例
     * @throws NoSuchBeanDefinitionException 如果没有指定名称的bean
     * @throws BeansException 如果无法创建bean
     * @since 5.1.5
     * @see #getBean(String, Class)
     */
    Object resolveBeanByName(String name, DependencyDescriptor descriptor) throws BeansException;

    /**
     * 解析指定依赖与在此工厂中定义的 Bean。
     * @param descriptor 依赖的描述符（字段/方法/构造函数）
     * @param requestingBeanName 声明给定依赖的 Bean 的名称
     * @return 解析出的对象，如果没有找到则返回 {@code null}
     * @throws NoSuchBeanDefinitionException 如果没有找到匹配的 Bean
     * @throws NoUniqueBeanDefinitionException 如果找到多个匹配的 Bean
     * @throws BeansException 如果由于其他原因依赖解析失败
     * @since 2.5
     * @see #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)
     */
    @Nullable
    Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException;

    /**
     * 解析指定依赖与在此工厂中定义的 Bean 之间的关联。
     * @param descriptor 依赖描述符（字段/方法/构造函数）
     * @param requestingBeanName 声明给定依赖的 Bean 的名称
     * @param autowiredBeanNames 一个 Set，所有自动装配的 Bean 名称（用于解析给定依赖）都应该添加到其中
     * @param typeConverter 用于填充数组或集合的 TypeConverter
     * @return 解析出的对象，如果没有找到则返回 {@code null}
     * @throws NoSuchBeanDefinitionException 如果没有找到匹配的 Bean
     * @throws NoUniqueBeanDefinitionException 如果找到多个匹配的 Bean
     * @throws BeansException 如果因其他原因导致依赖解析失败
     * @since 2.5
     * @see DependencyDescriptor
     */
    @Nullable
    Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName, @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException;
}
