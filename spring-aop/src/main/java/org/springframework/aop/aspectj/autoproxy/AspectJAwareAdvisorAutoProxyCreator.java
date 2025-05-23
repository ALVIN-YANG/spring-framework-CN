// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache License 2.0（以下简称“许可证”）许可；除非遵守许可证，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj.autoproxy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.aopalliance.aop.Advice;
import org.aspectj.util.PartialOrder;
import org.aspectj.util.PartialOrder.PartialComparable;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJPointcutAdvisor;
import org.springframework.aop.aspectj.AspectJProxyUtils;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator}
 * AspectJ的调用上下文暴露子类，理解当来自同一方面的多个通知（advice）存在时，AspectJ的优先级规则。
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJAwareAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {

    private static final Comparator<Advisor> DEFAULT_PRECEDENCE_COMPARATOR = new AspectJPrecedenceComparator();

    /**
     * 根据AspectJ优先级对提供的{@link Advisor}实例进行排序。
     * <p>如果两条通知来自同一个切面，它们将具有相同的顺序。来自同一切面的通知将进一步按照以下规则排序：
     * <ul>
     * <li>如果这对中的任何一个都是<em>after</em>通知，则最后声明的通知获得最高的优先级（即最后执行）。</li>
     * <li>否则，最早声明的通知获得最高的优先级（即首先执行）。</li>
     * </ul>
     * <p><b>重要：</b>通知按优先级排序，从最高优先级到最低优先级。在进入连接点时，最高优先级的通知应该首先运行。在离开连接点时，最高优先级的通知应该最后运行。
     */
    @Override
    protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
        List<PartiallyComparableAdvisorHolder> partiallyComparableAdvisors = new ArrayList<>(advisors.size());
        for (Advisor advisor : advisors) {
            partiallyComparableAdvisors.add(new PartiallyComparableAdvisorHolder(advisor, DEFAULT_PRECEDENCE_COMPARATOR));
        }
        List<PartiallyComparableAdvisorHolder> sorted = PartialOrder.sort(partiallyComparableAdvisors);
        if (sorted != null) {
            List<Advisor> result = new ArrayList<>(advisors.size());
            for (PartiallyComparableAdvisorHolder pcAdvisor : sorted) {
                result.add(pcAdvisor.getAdvisor());
            }
            return result;
        } else {
            return super.sortAdvisors(advisors);
        }
    }

    /**
     * 将一个 {@link ExposeInvocationInterceptor} 添加到建议链的开头。
     * <p>当使用 AspectJ 点切表达式和 AspectJ 风格的建议时，需要这个额外的建议。
     */
    @Override
    protected void extendAdvisors(List<Advisor> candidateAdvisors) {
        AspectJProxyUtils.makeAdvisorChainAspectJCapableIfNecessary(candidateAdvisors);
    }

    @Override
    protected boolean shouldSkip(Class<?> beanClass, String beanName) {
        // 待办：考虑通过缓存方面名称列表进行优化
        List<Advisor> candidateAdvisors = findCandidateAdvisors();
        for (Advisor advisor : candidateAdvisors) {
            if (advisor instanceof AspectJPointcutAdvisor pointcutAdvisor && pointcutAdvisor.getAspectName().equals(beanName)) {
                return true;
            }
        }
        return super.shouldSkip(beanClass, beanName);
    }

    /**
     * 实现了 AspectJ 的 {@link PartialComparable} 接口，用于定义偏序关系。
     */
    private static class PartiallyComparableAdvisorHolder implements PartialComparable {

        private final Advisor advisor;

        private final Comparator<Advisor> comparator;

        public PartiallyComparableAdvisorHolder(Advisor advisor, Comparator<Advisor> comparator) {
            this.advisor = advisor;
            this.comparator = comparator;
        }

        @Override
        public int compareTo(Object obj) {
            Advisor otherAdvisor = ((PartiallyComparableAdvisorHolder) obj).advisor;
            return this.comparator.compare(this.advisor, otherAdvisor);
        }

        @Override
        public int fallbackCompareTo(Object obj) {
            return 0;
        }

        public Advisor getAdvisor() {
            return this.advisor;
        }

        @Override
        public String toString() {
            Advice advice = this.advisor.getAdvice();
            StringBuilder sb = new StringBuilder(ClassUtils.getShortName(advice.getClass()));
            boolean appended = false;
            if (this.advisor instanceof Ordered ordered) {
                sb.append(": order = ").append(ordered.getOrder());
                appended = true;
            }
            if (advice instanceof AbstractAspectJAdvice ajAdvice) {
                sb.append(!appended ? ": " : ", ");
                sb.append("aspect name = ");
                sb.append(ajAdvice.getAspectName());
                sb.append(", declaration order = ");
                sb.append(ajAdvice.getDeclarationOrder());
            }
            return sb.toString();
        }
    }
}
