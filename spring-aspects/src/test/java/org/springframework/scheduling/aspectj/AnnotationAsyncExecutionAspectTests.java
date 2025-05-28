// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.scheduling.aspectj;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.testfixture.EnabledForTestGroups;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.concurrent.ListenableFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.testfixture.TestGroup.LONG_RUNNING;

/**
 * 对 {@link AnnotationAsyncExecutionAspect} 的单元测试。
 *
 * @author Ramnivas Laddad
 * @author Stephane Nicoll
 */
@EnabledForTestGroups(LONG_RUNNING)
public class AnnotationAsyncExecutionAspectTests {

    // 毫秒
    private static final long WAIT_TIME = 1000;

    private final AsyncUncaughtExceptionHandler defaultExceptionHandler = new SimpleAsyncUncaughtExceptionHandler();

    private CountingExecutor executor;

    @BeforeEach
    public void setUp() {
        executor = new CountingExecutor();
        AnnotationAsyncExecutionAspect.aspectOf().setExecutor(executor);
    }

    @Test
    public void asyncMethodGetsRoutedAsynchronously() {
        ClassWithoutAsyncAnnotation obj = new ClassWithoutAsyncAnnotation();
        obj.incrementAsync();
        executor.waitForCompletion();
        assertThat(obj.counter).isEqualTo(1);
        assertThat(executor.submitStartCounter).isEqualTo(1);
        assertThat(executor.submitCompleteCounter).isEqualTo(1);
    }

    @Test
    public void asyncMethodReturningFutureGetsRoutedAsynchronouslyAndReturnsAFuture() throws InterruptedException, ExecutionException {
        ClassWithoutAsyncAnnotation obj = new ClassWithoutAsyncAnnotation();
        Future<Integer> future = obj.incrementReturningAFuture();
        // 无需使用executor.waitForCompletion()，因为future.get()将产生相同的效果
        assertThat(future.get().intValue()).isEqualTo(5);
        assertThat(obj.counter).isEqualTo(1);
        assertThat(executor.submitStartCounter).isEqualTo(1);
        assertThat(executor.submitCompleteCounter).isEqualTo(1);
    }

    @Test
    public void syncMethodGetsRoutedSynchronously() {
        ClassWithoutAsyncAnnotation obj = new ClassWithoutAsyncAnnotation();
        obj.increment();
        assertThat(obj.counter).isEqualTo(1);
        assertThat(executor.submitStartCounter).isEqualTo(0);
        assertThat(executor.submitCompleteCounter).isEqualTo(0);
    }

    @Test
    public void voidMethodInAsyncClassGetsRoutedAsynchronously() {
        ClassWithAsyncAnnotation obj = new ClassWithAsyncAnnotation();
        obj.increment();
        executor.waitForCompletion();
        assertThat(obj.counter).isEqualTo(1);
        assertThat(executor.submitStartCounter).isEqualTo(1);
        assertThat(executor.submitCompleteCounter).isEqualTo(1);
    }

    @Test
    public void methodReturningFutureInAsyncClassGetsRoutedAsynchronouslyAndReturnsAFuture() throws InterruptedException, ExecutionException {
        ClassWithAsyncAnnotation obj = new ClassWithAsyncAnnotation();
        Future<Integer> future = obj.incrementReturningAFuture();
        assertThat(future.get().intValue()).isEqualTo(5);
        assertThat(obj.counter).isEqualTo(1);
        assertThat(executor.submitStartCounter).isEqualTo(1);
        assertThat(executor.submitCompleteCounter).isEqualTo(1);
    }

    /*@Test
// 测试异步类中返回非void、非Future类型的方法会同步路由
public void methodReturningNonVoidNonFutureInAsyncClassGetsRoutedSynchronously() {
    ClassWithAsyncAnnotation obj = new ClassWithAsyncAnnotation();
    int returnValue = obj.return5();
    assertEquals(5, returnValue);
    assertEquals(0, executor.submitStartCounter);
    assertEquals(0, executor.submitCompleteCounter);
}*/
    @Test
    public void qualifiedAsyncMethodsAreRoutedToCorrectExecutor() throws InterruptedException, ExecutionException {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("e1", new RootBeanDefinition(ThreadPoolTaskExecutor.class));
        AnnotationAsyncExecutionAspect.aspectOf().setBeanFactory(beanFactory);
        ClassWithQualifiedAsyncMethods obj = new ClassWithQualifiedAsyncMethods();
        Future<Thread> defaultThread = obj.defaultWork();
        assertThat(defaultThread.get()).isNotEqualTo(Thread.currentThread());
        assertThat(defaultThread.get().getName()).doesNotStartWith("e1-");
        ListenableFuture<Thread> e1Thread = obj.e1Work();
        assertThat(e1Thread.get().getName()).startsWith("e1-");
        CompletableFuture<Thread> e1OtherThread = obj.e1OtherWork();
        assertThat(e1OtherThread.get().getName()).startsWith("e1-");
    }

