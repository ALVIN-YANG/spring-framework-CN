// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory;

/**
 * 由需要在其所有属性被 {@link BeanFactory} 设置后进行反应的 bean 实现的接口：例如执行自定义初始化，或者仅仅是检查是否已设置所有必需的属性。
 *
 * <p>实现 {@code InitializingBean} 的另一种方法是指定一个自定义的初始化方法，例如在 XML bean 定义中。有关所有 bean 生命周期方法的列表，请参阅 {@link BeanFactory} 的 javadoc。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DisposableBean
 * @see org.springframework.beans.factory.config.BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getInitMethodName()
 */
public interface InitializingBean {

    /**
     * 由包含的{@code BeanFactory}在设置所有bean属性并满足{@link BeanFactoryAware}、{@code ApplicationContextAware}等之后调用
     * <p>此方法允许bean实例在其所有属性设置完毕后执行其整体配置的验证和最终初始化。
     * @throws Exception 如果发生配置错误（例如未能设置一个基本属性）或由于其他任何原因初始化失败
     */
    void afterPropertiesSet() throws Exception;
}
