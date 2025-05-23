// 翻译完成 glm-4-flash
/*版权所有 2002-2017 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）；除非符合许可证规定，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律强制要求或书面同意，否则在许可证下分发的软件按照“原样”分发，不提供任何形式的明示或暗示保证。
有关权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.lang.Nullable;

/**
 *  基于 BeanFactory 的具体 PointcutAdvisor，允许将任何 Advice 配置为 BeanFactory 中 Advice bean 的引用，同时可以通过 bean 属性配置 Pointcut。
 *
 * <p>在 BeanFactory 中指定 advice bean 的名称而不是 advice 对象本身（如果处于 BeanFactory 运行环境中），可以在初始化时提高松耦合度，这样就不需要在 pointcut 实际匹配之前初始化 advice 对象。
 *
 * @作者 Juergen Hoeller
 * @自 2.0.2 以来
 * @see #setPointcut
 * @see #setAdviceBeanName
 */
@SuppressWarnings("serial")
public class DefaultBeanFactoryPointcutAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private Pointcut pointcut = Pointcut.TRUE;

    /**
     * 指定针对通知的切入点。
     * <p>默认值为 {@code Pointcut.TRUE}。
     * @see #setAdviceBeanName
     */
    public void setPointcut(@Nullable Pointcut pointcut) {
        this.pointcut = (pointcut != null ? pointcut : Pointcut.TRUE);
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": pointcut [" + getPointcut() + "]; advice bean '" + getAdviceBeanName() + "'";
    }
}
