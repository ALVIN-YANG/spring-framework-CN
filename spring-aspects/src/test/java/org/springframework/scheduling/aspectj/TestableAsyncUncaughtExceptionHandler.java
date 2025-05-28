// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.scheduling.aspectj;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 一个用于测试目的的 {@link AsyncUncaughtExceptionHandler} 实现。
 *
 * @author Stephane Nicoll
 */
class TestableAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final CountDownLatch latch = new CountDownLatch(1);

    private UncaughtExceptionDescriptor descriptor;

    private final boolean throwUnexpectedException;

    TestableAsyncUncaughtExceptionHandler() {
        this(false);
    }

    TestableAsyncUncaughtExceptionHandler(boolean throwUnexpectedException) {
        this.throwUnexpectedException = throwUnexpectedException;
    }

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        descriptor = new UncaughtExceptionDescriptor(ex, method);
        this.latch.countDown();
        if (throwUnexpectedException) {
            throw new IllegalStateException("Test exception");
        }
    }

    public boolean isCalled() {
        return descriptor != null;
    }

    public void assertCalledWith(Method expectedMethod, Class<? extends Throwable> expectedExceptionType) {
        assertThat(descriptor).as("Handler not called").isNotNull();
        assertThat(descriptor.ex.getClass()).as("Wrong exception type").isEqualTo(expectedExceptionType);
        assertThat(descriptor.method).as("Wrong method").isEqualTo(expectedMethod);
    }

    public void await(long timeout) {
        try {
            this.latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class UncaughtExceptionDescriptor {

        private final Throwable ex;

        private final Method method;

        private UncaughtExceptionDescriptor(Throwable ex, Method method) {
            this.ex = ex;
            this.method = method;
        }
    }
}
