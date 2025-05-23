// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans;

/**
 * 简单工厂门面，用于获取 {@link PropertyAccessor} 实例，特别是用于获取 {@link BeanWrapper} 实例。隐藏了实际的目标实现类及其扩展的公共签名。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
public final class PropertyAccessorFactory {

    private PropertyAccessorFactory() {
    }

    /**
     * 获取给定目标对象的 BeanWrapper，
     * 以 JavaBeans 风格访问属性。
     * @param target 要包装的目标对象
     * @return 属性访问器
     * @see BeanWrapperImpl
     */
    public static BeanWrapper forBeanPropertyAccess(Object target) {
        return new BeanWrapperImpl(target);
    }

    /**
     * 获取给定目标对象的属性访问器，
     * 以直接字段样式访问属性。
     * @param target 要包装的目标对象
     * @return 属性访问器
     * @see DirectFieldAccessor
     */
    public static ConfigurablePropertyAccessor forDirectFieldAccess(Object target) {
        return new DirectFieldAccessor(target);
    }
}
