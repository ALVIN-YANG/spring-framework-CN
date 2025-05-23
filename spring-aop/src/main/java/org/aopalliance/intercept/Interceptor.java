// 翻译完成 glm-4-flash
/*版权所有 2002-2019 原作者或原作者。

根据Apache License, Version 2.0（"许可证"）许可；
除非适用法律要求或经书面同意，否则不得使用此文件，
除非遵守许可证。
您可以在以下网址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关许可的具体语言和限制，请参阅许可证。*/
package org.aopalliance.intercept;

import org.aopalliance.aop.Advice;

/**
 * 此接口表示一个通用拦截器。
 *
 * <p>一个通用拦截器可以拦截在基本程序中发生的运行时事件。这些事件由（在）连接点实现。运行时连接点可以是方法调用、字段访问、异常等...
 *
 * <p>此接口不直接使用。使用子接口来拦截特定事件。例如，以下类实现了某些特定的拦截器以实现一个调试器：
 *
 * <pre class=code>
 * class DebuggingInterceptor implements MethodInterceptor,
 *     ConstructorInterceptor {
 *
 *   Object invoke(MethodInvocation i) throws Throwable {
 *     debug(i.getMethod(), i.getThis(), i.getArgs());
 *     return i.proceed();
 *   }
 *
 *   Object construct(ConstructorInvocation i) throws Throwable {
 *     debug(i.getConstructor(), i.getThis(), i.getArgs());
 *     return i.proceed();
 *   }
 *
 *   void debug(AccessibleObject ao, Object this, Object value) {
 *     ...
 *   }
 * }
 * </pre>
 *
 * @author Rod Johnson
 * @see Joinpoint
 */
public interface Interceptor extends Advice {
}
