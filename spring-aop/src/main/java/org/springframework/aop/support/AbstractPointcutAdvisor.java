// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）许可；
除非根据法律要求或书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按照“现状”分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import org.aopalliance.aop.Advice;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * 用于实现 {@link org.springframework.aop.PointcutAdvisor} 抽象基类。可以继承该类以返回特定的切入点/建议或自由配置的切入点/建议。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1.2
 * @see AbstractGenericPointcutAdvisor
 */
@SuppressWarnings("serial")
public abstract class AbstractPointcutAdvisor implements PointcutAdvisor, Ordered, Serializable {

    @Nullable
    private Integer order;

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        if (this.order != null) {
            return this.order;
        }
        Advice advice = getAdvice();
        if (advice instanceof Ordered ordered) {
            return ordered.getOrder();
        }
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof PointcutAdvisor otherAdvisor && ObjectUtils.nullSafeEquals(getAdvice(), otherAdvisor.getAdvice()) && ObjectUtils.nullSafeEquals(getPointcut(), otherAdvisor.getPointcut())));
    }

    @Override
    public int hashCode() {
        return PointcutAdvisor.class.hashCode();
    }
}
