// 翻译完成 glm-4-flash
/*版权所有 2002-2012，原作者或原作者们。

根据Apache License，版本2.0（以下简称“许可证”）许可；除非符合许可证规定，否则不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性还是其特定用途的适用性。
请参阅许可证，了解具体规定许可权和限制的条款。*/
package org.springframework.aop;

import org.springframework.core.NestedRuntimeException;

/**
 * 当AOP调用因配置错误或意外运行时问题而失败时抛出的异常。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AopInvocationException extends NestedRuntimeException {

    /**
     * AopInvocationException 构造函数。
     * @param msg 详细消息
     */
    public AopInvocationException(String msg) {
        super(msg);
    }

    /**
     * AopInvocationException 构造函数。
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public AopInvocationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
