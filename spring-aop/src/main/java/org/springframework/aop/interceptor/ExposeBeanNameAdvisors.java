// 翻译完成 glm-4-flash
/*版权所有 2002-2021 原作者或作者们。

根据Apache许可证2.0版（以下简称“许可证”）许可；除非遵守许可证规定，否则您不得使用此文件。
您可以在以下网址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.factory.NamedBean;
import org.springframework.lang.Nullable;

/**
 * 创建顾问的便捷方法，可用于在Spring IoC容器自动代理使用时绑定Bean名称到当前调用。可能支持AspectJ的`bean()`切点指示符。
 *
 * <p>通常用于Spring自动代理中，在代理创建时已知Bean名称。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.NamedBean
 */
public abstract class ExposeBeanNameAdvisors {

    /**
     * 绑定当前正在调用的 Bean 的名称，该名称存储在 ReflectiveMethodInvocation 的 userAttributes Map 中。
     */
    private static final String BEAN_NAME_ATTRIBUTE = ExposeBeanNameAdvisors.class.getName() + ".BEAN_NAME";

    /**
     * 查找当前调用对应的Bean名称。假设拦截器链中已经包含了ExposeBeanNameAdvisor，并且调用是通过ExposeInvocationInterceptor暴露的。
     * @return 返回Bean名称（永不返回null）
     * @throws IllegalStateException 如果Bean名称尚未暴露
     */
    public static String getBeanName() throws IllegalStateException {
        return getBeanName(ExposeInvocationInterceptor.currentInvocation());
    }

    /**
     * 根据给定的调用查找对应的 Bean 名称。假设拦截器链中已经包含了 ExposeBeanNameAdvisor。
     * @param mi 应包含 Bean 名称属性的方法调用 MethodInvocation
     * @return Bean 名称（决不为 null）
     * @throws IllegalStateException 如果 Bean 名称尚未暴露
     */
    public static String getBeanName(MethodInvocation mi) throws IllegalStateException {
        if (!(mi instanceof ProxyMethodInvocation pmi)) {
            throw new IllegalArgumentException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
        }
        String beanName = (String) pmi.getUserAttribute(BEAN_NAME_ATTRIBUTE);
        if (beanName == null) {
            throw new IllegalStateException("Cannot get bean name; not set on MethodInvocation: " + mi);
        }
        return beanName;
    }

    /**
     * 创建一个新的顾问，该顾问将公开指定的bean名称，无需介绍。
     * @param beanName 要公开的bean名称
     */
    public static Advisor createAdvisorWithoutIntroduction(String beanName) {
        return new DefaultPointcutAdvisor(new ExposeBeanNameInterceptor(beanName));
    }

    /**
     * 创建一个新的顾问，该顾问将公开给定的bean名称，引入NamedBean接口，以便在不强制目标对象了解此Spring IoC概念的情况下访问bean名称。
     * @param beanName 要公开的bean名称
     */
    public static Advisor createAdvisorIntroducingNamedBean(String beanName) {
        return new DefaultIntroductionAdvisor(new ExposeBeanNameIntroduction(beanName));
    }

    /**
     * 拦截器，将指定的bean名称作为调用属性公开。
     */
    private static class ExposeBeanNameInterceptor implements MethodInterceptor {

        private final String beanName;

        public ExposeBeanNameInterceptor(String beanName) {
            this.beanName = beanName;
        }

        @Override
        @Nullable
        public Object invoke(MethodInvocation mi) throws Throwable {
            if (!(mi instanceof ProxyMethodInvocation pmi)) {
                throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
            }
            pmi.setUserAttribute(BEAN_NAME_ATTRIBUTE, this.beanName);
            return mi.proceed();
        }
    }

    /**
     * 介绍部分，暴露指定的bean名称作为调用属性。
     */
    @SuppressWarnings("serial")
    private static class ExposeBeanNameIntroduction extends DelegatingIntroductionInterceptor implements NamedBean {

        private final String beanName;

        public ExposeBeanNameIntroduction(String beanName) {
            this.beanName = beanName;
        }

        @Override
        @Nullable
        public Object invoke(MethodInvocation mi) throws Throwable {
            if (!(mi instanceof ProxyMethodInvocation pmi)) {
                throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
            }
            pmi.setUserAttribute(BEAN_NAME_ATTRIBUTE, this.beanName);
            return super.invoke(mi);
        }

        @Override
        public String getBeanName() {
            return this.beanName;
        }
    }
}
