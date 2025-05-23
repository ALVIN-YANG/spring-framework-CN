// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用，除非适用法律要求或经书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照 "原样" 提供分发，
* 不提供任何形式，明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans;

import java.io.Serializable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 用于存储单个属性信息和值的对象。
 * 使用对象，而不是仅仅通过属性名作为键在映射中存储所有属性，这提供了更多的灵活性，并且能够以优化的方式处理索引属性等。
 *
 * <p>注意，值不需要是最终所需的类型：
 * 一个 {@link BeanWrapper} 实现应该处理任何必要的转换，因为此对象对其将要应用的对象一无所知。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 13 May 2001
 * @see PropertyValues
 * @see BeanWrapper
 */
@SuppressWarnings("serial")
public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {

    private final String name;

    @Nullable
    private final Object value;

    private boolean optional = false;

    private boolean converted = false;

    @Nullable
    private Object convertedValue;

    /**
     * 包可见的字段，表示是否需要进行转换。
     */
    @Nullable
    volatile Boolean conversionNecessary;

    /**
     * 用于缓存已解析属性路径标记的包可见字段。
     */
    @Nullable
    transient volatile Object resolvedTokens;

    /**
     * 创建一个新的 PropertyValue 实例。
     * @param name 属性的名称（永不为 {@code null}）
     * @param value 属性的值（可能在类型转换之前）
     */
    public PropertyValue(String name, @Nullable Object value) {
        Assert.notNull(name, "Name must not be null");
        this.name = name;
        this.value = value;
    }

    /**
     * 复制构造函数。
     * @param original 要复制的 PropertyValue 对象（永不为 {@code null}）
     */
    public PropertyValue(PropertyValue original) {
        Assert.notNull(original, "Original must not be null");
        this.name = original.getName();
        this.value = original.getValue();
        this.optional = original.isOptional();
        this.converted = original.converted;
        this.convertedValue = original.convertedValue;
        this.conversionNecessary = original.conversionNecessary;
        this.resolvedTokens = original.resolvedTokens;
        setSource(original.getSource());
        copyAttributesFrom(original);
    }

    /**
     * 构造函数用于公开原始值持有者的新值。
     * 原始持有者将作为新持有者的数据源被公开。
     * @param original 要链接的 PropertyValue（不得为 {@code null}）
     * @param newValue 要应用的新值
     */
    public PropertyValue(PropertyValue original, @Nullable Object newValue) {
        Assert.notNull(original, "Original must not be null");
        this.name = original.getName();
        this.value = newValue;
        this.optional = original.isOptional();
        this.conversionNecessary = original.conversionNecessary;
        this.resolvedTokens = original.resolvedTokens;
        setSource(original);
        copyAttributesFrom(original);
    }

    /**
     * 返回属性的名称。
     */
    public String getName() {
        return this.name;
    }

    /**
     * 返回属性的值。
     * <p>请注意，此处不会发生类型转换。
     * 类型转换是BeanWrapper实现的责任。
     */
    @Nullable
    public Object getValue() {
        return this.value;
    }

    /**
     * 返回此值持有者的原始 PropertyValue 实例。
     * @return 原始的 PropertyValue（此值持有者的来源或此值持有者本身）。
     */
    public PropertyValue getOriginalPropertyValue() {
        PropertyValue original = this;
        Object source = getSource();
        while (source instanceof PropertyValue pv && source != original) {
            original = pv;
            source = original.getSource();
        }
        return original;
    }

    /**
     * 设置此值是否为可选值，即当目标类上不存在相应属性时，将被忽略。
     * @since 3.0
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * 返回此值是否为可选值，即当目标类上不存在相应属性时，应忽略此值。
     * @since 3.0
     */
    public boolean isOptional() {
        return this.optional;
    }

    /**
     * 返回此持有者是否已包含转换后的值（`true`），或者值是否还需要被转换（`false`）。
     */
    public synchronized boolean isConverted() {
        return this.converted;
    }

    /**
     * 设置此属性值的转换后值，
     * 在处理类型转换之后。
     */
    public synchronized void setConvertedValue(@Nullable Object value) {
        this.converted = true;
        this.convertedValue = value;
    }

    /**
     * 返回此属性值的转换后值，
     * 经过类型转换处理。
     */
    @Nullable
    public synchronized Object getConvertedValue() {
        return this.convertedValue;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof PropertyValue that && this.name.equals(that.name) && ObjectUtils.nullSafeEquals(this.value, that.value) && ObjectUtils.nullSafeEquals(getSource(), that.getSource())));
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
    }

    @Override
    public String toString() {
        return "bean property '" + this.name + "'";
    }
}
