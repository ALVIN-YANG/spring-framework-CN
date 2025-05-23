// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）许可；
除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权或特定用途的。
有关许可的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.aop.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 *  AOP Alliance 的 {@code MethodInterceptor}，它使用给定的 {@link org.springframework.core.task.AsyncTaskExecutor} 异步处理方法调用。
 *  通常与 {@link org.springframework.scheduling.annotation.Async} 注解一起使用。
 *
 * <p>在目标方法签名方面，支持任何参数类型。然而，返回类型限制为 {@code void} 或 {@code java.util.concurrent.Future}。在后一种情况下，代理返回的 Future 处理器将是一个真实的异步 Future，可以用来跟踪异步方法执行的最终结果。但是，由于目标方法需要实现相同的签名，它将不得不返回一个临时 Future 处理器，该处理器仅将返回值传递出去（类似于 Spring 的 {@link org.springframework.scheduling.annotation.AsyncResult} 或 EJB 的 {@code jakarta.ejb.AsyncResult}）。
 *
 * <p>当返回类型为 {@code java.util.concurrent.Future} 时，在执行过程中抛出的任何异常都可以被调用者访问和管理。然而，对于返回类型为 {@code void} 的情况，这些异常无法传输回。在这种情况下，可以注册一个 {@link AsyncUncaughtExceptionHandler} 来处理这些异常。
 *
 * <p>注意：由于支持与 Spring 的 {@code @Async} 注解一起的执行器资格，因此优先使用 {@code AnnotationAsyncExecutionInterceptor} 子类。
 *
 * @since 3.0
 * @see org.springframework.scheduling.annotation.Async
 * @see org.springframework.scheduling.annotation.AsyncAnnotationAdvisor
 * @see org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor
 */
public class AsyncExecutionInterceptor extends AsyncExecutionAspectSupport implements MethodInterceptor, Ordered {

    /**
     * 创建一个新的实例，并使用默认的 {@link AsyncUncaughtExceptionHandler}。
     * @param defaultExecutor 要委托的 {@link Executor}（通常是 Spring 的 {@link AsyncTaskExecutor}
     * 或 {@link java.util.concurrent.ExecutorService}）；否则将为这个拦截器构建一个本地执行器。
     */
    public AsyncExecutionInterceptor(@Nullable Executor defaultExecutor) {
        super(defaultExecutor);
    }

    /**
     * 创建一个新的 {@code AsyncExecutionInterceptor}。
     * @param defaultExecutor 要委托的 {@link Executor}（通常是 Spring 的 {@link AsyncTaskExecutor}
     * 或 {@link java.util.concurrent.ExecutorService}）；否则将为这个拦截器构建一个本地执行器
     * @param exceptionHandler 要使用的 {@link AsyncUncaughtExceptionHandler}
     */
    public AsyncExecutionInterceptor(@Nullable Executor defaultExecutor, AsyncUncaughtExceptionHandler exceptionHandler) {
        super(defaultExecutor, exceptionHandler);
    }

    /**
     * 拦截给定的方法调用，将方法的实际调用提交给正确的任务执行器，并立即返回给调用者。
     * @param invocation 要拦截并异步执行的方法
     * @return 如果原始方法返回的是 {@link Future}，则返回一个 {@link Future}；否则返回 {@code null}。
     */
    @Override
    @Nullable
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
        Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
        final Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        AsyncTaskExecutor executor = determineAsyncExecutor(userDeclaredMethod);
        if (executor == null) {
            throw new IllegalStateException("No executor specified and no default executor set on AsyncExecutionInterceptor either");
        }
        Callable<Object> task = () -> {
            try {
                Object result = invocation.proceed();
                if (result instanceof Future<?> future) {
                    return future.get();
                }
            } catch (ExecutionException ex) {
                handleError(ex.getCause(), userDeclaredMethod, invocation.getArguments());
            } catch (Throwable ex) {
                handleError(ex, userDeclaredMethod, invocation.getArguments());
            }
            return null;
        };
        return doSubmit(task, executor, invocation.getMethod().getReturnType());
    }

    /**
     * 获取执行给定方法时应使用的特定执行器的限定符。
     * <p>此方法的默认实现实际上是一个空操作。
     * <p>子类可以重写此方法以提供提取限定符信息的功能 &mdash; 例如，通过给定方法上的注解。
     * @return 总是返回 {@code null}
     * @since 3.1.2
     * @see #determineAsyncExecutor(Method)
     */
    @Override
    @Nullable
    protected String getExecutorQualifier(Method method) {
        return null;
    }

    /**
     * 此实现将在上下文中搜索一个唯一的 {@link org.springframework.core.task.TaskExecutor} bean，或者如果找不到，则搜索名为 "taskExecutor" 的 {@link Executor} bean。如果这两个都不可以解析（例如，如果没有配置任何 {@code BeanFactory}），则此实现将回退到新创建的 {@link SimpleAsyncTaskExecutor} 实例以供本地使用，如果找不到默认值。
     * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME
     */
    @Override
    @Nullable
    protected Executor getDefaultExecutor(@Nullable BeanFactory beanFactory) {
        Executor defaultExecutor = super.getDefaultExecutor(beanFactory);
        return (defaultExecutor != null ? defaultExecutor : new SimpleAsyncTaskExecutor());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
