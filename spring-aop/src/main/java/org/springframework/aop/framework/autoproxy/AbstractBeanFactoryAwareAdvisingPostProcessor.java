// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。

根据Apache License, Version 2.0（“许可证”）许可；除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵犯权利。
有关许可的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;

/**
 * 扩展了 {@link AbstractAutoProxyCreator}，实现了 {@link BeanFactoryAware}，
 * 为每个代理的Bean添加了对原始目标类的暴露（{@link AutoProxyUtils#ORIGINAL_TARGET_CLASS_ATTRIBUTE}），
 * 并参与了对任何给定Bean的外部强制的目标类模式（{@link AutoProxyUtils#PRESERVE_TARGET_CLASS_ATTRIBUTE}）。
 * 因此，该后处理器与 {@link AbstractAutoProxyCreator} 保持一致。
 *
 * @author Juergen Hoeller
 * @since 4.2.3
 * @see AutoProxyUtils#shouldProxyTargetClass
 * @see AutoProxyUtils#determineTargetClass
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanFactoryAwareAdvisingPostProcessor extends AbstractAdvisingBeanPostProcessor implements BeanFactoryAware {

    @Nullable
    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = (beanFactory instanceof ConfigurableListableBeanFactory clbf ? clbf : null);
    }

    @Override
    protected ProxyFactory prepareProxyFactory(Object bean, String beanName) {
        if (this.beanFactory != null) {
            AutoProxyUtils.exposeTargetClass(this.beanFactory, beanName, bean.getClass());
        }
        ProxyFactory proxyFactory = super.prepareProxyFactory(bean, beanName);
        if (!proxyFactory.isProxyTargetClass() && this.beanFactory != null && AutoProxyUtils.shouldProxyTargetClass(this.beanFactory, beanName)) {
            proxyFactory.setProxyTargetClass(true);
        }
        return proxyFactory;
    }

    @Override
    protected boolean isEligible(Object bean, String beanName) {
        return (!AutoProxyUtils.isOriginalInstance(beanName, bean.getClass()) && super.isEligible(bean, beanName));
    }
}
