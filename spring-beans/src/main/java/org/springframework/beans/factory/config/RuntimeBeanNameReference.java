// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），您可能不得使用此文件除非遵守许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件按“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或特定用途的适用性。
* 请参阅许可证了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory.config;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 不可变占位符类，用于在工厂中引用其他bean名称的属性值对象时，在运行时进行解析。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see RuntimeBeanReference
 * @see BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.BeanFactory#getBean
 */
public class RuntimeBeanNameReference implements BeanReference {

    private final String beanName;

    @Nullable
    private Object source;

    /**
     * 创建一个新的 RuntimeBeanNameReference 对象，指向给定的 bean 名称。
     * @param beanName 目标 bean 的名称
     */
    public RuntimeBeanNameReference(String beanName) {
        Assert.hasText(beanName, "'beanName' must not be empty");
        this.beanName = beanName;
    }

    @Override
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * 为此元数据元素设置配置源 {@code Object}。
     * <p>该对象的确切类型将取决于所使用的配置机制。
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
        return (this == other || (other instanceof RuntimeBeanNameReference that && this.beanName.equals(that.beanName)));
    }

    @Override
    public int hashCode() {
        return this.beanName.hashCode();
    }

    @Override
    public String toString() {
        return '<' + getBeanName() + '>';
    }
}
