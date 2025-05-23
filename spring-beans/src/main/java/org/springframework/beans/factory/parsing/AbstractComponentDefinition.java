// 翻译完成 glm-4-flash
/** 版权所有 2002-2012，原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是隐含的。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.parsing;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;

/**
 * 这是 {@link ComponentDefinition} 的基础实现，它提供了对 {@link #getDescription} 的基本实现，
 * 该实现委托给 {@link #getName}。同时，它还提供了一个基本的 {@link #toString} 实现，
 * 该实现遵循推荐的实现策略，委托给 {@link #getDescription}。此外，它还提供了
 * 对 {@link #getInnerBeanDefinitions} 和 {@link #getBeanReferences} 的默认实现，
 * 这些实现返回一个空数组。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractComponentDefinition implements ComponentDefinition {

    /**
     * 委托给 {@link #getName}。
     */
    @Override
    public String getDescription() {
        return getName();
    }

    /**
     * 返回一个空数组。
     */
    @Override
    public BeanDefinition[] getBeanDefinitions() {
        return new BeanDefinition[0];
    }

    /**
     * 返回一个空数组。
     */
    @Override
    public BeanDefinition[] getInnerBeanDefinitions() {
        return new BeanDefinition[0];
    }

    /**
     * 返回一个空数组。
     */
    @Override
    public BeanReference[] getBeanReferences() {
        return new BeanReference[0];
    }

    /**
     * 委托给 {@link #getDescription}。
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
