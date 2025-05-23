// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.lang.NonNull;

/**
 * 是一个表示无效覆盖尝试的 {@link BeanDefinitionStoreException} 子类：通常在调用者尝试在 {@link DefaultListableBeanFactory#isAllowBeanDefinitionOverriding()} 设置为 {@code false} 时，为同一个 Bean 名称注册一个新的定义。
 *
 * @author Juergen Hoeller
 * @since 5.1
 * @see DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
 * @see DefaultListableBeanFactory#registerBeanDefinition
 */
@SuppressWarnings("serial")
public class BeanDefinitionOverrideException extends BeanDefinitionStoreException {

    private final BeanDefinition beanDefinition;

    private final BeanDefinition existingDefinition;

    /**
     * 为给定的新定义和现有定义创建一个新的BeanDefinitionOverrideException。
     * @param beanName bean的名称
     * @param beanDefinition 新注册的bean定义
     * @param existingDefinition 同名称的现有bean定义
     */
    public BeanDefinitionOverrideException(String beanName, BeanDefinition beanDefinition, BeanDefinition existingDefinition) {
        super(beanDefinition.getResourceDescription(), beanName, "Cannot register bean definition [" + beanDefinition + "] for bean '" + beanName + "' since there is already [" + existingDefinition + "] bound.");
        this.beanDefinition = beanDefinition;
        this.existingDefinition = existingDefinition;
    }

    /**
     * 返回bean定义所来源的资源描述。
     */
    @Override
    @NonNull
    public String getResourceDescription() {
        return String.valueOf(super.getResourceDescription());
    }

    /**
     * 返回 bean 的名称。
     */
    @Override
    @NonNull
    public String getBeanName() {
        return String.valueOf(super.getBeanName());
    }

    /**
     * 返回新注册的Bean定义。
     * @see #getBeanName()
     */
    public BeanDefinition getBeanDefinition() {
        return this.beanDefinition;
    }

    /**
     * 返回具有相同名称的现有Bean定义。
     * @see #getBeanName()
     */
    public BeanDefinition getExistingDefinition() {
        return this.existingDefinition;
    }
}
