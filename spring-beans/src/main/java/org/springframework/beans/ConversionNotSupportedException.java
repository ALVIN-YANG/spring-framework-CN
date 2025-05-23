// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 包括但不限于适销性、特定用途适用性和非侵权性。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import org.springframework.lang.Nullable;

/**
 * 抛出异常时，无法找到适合的编辑器或转换器来处理bean属性。
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */
@SuppressWarnings("serial")
public class ConversionNotSupportedException extends TypeMismatchException {

    /**
     * 创建一个新的 ConversionNotSupportedException。
     * @param propertyChangeEvent 导致问题的 PropertyChangeEvent
     * @param requiredType 所需的目标类型（如果不知道，则为 {@code null}）
     * @param cause 根本原因（可能为 {@code null}）
     */
    public ConversionNotSupportedException(PropertyChangeEvent propertyChangeEvent, @Nullable Class<?> requiredType, @Nullable Throwable cause) {
        super(propertyChangeEvent, requiredType, cause);
    }

    /**
     * 创建一个新的 ConversionNotSupportedException。
     * @param value 无法转换的违规值（可能为 {@code null}）
     * @param requiredType 所需的目标类型（如果不知道，则为 {@code null}）
     * @param cause 根本原因（可能为 {@code null}）
     */
    public ConversionNotSupportedException(@Nullable Object value, @Nullable Class<?> requiredType, @Nullable Throwable cause) {
        super(value, requiredType, cause);
    }
}
