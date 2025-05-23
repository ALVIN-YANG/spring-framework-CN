// 翻译完成 glm-4-flash
/*版权所有 2002-2020 原作者或作者们。

根据Apache License, Version 2.0（以下简称“许可证”）；除非符合许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework.adapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AfterAdvice;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于包装抛出异常后通知的拦截器。
 *
 * <p>在 {@code ThrowsAdvice} 实现方法参数上的处理器方法签名必须是以下形式：<br>
 *
 * {@code void afterThrowing([Method, args, target], ThrowableSubclass);}
 *
 * <p>只需要最后一个参数。
 *
 * <p>一些有效的示例方法包括：
 *
 * <pre class="code">public void afterThrowing(Exception ex)</pre>
 * <pre class="code">public void afterThrowing(RemoteException)</pre>
 * <pre class="code">public void afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre>
 * <pre class="code">public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)</pre>
 *
 * <p>这是一个框架类，Spring用户通常不需要直接使用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see MethodBeforeAdviceInterceptor
 * @see AfterReturningAdviceInterceptor
 */
public class ThrowsAdviceInterceptor implements MethodInterceptor, AfterAdvice {

    private static final String AFTER_THROWING = "afterThrowing";

    private static final Log logger = LogFactory.getLog(ThrowsAdviceInterceptor.class);

    private final Object throwsAdvice;

    /**
     * 根据异常类键控的 throws 建议方法。
     */
    private final Map<Class<?>, Method> exceptionHandlerMap = new HashMap<>();

    /**
     * 为给定的ThrowsAdvice创建一个新的ThrowsAdviceInterceptor。
     * @param throwsAdvice 定义异常处理方法的建议对象（通常是一个 {@link org.springframework.aop.ThrowsAdvice} 实现）
     */
    public ThrowsAdviceInterceptor(Object throwsAdvice) {
        Assert.notNull(throwsAdvice, "Advice must not be null");
        this.throwsAdvice = throwsAdvice;
        Method[] methods = throwsAdvice.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(AFTER_THROWING) && (method.getParameterCount() == 1 || method.getParameterCount() == 4)) {
                Class<?> throwableParam = method.getParameterTypes()[method.getParameterCount() - 1];
                if (Throwable.class.isAssignableFrom(throwableParam)) {
                    // 异常处理器以注册...
                    this.exceptionHandlerMap.put(throwableParam, method);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found exception handler method on throws advice: " + method);
                    }
                }
            }
        }
        if (this.exceptionHandlerMap.isEmpty()) {
            throw new IllegalArgumentException("At least one handler method must be found in class [" + throwsAdvice.getClass() + "]");
        }
    }

    /**
     * 返回此建议中处理器方法的数量。
     */
    public int getHandlerMethodCount() {
        return this.exceptionHandlerMap.size();
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        } catch (Throwable ex) {
            Method handlerMethod = getExceptionHandler(ex);
            if (handlerMethod != null) {
                invokeHandlerMethod(mi, ex, handlerMethod);
            }
            throw ex;
        }
    }

    /**
     * 确定给定异常的异常处理方法。
     * @param exception 抛出的异常
     * @return 对应给定异常类型的处理器，如果没有找到则返回 {@code null}
     */
    @Nullable
    private Method getExceptionHandler(Throwable exception) {
        Class<?> exceptionClass = exception.getClass();
        if (logger.isTraceEnabled()) {
            logger.trace("Trying to find handler for exception of type [" + exceptionClass.getName() + "]");
        }
        Method handler = this.exceptionHandlerMap.get(exceptionClass);
        while (handler == null && exceptionClass != Throwable.class) {
            exceptionClass = exceptionClass.getSuperclass();
            handler = this.exceptionHandlerMap.get(exceptionClass);
        }
        if (handler != null && logger.isTraceEnabled()) {
            logger.trace("Found handler for exception of type [" + exceptionClass.getName() + "]: " + handler);
        }
        return handler;
    }

    private void invokeHandlerMethod(MethodInvocation mi, Throwable ex, Method method) throws Throwable {
        Object[] handlerArgs;
        if (method.getParameterCount() == 1) {
            handlerArgs = new Object[] { ex };
        } else {
            handlerArgs = new Object[] { mi.getMethod(), mi.getArguments(), mi.getThis(), ex };
        }
        try {
            method.invoke(this.throwsAdvice, handlerArgs);
        } catch (InvocationTargetException targetEx) {
            throw targetEx.getTargetException();
        }
    }
}
