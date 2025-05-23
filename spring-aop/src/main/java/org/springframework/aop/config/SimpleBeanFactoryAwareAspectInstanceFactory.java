// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非根据法律规定或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下链接获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，无论是关于其适用性还是关于其特定用途。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.config;

import org.springframework.aop.aspectj.AspectInstanceFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 实现了 {@link AspectInstanceFactory}，它通过配置的bean名称从 {@link org.springframework.beans.factory.BeanFactory} 中定位方面。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class SimpleBeanFactoryAwareAspectInstanceFactory implements AspectInstanceFactory, BeanFactoryAware {

    @Nullable
    private String aspectBeanName;

    @Nullable
    private BeanFactory beanFactory;

    /**
     * 设置方面Bean的名称。这是在调用
     * {@link #getAspectInstance()}时返回的Bean。
     */
    public void setAspectBeanName(String aspectBeanName) {
        this.aspectBeanName = aspectBeanName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        Assert.notNull(this.aspectBeanName, "'aspectBeanName' is required");
    }

    /**
     * 从 {@link BeanFactory} 中查找方面（Aspect）Bean 并返回它。
     * @see #setAspectBeanName
     */
    @Override
    public Object getAspectInstance() {
        Assert.state(this.beanFactory != null, "No BeanFactory set");
        Assert.state(this.aspectBeanName != null, "No 'aspectBeanName' set");
        return this.beanFactory.getBean(this.aspectBeanName);
    }

    @Override
    @Nullable
    public ClassLoader getAspectClassLoader() {
        if (this.beanFactory instanceof ConfigurableBeanFactory cbf) {
            return cbf.getBeanClassLoader();
        } else {
            return ClassUtils.getDefaultClassLoader();
        }
    }

    @Override
    public int getOrder() {
        if (this.beanFactory != null && this.aspectBeanName != null && this.beanFactory.isSingleton(this.aspectBeanName) && this.beanFactory.isTypeMatch(this.aspectBeanName, Ordered.class)) {
            return ((Ordered) this.beanFactory.getBean(this.aspectBeanName)).getOrder();
        }
        return Ordered.LOWEST_PRECEDENCE;
    }
}
