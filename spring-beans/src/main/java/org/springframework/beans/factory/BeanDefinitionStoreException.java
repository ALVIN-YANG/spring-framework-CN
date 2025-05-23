// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“许可证”），您可能不得使用此文件，除非遵守许可证规定。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可权和限制。*/
package org.springframework.beans.factory;

import org.springframework.beans.FatalBeanException;
import org.springframework.lang.Nullable;

/**
 * 当BeanFactory遇到无效的bean定义时抛出异常：
 * 例如，当bean元数据不完整或相互矛盾时。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 */
@SuppressWarnings("serial")
public class BeanDefinitionStoreException extends FatalBeanException {

    @Nullable
    private final String resourceDescription;

    @Nullable
    private final String beanName;

    /**
     * 创建一个新的 BeanDefinitionStoreException 对象。
     * @param msg 详细消息（直接用作异常消息）
     */
    public BeanDefinitionStoreException(String msg) {
        super(msg);
        this.resourceDescription = null;
        this.beanName = null;
    }

    /**
     * 创建一个新的 BeanDefinitionStoreException 对象。
     * @param msg 详细消息（直接用作异常消息）
     * @param cause 根本原因（可能为 {@code null}）
     */
    public BeanDefinitionStoreException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
        this.resourceDescription = null;
        this.beanName = null;
    }

    /**
     * 创建一个新的 BeanDefinitionStoreException。
     * @param resourceDescription 描述了 bean 定义来源的资源
     * @param msg 详细消息（作为异常消息直接使用）
     */
    public BeanDefinitionStoreException(@Nullable String resourceDescription, String msg) {
        super(msg);
        this.resourceDescription = resourceDescription;
        this.beanName = null;
    }

    /**
     * 创建一个新的BeanDefinitionStoreException异常。
     * @param resourceDescription 描述了bean定义来源的资源
     * @param msg 详细消息（作为异常消息直接使用）
     * @param cause 根本原因（可能为null）
     */
    public BeanDefinitionStoreException(@Nullable String resourceDescription, String msg, @Nullable Throwable cause) {
        super(msg, cause);
        this.resourceDescription = resourceDescription;
        this.beanName = null;
    }

    /**
     * 创建一个新的 BeanDefinitionStoreException 对象。
     * @param resourceDescription 描述了 bean 定义来源的资源
     * @param beanName bean 的名称
     * @param msg 详细消息（附加到一个 introductory message，该消息指示了资源以及 bean 的名称）
     */
    public BeanDefinitionStoreException(@Nullable String resourceDescription, String beanName, String msg) {
        this(resourceDescription, beanName, msg, null);
    }

    /**
     * 创建一个新的 BeanDefinitionStoreException。
     * @param resourceDescription 描述了 bean 定义来源的资源
     * @param beanName bean 的名称
     * @param msg 详细消息（附加到指示资源以及 bean 名称的 introductory 消息中）
     * @param cause 根本原因（可能为 null）
     */
    public BeanDefinitionStoreException(@Nullable String resourceDescription, String beanName, String msg, @Nullable Throwable cause) {
        super("Invalid bean definition with name '" + beanName + "' defined in " + resourceDescription + ": " + msg, cause);
        this.resourceDescription = resourceDescription;
        this.beanName = beanName;
    }

    /**
     * 如果可用，返回包含该bean定义的资源描述。
     */
    @Nullable
    public String getResourceDescription() {
        return this.resourceDescription;
    }

    /**
     * 如果有可用，则返回 Bean 的名称。
     */
    @Nullable
    public String getBeanName() {
        return this.beanName;
    }
}
