// 翻译完成 glm-4-flash
/** 版权所有 2002-2013 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途适用性和非侵权性。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans;

/**
 * 简单实现 {@link TypeConverter} 接口，不操作特定的目标对象。这是在需要任意类型转换时，使用完整的 BeanWrapperImpl 实例的替代方案，同时使用相同的转换算法（包括委托给 {@link java.beans.PropertyEditor} 和 {@link org.springframework.core.convert.ConversionService}）。
 *
 * <p><b>注意：</b>由于依赖于 {@link java.beans.PropertyEditor PropertyEditors}，SimpleTypeConverter <em>不是</em> 线程安全的。每个线程应使用单独的实例。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeanWrapperImpl
 */
public class SimpleTypeConverter extends TypeConverterSupport {

    public SimpleTypeConverter() {
        this.typeConverterDelegate = new TypeConverterDelegate(this);
        registerDefaultEditors();
    }
}
