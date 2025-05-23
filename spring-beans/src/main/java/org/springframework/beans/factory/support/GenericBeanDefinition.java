// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性还是其特定用途。
* 请参阅许可证了解具体管理权限和限制的语言。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 *  GenericBeanDefinition 是用于声明性bean定义的万能解决方案。
 *  就像所有常见的bean定义一样，它允许指定一个类，并且可以选择指定构造函数参数值和属性值。此外，通过 "parentName" 属性，可以从父bean定义灵活地配置继承关系。
 *
 * <p>通常，使用这个 {@code GenericBeanDefinition} 类来注册声明性bean定义（例如XML定义，bean后处理器可能对其进行操作，甚至可能重新配置父名称）。当父/子关系预先确定时，使用 {@code RootBeanDefinition}/{@code ChildBeanDefinition}，并特别推荐在从工厂方法/提供者派生程序定义时使用 {@link RootBeanDefinition}。
 *
 *  @author Juergen Hoeller
 *  @since 2.5
 *  @see #setParentName
 *  @see RootBeanDefinition
 *  @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public class GenericBeanDefinition extends AbstractBeanDefinition {

    @Nullable
    private String parentName;

    /**
     * 创建一个新的 GenericBeanDefinition，通过其bean属性和配置方法进行配置。
     * @see #setBeanClass
     * @see #setScope
     * @see #setConstructorArgumentValues
     * @see #setPropertyValues
     */
    public GenericBeanDefinition() {
        super();
    }

    /**
     * 创建一个新的 GenericBeanDefinition，作为给定 bean 定义的深拷贝。
     * @param original 从中复制的原始 bean 定义
     */
    public GenericBeanDefinition(BeanDefinition original) {
        super(original);
    }

    @Override
    public void setParentName(@Nullable String parentName) {
        this.parentName = parentName;
    }

    @Override
    @Nullable
    public String getParentName() {
        return this.parentName;
    }

    @Override
    public AbstractBeanDefinition cloneBeanDefinition() {
        return new GenericBeanDefinition(this);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof GenericBeanDefinition that && ObjectUtils.nullSafeEquals(this.parentName, that.parentName) && super.equals(other)));
    }

    @Override
    public String toString() {
        if (this.parentName != null) {
            return "Generic bean with parent '" + this.parentName + "': " + super.toString();
        }
        return "Generic bean: " + super.toString();
    }
}
