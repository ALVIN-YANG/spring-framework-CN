// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可”）进行许可；
* 您必须遵守该许可才能使用此文件。
* 您可以在以下链接处获取许可副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据该许可分发的软件
* 是在“现状”基础上分发的，不提供任何形式的明示或暗示保证，
* 包括但不限于适销性、适用于特定目的和不侵权。
* 请参阅许可了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.factory.NamedBean;
import org.springframework.util.Assert;

/**
 * 用于存储给定bean名称及其实例的简单容器。
 *
 * @author Juergen Hoeller
 * @since 4.3.3
 * @param <T> bean的类型
 * @see AutowireCapableBeanFactory#resolveNamedBean(Class)
 */
public class NamedBeanHolder<T> implements NamedBean {

    private final String beanName;

    private final T beanInstance;

    /**
     * 为给定的bean名称及其实例创建一个新的持有者。
     * @param beanName bean的名称
     * @param beanInstance 相应的bean实例
     */
    public NamedBeanHolder(String beanName, T beanInstance) {
        Assert.notNull(beanName, "Bean name must not be null");
        this.beanName = beanName;
        this.beanInstance = beanInstance;
    }

    /**
     * 返回该Bean的名称。
     */
    @Override
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * 返回相应的 Bean 实例。
     */
    public T getBeanInstance() {
        return this.beanInstance;
    }
}
