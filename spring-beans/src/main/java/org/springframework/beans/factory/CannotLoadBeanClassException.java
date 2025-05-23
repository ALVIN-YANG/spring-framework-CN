// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory;

import org.springframework.beans.FatalBeanException;
import org.springframework.lang.Nullable;

/**
 * 当BeanFactory无法加载给定bean指定的类时抛出异常。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class CannotLoadBeanClassException extends FatalBeanException {

    @Nullable
    private final String resourceDescription;

    private final String beanName;

    @Nullable
    private final String beanClassName;

    /**
     * 创建一个新的 CannotLoadBeanClassException。
     * @param resourceDescription 描述了从该资源中获取的bean定义
     * @param beanName 请求的bean的名称
     * @param beanClassName bean类的名称
     * @param cause 根本原因
     */
    public CannotLoadBeanClassException(@Nullable String resourceDescription, String beanName, @Nullable String beanClassName, ClassNotFoundException cause) {
        super("Cannot find class [" + beanClassName + "] for bean with name '" + beanName + "'" + (resourceDescription != null ? " defined in " + resourceDescription : ""), cause);
        this.resourceDescription = resourceDescription;
        this.beanName = beanName;
        this.beanClassName = beanClassName;
    }

    /**
     * 创建一个新的 CannotLoadBeanClassException。
     * @param resourceDescription 资源描述，该描述来自包含 bean 定义的资源
     * @param beanName 请求的 bean 名称
     * @param beanClassName bean 类的名称
     * @param cause 根本原因
     */
    public CannotLoadBeanClassException(@Nullable String resourceDescription, String beanName, @Nullable String beanClassName, LinkageError cause) {
        super("Error loading class [" + beanClassName + "] for bean with name '" + beanName + "'" + (resourceDescription != null ? " defined in " + resourceDescription : "") + ": problem with class file or dependent class", cause);
        this.resourceDescription = resourceDescription;
        this.beanName = beanName;
        this.beanClassName = beanClassName;
    }

    /**
     * 返回该Bean定义所来源的资源描述。
     */
    @Nullable
    public String getResourceDescription() {
        return this.resourceDescription;
    }

    /**
     * 返回请求的 bean 名称。
     */
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * 返回我们尝试加载的类的名称。
     */
    @Nullable
    public String getBeanClassName() {
        return this.beanClassName;
    }
}
