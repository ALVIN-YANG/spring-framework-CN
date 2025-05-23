// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或原作者。

根据Apache许可证2.0版本（以下简称“许可证”）；除非符合许可证，否则不得使用此文件。
您可以在以下链接获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.config;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.parsing.AbstractComponentDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.beans.factory.parsing.ComponentDefinition}
 * 用于连接由 {@code <aop:advisor>} 标签配置的顾问 bean 定义与组件定义基础设施之间的差距。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AdvisorComponentDefinition extends AbstractComponentDefinition {

    private final String advisorBeanName;

    private final BeanDefinition advisorDefinition;

    private final String description;

    private final BeanReference[] beanReferences;

    private final BeanDefinition[] beanDefinitions;

    public AdvisorComponentDefinition(String advisorBeanName, BeanDefinition advisorDefinition) {
        this(advisorBeanName, advisorDefinition, null);
    }

    public AdvisorComponentDefinition(String advisorBeanName, BeanDefinition advisorDefinition, @Nullable BeanDefinition pointcutDefinition) {
        Assert.notNull(advisorBeanName, "'advisorBeanName' must not be null");
        Assert.notNull(advisorDefinition, "'advisorDefinition' must not be null");
        this.advisorBeanName = advisorBeanName;
        this.advisorDefinition = advisorDefinition;
        MutablePropertyValues pvs = advisorDefinition.getPropertyValues();
        BeanReference adviceReference = (BeanReference) pvs.get("adviceBeanName");
        Assert.state(adviceReference != null, "Missing 'adviceBeanName' property");
        if (pointcutDefinition != null) {
            this.beanReferences = new BeanReference[] { adviceReference };
            this.beanDefinitions = new BeanDefinition[] { advisorDefinition, pointcutDefinition };
            this.description = buildDescription(adviceReference, pointcutDefinition);
        } else {
            BeanReference pointcutReference = (BeanReference) pvs.get("pointcut");
            Assert.state(pointcutReference != null, "Missing 'pointcut' property");
            this.beanReferences = new BeanReference[] { adviceReference, pointcutReference };
            this.beanDefinitions = new BeanDefinition[] { advisorDefinition };
            this.description = buildDescription(adviceReference, pointcutReference);
        }
    }

    private String buildDescription(BeanReference adviceReference, BeanDefinition pointcutDefinition) {
        return "Advisor <advice(ref)='" + adviceReference.getBeanName() + "', pointcut(expression)=[" + pointcutDefinition.getPropertyValues().get("expression") + "]>";
    }

    private String buildDescription(BeanReference adviceReference, BeanReference pointcutReference) {
        return "Advisor <advice(ref)='" + adviceReference.getBeanName() + "', pointcut(ref)='" + pointcutReference.getBeanName() + "'>";
    }

    @Override
    public String getName() {
        return this.advisorBeanName;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public BeanDefinition[] getBeanDefinitions() {
        return this.beanDefinitions;
    }

    @Override
    public BeanReference[] getBeanReferences() {
        return this.beanReferences;
    }

    @Override
    @Nullable
    public Object getSource() {
        return this.advisorDefinition.getSource();
    }
}
