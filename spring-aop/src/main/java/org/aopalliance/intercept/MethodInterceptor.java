// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache许可证版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
 
https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.aopalliance.intercept;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 拦截接口上的调用，在目标方法调用过程中进行。这些拦截器是嵌套在目标方法之上的。
 *
 * <p>用户应实现 {@link #invoke(MethodInvocation)} 方法来修改原始行为。例如，以下类实现了一个跟踪拦截器（跟踪所有被拦截的方法的调用）：
 *
 * <pre class=code>
 * class TracingInterceptor implements MethodInterceptor {
 *   Object invoke(MethodInvocation i) throws Throwable {
 *     System.out.println("方法 "+i.getMethod()+" 在 "+i.getThis()+" 上被调用，参数为 "+i.getArguments());
 *     Object ret=i.proceed();
 *     System.out.println("方法 "+i.getMethod()+" 返回 "+ret);
 *     return ret;
 *   }
 * }
 * </pre>
 *
 * @author Rod Johnson
 */
@FunctionalInterface
public interface MethodInterceptor extends Interceptor {

    /**
     * 实现此方法以在调用之前和之后执行额外处理。礼貌的实现当然会调用{@link Joinpoint#proceed()}。
     * @param invocation 方法调用的连接点
     * @return 对{@link Joinpoint#proceed()}的调用结果；可能会被拦截器截获
     * @throws Throwable 如果拦截器或目标对象抛出异常
     */
    @Nullable
    Object invoke(@Nonnull MethodInvocation invocation) throws Throwable;
}
