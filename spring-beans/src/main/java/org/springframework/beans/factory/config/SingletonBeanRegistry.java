// 翻译完成 glm-4-flash
/** 版权所有 2002-2015 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.config;

import org.springframework.lang.Nullable;

/**
 * 定义共享 Bean 实例注册表的接口。
 * 可以被 {@link org.springframework.beans.factory.BeanFactory} 实现类所实现，
 * 以统一的方式公开它们的单例管理功能。
 *
 * <p>该 {@link ConfigurableBeanFactory} 接口扩展了此接口。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ConfigurableBeanFactory
 * @see org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 */
public interface SingletonBeanRegistry {

    /**
     * 将给定的现有对象注册为单例到bean注册表中，名称为给定的bean名称。
     * 给定的实例应已完全初始化；注册表将不会执行任何初始化回调（特别是，它不会调用InitializingBean的afterPropertiesSet方法）。
     * 给定的实例也不会收到任何销毁回调（如DisposableBean的destroy方法）。
     * <p>当在完整的BeanFactory中运行时：<b>如果你的bean需要接收初始化和/或销毁回调，请注册一个bean定义而不是现有实例。</b>
     * <p>通常在注册表配置期间调用，但也可用于单例的运行时注册。因此，注册表实现应同步单例访问；如果它支持BeanFactory的单例懒加载初始化，它无论如何都需要这样做。
     * @param beanName bean的名称
     * @param singletonObject 现有的单例对象
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
     * @see org.springframework.beans.factory.DisposableBean#destroy
     * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#registerBeanDefinition
     */
    void registerSingleton(String beanName, Object singletonObject);

    /**
     * 返回给定名称下注册的（原始）单例对象。
     * <p>仅检查已实例化的单例；对于尚未实例化的单例bean定义，不会返回对象。
     * <p>此方法的主要目的是访问手动注册的单例（参见 {@link #registerSingleton}）。
     * 它还可以用于以原始方式访问已经创建的bean定义定义的单例。
     * <p><b>注意：</b>此查找方法不了解FactoryBean前缀或别名。
     * 在获取单例实例之前，您需要先解决规范bean名称。
     * @param beanName 要查找的bean的名称
     * @return 注册的单例对象，如果没有找到则返回 {@code null}
     * @see ConfigurableListableBeanFactory#getBeanDefinition
     */
    @Nullable
    Object getSingleton(String beanName);

    /**
     * 检查此注册表中是否包含一个具有给定名称的单例实例。
     * <p>仅检查已经实例化的单例；不会对尚未实例化的单例bean定义返回 {@code true}。
     * <p>此方法的主要目的是检查手动注册的单例（参见 {@link #registerSingleton}）。也可以用来检查由bean定义定义的单例是否已经被创建。
     * <p>要检查bean工厂是否包含具有给定名称的bean定义，请使用ListableBeanFactory的{@code containsBeanDefinition}。调用{@code containsBeanDefinition}和{@code containsSingleton}都可以回答特定的bean工厂是否包含具有给定名称的本地bean实例。
     * <p>使用BeanFactory的{@code containsBean}进行一般检查，以确定工厂是否知道具有给定名称的bean（是手动注册的单例实例还是由bean定义创建的），也会检查祖先工厂。
     * <p><b>注意：</b>此查找方法不了解FactoryBean前缀或别名。在检查单例状态之前，您需要先解决规范化的bean名称。
     * @param beanName 要查找的bean的名称
     * @return 如果此bean工厂包含具有给定名称的单例实例
     * @see #registerSingleton
     * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition
     * @see org.springframework.beans.factory.BeanFactory#containsBean
     */
    boolean containsSingleton(String beanName);

    /**
     * 返回在此注册表中注册的单例bean的名称。
     * <p>仅检查已实例化的单例；不返回尚未实例化的单例bean定义的名称。
     * <p>此方法的主要目的是检查手动注册的单例（参见{@link #registerSingleton}）。也可以用来检查由bean定义定义的单例哪些已经被创建。
     * @return 以字符串数组形式返回的名称列表（永远不会为{@code null}）
     * @see #registerSingleton
     * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#getBeanDefinitionNames
     * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionNames
     */
    String[] getSingletonNames();

    /**
     * 返回在此注册表中注册的单例Bean的数量。
     * <p>仅检查已实例化的单例；不计入尚未实例化的单例Bean定义。
     * <p>此方法的主要目的是检查手动注册的单例（见{@link #registerSingleton}）。也可以用来计算由Bean定义定义的单例数量，这些单例已经创建。
     * @return 单例Bean的数量
     * @see #registerSingleton
     * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#getBeanDefinitionCount
     * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionCount
     */
    int getSingletonCount();

    /**
     * 返回此注册表使用的单例互斥锁（供外部协作者使用）。
     * @return 互斥锁对象（绝不返回 {@code null}）
     * @since 4.2
     */
    Object getSingletonMutex();
}
