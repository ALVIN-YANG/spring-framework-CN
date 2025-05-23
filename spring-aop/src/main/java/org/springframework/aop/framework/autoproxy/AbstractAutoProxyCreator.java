// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）授权；
除非遵守许可证，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为还是适销性。
有关许可的具体语言管理权限和限制，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyProcessorSupport;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.SmartClassLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 *  实现{@link org.springframework.beans.factory.config.BeanPostProcessor}，为每个符合条件的bean包装一个AOP代理，在调用bean本身之前委托给指定的拦截器。
 *
 * <p>此类区分了“通用”拦截器：为所有创建的代理共享，以及“特定”拦截器：每个bean实例唯一。不需要任何通用拦截器。如果有，它们将通过interceptorNames属性设置。与{@link org.springframework.aop.framework.ProxyFactoryBean}类似，当前工厂中使用的拦截器名称而不是bean引用，以便正确处理原型顾问和拦截器：例如，支持有状态的混合。支持任何类型的建议作为“interceptorNames”条目的设置。
 *
 * <p>这种自动代理特别有用，如果有大量bean需要被类似的代理包装，即委托给相同的拦截器。而不是为x个目标bean重复x次代理定义，您可以通过将一个这样的后处理器注册到bean工厂来实现相同的效果。
 *
 * <p>子类可以应用任何策略来决定是否要代理bean，例如通过类型、通过名称、通过定义细节等。它们还可以返回应仅应用于特定bean实例的附加拦截器。一个简单的具体实现是{@link BeanNameAutoProxyCreator}，通过给定的名称识别要代理的bean。
 *
 * <p>可以使用任意数量的{@link TargetSourceCreator}实现来创建自定义的目标源：例如，用于池化原型对象。即使没有建议，只要TargetSourceCreator指定了自定义的{@link org.springframework.aop.TargetSource}，就会发生自动代理。如果没有设置TargetSourceCreators，或者没有匹配项，默认将使用{@link org.springframework.aop.target.SingletonTargetSource}来包装目标bean实例。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 13.10.2003
 * @see #setInterceptorNames
 * @see #getAdvicesAndAdvisorsForBean
 * @see BeanNameAutoProxyCreator
 * @see DefaultAdvisorAutoProxyCreator
 */
@SuppressWarnings("serial")
public abstract class AbstractAutoProxyCreator extends ProxyProcessorSupport implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware {

    /**
     * 方便子类使用的常量：表示"不代理"的返回值。
     * @see #getAdvicesAndAdvisorsForBean
     */
    @Nullable
    protected static final Object[] DO_NOT_PROXY = null;

    /**
     * 便利常量，用于子类：表示“没有额外拦截器，仅包含公共拦截器”的返回值。
     * @see #getAdvicesAndAdvisorsForBean
     */
    protected static final Object[] PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS = new Object[0];

    /**
     * 为子类提供可用的 Logger。
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * 默认使用全局的 AdvisorAdapterRegistry。
     */
    private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

    /**
     * 表示代理是否应该被冻结。重写自父类以防止配置过早冻结。
     */
    private boolean freezeProxy = false;

    /**
     * 默认情况下没有公共拦截器。
     */
    private String[] interceptorNames = new String[0];

    private boolean applyCommonInterceptorsFirst = true;

    @Nullable
    private TargetSourceCreator[] customTargetSourceCreators;

    @Nullable
    private BeanFactory beanFactory;

    private final Set<String> targetSourcedBeans = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    private final Map<Object, Object> earlyProxyReferences = new ConcurrentHashMap<>(16);

    private final Map<Object, Class<?>> proxyTypes = new ConcurrentHashMap<>(16);

    private final Map<Object, Boolean> advisedBeans = new ConcurrentHashMap<>(256);

    /**
     * 设置代理是否应该被冻结，防止在创建后添加任何建议到它。
     * <p>从超类中重写，以防止在创建代理之前冻结代理配置。
     */
    @Override
    public void setFrozen(boolean frozen) {
        this.freezeProxy = frozen;
    }

    @Override
    public boolean isFrozen() {
        return this.freezeProxy;
    }

