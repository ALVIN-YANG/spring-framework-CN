// 翻译完成 glm-4-flash
/*版权所有 2002-2020 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.Nullable;

/**
 * AOP Alliance 的 {@code MethodInterceptor}，可以被引入到链中，向日志记录器显示拦截调用时的详细信息。
 *
 * <p>在方法进入和退出时记录完整的调用细节，包括调用参数和调用次数。这仅适用于调试目的；使用 {@code SimpleTraceInterceptor} 或 {@code CustomizableTraceInterceptor} 进行纯跟踪。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SimpleTraceInterceptor
 * @see CustomizableTraceInterceptor
 */
@SuppressWarnings("serial")
public class DebugInterceptor extends SimpleTraceInterceptor {

    private volatile long count;

    /**
     * 创建一个新的 DebugInterceptor，并使用静态日志记录器。
     */
    public DebugInterceptor() {
    }

    /**
     * 根据给定的标志创建一个新的 DebugInterceptor，使用动态或静态日志记录器。
     * @param useDynamicLogger 是否使用动态日志记录器或静态日志记录器
     * @see #setUseDynamicLogger
     */
    public DebugInterceptor(boolean useDynamicLogger) {
        setUseDynamicLogger(useDynamicLogger);
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        synchronized (this) {
            this.count++;
        }
        return super.invoke(invocation);
    }

    @Override
    protected String getInvocationDescription(MethodInvocation invocation) {
        return invocation + "; count=" + this.count;
    }

    /**
     * 返回此拦截器被调用的次数。
     */
    public long getCount() {
        return this.count;
    }

    /**
     * 将调用次数重置为零。
     */
    public synchronized void resetCount() {
        this.count = 0;
    }
}
