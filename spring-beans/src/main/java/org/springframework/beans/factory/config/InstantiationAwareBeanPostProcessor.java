// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.lang.Nullable;

/**
 * 该接口是 {@link BeanPostProcessor} 的子接口，它添加了实例化前的回调和实例化后的回调，在显式设置属性或自动装配发生之前。
 *
 * <p>通常用于抑制特定目标Bean的默认实例化，例如创建具有特殊目标源（池化目标、延迟初始化目标等）的代理，或者实现如字段注入等额外的注入策略。
 *
 * <p><b>注意：</b>此接口是一个专用接口，主要用于框架内部使用。尽可能实现普通的 {@link BeanPostProcessor} 接口。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 1.2
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators
 * @see org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    /**
     * 在目标Bean被实例化之前应用此BeanPostProcessor <i>。</i>
     * 返回的Bean对象可能是一个代理，可以用来替代目标Bean，从而有效抑制目标Bean的默认实例化。
     * <p>如果此方法返回一个非空对象，则Bean创建过程将被短路。唯一进一步应用的处理是配置的
     * {@link BeanPostProcessor BeanPostProcessors} 的 {@link #postProcessAfterInitialization} 回调。
     * <p>此回调将应用于具有其Bean类的Bean定义，以及在工厂方法定义的情况下，此时将传递返回的Bean类型。
     * <p>后处理器可以实现扩展的
     * {@link SmartInstantiationAwareBeanPostProcessor} 接口，以便预测他们将要在这里返回的Bean对象的类型。
     * <p>默认实现返回 {@code null}。
     * @param beanClass 要实例化的Bean的类
     * @param beanName Bean的名称
     * @return 替代目标Bean默认实例的Bean对象，或返回 {@code null} 以继续默认实例化
     * @throws org.springframework.beans.BeansException 在出现错误的情况下
     * @see #postProcessAfterInstantiation
     * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getBeanClass()
     * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getFactoryMethodName()
     */
    @Nullable
    default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * 在通过构造函数或工厂方法实例化 Bean 之后，但在 Spring 属性填充（来自显式属性或自动装配）发生之前执行操作。
     * <p>这是在给定 Bean 实例上执行自定义字段注入的理想回调，就在 Spring 的自动装配启动之前。
     * <p>默认实现返回 {@code true}。
     * @param bean 创建的 Bean 实例，属性尚未设置
     * @param beanName Bean 的名称
     * @return 如果应在 Bean 上设置属性，则返回 {@code true}；如果应跳过属性填充，则返回 {@code false}。正常实现应返回 {@code true}。
     * 返回 {@code false} 也会阻止任何后续的 InstantiationAwareBeanPostProcessor 实例被调用在此 Bean 实例上。
     * @throws org.springframework.beans.BeansException 在出现错误的情况下
     * @see #postProcessBeforeInstantiation
     */
    default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

    /**
     * 在工厂应用给定属性值到指定bean之前，对这些属性值进行后处理。
     * <p>默认实现返回给定的 {@code pvs} 不变。
     * @param pvs 工厂即将应用的属性值（绝不会为 {@code null}）
     * @param bean 已创建的bean实例，但其属性尚未设置
     * @param beanName bean的名称
     * @return 实际要应用到给定bean上的属性值（可以是传入的 PropertyValues 实例），或返回 {@code null} 以跳过属性填充
     * @throws org.springframework.beans.BeansException 在出错情况下抛出
     * @since 5.1
     */
    @Nullable
    default PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        return pvs;
    }
}