    /**
     * 指定要使用的 {@link AdvisorAdapterRegistry}。
     * <p>默认为全局的 {@link AdvisorAdapterRegistry}。
     * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
     */
    public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
        this.advisorAdapterRegistry = advisorAdapterRegistry;
    }

    /**
     * 设置要按此顺序应用的自定义 {@code TargetSourceCreators}。
     * 如果列表为空，或者它们都返回 null，将为每个 bean 创建一个 {@link SingletonTargetSource}。
     * <p>注意，即使对于没有找到任何通知或顾问的目标 bean，目标源创建器也会生效。如果一个 {@code TargetSourceCreator}
     * 为特定的 bean 返回一个 {@link TargetSource}，则该 bean 将在任何情况下都会被代理。
     * <p>只有当此后处理器在 {@link BeanFactory} 中使用，并且其 {@link BeanFactoryAware} 回调被触发时，才能调用
     * {@code TargetSourceCreators}。
     * @param targetSourceCreators 要列表的 {@code TargetSourceCreators}。
     * 排序很重要：将从第一个匹配的 {@code TargetSourceCreator}（即第一个返回非 null 的）返回的
     * {@code TargetSource} 将被使用。
     */
    public void setCustomTargetSourceCreators(TargetSourceCreator... targetSourceCreators) {
        this.customTargetSourceCreators = targetSourceCreators;
    }

    /**
     * 设置通用拦截器。这些必须是在当前工厂中的bean名称。
     * 它们可以是Spring支持的任何类型的建议或顾问。
     * <p>如果未设置此属性，则不会有通用拦截器。
     * 如果我们只想使用“特定”拦截器，例如匹配顾问，这是完全有效的。
     */
    public void setInterceptorNames(String... interceptorNames) {
        this.interceptorNames = interceptorNames;
    }

    /**
     * 设置是否应在特定于bean的拦截器之前应用公共拦截器。
     * 默认值为 "true"；否则，将首先应用特定于bean的拦截器。
     */
    public void setApplyCommonInterceptorsFirst(boolean applyCommonInterceptorsFirst) {
        this.applyCommonInterceptorsFirst = applyCommonInterceptorsFirst;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 返回所属的 {@link BeanFactory}。
     * 可能是 {@code null}，因为此后处理器不需要属于一个bean工厂。
     */
    @Nullable
    protected BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    @Nullable
    public Class<?> predictBeanType(Class<?> beanClass, String beanName) {
        if (this.proxyTypes.isEmpty()) {
            return null;
        }
        Object cacheKey = getCacheKey(beanClass, beanName);
        return this.proxyTypes.get(cacheKey);
    }

    @Override
    public Class<?> determineBeanType(Class<?> beanClass, String beanName) {
        Object cacheKey = getCacheKey(beanClass, beanName);
        Class<?> proxyType = this.proxyTypes.get(cacheKey);
        if (proxyType == null) {
            TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
            if (targetSource != null) {
                if (StringUtils.hasLength(beanName)) {
                    this.targetSourcedBeans.add(beanName);
                }
            } else {
                targetSource = EmptyTargetSource.forClass(beanClass);
            }
            Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
            if (specificInterceptors != DO_NOT_PROXY) {
                this.advisedBeans.put(cacheKey, Boolean.TRUE);
                proxyType = createProxyClass(beanClass, beanName, specificInterceptors, targetSource);
                this.proxyTypes.put(cacheKey, proxyType);
            }
        }
        return (proxyType != null ? proxyType : beanClass);
    }

    @Override
    @Nullable
    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) {
        return null;
    }

    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) {
        Object cacheKey = getCacheKey(bean.getClass(), beanName);
        this.earlyProxyReferences.put(cacheKey, bean);
        return wrapIfNecessary(bean, beanName, cacheKey);
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        Object cacheKey = getCacheKey(beanClass, beanName);
        if (!StringUtils.hasLength(beanName) || !this.targetSourcedBeans.contains(beanName)) {
            if (this.advisedBeans.containsKey(cacheKey)) {
                return null;
            }
            if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
                this.advisedBeans.put(cacheKey, Boolean.FALSE);
                return null;
            }
        }
        // 在此处创建代理，如果我们有一个自定义的 TargetSource。
        // 抑制对目标bean的无用默认实例化：
        // TargetSource 将以自定义的方式处理目标实例。
        TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
        if (targetSource != null) {
            if (StringUtils.hasLength(beanName)) {
                this.targetSourcedBeans.add(beanName);
            }
            Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
            Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
            this.proxyTypes.put(cacheKey, proxy.getClass());
            return proxy;
        }
        return null;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
        // 跳过 postProcessPropertyValues
        return pvs;
    }

    /**
     * 如果子类识别出该Bean需要代理，则创建一个配置好的拦截器代理。
     * @see #getAdvicesAndAdvisorsForBean
     */
    @Override
    public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
        if (bean != null) {
            Object cacheKey = getCacheKey(bean.getClass(), beanName);
            if (this.earlyProxyReferences.remove(cacheKey) != bean) {
                return wrapIfNecessary(bean, beanName, cacheKey);
            }
        }
        return bean;
    }

    /**
     * 为给定的Bean类和Bean名称构建一个缓存键。
     * <p>注意：从4.2.3版本开始，此实现不再返回拼接的类/名称字符串，而是返回最有效的缓存键：
     * 一个普通的Bean名称，如果是FactoryBean，则在前面加上{@link BeanFactory#FACTORY_BEAN_PREFIX}；
     * 如果没有指定Bean名称，则直接使用给定的Bean类。
     * @param beanClass Bean类
     * @param beanName Bean名称
     * @return 给定类和名称的缓存键
     */
    protected Object getCacheKey(Class<?> beanClass, @Nullable String beanName) {
        if (StringUtils.hasLength(beanName)) {
            return (FactoryBean.class.isAssignableFrom(beanClass) ? BeanFactory.FACTORY_BEAN_PREFIX + beanName : beanName);
        } else {
            return beanClass;
        }
    }

    /**
     * 如果需要，则包装给定的bean，即如果bean有资格被代理。
     * @param bean 原始的bean实例
     * @param beanName bean的名称
     * @param cacheKey 用于元数据访问的缓存键
     * @return 包裹bean的代理，或直接返回原始bean实例
     */
    protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
        if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
            return bean;
        }
        if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
            return bean;
        }
        if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
            this.advisedBeans.put(cacheKey, Boolean.FALSE);
            return bean;
        }
        // 如果存在建议，则创建代理。
        Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
        if (specificInterceptors != DO_NOT_PROXY) {
            this.advisedBeans.put(cacheKey, Boolean.TRUE);
            Object proxy = createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
            this.proxyTypes.put(cacheKey, proxy.getClass());
            return proxy;
        }
        this.advisedBeans.put(cacheKey, Boolean.FALSE);
        return bean;
    }

    /**
     * 返回给定的 Bean 类是否表示一个不应被代理的基础类。
     * <p>默认实现将 Advices、Advisors 和 AopInfrastructureBeans 视为基础类。
     * @param beanClass Bean 的类
     * @return Bean 是否表示一个基础类
     * @see org.aopalliance.aop.Advice
     * @see org.springframework.aop.Advisor
     * @see org.springframework.aop.framework.AopInfrastructureBean
     * @see #shouldSkip
     */
    protected boolean isInfrastructureClass(Class<?> beanClass) {
        boolean retVal = Advice.class.isAssignableFrom(beanClass) || Pointcut.class.isAssignableFrom(beanClass) || Advisor.class.isAssignableFrom(beanClass) || AopInfrastructureBean.class.isAssignableFrom(beanClass);
        if (retVal && logger.isTraceEnabled()) {
            logger.trace("Did not attempt to auto-proxy infrastructure class [" + beanClass.getName() + "]");
        }
        return retVal;
    }

    /**
     * 子类应重写此方法，如果给定的bean不应该被此后处理器考虑进行自动代理，则返回 {@code true}。
     * <p>有时我们需要避免这种情况发生，例如，如果会导致循环引用，或者如果需要保留现有的目标实例。
     * 此实现返回 {@code false}，除非bean的名称根据 {@code AutowireCapableBeanFactory} 约定指示为“原始实例”。
     * @param beanClass bean的类
     * @param beanName bean的名称
     * @return 是否跳过给定的bean
     * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX
     */
    protected boolean shouldSkip(Class<?> beanClass, String beanName) {
        return AutoProxyUtils.isOriginalInstance(beanName, beanClass);
    }

    /**
     * 为Bean实例创建目标源。如果设置了任何目标源创建器，则使用它们。
     * 如果没有使用自定义目标源，则返回{@code null}。
     * <p>此实现使用“customTargetSourceCreators”属性。
     * 子类可以覆盖此方法以使用不同的机制。
     * @param beanClass 要为该Bean创建目标源的目标类
     * @param beanName Bean的名称
     * @return 该Bean的目标源
     * @see #setCustomTargetSourceCreators
     */
    @Nullable
    protected TargetSource getCustomTargetSource(Class<?> beanClass, String beanName) {
        // 我们不能为直接注册的单例创建复杂的目标源。
        if (this.customTargetSourceCreators != null && this.beanFactory != null && this.beanFactory.containsBean(beanName)) {
            for (TargetSourceCreator tsc : this.customTargetSourceCreators) {
                TargetSource ts = tsc.getTargetSource(beanClass, beanName);
                if (ts != null) {
                    // 找到了匹配的TargetSource。
                    if (logger.isTraceEnabled()) {
                        logger.trace("TargetSourceCreator [" + tsc + "] found custom TargetSource for bean with name '" + beanName + "'");
                    }
                    return ts;
                }
            }
        }
        // 未找到自定义的TargetSource。
        return null;
    }

    /**
     * 为指定的Bean创建一个AOP代理。
     * @param beanClass Bean的类
     * @param beanName Bean的名称
     * @param specificInterceptors 特定于该Bean的拦截器集合（可能为空，但不能为null）
     * @param targetSource 代理的目标源，已经预先配置好以访问该Bean
     * @return 该Bean的AOP代理
     * @see #buildAdvisors
     */
    protected Object createProxy(Class<?> beanClass, @Nullable String beanName, @Nullable Object[] specificInterceptors, TargetSource targetSource) {
        return buildProxy(beanClass, beanName, specificInterceptors, targetSource, false);
    }

    private Class<?> createProxyClass(Class<?> beanClass, @Nullable String beanName, @Nullable Object[] specificInterceptors, TargetSource targetSource) {
        return (Class<?>) buildProxy(beanClass, beanName, specificInterceptors, targetSource, true);
    }

    private Object buildProxy(Class<?> beanClass, @Nullable String beanName, @Nullable Object[] specificInterceptors, TargetSource targetSource, boolean classOnly) {
        if (this.beanFactory instanceof ConfigurableListableBeanFactory clbf) {
            AutoProxyUtils.exposeTargetClass(clbf, beanName, beanClass);
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.copyFrom(this);
        if (proxyFactory.isProxyTargetClass()) {
            // 显式处理 JDK 代理目标和使用lambda表达式（用于引入建议场景）
            if (Proxy.isProxyClass(beanClass) || ClassUtils.isLambdaClass(beanClass)) {
                // 必须允许进行介绍；不能仅仅将接口设置为代理的接口。
                for (Class<?> ifc : beanClass.getInterfaces()) {
                    proxyFactory.addInterface(ifc);
                }
            }
        } else {
            // 未强制执行proxyTargetClass标志，让我们应用我们的默认检查...
            if (shouldProxyTargetClass(beanClass, beanName)) {
                proxyFactory.setProxyTargetClass(true);
            } else {
                evaluateProxyInterfaces(beanClass, proxyFactory);
            }
        }
        Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
        proxyFactory.addAdvisors(advisors);
        proxyFactory.setTargetSource(targetSource);
        customizeProxyFactory(proxyFactory);
        proxyFactory.setFrozen(this.freezeProxy);
        if (advisorsPreFiltered()) {
            proxyFactory.setPreFiltered(true);
        }
        // 如果被覆盖的类加载器中未本地加载 bean 类，则使用原始 ClassLoader。
        ClassLoader classLoader = getProxyClassLoader();
        if (classLoader instanceof SmartClassLoader smartClassLoader && classLoader != beanClass.getClassLoader()) {
            classLoader = smartClassLoader.getOriginalClassLoader();
        }
        return (classOnly ? proxyFactory.getProxyClass(classLoader) : proxyFactory.getProxy(classLoader));
    }

    /**
     * 判断给定的bean是否应该使用其目标类而不是其接口进行代理。
     * <p>检查相应bean定义的{@link AutoProxyUtils#PRESERVE_TARGET_CLASS_ATTRIBUTE "preserveTargetClass"属性}
     * @param beanClass bean的类
     * @param beanName bean的名称
     * @return 给定的bean是否应该使用其目标类进行代理
     * @see AutoProxyUtils#shouldProxyTargetClass
     */
    protected boolean shouldProxyTargetClass(Class<?> beanClass, @Nullable String beanName) {
        return (this.beanFactory instanceof ConfigurableListableBeanFactory clbf && AutoProxyUtils.shouldProxyTargetClass(clbf, beanName));
    }

    /**
     * 返回子类返回的顾问是否已经预先过滤以匹配目标类，允许在构建面向切面编程（AOP）调用时的顾问链时跳过ClassFilter检查。
     * <p>默认值为{@code false}。如果子类将始终返回预先过滤的顾问，则可以覆盖此方法。
     * @return 是否顾问已预先过滤
     * @see #getAdvicesAndAdvisorsForBean
     * @see org.springframework.aop.framework.Advised#setPreFiltered
     */
    protected boolean advisorsPreFiltered() {
        return false;
    }

    /**
     * 确定给定 Bean 的顾问，包括特定的拦截器以及通用拦截器，所有都适配到 Advisor 接口。
     * @param beanName Bean 的名称
     * @param specificInterceptors 专门针对此 Bean 的拦截器集合（可能为空，但不能为 null）
     * @return 给定 Bean 的 Advisor 列表
     */
    protected Advisor[] buildAdvisors(@Nullable String beanName, @Nullable Object[] specificInterceptors) {
        // 正确处理原型...
        Advisor[] commonInterceptors = resolveInterceptorNames();
        List<Object> allInterceptors = new ArrayList<>();
        if (specificInterceptors != null) {
            if (specificInterceptors.length > 0) {
                // specificInterceptors 可能等于 PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS
                allInterceptors.addAll(Arrays.asList(specificInterceptors));
            }
            if (commonInterceptors.length > 0) {
                if (this.applyCommonInterceptorsFirst) {
                    allInterceptors.addAll(0, Arrays.asList(commonInterceptors));
                } else {
                    allInterceptors.addAll(Arrays.asList(commonInterceptors));
                }
            }
        }
        if (logger.isTraceEnabled()) {
            int nrOfCommonInterceptors = commonInterceptors.length;
            int nrOfSpecificInterceptors = (specificInterceptors != null ? specificInterceptors.length : 0);
            logger.trace("Creating implicit proxy for bean '" + beanName + "' with " + nrOfCommonInterceptors + " common interceptors and " + nrOfSpecificInterceptors + " specific interceptors");
        }
        Advisor[] advisors = new Advisor[allInterceptors.size()];
        for (int i = 0; i < allInterceptors.size(); i++) {
            advisors[i] = this.advisorAdapterRegistry.wrap(allInterceptors.get(i));
        }
        return advisors;
    }

    /**
     * 将指定的拦截器名称解析为Advisor对象。
     * @see #setInterceptorNames
     */
    private Advisor[] resolveInterceptorNames() {
        BeanFactory bf = this.beanFactory;
        ConfigurableBeanFactory cbf = (bf instanceof ConfigurableBeanFactory _cbf ? _cbf : null);
        List<Advisor> advisors = new ArrayList<>();
        for (String beanName : this.interceptorNames) {
            if (cbf == null || !cbf.isCurrentlyInCreation(beanName)) {
                Assert.state(bf != null, "BeanFactory required for resolving interceptor names");
                Object next = bf.getBean(beanName);
                advisors.add(this.advisorAdapterRegistry.wrap(next));
            }
        }
        return advisors.toArray(new Advisor[0]);
    }

    /**
     * 子类可以选择实现此方法：例如，用于更改暴露的接口。
     * <p>默认实现为空。
     * @param proxyFactory 一个已配置好 TargetSource 和接口的 ProxyFactory，该方法返回后将立即使用此工厂创建代理
     */
    protected void customizeProxyFactory(ProxyFactory proxyFactory) {
    }

    /**
     * 返回给定的bean是否需要代理，以及要应用哪些额外的建议（例如AOP Alliance拦截器）和顾问。
     * @param beanClass 要建议的bean的类
     * @param beanName bean的名称
     * @param customTargetSource 由getCustomTargetSource方法返回的TargetSource：可能被忽略。
     * 如果没有使用自定义目标源，则为null。
     * @return 一个数组，包含特定bean的额外拦截器；
     * 或者一个空数组，表示没有额外的拦截器但只有常见的拦截器；
     * 或者null，表示根本不需要代理，甚至不包括常见的拦截器。
     * 请参阅常量DO_NOT_PROXY和PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS。
     * @throws BeansException 如果发生错误
     * @see #DO_NOT_PROXY
     * @see #PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS
     */
    @Nullable
    protected abstract Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, @Nullable TargetSource customTargetSource) throws BeansException;
}
