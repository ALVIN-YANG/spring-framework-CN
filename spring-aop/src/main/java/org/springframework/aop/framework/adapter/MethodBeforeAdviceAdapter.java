// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者们。

根据Apache许可证版本2.0（以下简称“许可证”）授权；
除非符合许可证规定或书面同意，否则不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
不提供任何形式的明示或暗示保证，无论是关于其适用性还是关于其特定用途的适用性。
有关许可证对权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework.adapter;

import java.io.Serializable;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * 适配器，用于使 {@link org.springframework.aop.MethodBeforeAdvice} 能够在 Spring AOP 框架中使用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
class MethodBeforeAdviceAdapter implements AdvisorAdapter, Serializable {

    @Override
    public boolean supportsAdvice(Advice advice) {
        return (advice instanceof MethodBeforeAdvice);
    }

    @Override
    public MethodInterceptor getInterceptor(Advisor advisor) {
        MethodBeforeAdvice advice = (MethodBeforeAdvice) advisor.getAdvice();
        return new MethodBeforeAdviceInterceptor(advice);
    }
}
