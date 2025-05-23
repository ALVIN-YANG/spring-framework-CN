// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非根据法律规定或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下网址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

import org.springframework.core.NestedRuntimeException;

/**
 * 在非法AOP配置参数上抛出的异常。
 *
 * @author Rod Johnson
 * @since 2003年3月13日
 */
@SuppressWarnings("serial")
public class AopConfigException extends NestedRuntimeException {

    /**
     * AopConfigException 构造函数。
     * @param msg 详细消息
     */
    public AopConfigException(String msg) {
        super(msg);
    }

    /**
     * AopConfigException 构造函数。
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public AopConfigException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
