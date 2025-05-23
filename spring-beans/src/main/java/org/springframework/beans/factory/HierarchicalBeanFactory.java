// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途的适用性或不侵犯第三方权利。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory;

import org.springframework.lang.Nullable;

/**
 * 由 Bean 工厂实现，可以成为层次结构一部分的子接口。
 *
 * <p>对于允许以可配置方式设置父工厂的 Bean 工厂对应的 {@code setParentBeanFactory} 方法，可以在 ConfigurableBeanFactory 接口中找到。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 07.07.2003
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setParentBeanFactory
 */
public interface HierarchicalBeanFactory extends BeanFactory {

    /**
     * 返回父级bean工厂，如果没有则返回{@code null}。
     */
    @Nullable
    BeanFactory getParentBeanFactory();

    /**
     * 返回本地bean工厂是否包含给定名称的bean，忽略在祖先上下文中定义的bean。
     * <p>这是对{@code containsBean}的一个替代，忽略来自祖先bean工厂的给定名称的bean。
     * @param name 要查询的bean的名称
     * @return 是否在本地工厂中定义了具有给定名称的bean
     * @see BeanFactory#containsBean
     */
    boolean containsLocalBean(String name);
}
