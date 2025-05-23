// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者。
 
根据Apache许可证版本2.0（以下简称“许可证”）授权；
除非根据适用法律或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权或特定用途的适用性。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop;

import java.lang.reflect.Method;
import org.springframework.lang.Nullable;

/**
 * 在正常方法返回后才会调用返回后建议，不会在抛出异常时调用。此类建议可以看到返回值，但不能修改它。
 *
 * @author Rod Johnson
 * @see MethodBeforeAdvice
 * @see ThrowsAdvice
 */
public interface AfterReturningAdvice extends AfterAdvice {

    /**
     * 在给定方法成功返回后调用的回调。
     * @param returnValue 方法返回的值，如果有的话
     * @param method 被调用的方法
     * @param args 方法的参数
     * @param target 方法调用的目标。可能为 {@code null}。
     * @throws Throwable 如果此对象希望终止调用。
     * 任何抛出的异常如果方法签名允许，将被返回给调用者。否则，异常将被包装为运行时异常。
     */
    void afterReturning(@Nullable Object returnValue, Method method, Object[] args, @Nullable Object target) throws Throwable;
}
