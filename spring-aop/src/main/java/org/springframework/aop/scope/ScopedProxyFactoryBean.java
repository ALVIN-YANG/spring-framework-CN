// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或原作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的或非侵权性。
* 请参阅许可证，了解具体规定许可权限和限制。*/
package org.springframework.aop.scope;

import java.lang.reflect.Modifier;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.target.SimpleBeanTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 用于范围对象的便捷代理工厂Bean。
 *
 * <p>使用此工厂Bean创建的代理是线程安全的单例，并且可以注入到共享对象中，具有透明的范围行为。
 *
 * <p>此类返回的代理实现了{@link ScopedObject}接口。这目前允许在访问时无缝地从范围中删除相应的对象，并在下次访问时在范围中创建一个新的实例。
 *
 * <p>请注意，此工厂创建的代理默认是<i>基于类的</i>代理。这可以通过将"proxyTargetClass"属性切换为"false"来自定义。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setProxyTargetClass
 */
@SuppressWarnings("serial")
public class ScopedProxyFactoryBean extends ProxyConfig implements FactoryBean<Object>, BeanFactoryAware, AopInfrastructureBean {

    /**
     * 管理作用域的 TargetSource。
     */
    private final SimpleBeanTargetSource scopedTargetSource = new SimpleBeanTargetSource();

    /**
     * 目标 Bean 的名称。
     */
    @Nullable
    private String targetBeanName;

    /**
     * 缓存的单例代理。
     */
    @Nullable
    private Object proxy;

    /**
     * 创建一个新的ScopedProxyFactoryBean实例。
     */
    public ScopedProxyFactoryBean() {
        setProxyTargetClass(true);
    }

    /**
     * 设置要设置作用域的bean的名称。
     */
    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
        this.scopedTargetSource.setTargetBeanName(targetBeanName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ConfigurableBeanFactory cbf)) {
            throw new IllegalStateException("Not running in a ConfigurableBeanFactory: " + beanFactory);
        }
        this.scopedTargetSource.setBeanFactory(beanFactory);
        ProxyFactory pf = new ProxyFactory();
        pf.copyFrom(this);
        pf.setTargetSource(this.scopedTargetSource);
        Assert.notNull(this.targetBeanName, "Property 'targetBeanName' is required");
        Class<?> beanType = beanFactory.getType(this.targetBeanName);
        if (beanType == null) {
            throw new IllegalStateException("Cannot create scoped proxy for bean '" + this.targetBeanName + "': Target type could not be determined at the time of proxy creation.");
        }
        if (!isProxyTargetClass() || beanType.isInterface() || Modifier.isPrivate(beanType.getModifiers())) {
            pf.setInterfaces(ClassUtils.getAllInterfacesForClass(beanType, cbf.getBeanClassLoader()));
        }
        // 添加一个只实现ScopedObject上方法的介绍。
        ScopedObject scopedObject = new DefaultScopedObject(cbf, this.scopedTargetSource.getTargetBeanName());
        pf.addAdvice(new DelegatingIntroductionInterceptor(scopedObject));
        // 将 AopInfrastructureBean 标记添加到表示作用域代理
        // 它自身不受自动代理的影响！只有它的目标 Bean 才会。
        pf.addInterface(AopInfrastructureBean.class);
        this.proxy = pf.getProxy(cbf.getBeanClassLoader());
    }

    @Override
    public Object getObject() {
        if (this.proxy == null) {
            throw new FactoryBeanNotInitializedException();
        }
        return this.proxy;
    }

    @Override
    public Class<?> getObjectType() {
        if (this.proxy != null) {
            return this.proxy.getClass();
        }
        return this.scopedTargetSource.getTargetClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
