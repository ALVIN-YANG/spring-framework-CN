// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体规定许可范围和限制。*/
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.util.ClassUtils;

/**
 * 抛出当某个Bean不符合预期类型时。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class BeanNotOfRequiredTypeException extends BeansException {

    /**
     * 错误的实例名称。
     */
    private final String beanName;

    /**
     * 所需类型。
     */
    private final Class<?> requiredType;

    /**
     * 引发问题的类型。
     */
    private final Class<?> actualType;

    /**
     * 创建一个新的 BeanNotOfRequiredTypeException 对象。
     * @param beanName 请求的 Bean 名称
     * @param requiredType 所需的类型
     * @param actualType 实际返回的类型，该类型与期望的类型不匹配
     */
    public BeanNotOfRequiredTypeException(String beanName, Class<?> requiredType, Class<?> actualType) {
        super("Bean named '" + beanName + "' is expected to be of type '" + ClassUtils.getQualifiedName(requiredType) + "' but was actually of type '" + ClassUtils.getQualifiedName(actualType) + "'");
        this.beanName = beanName;
        this.requiredType = requiredType;
        this.actualType = actualType;
    }

    /**
     * 返回类型错误的实例的名称。
     */
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * 返回该 Bean 的预期类型。
     */
    public Class<?> getRequiredType() {
        return this.requiredType;
    }

    /**
     * 返回找到的实例的实际类型。
     */
    public Class<?> getActualType() {
        return this.actualType;
    }
}
