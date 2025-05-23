// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权或特定用途的适用性。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans;

import java.lang.reflect.Field;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;

/**
 * 定义类型转换方法的接口。通常（但不一定）与 {@link PropertyEditorRegistry} 接口一起实现。
 *
 * <p><b>注意：</b>由于 TypeConverter 实现通常基于 {@link java.beans.PropertyEditor PropertyEditors}，而这些编辑器不是线程安全的，
 * 因此 TypeConverters 本身也不应该被视为线程安全的。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleTypeConverter
 * @see BeanWrapperImpl
 */
public interface TypeConverter {

    /**
     * 将值转换为所需的类型（如果需要，从 String 转换）。
     * <p>从 String 转换到任何类型的转换通常使用 PropertyEditor 类的 {@code setAsText} 方法，或者 ConversionService 中的 Spring 转换器。
     * @param value 要转换的值
     * @param requiredType 我们必须转换到的类型
     * （或 {@code null} 如果未知，例如在集合元素的情况下）
     * @return 新的值，可能是类型转换的结果
     * @throws TypeMismatchException 如果类型转换失败
     * @see java.beans.PropertyEditor#setAsText(String)
     * @see java.beans.PropertyEditor#getValue()
     * @see org.springframework.core.convert.ConversionService
     * @see org.springframework.core.convert.converter.Converter
     */
    @Nullable
    <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType) throws TypeMismatchException;

    /**
     * 将值转换为所需的类型（如果需要，从字符串转换）。
     * <p>从字符串到任何类型的转换通常使用 PropertyEditor 类的 {@code setAsText} 方法，或者 ConversionService 中的 Spring 转换器。
     * @param value 要转换的值
     * @param requiredType 我们必须转换到的类型
     * （或 {@code null} 如果未知，例如在集合元素的情况下）
     * @param methodParam 转换的目标方法参数
     * （用于泛型类型的分析；可能为 {@code null}）
     * @return 新的值，可能是类型转换的结果
     * @throws TypeMismatchException 如果类型转换失败
     * @see java.beans.PropertyEditor#setAsText(String)
     * @see java.beans.PropertyEditor#getValue()
     * @see org.springframework.core.convert.ConversionService
     * @see org.springframework.core.convert.converter.Converter
     */
    @Nullable
    <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType, @Nullable MethodParameter methodParam) throws TypeMismatchException;

    /**
     * 将值转换为所需的类型（如果需要从 String 转换）。
     * <p>从 String 到任何类型的转换通常使用 PropertyEditor 类的 {@code setAsText} 方法，
     * 或者是在 ConversionService 中的 Spring Converter。
     * @param value 要转换的值
     * @param requiredType 我们必须转换到的类型
     * （或为 {@code null}，例如在集合元素的情况下）
     * @param field 转换的目标反射字段
     * （用于泛型类型的分析；可能为 {@code null}）
     * @return 新的值，可能是类型转换的结果
     * @throws TypeMismatchException 如果类型转换失败
     * @see java.beans.PropertyEditor#setAsText(String)
     * @see java.beans.PropertyEditor#getValue()
     * @see org.springframework.core.convert.ConversionService
     * @see org.springframework.core.convert.converter.Converter
     */
    @Nullable
    <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType, @Nullable Field field) throws TypeMismatchException;

    /**
     * 将值转换为所需的类型（如果需要，从字符串转换）。
     * <p>从字符串转换为任何类型的转换通常使用 PropertyEditor 类的 {@code setAsText} 方法，或者使用 ConversionService 的 Spring 转换器。
     * @param value 要转换的值
     * @param requiredType 我们必须转换到的类型
     * （或 {@code null} 如果未知，例如在集合元素的情况下）
     * @param typeDescriptor 要使用的类型描述符（可能为 {@code null}）
     * @return 新值，可能是类型转换的结果
     * @throws TypeMismatchException 如果类型转换失败
     * @since 5.1.4
     * @see java.beans.PropertyEditor#setAsText(String)
     * @see java.beans.PropertyEditor#getValue()
     * @see org.springframework.core.convert.ConversionService
     * @see org.springframework.core.convert.converter.Converter
     */
    @Nullable
    default <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType, @Nullable TypeDescriptor typeDescriptor) throws TypeMismatchException {
        throw new UnsupportedOperationException("TypeDescriptor resolution not supported");
    }
}
