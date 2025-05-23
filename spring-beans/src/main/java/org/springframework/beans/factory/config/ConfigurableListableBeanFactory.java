// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.util.Iterator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.lang.Nullable;

/**
 * 应由大多数可列表的bean工厂实现的配置接口。
 * 除了实现{@link ConfigurableBeanFactory}外，它还提供了分析和管理bean定义的设施，以及预实例化单例的功能。
 *
 * <p>这个继承自{@link org.springframework.beans.factory.BeanFactory}的子接口并不适合在正常的应用程序代码中使用：对于典型用例，请坚持使用
 * {@link org.springframework.beans.factory.BeanFactory}或{@link org.springframework.beans.factory.ListableBeanFactory}。
 * 这个接口只是为了允许框架内部实现即插即用，即使需要访问bean工厂配置方法。
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.context.support.AbstractApplicationContext#getBeanFactory()
 */
public interface ConfigurableListableBeanFactory extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

    /**
     * 忽略自动装配给定的依赖类型：
     * 例如，String。默认为无。
     * @param type 要忽略的依赖类型
     */
    void ignoreDependencyType(Class<?> type);

    /**
     * 忽略指定的依赖接口进行自动装配。
     * <p>这通常由应用程序上下文用于注册通过其他方式解决的依赖项，例如通过BeanFactory通过BeanFactoryAware或通过ApplicationContext通过ApplicationContextAware。
     * <p>默认情况下，只有BeanFactoryAware接口被忽略。
     * 对于要忽略的其他类型，为每个类型调用此方法。
     * @param ifc 要忽略的依赖接口
     * @see org.springframework.beans.factory.BeanFactoryAware
     * @see org.springframework.context.ApplicationContextAware
     */
    void ignoreDependencyInterface(Class<?> ifc);

    /**
     * 注册一个特殊的依赖类型及其对应的自动装配值。
     * <p>此功能旨在为那些应可自动装配但未在工厂中定义为bean的工厂/上下文引用注册：
     * 例如，类型为ApplicationContext的依赖项解析为包含该bean的ApplicationContext实例。
     * <p>注意：在普通的BeanFactory中未注册此类默认类型，甚至包括BeanFactory接口本身。
     * @param dependencyType 要注册的依赖类型。这通常是一个基础接口，如BeanFactory，如果声明为自动装配依赖项（例如ListableBeanFactory），则还会解析其扩展接口，只要给定的值实现了扩展接口。
     * @param autowiredValue 对应的自动装配值。这也可以是实现org.springframework.beans.factory.ObjectFactory接口的实现，它允许对实际目标值的延迟解析。
     */
    void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue);

    /**
     * 判断指定的 Bean 是否符合自动装配候选条件，以便将其注入声明了匹配类型依赖的其他 Bean 中。
     * <p>此方法还会检查祖先工厂。
     * @param beanName 要检查的 Bean 的名称
     * @param descriptor 要解决的依赖描述符
     * @return Bean 是否应该被视为自动装配候选
     * @throws NoSuchBeanDefinitionException 如果不存在具有给定名称的 Bean
     */
    boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException;

    /**
     * 返回指定Bean的注册BeanDefinition，允许访问其属性值和构造函数参数值（这些值可以在Bean工厂后处理过程中进行修改）。
     * <p>返回的BeanDefinition对象不应是副本，而应是在工厂中注册的原始定义对象。这意味着如果有必要，它应该可以被转换为更具体的实现类型。
     * <p><b>注意：</b>此方法不考虑父工厂。它仅用于访问此工厂的本地Bean定义。
     * @param beanName Bean的名称
     * @return 注册的BeanDefinition
     * @throws NoSuchBeanDefinitionException 如果此工厂中没有定义具有给定名称的Bean
     */
    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 返回由该工厂管理的所有bean名称的统一视图。
     * <p>包括bean定义名称以及手动注册的单例实例的名称，其中bean定义名称始终排在前面，
     * 类似于如何通过类型/注解特定的bean名称检索工作。
     * @return 用于bean名称视图的复合迭代器
     * @since 4.1.2
     * @see #containsBeanDefinition
     * @see #registerSingleton
     * @see #getBeanNamesForType
     * @see #getBeanNamesForAnnotation
     */
    Iterator<String> getBeanNamesIterator();

    /**
     * 清除合并的 Bean 定义缓存，移除尚未被视为适合完全元数据缓存的 Bean 的条目。
     * <p>通常在原始 Bean 定义更改后触发，例如在应用了 {@link BeanFactoryPostProcessor} 之后。请注意，此时已经创建的 Bean 的元数据将被保留。
     * @since 4.2
     * @see #getBeanDefinition
     * @see #getMergedBeanDefinition
     */
    void clearMetadataCache();

    /**
     * 冻结所有 Bean 定义，表示已注册的 Bean 定义将不会被进一步修改或后处理。
     * <p>这允许工厂在清除初始临时元数据缓存后，对 Bean 定义元数据进行积极缓存。
     * @see #clearMetadataCache()
     * @see #isConfigurationFrozen()
     */
    void freezeConfiguration();

    /**
     * 返回此工厂的bean定义是否已冻结，
     * 即不应该再进行修改或进一步的后处理。
     * @return 如果工厂的配置被认为是冻结的，则返回 {@code true}
     * @see #freezeConfiguration()
     */
    boolean isConfigurationFrozen();

    /**
     * 确保所有非懒加载的单例被实例化，同时考虑
     * {@link org.springframework.beans.factory.FactoryBean FactoryBeans}。
     * 通常在工厂设置结束时调用，如果需要的话。
     * @throws BeansException 如果某个单例bean无法被创建。
     * 注意：这可能会让工厂中的一些bean已经初始化！
     * 在这种情况下，调用 {@link #destroySingletons()} 进行完整清理。
     * @see #destroySingletons()
     */
    void preInstantiateSingletons() throws BeansException;
}
