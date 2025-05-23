// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）许可；
除非遵守许可证，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵犯知识产权。
有关许可证的具体语言管辖权限和限制，请参阅许可证。*/
package org.springframework.aop.interceptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.util.function.SingletonSupplier;

/**
 *  异步方法执行方面（如 {@code org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor} 或 {@code org.springframework.scheduling.aspectj.AnnotationAsyncExecutionAspect}）的基类。
 *
 * <p>提供按方法逐个支持 <i>执行器资格</i> 的功能。必须使用默认的 {@code Executor} 构造 {@code AsyncExecutionAspectSupport} 对象，但每个单独的方法可以进一步指定一个特定的 {@code Executor} bean，在执行时使用，例如通过注解属性。
 *
 * @作者 Chris Beams
 * @作者 Juergen Hoeller
 * @作者 Stephane Nicoll
 * @作者 He Bo
 * @作者 Sebastien Deleuze
 * @自 3.1.2以来
 */
public abstract class AsyncExecutionAspectSupport implements BeanFactoryAware {

    /**
     * 默认的 {@link TaskExecutor} Bean 名称用于选择："taskExecutor"。
     * <p>注意，初始查找是按类型进行的；这只是一个后备方案，以防在上下文中找到多个执行器 Bean。
     * @since 4.2.6
     */
    public static final String DEFAULT_TASK_EXECUTOR_BEAN_NAME = "taskExecutor";

    protected final Log logger = LogFactory.getLog(getClass());

    private final Map<Method, AsyncTaskExecutor> executors = new ConcurrentHashMap<>(16);

    private SingletonSupplier<Executor> defaultExecutor;

    private SingletonSupplier<AsyncUncaughtExceptionHandler> exceptionHandler;

    @Nullable
    private BeanFactory beanFactory;

    @Nullable
    private StringValueResolver embeddedValueResolver;

    /**
     * 创建一个使用默认的 {@link AsyncUncaughtExceptionHandler} 的新实例。
     * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring 的 {@code AsyncTaskExecutor}
     * 或 {@link java.util.concurrent.ExecutorService}），除非在异步方法上通过限定符请求了更具体的执行器，
     * 在这种情况下，执行器将在调用时在封装的bean工厂中查找。
     */
    public AsyncExecutionAspectSupport(@Nullable Executor defaultExecutor) {
        this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
        this.exceptionHandler = SingletonSupplier.of(SimpleAsyncUncaughtExceptionHandler::new);
    }

    /**
     * 创建一个新的带有指定异常处理器的 {@link AsyncExecutionAspectSupport}。
     * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring 的 {@code AsyncTaskExecutor}
     * 或 {@link java.util.concurrent.ExecutorService}），除非通过异步方法上的限定符请求了更具体的执行器，
     * 在这种情况下，执行器将在调用时根据封装的 bean 工厂进行查找
     * @param exceptionHandler 要使用的 {@link AsyncUncaughtExceptionHandler}
     */
    public AsyncExecutionAspectSupport(@Nullable Executor defaultExecutor, AsyncUncaughtExceptionHandler exceptionHandler) {
        this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
        this.exceptionHandler = SingletonSupplier.of(exceptionHandler);
    }

    /**
     * 使用给定的执行器（executor）和异常处理器提供者（exception handler suppliers）配置此方面（aspect），
     * 如果提供者无法解析，则应用相应的默认值。
     * @since 5.1
     */
    public void configure(@Nullable Supplier<Executor> defaultExecutor, @Nullable Supplier<AsyncUncaughtExceptionHandler> exceptionHandler) {
        this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
        this.exceptionHandler = new SingletonSupplier<>(exceptionHandler, SimpleAsyncUncaughtExceptionHandler::new);
    }

