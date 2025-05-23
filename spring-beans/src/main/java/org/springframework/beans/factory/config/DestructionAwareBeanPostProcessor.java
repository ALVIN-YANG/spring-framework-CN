// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * 是 {@link BeanPostProcessor} 的子接口，它添加了一个销毁前的回调。
 *
 * <p>典型用法是在特定的bean类型上调用自定义的销毁回调，与相应的初始化回调相对应。
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

    /**
     * 在给定的bean实例销毁之前应用此BeanPostProcessor，例如调用自定义的销毁回调。
     * <p>类似于DisposableBean的destroy方法和自定义的销毁方法，此回调仅适用于容器完全管理生命周期的bean。这通常适用于单例和作用域bean。
     * @param bean 要销毁的bean实例
     * @param beanName bean的名称
     * @throws org.springframework.beans.BeansException 在出错的情况下
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     * @see org.springframework.beans.factory.support.AbstractBeanDefinition#setDestroyMethodName(String)
     */
    void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;

    /**
     * 判断给定的 Bean 实例是否需要由这个后处理器进行销毁。
     * <p>默认实现返回 {@code true}。如果预 5 版本的 {@code DestructionAwareBeanPostProcessor} 没有提供这个方法的具体实现，Spring 会静默地假设返回值为 {@code true}。
     * @param bean 要检查的 Bean 实例
     * @return 如果最终需要为这个 Bean 实例调用 {@link #postProcessBeforeDestruction}，则返回 {@code true}，否则返回 {@code false}
     * @since 4.3
     */
    default boolean requiresDestruction(Object bean) {
        return true;
    }
}
