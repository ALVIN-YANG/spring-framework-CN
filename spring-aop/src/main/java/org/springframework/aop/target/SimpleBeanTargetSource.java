// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者。

根据Apache许可证版本2.0（以下简称“许可证”）；除非符合许可证，否则不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.target;

/**
 * 简单的 {@link org.springframework.aop.TargetSource} 实现，
 * 从其包含的 Spring {@link org.springframework.beans.factory.BeanFactory} 中动态获取指定的目标 Bean。
 *
 * <p>可以获取任何类型的目标 Bean：单例、作用域或原型。通常用于作用域 Bean。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 */
@SuppressWarnings("serial")
public class SimpleBeanTargetSource extends AbstractBeanFactoryBasedTargetSource {

    @Override
    public Object getTarget() throws Exception {
        return getBeanFactory().getBean(getTargetBeanName());
    }
}
