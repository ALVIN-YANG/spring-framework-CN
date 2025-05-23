// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者们。
*
* 根据 Apache License 2.0 版本（以下简称“许可证”）授权；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式（无论是明示的还是暗示的）保证或条件。
* 请参阅许可证，了解具体的管理权限和限制。*/
package org.springframework.beans.factory;

import org.springframework.beans.FatalBeanException;

/**
 * 表示表达式评估尝试失败的异常。
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
@SuppressWarnings("serial")
public class BeanExpressionException extends FatalBeanException {

    /**
     * 创建一个新的BeanExpressionException，带有指定的消息。
     * @param msg 详细消息
     */
    public BeanExpressionException(String msg) {
        super(msg);
    }

    /**
     * 创建一个新的 BeanExpressionException，带有指定的消息和根本原因。
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public BeanExpressionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
