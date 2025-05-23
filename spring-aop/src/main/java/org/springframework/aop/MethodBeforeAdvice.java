// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据 Apache License, Version 2.0（以下简称“许可证”）许可；
除非符合许可证规定，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性或特定用途的适用性的保证。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop;

import java.lang.reflect.Method;
import org.springframework.lang.Nullable;

/**
 * 在方法调用之前调用的建议。除非它们抛出 Throwable，否则此类建议不能阻止方法调用的进行。
 *
 * @author Rod Johnson
 * @see AfterReturningAdvice
 * @see ThrowsAdvice
 */
public interface MethodBeforeAdvice extends BeforeAdvice {

    /**
     * 在调用给定方法之前执行的回调。
     * @param method 正在调用的方法
     * @param args 方法的参数
     * @param target 方法调用的目标。可能为 {@code null}。
     * @throws Throwable 如果此对象希望中止调用。
     * 如果方法签名允许，任何抛出的异常将被返回给调用者。否则，异常将被包装为运行时异常。
     */
    void before(Method method, Object[] args, @Nullable Object target) throws Throwable;
}
