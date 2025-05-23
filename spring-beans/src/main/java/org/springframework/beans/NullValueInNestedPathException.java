// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0 版本（“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性还是对特定目的的适用性。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans;

/**
 * 当在有效的嵌套属性路径导航中遇到NullPointerException时抛出异常。
 *
 * <p>例如，导航"spouse.age"可能会失败，因为目标对象的spouse属性可能为null。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class NullValueInNestedPathException extends InvalidPropertyException {

    /**
     * 创建一个新的 NullValueInNestedPathException。
     * @param beanClass 违规的 bean 类
     * @param propertyName 违规的属性名称
     */
    public NullValueInNestedPathException(Class<?> beanClass, String propertyName) {
        super(beanClass, propertyName, "Value of nested property '" + propertyName + "' is null");
    }

    /**
     * 创建一个新的 NullValueInNestedPathException。
     * @param beanClass 违规的 Bean 类
     * @param propertyName 违规的属性名
     * @param msg 详细消息
     */
    public NullValueInNestedPathException(Class<?> beanClass, String propertyName, String msg) {
        super(beanClass, propertyName, msg);
    }

    /**
     * 创建一个新的 NullValueInNestedPathException。
     * @param beanClass 导致问题的 Bean 类
     * @param propertyName 导致问题的属性
     * @param msg 详细消息
     * @param cause 根本原因
     * @since 4.3.2
     */
    public NullValueInNestedPathException(Class<?> beanClass, String propertyName, String msg, Throwable cause) {
        super(beanClass, propertyName, msg, cause);
    }
}