    /**
     * 提供在执行异步方法时要使用的执行器。
     * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring 的 {@code AsyncTaskExecutor}
     * 或 {@link java.util.concurrent.ExecutorService}），除非异步方法通过限定符请求了更具体的执行器，
     * 在这种情况下，将在调用时通过封装的 bean 工厂查找执行器
     * @see #getExecutorQualifier(Method)
     * @see #setBeanFactory(BeanFactory)
     * @see #getDefaultExecutor(BeanFactory)
     */
    public void setExecutor(Executor defaultExecutor) {
        this.defaultExecutor = SingletonSupplier.of(defaultExecutor);
    }

    /**
     * 提供用于处理通过调用具有{@code void}返回类型的异步方法抛出的异常的{@link AsyncUncaughtExceptionHandler}。
     */
    public void setExceptionHandler(AsyncUncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = SingletonSupplier.of(exceptionHandler);
    }

    /**
     * 设置在通过限定符查找执行器或依赖于默认执行器查找算法时使用的{@link BeanFactory}
     * @see #findQualifiedExecutor(BeanFactory, String)
     * @see #getDefaultExecutor(BeanFactory)
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        if (beanFactory instanceof ConfigurableBeanFactory configurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver(configurableBeanFactory);
        }
    }

    /**
     * 确定执行给定方法时要使用的特定执行器。
     * @return 要使用的执行器（或为空，但仅在没有默认执行器可用时）
     */
    @Nullable
    protected AsyncTaskExecutor determineAsyncExecutor(Method method) {
        AsyncTaskExecutor executor = this.executors.get(method);
        if (executor == null) {
            Executor targetExecutor;
            String qualifier = getExecutorQualifier(method);
            if (this.embeddedValueResolver != null && StringUtils.hasLength(qualifier)) {
                qualifier = this.embeddedValueResolver.resolveStringValue(qualifier);
            }
            if (StringUtils.hasLength(qualifier)) {
                targetExecutor = findQualifiedExecutor(this.beanFactory, qualifier);
            } else {
                targetExecutor = this.defaultExecutor.get();
            }
            if (targetExecutor == null) {
                return null;
            }
            executor = (targetExecutor instanceof AsyncTaskExecutor asyncTaskExecutor ? asyncTaskExecutor : new TaskExecutorAdapter(targetExecutor));
            this.executors.put(method, executor);
        }
        return executor;
    }

    /**
     * 返回执行给定异步方法时要使用的执行器限定符或bean名称，通常以注解属性的形式指定。
     * <p>返回空字符串或{@code null}表示未指定特定执行器，并且应使用{@linkplain #setExecutor(Executor) 默认执行器}。
     * @param method 要检查执行器限定符元数据的异步方法
     * @return 如果指定，则返回限定符，否则返回空字符串或{@code null}
     * @see #determineAsyncExecutor(Method)
     * @see #findQualifiedExecutor(BeanFactory, String)
     */
    @Nullable
    protected abstract String getExecutorQualifier(Method method);

