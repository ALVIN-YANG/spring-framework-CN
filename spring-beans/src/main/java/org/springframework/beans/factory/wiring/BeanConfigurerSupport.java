// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非符合许可证规定。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权性还是特定用途的适用性。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.wiring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 用于在对象上进行依赖注入（无论它们是如何创建的）的方便的基类，通常由 AspectJ aspects 继承。
 *
 * <p>子类可能还需要自定义元数据解析策略，在 {@link BeanWiringInfoResolver} 接口中。默认实现查找与完全限定类名相同的 bean。（这是如果未使用 '{@code id}' 属性时 Spring XML 文件中 bean 的默认名称。）
 *
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @since 2.0
 * @see #setBeanWiringInfoResolver
 * @see ClassNameBeanWiringInfoResolver
 */
public class BeanConfigurerSupport implements BeanFactoryAware, InitializingBean, DisposableBean {

    /**
     * 供子类使用的 Logger。
     */
    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private volatile BeanWiringInfoResolver beanWiringInfoResolver;

    @Nullable
    private volatile ConfigurableListableBeanFactory beanFactory;

    /**
     * 设置要使用的 {@link BeanWiringInfoResolver}。
     * <p>默认行为是查找与类名相同的bean。
     * 作为替代方案，考虑使用注解驱动的bean连接。
     * @see ClassNameBeanWiringInfoResolver
     * @see org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver
     */
    public void setBeanWiringInfoResolver(BeanWiringInfoResolver beanWiringInfoResolver) {
        Assert.notNull(beanWiringInfoResolver, "BeanWiringInfoResolver must not be null");
        this.beanWiringInfoResolver = beanWiringInfoResolver;
    }

    /**
     * 设置在此方面必须配置 Bean 的 {@link BeanFactory}。
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory clbf)) {
            throw new IllegalArgumentException("Bean configurer aspect needs to run in a ConfigurableListableBeanFactory: " + beanFactory);
        }
        this.beanFactory = clbf;
        if (this.beanWiringInfoResolver == null) {
            this.beanWiringInfoResolver = createDefaultBeanWiringInfoResolver();
        }
    }

    /**
     * 创建默认的BeanWiringInfoResolver，用于在未明确指定的情况下使用。
     * <p>默认实现构建一个{@link ClassNameBeanWiringInfoResolver}。
     * @return 默认的BeanWiringInfoResolver（决不为null）
     */
    @Nullable
    protected BeanWiringInfoResolver createDefaultBeanWiringInfoResolver() {
        return new ClassNameBeanWiringInfoResolver();
    }

    /**
     * 检查是否已设置了 {@link BeanFactory}。
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.beanFactory, "BeanFactory must be set");
    }

    /**
     * 在容器销毁时释放对 {@link BeanFactory} 和
     * {@link BeanWiringInfoResolver} 的引用。
     */
    @Override
    public void destroy() {
        this.beanFactory = null;
        this.beanWiringInfoResolver = null;
    }

    /**
     * 配置 Bean 实例。
     * <p>子类可以覆盖此方法以提供自定义配置逻辑。
     * 通常由切面调用，用于所有匹配切点的 Bean 实例。
     * @param beanInstance 要配置的 Bean 实例（不得为 <b>空</b>）
     */
    public void configureBean(Object beanInstance) {
        if (this.beanFactory == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("BeanFactory has not been set on " + ClassUtils.getShortName(getClass()) + ": " + "Make sure this configurer runs in a Spring container. Unable to configure bean of type [" + ClassUtils.getDescriptiveType(beanInstance) + "]. Proceeding without injection.");
            }
            return;
        }
        BeanWiringInfoResolver bwiResolver = this.beanWiringInfoResolver;
        Assert.state(bwiResolver != null, "No BeanWiringInfoResolver available");
        BeanWiringInfo bwi = bwiResolver.resolveWiringInfo(beanInstance);
        if (bwi == null) {
            // 跳过该 Bean，如果没有提供连接信息。
            return;
        }
        ConfigurableListableBeanFactory beanFactory = this.beanFactory;
        Assert.state(beanFactory != null, "No BeanFactory available");
        try {
            String beanName = bwi.getBeanName();
            if (bwi.indicatesAutowiring() || (bwi.isDefaultBeanName() && beanName != null && !beanFactory.containsBean(beanName))) {
                // 执行自动装配（同时应用标准的工厂/后处理器回调）。
                beanFactory.autowireBeanProperties(beanInstance, bwi.getAutowireMode(), bwi.getDependencyCheck());
                beanFactory.initializeBean(beanInstance, (beanName != null ? beanName : ""));
            } else {
                // 根据指定的bean定义执行显式绑定。
                beanFactory.configureBean(beanInstance, (beanName != null ? beanName : ""));
            }
        } catch (BeanCreationException ex) {
            Throwable rootCause = ex.getMostSpecificCause();
            if (rootCause instanceof BeanCurrentlyInCreationException bce) {
                String bceBeanName = bce.getBeanName();
                if (bceBeanName != null && beanFactory.isCurrentlyInCreation(bceBeanName)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed to create target bean '" + bce.getBeanName() + "' while configuring object of type [" + beanInstance.getClass().getName() + "] - probably due to a circular reference. This is a common startup situation " + "and usually not fatal. Proceeding without injection. Original exception: " + ex);
                    }
                    return;
                }
            }
            throw ex;
        }
    }
}
