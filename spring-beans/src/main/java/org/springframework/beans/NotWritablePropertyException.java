// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans;

import org.springframework.lang.Nullable;

/**
 * 在尝试设置一个不可写属性（通常因为没有setter方法）的值时抛出异常。
 *
 * @author Rod Johnson
 * @author Alef Arendsen
 * @author Arjen Poutsma
 */
@SuppressWarnings("serial")
public class NotWritablePropertyException extends InvalidPropertyException {

    @Nullable
    private final String[] possibleMatches;

    /**
     * 创建一个新的 NotWritablePropertyException。
     * @param beanClass 出错的 Bean 类
     * @param propertyName 出错的属性名称
     */
    public NotWritablePropertyException(Class<?> beanClass, String propertyName) {
        super(beanClass, propertyName, "Bean property '" + propertyName + "' is not writable or has an invalid setter method: " + "Does the return type of the getter match the parameter type of the setter?");
        this.possibleMatches = null;
    }

    /**
     * 创建一个新的 NotWritablePropertyException。
     * @param beanClass 违规的 Bean 类
     * @param propertyName 违规的属性名称
     * @param msg 详细消息
     */
    public NotWritablePropertyException(Class<?> beanClass, String propertyName, String msg) {
        super(beanClass, propertyName, msg);
        this.possibleMatches = null;
    }

    /**
     * 创建一个新的 NotWritablePropertyException。
     * @param beanClass 出错的 bean 类
     * @param propertyName 出错的属性名
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public NotWritablePropertyException(Class<?> beanClass, String propertyName, String msg, Throwable cause) {
        super(beanClass, propertyName, msg, cause);
        this.possibleMatches = null;
    }

    /**
     * 创建一个新的 NotWritablePropertyException。
     * @param beanClass 违规的 bean 类
     * @param propertyName 违规的属性名称
     * @param msg 详细消息
     * @param possibleMatches 对于实际 bean 属性名称的建议，这些名称与无效属性名称非常相似
     */
    public NotWritablePropertyException(Class<?> beanClass, String propertyName, String msg, String[] possibleMatches) {
        super(beanClass, propertyName, msg);
        this.possibleMatches = possibleMatches;
    }

    /**
     * 如果有任何，返回与无效属性名称紧密匹配的实际 bean 属性名称的建议。
     */
    @Nullable
    public String[] getPossibleMatches() {
        return this.possibleMatches;
    }
}
