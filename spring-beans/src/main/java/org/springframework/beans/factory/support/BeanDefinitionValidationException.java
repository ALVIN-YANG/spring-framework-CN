// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License, Version 2.0（以下简称“许可证”）授权；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”提供的，不提供任何形式的质量保证或适用性保证；
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.FatalBeanException;

/**
 * 当Bean定义的验证失败时抛出的异常。
 *
 * @author Juergen Hoeller
 * @since 21.11.2003
 * @see AbstractBeanDefinition#validate()
 */
@SuppressWarnings("serial")
public class BeanDefinitionValidationException extends FatalBeanException {

    /**
     * 创建一个新的 BeanDefinitionValidationException，带有指定的消息。
     * @param msg 详细消息
     */
    public BeanDefinitionValidationException(String msg) {
        super(msg);
    }

    /**
     * 创建一个新的 BeanDefinitionValidationException，带有指定的消息和根本原因。
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public BeanDefinitionValidationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
