// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 您必须遵守许可证才能使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权或特定用途的适用性。
* 请参阅许可证以了解具体管理权限和限制的详细语言。*/
package org.springframework.aop.support;

import org.aopalliance.aop.Advice;

/**
 * 抽象通用 {@link org.springframework.aop.PointcutAdvisor}
 * 允许配置任何 {@link Advice}。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setAdvice
 * @see DefaultPointcutAdvisor
 */
@SuppressWarnings("serial")
public abstract class AbstractGenericPointcutAdvisor extends AbstractPointcutAdvisor {

    private Advice advice = EMPTY_ADVICE;

    /**
     * 指定此顾问应应用的建议。
     */
    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": advice [" + getAdvice() + "]";
    }
}
