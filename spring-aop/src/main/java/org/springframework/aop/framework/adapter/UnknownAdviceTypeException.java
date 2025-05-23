// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或原作者。

根据Apache License，版本2.0（以下简称“许可”）授权；
除非遵守许可，否则您不得使用此文件。
您可以在以下地址获取许可副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权或特定目的的。
有关许可的具体语言、权限和限制，请参阅许可。*/
package org.springframework.aop.framework.adapter;

/**
 * 当尝试使用不支持的Advisor或Advice类型时抛出的异常。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.aopalliance.aop.Advice
 * @see org.springframework.aop.Advisor
 */
@SuppressWarnings("serial")
public class UnknownAdviceTypeException extends IllegalArgumentException {

    /**
     * 为给定的建议对象创建一个新的UnknownAdviceTypeException。
     * 将创建一个消息文本，说明该对象既不是Advice的子接口，也不是Advisor。
     * @param advice 类型未知的建议对象
     */
    public UnknownAdviceTypeException(Object advice) {
        super("Advice object [" + advice + "] is neither a supported subinterface of " + "[org.aopalliance.aop.Advice] nor an [org.springframework.aop.Advisor]");
    }

    /**
     * 创建一个新的UnknownAdviceTypeException，并使用给定的消息。
     * @param message 消息文本
     */
    public UnknownAdviceTypeException(String message) {
        super(message);
    }
}
