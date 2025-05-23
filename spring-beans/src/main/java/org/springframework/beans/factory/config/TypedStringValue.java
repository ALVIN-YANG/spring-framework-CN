// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0 ("许可协议") 进行许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接处获取许可协议副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的
* 还是暗示的。有关许可权限和限制的具体语言，请参阅许可协议。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 用于存储类型化字符串值的容器。可以通过将其添加到bean定义中，显式指定字符串值的目标类型，例如用于集合元素。
 *
 * <p>此容器仅存储字符串值和目标类型。实际的转换将由bean工厂执行。
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see BeanDefinition#getPropertyValues
 * @see org.springframework.beans.MutablePropertyValues#addPropertyValue
 */
public class TypedStringValue implements BeanMetadataElement {

    @Nullable
    private String value;

    @Nullable
    private volatile Object targetType;

    @Nullable
    private Object source;

    @Nullable
    private String specifiedTypeName;

    private volatile boolean dynamic;

    /**
     * 为给定的字符串值创建一个新的 {@link TypedStringValue}。
     * @param value 字符串值
     */
    public TypedStringValue(@Nullable String value) {
        setValue(value);
    }

    /**
     * 为给定的字符串值和目标类型创建一个新的 {@link TypedStringValue}
     * @param value 字符串值
     * @param targetType 要转换到的类型
     */
    public TypedStringValue(@Nullable String value, Class<?> targetType) {
        setValue(value);
        setTargetType(targetType);
    }

    /**
     * 为给定的字符串值和目标类型创建一个新的 {@link TypedStringValue}。
     * @param value 字符串值
     * @param targetTypeName 要转换到的类型
     */
    public TypedStringValue(@Nullable String value, String targetTypeName) {
        setValue(value);
        setTargetTypeName(targetTypeName);
    }

    /**
     * 设置字符串值。
     * <p>仅在操作已注册的值时必要，
     * 例如在BeanFactoryPostProcessors中。
     */
    public void setValue(@Nullable String value) {
        this.value = value;
    }

    /**
     * 返回字符串值。
     */
    @Nullable
    public String getValue() {
        return this.value;
    }

    /**
     * 设置要转换的类型。
     * <p>仅在操作已注册的值时必要，例如在BeanFactoryPostProcessors中。
     */
    public void setTargetType(Class<?> targetType) {
        Assert.notNull(targetType, "'targetType' must not be null");
        this.targetType = targetType;
    }

    /**
     * 返回要转换的类型。
     */
    public Class<?> getTargetType() {
        Object targetTypeValue = this.targetType;
        if (!(targetTypeValue instanceof Class<?> clazz)) {
            throw new IllegalStateException("Typed String value does not carry a resolved target type");
        }
        return clazz;
    }

    /**
     * 指定要转换到的类型。
     */
    public void setTargetTypeName(@Nullable String targetTypeName) {
        this.targetType = targetTypeName;
    }

    /**
     * 返回要转换的类型。
     */
    @Nullable
    public String getTargetTypeName() {
        Object targetTypeValue = this.targetType;
        if (targetTypeValue instanceof Class<?> clazz) {
            return clazz.getName();
        } else {
            return (String) targetTypeValue;
        }
    }

    /**
     * 返回此类型化的字符串值是否携带目标类型。
     */
    public boolean hasTargetType() {
        return (this.targetType instanceof Class);
    }

    /**
     * 确定要转换到的类型，如有必要，从指定的类名中解析它。当调用时已解析目标类型，也会重新加载指定类。
     * @param classLoader 用于解析（潜在的）类名的 ClassLoader
     * @return 要转换到的解析后的类型
     * @throws ClassNotFoundException 如果类型无法解析
     */
    @Nullable
    public Class<?> resolveTargetType(@Nullable ClassLoader classLoader) throws ClassNotFoundException {
        String typeName = getTargetTypeName();
        if (typeName == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(typeName, classLoader);
        this.targetType = resolvedClass;
        return resolvedClass;
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

    /**
     * 如果有任何，将类型名称设置为实际指定给此特定值的类型名称。
     */
    public void setSpecifiedTypeName(@Nullable String specifiedTypeName) {
        this.specifiedTypeName = specifiedTypeName;
    }

    /**
     * 如果有，则返回为这个特定值实际指定的类型名称。
     */
    @Nullable
    public String getSpecifiedTypeName() {
        return this.specifiedTypeName;
    }

    /**
     * 将此值标记为动态的，即包含一个表达式
     * 因此不适用于缓存。
     */
    public void setDynamic() {
        this.dynamic = true;
    }

    /**
     * 返回此值是否已被标记为动态。
     */
    public boolean isDynamic() {
        return this.dynamic;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof TypedStringValue that && ObjectUtils.nullSafeEquals(this.value, that.value) && ObjectUtils.nullSafeEquals(this.targetType, that.targetType)));
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.value) * 29 + ObjectUtils.nullSafeHashCode(this.targetType);
    }

    @Override
    public String toString() {
        return "TypedStringValue: value [" + this.value + "], target type [" + this.targetType + "]";
    }
}