    /**
     * 根据给定的限定符获取目标执行器。
     * @param qualifier 要解析的限定符
     * @return 目标执行器，如果没有可用则返回 {@code null}
     * @since 4.2.6
     * @see #getExecutorQualifier(Method)
     */
    @Nullable
    protected Executor findQualifiedExecutor(@Nullable BeanFactory beanFactory, String qualifier) {
        if (beanFactory == null) {
            throw new IllegalStateException("BeanFactory must be set on " + getClass().getSimpleName() + " to access qualified executor '" + qualifier + "'");
        }
        return BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, Executor.class, qualifier);
    }

    /**
     * 获取或构建此建议实例的默认执行器。
     * <p>从这里返回的执行器将被缓存以供后续使用。
     * <p>默认实现将在上下文中搜索唯一的{@link TaskExecutor} bean，或者在没有找到的情况下搜索名为"taskExecutor"的{@link Executor} bean。
     * 如果两个都没有找到，此实现将返回{@code null}。
     * @param beanFactory 要用于默认执行器查找的BeanFactory
     * @return 默认执行器，如果没有可用则返回{@code null}
     * @since 4.2.6
     * @see #findQualifiedExecutor(BeanFactory, String)
     * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME
     */
    @Nullable
    protected Executor getDefaultExecutor(@Nullable BeanFactory beanFactory) {
        if (beanFactory != null) {
            try {
                // 搜索 TaskExecutor bean... 而不是 plain Executor，因为那样
                // 与ScheduledExecutorService匹配，但它对于以下用途不可用：
                // 此处的目的是。TaskExecutor 更适合用于此目的。
                return beanFactory.getBean(TaskExecutor.class);
            } catch (NoUniqueBeanDefinitionException ex) {
                logger.debug("Could not find unique TaskExecutor bean. " + "Continuing search for an Executor bean named 'taskExecutor'", ex);
                try {
                    return beanFactory.getBean(DEFAULT_TASK_EXECUTOR_BEAN_NAME, Executor.class);
                } catch (NoSuchBeanDefinitionException ex2) {
                    if (logger.isInfoEnabled()) {
                        logger.info("More than one TaskExecutor bean found within the context, and none is named " + "'taskExecutor'. Mark one of them as primary or name it 'taskExecutor' (possibly " + "as an alias) in order to use it for async processing: " + ex.getBeanNamesFound());
                    }
                }
            } catch (NoSuchBeanDefinitionException ex) {
                logger.debug("Could not find default TaskExecutor bean. " + "Continuing search for an Executor bean named 'taskExecutor'", ex);
                try {
                    return beanFactory.getBean(DEFAULT_TASK_EXECUTOR_BEAN_NAME, Executor.class);
                } catch (NoSuchBeanDefinitionException ex2) {
                    logger.info("No task executor bean found for async processing: " + "no bean of type TaskExecutor and no bean named 'taskExecutor' either");
                }
                // 放弃 -> 要么使用本地默认执行器，要么完全不使用...
            }
        }
        return null;
    }

    /**
     * 执行给定任务的委托实现，使用指定的执行器。
     * @param task 要执行的任务
     * @param executor 指定的执行器
     * @param returnType 声明的返回类型（可能是一个 {@link Future} 变体）
     * @return 执行结果（可能是一个相应的 {@link Future} 处理句柄）
     */
    @Nullable
    @SuppressWarnings("deprecation")
    protected Object doSubmit(Callable<Object> task, AsyncTaskExecutor executor, Class<?> returnType) {
        if (CompletableFuture.class.isAssignableFrom(returnType)) {
            return executor.submitCompletable(task);
        } else if (org.springframework.util.concurrent.ListenableFuture.class.isAssignableFrom(returnType)) {
            return ((org.springframework.core.task.AsyncListenableTaskExecutor) executor).submitListenable(task);
        } else if (Future.class.isAssignableFrom(returnType)) {
            return executor.submit(task);
        } else if (void.class == returnType || "kotlin.Unit".equals(returnType.getName())) {
            executor.submit(task);
            return null;
        } else {
            throw new IllegalArgumentException("Invalid return type for async method (only Future and void supported): " + returnType);
        }
    }

    /**
     * 处理在异步调用指定的
     * {@link Method} 时抛出的致命错误。
     * <p>如果方法的返回类型是
     * {@link Future} 对象，原始异常可以通过简单地将其抛出到更高级别来传播。然而，对于所有其他情况，异常将不会传输回客户端。在后一种情况下，将使用当前的
     * {@link AsyncUncaughtExceptionHandler} 来管理此类异常。
     * @param ex 要处理的异常
     * @param method 被调用的方法
     * @param params 调用方法使用的参数
     */
    protected void handleError(Throwable ex, Method method, Object... params) throws Exception {
        if (Future.class.isAssignableFrom(method.getReturnType())) {
            ReflectionUtils.rethrowException(ex);
        } else {
            // 无法将异常通过默认执行器传递给调用者
            try {
                this.exceptionHandler.obtain().handleUncaughtException(ex, method, params);
            } catch (Throwable ex2) {
                logger.warn("Exception handler for async method '" + method.toGenericString() + "' threw unexpected exception itself", ex2);
            }
        }
    }
}
