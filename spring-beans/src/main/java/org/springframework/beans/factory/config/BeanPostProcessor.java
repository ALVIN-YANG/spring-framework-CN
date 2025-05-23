// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或原作者。
*
* 根据 Apache License 2.0 ("许可协议") 进行许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * 工厂钩子，允许自定义修改新的Bean实例 —— 例如，检查标记接口或使用代理包装Bean。
 *
 * <p>通常，通过标记接口或类似方式填充Bean的后处理器实现 {@link #postProcessBeforeInitialization}，
 * 而使用代理包装Bean的后处理器通常实现 {@link #postProcessAfterInitialization}。
 *
 * <h3>注册</h3>
 * <p>一个 {@code ApplicationContext} 可以自动检测其bean定义中的 {@code BeanPostProcessor} Bean，
 * 并将这些后处理器应用于随后创建的任何Bean。一个普通的 {@code BeanFactory} 允许以编程方式注册后处理器，
 * 将它们应用于通过bean工厂创建的所有Bean。
 *
 * <h3>排序</h3>
 * <p>在 {@code ApplicationContext} 中自动检测到的 {@code BeanPostProcessor} Bean将根据
 * {@link org.springframework.core.PriorityOrdered} 和 {@link org.springframework.core.Ordered} 语义进行排序。
 * 相反，通过将一个 {@code BeanPostProcessor} Bean以编程方式注册到一个 {@code BeanFactory}，将按注册顺序应用；
 * 通过实现 {@code PriorityOrdered} 或 {@code Ordered} 接口表达出的任何排序语义都将被忽略。
 * 此外，对于编程方式注册的后处理器，不考虑使用
 * {@link org.springframework.core.annotation.Order @Order} 注解。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 10.10.2003
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
public interface BeanPostProcessor {

    /**
     * 将此 {@code BeanPostProcessor} 应用到给定的新豆实例上，在 <i>任何</i> 豆初始化回调（如 InitializingBean 的 {@code afterPropertiesSet} 或自定义的 init-method）之前。豆实例已经填充了属性值。返回的豆实例可能是原始实例的包装器。
     * <p>默认实现返回给定的 {@code bean} 不变。
     * @param bean 新的豆实例
     * @param beanName 豆的名称
     * @return 要使用的豆实例，可以是原始实例或包装实例；如果为 {@code null}，则不会调用后续的 BeanPostProcessors
     * @throws org.springframework.beans.BeansException 如果发生错误
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
     */
    @Nullable
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 将此 {@code BeanPostProcessor} 应用到给定的新 bean 实例上，在 <i>任何</i> bean 初始化回调（如 InitializingBean 的 {@code afterPropertiesSet} 或自定义的 init-method）之后。bean 已经填充了属性值。返回的 bean 实例可能是原始实例的包装器。
     * <p>在 FactoryBean 的情况下，从 Spring 2.0 开始，此回调将针对 FactoryBean 实例以及 FactoryBean 创建的对象调用。后处理器可以决定是否应用于 FactoryBean 或创建的对象，或者两者都通过相应的 {@code bean instanceof FactoryBean} 检查。
     * <p>与所有其他 {@code BeanPostProcessor} 回调不同，此回调还会在由 {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} 方法触发的短路后调用。
     * <p>默认实现返回给定的 {@code bean} 不变。
     * @param bean 新的 bean 实例
     * @param beanName bean 的名称
     * @return 要使用的 bean 实例，可以是原始实例或包装实例；如果为 {@code null}，则不会调用后续的 BeanPostProcessors
     * @throws org.springframework.beans.BeansException 在出现错误的情况下
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
     * @see org.springframework.beans.factory.FactoryBean
     */
    @Nullable
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
