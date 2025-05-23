// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import org.apache.commons.logging.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.AutowiredPropertyMarker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.StringUtils;
import org.springframework.util.function.ThrowingSupplier;

/**
 * 抽象的Bean工厂超类，实现了默认的Bean创建，具备由`RootBeanDefinition`类指定的全部功能。
 * 除了AbstractBeanFactory的`createBean`方法外，还实现了`org.springframework.beans.factory.config.AutowireCapableBeanFactory`接口。
 *
 * <p>提供Bean创建（包括构造函数解析）、属性填充、连接（包括自动装配）和初始化。处理运行时Bean引用、解析受管理集合、调用初始化方法等。
 * 支持自动装配构造函数、按名称装配属性以及按类型装配属性。
 *
 * <p>子类需要实现的主要模板方法是`resolveDependency(DependencyDescriptor, String, Set, TypeConverter)`，用于自动装配。
 * 对于能够搜索其Bean定义的`org.springframework.beans.factory.ListableBeanFactory`，通常通过这种搜索来实现匹配Bean。
 * 否则，可以实现简化的匹配。
 *
 * <p>请注意，此类不假定或实现Bean定义注册功能。请参阅`DefaultListableBeanFactory`，它实现了`org.springframework.beans.factory.ListableBeanFactory`和`BeanDefinitionRegistry`接口，
 * 分别代表此类工厂的API和SPI视图。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @author Costin Leau
 * @author Chris Beams
 * @author Sam Brannen
 * @author Phillip Webb
 * @since 13.02.2004
 * @see RootBeanDefinition
 * @see DefaultListableBeanFactory
 * @see BeanDefinitionRegistry
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

    /**
     * 创建 Bean 实例的策略。
     */
    private InstantiationStrategy instantiationStrategy;

    /**
     * 方法参数名称的解析策略。
     */
    @Nullable
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 是否自动尝试解决 beans 之间的循环引用。
     */
    private boolean allowCircularReferences = true;

    /**
     * 是否在发生循环引用的情况下，即使注入的bean最终被包装，仍然需要注入原始的bean实例。
     */
    private boolean allowRawInjectionDespiteWrapping = false;

    /**
     * 要在依赖检查和自动装配中忽略的依赖类型，以类对象集合的形式表示：例如，String。默认值为无。
     */
    private final Set<Class<?>> ignoredDependencyTypes = new HashSet<>();

    /**
     * 用于在依赖检查和自动装配时忽略的依赖接口，以 Class 对象的集合形式表示。默认情况下，仅忽略 BeanFactory 接口。
     */
    private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<>();

    /**
     * 当前创建的bean的名称，用于在由用户指定的Supplier回调触发的getBean等调用上隐式注册依赖关系。
     */
    private final NamedThreadLocal<String> currentlyCreatedBean = new NamedThreadLocal<>("Currently created bean");

    /**
     * 未完成 FactoryBean 实例的缓存：FactoryBean 名称到 BeanWrapper 的映射。
     */
    private final ConcurrentMap<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    /**
     * 每个工厂类对应的候选工厂方法的缓存。
     */
    private final ConcurrentMap<Class<?>, Method[]> factoryMethodCandidateCache = new ConcurrentHashMap<>();

    /**
     * 过滤后的属性描述符缓存：Bean 类到属性描述符数组的映射。
     */
    private final ConcurrentMap<Class<?>, PropertyDescriptor[]> filteredPropertyDescriptorsCache = new ConcurrentHashMap<>();

    /**
     * 创建一个新的 AbstractAutowireCapableBeanFactory。
     */
    public AbstractAutowireCapableBeanFactory() {
        super();
        ignoreDependencyInterface(BeanNameAware.class);
        ignoreDependencyInterface(BeanFactoryAware.class);
        ignoreDependencyInterface(BeanClassLoaderAware.class);
        this.instantiationStrategy = new CglibSubclassingInstantiationStrategy();
    }

    /**
     * 创建一个新的AbstractAutowireCapableBeanFactory，使用给定的父BeanFactory。
     * @param parentBeanFactory 父BeanFactory，如果没有则传null
     */
    public AbstractAutowireCapableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
        this();
        setParentBeanFactory(parentBeanFactory);
    }

    /**
     * 设置用于创建 Bean 实例的实例化策略。
     * 默认为 CglibSubclassingInstantiationStrategy。
     * @see CglibSubclassingInstantiationStrategy
     */
    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }

    /**
     * 返回用于创建 bean 实例的实例化策略。
     */
    public InstantiationStrategy getInstantiationStrategy() {
        return this.instantiationStrategy;
    }

    /**
     * 设置用于解决方法参数名称（例如，用于构造函数名称）的ParameterNameDiscoverer。
     * <p>默认为 {@link DefaultParameterNameDiscoverer}。
     */
    public void setParameterNameDiscoverer(@Nullable ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    /**
     * 返回用于解析方法参数名称的 ParameterNameDiscoverer，如果需要的话。
     */
    @Nullable
    public ParameterNameDiscoverer getParameterNameDiscoverer() {
        return this.parameterNameDiscoverer;
    }

    /**
     * 设置是否允许在 Bean 之间建立循环引用，并自动尝试解决它们。
     * <p>注意，循环引用解决意味着涉及的其中一个 Bean 将接收到一个指向另一个尚未完全初始化的 Bean 的引用。这可能导致初始化时出现微妙或不那么微妙的副作用；尽管如此，它对于许多场景来说工作良好。
     * <p>默认值为 "true"。关闭此选项将在遇到循环引用时抛出异常，从而完全禁止它们。
     * <p><b>注意：</b>通常建议不要在您的 Bean 之间依赖循环引用。重构您的应用程序逻辑，让两个涉及的 Bean 委托给一个封装它们共同逻辑的第三个 Bean。
     */
    public void setAllowCircularReferences(boolean allowCircularReferences) {
        this.allowCircularReferences = allowCircularReferences;
    }

    /**
     * 返回是否允许在 Bean 之间存在循环引用。
     * @since 5.3.10
     * @see #setAllowCircularReferences
     */
    public boolean isAllowCircularReferences() {
        return this.allowCircularReferences;
    }

    /**
     * 设置是否允许将原始的Bean实例注入到其他Bean的属性中，即使注入的Bean最终会被包装（例如，通过AOP自动代理）。
     * <p>这仅在使用其他方法无法解决循环引用的情况下作为最后的手段：本质上，更倾向于注入原始实例而不是整个Bean连接过程失败。
     * <p>自Spring 2.0起，默认值为"false"。开启此选项以允许将非包装的原始Bean注入到您的某些引用中，这是Spring 1.2的（有争议的）默认行为。
     * <p><b>注意：</b>通常建议不要在Bean之间依赖循环引用，尤其是在涉及自动代理的情况下。
     * @see #setAllowCircularReferences
     */
    public void setAllowRawInjectionDespiteWrapping(boolean allowRawInjectionDespiteWrapping) {
        this.allowRawInjectionDespiteWrapping = allowRawInjectionDespiteWrapping;
    }

    /**
     * 返回是否允许对 Bean 实例进行原始注入。
     * @since 5.3.10
     * @see #setAllowRawInjectionDespiteWrapping
     */
    public boolean isAllowRawInjectionDespiteWrapping() {
        return this.allowRawInjectionDespiteWrapping;
    }

    /**
     * 忽略给定的依赖类型进行自动装配：
     * 例如，String。默认为无。
     */
    public void ignoreDependencyType(Class<?> type) {
        this.ignoredDependencyTypes.add(type);
    }

    /**
     * 忽略给定的依赖接口进行自动装配。
     * <p>这通常用于应用程序上下文中注册
     * 通过其他方式解决的依赖，例如通过BeanFactory通过BeanFactoryAware或通过ApplicationContext通过ApplicationContextAware。
     * <p>默认情况下，只有BeanFactoryAware接口被忽略。
     * 对于要忽略的其他类型，为每个类型调用此方法。
     * @see org.springframework.beans.factory.BeanFactoryAware
     * @see org.springframework.context.ApplicationContextAware
     */
    public void ignoreDependencyInterface(Class<?> ifc) {
        this.ignoredDependencyInterfaces.add(ifc);
    }

    @Override
    public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
        super.copyConfigurationFrom(otherFactory);
        if (otherFactory instanceof AbstractAutowireCapableBeanFactory otherAutowireFactory) {
            this.instantiationStrategy = otherAutowireFactory.instantiationStrategy;
            this.allowCircularReferences = otherAutowireFactory.allowCircularReferences;
            this.ignoredDependencyTypes.addAll(otherAutowireFactory.ignoredDependencyTypes);
            this.ignoredDependencyInterfaces.addAll(otherAutowireFactory.ignoredDependencyInterfaces);
        }
    }

    // 很抱歉，您提供的代码注释内容为空，因此没有内容可以翻译。请提供具体的 Java 代码注释内容，我将为您翻译成中文。
    // 创建和填充外部 bean 实例的典型方法
    // 很抱歉，您提供的内容“-------------------------------------------------------------------------”并不是有效的Java代码注释，而是一串横线。在Java中，注释通常以两个斜杠“//”开始，或者以星号“*”开始用于多行注释。如果您能提供具体的Java代码注释内容，我将很乐意为您翻译成中文。
    @Override
    @SuppressWarnings("unchecked")
    public <T> T createBean(Class<T> beanClass) throws BeansException {
        // 使用非单例的 Bean 定义，以避免将 Bean 注册为依赖型 Bean。
        RootBeanDefinition bd = new CreateFromClassBeanDefinition(beanClass);
        bd.setScope(SCOPE_PROTOTYPE);
        bd.allowCaching = ClassUtils.isCacheSafe(beanClass, getBeanClassLoader());
        return (T) createBean(beanClass.getName(), bd, null);
    }

    @Override
    public void autowireBean(Object existingBean) {
        // 使用非单例的 Bean 定义，以避免将 Bean 注册为依赖 Bean。
        RootBeanDefinition bd = new RootBeanDefinition(ClassUtils.getUserClass(existingBean));
        bd.setScope(SCOPE_PROTOTYPE);
        bd.allowCaching = ClassUtils.isCacheSafe(bd.getBeanClass(), getBeanClassLoader());
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        populateBean(bd.getBeanClass().getName(), bd, bw);
    }

    @Override
    public Object configureBean(Object existingBean, String beanName) throws BeansException {
        markBeanAsCreated(beanName);
        BeanDefinition mbd = getMergedBeanDefinition(beanName);
        RootBeanDefinition bd = null;
        if (mbd instanceof RootBeanDefinition rbd) {
            bd = (rbd.isPrototype() ? rbd : rbd.cloneBeanDefinition());
        }
        if (bd == null) {
            bd = new RootBeanDefinition(mbd);
        }
        if (!bd.isPrototype()) {
            bd.setScope(SCOPE_PROTOTYPE);
            bd.allowCaching = ClassUtils.isCacheSafe(ClassUtils.getUserClass(existingBean), getBeanClassLoader());
        }
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        populateBean(beanName, bd, bw);
        return initializeBean(beanName, existingBean, bd);
    }

    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您翻译成中文。
    // 专门的方法用于精细控制 Bean 生命周期
    // 您似乎没有提供具体的Java代码注释内容。请提供需要翻译的代码注释，我将为您翻译成中文。
    @Override
    public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
        // 使用非单例的Bean定义，以避免将Bean注册为依赖Bean。
        RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
        bd.setScope(SCOPE_PROTOTYPE);
        return createBean(beanClass.getName(), bd, null);
    }

    @Override
    public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
        // 使用非单例的 Bean 定义，以避免将 Bean 注册为依赖 Bean。
        RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
        bd.setScope(SCOPE_PROTOTYPE);
        if (bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
            return autowireConstructor(beanClass.getName(), bd, null, null).getWrappedInstance();
        } else {
            Object bean = getInstantiationStrategy().instantiate(bd, null, this);
            populateBean(beanClass.getName(), bd, new BeanWrapperImpl(bean));
            return bean;
        }
    }

    @Override
    public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck) throws BeansException {
        if (autowireMode == AUTOWIRE_CONSTRUCTOR) {
            throw new IllegalArgumentException("AUTOWIRE_CONSTRUCTOR not supported for existing bean instance");
        }
        // 使用非单例的Bean定义，以避免将Bean注册为依赖Bean。
        RootBeanDefinition bd = new RootBeanDefinition(ClassUtils.getUserClass(existingBean), autowireMode, dependencyCheck);
        bd.setScope(SCOPE_PROTOTYPE);
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        populateBean(bd.getBeanClass().getName(), bd, bw);
    }

    @Override
    public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
        markBeanAsCreated(beanName);
        BeanDefinition bd = getMergedBeanDefinition(beanName);
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        applyPropertyValues(beanName, bd, bw, bd.getPropertyValues());
    }

    @Override
    public Object initializeBean(Object existingBean, String beanName) {
        return initializeBean(beanName, existingBean, null);
    }

    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            Object current = processor.postProcessBeforeInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            Object current = processor.postProcessAfterInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    @Override
    public void destroyBean(Object existingBean) {
        new DisposableBeanAdapter(existingBean, getBeanPostProcessorCache().destructionAware).destroy();
    }

    // 您提供的代码注释内容是空的，因此没有内容可以翻译。请提供具体的 Java 代码注释内容，以便我能够将其翻译成中文。
    // 委托方法以解决注入点
    // 很抱歉，您只提供了代码注释中的一个特殊字符序列 "-------------------------------------------------------------------------"。为了能够翻译这些注释，我需要看到实际的英文注释内容。请提供完整的英文代码注释文本，这样我才能进行准确的翻译。
    @Override
    public Object resolveBeanByName(String name, DependencyDescriptor descriptor) {
        InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
        try {
            return getBean(name, descriptor.getDependencyType());
        } finally {
            ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
        }
    }

    @Override
    @Nullable
    public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException {
        return resolveDependency(descriptor, requestingBeanName, null, null);
    }

    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供需要翻译的代码注释内容，我将为您翻译成中文。
    // 实现相关的 AbstractBeanFactory 模板方法
    // 很抱歉，您只提供了代码注释前的横线，没有提供实际的代码注释内容。请提供需要翻译的代码注释，我将为您翻译。
    /**
     * 本类的核心方法：创建一个Bean实例，
     * 填充Bean实例，应用后处理器等。
     * @see #doCreateBean
     */
    @Override
    protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) throws BeanCreationException {
        if (logger.isTraceEnabled()) {
            logger.trace("Creating instance of bean '" + beanName + "'");
        }
        RootBeanDefinition mbdToUse = mbd;
        // 确保在此点真正解析了bean类，并且
        // 在动态解析的类的情况下，克隆bean定义
        // 无法存储在共享合并的Bean定义中。
        Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
        if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
            mbdToUse = new RootBeanDefinition(mbd);
            mbdToUse.setBeanClass(resolvedClass);
            try {
                mbdToUse.prepareMethodOverrides();
            } catch (BeanDefinitionValidationException ex) {
                throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(), beanName, "Validation of method overrides failed", ex);
            }
        }
        try {
            // 给 BeanPostProcessors 机会返回一个代理对象，而不是目标 Bean 实例。
            Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
            if (bean != null) {
                return bean;
            }
        } catch (Throwable ex) {
            throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName, "BeanPostProcessor before instantiation of bean failed", ex);
        }
        try {
            Object beanInstance = doCreateBean(beanName, mbdToUse, args);
            if (logger.isTraceEnabled()) {
                logger.trace("Finished creating instance of bean '" + beanName + "'");
            }
            return beanInstance;
        } catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
            // 之前已经检测到一个异常，并且已经有了合适的bean创建上下文。
            // 或者将非法的单例状态上报给 DefaultSingletonBeanRegistry。
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
        }
    }

    /**
     * 实际创建指定的 bean。在此阶段，预创建处理已经发生，例如检查了 `postProcessBeforeInstantiation` 回调。
     * 区分默认 bean 实例化、使用工厂方法和自动注入构造函数。
     * @param beanName bean 的名称
     * @param mbd 该 bean 的合并后的 bean 定义
     * @param args 用于构造函数或工厂方法调用的显式参数
     * @return bean 的新实例
     * @throws BeanCreationException 如果 bean 无法创建
     * @see #instantiateBean
     * @see #instantiateUsingFactoryMethod
     * @see #autowireConstructor
     */
    protected Object doCreateBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) throws BeanCreationException {
        // 实例化该Bean。
        BeanWrapper instanceWrapper = null;
        if (mbd.isSingleton()) {
            instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
        }
        if (instanceWrapper == null) {
            instanceWrapper = createBeanInstance(beanName, mbd, args);
        }
        Object bean = instanceWrapper.getWrappedInstance();
        Class<?> beanType = instanceWrapper.getWrappedClass();
        if (beanType != NullBean.class) {
            mbd.resolvedTargetType = beanType;
        }
        // 允许后处理器修改合并后的 Bean 定义。
        synchronized (mbd.postProcessingLock) {
            if (!mbd.postProcessed) {
                try {
                    applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
                } catch (Throwable ex) {
                    throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Post-processing of merged bean definition failed", ex);
                }
                mbd.markAsPostProcessed();
            }
        }
        // 积极缓存单例以能够解决循环引用问题
        // 即使由生命周期接口（如BeanFactoryAware）触发。
        boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences && isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
            if (logger.isTraceEnabled()) {
                logger.trace("Eagerly caching bean '" + beanName + "' to allow for resolving potential circular references");
            }
            addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
        }
        // 初始化 bean 实例。
        Object exposedObject = bean;
        try {
            populateBean(beanName, mbd, instanceWrapper);
            exposedObject = initializeBean(beanName, exposedObject, mbd);
        } catch (Throwable ex) {
            if (ex instanceof BeanCreationException bce && beanName.equals(bce.getBeanName())) {
                throw bce;
            } else {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
            }
        }
        if (earlySingletonExposure) {
            Object earlySingletonReference = getSingleton(beanName, false);
            if (earlySingletonReference != null) {
                if (exposedObject == bean) {
                    exposedObject = earlySingletonReference;
                } else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
                    String[] dependentBeans = getDependentBeans(beanName);
                    Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
                    for (String dependentBean : dependentBeans) {
                        if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                            actualDependentBeans.add(dependentBean);
                        }
                    }
                    if (!actualDependentBeans.isEmpty()) {
                        throw new BeanCurrentlyInCreationException(beanName, "Bean with name '" + beanName + "' has been injected into other beans [" + StringUtils.collectionToCommaDelimitedString(actualDependentBeans) + "] in its raw version as part of a circular reference, but has eventually been " + "wrapped. This means that said other beans do not use the final version of the " + "bean. This is often the result of over-eager type matching - consider using " + "'getBeanNamesForType' with the 'allowEagerInit' flag turned off, for example.");
                    }
                }
            }
        }
        // 将 Bean 注册为可丢弃的。
        try {
            registerDisposableBeanIfNecessary(beanName, bean, mbd);
        } catch (BeanDefinitionValidationException ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
        }
        return exposedObject;
    }

    @Override
    @Nullable
    protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> targetType = determineTargetType(beanName, mbd, typesToMatch);
        // 应用 SmartInstantiationAwareBeanPostProcessors 来预测：
        // 在先于实例化快捷方式后的最终类型。
        if (targetType != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            boolean matchingOnlyFactoryBean = (typesToMatch.length == 1 && typesToMatch[0] == FactoryBean.class);
            for (SmartInstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().smartInstantiationAware) {
                Class<?> predicted = bp.predictBeanType(targetType, beanName);
                if (predicted != null && (!matchingOnlyFactoryBean || FactoryBean.class.isAssignableFrom(predicted))) {
                    return predicted;
                }
            }
        }
        return targetType;
    }

    /**
     * 确定给定 Bean 定义的目标类型。
     * @param beanName Bean 的名称（用于错误处理目的）
     * @param mbd Bean 的合并后的 Bean 定义
     * @param typesToMatch 用于内部类型匹配目的的类型（也表明返回的 {@code Class} 将永远不会暴露给应用程序代码）
     * @return 如果可确定，则返回 Bean 的类型，否则返回 {@code null}
     */
    @Nullable
    protected Class<?> determineTargetType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> targetType = mbd.getTargetType();
        if (targetType == null) {
            if (mbd.getFactoryMethodName() != null) {
                targetType = getTypeForFactoryMethod(beanName, mbd, typesToMatch);
            } else {
                targetType = resolveBeanClass(mbd, beanName, typesToMatch);
                if (mbd.hasBeanClass()) {
                    targetType = getInstantiationStrategy().getActualBeanClass(mbd, beanName, this);
                }
            }
            if (ObjectUtils.isEmpty(typesToMatch) || getTempClassLoader() == null) {
                mbd.resolvedTargetType = targetType;
            }
        }
        return targetType;
    }

    /**
     * 确定基于工厂方法的给定bean定义的目标类型。只有在尚未注册目标bean的单例实例时才会调用此方法。
     * <p>此实现确定与`#createBean`的不同创建策略匹配的类型。只要可能，我们将执行静态类型检查，以避免创建目标bean。
     * @param beanName bean的名称（用于错误处理目的）
     * @param mbd bean的合并后的bean定义
     * @param typesToMatch 内部类型匹配目的的匹配类型（同时也表示返回的`Class`永远不会暴露给应用程序代码）
     * @return 如果可确定，则返回bean的类型，否则返回`null`
     * @see #createBean
     */
    @Nullable
    protected Class<?> getTypeForFactoryMethod(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        ResolvableType cachedReturnType = mbd.factoryMethodReturnType;
        if (cachedReturnType != null) {
            return cachedReturnType.resolve();
        }
        Class<?> commonType = null;
        Method uniqueCandidate = mbd.factoryMethodToIntrospect;
        if (uniqueCandidate == null) {
            Class<?> factoryClass;
            boolean isStatic = true;
            String factoryBeanName = mbd.getFactoryBeanName();
            if (factoryBeanName != null) {
                if (factoryBeanName.equals(beanName)) {
                    throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName, "factory-bean reference points back to the same bean definition");
                }
                // 检查工厂类中声明的工厂方法的返回类型。
                factoryClass = getType(factoryBeanName);
                isStatic = false;
            } else {
                // 检查 Bean 类中声明的工厂方法的返回类型。
                factoryClass = resolveBeanClass(mbd, beanName, typesToMatch);
            }
            if (factoryClass == null) {
                return null;
            }
            factoryClass = ClassUtils.getUserClass(factoryClass);
            // 如果所有工厂方法具有相同的返回类型，则返回该类型。
            // 无法明确确定方法，因为类型转换/自动装配导致！
            int minNrOfArgs = (mbd.hasConstructorArgumentValues() ? mbd.getConstructorArgumentValues().getArgumentCount() : 0);
            Method[] candidates = this.factoryMethodCandidateCache.computeIfAbsent(factoryClass, clazz -> ReflectionUtils.getUniqueDeclaredMethods(clazz, ReflectionUtils.USER_DECLARED_METHODS));
            for (Method candidate : candidates) {
                if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate) && candidate.getParameterCount() >= minNrOfArgs) {
                    // 声明的类型变量以检查？
                    if (candidate.getTypeParameters().length > 0) {
                        try {
                            // 完全解析参数名称和参数值。
                            ConstructorArgumentValues cav = mbd.getConstructorArgumentValues();
                            Class<?>[] paramTypes = candidate.getParameterTypes();
                            String[] paramNames = null;
                            if (cav.containsNamedArgument()) {
                                ParameterNameDiscoverer pnd = getParameterNameDiscoverer();
                                if (pnd != null) {
                                    paramNames = pnd.getParameterNames(candidate);
                                }
                            }
                            Set<ConstructorArgumentValues.ValueHolder> usedValueHolders = new HashSet<>(paramTypes.length);
                            Object[] args = new Object[paramTypes.length];
                            for (int i = 0; i < args.length; i++) {
                                ConstructorArgumentValues.ValueHolder valueHolder = cav.getArgumentValue(i, paramTypes[i], (paramNames != null ? paramNames[i] : null), usedValueHolders);
                                if (valueHolder == null) {
                                    valueHolder = cav.getGenericArgumentValue(null, null, usedValueHolders);
                                }
                                if (valueHolder != null) {
                                    args[i] = valueHolder.getValue();
                                    usedValueHolders.add(valueHolder);
                                }
                            }
                            Class<?> returnType = AutowireUtils.resolveReturnTypeForFactoryMethod(candidate, args, getBeanClassLoader());
                            uniqueCandidate = (commonType == null && returnType == candidate.getReturnType() ? candidate : null);
                            commonType = ClassUtils.determineCommonAncestor(returnType, commonType);
                            if (commonType == null) {
                                // 发现返回类型不明确：返回 null 以表示“无法确定”。
                                return null;
                            }
                        } catch (Throwable ex) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Failed to resolve generic return type for factory method: " + ex);
                            }
                        }
                    } else {
                        uniqueCandidate = (commonType == null ? candidate : null);
                        commonType = ClassUtils.determineCommonAncestor(candidate.getReturnType(), commonType);
                        if (commonType == null) {
                            // 找到了模糊的返回类型：返回null以表示“不可确定”。
                            return null;
                        }
                    }
                }
            }
            mbd.factoryMethodToIntrospect = uniqueCandidate;
            if (commonType == null) {
                return null;
            }
        }
        // 常见的返回类型已找到：所有工厂方法返回相同的类型。对于非参数化的
        // 唯一候选者，缓存目标工厂方法的完整类型声明上下文。
        cachedReturnType = (uniqueCandidate != null ? ResolvableType.forMethodReturnType(uniqueCandidate) : ResolvableType.forClass(commonType));
        mbd.factoryMethodReturnType = cachedReturnType;
        return cachedReturnType.resolve();
    }

    /**
     * 此实现尝试查询 FactoryBean 的泛型参数元数据（如果存在）以确定对象类型。如果不存在，即 FactoryBean 被声明为原始类型，它将在尚未应用 bean 属性的 FactoryBean 的普通实例上检查 FactoryBean 的 `getObjectType` 方法。如果这还没有返回类型，并且 `allowInit` 为 `true`，则尝试通过委托到超类实现来尝试完全创建 FactoryBean 作为后备方案。
     * <p>对于 FactoryBean 的快捷检查仅在单例 FactoryBean 的情况下应用。如果 FactoryBean 实例本身不是作为单例保留，它将被完全创建以检查其公开的对象类型。
     */
    @Override
    protected ResolvableType getTypeForFactoryBean(String beanName, RootBeanDefinition mbd, boolean allowInit) {
        // 检查该 Bean 定义是否本身已通过属性定义了类型
        ResolvableType result = getTypeForFactoryBeanFromAttributes(mbd);
        if (result != ResolvableType.NONE) {
            return result;
        }
        // 例如，对于提供的豆类，立即尝试目标类型和豆类。
        if (mbd.getInstanceSupplier() != null) {
            result = getFactoryBeanGeneric(mbd.targetType);
            if (result.resolve() != null) {
                return result;
            }
            result = getFactoryBeanGeneric(mbd.hasBeanClass() ? ResolvableType.forClass(mbd.getBeanClass()) : null);
            if (result.resolve() != null) {
                return result;
            }
        }
        // 考虑工厂方法
        String factoryBeanName = mbd.getFactoryBeanName();
        String factoryMethodName = mbd.getFactoryMethodName();
        // 扫描工厂bean方法
        if (factoryBeanName != null) {
            if (factoryMethodName != null) {
                // 尝试从FactoryBean的工厂方法中获取其对象类型
                // 声明了包含的Bean，但没有实例化该Bean。
                BeanDefinition factoryBeanDefinition = getBeanDefinition(factoryBeanName);
                Class<?> factoryBeanClass;
                if (factoryBeanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition && abstractBeanDefinition.hasBeanClass()) {
                    factoryBeanClass = abstractBeanDefinition.getBeanClass();
                } else {
                    RootBeanDefinition fbmbd = getMergedBeanDefinition(factoryBeanName, factoryBeanDefinition);
                    factoryBeanClass = determineTargetType(factoryBeanName, fbmbd);
                }
                if (factoryBeanClass != null) {
                    result = getTypeForFactoryBeanFromMethod(factoryBeanClass, factoryMethodName);
                    if (result.resolve() != null) {
                        return result;
                    }
                }
            }
            // 如果上述方法无法解决问题，并且引用的工厂Bean尚不存在，
            // 在这里退出 - 我们不希望仅仅为了强制创建另一个bean而这样做
            // 获取 FactoryBean 的对象类型...
            if (!isBeanEligibleForMetadataCaching(factoryBeanName)) {
                return ResolvableType.NONE;
            }
        }
        // 如果我们被允许，我们可以提前创建工厂Bean并调用getObjectType()方法
        if (allowInit) {
            FactoryBean<?> factoryBean = (mbd.isSingleton() ? getSingletonFactoryBeanForTypeCheck(beanName, mbd) : getNonSingletonFactoryBeanForTypeCheck(beanName, mbd));
            if (factoryBean != null) {
                // 尝试在实例的早期阶段获取 FactoryBean 的对象类型。
                Class<?> type = getTypeForFactoryBean(factoryBean);
                if (type != null) {
                    return ResolvableType.forClass(type);
                }
                // 未找到用于快捷方式 FactoryBean 实例的类型：
                // 回退到创建完整的 FactoryBean 实例。
                return super.getTypeForFactoryBean(beanName, mbd, true);
            }
        }
        if (factoryBeanName == null && mbd.hasBeanClass() && factoryMethodName != null) {
            // 无法提前实例化Bean：从FactoryBean中确定类型
            // 静态工厂方法签名或类继承层次结构...
            return getTypeForFactoryBeanFromMethod(mbd.getBeanClass(), factoryMethodName);
        }
        // 对于常规的Bean，尝试使用目标类型和Bean类作为后备方案
        if (mbd.getInstanceSupplier() == null) {
            result = getFactoryBeanGeneric(mbd.targetType);
            if (result.resolve() != null) {
                return result;
            }
            result = getFactoryBeanGeneric(mbd.hasBeanClass() ? ResolvableType.forClass(mbd.getBeanClass()) : null);
            if (result.resolve() != null) {
                return result;
            }
        }
        // 工厂Bean类型无法解析
        return ResolvableType.NONE;
    }

    /**
     * 对给定的bean类上的工厂方法签名进行反射，
     * 尝试找到在其中声明的公共的 {@code FactoryBean} 对象类型。
     * @param beanClass 要在其上找到工厂方法的bean类
     * @param factoryMethodName 工厂方法的名字
     * @return 公共的 {@code FactoryBean} 对象类型，如果没有则返回 {@code null}
     */
    private ResolvableType getTypeForFactoryBeanFromMethod(Class<?> beanClass, String factoryMethodName) {
        // CGLIB 子类方法隐藏泛型参数；请查看原始用户类。
        Class<?> factoryBeanClass = ClassUtils.getUserClass(beanClass);
        FactoryBeanMethodTypeFinder finder = new FactoryBeanMethodTypeFinder(factoryMethodName);
        ReflectionUtils.doWithMethods(factoryBeanClass, finder, ReflectionUtils.USER_DECLARED_METHODS);
        return finder.getResult();
    }

    /**
     * 获取对指定bean的早期访问引用，
     * 通常用于解决循环引用的问题。
     * @param beanName bean的名称（用于错误处理目的）
     * @param mbd bean的合并后的bean定义
     * @param bean 原始的bean实例
     * @return 要公开为bean引用的对象
     */
    protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
        Object exposedObject = bean;
        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            for (SmartInstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().smartInstantiationAware) {
                exposedObject = bp.getEarlyBeanReference(exposedObject, beanName);
            }
        }
        return exposedObject;
    }

    // 请提供需要翻译的 Java 代码注释内容，这样我才能进行准确的翻译。目前您只提供了代码注释的开始符号 "---------------------------------------------------------------------"，并没有实际的注释文本。
    // 实现方法
    // 由于您提供的代码注释内容为空，因此我无法进行翻译。请提供具体的Java代码注释内容，以便我能够进行准确的翻译。
    /**
     * 获取一个用于执行 {@code getObjectType()} 调用的“快捷”单例 FactoryBean 实例，而不进行完整的 FactoryBean 初始化。
     * @param beanName bean的名称
     * @param mbd bean的定义
     * @return 返回 FactoryBean 实例，或返回 {@code null} 表示无法获取快捷 FactoryBean 实例
     */
    @Nullable
    private FactoryBean<?> getSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
        synchronized (getSingletonMutex()) {
            BeanWrapper bw = this.factoryBeanInstanceCache.get(beanName);
            if (bw != null) {
                return (FactoryBean<?>) bw.getWrappedInstance();
            }
            Object beanInstance = getSingleton(beanName, false);
            if (beanInstance instanceof FactoryBean<?> factoryBean) {
                return factoryBean;
            }
            if (isSingletonCurrentlyInCreation(beanName) || (mbd.getFactoryBeanName() != null && isSingletonCurrentlyInCreation(mbd.getFactoryBeanName()))) {
                return null;
            }
            Object instance;
            try {
                // 将此 Bean 标记为当前处于创建状态，即使只是部分创建。
                beforeSingletonCreation(beanName);
                // 给BeanPostProcessors一个机会来返回一个代理而不是目标bean实例。
                instance = resolveBeforeInstantiation(beanName, mbd);
                if (instance == null) {
                    bw = createBeanInstance(beanName, mbd, null);
                    instance = bw.getWrappedInstance();
                }
            } catch (UnsatisfiedDependencyException ex) {
                // 不要吞下，可能是配置错误...
                throw ex;
            } catch (BeanCreationException ex) {
                // 不要吞食链接错误，因为它包含完整的堆栈跟踪信息
                // 首次出现...之后只是一个简单的NoClassDefFoundError。
                if (ex.contains(LinkageError.class)) {
                    throw ex;
                }
                // 实例化失败，可能是太早了...
                if (logger.isDebugEnabled()) {
                    logger.debug("Bean creation exception on singleton FactoryBean type check: " + ex);
                }
                onSuppressedException(ex);
                return null;
            } finally {
                // 已完成此 Bean 的部分创建。
                afterSingletonCreation(beanName);
            }
            FactoryBean<?> fb = getFactoryBean(beanName, instance);
            if (bw != null) {
                this.factoryBeanInstanceCache.put(beanName, bw);
            }
            return fb;
        }
    }

    /**
     * 获取一个“快捷”的非单例FactoryBean实例，用于进行{@code getObjectType()}调用，而不进行FactoryBean的完整初始化。
     * @param beanName bean的名称
     * @param mbd bean的定义
     * @return 返回FactoryBean实例，或者返回{@code null}表示无法获取快捷的FactoryBean实例
     */
    @Nullable
    private FactoryBean<?> getNonSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
        if (isPrototypeCurrentlyInCreation(beanName)) {
            return null;
        }
        Object instance;
        try {
            // 将此 Bean 标记为当前处于创建状态，即使只是部分创建。
            beforePrototypeCreation(beanName);
            // 给 BeanPostProcessors 机会，使其能够返回一个代理实例而不是目标 Bean 实例。
            instance = resolveBeforeInstantiation(beanName, mbd);
            if (instance == null) {
                BeanWrapper bw = createBeanInstance(beanName, mbd, null);
                instance = bw.getWrappedInstance();
            }
        } catch (UnsatisfiedDependencyException ex) {
            // 不要吞下，可能是配置错误...
            throw ex;
        } catch (BeanCreationException ex) {
            // 实例化失败，可能过早...
            if (logger.isDebugEnabled()) {
                logger.debug("Bean creation exception on non-singleton FactoryBean type check: " + ex);
            }
            onSuppressedException(ex);
            return null;
        } finally {
            // 此Bean的部分创建已完成。
            afterPrototypeCreation(beanName);
        }
        return getFactoryBean(beanName, instance);
    }

    /**
     * 将合并后的Bean定义应用于指定的Bean定义，
     * 调用它们的`postProcessMergedBeanDefinition`方法。
     * @param mbd 该Bean的合并后的Bean定义
     * @param beanType 管理的Bean实例的实际类型
     * @param beanName Bean的名称
     * @see MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition
     */
    protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
        for (MergedBeanDefinitionPostProcessor processor : getBeanPostProcessorCache().mergedDefinition) {
            processor.postProcessMergedBeanDefinition(mbd, beanType, beanName);
        }
    }

    /**
     * 在实例化前应用后处理器，确定是否存在针对指定bean的实例化前快捷方式。
     * @param beanName bean的名称
     * @param mbd bean的定义
     * @return 由快捷方式确定的bean实例，或如果不存在则返回{@code null}
     */
    @Nullable
    protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
        Object bean = null;
        if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
            // 确保在此处实际上已经解析了 Bean 类。
            if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
                Class<?> targetType = determineTargetType(beanName, mbd);
                if (targetType != null) {
                    bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                    if (bean != null) {
                        bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                    }
                }
            }
            mbd.beforeInstantiationResolved = (bean != null);
        }
        return bean;
    }

    /**
     * 将 InstantiationAwareBeanPostProcessors 应用到指定的bean定义（通过类和名称），调用它们的 {@code postProcessBeforeInstantiation} 方法。
     * <p>任何返回的对象将用作bean，而不是实际实例化目标bean。后处理器的返回值为 {@code null} 将导致目标bean被实例化。
     * @param beanClass 要实例化的bean的类
     * @param beanName bean的名称
     * @return 用于替代目标bean默认实例的bean对象，或 {@code null}
     * @see InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation
     */
    @Nullable
    protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
        for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
            Object result = bp.postProcessBeforeInstantiation(beanClass, beanName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * 创建指定Bean的新实例，使用适当的实例化策略：
     * 工厂方法、构造器自动装配，或简单实例化。
     * @param beanName Bean的名称
     * @param mbd Bean的Bean定义
     * @param args 用于构造器或工厂方法调用的显式参数
     * @return 新实例的BeanWrapper
     * @see #obtainFromSupplier
     * @see #instantiateUsingFactoryMethod
     * @see #autowireConstructor
     * @see #instantiateBean
     */
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
        // 确保在这一点上已经真正解析了 Bean 类。
        Class<?> beanClass = resolveBeanClass(mbd, beanName);
        if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
        }
        Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
        if (instanceSupplier != null) {
            return obtainFromSupplier(instanceSupplier, beanName, mbd);
        }
        if (mbd.getFactoryMethodName() != null) {
            return instantiateUsingFactoryMethod(beanName, mbd, args);
        }
        // 重新创建相同Bean时的快捷方式
        boolean resolved = false;
        boolean autowireNecessary = false;
        if (args == null) {
            synchronized (mbd.constructorArgumentLock) {
                if (mbd.resolvedConstructorOrFactoryMethod != null) {
                    resolved = true;
                    autowireNecessary = mbd.constructorArgumentsResolved;
                }
            }
        }
        if (resolved) {
            if (autowireNecessary) {
                return autowireConstructor(beanName, mbd, null, null);
            } else {
                return instantiateBean(beanName, mbd);
            }
        }
        // 候选构造函数用于自动装配？
        Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
        if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR || mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
            return autowireConstructor(beanName, mbd, ctors, args);
        }
        // 首选的构造函数用于默认构造吗？
        ctors = mbd.getPreferredConstructors();
        if (ctors != null) {
            return autowireConstructor(beanName, mbd, ctors, null);
        }
        // 无需特殊处理：直接使用无参构造函数。
        return instantiateBean(beanName, mbd);
    }

    /**
     * 从给定的供应商中获取一个Bean实例。
     * @param supplier 配置的供应商
     * @param beanName 对应的Bean名称
     * @return 新实例的BeanWrapper
     */
    private BeanWrapper obtainFromSupplier(Supplier<?> supplier, String beanName, RootBeanDefinition mbd) {
        String outerBean = this.currentlyCreatedBean.get();
        this.currentlyCreatedBean.set(beanName);
        Object instance;
        try {
            instance = obtainInstanceFromSupplier(supplier, beanName, mbd);
        } catch (Throwable ex) {
            if (ex instanceof BeansException beansException) {
                throw beansException;
            }
            throw new BeanCreationException(beanName, "Instantiation of supplied bean failed", ex);
        } finally {
            if (outerBean != null) {
                this.currentlyCreatedBean.set(outerBean);
            } else {
                this.currentlyCreatedBean.remove();
            }
        }
        if (instance == null) {
            instance = new NullBean();
        }
        BeanWrapper bw = new BeanWrapperImpl(instance);
        initBeanWrapper(bw);
        return bw;
    }

    /**
     * 从给定的供应商中获取一个 Bean 实例。
     * @param supplier 配置的供应商
     * @param beanName 对应的 Bean 名称
     * @param mbd Bean 的定义
     * @return Bean 实例（可能为 {@code null}）
     * @since 6.0.7
     */
    @Nullable
    protected Object obtainInstanceFromSupplier(Supplier<?> supplier, String beanName, RootBeanDefinition mbd) throws Exception {
        if (supplier instanceof ThrowingSupplier<?> throwingSupplier) {
            return throwingSupplier.getWithException();
        }
        return supplier.get();
    }

    /**
     * 在此重写是为了在调用`Supplier`回调期间程序化检索到的进一步bean上隐式注册当前创建的bean作为依赖项。
     * @since 5.0
     * @see #obtainFromSupplier
     */
    @Override
    protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {
        String currentlyCreatedBean = this.currentlyCreatedBean.get();
        if (currentlyCreatedBean != null) {
            registerDependentBean(beanName, currentlyCreatedBean);
        }
        return super.getObjectForBeanInstance(beanInstance, name, beanName, mbd);
    }

    /**
     * 确定给定 bean 应使用的候选构造函数，检查所有注册的
     * {@link SmartInstantiationAwareBeanPostProcessor SmartInstantiationAwareBeanPostProcessors}。
     * @param beanClass bean 的原始类
     * @param beanName bean 的名称
     * @return 候选构造函数，如果没有指定则返回 {@code null}
     * @throws org.springframework.beans.BeansException 如果出现错误
     * @see org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors
     */
    @Nullable
    protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(@Nullable Class<?> beanClass, String beanName) throws BeansException {
        if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
            for (SmartInstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().smartInstantiationAware) {
                Constructor<?>[] ctors = bp.determineCandidateConstructors(beanClass, beanName);
                if (ctors != null) {
                    return ctors;
                }
            }
        }
        return null;
    }

    /**
     * 使用其默认构造函数实例化给定的Bean。
     * @param beanName Bean的名称
     * @param mbd Bean的定义
     * @return 新实例的BeanWrapper
     */
    protected BeanWrapper instantiateBean(String beanName, RootBeanDefinition mbd) {
        try {
            Object beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, this);
            BeanWrapper bw = new BeanWrapperImpl(beanInstance);
            initBeanWrapper(bw);
            return bw;
        } catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
        }
    }

    /**
     * 使用命名工厂方法实例化该Bean。如果mbd参数指定了一个类而不是一个FactoryBean，或者是一个本身配置了依赖注入的工厂对象的实例变量，则该方法可能是静态的。
     * @param beanName Bean的名称
     * @param mbd Bean的bean定义
     * @param explicitArgs 通过getBean方法程序化传递的参数值，或者为null（表示使用Bean定义中的构造器参数值）
     * @return 新实例的BeanWrapper
     * @see #getBean(String, Object[])
     */
    protected BeanWrapper instantiateUsingFactoryMethod(String beanName, RootBeanDefinition mbd, @Nullable Object[] explicitArgs) {
        return new ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd, explicitArgs);
    }

    /**
     * “自动装配构造器”行为（通过类型传递构造器参数）。
     * 如果显式指定了构造器参数值，同样适用，将所有剩余参数与bean工厂中的bean匹配。
     * <p>这对应于构造器注入：在此模式下，Spring bean工厂能够托管期望基于构造器依赖解析的组件。
     * @param beanName bean的名称
     * @param mbd bean的bean定义
     * @param ctors选择的候选构造器
     * @param explicitArgs通过getBean方法程序化传递的参数值，或null（表示使用bean定义中的构造器参数值）
     * @return 新实例的BeanWrapper
     */
    protected BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mbd, @Nullable Constructor<?>[] ctors, @Nullable Object[] explicitArgs) {
        return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, explicitArgs);
    }

    /**
     * 在给定的BeanWrapper中用bean定义中的属性值填充bean实例。
     * @param beanName bean的名称
     * @param mbd bean的定义
     * @param bw 包含bean实例的BeanWrapper
     */
    protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
        if (bw == null) {
            if (mbd.hasPropertyValues()) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
            } else {
                // 跳过对 null 实例的属性人口阶段。
                return;
            }
        }
        if (bw.getWrappedClass().isRecord()) {
            if (mbd.hasPropertyValues()) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Cannot apply property values to a record");
            } else {
                // 跳过记录的属性填充阶段，因为它们是不可变的。
                return;
            }
        }
        // 给任何 `InstantiationAwareBeanPostProcessors` 机会来修改以下内容：
        // 在设置属性之前的 Bean 状态。例如，这可以用于：
        // 以支持字段注入的风格。
        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
                if (!bp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
                    return;
                }
            }
        }
        PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);
        int resolvedAutowireMode = mbd.getResolvedAutowireMode();
        if (resolvedAutowireMode == AUTOWIRE_BY_NAME || resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
            MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
            // 根据适用的自动装配按名称添加属性值。
            if (resolvedAutowireMode == AUTOWIRE_BY_NAME) {
                autowireByName(beanName, mbd, bw, newPvs);
            }
            // 根据适用情况，通过自动装配类型添加属性值。
            if (resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
                autowireByType(beanName, mbd, bw, newPvs);
            }
            pvs = newPvs;
        }
        if (hasInstantiationAwareBeanPostProcessors()) {
            if (pvs == null) {
                pvs = mbd.getPropertyValues();
            }
            for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
                PropertyValues pvsToUse = bp.postProcessProperties(pvs, bw.getWrappedInstance(), beanName);
                if (pvsToUse == null) {
                    return;
                }
                pvs = pvsToUse;
            }
        }
        boolean needsDepCheck = (mbd.getDependencyCheck() != AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);
        if (needsDepCheck) {
            PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
            checkDependencies(beanName, mbd, filteredPds, pvs);
        }
        if (pvs != null) {
            applyPropertyValues(beanName, mbd, bw, pvs);
        }
    }

    /**
     * 如果自动装配设置为"byName"，则使用此工厂中其他bean的引用填充任何缺失的属性值。
     * @param beanName 我们正在连接的bean的名称。
     * 对调试消息有用；在功能上未使用。
     * @param mbd 要通过自动装配更新的bean定义
     * @param bw 可以从中获取有关bean信息的BeanWrapper
     * @param pvs 要将连接对象注册到其上的属性值
     */
    protected void autowireByName(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {
        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
        for (String propertyName : propertyNames) {
            if (containsBean(propertyName)) {
                Object bean = getBean(propertyName);
                pvs.add(propertyName, bean);
                registerDependentBean(propertyName, beanName);
                if (logger.isTraceEnabled()) {
                    logger.trace("Added autowiring by name from bean name '" + beanName + "' via property '" + propertyName + "' to bean named '" + propertyName + "'");
                }
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName + "' by name: no matching bean found");
                }
            }
        }
    }

    /**
     * 抽象方法定义了“按类型自动装配”（通过类型装配bean属性）的行为。
     * <p>这与PicoContainer的默认行为类似，即在bean工厂中必须有恰好一个与属性类型对应的bean。这使得对于小命名空间，bean工厂的配置变得简单，但对于大型应用程序来说，其表现并不如标准Spring行为。
     * @param beanName 要按类型自动装配的bean的名称
     * @param mbd 通过自动装配更新合并的bean定义
     * @param bw 可从中获取关于bean信息的BeanWrapper
     * @param pvs 要将已连接的对象注册到其上的属性值
     */
    protected void autowireByType(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {
        TypeConverter converter = getCustomTypeConverter();
        if (converter == null) {
            converter = bw;
        }
        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
        Set<String> autowiredBeanNames = new LinkedHashSet<>(propertyNames.length * 2);
        for (String propertyName : propertyNames) {
            try {
                PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
                // 不要尝试通过类型自动装配类型为 Object 的对象：这永远没有意义。
                // 即使从技术上讲，它是一个未满足的、非简单属性。
                if (Object.class != pd.getPropertyType()) {
                    MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
                    // 不允许在优先级较高的后处理器的情况下，对类型匹配进行预加载初始化。
                    boolean eager = !(bw.getWrappedInstance() instanceof PriorityOrdered);
                    DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
                    Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, converter);
                    if (autowiredArgument != null) {
                        pvs.add(propertyName, autowiredArgument);
                    }
                    for (String autowiredBeanName : autowiredBeanNames) {
                        registerDependentBean(autowiredBeanName, beanName);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Autowiring by type from bean name '" + beanName + "' via property '" + propertyName + "' to bean named '" + autowiredBeanName + "'");
                        }
                    }
                    autowiredBeanNames.clear();
                }
            } catch (BeansException ex) {
                throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, propertyName, ex);
            }
        }
    }

    /**
     * 返回一个包含不满足的非简单 Bean 属性的数组。
     * 这些可能是工厂中对其他 Bean 的不满足引用。不包括简单的属性，如原始数据类型或字符串。
     * @param mbd 使用创建 Bean 的合并后的 Bean 定义
     * @param bw 使用创建 Bean 的 BeanWrapper
     * @return 包含 Bean 属性名称的数组
     * @see org.springframework.beans.BeanUtils#isSimpleProperty
     */
    protected String[] unsatisfiedNonSimpleProperties(AbstractBeanDefinition mbd, BeanWrapper bw) {
        Set<String> result = new TreeSet<>();
        PropertyValues pvs = mbd.getPropertyValues();
        PropertyDescriptor[] pds = bw.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null && !isExcludedFromDependencyCheck(pd) && !pvs.contains(pd.getName()) && !BeanUtils.isSimpleProperty(pd.getPropertyType())) {
                result.add(pd.getName());
            }
        }
        return StringUtils.toStringArray(result);
    }

    /**
     * 从给定的BeanWrapper中提取一组经过筛选的PropertyDescriptor，
     * 排除被忽略的依赖类型或定义在忽略依赖接口上的属性。
     * @param bw 创建bean时使用的BeanWrapper
     * @param cache 是否缓存给定bean类的筛选后的PropertyDescriptor
     * @return 筛选后的PropertyDescriptor
     * @see #isExcludedFromDependencyCheck
     * @see #filterPropertyDescriptorsForDependencyCheck(org.springframework.beans.BeanWrapper)
     */
    protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw, boolean cache) {
        PropertyDescriptor[] filtered = this.filteredPropertyDescriptorsCache.get(bw.getWrappedClass());
        if (filtered == null) {
            filtered = filterPropertyDescriptorsForDependencyCheck(bw);
            if (cache) {
                PropertyDescriptor[] existing = this.filteredPropertyDescriptorsCache.putIfAbsent(bw.getWrappedClass(), filtered);
                if (existing != null) {
                    filtered = existing;
                }
            }
        }
        return filtered;
    }

    /**
     * 从给定的BeanWrapper中提取一个经过筛选的PropertyDescriptor集合，
     * 排除被忽略的依赖类型或定义在忽略的依赖接口上的属性。
     * @param bw 创建bean时使用的BeanWrapper
     * @return 筛选后的PropertyDescriptor集合
     * @see #isExcludedFromDependencyCheck
     */
    protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw) {
        List<PropertyDescriptor> pds = new ArrayList<>(Arrays.asList(bw.getPropertyDescriptors()));
        pds.removeIf(this::isExcludedFromDependencyCheck);
        return pds.toArray(new PropertyDescriptor[0]);
    }

    /**
     * 判断给定的 Bean 属性是否排除在依赖检查之外。
     * <p>此实现排除由 CGLIB 定义的属性以及类型匹配忽略的依赖类型或由忽略的依赖接口定义的属性。
     * @param pd Bean属性的 PropertyDescriptor
     * @return Bean属性是否被排除
     * @see #ignoreDependencyType(Class)
     * @see #ignoreDependencyInterface(Class)
     */
    protected boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
        return (AutowireUtils.isExcludedFromDependencyCheck(pd) || this.ignoredDependencyTypes.contains(pd.getPropertyType()) || AutowireUtils.isSetterDefinedInInterface(pd, this.ignoredDependencyInterfaces));
    }

    /**
     * 执行一个依赖性检查，以确保所有暴露的属性都已被设置（如果需要的话）。依赖性检查可以是对象（协作的bean）、简单类型（原始类型和String），或所有类型（两者皆是）。
     * @param beanName bean的名称
     * @param mbd 使用以创建bean的合并后的bean定义
     * @param pds 目标bean的相关属性描述符
     * @param pvs 应用到bean的属性值
     * @see #isExcludedFromDependencyCheck(java.beans.PropertyDescriptor)
     */
    protected void checkDependencies(String beanName, AbstractBeanDefinition mbd, PropertyDescriptor[] pds, @Nullable PropertyValues pvs) throws UnsatisfiedDependencyException {
        int dependencyCheck = mbd.getDependencyCheck();
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null && (pvs == null || !pvs.contains(pd.getName()))) {
                boolean isSimple = BeanUtils.isSimpleProperty(pd.getPropertyType());
                boolean unsatisfied = (dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_ALL) || (isSimple && dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE) || (!isSimple && dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
                if (unsatisfied) {
                    throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, pd.getName(), "Set this property value or disable dependency checking for this bean.");
                }
            }
        }
    }

    /**
     * 应用给定的属性值，解析此BeanFactory中其他bean的运行时引用。
     * 必须使用深度复制，以确保我们不永久修改此属性。
     * @param beanName 传递的bean名称，用于更好的异常信息
     * @param mbd 合并的bean定义
     * @param bw 包装目标对象的BeanWrapper
     * @param pvs 新的属性值
     */
    protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
        if (pvs.isEmpty()) {
            return;
        }
        MutablePropertyValues mpvs = null;
        List<PropertyValue> original;
        if (pvs instanceof MutablePropertyValues _mpvs) {
            mpvs = _mpvs;
            if (mpvs.isConverted()) {
                // 快捷方式：直接使用预先转换的值。
                try {
                    bw.setPropertyValues(mpvs);
                    return;
                } catch (BeansException ex) {
                    throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Error setting property values", ex);
                }
            }
            original = mpvs.getPropertyValueList();
        } else {
            original = Arrays.asList(pvs.getPropertyValues());
        }
        TypeConverter converter = getCustomTypeConverter();
        if (converter == null) {
            converter = bw;
        }
        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);
        // 创建一个深拷贝，解析任何对值的引用。
        List<PropertyValue> deepCopy = new ArrayList<>(original.size());
        boolean resolveNecessary = false;
        for (PropertyValue pv : original) {
            if (pv.isConverted()) {
                deepCopy.add(pv);
            } else {
                String propertyName = pv.getName();
                Object originalValue = pv.getValue();
                if (originalValue == AutowiredPropertyMarker.INSTANCE) {
                    Method writeMethod = bw.getPropertyDescriptor(propertyName).getWriteMethod();
                    if (writeMethod == null) {
                        throw new IllegalArgumentException("Autowire marker for property without write method: " + pv);
                    }
                    originalValue = new DependencyDescriptor(new MethodParameter(writeMethod, 0), true);
                }
                Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
                Object convertedValue = resolvedValue;
                boolean convertible = bw.isWritableProperty(propertyName) && !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
                if (convertible) {
                    convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
                }
                // 可能将转换后的值存储在合并的Bean定义中，
                // 为了避免为每个创建的bean实例进行重新转换。
                if (resolvedValue == originalValue) {
                    if (convertible) {
                        pv.setConvertedValue(convertedValue);
                    }
                    deepCopy.add(pv);
                } else if (convertible && originalValue instanceof TypedStringValue typedStringValue && !typedStringValue.isDynamic() && !(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
                    pv.setConvertedValue(convertedValue);
                    deepCopy.add(pv);
                } else {
                    resolveNecessary = true;
                    deepCopy.add(new PropertyValue(pv, convertedValue));
                }
            }
        }
        if (mpvs != null && !resolveNecessary) {
            mpvs.setConverted();
        }
        // 设置我们的（可能已调整的）深拷贝。
        try {
            bw.setPropertyValues(new MutablePropertyValues(deepCopy));
        } catch (BeansException ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
        }
    }

    /**
     * 将给定的值转换为指定的目标属性值。
     */
    @Nullable
    private Object convertForProperty(@Nullable Object value, String propertyName, BeanWrapper bw, TypeConverter converter) {
        if (converter instanceof BeanWrapperImpl beanWrapper) {
            return beanWrapper.convertForProperty(value, propertyName);
        } else {
            PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
            MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
            return converter.convertIfNecessary(value, pd.getPropertyType(), methodParam);
        }
    }

    /**
     * 初始化给定的Bean实例，应用工厂回调以及初始化方法和Bean后处理器。
     * <p>由 {@link #createBean} 在传统定义的Bean中调用，以及由 {@link #initializeBean} 在现有Bean实例中调用。
     * @param beanName 工厂中Bean的名称（用于调试目的）
     * @param bean 可能需要初始化的新Bean实例
     * @param mbd Bean创建时所使用的Bean定义（也可以为null，如果给定一个现有的Bean实例）
     * @return 初始化后的Bean实例（可能被包装）
     * @see BeanNameAware
     * @see BeanClassLoaderAware
     * @see BeanFactoryAware
     * @see #applyBeanPostProcessorsBeforeInitialization
     * @see #invokeInitMethods
     * @see #applyBeanPostProcessorsAfterInitialization
     */
    protected Object initializeBean(String beanName, Object bean, @Nullable RootBeanDefinition mbd) {
        invokeAwareMethods(beanName, bean);
        Object wrappedBean = bean;
        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
        }
        try {
            invokeInitMethods(beanName, wrappedBean, mbd);
        } catch (Throwable ex) {
            throw new BeanCreationException((mbd != null ? mbd.getResourceDescription() : null), beanName, ex.getMessage(), ex);
        }
        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }
        return wrappedBean;
    }

    private void invokeAwareMethods(String beanName, Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof BeanNameAware beanNameAware) {
                beanNameAware.setBeanName(beanName);
            }
            if (bean instanceof BeanClassLoaderAware beanClassLoaderAware) {
                ClassLoader bcl = getBeanClassLoader();
                if (bcl != null) {
                    beanClassLoaderAware.setBeanClassLoader(bcl);
                }
            }
            if (bean instanceof BeanFactoryAware beanFactoryAware) {
                beanFactoryAware.setBeanFactory(AbstractAutowireCapableBeanFactory.this);
            }
        }
    }

    /**
     * 给一个Bean一个机会在所有属性设置完毕后初始化自己，并让它有机会了解其所属的Bean工厂（此对象）。
     * <p>这意味着检查Bean是否实现了{@link InitializingBean}或定义了任何自定义的初始化方法，如果实现了，则调用必要的回调。
     * @param beanName 工厂中Bean的名称（用于调试目的）
     * @param bean 我们可能需要初始化的新Bean实例
     * @param mbd Bean创建时使用的合并后的Bean定义（也可以为{@code null}，如果提供了一个现有的Bean实例）
     * @throws Throwable 如果初始化方法或调用过程抛出
     * @see #invokeCustomInitMethod
     */
    protected void invokeInitMethods(String beanName, Object bean, @Nullable RootBeanDefinition mbd) throws Throwable {
        boolean isInitializingBean = (bean instanceof InitializingBean);
        if (isInitializingBean && (mbd == null || !mbd.hasAnyExternallyManagedInitMethod("afterPropertiesSet"))) {
            if (logger.isTraceEnabled()) {
                logger.trace("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
            }
            ((InitializingBean) bean).afterPropertiesSet();
        }
        if (mbd != null && bean.getClass() != NullBean.class) {
            String[] initMethodNames = mbd.getInitMethodNames();
            if (initMethodNames != null) {
                for (String initMethodName : initMethodNames) {
                    if (StringUtils.hasLength(initMethodName) && !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) && !mbd.hasAnyExternallyManagedInitMethod(initMethodName)) {
                        invokeCustomInitMethod(beanName, bean, mbd, initMethodName);
                    }
                }
            }
        }
    }

    /**
     * 在给定的 bean 上调用指定的自定义初始化方法。
     * <p>由 {@link #invokeInitMethods(String, Object, RootBeanDefinition)} 调用。
     * <p>子类可以覆盖此方法以自定义带有参数的初始化方法的解析。
     * @see #invokeInitMethods
     */
    protected void invokeCustomInitMethod(String beanName, Object bean, RootBeanDefinition mbd, String initMethodName) throws Throwable {
        Class<?> beanClass = bean.getClass();
        MethodDescriptor descriptor = MethodDescriptor.create(beanName, beanClass, initMethodName);
        String methodName = descriptor.methodName();
        Method initMethod = (mbd.isNonPublicAccessAllowed() ? BeanUtils.findMethod(descriptor.declaringClass(), methodName) : ClassUtils.getMethodIfAvailable(beanClass, methodName));
        if (initMethod == null) {
            if (mbd.isEnforceInitMethod()) {
                throw new BeanDefinitionValidationException("Could not find an init method named '" + methodName + "' on bean with name '" + beanName + "'");
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("No default init method named '" + methodName + "' found on bean with name '" + beanName + "'");
                }
                // 忽略不存在的默认生命周期方法。
                return;
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Invoking init method '" + methodName + "' on bean with name '" + beanName + "'");
        }
        Method methodToInvoke = ClassUtils.getInterfaceMethodIfPossible(initMethod, beanClass);
        try {
            ReflectionUtils.makeAccessible(methodToInvoke);
            methodToInvoke.invoke(bean);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    /**
     * 应用所有已注册的BeanPostProcessor的`postProcessAfterInitialization`回调，给它们一个机会对从FactoryBeans获取的对象进行后处理（例如，自动代理它们）。
     * @see #applyBeanPostProcessorsAfterInitialization
     */
    @Override
    protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
        return applyBeanPostProcessorsAfterInitialization(object, beanName);
    }

    /**
     * 重写此方法以清除 FactoryBean 实例缓存。
     */
    @Override
    protected void removeSingleton(String beanName) {
        synchronized (getSingletonMutex()) {
            super.removeSingleton(beanName);
            this.factoryBeanInstanceCache.remove(beanName);
        }
    }

    /**
     * 重写此方法以清除 FactoryBean 实例缓存。
     */
    @Override
    protected void clearSingletonCache() {
        synchronized (getSingletonMutex()) {
            super.clearSingletonCache();
            this.factoryBeanInstanceCache.clear();
        }
    }

    /**
     * 向协作代理公开记录器。
     * @since 5.0.7
     */
    Log getLogger() {
        return logger;
    }

    /**
     * 为 `#createBean` 调用提供的 `@RootBeanDefinition` 子类，除了默认构造函数外，还支持灵活选择 Kotlin 的主构造函数/单个公共构造函数/单个非公共构造函数候选者。
     * @see BeanUtils#getResolvableConstructor(Class)
     */
    @SuppressWarnings("serial")
    private static class CreateFromClassBeanDefinition extends RootBeanDefinition {

        public CreateFromClassBeanDefinition(Class<?> beanClass) {
            super(beanClass);
        }

        public CreateFromClassBeanDefinition(CreateFromClassBeanDefinition original) {
            super(original);
        }

        @Override
        @Nullable
        public Constructor<?>[] getPreferredConstructors() {
            return ConstructorResolver.determinePreferredConstructors(getBeanClass());
        }

        @Override
        public RootBeanDefinition cloneBeanDefinition() {
            return new CreateFromClassBeanDefinition(this);
        }
    }

    /**
     * 专为 Spring 旧版 autowire="byType" 模式设计的特殊 DependencyDescriptor 变体。
     * 总是可选的；在选择主候选者时，不考虑参数名称。
     */
    @SuppressWarnings("serial")
    private static class AutowireByTypeDependencyDescriptor extends DependencyDescriptor {

        public AutowireByTypeDependencyDescriptor(MethodParameter methodParameter, boolean eager) {
            super(methodParameter, false, eager);
        }

        @Override
        @Nullable
        public String getDependencyName() {
            return null;
        }
    }

    /**
     * 使用 {@link MethodCallback} 来查找 {@link FactoryBean} 类型的信息。
     */
    private static class FactoryBeanMethodTypeFinder implements MethodCallback {

        private final String factoryMethodName;

        private ResolvableType result = ResolvableType.NONE;

        FactoryBeanMethodTypeFinder(String factoryMethodName) {
            this.factoryMethodName = factoryMethodName;
        }

        @Override
        public void doWith(Method method) throws IllegalArgumentException {
            if (isFactoryBeanMethod(method)) {
                ResolvableType returnType = ResolvableType.forMethodReturnType(method);
                ResolvableType candidate = returnType.as(FactoryBean.class).getGeneric();
                if (this.result == ResolvableType.NONE) {
                    this.result = candidate;
                } else {
                    Class<?> resolvedResult = this.result.resolve();
                    Class<?> commonAncestor = ClassUtils.determineCommonAncestor(candidate.resolve(), resolvedResult);
                    if (!ObjectUtils.nullSafeEquals(resolvedResult, commonAncestor)) {
                        this.result = ResolvableType.forClass(commonAncestor);
                    }
                }
            }
        }

        private boolean isFactoryBeanMethod(Method method) {
            return (method.getName().equals(this.factoryMethodName) && FactoryBean.class.isAssignableFrom(method.getReturnType()));
        }

        ResolvableType getResult() {
            Class<?> resolved = this.result.resolve();
            boolean foundResult = resolved != null && resolved != Object.class;
            return (foundResult ? this.result : ResolvableType.NONE);
        }
    }
}
