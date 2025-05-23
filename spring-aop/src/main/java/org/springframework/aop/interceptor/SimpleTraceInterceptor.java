// 翻译完成 glm-4-flash
/** 版权所有 2002-2020，原作者或作者。
*
* 根据 Apache 许可证 2.0 版（"许可证"）授权；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权性的保证。
* 请参阅许可证以了解具体管理权限和限制的条款。*/
package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.util.Assert;

/**
 * 简单的 AOP 联盟 {@code MethodInterceptor}，可以被引入到链中，用于显示关于拦截的方法调用的详细跟踪信息，包括方法进入和退出的信息。
 *
 * <p>对于更高级的需求，请考虑使用 {@code CustomizableTraceInterceptor}。
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 1.2
 * @see CustomizableTraceInterceptor
 */
@SuppressWarnings("serial")
public class SimpleTraceInterceptor extends AbstractTraceInterceptor {

    /**
     * 创建一个新的 SimpleTraceInterceptor 实例，并使用静态日志记录器。
     */
    public SimpleTraceInterceptor() {
    }

    /**
     * 根据给定的标志，创建一个新的 SimpleTraceInterceptor，使用动态或静态日志记录器。
     * @param useDynamicLogger 是否使用动态日志记录器或静态日志记录器
     * @see #setUseDynamicLogger
     */
    public SimpleTraceInterceptor(boolean useDynamicLogger) {
        setUseDynamicLogger(useDynamicLogger);
    }

    @Override
    protected Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {
        String invocationDescription = getInvocationDescription(invocation);
        writeToLog(logger, "Entering " + invocationDescription);
        try {
            Object rval = invocation.proceed();
            writeToLog(logger, "Exiting " + invocationDescription);
            return rval;
        } catch (Throwable ex) {
            writeToLog(logger, "Exception thrown in " + invocationDescription, ex);
            throw ex;
        }
    }

    /**
     * 返回对给定方法调用的描述。
     * @param invocation 需要描述的调用
     * @return 描述信息
     */
    protected String getInvocationDescription(MethodInvocation invocation) {
        Object target = invocation.getThis();
        Assert.state(target != null, "Target must not be null");
        String className = target.getClass().getName();
        return "method '" + invocation.getMethod().getName() + "' of class [" + className + "]";
    }
}
