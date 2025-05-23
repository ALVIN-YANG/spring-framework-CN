// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非您遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体语言管理权限和限制。*/
package org.springframework.aop.framework;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * 为生产单例作用域代理对象的 {@link FactoryBean} 类型的便捷超类。
 *
 * <p>管理前置和后置拦截器（与在 {@link ProxyFactoryBean} 中的拦截器名称不同，这里是引用）并提供一致的接口管理。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public abstract class AbstractSingletonProxyFactoryBean extends ProxyConfig implements FactoryBean<Object>, BeanClassLoaderAware, InitializingBean {

    @Nullable
    private Object target;

    @Nullable
    private Class<?>[] proxyInterfaces;

    @Nullable
    private Object[] preInterceptors;

    @Nullable
    private Object[] postInterceptors;

    /**
     * 默认为全局 AdvisorAdapterRegistry。
     */
    private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

    @Nullable
    private transient ClassLoader proxyClassLoader;

    @Nullable
    private Object proxy;

    /**
     * 设置目标对象，即要使用事务代理包装的bean。
     * <p>目标可以是任何对象，在这种情况下将创建一个SingletonTargetSource。如果它是一个TargetSource，则不会创建包装的TargetSource：
     * 这允许使用池化或原型TargetSource等。
     * @see org.springframework.aop.TargetSource
     * @see org.springframework.aop.target.SingletonTargetSource
     * @see org.springframework.aop.target.LazyInitTargetSource
     * @see org.springframework.aop.target.PrototypeTargetSource
     * @see org.springframework.aop.target.CommonsPool2TargetSource
     */
    public void setTarget(Object target) {
        this.target = target;
    }

    /**
     * 指定要代理的接口集合。
     * <p>如果没有指定（默认行为），AOP（面向切面编程）基础设施会通过分析目标对象来确定需要代理哪些接口，
     * 并代理目标对象实现的所有接口。
     */
    public void setProxyInterfaces(Class<?>[] proxyInterfaces) {
        this.proxyInterfaces = proxyInterfaces;
    }

    /**
     * 设置在隐式事务拦截器之前应用的其他拦截器（或顾问），例如性能监控拦截器。
     * <p>您可以指定任何AOP联盟方法拦截器或其他Spring AOP建议，以及Spring AOP顾问。
     * @see org.springframework.aop.interceptor.PerformanceMonitorInterceptor
     */
    public void setPreInterceptors(Object[] preInterceptors) {
        this.preInterceptors = preInterceptors;
    }

    /**
     * 设置在隐式事务拦截器之后要应用的其他拦截器（或顾问）。
     * <p>您可以指定任何AOP联盟方法拦截器或其他Spring AOP建议，以及Spring AOP顾问。
     */
    public void setPostInterceptors(Object[] postInterceptors) {
        this.postInterceptors = postInterceptors;
    }

    /**
     * 指定要使用的 AdvisorAdapterRegistry。
     * 默认使用全局的 AdvisorAdapterRegistry。
     * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
     */
    public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
        this.advisorAdapterRegistry = advisorAdapterRegistry;
    }

    /**
     * 设置用于生成代理类的 ClassLoader。
     * <p>默认值为 bean 的 ClassLoader，即包含 BeanFactory 用于加载所有 bean 类的 ClassLoader。这里可以覆盖默认值以针对特定的代理进行设置。
     */
    public void setProxyClassLoader(ClassLoader classLoader) {
        this.proxyClassLoader = classLoader;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        if (this.proxyClassLoader == null) {
            this.proxyClassLoader = classLoader;
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (this.target == null) {
            throw new IllegalArgumentException("Property 'target' is required");
        }
        if (this.target instanceof String) {
            throw new IllegalArgumentException("'target' needs to be a bean reference, not a bean name as value");
        }
        if (this.proxyClassLoader == null) {
            this.proxyClassLoader = ClassUtils.getDefaultClassLoader();
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        if (this.preInterceptors != null) {
            for (Object interceptor : this.preInterceptors) {
                proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(interceptor));
            }
        }
        // 添加主拦截器（通常是一个Advisor）。
        proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(createMainInterceptor()));
        if (this.postInterceptors != null) {
            for (Object interceptor : this.postInterceptors) {
                proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(interceptor));
            }
        }
        proxyFactory.copyFrom(this);
        TargetSource targetSource = createTargetSource(this.target);
        proxyFactory.setTargetSource(targetSource);
        if (this.proxyInterfaces != null) {
            proxyFactory.setInterfaces(this.proxyInterfaces);
        } else if (!isProxyTargetClass()) {
            // 依赖 AOP（面向切面编程）基础设施来告知我们哪些接口需要代理。
            Class<?> targetClass = targetSource.getTargetClass();
            if (targetClass != null) {
                proxyFactory.setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
            }
        }
        postProcessProxyFactory(proxyFactory);
        this.proxy = proxyFactory.getProxy(this.proxyClassLoader);
    }

    /**
     * 确定给定目标（或目标源）的TargetSource。
     * @param target 目标。如果这个对象实现了TargetSource接口，则直接使用它作为我们的TargetSource；否则，将其包装在SingletonTargetSource中。
     * @return 该对象的TargetSource
     */
    protected TargetSource createTargetSource(Object target) {
        if (target instanceof TargetSource targetSource) {
            return targetSource;
        } else {
            return new SingletonTargetSource(target);
        }
    }

    /**
     * 子类用于在创建代理实例之前对 {@link ProxyFactory} 进行后处理的钩子
     * @param proxyFactory 即将使用的 AOP ProxyFactory
     * @since 4.2
     */
    protected void postProcessProxyFactory(ProxyFactory proxyFactory) {
    }

    @Override
    public Object getObject() {
        if (this.proxy == null) {
            throw new FactoryBeanNotInitializedException();
        }
        return this.proxy;
    }

    @Override
    @Nullable
    public Class<?> getObjectType() {
        if (this.proxy != null) {
            return this.proxy.getClass();
        }
        if (this.proxyInterfaces != null && this.proxyInterfaces.length == 1) {
            return this.proxyInterfaces[0];
        }
        if (this.target instanceof TargetSource targetSource) {
            return targetSource.getTargetClass();
        }
        if (this.target != null) {
            return this.target.getClass();
        }
        return null;
    }

    @Override
    public final boolean isSingleton() {
        return true;
    }

    /**
     * 为此代理工厂Bean创建“main”拦截器。
     * 通常是一个Advisor，但也可能是任何类型的Advice。
     * <p>预拦截器将在本拦截器之前应用，后拦截器将在本拦截器之后应用。
     */
    protected abstract Object createMainInterceptor();
}
