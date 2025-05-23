// 翻译完成 glm-4-flash
/*版权所有 2002-2022，原作者或作者。

根据Apache License，版本2.0（以下简称“许可证”）；除非遵守许可证，否则不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj;

import java.util.List;
import org.springframework.aop.Advisor;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 用于处理 AspectJ 代理的实用方法。
 *
 * @author Rod Johnson
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AspectJProxyUtils {

    /**
     * 如有必要，添加特殊顾问以与包含 AspectJ 顾问的代理链一起工作：
     * 具体来说，在列表开头添加 {@link ExposeInvocationInterceptor}。
     * <p>这将暴露当前的 Spring AOP 调用（对于某些 AspectJ 切入点匹配是必要的）并使当前 AspectJ JoinPoint 可用。如果没有 AspectJ 顾问在顾问链中，则调用将不会有任何效果。
     * @param advisors 可用的顾问列表
     * @return 如果向列表中添加了 {@link ExposeInvocationInterceptor}，则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean makeAdvisorChainAspectJCapableIfNecessary(List<Advisor> advisors) {
        // 不要向一个空列表添加顾问；这可能表明代理根本不是必需的
        if (!advisors.isEmpty()) {
            boolean foundAspectJAdvice = false;
            for (Advisor advisor : advisors) {
                // 请小心不要在没有防护措施的情况下获取建议，因为这可能会过于急切地
                // 实例化一个非单例的AspectJ切面...
                if (isAspectJAdvice(advisor)) {
                    foundAspectJAdvice = true;
                    break;
                }
            }
            if (foundAspectJAdvice && !advisors.contains(ExposeInvocationInterceptor.ADVISOR)) {
                advisors.add(0, ExposeInvocationInterceptor.ADVISOR);
                return true;
            }
        }
        return false;
    }

    /**
     * 确定给定的Advisor是否包含AspectJ通知。
     * @param advisor 要检查的Advisor
     */
    private static boolean isAspectJAdvice(Advisor advisor) {
        return (advisor instanceof InstantiationModelAwarePointcutAdvisor || advisor.getAdvice() instanceof AbstractAspectJAdvice || (advisor instanceof PointcutAdvisor pointcutAdvisor && pointcutAdvisor.getPointcut() instanceof AspectJExpressionPointcut));
    }

    static boolean isVariableName(@Nullable String name) {
        if (!StringUtils.hasLength(name)) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
