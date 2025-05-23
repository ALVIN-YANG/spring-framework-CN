// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证以了解具体规定权限和限制。*/
package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 在尝试设置一个 Bean 属性时，由于类型不匹配而抛出异常。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class TypeMismatchException extends PropertyAccessException {

    /**
     * 将会与类型不匹配错误注册的错误代码。
     */
    public static final String ERROR_CODE = "typeMismatch";

    @Nullable
    private String propertyName;

    @Nullable
    private final transient Object value;

    @Nullable
    private final Class<?> requiredType;

    /**
     * 创建一个新的 {@code TypeMismatchException}。
     * @param propertyChangeEvent 导致问题的属性变更事件
     * @param requiredType 所需的目标类型
     */
    public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class<?> requiredType) {
        this(propertyChangeEvent, requiredType, null);
    }

    /**
     * 创建一个新的 {@code TypeMismatchException}。
     * @param propertyChangeEvent 导致问题的 PropertyChangeEvent
     * @param requiredType 所需的目标类型（如果不知道，则为 {@code null}）
     * @param cause 根本原因（可能为 {@code null}）
     */
    public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, @Nullable Class<?> requiredType, @Nullable Throwable cause) {
        super(propertyChangeEvent, "Failed to convert property value of type '" + ClassUtils.getDescriptiveType(propertyChangeEvent.getNewValue()) + "'" + (requiredType != null ? " to required type '" + ClassUtils.getQualifiedName(requiredType) + "'" : "") + (propertyChangeEvent.getPropertyName() != null ? " for property '" + propertyChangeEvent.getPropertyName() + "'" : "") + (cause != null ? "; " + cause.getMessage() : ""), cause);
        this.propertyName = propertyChangeEvent.getPropertyName();
        this.value = propertyChangeEvent.getNewValue();
        this.requiredType = requiredType;
    }

    /**
     * 创建一个新的无{@code PropertyChangeEvent}的{@code TypeMismatchException}。
     * @param value 无法转换的违规值（可能为{@code null}）
     * @param requiredType 所需的目标类型（如果不知道则为{@code null}）
     * @see #initPropertyName
     */
    public TypeMismatchException(@Nullable Object value, @Nullable Class<?> requiredType) {
        this(value, requiredType, null);
    }

    /**
     * 创建一个新的无 {@code PropertyChangeEvent} 的 {@code TypeMismatchException}。
     * @param value 无法转换的违规值（可能为 {@code null}）
     * @param requiredType 所需的目标类型（如果不知道，则为 {@code null}）
     * @param cause 根本原因（可能为 {@code null}）
     * @see #initPropertyName
     */
    public TypeMismatchException(@Nullable Object value, @Nullable Class<?> requiredType, @Nullable Throwable cause) {
        super("Failed to convert value of type '" + ClassUtils.getDescriptiveType(value) + "'" + (requiredType != null ? " to required type '" + ClassUtils.getQualifiedName(requiredType) + "'" : "") + (cause != null ? "; " + cause.getMessage() : ""), cause);
        this.value = value;
        this.requiredType = requiredType;
    }

    /**
     * 初始化此异常的属性名称，以便通过 {@link #getPropertyName()} 方法暴露，作为通过 {@link PropertyChangeEvent} 初始化的替代方案。
     * @param propertyName 要暴露的属性名称
     * @since 5.0.4
     * @see #TypeMismatchException(Object, Class)
     * @see #TypeMismatchException(Object, Class, Throwable)
     */
    public void initPropertyName(String propertyName) {
        Assert.state(this.propertyName == null, "Property name already initialized");
        this.propertyName = propertyName;
    }

    /**
     * 如果可用，则返回受影响的属性名称。
     */
    @Override
    @Nullable
    public String getPropertyName() {
        return this.propertyName;
    }

    /**
     * 返回违规的值（可能为{@code null}）。
     */
    @Override
    @Nullable
    public Object getValue() {
        return this.value;
    }

    /**
     * 如果有，则返回所需的目标类型。
     */
    @Nullable
    public Class<?> getRequiredType() {
        return this.requiredType;
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
