// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，除非法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下链接处获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议以了解具体管理许可和限制的条款。*/
package org.springframework.beans;

import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;

/**
 * 封装 PropertyAccessor 配置方法的接口。
 * 同时扩展了 PropertyEditorRegistry 接口，该接口定义了 PropertyEditor 管理的方法。
 *
 * <p>作为 {@link BeanWrapper} 的基础接口。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.0
 * @see BeanWrapper
 */
public interface ConfigurablePropertyAccessor extends PropertyAccessor, PropertyEditorRegistry, TypeConverter {

    /**
     * 指定一个用于转换属性值的 {@link ConversionService}，作为 JavaBeans 属性编辑器的替代方案。
     */
    void setConversionService(@Nullable ConversionService conversionService);

    /**
     * 如果存在，则返回关联的ConversionService。
     */
    @Nullable
    ConversionService getConversionService();

    /**
     * 设置是否在将属性编辑器应用于属性的新值时提取旧属性值。
     */
    void setExtractOldValueForEditor(boolean extractOldValueForEditor);

    /**
     * 返回在将属性编辑器应用于属性的新值时，是否提取旧属性值。
     */
    boolean isExtractOldValueForEditor();

    /**
     * 设置此实例是否尝试“自动增长”包含一个 {@code null} 值的嵌套路径。
     * <p>如果为 {@code true}，则将使用默认对象值填充一个 {@code null} 路径位置，并对其进行遍历，而不是抛出 {@link NullValueInNestedPathException}。
     * <p>对于普通的 PropertyAccessor 实例，默认值为 {@code false}。
     */
    void setAutoGrowNestedPaths(boolean autoGrowNestedPaths);

    /**
     * 返回是否已激活嵌套路径的“自动增长”。
     */
    boolean isAutoGrowNestedPaths();
}
