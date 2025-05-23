// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache许可证版本2.0（“许可证”），除非法律要求或书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何形式的保证或条件，无论是明示的还是暗示的。有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy.target;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.autoproxy.TargetSourceCreator;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 为需要创建多个原型bean实例的
 * {@link org.springframework.aop.framework.autoproxy.TargetSourceCreator}
 * 实现提供了一个便捷的超类。
 *
 * <p>使用内部BeanFactory来管理目标实例，
 * 将原始bean定义复制到这个内部工厂中。
 * 这是必要的，因为原始BeanFactory将只
 * 包含通过自动代理创建的代理实例。
 *
 * <p>需要在一个
 * {@link org.springframework.beans.factory.support.AbstractBeanFactory}
 * 中运行。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 */
public abstract class AbstractBeanFactoryBasedTargetSourceCreator implements TargetSourceCreator, BeanFactoryAware, DisposableBean {

    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private ConfigurableBeanFactory beanFactory;

    /**
     * 内部使用的 DefaultListableBeanFactory 实例，通过 Bean 名称进行索引。
     */
    private final Map<String, DefaultListableBeanFactory> internalBeanFactories = new HashMap<>();

    @Override
    public final void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ConfigurableBeanFactory clbf)) {
            throw new IllegalStateException("Cannot do auto-TargetSource creation with a BeanFactory " + "that doesn't implement ConfigurableBeanFactory: " + beanFactory.getClass());
        }
        this.beanFactory = clbf;
    }

    /**
     * 返回此TargetSourceCreators运行的BeanFactory。
     */
    @Nullable
    protected final BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    private ConfigurableBeanFactory getConfigurableBeanFactory() {
        Assert.state(this.beanFactory != null, "BeanFactory not set");
        return this.beanFactory;
    }

    // 由于您只提供了代码注释中的一个分隔符（"---------------------------------------------------------------------"），而没有提供实际的注释内容，我无法进行翻译。请提供具体的代码注释内容，我将为您翻译成中文。
    // 实现 TargetSourceCreator 接口
    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您翻译成中文。
    @Override
    @Nullable
    public final TargetSource getTargetSource(Class<?> beanClass, String beanName) {
        AbstractBeanFactoryBasedTargetSource targetSource = createBeanFactoryBasedTargetSource(beanClass, beanName);
        if (targetSource == null) {
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Configuring AbstractBeanFactoryBasedTargetSource: " + targetSource);
        }
        DefaultListableBeanFactory internalBeanFactory = getInternalBeanFactoryForBean(beanName);
        // 我们需要只覆盖这个Bean定义，因为它可能引用其他Bean。
        // 并且我们很高兴采用父类的定义来处理这些。
        // 始终在需要时使用原型作用域。
        BeanDefinition bd = getConfigurableBeanFactory().getMergedBeanDefinition(beanName);
        GenericBeanDefinition bdCopy = new GenericBeanDefinition(bd);
        if (isPrototypeBased()) {
            bdCopy.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        }
        internalBeanFactory.registerBeanDefinition(beanName, bdCopy);
        // 完成对PrototypeTargetSource的配置。
        targetSource.setTargetBeanName(beanName);
        targetSource.setBeanFactory(internalBeanFactory);
        return targetSource;
    }

    /**
     * 返回用于指定bean的内部BeanFactory。
     * @param beanName 目标bean的名称
     * @return 要使用的内部BeanFactory
     */
    protected DefaultListableBeanFactory getInternalBeanFactoryForBean(String beanName) {
        synchronized (this.internalBeanFactories) {
            return this.internalBeanFactories.computeIfAbsent(beanName, name -> buildInternalBeanFactory(getConfigurableBeanFactory()));
        }
    }

    /**
     * 构建一个内部BeanFactory，用于解析目标Bean。
     * @param containingFactory 包含的BeanFactory，最初定义了这些Bean
     * @return 一个独立的内部BeanFactory，用于持有一些目标Bean的副本
     */
    protected DefaultListableBeanFactory buildInternalBeanFactory(ConfigurableBeanFactory containingFactory) {
        // 设置父类，以便正确解析引用（向上容器层次结构）。
        DefaultListableBeanFactory internalBeanFactory = new DefaultListableBeanFactory(containingFactory);
        // 必需的，以便所有BeanPostProcessors、Scopes等均可用。
        internalBeanFactory.copyConfigurationFrom(containingFactory);
        // 过滤掉属于AOP基础设施的Bean后处理器
        // 因为这些只适用于在原始工厂中定义的 beans。
        internalBeanFactory.getBeanPostProcessors().removeIf(beanPostProcessor -> beanPostProcessor instanceof AopInfrastructureBean);
        return internalBeanFactory;
    }

    /**
     * 在TargetSourceCreator关闭时销毁内部的BeanFactory。
     * @see #getInternalBeanFactoryForBean
     */
    @Override
    public void destroy() {
        synchronized (this.internalBeanFactories) {
            for (DefaultListableBeanFactory bf : this.internalBeanFactories.values()) {
                bf.destroySingletons();
            }
        }
    }

    // 您似乎没有提供Java代码注释内容。请提供您希望翻译的Java代码注释，我将为您进行翻译。
    // 模板方法由子类实现
    // 由于您只提供了代码注释的开始标记“---------------------------------------------------------------------”，并没有提供具体的代码注释内容，因此我无法进行翻译。请提供完整的代码注释内容，我将为您翻译成中文。
    /**
     * 返回此TargetSourceCreator是否基于原型。
     * 将根据此设置目标bean定义的作用域。
     * <p>默认值为"true"。
     * @see org.springframework.beans.factory.config.BeanDefinition#isSingleton()
     */
    protected boolean isPrototypeBased() {
        return true;
    }

    /**
     * 子类必须实现此方法以返回一个新的 AbstractPrototypeBasedTargetSource，
     * 如果它们希望为该 Bean 创建自定义的 TargetSource，或者返回 {@code null}，
     * 如果它们对此不感兴趣，在这种情况下，将不会创建特殊的目标源。
     * 子类不应在 AbstractPrototypeBasedTargetSource 上调用 {@code setTargetBeanName} 或
     * {@code setBeanFactory}：此类的 {@code getTargetSource()} 实现将执行此操作。
     * @param beanClass 要为该 Bean 创建 TargetSource 的类
     * @param beanName Bean 的名称
     * @return AbstractPrototypeBasedTargetSource，或者如果不符合条件则返回 {@code null}
     */
    @Nullable
    protected abstract AbstractBeanFactoryBasedTargetSource createBeanFactoryBasedTargetSource(Class<?> beanClass, String beanName);
}
