// 翻译完成 glm-4-flash
/** 版权所有 2002-2020，原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，
* 无论是明示的还是暗示的。有关许可权限和限制的特定语言，
* 请参阅许可证。*/
package org.springframework.aop.aspectj.autoproxy;

import java.util.Comparator;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJAopUtils;
import org.springframework.aop.aspectj.AspectJPrecedenceInformation;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;

/**
 *  通过优先级（<i>而非</i>调用顺序）对 AspectJ 的建议/建议者进行排序。
 *
 * <p>给定两个建议，分别为 {@code A} 和 {@code B}：
 * <ul>
 * <li>如果 {@code A} 和 {@code B} 定义在不同的方面，则优先级最低的方面的建议具有最高优先级。</li>
 * <li>如果 {@code A} 和 {@code B} 定义在同一个方面，如果其中一个是 <em>after</em> 建议的形式，则方面中最后声明的建议具有最高优先级。如果 {@code A} 和 {@code B} 都不是 <em>after</em> 建议的形式，则方面中首先声明的建议具有最高优先级。</li>
 * </ul>
 *
 * <p>重要：此比较器与 AspectJ 的 {@link org.aspectj.util.PartialOrder PartialOrder} 排序实用工具一起使用。因此，与正常的 {@link Comparator} 不同，此比较器返回值为 {@code 0} 表示我们不关心排序，而不是两个元素必须以相同的方式排序。
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
class AspectJPrecedenceComparator implements Comparator<Advisor> {

    private static final int HIGHER_PRECEDENCE = -1;

    private static final int SAME_PRECEDENCE = 0;

    private static final int LOWER_PRECEDENCE = 1;

    private final Comparator<? super Advisor> advisorComparator;

    /**
     * 创建一个默认的 {@code AspectJPrecedenceComparator}。
     */
    public AspectJPrecedenceComparator() {
        this.advisorComparator = AnnotationAwareOrderComparator.INSTANCE;
    }

    /**
     * 创建一个 {@code AspectJPrecedenceComparator}，使用给定的 {@link Comparator} 来比较 {@link org.springframework.aop.Advisor} 实例。
     * @param advisorComparator 用于顾问的 {@code Comparator}
     */
    public AspectJPrecedenceComparator(Comparator<? super Advisor> advisorComparator) {
        Assert.notNull(advisorComparator, "Advisor comparator must not be null");
        this.advisorComparator = advisorComparator;
    }

    @Override
    public int compare(Advisor o1, Advisor o2) {
        int advisorPrecedence = this.advisorComparator.compare(o1, o2);
        if (advisorPrecedence == SAME_PRECEDENCE && declaredInSameAspect(o1, o2)) {
            advisorPrecedence = comparePrecedenceWithinAspect(o1, o2);
        }
        return advisorPrecedence;
    }

    private int comparePrecedenceWithinAspect(Advisor advisor1, Advisor advisor2) {
        boolean oneOrOtherIsAfterAdvice = (AspectJAopUtils.isAfterAdvice(advisor1) || AspectJAopUtils.isAfterAdvice(advisor2));
        int adviceDeclarationOrderDelta = getAspectDeclarationOrder(advisor1) - getAspectDeclarationOrder(advisor2);
        if (oneOrOtherIsAfterAdvice) {
            // 最后声明的建议具有更高的优先级
            if (adviceDeclarationOrderDelta < 0) {
                // advice1 在 advice2 前被声明
                // 因此，advice1 具有较低的优先级
                return LOWER_PRECEDENCE;
            } else if (adviceDeclarationOrderDelta == 0) {
                return SAME_PRECEDENCE;
            } else {
                return HIGHER_PRECEDENCE;
            }
        } else {
            // 首先声明的建议具有更高的优先级
            if (adviceDeclarationOrderDelta < 0) {
                // advice1 在 advice2 声明之前
                // 因此，advice1 具有更高的优先级
                return HIGHER_PRECEDENCE;
            } else if (adviceDeclarationOrderDelta == 0) {
                return SAME_PRECEDENCE;
            } else {
                return LOWER_PRECEDENCE;
            }
        }
    }

    private boolean declaredInSameAspect(Advisor advisor1, Advisor advisor2) {
        return (hasAspectName(advisor1) && hasAspectName(advisor2) && getAspectName(advisor1).equals(getAspectName(advisor2)));
    }

    private boolean hasAspectName(Advisor advisor) {
        return (advisor instanceof AspectJPrecedenceInformation || advisor.getAdvice() instanceof AspectJPrecedenceInformation);
    }

    // 前提条件是hasAspectName返回了true
    private String getAspectName(Advisor advisor) {
        AspectJPrecedenceInformation precedenceInfo = AspectJAopUtils.getAspectJPrecedenceInformationFor(advisor);
        Assert.state(precedenceInfo != null, () -> "Unresolvable AspectJPrecedenceInformation for " + advisor);
        return precedenceInfo.getAspectName();
    }

    private int getAspectDeclarationOrder(Advisor advisor) {
        AspectJPrecedenceInformation precedenceInfo = AspectJAopUtils.getAspectJPrecedenceInformationFor(advisor);
        return (precedenceInfo != null ? precedenceInfo.getDeclarationOrder() : 0);
    }
}
