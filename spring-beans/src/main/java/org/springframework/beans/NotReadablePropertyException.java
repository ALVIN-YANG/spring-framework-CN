// 翻译完成 glm-4-flash
/** 版权所有 2002-2014 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 进行许可；
* 除非符合许可协议，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议，了解具体的权限和限制。*/
package org.springframework.beans;

/**
 * 在尝试获取一个不可读属性的值时抛出异常，因为没有getter方法。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 */
@SuppressWarnings("serial")
public class NotReadablePropertyException extends InvalidPropertyException {

    /**
     * 创建一个新的 NotReadablePropertyException。
     * @param beanClass 出错的 bean 类
     * @param propertyName 出错的属性名称
     */
    public NotReadablePropertyException(Class<?> beanClass, String propertyName) {
        super(beanClass, propertyName, "Bean property '" + propertyName + "' is not readable or has an invalid getter method: " + "Does the return type of the getter match the parameter type of the setter?");
    }

    /**
     * 创建一个新的 NotReadablePropertyException。
     * @param beanClass 违规的 Bean 类
     * @param propertyName 违规的属性名
     * @param msg 详细消息
     */
    public NotReadablePropertyException(Class<?> beanClass, String propertyName, String msg) {
        super(beanClass, propertyName, msg);
    }

    /**
     * 创建一个新的 NotReadablePropertyException。
     * @param beanClass 出问题的 Bean 类
     * @param propertyName 出问题的属性
     * @param msg 详细消息
     * @param cause 根本原因
     * @since 4.0.9
     */
    public NotReadablePropertyException(Class<?> beanClass, String propertyName, String msg, Throwable cause) {
        super(beanClass, propertyName, msg, cause);
    }
}
