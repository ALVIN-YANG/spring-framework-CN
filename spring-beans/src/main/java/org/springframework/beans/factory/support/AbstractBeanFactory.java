// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按 "原样" 分发，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性还是其特定用途。
* 请参阅许可证了解管理权限和限制的具体语言。*/
package org.springframework.beans.factory.support;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.DecoratingClassLoader;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.log.LogMessage;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 *  用于实现 {@link org.springframework.beans.factory.BeanFactory} 的抽象基类，提供
 *  {@link org.springframework.beans.factory.config.ConfigurableBeanFactory} SPI 的全部功能。
 *  <i>不</i>假设是可列表的bean工厂：因此也可以用作从某些后端资源（其中bean定义访问是一个昂贵的操作）
 *  获取bean定义的bean工厂实现的基础类。
 *
 * <p>此类通过其基类
 *  {@link org.springframework.beans.factory.support.DefaultSingletonBeanRegistry}
 *  提供单例缓存（单例/原型确定、处理
 *  {@link org.springframework.beans.factory.FactoryBean}、别名、子bean定义合并以及bean销毁（
 *  实现
 *  {@link org.springframework.beans.factory.DisposableBean} 接口、自定义销毁方法）。此外，它可以通过实现
 *  接口管理bean工厂层次结构（在未知bean的情况下委托给父级）。
 *
 *  <p>子类需要实现的主要模板方法是
 *  {@link #getBeanDefinition} 和
 *  {@link #createBean}，分别用于获取给定bean名称的bean定义和为给定bean定义创建bean实例。
 *  这些操作的默认实现可以在
 *  {@link DefaultListableBeanFactory} 和
 *  {@link AbstractAutowireCapableBeanFactory} 中找到。
 *
 * @作者 Rod Johnson
 * @作者 Juergen Hoeller
 * @作者 Costin Leau
 * @作者 Chris Beams
 * @作者 Phillip Webb
 * @作者 Sam Brannen
 * @since 2001年4月15日
 * @see #getBeanDefinition
 * @see #createBean
 * @see AbstractAutowireCapableBeanFactory#createBean
 * @see DefaultListableBeanFactory#getBeanDefinition
 */
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {

    /**
     * 父级 Bean 工厂，用于支持 Bean 继承。
     */
    @Nullable
    private BeanFactory parentBeanFactory;

    /**
     * 用于解析 bean 类名称的 ClassLoader，如有必要。
     */
    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    /**
     * 用于临时解析必要时的bean类名的类加载器。
     */
    @Nullable
    private ClassLoader tempClassLoader;

    /**
     * 是否缓存 Bean 元数据，还是每次访问时重新获取它。
     */
    private boolean cacheBeanMetadata = true;

    /**
     * 用于Bean定义值中表达式的解析策略。
     */
    @Nullable
    private BeanExpressionResolver beanExpressionResolver;

    /**
     * 使用 Spring ConversionService 代替 PropertyEditors。
     */
    @Nullable
    private ConversionService conversionService;

    /**
     * 自定义属性编辑器注册器，用于应用于此工厂的bean。
     */
    private final Set<PropertyEditorRegistrar> propertyEditorRegistrars = new LinkedHashSet<>(4);

    /**
     * 为该工厂的bean应用的自定义属性编辑器。
     */
    private final Map<Class<?>, Class<? extends PropertyEditor>> customEditors = new HashMap<>(4);

    /**
     * 一个自定义的TypeConverter，用于覆盖默认的PropertyEditor机制。
     */
    @Nullable
    private TypeConverter typeConverter;

    /**
     * 字符串解析器，例如用于注解属性值。
     */
    private final List<StringValueResolver> embeddedValueResolvers = new CopyOnWriteArrayList<>();

    /**
     * 要应用的 Bean 后置处理器。
     */
    private final List<BeanPostProcessor> beanPostProcessors = new BeanPostProcessorCacheAwareList();

    /**
     * 预过滤后处理器的缓存。
     */
    @Nullable
    private BeanPostProcessorCache beanPostProcessorCache;

    /**
     * 将作用域标识符 String 映射到相应的 Scope。
     */
    private final Map<String, Scope> scopes = new LinkedHashMap<>(8);

    /**
     * 应用程序启动指标。
     */
    private ApplicationStartup applicationStartup = ApplicationStartup.DEFAULT;

    /**
     * 从 Bean 名称到合并后的 RootBeanDefinition 的映射。
     */
    private final Map<String, RootBeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<>(256);

    /**
     * 已经至少创建过一次的 bean 的名称。
     */
    private final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

    /**
     * 当前正在创建的 Bean 名称。
     */
    private final ThreadLocal<Object> prototypesCurrentlyInCreation = new NamedThreadLocal<>("Prototype beans currently in creation");

    /**
     * 创建一个新的 AbstractBeanFactory。
     */
    public AbstractBeanFactory() {
    }

    /**
     * 创建一个新的 AbstractBeanFactory，使用给定的父类。
     * @param parentBeanFactory 父级bean工厂，如果没有则使用 {@code null}
     * @see #getBean
     */
    public AbstractBeanFactory(@Nullable BeanFactory parentBeanFactory) {
        this.parentBeanFactory = parentBeanFactory;
    }

    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您翻译成中文。
    // BeanFactory接口的实现
    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释部分，我将会为您翻译成中文。
    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null, null, false);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return doGetBean(name, requiredType, null, false);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return doGetBean(name, null, args, false);
    }

    /**
     * 返回指定bean的实例，该实例可以是共享的或独立的。
     * @param name 要检索的bean的名称
     * @param requiredType 要检索的bean所需的数据类型
     * @param args 使用显式参数创建bean实例时使用的参数（仅当创建新实例而不是检索现有实例时适用）
     * @return bean的实例
     * @throws BeansException 如果无法创建bean
     */
    public <T> T getBean(String name, @Nullable Class<T> requiredType, @Nullable Object... args) throws BeansException {
        return doGetBean(name, requiredType, args, false);
    }

    /**
     * 返回指定Bean的实例，该实例可能是共享的或独立的。
     * @param name 要检索的Bean的名称
     * @param requiredType 要检索的Bean的所需类型
     * @param args 使用显式参数创建Bean实例时要使用的参数（仅当创建新实例而不是检索现有实例时适用）
     * @param typeCheckOnly 是否仅用于类型检查，而不是实际使用
     * @return Bean的实例
     * @throws BeansException 如果无法创建Bean
     */
    @SuppressWarnings("unchecked")
    protected <T> T doGetBean(String name, @Nullable Class<T> requiredType, @Nullable Object[] args, boolean typeCheckOnly) throws BeansException {
        String beanName = transformedBeanName(name);
        Object beanInstance;
        // 积极检查手动注册的单例缓存。
        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null && args == null) {
            if (logger.isTraceEnabled()) {
                if (isSingletonCurrentlyInCreation(beanName)) {
                    logger.trace("Returning eagerly cached instance of singleton bean '" + beanName + "' that is not fully initialized yet - a consequence of a circular reference");
                } else {
                    logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
                }
            }
            beanInstance = getObjectForBeanInstance(sharedInstance, name, beanName, null);
        } else {
            // 如果当前正在创建此 Bean 实例，则失败：
            // 我们可能处于一个循环引用中。
            if (isPrototypeCurrentlyInCreation(beanName)) {
                throw new BeanCurrentlyInCreationException(beanName);
            }
            // 检查该工厂中是否存在Bean定义。
            BeanFactory parentBeanFactory = getParentBeanFactory();
            if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
                // 未找到 -> 检查父节点。
                String nameToLookup = originalBeanName(name);
                if (parentBeanFactory instanceof AbstractBeanFactory abf) {
                    return abf.doGetBean(nameToLookup, requiredType, args, typeCheckOnly);
                } else if (args != null) {
                    // 显式参数传递给父类委托。
                    return (T) parentBeanFactory.getBean(nameToLookup, args);
                } else if (requiredType != null) {
                    // 无参数 -> 委派给标准的getBean方法。
                    return parentBeanFactory.getBean(nameToLookup, requiredType);
                } else {
                    return (T) parentBeanFactory.getBean(nameToLookup);
                }
            }
            if (!typeCheckOnly) {
                markBeanAsCreated(beanName);
            }
            StartupStep beanCreation = this.applicationStartup.start("spring.beans.instantiate").tag("beanName", name);
            try {
                if (requiredType != null) {
                    beanCreation.tag("beanType", requiredType::toString);
                }
                RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                checkMergedBeanDefinition(mbd, beanName, args);
                // 确保初始化当前Bean所依赖的Bean。
                String[] dependsOn = mbd.getDependsOn();
                if (dependsOn != null) {
                    for (String dep : dependsOn) {
                        if (isDependent(beanName, dep)) {
                            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
                        }
                        registerDependentBean(dep, beanName);
                        try {
                            getBean(dep);
                        } catch (NoSuchBeanDefinitionException ex) {
                            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "'" + beanName + "' depends on missing bean '" + dep + "'", ex);
                        }
                    }
                }
                // 创建 bean 实例。
                if (mbd.isSingleton()) {
                    sharedInstance = getSingleton(beanName, () -> {
                        try {
                            return createBean(beanName, mbd, args);
                        } catch (BeansException ex) {
                            // 显式地从单例缓存中移除实例：它可能已经被放入其中
                            // 通过创建过程预先加载，以允许解决循环引用。
                            // 同时移除任何获得临时引用的该 Bean。
                            destroySingleton(beanName);
                            throw ex;
                        }
                    });
                    beanInstance = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
                } else if (mbd.isPrototype()) {
                    // 这是一个原型 -> 创建一个新的实例。
                    Object prototypeInstance = null;
                    try {
                        beforePrototypeCreation(beanName);
                        prototypeInstance = createBean(beanName, mbd, args);
                    } finally {
                        afterPrototypeCreation(beanName);
                    }
                    beanInstance = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
                } else {
                    String scopeName = mbd.getScope();
                    if (!StringUtils.hasLength(scopeName)) {
                        throw new IllegalStateException("No scope name defined for bean '" + beanName + "'");
                    }
                    Scope scope = this.scopes.get(scopeName);
                    if (scope == null) {
                        throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
                    }
                    try {
                        Object scopedInstance = scope.get(beanName, () -> {
                            beforePrototypeCreation(beanName);
                            try {
                                return createBean(beanName, mbd, args);
                            } finally {
                                afterPrototypeCreation(beanName);
                            }
                        });
                        beanInstance = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
                    } catch (IllegalStateException ex) {
                        throw new ScopeNotActiveException(beanName, scopeName, ex);
                    }
                }
            } catch (BeansException ex) {
                beanCreation.tag("exception", ex.getClass().toString());
                beanCreation.tag("message", String.valueOf(ex.getMessage()));
                cleanupAfterBeanCreationFailure(beanName);
                throw ex;
            } finally {
                beanCreation.end();
            }
        }
        return adaptBeanInstance(name, beanInstance, requiredType);
    }

    @SuppressWarnings("unchecked")
    <T> T adaptBeanInstance(String name, Object bean, @Nullable Class<?> requiredType) {
        // 检查所需类型是否与实际bean实例的类型匹配。
        if (requiredType != null && !requiredType.isInstance(bean)) {
            try {
                Object convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
                if (convertedBean == null) {
                    throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
                }
                return (T) convertedBean;
            } catch (TypeMismatchException ex) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Failed to convert bean '" + name + "' to required type '" + ClassUtils.getQualifiedName(requiredType) + "'", ex);
                }
                throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
            }
        }
        return (T) bean;
    }

    @Override
    public boolean containsBean(String name) {
        String beanName = transformedBeanName(name);
        if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
            return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(name));
        }
        // 未找到 -> 检查父节点。
        BeanFactory parentBeanFactory = getParentBeanFactory();
        return (parentBeanFactory != null && parentBeanFactory.containsBean(originalBeanName(name)));
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean<?> factoryBean) {
                return (BeanFactoryUtils.isFactoryDereference(name) || factoryBean.isSingleton());
            } else {
                return !BeanFactoryUtils.isFactoryDereference(name);
            }
        }
        // 未找到单例实例 -> 检查bean定义。
        BeanFactory parentBeanFactory = getParentBeanFactory();
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            // 在此工厂中未找到任何 Bean 定义 -> 委托给父级。
            return parentBeanFactory.isSingleton(originalBeanName(name));
        }
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        // 如果是一个FactoryBean，如果对象不是解引用的，则返回创建的对象的单例状态。
        if (mbd.isSingleton()) {
            if (isFactoryBean(beanName, mbd)) {
                if (BeanFactoryUtils.isFactoryDereference(name)) {
                    return true;
                }
                FactoryBean<?> factoryBean = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
                return factoryBean.isSingleton();
            } else {
                return !BeanFactoryUtils.isFactoryDereference(name);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);
        BeanFactory parentBeanFactory = getParentBeanFactory();
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            // 在这个工厂中未找到任何 Bean 定义 -> 委托给父工厂。
            return parentBeanFactory.isPrototype(originalBeanName(name));
        }
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        if (mbd.isPrototype()) {
            // 在FactoryBean的情况下，如果对象不是解引用的，则返回创建对象的单例状态。
            return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName, mbd));
        }
        // 单例或作用域内 - 不是原型。
        // 然而，FactoryBean仍然可能生产一个原型对象...
        if (BeanFactoryUtils.isFactoryDereference(name)) {
            return false;
        }
        if (isFactoryBean(beanName, mbd)) {
            FactoryBean<?> fb = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
            return ((fb instanceof SmartFactoryBean<?> smartFactoryBean && smartFactoryBean.isPrototype()) || !fb.isSingleton());
        } else {
            return false;
        }
    }

    @Override
    public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        return isTypeMatch(name, typeToMatch, true);
    }

    /**
     * 内部扩展版本的 {@link #isTypeMatch(String, ResolvableType)}
     * 用于检查给定名称的 Bean 是否与指定的类型匹配。允许应用额外的约束以确保 Bean 不会被提前创建。
     * @param name 要查询的 Bean 的名称
     * @param typeToMatch 要匹配的类型（作为 {@code ResolvableType}）
     * @return 如果 Bean 类型匹配，则返回 {@code true}，如果不匹配或尚不能确定，则返回 {@code false}
     * @throws NoSuchBeanDefinitionException 如果没有给定名称的 Bean
     * @since 5.2
     * @see #getBean
     * @see #getType
     */
    protected boolean isTypeMatch(String name, ResolvableType typeToMatch, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);
        boolean isFactoryDereference = BeanFactoryUtils.isFactoryDereference(name);
        // 检查手动注册的单例。
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null && beanInstance.getClass() != NullBean.class) {
            if (beanInstance instanceof FactoryBean<?> factoryBean) {
                if (!isFactoryDereference) {
                    Class<?> type = getTypeForFactoryBean(factoryBean);
                    return (type != null && typeToMatch.isAssignableFrom(type));
                } else {
                    return typeToMatch.isInstance(beanInstance);
                }
            } else if (!isFactoryDereference) {
                if (typeToMatch.isInstance(beanInstance)) {
                    // 直接匹配暴露的实例？
                    return true;
                } else if (typeToMatch.hasGenerics() && containsBeanDefinition(beanName)) {
                    // 泛型可能仅在目标类上匹配，而不是在代理上匹配。
                    RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                    Class<?> targetType = mbd.getTargetType();
                    if (targetType != null && targetType != ClassUtils.getUserClass(beanInstance)) {
                        // 同时检查原始类匹配，确保其在代理上已暴露。
                        Class<?> classToMatch = typeToMatch.resolve();
                        if (classToMatch != null && !classToMatch.isInstance(beanInstance)) {
                            return false;
                        }
                        if (typeToMatch.isAssignableFrom(targetType)) {
                            return true;
                        }
                    }
                    ResolvableType resolvableType = mbd.targetType;
                    if (resolvableType == null) {
                        resolvableType = mbd.factoryMethodReturnType;
                    }
                    return (resolvableType != null && typeToMatch.isAssignableFrom(resolvableType));
                }
            }
            return false;
        } else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            // 未注册实例
            return false;
        }
        // 未找到单例实例 -> 检查bean定义。
        BeanFactory parentBeanFactory = getParentBeanFactory();
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            // 在此工厂中未找到任何 Bean 定义 -> 委托给父工厂。
            return parentBeanFactory.isTypeMatch(originalBeanName(name), typeToMatch);
        }
        // 检索相应的 Bean 定义。
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
        // 设置我们想要进行匹配的类型
        Class<?> classToMatch = typeToMatch.resolve();
        if (classToMatch == null) {
            classToMatch = FactoryBean.class;
        }
        Class<?>[] typesToMatch = (FactoryBean.class == classToMatch ? new Class<?>[] { classToMatch } : new Class<?>[] { FactoryBean.class, classToMatch });
        // 尝试预测豆类类型
        Class<?> predictedType = null;
        // 我们正在寻找一个常规引用，但我们是一个工厂Bean，它有
        // 一个装饰过的 Bean 定义。目标 Bean 应该是同一类型
        // 正如FactoryBean最终会返回的。
        if (!isFactoryDereference && dbd != null && isFactoryBean(beanName, mbd)) {
            // 我们只有在用户明确将 lazy-init 设置为 true 时才尝试
            // 我们知道合并的Bean定义是用于一个工厂Bean的。
            if (!mbd.isLazyInit() || allowFactoryBeanInit) {
                RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
                Class<?> targetType = predictBeanType(dbd.getBeanName(), tbd, typesToMatch);
                if (targetType != null && !FactoryBean.class.isAssignableFrom(targetType)) {
                    predictedType = targetType;
                }
            }
        }
        // 如果我们不能使用目标类型，则尝试进行常规预测。
        if (predictedType == null) {
            predictedType = predictBeanType(beanName, mbd, typesToMatch);
            if (predictedType == null) {
                return false;
            }
        }
        // 尝试获取该 Bean 的实际 ResolvableType。
        ResolvableType beanType = null;
        // 如果这是一个FactoryBean，我们想要查看它创建的对象，而不是工厂类。
        if (FactoryBean.class.isAssignableFrom(predictedType)) {
            if (beanInstance == null && !isFactoryDereference) {
                beanType = getTypeForFactoryBean(beanName, mbd, allowFactoryBeanInit);
                predictedType = beanType.resolve();
                if (predictedType == null) {
                    return false;
                }
            }
        } else if (isFactoryDereference) {
            // 特殊情况：一个 SmartInstantiationAwareBeanPostProcessor 返回了一个非 FactoryBean
            // 类型，但我们仍然被要求取消 FactoryBean 的引用...
            // 让我们检查原始的Bean类，如果它是FactoryBean，就继续使用它。
            predictedType = predictBeanType(beanName, mbd, FactoryBean.class);
            if (predictedType == null || !FactoryBean.class.isAssignableFrom(predictedType)) {
                return false;
            }
        }
        // 我们没有一个确切的类型，但如果目标类型是bean定义或工厂类型：
        // 方法返回类型与预测类型匹配时，我们可以使用它。
        if (beanType == null) {
            ResolvableType definedType = mbd.targetType;
            if (definedType == null) {
                definedType = mbd.factoryMethodReturnType;
            }
            if (definedType != null && definedType.resolve() == predictedType) {
                beanType = definedType;
            }
        }
        // 如果我们有一个 bean 类型，则使用它以考虑泛型
        if (beanType != null) {
            return typeToMatch.isAssignableFrom(beanType);
        }
        // 如果我们没有bean类型，则回退到预测的类型
        return typeToMatch.isAssignableFrom(predictedType);
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return isTypeMatch(name, ResolvableType.forRawClass(typeToMatch));
    }

    @Override
    @Nullable
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return getType(name, true);
    }

    @Override
    @Nullable
    public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);
        // 检查手动注册的单例。
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null && beanInstance.getClass() != NullBean.class) {
            if (beanInstance instanceof FactoryBean<?> factoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
                return getTypeForFactoryBean(factoryBean);
            } else {
                return beanInstance.getClass();
            }
        }
        // 未找到单例实例 -> 检查Bean定义。
        BeanFactory parentBeanFactory = getParentBeanFactory();
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            // 在这个工厂中没有找到任何 Bean 定义 -> 委派给父类。
            return parentBeanFactory.getType(originalBeanName(name));
        }
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        Class<?> beanClass = predictBeanType(beanName, mbd);
        if (beanClass != null) {
            // 检查 Bean 类，判断我们是否处理的是 FactoryBean。
            if (FactoryBean.class.isAssignableFrom(beanClass)) {
                if (!BeanFactoryUtils.isFactoryDereference(name)) {
                    // 如果这是一个FactoryBean，我们希望查看它所创建的对象，而不是查看工厂类。
                    beanClass = getTypeForFactoryBean(beanName, mbd, allowFactoryBeanInit).resolve();
                }
            } else if (BeanFactoryUtils.isFactoryDereference(name)) {
                return null;
            }
        }
        if (beanClass == null) {
            // 检查装饰过的bean定义（如果有）：我们假设这会更容易
            // 确定装饰后的Bean的类型而不是代理的类型。
            BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
            if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
                RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
                Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd);
                if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
                    return targetClass;
                }
            }
        }
        return beanClass;
    }

    @Override
    public String[] getAliases(String name) {
        String beanName = transformedBeanName(name);
        List<String> aliases = new ArrayList<>();
        boolean factoryPrefix = name.startsWith(FACTORY_BEAN_PREFIX);
        String fullBeanName = beanName;
        if (factoryPrefix) {
            fullBeanName = FACTORY_BEAN_PREFIX + beanName;
        }
        if (!fullBeanName.equals(name)) {
            aliases.add(fullBeanName);
        }
        String[] retrievedAliases = super.getAliases(beanName);
        String prefix = (factoryPrefix ? FACTORY_BEAN_PREFIX : "");
        for (String retrievedAlias : retrievedAliases) {
            String alias = prefix + retrievedAlias;
            if (!alias.equals(name)) {
                aliases.add(alias);
            }
        }
        if (!containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            BeanFactory parentBeanFactory = getParentBeanFactory();
            if (parentBeanFactory != null) {
                aliases.addAll(Arrays.asList(parentBeanFactory.getAliases(fullBeanName)));
            }
        }
        return StringUtils.toStringArray(aliases);
    }

    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供需要翻译的英文代码注释，我将为您翻译成中文。
    // 实现 HierarchicalBeanFactory 接口
    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您进行准确的翻译。
    @Override
    @Nullable
    public BeanFactory getParentBeanFactory() {
        return this.parentBeanFactory;
    }

    @Override
    public boolean containsLocalBean(String name) {
        String beanName = transformedBeanName(name);
        return ((containsSingleton(beanName) || containsBeanDefinition(beanName)) && (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName)));
    }

    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您翻译成中文。
    // ConfigurableBeanFactory 接口的实现
    // 很抱歉，您提供的内容“---------------------------------------------------------------------”并不是有效的Java代码注释，而是一串横线。因此，没有注释内容可以翻译。如果您能提供具体的Java代码注释内容，我将很乐意为您翻译成中文。
    @Override
    public void setParentBeanFactory(@Nullable BeanFactory parentBeanFactory) {
        if (this.parentBeanFactory != null && this.parentBeanFactory != parentBeanFactory) {
            throw new IllegalStateException("Already associated with parent BeanFactory: " + this.parentBeanFactory);
        }
        if (this == parentBeanFactory) {
            throw new IllegalStateException("Cannot set parent bean factory to self");
        }
        this.parentBeanFactory = parentBeanFactory;
    }

    @Override
    public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
        this.beanClassLoader = (beanClassLoader != null ? beanClassLoader : ClassUtils.getDefaultClassLoader());
    }

    @Override
    @Nullable
    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    @Override
    public void setTempClassLoader(@Nullable ClassLoader tempClassLoader) {
        this.tempClassLoader = tempClassLoader;
    }

    @Override
    @Nullable
    public ClassLoader getTempClassLoader() {
        return this.tempClassLoader;
    }

    @Override
    public void setCacheBeanMetadata(boolean cacheBeanMetadata) {
        this.cacheBeanMetadata = cacheBeanMetadata;
    }

    @Override
    public boolean isCacheBeanMetadata() {
        return this.cacheBeanMetadata;
    }

    @Override
    public void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver) {
        this.beanExpressionResolver = resolver;
    }

    @Override
    @Nullable
    public BeanExpressionResolver getBeanExpressionResolver() {
        return this.beanExpressionResolver;
    }

    @Override
    public void setConversionService(@Nullable ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    @Nullable
    public ConversionService getConversionService() {
        return this.conversionService;
    }

    @Override
    public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
        Assert.notNull(registrar, "PropertyEditorRegistrar must not be null");
        this.propertyEditorRegistrars.add(registrar);
    }

    /**
     * 返回属性编辑器注册器的集合。
     */
    public Set<PropertyEditorRegistrar> getPropertyEditorRegistrars() {
        return this.propertyEditorRegistrars;
    }

    @Override
    public void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass) {
        Assert.notNull(requiredType, "Required type must not be null");
        Assert.notNull(propertyEditorClass, "PropertyEditor class must not be null");
        this.customEditors.put(requiredType, propertyEditorClass);
    }

    @Override
    public void copyRegisteredEditorsTo(PropertyEditorRegistry registry) {
        registerCustomEditors(registry);
    }

    /**
     * 返回自定义编辑器的映射，其中以类（Class）作为键，以属性编辑器（PropertyEditor）类作为值。
     */
    public Map<Class<?>, Class<? extends PropertyEditor>> getCustomEditors() {
        return this.customEditors;
    }

    @Override
    public void setTypeConverter(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    /**
     * 返回要使用的自定义TypeConverter，如果有的话。
     * @return 自定义TypeConverter，如果没有指定则返回{@code null}
     */
    @Nullable
    protected TypeConverter getCustomTypeConverter() {
        return this.typeConverter;
    }

    @Override
    public TypeConverter getTypeConverter() {
        TypeConverter customConverter = getCustomTypeConverter();
        if (customConverter != null) {
            return customConverter;
        } else {
            // 构建默认的TypeConverter，并注册自定义编辑器。
            SimpleTypeConverter typeConverter = new SimpleTypeConverter();
            typeConverter.setConversionService(getConversionService());
            registerCustomEditors(typeConverter);
            return typeConverter;
        }
    }

    @Override
    public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
        Assert.notNull(valueResolver, "StringValueResolver must not be null");
        this.embeddedValueResolvers.add(valueResolver);
    }

    @Override
    public boolean hasEmbeddedValueResolver() {
        return !this.embeddedValueResolvers.isEmpty();
    }

    @Override
    @Nullable
    public String resolveEmbeddedValue(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String result = value;
        for (StringValueResolver resolver : this.embeddedValueResolvers) {
            result = resolver.resolveStringValue(result);
            if (result == null) {
                return null;
            }
        }
        return result;
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");
        synchronized (this.beanPostProcessors) {
            // 从旧位置移除，如果有的话
            this.beanPostProcessors.remove(beanPostProcessor);
            // 添加到列表末尾
            this.beanPostProcessors.add(beanPostProcessor);
        }
    }

    /**
     * 添加新的BeanPostProcessors，这些处理器将应用于由该工厂创建的Bean。在工厂配置期间调用。
     * @since 5.3
     * @see #addBeanPostProcessor
     */
    public void addBeanPostProcessors(Collection<? extends BeanPostProcessor> beanPostProcessors) {
        synchronized (this.beanPostProcessors) {
            // 从旧位置移除，如果有的话
            this.beanPostProcessors.removeAll(beanPostProcessors);
            // 添加到列表末尾
            this.beanPostProcessors.addAll(beanPostProcessors);
        }
    }

    @Override
    public int getBeanPostProcessorCount() {
        return this.beanPostProcessors.size();
    }

    /**
     * 返回将应用于使用此工厂创建的 Bean 的 BeanPostProcessors 列表。
     */
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    /**
     * 返回内部预过滤后处理器的缓存，
     * 如果需要，则重新（重新）构建它。
     * @since 5.3
     */
    BeanPostProcessorCache getBeanPostProcessorCache() {
        synchronized (this.beanPostProcessors) {
            BeanPostProcessorCache bppCache = this.beanPostProcessorCache;
            if (bppCache == null) {
                bppCache = new BeanPostProcessorCache();
                for (BeanPostProcessor bpp : this.beanPostProcessors) {
                    if (bpp instanceof InstantiationAwareBeanPostProcessor instantiationAwareBpp) {
                        bppCache.instantiationAware.add(instantiationAwareBpp);
                        if (bpp instanceof SmartInstantiationAwareBeanPostProcessor smartInstantiationAwareBpp) {
                            bppCache.smartInstantiationAware.add(smartInstantiationAwareBpp);
                        }
                    }
                    if (bpp instanceof DestructionAwareBeanPostProcessor destructionAwareBpp) {
                        bppCache.destructionAware.add(destructionAwareBpp);
                    }
                    if (bpp instanceof MergedBeanDefinitionPostProcessor mergedBeanDefBpp) {
                        bppCache.mergedDefinition.add(mergedBeanDefBpp);
                    }
                }
                this.beanPostProcessorCache = bppCache;
            }
            return bppCache;
        }
    }

    private void resetBeanPostProcessorCache() {
        synchronized (this.beanPostProcessors) {
            this.beanPostProcessorCache = null;
        }
    }

    /**
     * 返回此工厂是否包含一个将在创建单例bean时应用的`InstantiationAwareBeanPostProcessor`。
     * @see #addBeanPostProcessor
     * @see org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor
     */
    protected boolean hasInstantiationAwareBeanPostProcessors() {
        return !getBeanPostProcessorCache().instantiationAware.isEmpty();
    }

    /**
     * 返回此工厂是否包含一个`DestructionAwareBeanPostProcessor`，该处理器将在关闭时应用于单例bean。
     * @see #addBeanPostProcessor
     * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
     */
    protected boolean hasDestructionAwareBeanPostProcessors() {
        return !getBeanPostProcessorCache().destructionAware.isEmpty();
    }

    @Override
    public void registerScope(String scopeName, Scope scope) {
        Assert.notNull(scopeName, "Scope identifier must not be null");
        Assert.notNull(scope, "Scope must not be null");
        if (SCOPE_SINGLETON.equals(scopeName) || SCOPE_PROTOTYPE.equals(scopeName)) {
            throw new IllegalArgumentException("Cannot replace existing scopes 'singleton' and 'prototype'");
        }
        Scope previous = this.scopes.put(scopeName, scope);
        if (previous != null && previous != scope) {
            if (logger.isDebugEnabled()) {
                logger.debug("Replacing scope '" + scopeName + "' from [" + previous + "] to [" + scope + "]");
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Registering scope '" + scopeName + "' with implementation [" + scope + "]");
            }
        }
    }

    @Override
    public String[] getRegisteredScopeNames() {
        return StringUtils.toStringArray(this.scopes.keySet());
    }

    @Override
    @Nullable
    public Scope getRegisteredScope(String scopeName) {
        Assert.notNull(scopeName, "Scope identifier must not be null");
        return this.scopes.get(scopeName);
    }

    @Override
    public void setApplicationStartup(ApplicationStartup applicationStartup) {
        Assert.notNull(applicationStartup, "ApplicationStartup must not be null");
        this.applicationStartup = applicationStartup;
    }

    @Override
    public ApplicationStartup getApplicationStartup() {
        return this.applicationStartup;
    }

    @Override
    public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
        Assert.notNull(otherFactory, "BeanFactory must not be null");
        setBeanClassLoader(otherFactory.getBeanClassLoader());
        setCacheBeanMetadata(otherFactory.isCacheBeanMetadata());
        setBeanExpressionResolver(otherFactory.getBeanExpressionResolver());
        setConversionService(otherFactory.getConversionService());
        if (otherFactory instanceof AbstractBeanFactory otherAbstractFactory) {
            this.propertyEditorRegistrars.addAll(otherAbstractFactory.propertyEditorRegistrars);
            this.customEditors.putAll(otherAbstractFactory.customEditors);
            this.typeConverter = otherAbstractFactory.typeConverter;
            this.beanPostProcessors.addAll(otherAbstractFactory.beanPostProcessors);
            this.scopes.putAll(otherAbstractFactory.scopes);
        } else {
            setTypeConverter(otherFactory.getTypeConverter());
            String[] otherScopeNames = otherFactory.getRegisteredScopeNames();
            for (String scopeName : otherScopeNames) {
                this.scopes.put(scopeName, otherFactory.getRegisteredScope(scopeName));
            }
        }
    }

    /**
     * 返回给定bean名称的'merged' BeanDefinition，
     * 如果需要，合并子bean定义与其父定义。
     * <p>此{@code getMergedBeanDefinition}考虑了祖先中的bean定义。
     * @param name 要检索合并定义的bean名称（可能是一个别名）
     * @return 给定bean的（可能已合并的）RootBeanDefinition
     * @throws NoSuchBeanDefinitionException 如果没有给定名称的bean
     * @throws BeanDefinitionStoreException 在无效的bean定义的情况下
     */
    @Override
    public BeanDefinition getMergedBeanDefinition(String name) throws BeansException {
        String beanName = transformedBeanName(name);
        // 高效地检查此工厂中是否存在Bean定义。
        if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory parent) {
            return parent.getMergedBeanDefinition(beanName);
        }
        // 本地解决合并的 Bean 定义。
        return getMergedLocalBeanDefinition(beanName);
    }

    @Override
    public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            return (beanInstance instanceof FactoryBean);
        }
        // 未找到单例实例 -> 检查Bean定义。
        if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory cbf) {
            // 在这个工厂中没有找到Bean定义 -> 委派给父工厂。
            return cbf.isFactoryBean(name);
        }
        return isFactoryBean(beanName, getMergedLocalBeanDefinition(beanName));
    }

    @Override
    public boolean isActuallyInCreation(String beanName) {
        return (isSingletonCurrentlyInCreation(beanName) || isPrototypeCurrentlyInCreation(beanName));
    }

    /**
     * 返回指定的原型bean是否当前处于创建状态（在当前线程内）。
     * @param beanName bean的名称
     */
    protected boolean isPrototypeCurrentlyInCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        return (curVal != null && (curVal.equals(beanName) || (curVal instanceof Set<?> set && set.contains(beanName))));
    }

    /**
     * 在原型创建之前的回调。
     * <p>默认实现将原型注册为当前正在创建中。
     * @param beanName 即将创建的原型的名称
     * @see #isPrototypeCurrentlyInCreation
     */
    @SuppressWarnings("unchecked")
    protected void beforePrototypeCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        if (curVal == null) {
            this.prototypesCurrentlyInCreation.set(beanName);
        } else if (curVal instanceof String strValue) {
            Set<String> beanNameSet = new HashSet<>(2);
            beanNameSet.add(strValue);
            beanNameSet.add(beanName);
            this.prototypesCurrentlyInCreation.set(beanNameSet);
        } else {
            Set<String> beanNameSet = (Set<String>) curVal;
            beanNameSet.add(beanName);
        }
    }

    /**
     * 原型创建后的回调。
     * <p>默认实现将原型标记为不再处于创建状态。
     * @param beanName 已创建的原型的名称
     * @see #isPrototypeCurrentlyInCreation
     */
    @SuppressWarnings("unchecked")
    protected void afterPrototypeCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        if (curVal instanceof String) {
            this.prototypesCurrentlyInCreation.remove();
        } else if (curVal instanceof Set<?> beanNameSet) {
            beanNameSet.remove(beanName);
            if (beanNameSet.isEmpty()) {
                this.prototypesCurrentlyInCreation.remove();
            }
        }
    }

    @Override
    public void destroyBean(String beanName, Object beanInstance) {
        destroyBean(beanName, beanInstance, getMergedLocalBeanDefinition(beanName));
    }

    /**
     * 根据给定的bean定义销毁指定的bean实例（通常是从该工厂获得的原型实例）。
     * @param beanName bean定义的名称
     * @param bean 要销毁的bean实例
     * @param mbd 合并后的bean定义
     */
    protected void destroyBean(String beanName, Object bean, RootBeanDefinition mbd) {
        new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessorCache().destructionAware).destroy();
    }

    @Override
    public void destroyScopedBean(String beanName) {
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        if (mbd.isSingleton() || mbd.isPrototype()) {
            throw new IllegalArgumentException("Bean name '" + beanName + "' does not correspond to an object in a mutable scope");
        }
        String scopeName = mbd.getScope();
        Scope scope = this.scopes.get(scopeName);
        if (scope == null) {
            throw new IllegalStateException("No Scope SPI registered for scope name '" + scopeName + "'");
        }
        Object bean = scope.remove(beanName);
        if (bean != null) {
            destroyBean(beanName, bean, mbd);
        }
    }

    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您翻译成中文。
    // 实现方法
    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您翻译成中文。
    /**
     * 返回 Bean 名称，如有必要，将移除工厂引用前缀，并解析别名到规范名称。
     * @param name 用户指定的名称
     * @return 转换后的 Bean 名称
     */
    protected String transformedBeanName(String name) {
        return canonicalName(BeanFactoryUtils.transformedBeanName(name));
    }

    /**
     * 确定原始的 Bean 名称，解析本地定义的别名到规范名称。
     * @param name 用户指定的名称
     * @return 原始的 Bean 名称
     */
    protected String originalBeanName(String name) {
        String beanName = transformedBeanName(name);
        if (name.startsWith(FACTORY_BEAN_PREFIX)) {
            beanName = FACTORY_BEAN_PREFIX + beanName;
        }
        return beanName;
    }

    /**
     * 使用此工厂注册的自定义编辑器初始化给定的BeanWrapper。该方法用于将要创建和填充bean实例的BeanWrapper。
     * <p>默认实现委托给{@link #registerCustomEditors}。可以在子类中重写此方法。
     * @param bw 要初始化的BeanWrapper
     */
    protected void initBeanWrapper(BeanWrapper bw) {
        bw.setConversionService(getConversionService());
        registerCustomEditors(bw);
    }

    /**
     * 初始化给定的PropertyEditorRegistry，使用已在此BeanFactory中注册的自定义编辑器
     * <p>此方法用于BeanWrappers，这些BeanWrappers将创建和填充bean实例，以及用于构造函数参数和工厂方法类型转换的SimpleTypeConverter。
     * @param registry 要初始化的PropertyEditorRegistry
     */
    protected void registerCustomEditors(PropertyEditorRegistry registry) {
        if (registry instanceof PropertyEditorRegistrySupport registrySupport) {
            registrySupport.useConfigValueEditors();
        }
        if (!this.propertyEditorRegistrars.isEmpty()) {
            for (PropertyEditorRegistrar registrar : this.propertyEditorRegistrars) {
                try {
                    registrar.registerCustomEditors(registry);
                } catch (BeanCreationException ex) {
                    Throwable rootCause = ex.getMostSpecificCause();
                    if (rootCause instanceof BeanCurrentlyInCreationException bce) {
                        String bceBeanName = bce.getBeanName();
                        if (bceBeanName != null && isCurrentlyInCreation(bceBeanName)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("PropertyEditorRegistrar [" + registrar.getClass().getName() + "] failed because it tried to obtain currently created bean '" + ex.getBeanName() + "': " + ex.getMessage());
                            }
                            onSuppressedException(ex);
                            continue;
                        }
                    }
                    throw ex;
                }
            }
        }
        if (!this.customEditors.isEmpty()) {
            this.customEditors.forEach((requiredType, editorClass) -> registry.registerCustomEditor(requiredType, BeanUtils.instantiateClass(editorClass)));
        }
    }

    /**
     * 返回一个合并后的RootBeanDefinition，如果指定的bean对应一个子bean定义，则遍历父bean定义。
     * @param beanName 要获取合并定义的bean的名称
     * @return 给定bean的（可能已合并的）RootBeanDefinition
     * @throws NoSuchBeanDefinitionException 如果不存在具有给定名称的bean
     * @throws BeanDefinitionStoreException 在bean定义无效的情况下抛出
     */
    protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
        // 首先进行快速检查并发映射，使用最小化锁定。
        RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
        if (mbd != null && !mbd.stale) {
            return mbd;
        }
        return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
    }

    /**
     * 返回给定顶级Bean的RootBeanDefinition，如果给定Bean的定义是子Bean定义，则与父定义合并。
     * @param beanName Bean定义的名称
     * @param bd 原始Bean定义（Root/ChildBeanDefinition）
     * @return 给定Bean的（可能已合并的）RootBeanDefinition
     * @throws BeanDefinitionStoreException 如果Bean定义无效
     */
    protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd) throws BeanDefinitionStoreException {
        return getMergedBeanDefinition(beanName, bd, null);
    }

    /**
     * 返回给定 bean 的 RootBeanDefinition，如果给定 bean 的定义是子 bean 定义，则与父定义合并。
     * @param beanName bean 定义的名字
     * @param bd 原始的 bean 定义（Root/ChildBeanDefinition）
     * @param containingBd 如果是内部 bean，则为包含的 bean 定义；如果是顶层 bean，则为 null
     * @return 给定 bean 的（可能已合并的）RootBeanDefinition
     * @throws BeanDefinitionStoreException 如果 bean 定义无效
     */
    protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd) throws BeanDefinitionStoreException {
        synchronized (this.mergedBeanDefinitions) {
            RootBeanDefinition mbd = null;
            RootBeanDefinition previous = null;
            // 现在使用完全锁来确保使用相同的合并实例。
            if (containingBd == null) {
                mbd = this.mergedBeanDefinitions.get(beanName);
            }
            if (mbd == null || mbd.stale) {
                previous = mbd;
                if (bd.getParentName() == null) {
                    // 使用给定根bean定义的副本。
                    if (bd instanceof RootBeanDefinition rootBeanDef) {
                        mbd = rootBeanDef.cloneBeanDefinition();
                    } else {
                        mbd = new RootBeanDefinition(bd);
                    }
                } else {
                    // 子bean定义：需要与父bean合并。
                    BeanDefinition pbd;
                    try {
                        String parentBeanName = transformedBeanName(bd.getParentName());
                        if (!beanName.equals(parentBeanName)) {
                            pbd = getMergedBeanDefinition(parentBeanName);
                        } else {
                            if (getParentBeanFactory() instanceof ConfigurableBeanFactory parent) {
                                pbd = parent.getMergedBeanDefinition(parentBeanName);
                            } else {
                                throw new NoSuchBeanDefinitionException(parentBeanName, "Parent name '" + parentBeanName + "' is equal to bean name '" + beanName + "': cannot be resolved without a ConfigurableBeanFactory parent");
                            }
                        }
                    } catch (NoSuchBeanDefinitionException ex) {
                        throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName, "Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
                    }
                    // 深度复制并覆盖值。
                    mbd = new RootBeanDefinition(pbd);
                    mbd.overrideFrom(bd);
                }
                // 如果之前未配置，设置默认的单例作用域。
                if (!StringUtils.hasLength(mbd.getScope())) {
                    mbd.setScope(SCOPE_SINGLETON);
                }
                // 包含在非单例Bean中的Bean本身不能是单例。
                // 让我们在这里即时修正，因为这个可能是以下结果：
                // 对外部Bean进行父子合并，在这种情况下，原始的内部Bean
                // 定义将不会继承合并的外部Bean的单例状态。
                if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
                    mbd.setScope(containingBd.getScope());
                }
                // 暂时缓存合并后的 Bean 定义
                // （它可能稍后仍然会被重新合并，以便获取元数据更改）
                if (containingBd == null && isCacheBeanMetadata()) {
                    this.mergedBeanDefinitions.put(beanName, mbd);
                }
            }
            if (previous != null) {
                copyRelevantMergedBeanDefinitionCaches(previous, mbd);
            }
            return mbd;
        }
    }

    private void copyRelevantMergedBeanDefinitionCaches(RootBeanDefinition previous, RootBeanDefinition mbd) {
        if (ObjectUtils.nullSafeEquals(mbd.getBeanClassName(), previous.getBeanClassName()) && ObjectUtils.nullSafeEquals(mbd.getFactoryBeanName(), previous.getFactoryBeanName()) && ObjectUtils.nullSafeEquals(mbd.getFactoryMethodName(), previous.getFactoryMethodName())) {
            ResolvableType targetType = mbd.targetType;
            ResolvableType previousTargetType = previous.targetType;
            if (targetType == null || targetType.equals(previousTargetType)) {
                mbd.targetType = previousTargetType;
                mbd.isFactoryBean = previous.isFactoryBean;
                mbd.resolvedTargetType = previous.resolvedTargetType;
                mbd.factoryMethodReturnType = previous.factoryMethodReturnType;
                mbd.factoryMethodToIntrospect = previous.factoryMethodToIntrospect;
            }
        }
    }

    /**
     * 检查给定的合并后的 Bean 定义，
     * 可能会抛出验证异常。
     * @param mbd 要检查的合并后的 Bean 定义
     * @param beanName Bean 的名称
     * @param args Bean 创建的参数，如果有
     * @throws BeanDefinitionStoreException 在验证失败的情况下
     */
    protected void checkMergedBeanDefinition(RootBeanDefinition mbd, String beanName, @Nullable Object[] args) throws BeanDefinitionStoreException {
        if (mbd.isAbstract()) {
            throw new BeanIsAbstractException(beanName);
        }
    }

    /**
     * 删除指定Bean的合并Bean定义，
     * 在下次访问时重新创建。
     * @param beanName 要清除合并定义的Bean名称
     */
    protected void clearMergedBeanDefinition(String beanName) {
        RootBeanDefinition bd = this.mergedBeanDefinitions.get(beanName);
        if (bd != null) {
            bd.stale = true;
        }
    }

    /**
     * 清除合并的Bean定义缓存，移除那些尚未被认为适合完整元数据缓存的Bean条目。
     * <p>通常在原始Bean定义更改后触发，例如在应用一个{@code BeanFactoryPostProcessor}之后。请注意，此时已创建的Bean的元数据将保留。
     * @since 4.2
     */
    public void clearMetadataCache() {
        this.mergedBeanDefinitions.forEach((beanName, bd) -> {
            if (!isBeanEligibleForMetadataCaching(beanName)) {
                bd.stale = true;
            }
        });
    }

    /**
     * 解决指定bean定义的bean类，
     * 将bean类名解析为Class引用（如果需要）
     * 并将解析出的Class存储在bean定义中以便进一步使用。
     * @param mbd 要确定类的合并bean定义
     * @param beanName bean的名称（用于错误处理目的）
     * @param typesToMatch 内部类型匹配目的的匹配类型
     * （同时也表示返回的 {@code Class} 将永远不会暴露给应用程序代码）
     * @return 解析出的bean类（如果没有则返回 {@code null}）
     * @throws CannotLoadBeanClassException 如果我们未能加载类
     */
    @Nullable
    protected Class<?> resolveBeanClass(RootBeanDefinition mbd, String beanName, Class<?>... typesToMatch) throws CannotLoadBeanClassException {
        try {
            if (mbd.hasBeanClass()) {
                return mbd.getBeanClass();
            }
            Class<?> beanClass = doResolveBeanClass(mbd, typesToMatch);
            if (mbd.hasBeanClass()) {
                mbd.prepareMethodOverrides();
            }
            return beanClass;
        } catch (ClassNotFoundException ex) {
            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
        } catch (LinkageError err) {
            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), err);
        } catch (BeanDefinitionValidationException ex) {
            throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName, "Validation of method overrides failed", ex);
        }
    }

    @Nullable
    private Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch) throws ClassNotFoundException {
        ClassLoader beanClassLoader = getBeanClassLoader();
        ClassLoader dynamicLoader = beanClassLoader;
        boolean freshResolve = false;
        if (!ObjectUtils.isEmpty(typesToMatch)) {
            // 当仅进行类型检查（即尚未创建实际实例时），
            // 使用指定的临时类加载器（例如，在织入场景中）。
            ClassLoader tempClassLoader = getTempClassLoader();
            if (tempClassLoader != null) {
                dynamicLoader = tempClassLoader;
                freshResolve = true;
                if (tempClassLoader instanceof DecoratingClassLoader dcl) {
                    for (Class<?> typeToMatch : typesToMatch) {
                        dcl.excludeClass(typeToMatch.getName());
                    }
                }
            }
        }
        String className = mbd.getBeanClassName();
        if (className != null) {
            Object evaluated = evaluateBeanDefinitionString(className, mbd);
            if (!className.equals(evaluated)) {
                // 一个动态解析的表达式，自4.2版本开始支持...
                if (evaluated instanceof Class<?> clazz) {
                    return clazz;
                } else if (evaluated instanceof String name) {
                    className = name;
                    freshResolve = true;
                } else {
                    throw new IllegalStateException("Invalid class name expression result: " + evaluated);
                }
            }
            if (freshResolve) {
                // 当针对临时类加载器进行解析时，为了提高效率，应尽早退出。
                // 为了避免在Bean定义中存储解析后的Class。
                if (dynamicLoader != null) {
                    try {
                        return dynamicLoader.loadClass(className);
                    } catch (ClassNotFoundException ex) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Could not load class [" + className + "] from " + dynamicLoader + ": " + ex);
                        }
                    }
                }
                return ClassUtils.forName(className, dynamicLoader);
            }
        }
        // 定期解决，并将结果缓存在 BeanDefinition 中...
        return mbd.resolveBeanClass(beanClassLoader);
    }

    /**
     * 评估包含在Bean定义中的给定String，
     * 可能将其解析为表达式。
     * @param value 要检查的值
     * @param beanDefinition 值来源的Bean定义
     * @return 解析后的值
     * @see #setBeanExpressionResolver
     */
    @Nullable
    protected Object evaluateBeanDefinitionString(@Nullable String value, @Nullable BeanDefinition beanDefinition) {
        if (this.beanExpressionResolver == null) {
            return value;
        }
        Scope scope = null;
        if (beanDefinition != null) {
            String scopeName = beanDefinition.getScope();
            if (scopeName != null) {
                scope = getRegisteredScope(scopeName);
            }
        }
        return this.beanExpressionResolver.evaluate(value, new BeanExpressionContext(this, scope));
    }

    /**
     * 预测指定 Bean 的最终 Bean 类型（处理后的 Bean 实例的类型）。由 {@link #getType} 和 {@link #isTypeMatch} 调用。无需专门处理 FactoryBeans，因为它只应该操作原始 Bean 类型。
     * <p>此实现很简单，因为它无法处理工厂方法和 InstantiationAwareBeanPostProcessors。它只能正确预测标准 Bean 的 Bean 类型。在子类中应重写此方法，以应用更复杂类型检测。
     * @param beanName Bean 的名称
     * @param mbd 要确定类型的合并 Bean 定义
     * @param typesToMatch 内部类型匹配目的的类型，如果返回的 {@code Class} 永远不会被暴露给应用程序代码
     * @return Bean 的类型，如果不可预测则返回 {@code null}
     */
    @Nullable
    protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> targetType = mbd.getTargetType();
        if (targetType != null) {
            return targetType;
        }
        if (mbd.getFactoryMethodName() != null) {
            return null;
        }
        return resolveBeanClass(mbd, beanName, typesToMatch);
    }

    /**
     * 检查给定的bean是否定义为{@link FactoryBean}。
     * @param beanName bean的名称
     * @param mbd 相应的bean定义
     */
    protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {
        Boolean result = mbd.isFactoryBean;
        if (result == null) {
            Class<?> beanType = predictBeanType(beanName, mbd, FactoryBean.class);
            result = (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
            mbd.isFactoryBean = result;
        }
        return result;
    }

    /**
     * 确定给定FactoryBean定义的Bean类型，尽可能做到。
     * 只有在目标Bean已经注册了单例实例的情况下才调用此方法。实现方式允许在允许初始化（即`allowInit`为`true`）且类型无法以其他方式确定的情况下实例化目标FactoryBean；否则，它将仅限于检查签名和相关元数据。
     * <p>如果bean定义上没有设置`@link FactoryBean#OBJECT_TYPE_ATTRIBUTE`，且`allowInit`为`true`，则默认实现将通过`getBean`创建FactoryBean来调用其`getObjectType`方法。鼓励子类优化此操作，通常是通过检查工厂Bean类的泛型签名或创建它的工厂方法。
     * 如果子类实例化了FactoryBean，它们应该考虑在不完全填充Bean的情况下尝试`getObjectType`方法。如果失败，则应使用此实现执行的完整FactoryBean创建作为后备方案。
     * @param beanName Bean的名称
     * @param mbd Bean的合并定义
     * @param allowInit 如果无法以其他方式确定类型，则允许初始化FactoryBean
     * @return 如果可确定，则返回Bean的类型，否则返回`ResolvableType.NONE`
     * @since 5.2
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     * @see #getBean(String)
     */
    protected ResolvableType getTypeForFactoryBean(String beanName, RootBeanDefinition mbd, boolean allowInit) {
        ResolvableType result = getTypeForFactoryBeanFromAttributes(mbd);
        if (result != ResolvableType.NONE) {
            return result;
        }
        if (allowInit && mbd.isSingleton()) {
            try {
                FactoryBean<?> factoryBean = doGetBean(FACTORY_BEAN_PREFIX + beanName, FactoryBean.class, null, true);
                Class<?> objectType = getTypeForFactoryBean(factoryBean);
                return (objectType != null ? ResolvableType.forClass(objectType) : ResolvableType.NONE);
            } catch (BeanCreationException ex) {
                if (ex.contains(BeanCurrentlyInCreationException.class)) {
                    logger.trace(LogMessage.format("Bean currently in creation on FactoryBean type check: %s", ex));
                } else if (mbd.isLazyInit()) {
                    logger.trace(LogMessage.format("Bean creation exception on lazy FactoryBean type check: %s", ex));
                } else {
                    logger.debug(LogMessage.format("Bean creation exception on eager FactoryBean type check: %s", ex));
                }
                onSuppressedException(ex);
            }
        }
        // 工厂Bean类型无法解析
        return ResolvableType.NONE;
    }

    /**
     * 将指定的bean标记为已创建（或即将创建）。
     * <p>这允许bean工厂优化其缓存，以便重复创建指定的bean。
     * @param beanName bean的名称
     */
    protected void markBeanAsCreated(String beanName) {
        if (!this.alreadyCreated.contains(beanName)) {
            synchronized (this.mergedBeanDefinitions) {
                if (!isBeanEligibleForMetadataCaching(beanName)) {
                    // 现在我们实际上正在创建，允许bean定义重新合并
                    // 这个 Bean...以防万一其间它的元数据发生了变化。
                    clearMergedBeanDefinition(beanName);
                }
                this.alreadyCreated.add(beanName);
            }
        }
    }

    /**
     * 在创建 Bean 失败后执行适当的缓存元数据的清理操作。
     * @param beanName Bean 的名称
     */
    protected void cleanupAfterBeanCreationFailure(String beanName) {
        synchronized (this.mergedBeanDefinitions) {
            this.alreadyCreated.remove(beanName);
        }
    }

    /**
     * 判断指定的bean是否有资格缓存其bean定义元数据。
     * @param beanName bean的名称
     * @return 如果bean的元数据在此点可能已被缓存，则返回{@code true}
     */
    protected boolean isBeanEligibleForMetadataCaching(String beanName) {
        return this.alreadyCreated.contains(beanName);
    }

    /**
     * 删除给定bean名称的单例实例（如果有的话），
     * 但仅当它未被用于除类型检查以外的其他目的时。
     * @param beanName bean的名称
     * @return 如果实际删除，则返回 {@code true}，否则返回 {@code false}
     */
    protected boolean removeSingletonIfCreatedForTypeCheckOnly(String beanName) {
        if (!this.alreadyCreated.contains(beanName)) {
            removeSingleton(beanName);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查此工厂的bean创建阶段是否已经开始，
     * 即在此期间是否有任何bean被标记为已创建。
     * @since 4.2.2
     * @see #markBeanAsCreated
     */
    protected boolean hasBeanCreationStarted() {
        return !this.alreadyCreated.isEmpty();
    }

    /**
     * 获取给定bean实例的对象，无论是bean实例本身还是FactoryBean创建的对象。
     * @param beanInstance 共享的bean实例
     * @param name 可能包含工厂解除引用前缀的名称
     * @param beanName 标准化的bean名称
     * @param mbd 合并后的bean定义
     * @return 为bean公开的对象
     */
    protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {
        // 不允许调用代码尝试对非工厂的bean进行解引用。
        if (BeanFactoryUtils.isFactoryDereference(name)) {
            if (beanInstance instanceof NullBean) {
                return beanInstance;
            }
            if (!(beanInstance instanceof FactoryBean)) {
                throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
            }
            if (mbd != null) {
                mbd.isFactoryBean = true;
            }
            return beanInstance;
        }
        // 现在我们有了bean实例，它可能是一个普通bean或者一个FactoryBean。
        // 如果它是一个FactoryBean，我们使用它来创建一个bean实例，除非它的类型不是我们期望的类型。
        // 调用者实际上想要工厂的引用。
        if (!(beanInstance instanceof FactoryBean<?> factoryBean)) {
            return beanInstance;
        }
        Object object = null;
        if (mbd != null) {
            mbd.isFactoryBean = true;
        } else {
            object = getCachedObjectForFactoryBean(beanName);
        }
        if (object == null) {
            // 从工厂返回 bean 实例。
            // 如果从 FactoryBean 获取的对象是单例，则将其缓存。
            if (mbd == null && containsBeanDefinition(beanName)) {
                mbd = getMergedLocalBeanDefinition(beanName);
            }
            boolean synthetic = (mbd != null && mbd.isSynthetic());
            object = getObjectFromFactoryBean(factoryBean, beanName, !synthetic);
        }
        return object;
    }

    /**
     * 判断给定的 Bean 名称是否已在当前工厂中使用，
     * 即是否存在以该名称注册的本地 Bean 或别名，或者是否已使用该名称创建了一个内部 Bean。
     * @param beanName 要检查的名称
     */
    public boolean isBeanNameInUse(String beanName) {
        return isAlias(beanName) || containsLocalBean(beanName) || hasDependentBean(beanName);
    }

    /**
     * 确定给定的bean在关闭时是否需要销毁。
     * <p>默认实现会检查DisposableBean接口、指定的销毁方法以及注册的DestructionAwareBeanPostProcessors。
     * @param bean 要检查的bean实例
     * @param mbd 相应的bean定义
     * @see org.springframework.beans.factory.DisposableBean
     * @see AbstractBeanDefinition#getDestroyMethodName()
     * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
     */
    protected boolean requiresDestruction(Object bean, RootBeanDefinition mbd) {
        return (bean.getClass() != NullBean.class && (DisposableBeanAdapter.hasDestroyMethod(bean, mbd) || (hasDestructionAwareBeanPostProcessors() && DisposableBeanAdapter.hasApplicableProcessors(bean, getBeanPostProcessorCache().destructionAware))));
    }

    /**
     * 将给定的bean添加到本工厂的可丢弃bean列表中，
     * 注册其DisposableBean接口和/或给定的销毁方法
     * 在工厂关闭时调用（如果适用）。仅适用于单例。
     * @param beanName bean的名称
     * @param bean bean实例
     * @param mbd bean的定义
     * @see RootBeanDefinition#isSingleton
     * @see RootBeanDefinition#getDependsOn
     * @see #registerDisposableBean
     * @see #registerDependentBean
     */
    protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
        if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
            if (mbd.isSingleton()) {
                // 注册一个实现DisposableBean的实例，该实例执行所有销毁操作
                // 针对给定bean进行工作：DestructionAwareBeanPostProcessors，
                // `DisposableBean` 接口，自定义销毁方法。
                registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessorCache().destructionAware));
            } else {
                // 具有自定义范围的 Bean...
                Scope scope = this.scopes.get(mbd.getScope());
                if (scope == null) {
                    throw new IllegalStateException("No Scope registered for scope name '" + mbd.getScope() + "'");
                }
                scope.registerDestructionCallback(beanName, new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessorCache().destructionAware));
            }
        }
    }

    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您进行准确的翻译。
    // 抽象方法由子类实现
    // 由于您提供的代码注释内容是空的，即只有一个空行和一个横线（"---------------------------------------------------------------------"），因此没有实际的注释内容可供翻译。如果您能提供具体的Java代码注释内容，我将很乐意为您进行翻译。
    /**
     * 检查此bean工厂是否包含具有给定名称的bean定义。
     * 不考虑此工厂可能参与的任何层次结构。
     * 当未找到缓存的单例实例时，由{@code containsBean}调用。
     * <p>根据具体bean工厂实现的方式，
     * 此操作可能代价高昂（例如，因为在外部注册表中进行目录查找）。
     * 然而，对于可列表的bean工厂，这通常
     * 只相当于本地哈希查找：因此，该操作是公共接口的一部分。
     * 在这种情况下，相同的实现可以用于这个模板方法和公共接口方法。
     * @param beanName 要查找的bean的名称
     * @return 如果此bean工厂包含具有给定名称的bean定义
     * @see #containsBean
     * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition
     */
    protected abstract boolean containsBeanDefinition(String beanName);

    /**
     * 返回给定bean名称的bean定义。
     * 子类通常应该实现缓存，因为这个方法会在需要bean定义元数据时被此类调用。
     * <p>根据具体bean工厂实现的方式，这个操作可能很昂贵（例如，因为在外部注册表中执行目录查找）。然而，对于可列表的bean工厂，这通常只是本地哈希查找：因此，这个操作是公共接口的一部分。在这种情况下，相同的实现可以服务于这个模板方法和公共接口方法。
     * @param beanName 要查找定义的bean的名称
     * @return 此原型名称的BeanDefinition（从不为{@code null}）
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     * 如果无法解析bean定义
     * @throws BeansException 如果出错
     * @see RootBeanDefinition
     * @see ChildBeanDefinition
     * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#getBeanDefinition
     */
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    /**
     * 为给定的合并后的bean定义（和参数）创建一个bean实例。
     * 如果是子定义，bean定义已经与父定义合并。
     * <p>所有bean检索方法都委托给此方法以进行实际的bean创建。
     * @param beanName bean的名称
     * @param mbd bean的合并后的定义
     * @param args 用于构造函数或工厂方法调用的显式参数
     * @return bean的新实例
     * @throws BeanCreationException 如果bean无法创建
     */
    protected abstract Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) throws BeanCreationException;

    /**
     * CopyOnWriteArrayList，在修改时重置beanPostProcessorCache字段。
     *
     * @since 5.3
     */
    @SuppressWarnings("serial")
    private class BeanPostProcessorCacheAwareList extends CopyOnWriteArrayList<BeanPostProcessor> {

        @Override
        public BeanPostProcessor set(int index, BeanPostProcessor element) {
            BeanPostProcessor result = super.set(index, element);
            resetBeanPostProcessorCache();
            return result;
        }

        @Override
        public boolean add(BeanPostProcessor o) {
            boolean success = super.add(o);
            resetBeanPostProcessorCache();
            return success;
        }

        @Override
        public void add(int index, BeanPostProcessor element) {
            super.add(index, element);
            resetBeanPostProcessorCache();
        }

        @Override
        public BeanPostProcessor remove(int index) {
            BeanPostProcessor result = super.remove(index);
            resetBeanPostProcessorCache();
            return result;
        }

        @Override
        public boolean remove(Object o) {
            boolean success = super.remove(o);
            if (success) {
                resetBeanPostProcessorCache();
            }
            return success;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean success = super.removeAll(c);
            if (success) {
                resetBeanPostProcessorCache();
            }
            return success;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean success = super.retainAll(c);
            if (success) {
                resetBeanPostProcessorCache();
            }
            return success;
        }

        @Override
        public boolean addAll(Collection<? extends BeanPostProcessor> c) {
            boolean success = super.addAll(c);
            if (success) {
                resetBeanPostProcessorCache();
            }
            return success;
        }

        @Override
        public boolean addAll(int index, Collection<? extends BeanPostProcessor> c) {
            boolean success = super.addAll(index, c);
            if (success) {
                resetBeanPostProcessorCache();
            }
            return success;
        }

        @Override
        public boolean removeIf(Predicate<? super BeanPostProcessor> filter) {
            boolean success = super.removeIf(filter);
            if (success) {
                resetBeanPostProcessorCache();
            }
            return success;
        }

        @Override
        public void replaceAll(UnaryOperator<BeanPostProcessor> operator) {
            super.replaceAll(operator);
            resetBeanPostProcessorCache();
        }
    }

    /**
     * 内部缓存预先过滤后的后处理器。
     *
     * @since 5.3
     */
    static class BeanPostProcessorCache {

        final List<InstantiationAwareBeanPostProcessor> instantiationAware = new ArrayList<>();

        final List<SmartInstantiationAwareBeanPostProcessor> smartInstantiationAware = new ArrayList<>();

        final List<DestructionAwareBeanPostProcessor> destructionAware = new ArrayList<>();

        final List<MergedBeanDefinitionPostProcessor> mergedDefinition = new ArrayList<>();
    }
}
