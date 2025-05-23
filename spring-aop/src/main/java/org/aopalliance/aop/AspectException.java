// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.aopalliance.aop;

/**
 * 所有 AOP 基础设施异常的父类。
 * 未检查的，因为这些异常是致命的，并且最终用户代码不应该被强制捕获它们。
 *
 * @author Rod Johnson
 * @author Bob Lee
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class AspectException extends RuntimeException {

    /**
     * AspectException 构造函数。
     * @param message 异常信息
     */
    public AspectException(String message) {
        super(message);
    }

    /**
     * AspectException 构造函数。
     * @param message 异常信息
     * @param cause 根本原因，如果有的话
     */
    public AspectException(String message, Throwable cause) {
        super(message, cause);
    }
}
