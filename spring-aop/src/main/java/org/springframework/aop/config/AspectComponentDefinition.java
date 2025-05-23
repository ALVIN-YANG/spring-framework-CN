// 翻译完成 glm-4-flash
/*版权所有 2002-2017 原作者或作者。
 
根据Apache许可证版本2.0（以下简称“许可证”）许可；除非遵守许可证，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
有关许可的具体语言、权限和限制，请参阅许可证。*/
package org.springframework.aop.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.lang.Nullable;

/**
 * /**
 *  保存一个包含其嵌套切入点定义的方面定义的
 *  {@link org.springframework.beans.factory.parsing.ComponentDefinition}。
 *
 *  @author Rob Harrop
 *  @author Juergen Hoeller
 *  @since 2.0
 *  @see #getNestedComponents()
 *  @see PointcutComponentDefinition
 * /
 */
public class AspectComponentDefinition extends CompositeComponentDefinition {

    private final BeanDefinition[] beanDefinitions;

    private final BeanReference[] beanReferences;

    public AspectComponentDefinition(String aspectName, @Nullable BeanDefinition[] beanDefinitions, @Nullable BeanReference[] beanReferences, @Nullable Object source) {
        super(aspectName, source);
        this.beanDefinitions = (beanDefinitions != null ? beanDefinitions : new BeanDefinition[0]);
        this.beanReferences = (beanReferences != null ? beanReferences : new BeanReference[0]);
    }

    @Override
    public BeanDefinition[] getBeanDefinitions() {
        return this.beanDefinitions;
    }

    @Override
    public BeanReference[] getBeanReferences() {
        return this.beanReferences;
    }
}
