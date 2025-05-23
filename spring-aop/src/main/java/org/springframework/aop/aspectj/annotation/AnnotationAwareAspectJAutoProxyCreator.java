// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者们。
*
* 根据 Apache 许可证 2.0 版（"许可证"）授权；
* 除非适用法律要求或经书面同意，否则不得使用此文件，除非符合许可证。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律规定或书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何形式的明示或暗示保证，无论是关于其适销性、适用性还是其他。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.aop.aspectj.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link AspectJAwareAdvisorAutoProxyCreator} 的子类，用于处理当前应用程序上下文中的所有 AspectJ 注解切面以及 Spring 切面。
 *
 * <p>任何被 AspectJ 注解的类都将自动被识别，并且如果 Spring AOP 的基于代理的模型能够应用它，其建议将被应用。这包括方法执行连接点。
 *
 * <p>如果使用了 &lt;aop:include&gt; 元素，则只有名称与包含模式匹配的 @AspectJ 实例将被视为定义用于 Spring 自动代理的切面。
 *
 * <p>Spring 切面的处理遵循在 {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator} 中建立的规则。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory
 */
@SuppressWarnings("serial")
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {

    @Nullable
    private List<Pattern> includePatterns;

    @Nullable
    private AspectJAdvisorFactory aspectJAdvisorFactory;

    @Nullable
    private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;

    /**
     * 设置一个正则表达式模式列表，用于匹配符合条件的@AspectJ bean名称。
     * <p>默认情况下，将所有@AspectJ bean视为符合条件的。
     */
    public void setIncludePatterns(List<String> patterns) {
        this.includePatterns = new ArrayList<>(patterns.size());
        for (String patternText : patterns) {
            this.includePatterns.add(Pattern.compile(patternText));
        }
    }

    public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
        Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
        this.aspectJAdvisorFactory = aspectJAdvisorFactory;
    }

    @Override
    protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.initBeanFactory(beanFactory);
        if (this.aspectJAdvisorFactory == null) {
            this.aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
        }
        this.aspectJAdvisorsBuilder = new BeanFactoryAspectJAdvisorsBuilderAdapter(beanFactory, this.aspectJAdvisorFactory);
    }

    @Override
    protected List<Advisor> findCandidateAdvisors() {
        // 根据超类规则添加所有找到的 Spring 通知器。
        List<Advisor> advisors = super.findCandidateAdvisors();
        // 为所有 AspectJ 方面在 Bean 工厂中构建顾问。
        if (this.aspectJAdvisorsBuilder != null) {
            advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
        }
        return advisors;
    }

    @Override
    protected boolean isInfrastructureClass(Class<?> beanClass) {
        // 之前我们在构造函数中设置了 setProxyTargetClass(true)，但这太
        // 我们不再选择实现具有广泛影响。相反，我们现在通过覆盖isInfrastructureClass方法来避免代理。
        // aspects。我对这一点并不完全满意，因为没有充分的理由不支持。
        // 向方面提供建议，除了它会导致建议调用通过一个
        // 代理，并且如果该方面实现例如Ordered接口，它将
        // 通过该接口代理，并在运行时失败，因为建议方法（advice method）不存在或不正确。
        // 在接口上定义的。我们可能放松关于
        // 未来不再建议方面。
        return (super.isInfrastructureClass(beanClass) || (this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(beanClass)));
    }

    /**
     * 检查给定的方面（Aspect）Bean是否适合自动代理。
     * <p>如果没有使用任何 &lt;aop:include&gt; 元素，则 "includePatterns" 将为 {@code null}，所有Bean都将被包括。如果 "includePatterns" 非空，则必须匹配其中一个模式。
     */
    protected boolean isEligibleAspectBean(String beanName) {
        if (this.includePatterns == null) {
            return true;
        } else {
            for (Pattern pattern : this.includePatterns) {
                if (pattern.matcher(beanName).matches()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 该类是BeanFactoryAspectJAdvisorsBuilderAdapter的子类，它将职责委托给周围的AnnotationAwareAspectJAutoProxyCreator功能。
     */
    private class BeanFactoryAspectJAdvisorsBuilderAdapter extends BeanFactoryAspectJAdvisorsBuilder {

        public BeanFactoryAspectJAdvisorsBuilderAdapter(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
            super(beanFactory, advisorFactory);
        }

        @Override
        protected boolean isEligibleBean(String beanName) {
            return AnnotationAwareAspectJAutoProxyCreator.this.isEligibleAspectBean(beanName);
        }
    }
}
