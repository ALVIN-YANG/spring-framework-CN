// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * 为继承其父类设置的 Bean 定义。
 * 子 Bean 定义对父 Bean 定义有一个固定的依赖关系。
 *
 * <p>子 Bean 定义将继承构造函数参数值、属性值和方法重写来自父类，同时可以选择添加新值。如果指定了初始化方法、销毁方法或静态工厂方法，它们将覆盖相应的父类设置。其余设置将<i>始终</i>从子定义中获取：依赖项、自动装配模式、依赖项检查、单例、懒加载。
 *
 * <p><b>注意：</b>从 Spring 2.5 开始，通过程序注册 Bean 定义的首选方法是使用 {@link GenericBeanDefinition} 类，该类允许通过 {@link GenericBeanDefinition#setParentName} 方法动态定义父级依赖关系。这有效地替代了 ChildBeanDefinition 类，对于大多数用例来说。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see GenericBeanDefinition
 * @see RootBeanDefinition
 */
@SuppressWarnings("serial")
public class ChildBeanDefinition extends AbstractBeanDefinition {

    @Nullable
    private String parentName;

    /**
     * 为给定的父类创建一个新的 ChildBeanDefinition，将通过其 Bean 属性和配置方法进行配置。
     * @param parentName 父 Bean 的名称
     * @see #setBeanClass
     * @see #setScope
     * @see #setConstructorArgumentValues
     * @see #setPropertyValues
     */
    public ChildBeanDefinition(String parentName) {
        super();
        this.parentName = parentName;
    }

    /**
     * 为给定的父bean创建一个新的ChildBeanDefinition。
     * @param parentName 父bean的名称
     * @param pvs 子bean的附加属性值
     */
    public ChildBeanDefinition(String parentName, MutablePropertyValues pvs) {
        super(null, pvs);
        this.parentName = parentName;
    }

    /**
     * 为给定的父对象创建一个新的 ChildBeanDefinition。
     * @param parentName 父bean的名称
     * @param cargs 要应用构造函数参数值
     * @param pvs 子对象的附加属性值
     */
    public ChildBeanDefinition(String parentName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        super(cargs, pvs);
        this.parentName = parentName;
    }

    /**
     * 为给定父对象创建一个新的 ChildBeanDefinition，
     * 提供构造函数参数和属性值。
     * @param parentName 父Bean的名称
     * @param beanClass 要实例化的Bean的类
     * @param cargs 要应用的构造函数参数值
     * @param pvs 要应用的属性值
     */
    public ChildBeanDefinition(String parentName, Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        super(cargs, pvs);
        this.parentName = parentName;
        setBeanClass(beanClass);
    }

    /**
     * 为给定的父对象创建一个新的 ChildBeanDefinition，
     * 提供构造函数参数和属性值。
     * 通过传递一个bean类名来避免对bean类的预先加载。
     * @param parentName 父bean的名称
     * @param beanClassName 要实例化的类名
     * @param cargs 应用构造函数参数的值
     * @param pvs 应用属性值的列表
     */
    public ChildBeanDefinition(String parentName, String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        super(cargs, pvs);
        this.parentName = parentName;
        setBeanClassName(beanClassName);
    }

    /**
     * 创建一个新的ChildBeanDefinition，作为给定bean定义的深度副本。
     * @param original 要从中复制的原始bean定义
     */
    public ChildBeanDefinition(ChildBeanDefinition original) {
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
    public void validate() throws BeanDefinitionValidationException {
        super.validate();
        if (this.parentName == null) {
            throw new BeanDefinitionValidationException("'parentName' must be set in ChildBeanDefinition");
        }
    }

    @Override
    public AbstractBeanDefinition cloneBeanDefinition() {
        return new ChildBeanDefinition(this);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof ChildBeanDefinition that && ObjectUtils.nullSafeEquals(this.parentName, that.parentName) && super.equals(other)));
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.parentName) * 29 + super.hashCode();
    }

    @Override
    public String toString() {
        return "Child bean with parent '" + this.parentName + "': " + super.toString();
    }
}
