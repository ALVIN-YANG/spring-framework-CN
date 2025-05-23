// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件，除非遵守许可证。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权的具体语言和限制，请参阅许可证。*/
package org.springframework.beans.factory;

/**
 * 抛出异常，当引用一个当前正在创建的 Bean 时发生。
 * 通常发生在构造函数自动装配与当前正在构建的 Bean 匹配时。
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
@SuppressWarnings("serial")
public class BeanCurrentlyInCreationException extends BeanCreationException {

    /**
     * 创建一个新的 BeanCurrentlyInCreationException，
     * 使用默认的错误消息，指示存在循环引用。
     * @param beanName 请求的bean名称
     */
    public BeanCurrentlyInCreationException(String beanName) {
        super(beanName, "Requested bean is currently in creation: Is there an unresolvable circular reference?");
    }

    /**
     * 创建一个新的 BeanCurrentlyInCreationException。
     * @param beanName 请求的bean名称
     * @param msg 详细消息
     */
    public BeanCurrentlyInCreationException(String beanName, String msg) {
        super(beanName, msg);
    }
}
