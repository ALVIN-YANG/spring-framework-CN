// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。

根据Apache License 2.0许可（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式（明示或暗示）的保证或条件。
有关权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.framework.adapter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 一个BeanPostProcessor，用于在BeanFactory中注册带有AdvisorAdapterRegistry的beans（默认为GlobalAdvisorAdapterRegistry）。
 *
 * <p>它要正常工作，唯一的要求是它需要定义在应用程序上下文中，并包含“非原生”的Spring AdvisorAdapters，这些Adapter需要被Spring的AOP框架“识别”。
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 27.02.2004
 * @see #setAdvisorAdapterRegistry
 * @see AdvisorAdapter
 */
public class AdvisorAdapterRegistrationManager implements BeanPostProcessor {

    private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

    /**
     * 指定 AdvisorAdapterRegistry 以注册 AdvisorAdapter 实例。
     * 默认为全局 AdvisorAdapterRegistry。
     * @see GlobalAdvisorAdapterRegistry
     */
    public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
        this.advisorAdapterRegistry = advisorAdapterRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AdvisorAdapter advisorAdapter) {
            this.advisorAdapterRegistry.registerAdvisorAdapter(advisorAdapter);
        }
        return bean;
    }
}
