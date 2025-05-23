// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans;

/**
 * 用于注册自定义
 * {@link java.beans.PropertyEditor 属性编辑器}与
 * {@link org.springframework.beans.PropertyEditorRegistry 属性编辑器注册表}的策略接口。
 *
 * <p>这在您需要在多种情况下使用相同的属性编辑器集合时特别有用：编写相应的注册器，并在每种情况下重用该注册器。
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see PropertyEditorRegistry
 * @see java.beans.PropertyEditor
 */
public interface PropertyEditorRegistrar {

    /**
     * 向给定的 {@code PropertyEditorRegistry} 注册自定义的 {@link java.beans.PropertyEditor PropertyEditors}。
     * <p>传入的注册器通常是 {@link BeanWrapper} 或一个 {@link org.springframework.validation.DataBinder DataBinder}。
     * <p>预期实现将为该方法每次调用创建全新的 {@code PropertyEditors} 实例（因为 {@code PropertyEditors} 不是线程安全的）。
     * @param registry 用于注册自定义的 {@code PropertyEditors} 的 {@code PropertyEditorRegistry}
     */
    void registerCustomEditors(PropertyEditorRegistry registry);
}
