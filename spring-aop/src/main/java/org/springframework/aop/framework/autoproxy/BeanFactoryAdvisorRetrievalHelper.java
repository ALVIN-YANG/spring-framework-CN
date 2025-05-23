// 翻译完成 glm-4-flash
/*版权所有 2002-2023，原作者或作者。

根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下网址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“原样”提供，
不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途适用性和非侵权性的保证。
有关许可的具体语言、权限和限制，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 从BeanFactory中检索标准Spring Advisor的辅助工具，用于与自动代理一起使用。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see AbstractAdvisorAutoProxyCreator
 */
public class BeanFactoryAdvisorRetrievalHelper {

    private static final Log logger = LogFactory.getLog(BeanFactoryAdvisorRetrievalHelper.class);

    private final ConfigurableListableBeanFactory beanFactory;

    @Nullable
    private volatile String[] cachedAdvisorBeanNames;

    /**
     * 为给定的BeanFactory创建一个新的BeanFactoryAdvisorRetrievalHelper实例。
     * @param beanFactory 要扫描的ListableBeanFactory
     */
    public BeanFactoryAdvisorRetrievalHelper(ConfigurableListableBeanFactory beanFactory) {
        Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
        this.beanFactory = beanFactory;
    }

    /**
     * 在当前bean工厂中查找所有符合条件的Advisor bean，
     * 忽略FactoryBean，并排除当前正在创建的bean。
     * @return 包含所有{@link org.springframework.aop.Advisor} bean的列表
     * @see #isEligibleBean
     */
    public List<Advisor> findAdvisorBeans() {
        // 确定顾问 Bean 名称列表，如果尚未缓存。
        String[] advisorNames = this.cachedAdvisorBeanNames;
        if (advisorNames == null) {
            // 在此处不要初始化 FactoryBeans：我们需要保留所有常规的 Bean
            // 未初始化，以便自动代理创建器可以应用它们！
            advisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.beanFactory, Advisor.class, true, false);
            this.cachedAdvisorBeanNames = advisorNames;
        }
        if (advisorNames.length == 0) {
            return new ArrayList<>();
        }
        List<Advisor> advisors = new ArrayList<>();
        for (String name : advisorNames) {
            if (isEligibleBean(name)) {
                if (this.beanFactory.isCurrentlyInCreation(name)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Skipping currently created advisor '" + name + "'");
                    }
                } else {
                    try {
                        advisors.add(this.beanFactory.getBean(name, Advisor.class));
                    } catch (BeanCreationException ex) {
                        Throwable rootCause = ex.getMostSpecificCause();
                        if (rootCause instanceof BeanCurrentlyInCreationException bce) {
                            String bceBeanName = bce.getBeanName();
                            if (bceBeanName != null && this.beanFactory.isCurrentlyInCreation(bceBeanName)) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("Skipping advisor '" + name + "' with dependency on currently created bean: " + ex.getMessage());
                                }
                                // 忽略：表示对我们要进行建议的bean的引用回退。
                                // 我们希望找到除了当前创建的bean本身之外的其他顾问。
                                continue;
                            }
                        }
                        throw ex;
                    }
                }
            }
        }
        return advisors;
    }

    /**
     * 判断给定的名称的切面bean是否合格。
     * <p>默认实现始终返回{@code true}。
     * @param beanName 切面bean的名称
     * @return bean是否合格
     */
    protected boolean isEligibleBean(String beanName) {
        return true;
    }
}
