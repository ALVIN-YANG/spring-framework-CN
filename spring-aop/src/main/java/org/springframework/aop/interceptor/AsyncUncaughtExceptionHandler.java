// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或经书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.aop.interceptor;

import java.lang.reflect.Method;

/**
 * 处理异步方法抛出的未捕获异常的策略。
 *
 * <p>异步方法通常返回一个 {@link java.util.concurrent.Future} 实例，该实例提供对底层异常的访问。当方法不提供该返回类型时，可以使用此处理器来管理此类未捕获的异常。
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
@FunctionalInterface
public interface AsyncUncaughtExceptionHandler {

    /**
     * 处理从异步方法中抛出的给定未捕获异常。
     * @param ex 从异步方法抛出的异常
     * @param method 异步方法
     * @param params 调用方法使用的参数
     */
    void handleUncaughtException(Throwable ex, Method method, Object... params);
}
