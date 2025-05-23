// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途适用性的保证。
* 请参阅许可证以了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 访问 Spring 容器中 Spring Bean 的根接口。
 *
 * <p>这是 Bean 容器的基本客户端视图；针对特定目的，还提供了如 {@link ListableBeanFactory} 和
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory} 等更多接口。
 *
 * <p>此接口由包含多个 Bean 定义的对象实现，每个 Bean 由一个唯一的字符串名称标识。根据 Bean 定义，
 * 工厂将返回一个包含对象的单例实例（原型设计模式）或单个共享实例（优于 Singleton 设计模式，其中实例是工厂范围内的单例）。返回哪种类型的实例取决于 Bean 工厂配置：API 是相同的。自 Spring 2.0 以来，根据具体的应用上下文，还提供了更多的作用域（例如，在 Web 环境中的 "request" 和 "session" 作用域）。
 *
 * <p>这种方法的目的是 BeanFactory 是应用程序组件的中央注册表，并集中配置应用程序组件（不再需要单个对象读取属性文件等）。请参阅 "Expert One-on-One J2EE Design and Development" 的第 4 章和第 11 章，以了解这种方法的优点。
 *
 * <p>请注意，通常最好依赖于依赖注入（“推”配置）通过设置器或构造函数配置应用程序对象，而不是使用任何形式的“拉”配置，如 BeanFactory 查找。Spring 的依赖注入功能是通过此 BeanFactory 接口及其子接口实现的。
 *
 * <p>通常，BeanFactory 会加载存储在配置源（例如 XML 文档）中的 Bean 定义，并使用 {@code org.springframework.beans} 包来配置 Bean。然而，实现可以简单地直接在 Java 代码中返回它创建的 Java 对象。没有对如何存储定义的约束：LDAP、RDBMS、XML、属性文件等。鼓励实现支持 Bean 之间的引用（依赖注入）。
 *
 * <p>与 {@link ListableBeanFactory} 中的方法相比，此接口中的所有操作也将检查父工厂，如果此是 {@link HierarchicalBeanFactory}，则进行检查。如果在此工厂实例中找不到 Bean，则将询问直接父工厂。此工厂实例中的 Bean 应该覆盖任何父工厂中同名的 Bean。
 *
 * <p>Bean 工厂实现应尽可能支持标准的 Bean 生命周期接口。初始化方法的全套和它们的标准顺序如下：
 * <ol>
 * <li>BeanNameAware 的 {@code setBeanName}
 * <li>BeanClassLoaderAware 的 {@code setBeanClassLoader}
 * <li>BeanFactoryAware 的 {@code setBeanFactory}
 * <li>EnvironmentAware 的 {@code setEnvironment}
 * <li>EmbeddedValueResolverAware 的 {@code setEmbeddedValueResolver}
 * <li>ResourceLoaderAware 的 {@code setResourceLoader}
 * （仅适用于在应用程序上下文中运行）
 * <li>ApplicationEventPublisherAware 的 {@code setApplicationEventPublisher}
 * （仅适用于在应用程序上下文中运行）
 * <li>MessageSourceAware 的 {@code setMessageSource}
 * （仅适用于在应用程序上下文中运行）
 * <li>ApplicationContextAware 的 {@code setApplicationContext}
 * （仅适用于在应用程序上下文中运行）
 * <li>ServletContextAware 的 {@code setServletContext}
 * （仅适用于在 Web 应用程序上下文中运行）
 * <li>BeanPostProcessors 的 {@code postProcessBeforeInitialization} 方法
 * <li>InitializingBean 的 {@code afterPropertiesSet}
 * <li>自定义的 {@code init-method} 定义
 * <li>BeanPostProcessors 的 {@code postProcessAfterInitialization} 方法
 * </ol>
 *
 * <p>在 Bean 工厂关闭时，以下生命周期方法适用：
 * <ol>
 * <li>DestructionAwareBeanPostProcessors 的 {@code postProcessBeforeDestruction} 方法
 * <li>DisposableBean 的 {@code destroy}
 * <li>自定义的 {@code destroy-method} 定义
 * </ol>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 13 April 2001
 * @see BeanNameAware#setBeanName
 * @see BeanClassLoaderAware#setBeanClassLoader
 * @see BeanFactoryAware#setBeanFactory
 * @see org.springframework.context.EnvironmentAware#setEnvironment
 * @see org.springframework.context.EmbeddedValueResolverAware#setEmbeddedValueResolver
 * @see org.springframework.context.ResourceLoaderAware#setResourceLoader
 * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher
 * @see org.springframework.context.MessageSourceAware#setMessageSource
 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
 * @see org.springframework.web.context.ServletContextAware#setServletContext
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization
 * @see InitializingBean#afterPropertiesSet
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization
 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#postProcessBeforeDestruction
 * @see DisposableBean#destroy
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName
 */
