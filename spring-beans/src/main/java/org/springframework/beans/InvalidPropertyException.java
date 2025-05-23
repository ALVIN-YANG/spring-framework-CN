// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或原作者。
*
* 根据 Apache License 2.0（“许可协议”）许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按“现状”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 有关许可协议下权限和限制的具体语言，请参阅许可协议。*/
package org.springframework.beans;

import org.springframework.lang.Nullable;

/**
 * 当引用无效的Bean属性时抛出的异常。
 * 包含违规的Bean类和属性名。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 */
@SuppressWarnings("serial")
public class InvalidPropertyException extends FatalBeanException {

    private final Class<?> beanClass;

    private final String propertyName;

    /**
     * 创建一个新的 InvalidPropertyException。
     * @param beanClass 违法的 Bean 类
     * @param propertyName 违法的属性
     * @param msg 详细消息
     */
    public InvalidPropertyException(Class<?> beanClass, String propertyName, String msg) {
        this(beanClass, propertyName, msg, null);
    }

    /**
     * 创建一个新的 InvalidPropertyException。
     * @param beanClass 出错的 Bean 类
     * @param propertyName 出错的属性
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public InvalidPropertyException(Class<?> beanClass, String propertyName, String msg, @Nullable Throwable cause) {
        super("Invalid property '" + propertyName + "' of bean class [" + beanClass.getName() + "]: " + msg, cause);
        this.beanClass = beanClass;
        this.propertyName = propertyName;
    }

    /**
     * 返回有问题的 Bean 类。
     */
    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    /**
     * 返回导致问题的属性名称。
     */
    public String getPropertyName() {
        return this.propertyName;
    }
}
