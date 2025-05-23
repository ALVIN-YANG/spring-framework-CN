// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans;

import org.springframework.lang.Nullable;

/**
 * 在beans包或子包中遇到无法恢复的问题时抛出，例如类或字段错误。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public class FatalBeanException extends BeansException {

    /**
     * 创建一个新的 FatalBeanException，带有指定的消息。
     * @param msg 详细消息
     */
    public FatalBeanException(String msg) {
        super(msg);
    }

    /**
     * 创建一个新的FatalBeanException实例，带有指定的消息和根本原因。
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public FatalBeanException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