public interface BeanFactory {

    /**
     * 用于取消引用一个 {@link FactoryBean} 实例，并将其与由 FactoryBean <i>创建</i> 的 Bean 区分开来。例如，如果名为 {@code myJndiObject} 的 Bean 是一个 FactoryBean，获取 {@code &myJndiObject} 将返回工厂，而不是工厂返回的实例。
     */
    String FACTORY_BEAN_PREFIX = "&";

    /**
     * 返回指定bean的实例，该实例可能是共享的或独立的。
     * <p>此方法允许Spring BeanFactory作为Singleton或Prototype设计模式的替代方案。在Singleton bean的情况下，调用者可以保留返回对象的引用。
     * <p>将别名转换回对应的规范bean名称。
     * <p>如果在此工厂实例中找不到bean，将询问父工厂。
     * @param name 要检索的bean的名称
     * @return bean的实例。
     * 注意，返回值永远不会为null，但可能是来自工厂方法的null的占位符，可以通过调用equals(null)来检查。考虑使用{@link #getBeanProvider(Class)}来解析可选依赖项。
     * @throws NoSuchBeanDefinitionException 如果没有指定名称的bean
     * @throws BeansException 如果无法获取bean
     */
    Object getBean(String name) throws BeansException;

    /**
     * 返回指定bean的实例，该实例可以是共享的或独立的。
     * <p>与{@link #getBean(String)}的行为相同，但通过抛出BeanNotOfRequiredTypeException异常来提供一定程度的安全保障，如果bean不是所需类型。这意味着在转换结果时不会抛出ClassCastException，这可能会在调用{@link #getBean(String)}时发生。
     * <p>将别名转换回相应的规范bean名称。
     * <p>如果在此工厂实例中找不到bean，将询问父工厂。
     * @param name 要检索的bean的名称
     * @param requiredType bean必须匹配的类型；可以是接口或超类
     * @return bean的实例。
     * 注意，返回值永远不会是{@code null}。如果为请求的bean解析了具有null占位符的工厂方法，则将针对NullBean占位符引发一个针对BeanNotOfRequiredTypeException的异常。
     * 考虑使用{@link #getBeanProvider(Class)}来解析可选依赖项。
     * @throws NoSuchBeanDefinitionException 如果没有这样的bean定义
     * @throws BeanNotOfRequiredTypeException 如果bean不是所需类型
     * @throws BeansException 如果无法创建bean
     */
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

    /**
     * 返回指定bean的实例，该实例可能是共享的或独立的。
     * <p>允许指定显式的构造函数参数/工厂方法参数，
     * 覆盖bean定义中指定的默认参数（如果有）。
     * @param name 要检索的bean的名称
     * @param args 当使用显式参数创建bean实例时使用的参数
     * （仅在创建新实例时应用，而不是检索现有实例）
     * @return bean的实例
     * @throws NoSuchBeanDefinitionException 如果不存在该bean定义
     * @throws BeanDefinitionStoreException 如果已给出参数，但受影响的bean不是原型
     * @throws BeansException 如果无法创建bean
     * @since 2.5
     */
    Object getBean(String name, Object... args) throws BeansException;

    /**
     * 返回唯一匹配给定对象类型的bean实例，如果存在的话。
     * <p>此方法进入 {@link ListableBeanFactory} 的按类型查找领域，但也可以根据给定类型的名称转换为传统的按名称查找。对于更广泛的跨bean集合的检索操作，请使用 {@link ListableBeanFactory} 和/或 {@link BeanFactoryUtils}。
     * @param requiredType 必须匹配的bean类型；可以是一个接口或超类
     * @return 单个匹配所需类型的bean的实例
     * @throws NoSuchBeanDefinitionException 如果未找到给定类型的bean
     * @throws NoUniqueBeanDefinitionException 如果找到多个给定类型的bean
     * @throws BeansException 如果bean无法创建
     * @since 3.0
     * @see ListableBeanFactory
     */
    <T> T getBean(Class<T> requiredType) throws BeansException;

