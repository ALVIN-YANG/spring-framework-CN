// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的明示或暗示保证，
* 包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.io.Serializable;
import jakarta.inject.Provider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 *  一个实现 {@link org.springframework.beans.factory.FactoryBean} 的类，它返回一个 JSR-330 的 {@link jakarta.inject.Provider} 值，而该值又转而返回来自一个 {@link org.springframework.beans.factory.BeanFactory} 的 Bean。
 *
 * <p>这基本上是 Spring 中古老的 {@link ObjectFactoryCreatingFactoryBean} 的一个 JSR-330 兼容版本。它可以用于传统的面向属性或构造函数参数的依赖注入配置，这些参数的类型为 {@code jakarta.inject.Provider}，作为 JSR-330 的 {@code @Inject} 注解驱动方法的替代方案。
 *
 *  @author Juergen Hoeller
 *  @since 3.0.2
 *  @see jakarta.inject.Provider
 *  @see ObjectFactoryCreatingFactoryBean
 */
public class ProviderCreatingFactoryBean extends AbstractFactoryBean<Provider<Object>> {

    @Nullable
    private String targetBeanName;

    /**
     * 设置目标 Bean 的名称。
     * <p>目标不必是非单例 Bean，但实际上通常总是如此（因为如果目标 Bean 是单例，那么这个单例 Bean 可以直接注入到依赖对象中，从而消除了由这种工厂方法提供的额外间接层的需求）。
     */
    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.targetBeanName, "Property 'targetBeanName' is required");
        super.afterPropertiesSet();
    }

    @Override
    public Class<?> getObjectType() {
        return Provider.class;
    }

    @Override
    protected Provider<Object> createInstance() {
        BeanFactory beanFactory = getBeanFactory();
        Assert.state(beanFactory != null, "No BeanFactory available");
        Assert.state(this.targetBeanName != null, "No target bean name specified");
        return new TargetBeanProvider(beanFactory, this.targetBeanName);
    }

    /**
     * 独立内部类 - 用于序列化目的。
     */
    @SuppressWarnings("serial")
    private static class TargetBeanProvider implements Provider<Object>, Serializable {

        private final BeanFactory beanFactory;

        private final String targetBeanName;

        public TargetBeanProvider(BeanFactory beanFactory, String targetBeanName) {
            this.beanFactory = beanFactory;
            this.targetBeanName = targetBeanName;
        }

        @Override
        public Object get() throws BeansException {
            return this.beanFactory.getBean(this.targetBeanName);
        }
    }
}
