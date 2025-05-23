// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）许可；
除非根据适用法律要求或书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式（明示或暗示）的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * 适用于同时作为静态切点的顾问的便捷基类。
 * 如果建议和子类是可序列化的，则也是可序列化的。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public abstract class StaticMethodMatcherPointcutAdvisor extends StaticMethodMatcherPointcut implements PointcutAdvisor, Ordered, Serializable {

    private Advice advice = EMPTY_ADVICE;

    private int order = Ordered.LOWEST_PRECEDENCE;

    /**
     * 创建一个新的 StaticMethodMatcherPointcutAdvisor，
     * 预期使用bean风格的配置。
     * @see #setAdvice
     */
    public StaticMethodMatcherPointcutAdvisor() {
    }

    /**
     * 为给定的建议创建一个新的 StaticMethodMatcherPointcutAdvisor。
     * @param advice 要使用的建议
     */
    public StaticMethodMatcherPointcutAdvisor(Advice advice) {
        Assert.notNull(advice, "Advice must not be null");
        this.advice = advice;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public Pointcut getPointcut() {
        return this;
    }
}
