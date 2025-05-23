// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“协议”）许可，您可能只能在不违反协议的情况下使用此文件。
* 您可以在以下链接获取协议副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在协议下分发的软件按“现状”提供，不提供任何形式的明示或暗示保证。
* 请参阅协议了解具体规定许可和限制。*/
package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import org.springframework.lang.Nullable;

/**
 * 与属性访问相关的异常的超类，
 * 例如类型不匹配或调用目标异常。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public abstract class PropertyAccessException extends BeansException {

    @Nullable
    private final PropertyChangeEvent propertyChangeEvent;

    /**
     * 创建一个新的 PropertyAccessException。
     * @param propertyChangeEvent 导致问题的 PropertyChangeEvent
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public PropertyAccessException(PropertyChangeEvent propertyChangeEvent, String msg, @Nullable Throwable cause) {
        super(msg, cause);
        this.propertyChangeEvent = propertyChangeEvent;
    }

    /**
     * 创建一个新的 PropertyAccessException，不伴随 PropertyChangeEvent。
     * @param msg 详细信息
     * @param cause 根本原因
     */
    public PropertyAccessException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
        this.propertyChangeEvent = null;
    }

    /**
     * 返回导致问题的 PropertyChangeEvent。
     * <p>可能为 {@code null}；只有当实际的 bean 属性受到影响时才可用。
     */
    @Nullable
    public PropertyChangeEvent getPropertyChangeEvent() {
        return this.propertyChangeEvent;
    }

    /**
     * 如果有，返回受影响的属性名称。
     */
    @Nullable
    public String getPropertyName() {
        return (this.propertyChangeEvent != null ? this.propertyChangeEvent.getPropertyName() : null);
    }

    /**
     * 返回即将设置的受影响值，如果有任何值的话。
     */
    @Nullable
    public Object getValue() {
        return (this.propertyChangeEvent != null ? this.propertyChangeEvent.getNewValue() : null);
    }

    /**
     * 返回此类异常对应的错误代码。
     */
    public abstract String getErrorCode();
}
