// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可；除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的明示或暗示保证。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * AspectJ 的 {@link PointcutAdvisor}，它将一个 {@link AbstractAspectJAdvice} 适配到 {@link org.springframework.aop.PointcutAdvisor} 接口。
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AspectJPointcutAdvisor implements PointcutAdvisor, Ordered {

    private final AbstractAspectJAdvice advice;

    private final Pointcut pointcut;

    @Nullable
    private Integer order;

    /**
     * 为给定的通知创建一个新的 AspectJPointcutAdvisor。
     * @param advice 需要包装的 AbstractAspectJAdvice 对象
     */
    public AspectJPointcutAdvisor(AbstractAspectJAdvice advice) {
        Assert.notNull(advice, "Advice must not be null");
        this.advice = advice;
        this.pointcut = advice.buildSafePointcut();
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        if (this.order != null) {
            return this.order;
        } else {
            return this.advice.getOrder();
        }
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    /**
     * 返回声明建议（通知）的方面（Bean）的名称。
     * @since 4.3.15
     * @see AbstractAspectJAdvice#getAspectName()
     */
    public String getAspectName() {
        return this.advice.getAspectName();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof AspectJPointcutAdvisor that && this.advice.equals(that.advice)));
    }

    @Override
    public int hashCode() {
        return AspectJPointcutAdvisor.class.hashCode() * 29 + this.advice.hashCode();
    }
}
