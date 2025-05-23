// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可以使用此文件，但必须遵守许可证规定。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * 工厂钩子，允许自定义修改应用程序上下文的bean定义，适应上下文底层bean工厂的bean属性值。
 *
 * <p>对于针对系统管理员的定制配置文件，这些配置文件可以覆盖应用程序上下文中配置的bean属性。请参阅{@link PropertyResourceConfigurer}及其具体实现，它们提供了现成的解决方案来满足此类配置需求。
 *
 * <p>一个{@code BeanFactoryPostProcessor}可以与并修改bean定义，但永远不会与bean实例交互。这样做可能会导致bean实例化过早，违反容器规范，并引起未预期的副作用。如果需要与bean实例交互，请考虑实现{@link BeanPostProcessor}。
 *
 * <h3>注册</h3>
 * <p>一个{@code ApplicationContext}会自动检测其bean定义中的{@code BeanFactoryPostProcessor} bean，并在创建任何其他bean之前应用它们。一个{@code BeanFactoryPostProcessor}也可以通过一个{@code ConfigurableApplicationContext}进行程序性注册。
 *
 * <h3>排序</h3>
 * <p>在{@code ApplicationContext}中自动检测到的{@code BeanFactoryPostProcessor} bean将根据{@link org.springframework.core.PriorityOrdered}和{@link org.springframework.core.Ordered}语义进行排序。相比之下，通过一个{@code ConfigurableApplicationContext}程序性注册的{@code BeanFactoryPostProcessor} bean将按照注册顺序应用；对于程序性注册的后处理器，任何通过实现{@code PriorityOrdered}或{@code Ordered}接口表达的排序语义都将被忽略。此外，对于{@code BeanFactoryPostProcessor} bean，不考虑使用{@link org.springframework.core.annotation.Order @Order}注解。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 06.07.2003
 * @see BeanPostProcessor
 * @see PropertyResourceConfigurer
 */
@FunctionalInterface
public interface BeanFactoryPostProcessor {

    /**
     * 在应用程序上下文的标准化初始化之后修改其内部bean工厂。所有bean定义都将被加载，但尚未实例化任何bean。这允许覆盖或添加属性，甚至是对 eager-initializing beans。
     * @param beanFactory 应用程序上下文所使用的bean工厂
     * @throws org.springframework.beans.BeansException 在出现错误的情况下
     */
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
}