    @Test
    public void exceptionHandlerCalled() {
        Method m = ReflectionUtils.findMethod(ClassWithException.class, "failWithVoid");
        TestableAsyncUncaughtExceptionHandler exceptionHandler = new TestableAsyncUncaughtExceptionHandler();
        AnnotationAsyncExecutionAspect.aspectOf().setExceptionHandler(exceptionHandler);
        try {
            assertThat(exceptionHandler.isCalled()).as("Handler should not have been called").isFalse();
            ClassWithException obj = new ClassWithException();
            obj.failWithVoid();
            exceptionHandler.await(3000);
            exceptionHandler.assertCalledWith(m, UnsupportedOperationException.class);
        } finally {
            AnnotationAsyncExecutionAspect.aspectOf().setExceptionHandler(defaultExceptionHandler);
        }
    }

    @Test
    public void exceptionHandlerNeverThrowsUnexpectedException() {
        Method m = ReflectionUtils.findMethod(ClassWithException.class, "failWithVoid");
        TestableAsyncUncaughtExceptionHandler exceptionHandler = new TestableAsyncUncaughtExceptionHandler(true);
        AnnotationAsyncExecutionAspect.aspectOf().setExceptionHandler(exceptionHandler);
        try {
            assertThat(exceptionHandler.isCalled()).as("Handler should not have been called").isFalse();
            ClassWithException obj = new ClassWithException();
            obj.failWithVoid();
            exceptionHandler.await(3000);
            exceptionHandler.assertCalledWith(m, UnsupportedOperationException.class);
        } finally {
            AnnotationAsyncExecutionAspect.aspectOf().setExceptionHandler(defaultExceptionHandler);
        }
    }

    @SuppressWarnings("serial")
    private static class CountingExecutor extends SimpleAsyncTaskExecutor {

        int submitStartCounter;

        int submitCompleteCounter;

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            submitStartCounter++;
            Future<T> future = super.submit(task);
            submitCompleteCounter++;
            synchronized (this) {
                notifyAll();
            }
            return future;
        }

        public synchronized void waitForCompletion() {
            try {
                wait(WAIT_TIME);
            } catch (InterruptedException ex) {
                throw new AssertionError("Didn't finish the async job in " + WAIT_TIME + " milliseconds");
            }
        }
    }

    static class ClassWithoutAsyncAnnotation {

        int counter;

        @Async
        public void incrementAsync() {
            counter++;
        }

        public void increment() {
            counter++;
        }

        @Async
        public Future<Integer> incrementReturningAFuture() {
            counter++;
            return new AsyncResult<Integer>(5);
        }
        /**
         * 应该抛出一个错误，如果将@Async应用于返回非void或非Future的方法。此方法必须保持注释状态，否则将产生编译时错误。取消注释以手动验证编译器是否由于在`@link AnnotationAsyncExecutionAspect`中的'declare error'语句而产生错误信息。
         */
        // @异步 public int getInt() {
        // 返回 0；
        // 由于您提供的代码注释内容为空，因此无法进行翻译。请提供具体的英文代码注释内容，以便我能够将其翻译成中文。
    }

    @Async
    static class ClassWithAsyncAnnotation {

        int counter;

        public void increment() {
            counter++;
        }

        // 手动检查是否存在来自 'declare warning' 语句的警告。
        // 注解异步执行切面
        /*public int return5() {
			return 5;
		}

// 返回5的整数值*/
        public Future<Integer> incrementReturningAFuture() {
            counter++;
            return new AsyncResult<Integer>(5);
        }
    }

    static class ClassWithQualifiedAsyncMethods {

        @Async
        public Future<Thread> defaultWork() {
            return new AsyncResult<Thread>(Thread.currentThread());
        }

        @Async("e1")
        public ListenableFuture<Thread> e1Work() {
            return new AsyncResult<Thread>(Thread.currentThread());
        }

        @Async("e1")
        public CompletableFuture<Thread> e1OtherWork() {
            return CompletableFuture.completedFuture(Thread.currentThread());
        }
    }

    static class ClassWithException {

        @Async
        public void failWithVoid() {
            throw new UnsupportedOperationException("failWithVoid");
        }
    }
}
