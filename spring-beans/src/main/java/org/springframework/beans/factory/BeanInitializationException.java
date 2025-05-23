// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0 版本（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory;

import org.springframework.beans.FatalBeanException;

/**
 * 当一个Bean实现自己的工厂感知初始化代码失败时，建议抛出的异常。Bean工厂方法本身抛出的BeansExceptions应直接传播。
 *
 * <p>注意，`afterPropertiesSet()`或自定义的"init-method"可以抛出任何异常。
 *
 * @author Juergen Hoeller
 * @since 13.11.2003
 * @see BeanFactoryAware#setBeanFactory
 * @see InitializingBean#afterPropertiesSet
 */
@SuppressWarnings("serial")
public class BeanInitializationException extends FatalBeanException {

    /**
     * 创建一个新的 BeanInitializationException，并使用指定的消息。
     * @param msg 详细消息
     */
    public BeanInitializationException(String msg) {
        super(msg);
    }

    /**
     * 创建一个新的BeanInitializationException，指定消息和根本原因。
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public BeanInitializationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
