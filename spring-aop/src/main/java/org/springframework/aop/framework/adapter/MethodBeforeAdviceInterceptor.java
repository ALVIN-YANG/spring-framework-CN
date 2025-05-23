// 翻译完成 glm-4-flash
/** 版权所有 2002-2020，原作者或原作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、
* 或特定用途的适用性。请参阅许可证以了解具体管理权限和限制的条款。*/
package org.springframework.aop.framework.adapter;

import java.io.Serializable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于包装一个 {@link MethodBeforeAdvice} 的拦截器。
 * <p>该类由 AOP 框架内部使用；应用程序开发者通常不需要直接使用这个类。
 *
 * @author Rod Johnson
 * @see AfterReturningAdviceInterceptor
 * @see ThrowsAdviceInterceptor
 */
@SuppressWarnings("serial")
public class MethodBeforeAdviceInterceptor implements MethodInterceptor, BeforeAdvice, Serializable {

    private final MethodBeforeAdvice advice;

    /**
     * 为给定的建议创建一个新的 MethodBeforeAdviceInterceptor。
     * @param advice 要包装的 MethodBeforeAdvice
     */
    public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
        Assert.notNull(advice, "Advice must not be null");
        this.advice = advice;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation mi) throws Throwable {
        this.advice.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
