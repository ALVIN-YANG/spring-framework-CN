// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）许可；
除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性还是其特定用途。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import java.io.Serializable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 基于 Spring 的 {@link org.springframework.beans.factory.BeanFactory} 支持的 {@link org.springframework.aop.aspectj.AspectInstanceFactory} 实现。
 *
 * <p>请注意，如果使用原型模式，这可能会多次实例化，这可能不会给出您预期的语义。请使用 {@link LazySingletonAspectInstanceFactoryDecorator} 来包装它，以确保只返回一个新方面。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory
 * @see LazySingletonAspectInstanceFactoryDecorator
 */
@SuppressWarnings("serial")
public class BeanFactoryAspectInstanceFactory implements MetadataAwareAspectInstanceFactory, Serializable {

    private final BeanFactory beanFactory;

    private final String name;

    private final AspectMetadata aspectMetadata;

    /**
     * 创建一个BeanFactoryAspectInstanceFactory。AspectJ将被调用以进行内省，并使用从BeanFactory返回的给定bean名称的类型来创建AJType元数据。
     * @param beanFactory 要从中获取实例的BeanFactory
     * @param name bean的名称
     */
    public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name) {
        this(beanFactory, name, null);
    }

    /**
     * 创建一个BeanFactoryAspectInstanceFactory，提供一个类型，AspectJ将对其进行反射以创建AJType元数据。当BeanFactory可能将类型视为子类（如使用CGLIB）时使用，并且信息应与超类相关。
     * @param beanFactory 要从中获取实例的BeanFactory
     * @param name Bean的名称
     * @param type AspectJ应进行反射的类型（如果为null，则通过bean名称通过{@link BeanFactory#getType}进行解析）
     */
    public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name, @Nullable Class<?> type) {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        Assert.notNull(name, "Bean name must not be null");
        this.beanFactory = beanFactory;
        this.name = name;
        Class<?> resolvedType = type;
        if (type == null) {
            resolvedType = beanFactory.getType(name);
            Assert.notNull(resolvedType, "Unresolvable bean type - explicitly specify the aspect class");
        }
        this.aspectMetadata = new AspectMetadata(resolvedType, name);
    }

    @Override
    public Object getAspectInstance() {
        return this.beanFactory.getBean(this.name);
    }

    @Override
    @Nullable
    public ClassLoader getAspectClassLoader() {
        return (this.beanFactory instanceof ConfigurableBeanFactory cbf ? cbf.getBeanClassLoader() : ClassUtils.getDefaultClassLoader());
    }

    @Override
    public AspectMetadata getAspectMetadata() {
        return this.aspectMetadata;
    }

    @Override
    @Nullable
    public Object getAspectCreationMutex() {
        if (this.beanFactory.isSingleton(this.name)) {
            // 依赖工厂提供的单例语义 -> 无局部锁。
            return null;
        } else if (this.beanFactory instanceof ConfigurableBeanFactory cbf) {
            // 没有从工厂处获得单例保证 -> 让我们在本地进行锁定，但
            // 复用工厂的单一实例锁，以防万一有懒加载的依赖项
            // 我们的建议 Bean 不小心隐式地触发了单例锁...
            return cbf.getSingletonMutex();
        } else {
            return this;
        }
    }

    /**
     * 确定该工厂的目标切面的顺序，可以是以下两种情况之一：
     * 1. 通过实现 {@link org.springframework.core.Ordered} 接口表示的实例特定顺序（仅对单例bean进行检查）；
     * 2. 通过类级别上的 {@link org.springframework.core.annotation.Order} 注解表示的顺序。
     * @see org.springframework.core.Ordered
     * @see org.springframework.core.annotation.Order
     */
    @Override
    public int getOrder() {
        Class<?> type = this.beanFactory.getType(this.name);
        if (type != null) {
            if (Ordered.class.isAssignableFrom(type) && this.beanFactory.isSingleton(this.name)) {
                return ((Ordered) this.beanFactory.getBean(this.name)).getOrder();
            }
            return OrderUtils.getOrder(type, Ordered.LOWEST_PRECEDENCE);
        }
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": bean name '" + this.name + "'";
    }
}
