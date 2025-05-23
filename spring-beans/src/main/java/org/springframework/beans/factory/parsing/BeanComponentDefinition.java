// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非适用法律要求或经书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.parsing;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.lang.Nullable;

/**
 * 基于标准BeanDefinition的ComponentDefinition，暴露给定的bean定义以及给定bean的内部bean定义和bean引用。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class BeanComponentDefinition extends BeanDefinitionHolder implements ComponentDefinition {

    private final BeanDefinition[] innerBeanDefinitions;

    private final BeanReference[] beanReferences;

    /**
     * 为给定的 Bean 创建一个新的 BeanComponentDefinition。
     * @param beanDefinition BeanDefinition
     * @param beanName Bean 的名称
     */
    public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName) {
        this(new BeanDefinitionHolder(beanDefinition, beanName));
    }

    /**
     * 为给定的bean创建一个新的BeanComponentDefinition。
     * @param beanDefinition BeanDefinition
     * @param beanName bean的名称
     * @param aliases bean的别名，如果没有则传入{@code null}
     */
    public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName, @Nullable String[] aliases) {
        this(new BeanDefinitionHolder(beanDefinition, beanName, aliases));
    }

    /**
     * 为给定的 Bean 创建一个新的 BeanComponentDefinition。
     * @param beanDefinitionHolder 封装了 Bean 定义以及 Bean 名称的 BeanDefinitionHolder
     */
    public BeanComponentDefinition(BeanDefinitionHolder beanDefinitionHolder) {
        super(beanDefinitionHolder);
        List<BeanDefinition> innerBeans = new ArrayList<>();
        List<BeanReference> references = new ArrayList<>();
        PropertyValues propertyValues = beanDefinitionHolder.getBeanDefinition().getPropertyValues();
        for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
            Object value = propertyValue.getValue();
            if (value instanceof BeanDefinitionHolder beanDefHolder) {
                innerBeans.add(beanDefHolder.getBeanDefinition());
            } else if (value instanceof BeanDefinition beanDef) {
                innerBeans.add(beanDef);
            } else if (value instanceof BeanReference beanRef) {
                references.add(beanRef);
            }
        }
        this.innerBeanDefinitions = innerBeans.toArray(new BeanDefinition[0]);
        this.beanReferences = references.toArray(new BeanReference[0]);
    }

    @Override
    public String getName() {
        return getBeanName();
    }

    @Override
    public String getDescription() {
        return getShortDescription();
    }

    @Override
    public BeanDefinition[] getBeanDefinitions() {
        return new BeanDefinition[] { getBeanDefinition() };
    }

    @Override
    public BeanDefinition[] getInnerBeanDefinitions() {
        return this.innerBeanDefinitions;
    }

    @Override
    public BeanReference[] getBeanReferences() {
        return this.beanReferences;
    }

    /**
     * 此实现返回此 ComponentDefinition 的描述。
     * @see #getDescription()
     */
    @Override
    public String toString() {
        return getDescription();
    }

    /**
     * 此实现期望另一个对象不仅是BeanComponentDefinition类型，还满足父类的等价性要求。
     */
    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof BeanComponentDefinition && super.equals(other)));
    }
}
