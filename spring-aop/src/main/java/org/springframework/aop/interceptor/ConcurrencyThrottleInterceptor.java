// 翻译完成 glm-4-flash
/*版权所有 2002-2020，原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）许可；
除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性或特定用途的适用性的保证。
有关许可证的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.aop.interceptor;

import java.io.Serializable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrencyThrottleSupport;

/**
 * 用于限制并发访问的拦截器，当达到指定的并发限制时，会阻塞调用。
 *
 * <p>可以应用于涉及大量系统资源使用的本地服务的方法，在这种情况下，限制特定服务的并发性比限制整个线程池（例如，Web 容器的线程池）更有效。
 *
 * <p>此拦截器的默认并发限制为1。通过指定“concurrencyLimit”bean属性来更改此值。
 *
 * @author Juergen Hoeller
 * @since 11.02.2004
 * @see #setConcurrencyLimit
 */
@SuppressWarnings("serial")
public class ConcurrencyThrottleInterceptor extends ConcurrencyThrottleSupport implements MethodInterceptor, Serializable {

    public ConcurrencyThrottleInterceptor() {
        setConcurrencyLimit(1);
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        beforeAccess();
        try {
            return methodInvocation.proceed();
        } finally {
            afterAccess();
        }
    }
}
