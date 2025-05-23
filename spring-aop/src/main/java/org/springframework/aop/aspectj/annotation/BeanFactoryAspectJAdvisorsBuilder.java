// 翻译完成 glm-4-flash
/*版权所有 2002-2024 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可；除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的保证或条件，无论是明示的还是暗示的。有关许可的具体语言，请参阅许可证，了解权限和限制。*/
package org.springframework.aop.aspectj.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于从BeanFactory中检索@AspectJ bean并基于它们构建Spring顾问的辅助类，用于与自动代理一起使用。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see AnnotationAwareAspectJAutoProxyCreator
 */
public class BeanFactoryAspectJAdvisorsBuilder {

    private static final Log logger = LogFactory.getLog(BeanFactoryAspectJAdvisorsBuilder.class);

    private final ListableBeanFactory beanFactory;

    private final AspectJAdvisorFactory advisorFactory;

    @Nullable
    private volatile List<String> aspectBeanNames;

    private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();

    private final Map<String, MetadataAwareAspectInstanceFactory> aspectFactoryCache = new ConcurrentHashMap<>();

    /**
     * 为给定的BeanFactory创建一个新的BeanFactoryAspectJAdvisorsBuilder。
     * @param beanFactory 要扫描的ListableBeanFactory
     */
    public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory) {
        this(beanFactory, new ReflectiveAspectJAdvisorFactory(beanFactory));
    }

    /**
     * 为给定的BeanFactory创建一个新的BeanFactoryAspectJAdvisorsBuilder。
     * @param beanFactory 要扫描的ListableBeanFactory
     * @param advisorFactory 用于构建每个Advisor的AspectJAdvisorFactory
     */
    public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
        Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
        Assert.notNull(advisorFactory, "AspectJAdvisorFactory must not be null");
        this.beanFactory = beanFactory;
        this.advisorFactory = advisorFactory;
    }

    /**
     * 在当前bean工厂中查找被AspectJ注解的aspect beans，并返回代表它们的Spring AOP Advisor列表。
     * <p>为每个AspectJ建议方法创建一个Spring Advisor。
     * @return 包含{@link org.springframework.aop.Advisor} bean的列表
     * @see #isEligibleBean
     */
    public List<Advisor> buildAspectJAdvisors() {
        List<String> aspectNames = this.aspectBeanNames;
        if (aspectNames == null) {
            synchronized (this) {
                aspectNames = this.aspectBeanNames;
                if (aspectNames == null) {
                    List<Advisor> advisors = new ArrayList<>();
                    aspectNames = new ArrayList<>();
                    String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.beanFactory, Object.class, true, false);
                    for (String beanName : beanNames) {
                        if (!isEligibleBean(beanName)) {
                            continue;
                        }
                        // 我们必须小心不要像在这种情况下一样预先实例化 Bean，因为它们
                        // 将被 Spring 容器缓存，但尚未被织入。
                        Class<?> beanType = this.beanFactory.getType(beanName, false);
                        if (beanType == null) {
                            continue;
                        }
                        if (this.advisorFactory.isAspect(beanType)) {
                            try {
                                AspectMetadata amd = new AspectMetadata(beanType, beanName);
                                if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
                                    MetadataAwareAspectInstanceFactory factory = new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
                                    List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
                                    if (this.beanFactory.isSingleton(beanName)) {
                                        this.advisorsCache.put(beanName, classAdvisors);
                                    } else {
                                        this.aspectFactoryCache.put(beanName, factory);
                                    }
                                    advisors.addAll(classAdvisors);
                                } else {
                                    // 针对每个目标或针对当前对象。
                                    if (this.beanFactory.isSingleton(beanName)) {
                                        throw new IllegalArgumentException("Bean with name '" + beanName + "' is a singleton, but aspect instantiation model is not singleton");
                                    }
                                    MetadataAwareAspectInstanceFactory factory = new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
                                    this.aspectFactoryCache.put(beanName, factory);
                                    advisors.addAll(this.advisorFactory.getAdvisors(factory));
                                }
                                aspectNames.add(beanName);
                            } catch (IllegalArgumentException | IllegalStateException | AopConfigException ex) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Ignoring incompatible aspect [" + beanType.getName() + "]: " + ex);
                                }
                            }
                        }
                    }
                    this.aspectBeanNames = aspectNames;
                    return advisors;
                }
            }
        }
        if (aspectNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<Advisor> advisors = new ArrayList<>();
        for (String aspectName : aspectNames) {
            List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
            if (cachedAdvisors != null) {
                advisors.addAll(cachedAdvisors);
            } else {
                MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
                advisors.addAll(this.advisorFactory.getAdvisors(factory));
            }
        }
        return advisors;
    }

    /**
     * 返回指定名称的切面bean是否合格。
     * @param beanName 切面bean的名称
     * @return bean是否合格
     */
    protected boolean isEligibleBean(String beanName) {
        return true;
    }
}
