// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.aot;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.aot.AotServices.Source;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.log.LogMessage;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 工厂类，用于创建一个针对 {@link BeanDefinitionMethodGenerator} 实例的
 * 用于 {@link RegisteredBean} 的。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanDefinitionMethodGenerator
 * @see #getBeanDefinitionMethodGenerator(RegisteredBean)
 */
class BeanDefinitionMethodGeneratorFactory {

    private static final Log logger = LogFactory.getLog(BeanDefinitionMethodGeneratorFactory.class);

    private final AotServices<BeanRegistrationAotProcessor> aotProcessors;

    private final AotServices<BeanRegistrationExcludeFilter> excludeFilters;

    /**
     * 创建一个新的由给定的 {@link ConfigurableListableBeanFactory} 支持的 {@link BeanDefinitionMethodGeneratorFactory}。
     * @param beanFactory 要使用的bean工厂
     */
    BeanDefinitionMethodGeneratorFactory(ConfigurableListableBeanFactory beanFactory) {
        this(AotServices.factoriesAndBeans(beanFactory));
    }

    /**
     * 创建一个新的由给定的 {@link AotServices.Loader} 支持的 {@link BeanDefinitionMethodGeneratorFactory}。
     * @param loader 要使用的 AOT 服务加载器
     */
    BeanDefinitionMethodGeneratorFactory(AotServices.Loader loader) {
        this.aotProcessors = loader.load(BeanRegistrationAotProcessor.class);
        this.excludeFilters = loader.load(BeanRegistrationExcludeFilter.class);
        for (BeanRegistrationExcludeFilter excludeFilter : this.excludeFilters) {
            if (this.excludeFilters.getSource(excludeFilter) == Source.BEAN_FACTORY) {
                Assert.state(excludeFilter instanceof BeanRegistrationAotProcessor || excludeFilter instanceof BeanFactoryInitializationAotProcessor, () -> "BeanRegistrationExcludeFilter bean of type %s must also implement an AOT processor interface".formatted(excludeFilter.getClass().getName()));
            }
        }
    }

    /**
     * 返回一个针对给定指定属性名的 {@link BeanDefinitionMethodGenerator}，或者如果注册的bean被一个 {@link BeanRegistrationExcludeFilter} 排除，则返回 {@code null}。结果中的 {@link BeanDefinitionMethodGenerator} 将包含所有由 {@link BeanRegistrationAotProcessor} 提供的贡献。
     * @param registeredBean 注册的bean
     * @param currentPropertyName 此bean所属的属性名
     * @return 一个新的 {@link BeanDefinitionMethodGenerator} 实例或 {@code null}
     */
    @Nullable
    BeanDefinitionMethodGenerator getBeanDefinitionMethodGenerator(RegisteredBean registeredBean, @Nullable String currentPropertyName) {
        if (isExcluded(registeredBean)) {
            return null;
        }
        List<BeanRegistrationAotContribution> contributions = getAotContributions(registeredBean);
        return new BeanDefinitionMethodGenerator(this, registeredBean, currentPropertyName, contributions);
    }

    /**
     * 返回一个针对给定 {@link RegisteredBean} 的 {@link BeanDefinitionMethodGenerator}，或者在注册的 bean 被一个 {@link BeanRegistrationExcludeFilter} 排除时返回 {@code null}。结果将包含所有由 {@link BeanRegistrationAotProcessor} 提供的贡献。
     * @param registeredBean 注册的 bean
     * @return 一个新的 {@link BeanDefinitionMethodGenerator} 实例或 {@code null}
     */
    @Nullable
    BeanDefinitionMethodGenerator getBeanDefinitionMethodGenerator(RegisteredBean registeredBean) {
        return getBeanDefinitionMethodGenerator(registeredBean, null);
    }

    private boolean isExcluded(RegisteredBean registeredBean) {
        if (isImplicitlyExcluded(registeredBean)) {
            return true;
        }
        for (BeanRegistrationExcludeFilter excludeFilter : this.excludeFilters) {
            if (excludeFilter.isExcludedFromAotProcessing(registeredBean)) {
                logger.trace(LogMessage.format("Excluding registered bean '%s' from bean factory %s due to %s", registeredBean.getBeanName(), ObjectUtils.identityToString(registeredBean.getBeanFactory()), excludeFilter.getClass().getName()));
                return true;
            }
        }
        return false;
    }

    private boolean isImplicitlyExcluded(RegisteredBean registeredBean) {
        Class<?> beanClass = registeredBean.getBeanClass();
        if (BeanFactoryInitializationAotProcessor.class.isAssignableFrom(beanClass)) {
            return true;
        }
        if (BeanRegistrationAotProcessor.class.isAssignableFrom(beanClass)) {
            BeanRegistrationAotProcessor processor = this.aotProcessors.findByBeanName(registeredBean.getBeanName());
            return (processor == null || processor.isBeanExcludedFromAotProcessing());
        }
        return false;
    }

    private List<BeanRegistrationAotContribution> getAotContributions(RegisteredBean registeredBean) {
        String beanName = registeredBean.getBeanName();
        List<BeanRegistrationAotContribution> contributions = new ArrayList<>();
        for (BeanRegistrationAotProcessor aotProcessor : this.aotProcessors) {
            BeanRegistrationAotContribution contribution = aotProcessor.processAheadOfTime(registeredBean);
            if (contribution != null) {
                logger.trace(LogMessage.format("Adding bean registration AOT contribution %S from %S to '%S'", contribution.getClass().getName(), aotProcessor.getClass().getName(), beanName));
                contributions.add(contribution);
            }
        }
        return contributions;
    }
}
