// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”），您可能不得使用此文件除非遵守许可证。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“现状”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.config;

import java.lang.reflect.Constructor;
import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * 扩展了 {@link InstantiationAwareBeanPostProcessor} 接口，
 * 增加了一个用于预测处理过的 Bean 最终类型的回调。
 *
 * <p><b>注意：</b>此接口是一个专用接口，主要用于框架内部的内部使用。通常，
 * 应用程序提供的后处理器应简单地实现普通的 {@link BeanPostProcessor} 接口。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 */
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {

    /**
     * 预测从该处理器`#postProcessBeforeInstantiation`回调最终返回的bean类型。
     * 默认实现返回`null`。
     * 特定实现应尽可能预测bean类型，已知/已缓存，而不需要额外的处理步骤。
     * @param beanClass bean的原始类
     * @param beanName bean的名称
     * @return bean的类型，或如果不可预测则返回`null`
     * @throws org.springframework.beans.BeansException 在出现错误的情况下
     */
    @Nullable
    default Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * 确定从本处理器`#postProcessBeforeInstantiation`回调最终返回的bean类型。
     * 默认实现直接返回给定的bean类。
     * 特定实现应完全评估其处理步骤，以便提前创建/初始化潜在的代理类。
     * @param beanClass bean的原始类
     * @param beanName bean的名称
     * @return bean的类型（永不返回null）
     * @throws org.springframework.beans.BeansException 如果出现错误
     * @since 6.0
     */
    default Class<?> determineBeanType(Class<?> beanClass, String beanName) throws BeansException {
        return beanClass;
    }

    /**
     * 确定用于给定 Bean 的候选构造函数。
     * <p>默认实现返回 {@code null}。
     * @param beanClass Bean 的原始类（永远不会为 {@code null}）
     * @param beanName Bean 的名称
     * @return 候选构造函数，如果没有指定则返回 {@code null}
     * @throws org.springframework.beans.BeansException 在出错的情况下
     */
    @Nullable
    default Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * 获取对指定bean的早期访问引用，
     * 通常用于解决循环引用问题。
     * <p>此回调允许后处理器有机会提前暴露一个包装器 -
     * 即在目标bean实例完全初始化之前。暴露的对象应与
     * {@link #postProcessBeforeInitialization} / {@link #postProcessAfterInitialization}
     * 产生的对象等效。请注意，此方法返回的对象将被用作bean引用，
     * 除非后处理器从上述后处理回调中返回不同的包装器。换句话说：
     * 那些后处理回调可能最终暴露相同的引用，或者作为替代
     * 在后续回调中返回原始bean实例（如果已为调用此方法构建了受影响bean的包装器，
     * 它将默认作为最终的bean引用暴露）。
     * <p>默认实现返回给定的bean原样。
     * @param bean 原始bean实例
     * @param beanName bean的名称
     * @return 要暴露为bean引用的对象
     * （通常使用传入的bean实例作为默认值）
     * @throws org.springframework.beans.BeansException 在出现错误的情况下
     */
    default Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
