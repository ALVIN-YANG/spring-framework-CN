// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.aop.support;

import java.io.Serializable;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.lang.Nullable;

/**
 * 方便的切点驱动型顾问实现。
 *
 * 这是最常用的顾问实现方式。它可以与任何切点和建议类型一起使用，除了引入（introductions）。通常不需要子类化这个类，或者实现自定义的顾问。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setPointcut
 * @see #setAdvice
 */
@SuppressWarnings("serial")
public class DefaultPointcutAdvisor extends AbstractGenericPointcutAdvisor implements Serializable {

    private Pointcut pointcut = Pointcut.TRUE;

    /**
     * 创建一个空的 DefaultPointcutAdvisor。
     * <p>在使用setter方法之前必须设置通知。
     * 通常也会设置切入点，但默认为 {@code Pointcut.TRUE}。
     */
    public DefaultPointcutAdvisor() {
    }

    /**
     * 创建一个匹配所有方法的 DefaultPointcutAdvisor。
     * <p>将使用 {@code Pointcut.TRUE} 作为切入点。
     * @param advice 要使用的通知（Advice）
     */
    public DefaultPointcutAdvisor(Advice advice) {
        this(Pointcut.TRUE, advice);
    }

    /**
     * 创建一个 DefaultPointcutAdvisor，指定 Pointcut 和 Advice。
     * @param pointcut 目标 Advice 的 Pointcut
     * @param advice 当 Pointcut 匹配时运行的 Advice
     */
    public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
        this.pointcut = pointcut;
        setAdvice(advice);
    }

    /**
     * 指定目标通知（Advice）的切入点。
     * <p>默认值为 {@code Pointcut.TRUE}。
     * @see #setAdvice
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
        return getClass().getName() + ": pointcut [" + getPointcut() + "]; advice [" + getAdvice() + "]";
    }
}
