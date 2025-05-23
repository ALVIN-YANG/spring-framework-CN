// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非您遵守许可证，否则不得使用此文件。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。有关权限和限制的特定语言，
* 请参阅许可证。*/
package org.springframework.aop.aspectj;

import java.io.Serializable;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.lang.Nullable;

/**
 * 这是一个Spring AOP的环绕通知（MethodInterceptor），它包装了一个AspectJ通知方法。暴露了ProceedingJoinPoint。
 *
 *  @author Rod Johnson
 *  @author Juergen Hoeller
 *  @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJAroundAdvice extends AbstractAspectJAdvice implements MethodInterceptor, Serializable {

    public AspectJAroundAdvice(Method aspectJAroundAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {
        super(aspectJAroundAdviceMethod, pointcut, aif);
    }

    @Override
    public boolean isBeforeAdvice() {
        return false;
    }

    @Override
    public boolean isAfterAdvice() {
        return false;
    }

    @Override
    protected boolean supportsProceedingJoinPoint() {
        return true;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation mi) throws Throwable {
        if (!(mi instanceof ProxyMethodInvocation pmi)) {
            throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
        }
        ProceedingJoinPoint pjp = lazyGetProceedingJoinPoint(pmi);
        JoinPointMatch jpm = getJoinPointMatch(pmi);
        return invokeAdviceMethod(pjp, jpm, null, null);
    }

    /**
     * 返回当前调用的 ProceedingJoinPoint，
     * 如果它尚未绑定到线程，则将其懒实例化。
     * @param rmi 当前 Spring AOP ReflectiveMethodInvocation，
     * 我们将使用它进行属性绑定
     * @return 可用于通知方法的 ProceedingJoinPoint
     */
    protected ProceedingJoinPoint lazyGetProceedingJoinPoint(ProxyMethodInvocation rmi) {
        return new MethodInvocationProceedingJoinPoint(rmi);
    }
}
