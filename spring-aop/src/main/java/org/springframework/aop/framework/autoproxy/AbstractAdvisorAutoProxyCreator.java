// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非根据法律要求或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy;

import java.util.List;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 通用自动代理创建器，根据每个bean检测到的顾问为特定bean构建AOP代理。
 *
 * <p>子类可以覆盖 {@link #findCandidateAdvisors()} 方法以返回适用于任何对象的自定义顾问列表。子类还可以覆盖继承的 {@link #shouldSkip} 方法以排除某些对象从自动代理中。
 *
 * <p>需要排序的顾问或建议应该使用 {@link org.springframework.core.annotation.Order @Order} 注解或实现 {@link org.springframework.core.Ordered} 接口。此类使用 {@link AnnotationAwareOrderComparator} 对顾问进行排序。未使用 {@code @Order} 注解或未实现 {@code Ordered} 接口的顾问将被视为无序；它们将以未定义的顺序出现在顾问链的末尾。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #findCandidateAdvisors
 */
@SuppressWarnings("serial")
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

    @Nullable
    private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        if (!(beanFactory instanceof ConfigurableListableBeanFactory clbf)) {
            throw new IllegalArgumentException("AdvisorAutoProxyCreator requires a ConfigurableListableBeanFactory: " + beanFactory);
        }
        initBeanFactory(clbf);
    }

    protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
    }

    @Override
    @Nullable
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {
        List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
        if (advisors.isEmpty()) {
            return DO_NOT_PROXY;
        }
        return advisors.toArray();
    }

    /**
     * 查找所有适用于自动代理此类的合格顾问。
     * @param beanClass 要查找顾问的类
     * @param beanName 当前被代理的bean的名称
     * @return 如果没有切入点或拦截器，则返回空列表，不为null
     * @see #findCandidateAdvisors
     * @see #sortAdvisors
     * @see #extendAdvisors
     */
    protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
        List<Advisor> candidateAdvisors = findCandidateAdvisors();
        List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
        extendAdvisors(eligibleAdvisors);
        if (!eligibleAdvisors.isEmpty()) {
            eligibleAdvisors = sortAdvisors(eligibleAdvisors);
        }
        return eligibleAdvisors;
    }

    /**
     * 查找所有可用于自动代理的候选顾问。
     * @return 返回候选顾问的列表
     */
    protected List<Advisor> findCandidateAdvisors() {
        Assert.state(this.advisorRetrievalHelper != null, "No BeanFactoryAdvisorRetrievalHelper available");
        return this.advisorRetrievalHelper.findAdvisorBeans();
    }

    /**
     * 在给定的候选顾问中搜索，以找到所有可以申请指定bean的顾问。
     * @param candidateAdvisors 候选顾问列表
     * @param beanClass 目标bean的类
     * @param beanName 目标bean的名称
     * @return 可应用的顾问列表
     * @see ProxyCreationContext#getCurrentProxiedBeanName()
     */
    protected List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {
        ProxyCreationContext.setCurrentProxiedBeanName(beanName);
        try {
            return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
        } finally {
            ProxyCreationContext.setCurrentProxiedBeanName(null);
        }
    }

    /**
     * 返回名称为给定名称的Advisor对象是否首先有资格进行代理。
     * @param beanName Advisor对象的名称
     * @return 该对象是否有资格
     */
    protected boolean isEligibleAdvisorBean(String beanName) {
        return true;
    }

    /**
     * /**
     *  根据排序顺序对顾问进行排序。子类可以选择覆盖此方法以自定义排序策略。
     *  @param advisors 顾问的源列表
     *  @return 排序后的顾问列表
     *  @see org.springframework.core.Ordered
     *  @see org.springframework.core.annotation.Order
     *  @see org.springframework.core.annotation.AnnotationAwareOrderComparator
     * /
     */
    protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
        AnnotationAwareOrderComparator.sort(advisors);
        return advisors;
    }

    /**
     * 扩展钩子，子类可以覆盖以注册额外的Advisors，前提是已经获得了排序后的Advisors。
     * <p>默认实现为空。
     * <p>通常用于添加需要后续Advisors暴露的上下文信息的Advisors。
     * @param candidateAdvisors 已经被确定为适用于特定bean的Advisors
     */
    protected void extendAdvisors(List<Advisor> candidateAdvisors) {
    }

    /**
     * 这个自动代理创建器始终返回预过滤的顾问。
     */
    @Override
    protected boolean advisorsPreFiltered() {
        return true;
    }

    /**
     * BeanFactoryAdvisorRetrievalHelper 的子类，该子类委托给周围的 AbstractAdvisorAutoProxyCreator 功能。
     */
    private class BeanFactoryAdvisorRetrievalHelperAdapter extends BeanFactoryAdvisorRetrievalHelper {

        public BeanFactoryAdvisorRetrievalHelperAdapter(ConfigurableListableBeanFactory beanFactory) {
            super(beanFactory);
        }

        @Override
        protected boolean isEligibleBean(String beanName) {
            return AbstractAdvisorAutoProxyCreator.this.isEligibleAdvisorBean(beanName);
        }
    }
}
