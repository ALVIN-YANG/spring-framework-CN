// 翻译完成 glm-4-flash
/*版权所有 2002-2021，原作者或作者。

根据Apache License，版本2.0（以下简称“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework.adapter;

import java.io.Serializable;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.ThrowsAdvice;

/**
 * 适配器，用于使 {@link org.springframework.aop.ThrowsAdvice} 能够在 Spring AOP 框架中使用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
class ThrowsAdviceAdapter implements AdvisorAdapter, Serializable {

    @Override
    public boolean supportsAdvice(Advice advice) {
        return (advice instanceof ThrowsAdvice);
    }

    @Override
    public MethodInterceptor getInterceptor(Advisor advisor) {
        return new ThrowsAdviceInterceptor(advisor.getAdvice());
    }
}
