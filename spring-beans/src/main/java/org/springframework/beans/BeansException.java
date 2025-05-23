// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans;

import org.springframework.core.NestedRuntimeException;
import org.springframework.lang.Nullable;

/**
 * 所有在beans包及其子包中抛出的异常的抽象超类。
 *
 * <p>请注意，这是一个运行时（非受检）异常。Beans异常通常具有致命性；没有理由对它们进行检查。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public abstract class BeansException extends NestedRuntimeException {

    /**
     * 创建一个新的 BeansException，带有指定的消息。
     * @param msg 详细消息
     */
    public BeansException(String msg) {
        super(msg);
    }

    /**
     * 创建一个新的 BeansException，带有指定的消息和根本原因。
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public BeansException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
