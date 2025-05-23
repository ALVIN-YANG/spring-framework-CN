// 翻译完成 glm-4-flash
/** 版权所有 2002-2020，原作者或原作者们。
*
* 根据Apache License，版本2.0（以下简称“许可证”）；除非您遵守许可证，否则不得使用此文件。
* 您可以在以下网址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何形式的明示或暗示保证。
* 请参阅许可证以了解特定语言的权限和限制。*/
package org.springframework.aop.framework.adapter;

import java.io.Serializable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.AfterAdvice;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于包装 {@link org.springframework.aop.AfterReturningAdvice} 的拦截器。
 * 该类由 AOP 框架内部使用；应用开发者通常不需要直接使用这个类。
 *
 * @author Rod Johnson
 * @see MethodBeforeAdviceInterceptor
 * @see ThrowsAdviceInterceptor
 */
@SuppressWarnings("serial")
public class AfterReturningAdviceInterceptor implements MethodInterceptor, AfterAdvice, Serializable {

    private final AfterReturningAdvice advice;

    /**
     * 为给定建议创建一个新的 AfterReturningAdviceInterceptor。
     * @param advice 要包装的 AfterReturningAdvice 对象
     */
    public AfterReturningAdviceInterceptor(AfterReturningAdvice advice) {
        Assert.notNull(advice, "Advice must not be null");
        this.advice = advice;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.advice.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }
}
