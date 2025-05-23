// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 您除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照"现状"提供，
* 不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证以了解管理权限和限制的具体语言。*/
package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.util.StopWatch;

/**
 * 简单的AOP联盟{@code MethodInterceptor}用于性能监控。
 * 此拦截器对被拦截的方法调用没有影响。
 *
 * <p>使用{@code StopWatch}进行实际性能测量。
 *
 * @author Rod Johnson
 * @author Dmitriy Kopylenko
 * @author Rob Harrop
 * @see org.springframework.util.StopWatch
 */
@SuppressWarnings("serial")
public class PerformanceMonitorInterceptor extends AbstractMonitoringInterceptor {

    /**
     * 创建一个新的 PerformanceMonitorInterceptor，并带有静态日志记录器。
     */
    public PerformanceMonitorInterceptor() {
    }

    /**
     * 根据给定的标志，创建一个新的 PerformanceMonitorInterceptor，使用动态或静态日志记录器。
     * @param useDynamicLogger 是否使用动态日志记录器或静态日志记录器
     * @see #setUseDynamicLogger
     */
    public PerformanceMonitorInterceptor(boolean useDynamicLogger) {
        setUseDynamicLogger(useDynamicLogger);
    }

    @Override
    protected Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {
        String name = createInvocationTraceName(invocation);
        StopWatch stopWatch = new StopWatch(name);
        stopWatch.start(name);
        try {
            return invocation.proceed();
        } finally {
            stopWatch.stop();
            writeToLog(logger, stopWatch.shortSummary());
        }
    }
}
