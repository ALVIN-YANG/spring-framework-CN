// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterAdvice;
import org.springframework.aop.BeforeAdvice;
import org.springframework.lang.Nullable;

/**
 * 处理 AspectJ 顾问的实用方法。
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AspectJAopUtils {

    /**
     * 返回 {@code true} 如果顾问是前置通知（Before Advice）的一种形式。
     */
    public static boolean isBeforeAdvice(Advisor anAdvisor) {
        AspectJPrecedenceInformation precedenceInfo = getAspectJPrecedenceInformationFor(anAdvisor);
        if (precedenceInfo != null) {
            return precedenceInfo.isBeforeAdvice();
        }
        return (anAdvisor.getAdvice() instanceof BeforeAdvice);
    }

    /**
     * 如果顾问是后置通知的一种形式，则返回 {@code true}。
     */
    public static boolean isAfterAdvice(Advisor anAdvisor) {
        AspectJPrecedenceInformation precedenceInfo = getAspectJPrecedenceInformationFor(anAdvisor);
        if (precedenceInfo != null) {
            return precedenceInfo.isAfterAdvice();
        }
        return (anAdvisor.getAdvice() instanceof AfterAdvice);
    }

    /**
     * 返回由该顾问或其建议提供的 AspectJPrecedenceInformation。
     * 如果顾问或建议都没有优先级信息，此方法将返回 {@code null}。
     */
    @Nullable
    public static AspectJPrecedenceInformation getAspectJPrecedenceInformationFor(Advisor anAdvisor) {
        if (anAdvisor instanceof AspectJPrecedenceInformation ajpi) {
            return ajpi;
        }
        Advice advice = anAdvisor.getAdvice();
        if (advice instanceof AspectJPrecedenceInformation ajpi) {
            return ajpi;
        }
        return null;
    }
}
