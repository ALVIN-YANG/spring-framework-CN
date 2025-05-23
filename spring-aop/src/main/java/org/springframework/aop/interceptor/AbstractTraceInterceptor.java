// 翻译完成 glm-4-flash
/*版权所有 2002-2020 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）；除非遵守许可证，否则不得使用此文件。
您可以在以下链接获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按照“现状”分发，不提供任何明示或暗示的保证或条件。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.interceptor;

import java.io.Serializable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 这是用于跟踪的基本 {@code MethodInterceptor} 实现。
 *
 * 默认情况下，日志消息会被写入拦截器类的日志，而不是被拦截的类。将 {@code useDynamicLogger} 实体属性设置为 {@code true} 将导致所有日志消息都被写入被拦截的目标类的 {@code Log}。
 *
 * 子类必须实现 {@code invokeUnderTrace} 方法，该方法仅在特定调用应该被跟踪时由这个类调用。子类应该写入提供的 {@code Log} 实例。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setUseDynamicLogger
 * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)
 */
@SuppressWarnings("serial")
public abstract class AbstractTraceInterceptor implements MethodInterceptor, Serializable {

    /**
     * 默认的 {@code Log} 实例，用于写入跟踪消息。
     * 此实例映射到实现类 {@code Class}。
     */
    @Nullable
    protected transient Log defaultLogger = LogFactory.getLog(getClass());

    /**
     * 表示在使用动态日志记录器时，是否应该隐藏代理类名。
     * @see #setUseDynamicLogger
     */
    private boolean hideProxyClassNames = false;

    /**
     * 指示是否将异常传递给日志记录器。
     * @see #writeToLog(Log, String, Throwable)
     */
    private boolean logExceptionStackTrace = true;

    /**
     * 设置是否使用动态日志记录器或静态日志记录器。
     * 默认情况下，此跟踪拦截器使用静态日志记录器。
     * <p>用于确定应使用哪个 {@code Log} 实例来为特定的方法调用写入日志消息：对于被调用的 {@code Class} 使用动态的，对于跟踪拦截器的 {@code Class} 使用静态的。
     * <p><b>注意：</b>指定此属性或 "loggerName"，但不要同时指定两者。
     * @see #getLoggerForInvocation(org.aopalliance.intercept.MethodInvocation)
     */
    public void setUseDynamicLogger(boolean useDynamicLogger) {
        // 如果默认的日志记录器未被使用，则释放它。
        this.defaultLogger = (useDynamicLogger ? null : LogFactory.getLog(getClass()));
    }

    /**
     * 设置要使用的日志记录器的名称。该名称将通过 Commons Logging 传递给底层的日志实现，并根据日志记录器的配置将其解释为日志类别。
     * <p>这可以指定不将日志记录到类的类别（无论是此拦截器的类还是被调用的类）中，而是记录到特定的命名类别中。
     * <p><b>注意：</b>指定此属性或 "useDynamicLogger"，但不能同时指定两者。
     * @see org.apache.commons.logging.LogFactory#getLog(String)
     * @see java.util.logging.Logger#getLogger(String)
     */
    public void setLoggerName(String loggerName) {
        this.defaultLogger = LogFactory.getLog(loggerName);
    }

    /**
     * 将其设置为 "true" 以使动态日志记录器在可能的情况下隐藏代理类名。默认值为 "false"。
     */
    public void setHideProxyClassNames(boolean hideProxyClassNames) {
        this.hideProxyClassNames = hideProxyClassNames;
    }

    /**
     * 设置是否将异常传递给日志记录器，建议将其堆栈跟踪包含在日志中。默认值为 "true"；将此设置为 "false" 以减少日志输出，仅包含跟踪消息（可能包括异常类名和异常消息，如果适用）。
     * @since 4.3.10
     */
    public void setLogExceptionStackTrace(boolean logExceptionStackTrace) {
        this.logExceptionStackTrace = logExceptionStackTrace;
    }

