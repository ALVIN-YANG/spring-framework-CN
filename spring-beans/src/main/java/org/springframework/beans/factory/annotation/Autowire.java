// 翻译完成 glm-4-flash
/** 版权所有 2002-2009 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * 用于确定自动装配状态的枚举：即是否应该由 Spring 容器通过setter注入自动注入bean的依赖项。这是 Spring DI 的核心概念。
 *
 * <p>可用于基于注解的配置，例如 AspectJ AnnotationBeanConfigurer aspect。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.annotation.Configurable
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory
 */
public enum Autowire {

    /**
     * 表示完全不进行自动装配的常量。
     */
    NO(AutowireCapableBeanFactory.AUTOWIRE_NO),
    /**
     * 表示通过名称自动装配 Bean 属性的常量。
     */
    BY_NAME(AutowireCapableBeanFactory.AUTOWIRE_BY_NAME),
    /**
     * 表示通过类型自动装配bean属性的常量。
     */
    BY_TYPE(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);

    private final int value;

    Autowire(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    /**
     * 返回此是否代表实际的自动装配值。
     * @return 是否指定了实际的自动装配（无论是BY_NAME还是BY_TYPE）
     */
    public boolean isAutowire() {
        return (this == BY_NAME || this == BY_TYPE);
    }
}
