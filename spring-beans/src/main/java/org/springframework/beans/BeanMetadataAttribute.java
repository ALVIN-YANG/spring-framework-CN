// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 用于存储键值风格属性，该属性是Bean定义的一部分。
 * 除了键值对，还跟踪定义的来源。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class BeanMetadataAttribute implements BeanMetadataElement {

    private final String name;

    @Nullable
    private final Object value;

    @Nullable
    private Object source;

    /**
     * 创建一个新的 AttributeValue 实例。
     * @param name 属性的名称（永远不会为 {@code null}）
     * @param value 属性的值（可能在类型转换之前）
     */
    public BeanMetadataAttribute(String name, @Nullable Object value) {
        Assert.notNull(name, "Name must not be null");
        this.name = name;
        this.value = value;
    }

    /**
     * 返回属性的名称。
     */
    public String getName() {
        return this.name;
    }

    /**
     * 返回属性的值。
     */
    @Nullable
    public Object getValue() {
        return this.value;
    }

    /**
     * 设置此元数据元素的配置源对象。
     * <p>对象的精确类型将取决于所使用的配置机制。
     */
    public void setSource(@Nullable Object source) {
        this.source = source;
    }

    @Override
    @Nullable
    public Object getSource() {
        return this.source;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof BeanMetadataAttribute that && this.name.equals(that.name) && ObjectUtils.nullSafeEquals(this.value, that.value) && ObjectUtils.nullSafeEquals(this.source, that.source)));
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
    }

    @Override
    public String toString() {
        return "metadata attribute '" + this.name + "'";
    }
}
