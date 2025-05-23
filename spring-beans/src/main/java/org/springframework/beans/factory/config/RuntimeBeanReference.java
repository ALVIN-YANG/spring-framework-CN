// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”），您可能不得使用此文件，除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵权。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.config;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 不可变占位符类，用于在工厂中引用其他bean时作为属性值对象，将在运行时解析。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.BeanFactory#getBean(String)
 * @see org.springframework.beans.factory.BeanFactory#getBean(Class)
 */
public class RuntimeBeanReference implements BeanReference {

    private final String beanName;

    @Nullable
    private final Class<?> beanType;

    private final boolean toParent;

    @Nullable
    private Object source;

    /**
     * 创建一个新的 RuntimeBeanReference 到指定的 bean 名称。
     * @param beanName 目标 bean 的名称
     */
    public RuntimeBeanReference(String beanName) {
        this(beanName, false);
    }

    /**
     * 创建一个新的 RuntimeBeanReference，指向给定的bean名称，
     * 可选地将其标记为指向父工厂中的bean。
     * @param beanName 目标bean的名称
     * @param toParent 这是否是显式指向父工厂中bean的引用
     */
    public RuntimeBeanReference(String beanName, boolean toParent) {
        Assert.hasText(beanName, "'beanName' must not be empty");
        this.beanName = beanName;
        this.beanType = null;
        this.toParent = toParent;
    }

    /**
     * 创建一个新的 RuntimeBeanReference，指向给定类型的bean。
     * @param beanType 目标bean的类型
     * @since 5.2
     */
    public RuntimeBeanReference(Class<?> beanType) {
        this(beanType, false);
    }

    /**
     * 创建一个新的 RuntimeBeanReference，指向给定类型的bean，
     * 选项用于标记它是否指向父工厂中的bean。
     * @param beanType 目标bean的类型
     * @param toParent 这是否是对父工厂中bean的显式引用
     * @since 5.2
     */
    public RuntimeBeanReference(Class<?> beanType, boolean toParent) {
        Assert.notNull(beanType, "'beanType' must not be null");
        this.beanName = beanType.getName();
        this.beanType = beanType;
        this.toParent = toParent;
    }

    /**
     * 返回请求的 Bean 名称，或者在按类型解析的情况下返回完全限定的类型名称。
     * @see #getBeanType()
     */
    @Override
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * 如果要求通过类型解析，则返回请求的 bean 类型。
     * @since 5.2
     */
    @Nullable
    public Class<?> getBeanType() {
        return this.beanType;
    }

    /**
     * 返回这是否是对父工厂中一个bean的显式引用。
     */
    public boolean isToParent() {
        return this.toParent;
    }

    /**
     * 为此元数据元素设置配置源对象。
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
        return (this == other || (other instanceof RuntimeBeanReference that && this.beanName.equals(that.beanName) && this.beanType == that.beanType && this.toParent == that.toParent));
    }

    @Override
    public int hashCode() {
        int result = this.beanName.hashCode();
        result = 29 * result + (this.toParent ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return '<' + getBeanName() + '>';
    }
}
