// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可，您可以使用此文件，但必须遵守许可证条款。
您可以在以下链接处获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.AbstractComponentDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 对应于 `org.springframework.beans.factory.parsing.ComponentDefinition` 的实现，用于持有切入点定义。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class PointcutComponentDefinition extends AbstractComponentDefinition {

    private final String pointcutBeanName;

    private final BeanDefinition pointcutDefinition;

    private final String description;

    public PointcutComponentDefinition(String pointcutBeanName, BeanDefinition pointcutDefinition, String expression) {
        Assert.notNull(pointcutBeanName, "Bean name must not be null");
        Assert.notNull(pointcutDefinition, "Pointcut definition must not be null");
        Assert.notNull(expression, "Expression must not be null");
        this.pointcutBeanName = pointcutBeanName;
        this.pointcutDefinition = pointcutDefinition;
        this.description = "Pointcut <name='" + pointcutBeanName + "', expression=[" + expression + "]>";
    }

    @Override
    public String getName() {
        return this.pointcutBeanName;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public BeanDefinition[] getBeanDefinitions() {
        return new BeanDefinition[] { this.pointcutDefinition };
    }

    @Override
    @Nullable
    public Object getSource() {
        return this.pointcutDefinition.getSource();
    }
}
