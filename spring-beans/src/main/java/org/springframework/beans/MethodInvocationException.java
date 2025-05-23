// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。有关许可权的具体语言和限制，
* 请参阅许可证。*/
package org.springframework.beans;

import java.beans.PropertyChangeEvent;

/**
 * 抛出当Bean属性的getter或setter方法抛出异常时使用，类似于InvocationTargetException。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public class MethodInvocationException extends PropertyAccessException {

    /**
     * 方法调用错误将注册的错误代码。
     */
    public static final String ERROR_CODE = "methodInvocation";

    /**
     * 创建一个新的 MethodInvocationException。
     * @param propertyChangeEvent 导致异常的 PropertyChangeEvent
     * @param cause 被调用方法抛出的 Throwable
     */
    public MethodInvocationException(PropertyChangeEvent propertyChangeEvent, Throwable cause) {
        super(propertyChangeEvent, "Property '" + propertyChangeEvent.getPropertyName() + "' threw exception", cause);
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
