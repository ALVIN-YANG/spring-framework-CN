// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 用于在运行时处理<i>合并</i>的Bean定义的后处理器回调接口。
 * 实现{@link BeanPostProcessor}的类可以通过实现这个子接口来后处理Spring {@code BeanFactory}用于创建Bean实例的合并Bean定义（原始Bean定义的已处理副本）。
 *
 * <p>例如，可以使用{@link #postProcessMergedBeanDefinition}方法来检查Bean定义，以便在处理Bean的实际实例之前准备一些缓存的元数据。还允许修改Bean定义，但<i>仅</i>针对实际意图用于并发修改的定义属性。本质上，这仅适用于定义在{@link RootBeanDefinition}本身上的操作，而不适用于其基类的属性。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getMergedBeanDefinition
 */
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

    /**
     * 对指定Bean的合并后的Bean定义进行后处理。
     * @param beanDefinition 合并后的Bean定义
     * @param beanType 管理Bean实例的实际类型
     * @param beanName Bean的名称
     * @see AbstractAutowireCapableBeanFactory#applyMergedBeanDefinitionPostProcessors
     */
    void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);

    /**
     * 通知指定名称的Bean定义已被重置，并且这个后处理器应该清除受影响Bean的任何元数据。
     * <p>默认实现为空。
     * @param beanName Bean的名称
     * @since 5.1
     * @see DefaultListableBeanFactory#resetBeanDefinition
     */
    default void resetBeanDefinition(String beanName) {
    }
}
