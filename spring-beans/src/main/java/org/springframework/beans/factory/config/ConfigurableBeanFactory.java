// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”），除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

/**
 * 由大多数bean工厂实现的配置接口。提供了配置bean工厂的设施，除了在{@link org.springframework.beans.factory.BeanFactory}接口中的bean工厂客户端方法外。
 *
 * <p>此bean工厂接口不适用于常规应用程序代码：对于典型需求，请坚持使用{@link org.springframework.beans.factory.BeanFactory}或{@link org.springframework.beans.factory.ListableBeanFactory}。此扩展接口仅用于允许框架内部即插即用，并访问bean工厂配置方法。
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see ConfigurableListableBeanFactory
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

    /**
     * 标准单例作用域的标识符：{@value}。
     * <p>可以通过 {@code registerScope} 添加自定义作用域。
     * @see #registerScope
     */
    String SCOPE_SINGLETON = "singleton";

    /**
     * 标准原型作用域的作用域标识符：{@value}。
     * <p>可以通过 {@code registerScope} 方法添加自定义作用域。
     * @see #registerScope
     */
    String SCOPE_PROTOTYPE = "prototype";

    /**
     * 设置此 BeanFactory 的父类。
     * <p>注意，父类不能更改：它只能在工厂实例化时不可用的情况下在构造函数外部设置。
     * @param parentBeanFactory 父 BeanFactory
     * @throws IllegalStateException 如果此工厂已经与父 BeanFactory 关联
     * @see #getParentBeanFactory()
     */
    void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

    /**
     * 设置用于加载 Bean 类的类加载器。
     * 默认值为线程上下文类加载器。
     * <p>请注意，此类加载器仅适用于尚未携带已解析 Bean 类的 Bean 定义。这是从 Spring 2.0 默认开始的：Bean 定义仅携带 Bean 类名，将在工厂处理 Bean 定义时进行解析。
     * @param beanClassLoader 要使用的类加载器，
     * 或 {@code null} 以建议使用默认类加载器
     */
    void setBeanClassLoader(@Nullable ClassLoader beanClassLoader);

    /**
     * 返回此工厂的类加载器，用于加载 Bean 类
     * （只有在连系统类加载器都无法访问时才会是 {@code null}）。
     * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
     */
    @Nullable
    ClassLoader getBeanClassLoader();

    /**
     * 指定用于类型匹配目的的临时 ClassLoader。
     * 默认值为无，直接使用标准的 bean ClassLoader。
     * <p>通常只在涉及 <i>加载时编织</i> 时指定临时 ClassLoader，以确保实际 bean 类尽可能延迟加载。一旦 BeanFactory 完成其引导阶段，临时加载器就会被移除。
     * @since 2.5
     */
    void setTempClassLoader(@Nullable ClassLoader tempClassLoader);

    /**
     * 返回用于类型匹配目的的临时 ClassLoader，如果有的话。
     * @since 2.5
     */
    @Nullable
    ClassLoader getTempClassLoader();

    /**
     * 设置是否缓存豆元数据，例如给定的豆定义（以合并方式）和解析的豆类。默认为开启。
     * <p>关闭此标志以启用豆定义对象的热刷新，特别是豆类。如果此标志关闭，任何创建豆实例的操作都将重新查询豆类加载器以获取新解析的类。
     */
    void setCacheBeanMetadata(boolean cacheBeanMetadata);

    /**
     * 返回是否缓存诸如给定bean定义（以合并方式）和解析的bean类等bean元数据。
     */
    boolean isCacheBeanMetadata();

    /**
     * 指定在Bean定义值中表达式的解析策略。
     * <p>默认情况下，BeanFactory中没有激活表达式支持。
     * ApplicationContext通常会在这里设置一个标准的表达式策略，
     * 支持与Unified EL兼容风格的 "#{...}" 表达式。
     * @since 3.0
     */
    void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver);

    /**
     * 返回用于Bean定义值中表达式的解析策略。
     * @since 3.0
     */
    @Nullable
    BeanExpressionResolver getBeanExpressionResolver();

    /**
     * 指定一个用于转换属性值的 {@link ConversionService}，作为对 JavaBeans 属性编辑器的替代。
     * @since 3.0
     */
    void setConversionService(@Nullable ConversionService conversionService);

    /**
     * 返回关联的ConversionService，如果有的话。
     * @since 3.0
     */
    @Nullable
    ConversionService getConversionService();

    /**
     * 为所有Bean创建过程添加一个PropertyEditorRegistrar。
     * <p>此类注册器为每个Bean创建尝试创建新的PropertyEditor实例并将它们注册到指定的注册表中。这样避免了在自定义编辑器上同步的需要；因此，通常更倾向于使用此方法而不是使用{@link #registerCustomEditor}。
     * @param registrar 要注册的PropertyEditorRegistrar
     */
    void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);

    /**
     * 为给定类型的所有属性注册指定的自定义属性编辑器。该方法应在工厂配置期间调用。
     * <p>注意，此方法将注册一个共享的自定义编辑器实例；对该实例的访问将进行同步以保证线程安全。通常，使用 {@link #addPropertyEditorRegistrar} 而不是此方法更为可取，以避免对自定义编辑器进行同步的需求。
     * @param requiredType 属性的类型
     * @param propertyEditorClass 要注册的 {@link PropertyEditor} 类
     */
    void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);

    /**
     * 使用与该BeanFactory已注册的自定义编辑器初始化给定的PropertyEditorRegistry
     * @param registry 要初始化的PropertyEditorRegistry
     */
    void copyRegisteredEditorsTo(PropertyEditorRegistry registry);

    /**
     * 为这个BeanFactory设置一个自定义类型转换器，用于转换Bean属性值、构造函数参数值等。
     * <p>这将覆盖默认的PropertyEditor机制，因此任何自定义编辑器或自定义编辑器注册器都将变得无关紧要。
     * @since 2.5
     * @see #addPropertyEditorRegistrar
     * @see #registerCustomEditor
     */
    void setTypeConverter(TypeConverter typeConverter);

    /**
     * 获取由该BeanFactory使用的类型转换器。由于类型转换器通常不是线程安全的，因此每次调用可能会得到一个新的实例。
     * <p>如果默认的属性编辑器机制处于活动状态，返回的类型转换器将了解所有已注册的自定义编辑器。
     * @since 2.5
     */
    TypeConverter getTypeConverter();

    /**
     * 为嵌入式值（如注解属性）添加一个字符串解析器。
     * @param valueResolver 要应用于嵌入式值的字符串解析器
     * @since 3.0
     */
    void addEmbeddedValueResolver(StringValueResolver valueResolver);

    /**
     * 判断是否已将嵌入式值解析器注册到这个bean工厂中，以便通过`#resolveEmbeddedValue(String)`应用。
     * @since 4.3
     */
    boolean hasEmbeddedValueResolver();

    /**
     * 解决给定的嵌入式值，例如注解属性。
     * @param value 需要解决的值
     * @return 解决后的值（可能是原值本身）
     * @since 3.0
     */
    @Nullable
    String resolveEmbeddedValue(String value);

    /**
     * 添加一个新的BeanPostProcessor，该处理器将应用于由该工厂创建的bean。在工厂配置期间将被调用。
     * <p>注意：在此提交的处理器将按照注册顺序应用；通过实现{@link org.springframework.core.Ordered}接口表达的任何排序语义都将被忽略。请注意，自动检测到的处理器（例如，作为ApplicationContext中的bean）始终会在程序注册的处理器之后应用。
     * @param beanPostProcessor 要注册的处理器
     */
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    /**
     * 返回当前已注册的BeanPostProcessors的数量，如果有任何的话。
     */
    int getBeanPostProcessorCount();

    /**
     * 注册指定的作用域，该作用域由指定的Scope实现支持。
     * @param scopeName 作用域标识符
     * @param scope 支持的Scope实现
     */
    void registerScope(String scopeName, Scope scope);

    /**
     * 返回所有当前已注册作用域的名称。
     * <p>此方法仅返回显式注册的作用域名称。
     * 带内置作用域，如"singleton"和"prototype"，将不会公开。
     * @return 作用域名称数组，如果没有则返回空数组
     * @see #registerScope
     */
    String[] getRegisteredScopeNames();

    /**
     * 返回给定作用域名称的 Scope 实现，如果有的话。
     * <p>这只会返回显式注册的作用域。
     * 内置作用域，如 "singleton" 和 "prototype"，将不会被公开。
     * @param scopeName 作用域的名称
     * @return 已注册的 Scope 实现，如果没有则返回 {@code null}
     * @see #registerScope
     */
    @Nullable
    Scope getRegisteredScope(String scopeName);

    /**
     * 设置此bean工厂的{@code ApplicationStartup}。
     * <p>这允许应用程序上下文在应用程序启动期间记录指标。
     * @param applicationStartup 新的应用程序启动
     * @since 5.3
     */
    void setApplicationStartup(ApplicationStartup applicationStartup);

    /**
     * 返回此bean工厂的{@code ApplicationStartup}。
     * @since 5.3
     */
    ApplicationStartup getApplicationStartup();

    /**
     * 从给定的其他工厂复制所有相关配置。
     * <p>应包括所有标准配置设置，以及BeanPostProcessors、作用域和工厂特定的内部设置。
     * 不应包括任何实际bean定义的元数据，例如BeanDefinition对象和bean名称别名。
     * @param otherFactory 要从中复制的另一个BeanFactory
     */
    void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

    /**
     * 给定一个Bean名称，创建一个别名。我们通常使用这个方法来支持在XML ids（用于Bean名称）中非法的名称。
     * <p>通常在工厂配置期间调用此方法，但也可以用于运行时注册别名。因此，工厂实现应该同步别名访问。
     * @param beanName 目标Bean的规范名称
     * @param alias 为Bean注册的别名
     * @throws BeanDefinitionStoreException 如果别名已经被使用
     */
    void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;

    /**
     * 解析此工厂中所有别名目标名称和注册的别名，并将给定的 StringValueResolver 应用到它们上。
     * <p>值解析器可以解析目标bean名称中的占位符，甚至别名名称中的占位符。
     * @param valueResolver 要应用的字面值解析器
     * @since 2.5
     */
    void resolveAliases(StringValueResolver valueResolver);

    /**
     * 返回给定 bean 名称的合并 BeanDefinition，
     * 如果需要，将子 bean 定义与其父定义合并。
     * 考虑祖先工厂中的 bean 定义。
     * @param beanName 要检索合并定义的 bean 名称
     * @return 给定 bean 的（可能已合并的）BeanDefinition
     * @throws NoSuchBeanDefinitionException 如果不存在具有给定名称的 bean 定义
     * @since 2.5
     */
    BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 判断给定名称的Bean是否为FactoryBean。
     * @param name 要检查的Bean的名称
     * @return Bean是否为FactoryBean（返回值为{@code false}表示Bean存在但不是FactoryBean）
     * @throws NoSuchBeanDefinitionException 如果不存在给定名称的Bean
     * @since 2.5
     */
    boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

    /**
     * 显式控制指定 Bean 的当前创建状态。
     * 仅用于容器内部使用。
     * @param beanName Bean 的名称
     * @param inCreation Bean 是否当前处于创建状态
     * @since 3.1
     */
    void setCurrentlyInCreation(String beanName, boolean inCreation);

    /**
     * 判断指定的 Bean 是否当前处于创建状态。
     * @param beanName Bean 的名称
     * @return Bean 是否当前处于创建状态
     * @since 2.5
     */
    boolean isCurrentlyInCreation(String beanName);

    /**
     * 为指定的 bean 注册一个依赖的 bean，
     * 在指定 bean 销毁之前销毁。
     * @param beanName bean 的名称
     * @param dependentBeanName 依赖 bean 的名称
     * @since 2.5
     */
    void registerDependentBean(String beanName, String dependentBeanName);

    /**
     * 返回所有依赖于指定 Bean 的 Bean 名称，如果有的话。
     * @param beanName Bean 的名称
     * @return 依赖于 Bean 名称的数组，如果没有则返回空数组
     * @since 2.5
     */
    String[] getDependentBeans(String beanName);

    /**
     * 返回指定Bean所依赖的所有Bean的名称，如果有的话。
     * @param beanName Bean的名称
     * @return 返回依赖该Bean的Bean名称数组，
     * 如果没有依赖则返回一个空数组
     * @since 2.5
     */
    String[] getDependenciesForBean(String beanName);

    /**
     * 销毁指定的 Bean 实例（通常是此工厂获取的原型实例）根据其 Bean 定义。
     * <p>在销毁过程中出现的任何异常应被捕获并记录，而不是传播给此方法的调用者。
     * @param beanName Bean 定义的名称
     * @param beanInstance 要销毁的 Bean 实例
     */
    void destroyBean(String beanName, Object beanInstance);

    /**
     * 销毁当前目标作用域中指定的作用域Bean，如果存在的话。
     * <p>在销毁过程中产生的任何异常都应被捕获并记录，而不是传播给此方法调用者。
     * @param beanName 作用域Bean的名称
     */
    void destroyScopedBean(String beanName);

    /**
     * 销毁此工厂中的所有单例bean，包括已注册为可销毁的内部bean。应在工厂关闭时调用。
     * <p>在销毁过程中出现的任何异常都应被捕获并记录，而不是传播给此方法调用者。
     */
    void destroySingletons();
}
