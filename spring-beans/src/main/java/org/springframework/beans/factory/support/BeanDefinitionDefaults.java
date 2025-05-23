// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接处获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是“按原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.support;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 一个用于存储 {@code BeanDefinition} 属性默认值的简单容器。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see AbstractBeanDefinition#applyDefaults
 */
public class BeanDefinitionDefaults {

    @Nullable
    private Boolean lazyInit;

    private int autowireMode = AbstractBeanDefinition.AUTOWIRE_NO;

    private int dependencyCheck = AbstractBeanDefinition.DEPENDENCY_CHECK_NONE;

    @Nullable
    private String initMethodName;

    @Nullable
    private String destroyMethodName;

    /**
     * 设置是否默认使用懒加载初始化 Bean。
     * <p>如果设置为 {@code false}，则 Bean 将在启动时由执行单例 eager 初始化的 Bean 工厂进行实例化。
     * @see AbstractBeanDefinition#setLazyInit
     */
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    /**
     * 返回是否默认按懒加载方式初始化 beans，即不在启动时立即实例化。仅适用于单例 beans。
     * @return 是否应用懒加载语义（默认为 {@code false}）
     */
    public boolean isLazyInit() {
        return (this.lazyInit != null && this.lazyInit.booleanValue());
    }

    /**
     * 返回是否默认采用懒加载初始化豆类，即不在启动时立即实例化。仅适用于单例豆类。
     * @return 如果显式设置，则返回懒加载标志，否则返回 {@code null}
     * @since 5.2
     */
    @Nullable
    public Boolean getLazyInit() {
        return this.lazyInit;
    }

    /**
     * 设置自动装配模式。这决定了是否会发生任何自动检测和设置Bean引用。默认为AUTOWIRE_NO，这意味着不会通过约定进行基于名称或类型的自动装配（然而，仍然可能存在显式的基于注解的自动装配）。
     * @param autowireMode 要设置的自动装配模式。
     * 必须是定义在{@link AbstractBeanDefinition}中的常量之一。
     * @see AbstractBeanDefinition#setAutowireMode
     */
    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    /**
     * 返回默认的自动装配模式。
     */
    public int getAutowireMode() {
        return this.autowireMode;
    }

    /**
     * 设置依赖检查代码。
     * @param dependencyCheck 要设置的代码。
     * 必须是定义在 {@link AbstractBeanDefinition} 中的常量之一。
     * @see AbstractBeanDefinition#setDependencyCheck
     */
    public void setDependencyCheck(int dependencyCheck) {
        this.dependencyCheck = dependencyCheck;
    }

    /**
     * 返回默认的依赖检查代码。
     */
    public int getDependencyCheck() {
        return this.dependencyCheck;
    }

    /**
     * 设置默认初始化方法的名称。
     * <p>请注意，此方法并非对所有受影响的Bean定义强制执行，而是一个可选的回调，只有在实际存在时才会被调用。
     * @see AbstractBeanDefinition#setInitMethodName
     * @see AbstractBeanDefinition#setEnforceInitMethod
     */
    public void setInitMethodName(@Nullable String initMethodName) {
        this.initMethodName = (StringUtils.hasText(initMethodName) ? initMethodName : null);
    }

    /**
     * 返回默认初始化方法的名称。
     */
    @Nullable
    public String getInitMethodName() {
        return this.initMethodName;
    }

    /**
     * 设置默认销毁方法的名称。
     * <p>请注意，此方法并非对所有受影响的bean定义强制执行，而是一个可选的回调，当实际存在时将被调用。
     * @see AbstractBeanDefinition#setDestroyMethodName
     * @see AbstractBeanDefinition#setEnforceDestroyMethod
     */
    public void setDestroyMethodName(@Nullable String destroyMethodName) {
        this.destroyMethodName = (StringUtils.hasText(destroyMethodName) ? destroyMethodName : null);
    }

    /**
     * 返回默认销毁方法的名称。
     */
    @Nullable
    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }
}
