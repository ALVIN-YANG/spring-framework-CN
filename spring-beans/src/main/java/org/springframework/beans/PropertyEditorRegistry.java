// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。有关许可权限和限制的具体语言，
* 请参阅许可证。*/
package org.springframework.beans;

import java.beans.PropertyEditor;
import org.springframework.lang.Nullable;

/**
 * 封装了注册 JavaBeans 属性编辑器（PropertyEditor）的方法。
 * 这是 PropertyEditorRegistrar 操作的核心接口。
 *
 * <p>由 BeanWrapper 扩展；由 BeanWrapperImpl 和 org.springframework.validation.DataBinder 实现。
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see java.beans.PropertyEditor
 * @see PropertyEditorRegistrar
 * @see BeanWrapper
 * @see org.springframework.validation.DataBinder
 */
public interface PropertyEditorRegistry {

    /**
     * 为给定类型的所有属性注册指定的自定义属性编辑器。
     * @param requiredType 属性的类型
     * @param propertyEditor 要注册的编辑器
     */
    void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);

    /**
     * 为给定类型和属性注册指定的自定义属性编辑器，或者为给定类型的所有属性注册。
     * <p>如果属性路径表示数组或集合属性，则编辑器将应用于数组/集合本身（属性编辑器必须创建数组或集合值）或每个元素（属性编辑器必须创建元素类型），具体取决于指定的所需类型。
     * <p>注意：每个属性路径只支持一个已注册的自定义编辑器。在集合/数组的情况下，不要为集合/数组和同一属性的每个元素注册编辑器。
     * <p>例如，如果您想为 "items[n].quantity"（对于所有值 n）注册编辑器，您将使用 "items.quantity" 作为此方法的 'propertyPath' 参数的值。
     * @param requiredType 属性的类型。如果给出了属性但应该指定，则此值可以为 {@code null}，特别是在集合的情况下 - 明确编辑器是应用于整个集合本身还是应用于其每个条目。因此，作为一般规则：
     * <b>在集合/数组的情况下，不要指定 {@code null}！</b>
     * @param propertyPath 属性的路径（名称或嵌套路径），或如果为给定类型的所有属性注册编辑器，则为 {@code null}
     * @param propertyEditor 要注册的编辑器
     */
    void registerCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath, PropertyEditor propertyEditor);

    /**
     * 查找给定类型和属性的定制属性编辑器。
     * @param requiredType 属性的类型（如果给定属性但类型可以为null，则应始终指定以进行一致性检查）
     * @param propertyPath 属性的路径（名称或嵌套路径），或
     * {@code null} 如果正在寻找给定类型的所有属性的编辑器
     * @return 已注册的编辑器，或
     * {@code null} 如果没有找到
     */
    @Nullable
    PropertyEditor findCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath);
}