    /**
     * 判断是否为特定的 {@code MethodInvocation} 启用了日志记录。
     * 如果没有启用，方法调用将按正常流程进行，否则方法调用将被传递给 {@code invokeUnderTrace} 方法进行处理。
     * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)
     */
    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Log logger = getLoggerForInvocation(invocation);
        if (isInterceptorEnabled(invocation, logger)) {
            return invokeUnderTrace(invocation, logger);
        } else {
            return invocation.proceed();
        }
    }

    /**
     * 返回用于给定 {@code MethodInvocation} 的适当 {@code Log} 实例。如果设置了 {@code useDynamicLogger} 标志，
     * 则 {@code Log} 实例将针对 {@code MethodInvocation} 的目标类，否则将使用默认的静态日志记录器。
     * @param invocation 正在被追踪的 {@code MethodInvocation}
     * @return 要使用的 {@code Log} 实例
     * @see #setUseDynamicLogger
     */
    protected Log getLoggerForInvocation(MethodInvocation invocation) {
        if (this.defaultLogger != null) {
            return this.defaultLogger;
        } else {
            Object target = invocation.getThis();
            Assert.state(target != null, "Target must not be null");
            return LogFactory.getLog(getClassForLogging(target));
        }
    }

    /**
     * 确定用于日志记录目的的类。
     * @param target 要进行反射的目标对象
     * @return 给定对象的相应目标类
     * @see #setHideProxyClassNames
     */
    protected Class<?> getClassForLogging(Object target) {
        return (this.hideProxyClassNames ? AopUtils.getTargetClass(target) : target.getClass());
    }

    /**
     * 判断拦截器是否应该启动，即是否应该调用 {@code invokeUnderTrace} 方法。
     * <p>默认行为是检查给定的 {@code Log} 实例是否启用。子类可以覆盖此方法以在其他情况下也应用拦截器。
     * @param invocation 正在被追踪的 {@code MethodInvocation}
     * @param logger 要检查的 {@code Log} 实例
     * @see #invokeUnderTrace
     * @see #isLogEnabled
     */
    protected boolean isInterceptorEnabled(MethodInvocation invocation, Log logger) {
        return isLogEnabled(logger);
    }

    /**
     * 判断给定的 {@link Log} 实例是否启用。
     * <p>当 "trace" 级别启用时，默认为 {@code true}。
     * 子类可以重写此方法以更改触发 '跟踪' 的级别。
     * @param logger 要检查的 {@code Log} 实例
     */
    protected boolean isLogEnabled(Log logger) {
        return logger.isTraceEnabled();
    }

    /**
     * 将提供的跟踪消息写入提供的 {@code Log} 实例。
     * <p>由 {@link #invokeUnderTrace} 调用，用于进入/退出消息。
     * <p>委托给 {@link #writeToLog(Log, String, Throwable)} 作为最终委托，该委托控制底层的日志调用。
     * @since 4.3.10
     * @see #writeToLog(Log, String, Throwable)
     */
    protected void writeToLog(Log logger, String message) {
        writeToLog(logger, message, null);
    }

    /**
     * 将提供的跟踪消息和 {@link Throwable} 写入提供的 {@code Log} 实例。
     * <p>该方法由 {@link #invokeUnderTrace} 在进入/退出结果时调用，可能包括一个异常。注意，当 {@link #setLogExceptionStackTrace} 设置为 "false" 时，异常的堆栈跟踪不会记录。
     * <p>默认情况下，消息以 {@code TRACE} 级别写入。子类可以覆盖此方法来控制消息的写入级别，通常也会相应地覆盖 {@link #isLogEnabled} 方法。
     * @since 4.3.10
     * @see #setLogExceptionStackTrace
     * @see #isLogEnabled
     */
    protected void writeToLog(Log logger, String message, @Nullable Throwable ex) {
        if (ex != null && this.logExceptionStackTrace) {
            logger.trace(message, ex);
        } else {
            logger.trace(message);
        }
    }

    /**
     * 子类必须重写此方法以在提供的 {@code MethodInvocation} 周围执行任何跟踪。子类负责确保通过调用 {@code MethodInvocation.proceed()} 实际执行 {@code MethodInvocation}。
     * <p>默认情况下，传入的 {@code Log} 实例将启用 "trace" 级别的日志。子类无需再次检查此设置，除非它们重写 {@code isInterceptorEnabled} 方法以修改默认行为，并且可以将实际写入日志的消息委托给 {@code writeToLog}。
     * @param logger 要写入跟踪消息的 {@code Log}
     * @return 调用 {@code MethodInvocation.proceed()} 的结果
     * @throws Throwable 如果调用 {@code MethodInvocation.proceed()} 遇到任何错误
     * @see #isLogEnabled
     * @see #writeToLog(Log, String)
     * @see #writeToLog(Log, String, Throwable)
     */
    @Nullable
    protected abstract Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable;
}
