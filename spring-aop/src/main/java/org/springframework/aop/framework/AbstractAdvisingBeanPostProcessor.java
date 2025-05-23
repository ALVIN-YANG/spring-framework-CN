// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0 ("许可证")授权；
除非遵守许可证，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或书面同意，否则在许可证下分发的软件按"原样"分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.framework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.SmartClassLoader;
import org.springframework.lang.Nullable;

/**
 * 为应用Spring AOP的{@link Advisor}到特定bean的{@link BeanPostProcessor}实现提供基类的基类。
 *
 * @author Juergen Hoeller
 * @since 3.2
 */
@SuppressWarnings("serial")
public abstract class AbstractAdvisingBeanPostProcessor extends ProxyProcessorSupport implements SmartInstantiationAwareBeanPostProcessor {

    @Nullable
    protected Advisor advisor;

    protected boolean beforeExistingAdvisors = false;

    private final Map<Class<?>, Boolean> eligibleBeans = new ConcurrentHashMap<>(256);

    /**
     * 设置是否在遇到预先建议的对象时，此后处理器建议器应该应用于现有建议器之前。
     * <p>默认为"false"，即在现有建议器之后应用建议器，即尽可能接近目标方法。将此切换为"true"，
     * 以便此后处理器的建议器也能包装现有建议器。
     * <p>注意：请检查具体后处理器的javadoc，以了解它是否默认根据其建议器的性质更改此标志。
     */
    public void setBeforeExistingAdvisors(boolean beforeExistingAdvisors) {
        this.beforeExistingAdvisors = beforeExistingAdvisors;
    }

    @Override
    public Class<?> determineBeanType(Class<?> beanClass, String beanName) {
        if (this.advisor != null && isEligible(beanClass)) {
            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.copyFrom(this);
            proxyFactory.setTargetClass(beanClass);
            if (!proxyFactory.isProxyTargetClass()) {
                evaluateProxyInterfaces(beanClass, proxyFactory);
            }
            proxyFactory.addAdvisor(this.advisor);
            customizeProxyFactory(proxyFactory);
            // 如果被覆盖的类加载器中没有本地加载的bean类，则使用原始的ClassLoader。
            ClassLoader classLoader = getProxyClassLoader();
            if (classLoader instanceof SmartClassLoader smartClassLoader && classLoader != beanClass.getClassLoader()) {
                classLoader = smartClassLoader.getOriginalClassLoader();
            }
            return proxyFactory.getProxyClass(classLoader);
        }
        return beanClass;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (this.advisor == null || bean instanceof AopInfrastructureBean) {
            // 忽略 AOP（面向切面编程）基础设施，例如作用域代理。
            return bean;
        }
        if (bean instanceof Advised advised) {
            if (!advised.isFrozen() && isEligible(AopUtils.getTargetClass(bean))) {
                // 将我们的本地顾问（Advisor）添加到现有代理（proxy）的顾问链（Advisor chain）中。
                if (this.beforeExistingAdvisors) {
                    advised.addAdvisor(0, this.advisor);
                } else if (advised.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE && advised.getAdvisorCount() > 0) {
                    // 没有目标，保持最后一位顾问的位置不变，并在其右侧添加新的顾问。
                    advised.addAdvisor(advised.getAdvisorCount() - 1, this.advisor);
                    return bean;
                } else {
                    advised.addAdvisor(this.advisor);
                }
                return bean;
            }
        }
        if (isEligible(bean, beanName)) {
            ProxyFactory proxyFactory = prepareProxyFactory(bean, beanName);
            if (!proxyFactory.isProxyTargetClass()) {
                evaluateProxyInterfaces(bean.getClass(), proxyFactory);
            }
            proxyFactory.addAdvisor(this.advisor);
            customizeProxyFactory(proxyFactory);
            // 如果被覆盖的类加载器中没有本地加载的bean类，则使用原始的ClassLoader。
            ClassLoader classLoader = getProxyClassLoader();
            if (classLoader instanceof SmartClassLoader smartClassLoader && classLoader != bean.getClass().getClassLoader()) {
                classLoader = smartClassLoader.getOriginalClassLoader();
            }
            return proxyFactory.getProxy(classLoader);
        }
        // 无需代理。
        return bean;
    }

    /**
     * 检查给定的bean是否适合使用此后处理器的{@link Advisor}进行咨询。
     * <p>委托给{@link #isEligible(Class)}进行目标类的检查。
     * 可以被覆盖，例如，可以通过名称特别排除某些bean。
     * <p>注意：仅对常规bean实例进行调用，而不是对实现{@link Advised}并允许将本地{@link Advisor}添加到现有代理的{@link Advisor}链的现有代理实例进行调用。
     * 对于后者，直接调用{@link #isEligible(Class)}，使用现有代理后面的实际目标类（由{@link AopUtils#getTargetClass(Object)}确定）。
     * @param bean bean实例
     * @param beanName bean的名称
     * @see #isEligible(Class)
     */
    protected boolean isEligible(Object bean, String beanName) {
        return isEligible(bean.getClass());
    }

    /**
     * 检查给定的类是否适合使用此后处理器的 {@link Advisor} 进行建议。
     * <p>实现缓存每个目标类上的 {@code canApply} 结果。
     * @param targetClass 要检查的类
     * @see AopUtils#canApply(Advisor, Class)
     */
    protected boolean isEligible(Class<?> targetClass) {
        Boolean eligible = this.eligibleBeans.get(targetClass);
        if (eligible != null) {
            return eligible;
        }
        if (this.advisor == null) {
            return false;
        }
        eligible = AopUtils.canApply(this.advisor, targetClass);
        this.eligibleBeans.put(targetClass, eligible);
        return eligible;
    }

    /**
     * 为给定Bean准备一个 {@link ProxyFactory}。
     * <p>子类可以自定义对目标实例的处理，特别是对目标类的暴露。默认情况下，非目标类代理将应用接口的反射，以及配置的顾问；之后，`#customizeProxyFactory` 允许在创建代理之前对这些部分进行晚期自定义。
     * @param bean 要创建代理的Bean实例
     * @param beanName 相应的Bean名称
     * @return 初始化了此处理器 {@link ProxyConfig} 设置和指定Bean的 ProxyFactory
     * @since 4.2.3
     * @see #customizeProxyFactory
     */
    protected ProxyFactory prepareProxyFactory(Object bean, String beanName) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.copyFrom(this);
        proxyFactory.setTarget(bean);
        return proxyFactory;
    }

    /**
     * 子类可以选择实现此方法：例如，
     * 用于更改公开的接口。
     * <p>默认实现为空。
     * @param proxyFactory 已配置了目标、顾问和接口的 ProxyFactory，将在此方法返回后立即使用它来创建代理
     * @since 4.2.3
     * @see #prepareProxyFactory
     */
    protected void customizeProxyFactory(ProxyFactory proxyFactory) {
    }
}