    /**
     * 返回指定bean的一个实例，该实例可以是共享的或独立的。
     * <p>允许指定显式的构造函数参数/工厂方法参数，覆盖bean定义中指定的默认参数（如果有的话）。
     * <p>此方法进入基于类型的 {@link ListableBeanFactory} 查找区域，但也可以根据给定类型的名称转换为传统的按名称查找。对于更广泛的bean集合检索操作，请使用 {@link ListableBeanFactory} 和/或 {@link BeanFactoryUtils}。
     * @param requiredType bean必须匹配的类型；可以是接口或超类
     * @param args 当使用显式参数创建bean实例时使用的参数（仅在创建新实例时应用，而不是检索现有实例时）
     * @return bean的一个实例
     * @throws NoSuchBeanDefinitionException 如果没有这样的bean定义
     * @throws BeanDefinitionStoreException 如果已给出参数，但受影响的bean不是原型
     * @throws BeansException 如果无法创建bean
     * @since 4.1
     */
    <T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

    /**
     * 返回指定bean的提供者，允许在需要时按需检索实例，包括可用性和唯一性选项。
     * <p>对于匹配泛型类型，请考虑使用{@link #getBeanProvider(ResolvableType)}。
     * @param requiredType bean必须匹配的类型；可以是一个接口或超类
     * @return 相应的提供者句柄
     * @since 5.1
     * @see #getBeanProvider(ResolvableType)
     */
    <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);

    /**
     *  返回指定bean的提供者，允许按需延迟检索实例，包括可用性和唯一性选项。此变体允许指定一个泛型类型以匹配，类似于在方法/构造函数参数中的泛型类型声明中使用的反射注入点。
     * 	<p>请注意，此处不支持bean的集合，与反射注入点不同。对于以编程方式检索匹配特定类型的bean列表，请在此处指定实际的bean类型作为参数，然后使用{@link ObjectProvider#orderedStream()}或其懒加载流/迭代选项。
     * 	<p>此外，泛型匹配是严格的，符合Java赋值规则。如果在此变体中没有完全的泛型匹配可用，并且希望进行宽松的回退匹配（类似于'unchecked' Java编译器警告），则在没有完全泛型匹配的情况下，可以考虑在第二步中调用{@link #getBeanProvider(Class)}，将原始类型作为第二个参数。
     * 	@return 相应的提供者句柄
     * 	@param requiredType bean必须匹配的类型；可以是泛型类型声明
     * 	@since 5.1
     * 	@see ObjectProvider#iterator()
     * 	@see ObjectProvider#stream()
     * 	@see ObjectProvider#orderedStream()
     */
    <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType);

    /**
     * 此Bean工厂是否包含具有给定名称的Bean定义或外部注册的单例实例？
     * <p>如果给定的名称是一个别名，它将被转换回相应的规范Bean名称。
     * <p>如果此工厂是分层的，当在当前工厂实例中找不到Bean时，将询问任何父工厂。
     * <p>如果找到匹配给定名称的Bean定义或单例实例，则此方法将返回true，无论该命名Bean定义是具体的还是抽象的，是懒加载的还是 eager 加载的，是否有作用域或没有。因此，请注意，此方法返回true并不一定意味着可以通过# getBean获取相同名称的实例。
     * @param name 要查询的Bean的名称
     * @return 是否存在具有给定名称的Bean
     */
    boolean containsBean(String name);

    /**
     * 这个Bean是否是一个共享的单例？也就是说，`#getBean`方法是否总是返回相同的实例？
     * <p>注意：此方法返回`false`并不明确表示独立的实例。它表示非单例实例，这些实例可能也对应于作用域Bean。请使用`#isPrototype`操作来显式检查独立实例。
     * <p>将别名转换回相应的规范Bean名称。
     * <p>如果在此工厂实例中找不到该Bean，将询问父工厂。
     * @param name 要查询的Bean的名称
     * @return 此Bean是否对应于单例实例
     * @throws NoSuchBeanDefinitionException 如果不存在具有给定名称的Bean
     * @see #getBean
     * @see #isPrototype
     */
    boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

    /**
     * 这个Bean是否是原型？也就是说，`#getBean`方法是否总是返回独立的实例？
     * <p>注意：此方法返回`false`并不明确表示单例对象。它表示非独立实例，这可能与作用域Bean相对应。请使用`#isSingleton`操作来显式检查是否存在共享的单例实例。
     * <p>将别名转换回相应的规范Bean名称。
     * <p>如果在本工厂实例中找不到Bean，将询问父工厂。
     * @param name 要查询的Bean的名称
     * @return 此Bean是否总是提供独立的实例
     * @throws NoSuchBeanDefinitionException 如果不存在具有给定名称的Bean
     * @since 2.0.3
     * @see #getBean
     * @see #isSingleton
     */
    boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

    /**
     * 检查给定名称的bean是否与指定的类型匹配。
     * 更具体地说，检查对给定名称的 {@link #getBean} 调用是否会返回一个可以赋值给指定目标类型的对象。
     * <p>将别名转换回相应的规范bean名称。
     * <p>如果在此工厂实例中找不到bean，将询问父工厂。
     * @param name 要查询的bean的名称
     * @param typeToMatch 要匹配的类型（作为 {@code ResolvableType}）
     * @return 如果bean类型匹配，则返回 {@code true}，
     * 如果不匹配或尚不能确定，则返回 {@code false}
     * @throws NoSuchBeanDefinitionException 如果不存在具有给定名称的bean
     * @since 4.2
     * @see #getBean
     * @see #getType
     */
    boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;

    /**
     * 检查给定名称的bean是否与指定的类型匹配。
     * 更具体地说，检查对给定名称的getBean调用是否会返回一个可以赋值给指定目标类型的对象。
     * <p>将别名转换回相应的规范bean名称。
     * <p>如果在此工厂实例中找不到bean，将询问父工厂。
     * @param name 要查询的bean的名称
     * @param typeToMatch 要匹配的类型（作为Class）
     * @return 如果bean类型匹配，则返回true，
     * 如果不匹配或尚不能确定，则返回false
     * @throws NoSuchBeanDefinitionException 如果没有给定名称的bean
     * @since 2.0.1
     * @see #getBean
     * @see #getType
     */
    boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

    /**
     * 确定具有给定名称的bean类型。更具体地说，确定对于给定名称，`#getBean`方法将返回的对象类型。
     * 对于一个`FactoryBean`，返回由`FactoryBean`创建的对象类型，该类型通过`FactoryBean#getObjectType()`暴露。这可能导致之前未初始化的`FactoryBean`被初始化（参见`#getType(String, boolean)`）。
     * <p>将别名转换回相应的规范bean名称。
     * <p>如果在此工厂实例中找不到bean，将询问父工厂。
     * @param name 要查询的bean的名称
     * @return bean的类型，如果不可确定则返回`null`
     * @throws NoSuchBeanDefinitionException 如果没有具有给定名称的bean
     * @since 1.1.2
     * @see #getBean
     * @see #isTypeMatch
     */
    @Nullable
    Class<?> getType(String name) throws NoSuchBeanDefinitionException;

    /**
     * 确定给定名称的bean类型。更具体地说，确定对于给定的名称，`getBean`方法会返回的对象类型。
     * 对于一个`FactoryBean`，返回`FactoryBean`创建的对象类型，该类型通过`FactoryBean#getObjectType()`暴露。
     * 根据`allowFactoryBeanInit`标志，如果不存在早期类型信息，这可能会导致之前未初始化的`FactoryBean`被初始化。
     * <p>将别名转换回相应的规范bean名称。
     * <p>如果在此工厂实例中找不到bean，将询问父工厂。
     * @param name 要查询的bean的名称
     * @param allowFactoryBeanInit 是否可以仅为确定对象类型而初始化`FactoryBean`
     * @return bean的类型，如果不可确定则返回`null`
     * @throws NoSuchBeanDefinitionException 如果没有给定名称的bean
     * @since 5.2
     * @see #getBean
     * @see #isTypeMatch
     */
    @Nullable
    Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException;

    /**
     * 返回给定bean名称的别名（如果有）。
     * <p>所有这些别名在使用于调用 {@link #getBean} 时都指向同一个bean。
     * <p>如果给定的名称是一个别名，则将返回相应的原始bean名称和其他别名（如果有），其中原始bean名称为数组的第一个元素。
     * <p>如果在此工厂实例中找不到bean，将询问父工厂。
     * @param name 要检查别名的bean名称
     * @return 别名，如果没有则返回空数组
     * @see #getBean
     */
    String[] getAliases(String name);
}
