// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.wiring;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于存储特定类的bean绑定元数据信息的持有者。与`@link org.springframework.beans.factory.annotation.Configurable`注解和AspectJ的`AnnotationBeanConfigurerAspect`一起使用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeanWiringInfoResolver
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory
 * @see org.springframework.beans.factory.annotation.Configurable
 */
public class BeanWiringInfo {

    /**
     * 常量，表示通过名称自动装配 Bean 属性。
     * @see #BeanWiringInfo(int, boolean)
     * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#AUTOWIRE_BY_NAME
     */
    public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    /**
     * 表示按类型自动装配 bean 属性的常量。
     * @see #BeanWiringInfo(int, boolean)
     * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#AUTOWIRE_BY_TYPE
     */
    public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

    @Nullable
    private String beanName;

    private boolean isDefaultBeanName = false;

    private int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_NO;

    private boolean dependencyCheck = false;

    /**
     * 创建一个默认的BeanWiringInfo，该默认值建议使用平面初始化来设置工厂和后处理器回调，这些回调可能是bean类所期望的。
     */
    public BeanWiringInfo() {
    }

    /**
     * 创建一个新的 BeanWiringInfo 对象，该对象指向给定的 bean 名称。
     * @param beanName 要从中获取属性值的 bean 定义名称
     * @throws IllegalArgumentException 如果提供的 beanName 为 null、为空，或者完全由空白字符组成
     */
    public BeanWiringInfo(String beanName) {
        this(beanName, false);
    }

    /**
     * 创建一个新的BeanWiringInfo，指向给定的bean名称。
     * @param beanName 要从中获取属性值的bean定义的名称
     * @param isDefaultBeanName 给定的bean名称是否是一个建议的默认bean名称，不一定匹配实际的bean定义
     * @throws IllegalArgumentException 如果提供的beanName为null、为空或完全由空白字符组成
     */
    public BeanWiringInfo(String beanName, boolean isDefaultBeanName) {
        Assert.hasText(beanName, "'beanName' must not be empty");
        this.beanName = beanName;
        this.isDefaultBeanName = isDefaultBeanName;
    }

    /**
     * 创建一个新的BeanWiringInfo对象，表示自动装配。
     * @param autowireMode 自动装配模式，可以是以下常量之一：{@link #AUTOWIRE_BY_NAME} 或 {@link #AUTOWIRE_BY_TYPE}
     * @param dependencyCheck 是否对bean实例中的对象引用执行依赖检查（自动装配之后）
     * @throws IllegalArgumentException 如果提供的{@code autowireMode}不是允许的值之一
     * @see #AUTOWIRE_BY_NAME
     * @see #AUTOWIRE_BY_TYPE
     */
    public BeanWiringInfo(int autowireMode, boolean dependencyCheck) {
        if (autowireMode != AUTOWIRE_BY_NAME && autowireMode != AUTOWIRE_BY_TYPE) {
            throw new IllegalArgumentException("Only constants AUTOWIRE_BY_NAME and AUTOWIRE_BY_TYPE supported");
        }
        this.autowireMode = autowireMode;
        this.dependencyCheck = dependencyCheck;
    }

    /**
     * 返回此 BeanWiringInfo 是否指示自动装配。
     */
    public boolean indicatesAutowiring() {
        return (this.beanName == null);
    }

    /**
     * 返回此BeanWiringInfo指向的具体Bean名称，如果有。
     */
    @Nullable
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * 返回特定bean名称是否为建议的默认bean名称，
     * 不一定匹配工厂中的实际bean定义。
     */
    public boolean isDefaultBeanName() {
        return this.isDefaultBeanName;
    }

    /**
     * 如果指示了自动装配，则返回其中一个常量 {@link #AUTOWIRE_BY_NAME} / {@link #AUTOWIRE_BY_TYPE}。
     */
    public int getAutowireMode() {
        return this.autowireMode;
    }

    /**
     * 返回是否在Bean实例（自动装配后）中执行对象引用的依赖检查
     */
    public boolean getDependencyCheck() {
        return this.dependencyCheck;
    }
}
