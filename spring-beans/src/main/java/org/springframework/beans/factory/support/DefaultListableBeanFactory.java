// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据Apache许可证2.0版（以下简称“许可证”）；除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则根据许可证分发的软件按“原样”分发，
* 不提供任何形式（明示或暗示）的保证或条件。有关权限和限制的特定语言，请参阅许可证。*/
package org.springframework.beans.factory.support;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import jakarta.inject.Provider;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.core.OrderComparator;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.log.LogMessage;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.CompositeIterator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Spring 对 {@link ConfigurableListableBeanFactory} 和 {@link BeanDefinitionRegistry} 接口的默认实现：一个基于 bean 定义元数据的完整 bean 工厂，可通过后处理器进行扩展。
 *
 * <p>典型用法是首先注册所有 bean 定义（可能从 bean 定义文件中读取），然后再访问 bean。因此，通过名称查找 bean 在本地 bean 定义表中是一个低成本的操作，它操作的是预先解析的 bean 定义元数据对象。
 *
 * <p>请注意，特定 bean 定义格式的读取器通常是单独实现的，而不是作为 bean 工厂子类：例如，请参阅 {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}。
 *
 * <p>对于 {@link org.springframework.beans.factory.ListableBeanFactory} 接口的另一种实现，请查看 {@link StaticListableBeanFactory}，它管理现有的 bean 实例，而不是根据 bean 定义创建新的实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Costin Leau
 * @author Chris Beams
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 16 April 2001
 * @see #registerBeanDefinition
 * @see #addBeanPostProcessor
 * @see #getBean
 * @see #resolveDependency
 */
@SuppressWarnings("serial")
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

    @Nullable
    private static Class<?> jakartaInjectProviderClass;

    static {
        try {
            jakartaInjectProviderClass = ClassUtils.forName("jakarta.inject.Provider", DefaultListableBeanFactory.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // JSR-330 API不可用 - 因此不支持提供者接口。
            jakartaInjectProviderClass = null;
        }
    }

    /**
     * 将序列化ID映射到工厂实例的映射。
     */
    private static final Map<String, Reference<DefaultListableBeanFactory>> serializableFactories = new ConcurrentHashMap<>(8);

    /**
     * 此工厂的Optional ID，用于序列化目的。
     */
    @Nullable
    private String serializationId;

    /**
     * 是否允许以相同名称重新注册不同的定义。
     */
    private boolean allowBeanDefinitionOverriding = true;

    /**
     * 是否允许对懒加载（lazy-init）的Bean进行急切（eager）类加载。
     */
    private boolean allowEagerClassLoading = true;

    /**
     * 为依赖列表和数组提供可选的OrderComparator。
     */
    @Nullable
    private Comparator<Object> dependencyComparator;

    /**
     * 用于检查一个Bean定义是否为自动装配候选者的解析器。
     */
    private AutowireCandidateResolver autowireCandidateResolver = SimpleAutowireCandidateResolver.INSTANCE;

    /**
     * 将依赖类型映射到相应的自动装配值。
     */
    private final Map<Class<?>, Object> resolvableDependencies = new ConcurrentHashMap<>(16);

    /**
     * 根据bean名称键控的bean定义对象映射。
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    /**
     * 从Bean名称到合并后的BeanDefinitionHolder的映射。
     */
    private final Map<String, BeanDefinitionHolder> mergedBeanDefinitionHolders = new ConcurrentHashMap<>(256);

    /**
     * 根据依赖类型键控的单例和非单例bean名称的映射。
     */
    private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>(64);

    /**
     * 仅包含单例bean名称的映射，以依赖类型为键。
     */
    private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<>(64);

    /**
     * Bean定义名称列表，按照注册顺序排列。
     */
    private volatile List<String> beanDefinitionNames = new ArrayList<>(256);

    /**
     * 手动注册单例的名称列表，按注册顺序排列。
     */
    private volatile Set<String> manualSingletonNames = new LinkedHashSet<>(16);

    /**
     * 在配置冻结的情况下，缓存了Bean定义名称的数组。
     */
    @Nullable
    private volatile String[] frozenBeanDefinitionNames;

    /**
     * 是否可以缓存所有 Bean 的定义元数据。
     */
    private volatile boolean configurationFrozen;

    /**
     * 创建一个新的 DefaultListableBeanFactory。
     */
    public DefaultListableBeanFactory() {
        super();
    }

    /**
     * 使用给定的父级创建一个新的 DefaultListableBeanFactory。
     * @param parentBeanFactory 父级 BeanFactory
     */
    public DefaultListableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
        super(parentBeanFactory);
    }

    /**
     * 指定一个ID用于序列化目的，允许在此需要的条件下，通过此ID将BeanFactory对象反序列化回BeanFactory对象。
     */
    public void setSerializationId(@Nullable String serializationId) {
        if (serializationId != null) {
            serializableFactories.put(serializationId, new WeakReference<>(this));
        } else if (this.serializationId != null) {
            serializableFactories.remove(this.serializationId);
        }
        this.serializationId = serializationId;
    }

    /**
     * 返回一个用于序列化的ID，如果指定，则允许此BeanFactory通过该ID反序列化为BeanFactory对象，如果需要的话。
     * @since 4.1.2
     */
    @Nullable
    public String getSerializationId() {
        return this.serializationId;
    }

    /**
     * 设置是否允许通过注册具有相同名称的不同定义来覆盖bean定义，自动替换之前的定义。
     * 如果不允许，将抛出异常。这也适用于覆盖别名。
     * <p>默认值为 "true"。
     * @see #registerBeanDefinition
     */
    public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
        this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
    }

    /**
     * 返回是否允许通过注册具有相同名称的不同定义来覆盖bean定义，自动替换前者。
     * @since 4.1.2
     */
    public boolean isAllowBeanDefinitionOverriding() {
        return this.allowBeanDefinitionOverriding;
    }

    /**
     * 设置工厂是否允许在标记为 "lazy-init" 的bean定义上贪婪地加载bean类
     * <p>默认值为 "true"。关闭此标志将抑制对 lazy-init bean 的类加载，
     * 除非此类bean被明确请求。特别是，在类型查找时，将简单地忽略没有解析的类名的bean定义，
     * 而不是为了执行类型检查而按需加载bean类。
     * @see AbstractBeanDefinition#setLazyInit
     */
    public void setAllowEagerClassLoading(boolean allowEagerClassLoading) {
        this.allowEagerClassLoading = allowEagerClassLoading;
    }

    /**
     * 返回工厂是否允许对于标记为“lazy-init”的bean定义，也进行bean类的主动加载
     * @since 4.1.2
     */
    public boolean isAllowEagerClassLoading() {
        return this.allowEagerClassLoading;
    }

    /**
     * 为依赖列表和数组设置一个 {@link java.util.Comparator}。
     * @since 4.0
     * @see org.springframework.core.OrderComparator
     * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
     */
    public void setDependencyComparator(@Nullable Comparator<Object> dependencyComparator) {
        this.dependencyComparator = dependencyComparator;
    }

    /**
     * 返回此BeanFactory的依赖比较器（可能为null）。
     * @since 4.0
     */
    @Nullable
    public Comparator<Object> getDependencyComparator() {
        return this.dependencyComparator;
    }

    /**
     * 为此BeanFactory设置一个自定义的自动装配候选解析器，在决定一个Bean定义是否应被视为自动装配的候选时使用。
     */
    public void setAutowireCandidateResolver(AutowireCandidateResolver autowireCandidateResolver) {
        Assert.notNull(autowireCandidateResolver, "AutowireCandidateResolver must not be null");
        if (autowireCandidateResolver instanceof BeanFactoryAware beanFactoryAware) {
            beanFactoryAware.setBeanFactory(this);
        }
        this.autowireCandidateResolver = autowireCandidateResolver;
    }

    /**
     * 返回此BeanFactory的自动装配候选解析器（从不为null）。
     */
    public AutowireCandidateResolver getAutowireCandidateResolver() {
        return this.autowireCandidateResolver;
    }

    @Override
    public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
        super.copyConfigurationFrom(otherFactory);
        if (otherFactory instanceof DefaultListableBeanFactory otherListableFactory) {
            this.allowBeanDefinitionOverriding = otherListableFactory.allowBeanDefinitionOverriding;
            this.allowEagerClassLoading = otherListableFactory.allowEagerClassLoading;
            this.dependencyComparator = otherListableFactory.dependencyComparator;
            // 这是一个 AutowireCandidateResolver 的克隆，因为它可能实现了 BeanFactoryAware 接口。
            setAutowireCandidateResolver(otherListableFactory.getAutowireCandidateResolver().cloneIfNecessary());
            // 在此处也使可解析的依赖项（例如 ResourceLoader）可用
            this.resolvableDependencies.putAll(otherListableFactory.resolvableDependencies);
        }
    }

    // ---------------------------------------------------------------------这个代码注释内容是一个水平的横线，通常用于分隔代码块或用于视觉上的区分，并没有实际的代码注释内容。因此，不需要翻译，它本身就是表示分隔的符号。如果需要用中文描述其作用，可以说：--------------------------------------------------------------------------------或者------------------------这两者都是用于分隔代码块的中文表示。
    // 剩余BeanFactory方法的实现
    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释部分，我将根据您提供的内容进行翻译。
    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return getBean(requiredType, (Object[]) null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(Class<T> requiredType, @Nullable Object... args) throws BeansException {
        Assert.notNull(requiredType, "Required type must not be null");
        Object resolved = resolveBean(ResolvableType.forRawClass(requiredType), args, false);
        if (resolved == null) {
            throw new NoSuchBeanDefinitionException(requiredType);
        }
        return (T) resolved;
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
        Assert.notNull(requiredType, "Required type must not be null");
        return getBeanProvider(ResolvableType.forRawClass(requiredType), true);
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
        return getBeanProvider(requiredType, true);
    }

    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将尽力为您翻译成中文。
    // 实现 ListableBeanFactory 接口
    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释内容，我将为您进行翻译。
    @Override
    public boolean containsBeanDefinition(String beanName) {
        Assert.notNull(beanName, "Bean name must not be null");
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        String[] frozenNames = this.frozenBeanDefinitionNames;
        if (frozenNames != null) {
            return frozenNames.clone();
        } else {
            return StringUtils.toStringArray(this.beanDefinitionNames);
        }
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
        Assert.notNull(requiredType, "Required type must not be null");
        return getBeanProvider(ResolvableType.forRawClass(requiredType), allowEagerInit);
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {
        return new BeanObjectProvider<>() {

            @Override
            public T getObject() throws BeansException {
                T resolved = resolveBean(requiredType, null, false);
                if (resolved == null) {
                    throw new NoSuchBeanDefinitionException(requiredType);
                }
                return resolved;
            }

            @Override
            public T getObject(Object... args) throws BeansException {
                T resolved = resolveBean(requiredType, args, false);
                if (resolved == null) {
                    throw new NoSuchBeanDefinitionException(requiredType);
                }
                return resolved;
            }

            @Override
            @Nullable
            public T getIfAvailable() throws BeansException {
                try {
                    return resolveBean(requiredType, null, false);
                } catch (ScopeNotActiveException ex) {
                    // 忽略非活动作用域中的已解析Bean
                    return null;
                }
            }

            @Override
            public void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
                T dependency = getIfAvailable();
                if (dependency != null) {
                    try {
                        dependencyConsumer.accept(dependency);
                    } catch (ScopeNotActiveException ex) {
                        // 忽略非活动作用域中已解析的bean，即使在作用域代理调用时也是如此
                    }
                }
            }

            @Override
            @Nullable
            public T getIfUnique() throws BeansException {
                try {
                    return resolveBean(requiredType, null, true);
                } catch (ScopeNotActiveException ex) {
                    // 忽略非活动作用域中的已解析Bean
                    return null;
                }
            }

            @Override
            public void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
                T dependency = getIfUnique();
                if (dependency != null) {
                    try {
                        dependencyConsumer.accept(dependency);
                    } catch (ScopeNotActiveException ex) {
                        // 忽略非活动作用域中已解析的Bean，即使在作用域代理调用时也是如此。
                    }
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public Stream<T> stream() {
                return Arrays.stream(getBeanNamesForTypedStream(requiredType, allowEagerInit)).map(name -> (T) getBean(name)).filter(bean -> !(bean instanceof NullBean));
            }

            @SuppressWarnings("unchecked")
            @Override
            public Stream<T> orderedStream() {
                String[] beanNames = getBeanNamesForTypedStream(requiredType, allowEagerInit);
                if (beanNames.length == 0) {
                    return Stream.empty();
                }
                Map<String, T> matchingBeans = CollectionUtils.newLinkedHashMap(beanNames.length);
                for (String beanName : beanNames) {
                    Object beanInstance = getBean(beanName);
                    if (!(beanInstance instanceof NullBean)) {
                        matchingBeans.put(beanName, (T) beanInstance);
                    }
                }
                Stream<T> stream = matchingBeans.values().stream();
                return stream.sorted(adaptOrderComparator(matchingBeans));
            }
        };
    }

    @Nullable
    private <T> T resolveBean(ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) {
        NamedBeanHolder<T> namedBean = resolveNamedBean(requiredType, args, nonUniqueAsNull);
        if (namedBean != null) {
            return namedBean.getBeanInstance();
        }
        BeanFactory parent = getParentBeanFactory();
        if (parent instanceof DefaultListableBeanFactory dlfb) {
            return dlfb.resolveBean(requiredType, args, nonUniqueAsNull);
        } else if (parent != null) {
            ObjectProvider<T> parentProvider = parent.getBeanProvider(requiredType);
            if (args != null) {
                return parentProvider.getObject(args);
            } else {
                return (nonUniqueAsNull ? parentProvider.getIfUnique() : parentProvider.getIfAvailable());
            }
        }
        return null;
    }

    private String[] getBeanNamesForTypedStream(ResolvableType requiredType, boolean allowEagerInit) {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType, true, allowEagerInit);
    }

    @Override
    public String[] getBeanNamesForType(ResolvableType type) {
        return getBeanNamesForType(type, true, true);
    }

    @Override
    public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
        Class<?> resolved = type.resolve();
        if (resolved != null && !type.hasGenerics()) {
            return getBeanNamesForType(resolved, includeNonSingletons, allowEagerInit);
        } else {
            return doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
        }
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type) {
        return getBeanNamesForType(type, true, true);
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        if (!isConfigurationFrozen() || type == null || !allowEagerInit) {
            return doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, allowEagerInit);
        }
        Map<Class<?>, String[]> cache = (includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType);
        String[] resolvedBeanNames = cache.get(type);
        if (resolvedBeanNames != null) {
            return resolvedBeanNames;
        }
        resolvedBeanNames = doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, true);
        if (ClassUtils.isCacheSafe(type, getBeanClassLoader())) {
            cache.put(type, resolvedBeanNames);
        }
        return resolvedBeanNames;
    }

    private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
        List<String> result = new ArrayList<>();
        // 检查所有 Bean 定义。
        for (String beanName : this.beanDefinitionNames) {
            // 仅当bean名称未被定义为其他bean的别名时，才考虑该bean为有效bean。
            if (!isAlias(beanName)) {
                try {
                    RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                    // 仅当 Bean 定义完整时才检查。
                    if (!mbd.isAbstract() && (allowEagerInit || (mbd.hasBeanClass() || !mbd.isLazyInit() || isAllowEagerClassLoading()) && !requiresEagerInitForType(mbd.getFactoryBeanName()))) {
                        boolean isFactoryBean = isFactoryBean(beanName, mbd);
                        BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
                        boolean matchFound = false;
                        boolean allowFactoryBeanInit = (allowEagerInit || containsSingleton(beanName));
                        boolean isNonLazyDecorated = (dbd != null && !mbd.isLazyInit());
                        if (!isFactoryBean) {
                            if (includeNonSingletons || isSingleton(beanName, mbd, dbd)) {
                                matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
                            }
                        } else {
                            if (includeNonSingletons || isNonLazyDecorated || (allowFactoryBeanInit && isSingleton(beanName, mbd, dbd))) {
                                matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
                            }
                            if (!matchFound) {
                                // 在FactoryBean的情况下，接下来尝试匹配FactoryBean实例本身。
                                beanName = FACTORY_BEAN_PREFIX + beanName;
                                if (includeNonSingletons || isSingleton(beanName, mbd, dbd)) {
                                    matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
                                }
                            }
                        }
                        if (matchFound) {
                            result.add(beanName);
                        }
                    }
                } catch (CannotLoadBeanClassException | BeanDefinitionStoreException ex) {
                    if (allowEagerInit) {
                        throw ex;
                    }
                    // 可能是一个占位符：为了类型匹配的目的，我们暂时忽略它。
                    LogMessage message = (ex instanceof CannotLoadBeanClassException ? LogMessage.format("Ignoring bean class loading failure for bean '%s'", beanName) : LogMessage.format("Ignoring unresolvable metadata in bean definition '%s'", beanName));
                    logger.trace(message, ex);
                    // 注册异常，以防意外无法解析该Bean。
                    onSuppressedException(ex);
                } catch (NoSuchBeanDefinitionException ex) {
                    // 在迭代过程中移除了 Bean 定义 -> 忽略。
                }
            }
        }
        // 检查手动注册的单例实例。
        for (String beanName : this.manualSingletonNames) {
            try {
                // 如果涉及 FactoryBean，则匹配由 FactoryBean 创建的对象。
                if (isFactoryBean(beanName)) {
                    if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
                        result.add(beanName);
                        // 找到了该Bean的匹配项：不再匹配FactoryBean本身。
                        continue;
                    }
                    // 在FactoryBean的情况下，尝试接下来匹配FactoryBean本身。
                    beanName = FACTORY_BEAN_PREFIX + beanName;
                }
                // 匹配原始的 Bean 实例（可能为原始 FactoryBean）。
                if (isTypeMatch(beanName, type)) {
                    result.add(beanName);
                }
            } catch (NoSuchBeanDefinitionException ex) {
                // 不应该发生 - 很可能是循环引用解析的结果...
                logger.trace(LogMessage.format("Failed to check manually registered singleton with name '%s'", beanName), ex);
            }
        }
        return StringUtils.toStringArray(result);
    }

    private boolean isSingleton(String beanName, RootBeanDefinition mbd, @Nullable BeanDefinitionHolder dbd) {
        return (dbd != null ? mbd.isSingleton() : isSingleton(beanName));
    }

    /**
     * 检查指定的Bean是否需要预先初始化
     * 以确定其类型。
     * @param factoryBeanName 一个工厂-bean引用，该Bean定义了一个工厂方法
     * @return 是否需要预先初始化
     */
    private boolean requiresEagerInitForType(@Nullable String factoryBeanName) {
        return (factoryBeanName != null && isFactoryBean(factoryBeanName) && !containsSingleton(factoryBeanName));
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
        return getBeansOfType(type, true, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
        String[] beanNames = getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
        Map<String, T> result = CollectionUtils.newLinkedHashMap(beanNames.length);
        for (String beanName : beanNames) {
            try {
                Object beanInstance = getBean(beanName);
                if (!(beanInstance instanceof NullBean)) {
                    result.put(beanName, (T) beanInstance);
                }
            } catch (BeanCreationException ex) {
                Throwable rootCause = ex.getMostSpecificCause();
                if (rootCause instanceof BeanCurrentlyInCreationException bce) {
                    String exBeanName = bce.getBeanName();
                    if (exBeanName != null && isCurrentlyInCreation(exBeanName)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Ignoring match to currently created bean '" + exBeanName + "': " + ex.getMessage());
                        }
                        onSuppressedException(ex);
                        // 忽略：表示在自动装配构造器时指示循环引用。
                        // 我们想要找到除了当前创建的bean本身之外的匹配项。
                        continue;
                    }
                }
                throw ex;
            }
        }
        return result;
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        List<String> result = new ArrayList<>();
        for (String beanName : this.beanDefinitionNames) {
            BeanDefinition bd = this.beanDefinitionMap.get(beanName);
            if (bd != null && !bd.isAbstract() && findAnnotationOnBean(beanName, annotationType) != null) {
                result.add(beanName);
            }
        }
        for (String beanName : this.manualSingletonNames) {
            if (!result.contains(beanName) && findAnnotationOnBean(beanName, annotationType) != null) {
                result.add(beanName);
            }
        }
        return StringUtils.toStringArray(result);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        String[] beanNames = getBeanNamesForAnnotation(annotationType);
        Map<String, Object> result = CollectionUtils.newLinkedHashMap(beanNames.length);
        for (String beanName : beanNames) {
            Object beanInstance = getBean(beanName);
            if (!(beanInstance instanceof NullBean)) {
                result.put(beanName, beanInstance);
            }
        }
        return result;
    }

    @Override
    @Nullable
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
        return findAnnotationOnBean(beanName, annotationType, true);
    }

    @Override
    @Nullable
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        Class<?> beanType = getType(beanName, allowFactoryBeanInit);
        if (beanType != null) {
            MergedAnnotation<A> annotation = MergedAnnotations.from(beanType, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
            if (annotation.isPresent()) {
                return annotation.synthesize();
            }
        }
        if (containsBeanDefinition(beanName)) {
            RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
            // 检查原始的 Bean 类，例如在代理的情况下。
            if (bd.hasBeanClass() && bd.getFactoryMethodName() == null) {
                Class<?> beanClass = bd.getBeanClass();
                if (beanClass != beanType) {
                    MergedAnnotation<A> annotation = MergedAnnotations.from(beanClass, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
                    if (annotation.isPresent()) {
                        return annotation.synthesize();
                    }
                }
            }
            // 检查在工厂方法上声明的注解，如果有的话。
            Method factoryMethod = bd.getResolvedFactoryMethod();
            if (factoryMethod != null) {
                MergedAnnotation<A> annotation = MergedAnnotations.from(factoryMethod, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
                if (annotation.isPresent()) {
                    return annotation.synthesize();
                }
            }
        }
        return null;
    }

    @Override
    public <A extends Annotation> Set<A> findAllAnnotationsOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        Set<A> annotations = new LinkedHashSet<>();
        Class<?> beanType = getType(beanName, allowFactoryBeanInit);
        if (beanType != null) {
            MergedAnnotations.from(beanType, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).stream(annotationType).filter(MergedAnnotation::isPresent).forEach(mergedAnnotation -> annotations.add(mergedAnnotation.synthesize()));
        }
        if (containsBeanDefinition(beanName)) {
            RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
            // 检查原始的 Bean 类，例如在代理的情况下。
            if (bd.hasBeanClass() && bd.getFactoryMethodName() == null) {
                Class<?> beanClass = bd.getBeanClass();
                if (beanClass != beanType) {
                    MergedAnnotations.from(beanClass, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).stream(annotationType).filter(MergedAnnotation::isPresent).forEach(mergedAnnotation -> annotations.add(mergedAnnotation.synthesize()));
                }
            }
            // 检查工厂方法上声明的注解，如果有的话。
            Method factoryMethod = bd.getResolvedFactoryMethod();
            if (factoryMethod != null) {
                MergedAnnotations.from(factoryMethod, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).stream(annotationType).filter(MergedAnnotation::isPresent).forEach(mergedAnnotation -> annotations.add(mergedAnnotation.synthesize()));
            }
        }
        return annotations;
    }

    // ---------------------------------------------------------------------（此行内容为水平分隔线，无注释，无需翻译。）
    // ConfigurableListableBeanFactory接口的实现
    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将根据内容进行准确的翻译。
    @Override
    public void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue) {
        Assert.notNull(dependencyType, "Dependency type must not be null");
        if (autowiredValue != null) {
            if (!(autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue))) {
                throw new IllegalArgumentException("Value [" + autowiredValue + "] does not implement specified dependency type [" + dependencyType.getName() + "]");
            }
            this.resolvableDependencies.put(dependencyType, autowiredValue);
        }
    }

    @Override
    public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException {
        return isAutowireCandidate(beanName, descriptor, getAutowireCandidateResolver());
    }

    /**
     * 判断指定的 Bean 定义是否符合自动装配候选资格，
     * 以便将其注入声明了匹配类型依赖的其他 Bean。
     * @param beanName 要检查的 Bean 定义的名称
     * @param descriptor 要解决的依赖描述符
     * @param resolver 用于实际解决算法的自动装配候选解析器
     * @return 是否将 Bean 考虑为自动装配候选
     */
    protected boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor, AutowireCandidateResolver resolver) throws NoSuchBeanDefinitionException {
        String bdName = BeanFactoryUtils.transformedBeanName(beanName);
        if (containsBeanDefinition(bdName)) {
            return isAutowireCandidate(beanName, getMergedLocalBeanDefinition(bdName), descriptor, resolver);
        } else if (containsSingleton(beanName)) {
            return isAutowireCandidate(beanName, new RootBeanDefinition(getType(beanName)), descriptor, resolver);
        }
        BeanFactory parent = getParentBeanFactory();
        if (parent instanceof DefaultListableBeanFactory dlbf) {
            // 在这个工厂中没有找到 bean 定义 -> 委托给父工厂。
            return dlbf.isAutowireCandidate(beanName, descriptor, resolver);
        } else if (parent instanceof ConfigurableListableBeanFactory clbf) {
            // 如果没有 DefaultListableBeanFactory，则无法传递解析器。
            return clbf.isAutowireCandidate(beanName, descriptor);
        } else {
            return true;
        }
    }

    /**
     * 判断指定的 Bean 定义是否符合自动装配候选资格，
     * 以便将其注入声明了匹配类型依赖的其他 Bean。
     * @param beanName 要检查的 Bean 定义名称
     * @param mbd 要检查的合并后的 Bean 定义
     * @param descriptor 要解决的依赖描述符
     * @param resolver 用于实际解决算法的自动装配候选解析器
     * @return Bean 是否应该被视为自动装配候选
     */
    protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd, DependencyDescriptor descriptor, AutowireCandidateResolver resolver) {
        String bdName = BeanFactoryUtils.transformedBeanName(beanName);
        resolveBeanClass(mbd, bdName);
        if (mbd.isFactoryMethodUnique && mbd.factoryMethodToIntrospect == null) {
            new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
        }
        BeanDefinitionHolder holder = (beanName.equals(bdName) ? this.mergedBeanDefinitionHolders.computeIfAbsent(beanName, key -> new BeanDefinitionHolder(mbd, beanName, getAliases(bdName))) : new BeanDefinitionHolder(mbd, beanName, getAliases(bdName)));
        return resolver.isAutowireCandidate(holder, descriptor);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        BeanDefinition bd = this.beanDefinitionMap.get(beanName);
        if (bd == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No bean named '" + beanName + "' found in " + this);
            }
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return bd;
    }

    @Override
    public Iterator<String> getBeanNamesIterator() {
        CompositeIterator<String> iterator = new CompositeIterator<>();
        iterator.add(this.beanDefinitionNames.iterator());
        iterator.add(this.manualSingletonNames.iterator());
        return iterator;
    }

    @Override
    protected void clearMergedBeanDefinition(String beanName) {
        super.clearMergedBeanDefinition(beanName);
        this.mergedBeanDefinitionHolders.remove(beanName);
    }

    @Override
    public void clearMetadataCache() {
        super.clearMetadataCache();
        this.mergedBeanDefinitionHolders.clear();
        clearByTypeCache();
    }

    @Override
    public void freezeConfiguration() {
        clearMetadataCache();
        this.configurationFrozen = true;
        this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
    }

    @Override
    public boolean isConfigurationFrozen() {
        return this.configurationFrozen;
    }

    /**
     * 如果工厂的配置已被标记为冻结，则认为所有bean都符合元数据缓存的条件。
     * @see #freezeConfiguration()
     */
    @Override
    protected boolean isBeanEligibleForMetadataCaching(String beanName) {
        return (this.configurationFrozen || super.isBeanEligibleForMetadataCaching(beanName));
    }

    @Override
    @Nullable
    protected Object obtainInstanceFromSupplier(Supplier<?> supplier, String beanName, RootBeanDefinition mbd) throws Exception {
        if (supplier instanceof InstanceSupplier<?> instanceSupplier) {
            return instanceSupplier.get(RegisteredBean.of(this, beanName, mbd));
        }
        return super.obtainInstanceFromSupplier(supplier, beanName, mbd);
    }

    @Override
    public void preInstantiateSingletons() throws BeansException {
        if (logger.isTraceEnabled()) {
            logger.trace("Pre-instantiating singletons in " + this);
        }
        // 遍历一个副本以允许初始化方法，这些方法反过来会注册新的bean定义。
        // 虽然这可能不是常规的工厂引导程序的一部分，但除此之外它运行正常。
        List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);
        // 触发所有非懒加载单例bean的初始化...
        for (String beanName : beanNames) {
            RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
            if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                if (isFactoryBean(beanName)) {
                    Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
                    if (bean instanceof SmartFactoryBean<?> smartFactoryBean && smartFactoryBean.isEagerInit()) {
                        getBean(beanName);
                    }
                } else {
                    getBean(beanName);
                }
            }
        }
        // 为所有适用的 bean 触发初始化后的回调...
        for (String beanName : beanNames) {
            Object singletonInstance = getSingleton(beanName);
            if (singletonInstance instanceof SmartInitializingSingleton smartSingleton) {
                StartupStep smartInitialize = getApplicationStartup().start("spring.beans.smart-initialize").tag("beanName", beanName);
                smartSingleton.afterSingletonsInstantiated();
                smartInitialize.end();
            }
        }
    }

    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您翻译成中文。
    // BeanDefinitionRegistry 接口的实现
    // 由于您只提供了代码注释的分割线（---------------------------------------------------------------------），并没有提供具体的代码注释内容，我无法进行翻译。请提供需要翻译的英文代码注释内容，以便我能够为您提供准确的中文翻译。
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        Assert.hasText(beanName, "Bean name must not be empty");
        Assert.notNull(beanDefinition, "BeanDefinition must not be null");
        if (beanDefinition instanceof AbstractBeanDefinition abd) {
            try {
                abd.validate();
            } catch (BeanDefinitionValidationException ex) {
                throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName, "Validation of bean definition failed", ex);
            }
        }
        BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
        if (existingDefinition != null) {
            if (!isAllowBeanDefinitionOverriding()) {
                throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
            } else if (existingDefinition.getRole() < beanDefinition.getRole()) {
                // 例如，原来是 ROLE_APPLICATION，现在覆盖为 ROLE_SUPPORT 或 ROLE_INFRASTRUCTURE
                if (logger.isInfoEnabled()) {
                    logger.info("Overriding user-defined bean definition for bean '" + beanName + "' with a framework-generated bean definition: replacing [" + existingDefinition + "] with [" + beanDefinition + "]");
                }
            } else if (!beanDefinition.equals(existingDefinition)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Overriding bean definition for bean '" + beanName + "' with a different definition: replacing [" + existingDefinition + "] with [" + beanDefinition + "]");
                }
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Overriding bean definition for bean '" + beanName + "' with an equivalent definition: replacing [" + existingDefinition + "] with [" + beanDefinition + "]");
                }
            }
            this.beanDefinitionMap.put(beanName, beanDefinition);
        } else {
            if (isAlias(beanName)) {
                if (!isAllowBeanDefinitionOverriding()) {
                    String aliasedName = canonicalName(beanName);
                    if (containsBeanDefinition(aliasedName)) {
                        // 现有 Bean 定义的重命名别名
                        throw new BeanDefinitionOverrideException(beanName, beanDefinition, getBeanDefinition(aliasedName));
                    } else {
                        // 别名指向不存在的 Bean 定义
                        throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName, "Cannot register bean definition for bean '" + beanName + "' since there is already an alias for bean '" + aliasedName + "' bound.");
                    }
                } else {
                    removeAlias(beanName);
                }
            }
            if (hasBeanCreationStarted()) {
                // 无法再修改启动时收集的元素（以保证稳定迭代）
                synchronized (this.beanDefinitionMap) {
                    this.beanDefinitionMap.put(beanName, beanDefinition);
                    List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
                    updatedDefinitions.addAll(this.beanDefinitionNames);
                    updatedDefinitions.add(beanName);
                    this.beanDefinitionNames = updatedDefinitions;
                    removeManualSingletonName(beanName);
                }
            } else {
                // 仍在启动注册阶段
                this.beanDefinitionMap.put(beanName, beanDefinition);
                this.beanDefinitionNames.add(beanName);
                removeManualSingletonName(beanName);
            }
            this.frozenBeanDefinitionNames = null;
        }
        if (existingDefinition != null || containsSingleton(beanName)) {
            resetBeanDefinition(beanName);
        } else if (isConfigurationFrozen()) {
            clearByTypeCache();
        }
    }

    @Override
    public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        Assert.hasText(beanName, "'beanName' must not be empty");
        BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
        if (bd == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No bean named '" + beanName + "' found in " + this);
            }
            throw new NoSuchBeanDefinitionException(beanName);
        }
        if (hasBeanCreationStarted()) {
            // 无法再修改启动时收集的元素（以保证稳定迭代）
            synchronized (this.beanDefinitionMap) {
                List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames);
                updatedDefinitions.remove(beanName);
                this.beanDefinitionNames = updatedDefinitions;
            }
        } else {
            // 仍在启动注册阶段
            this.beanDefinitionNames.remove(beanName);
        }
        this.frozenBeanDefinitionNames = null;
        resetBeanDefinition(beanName);
    }

    /**
     *  重置给定 Bean 的所有 Bean 定义缓存，包括从其派生的 Bean 的缓存。
     *  * <p>在现有 Bean 定义被替换或删除后调用，触发给定的 Bean 以及所有将给定 Bean 作为父级的 Bean 定义上的
     *  * {@link #clearMergedBeanDefinition}、{@link #destroySingleton} 和
     *  * {@link MergedBeanDefinitionPostProcessor#resetBeanDefinition}。
     *  * @param beanName 要重置的 Bean 的名称
     *  * @see #registerBeanDefinition
     *  * @see #removeBeanDefinition
     */
    protected void resetBeanDefinition(String beanName) {
        // 移除给定 bean 的合并 bean 定义，如果已经创建。
        clearMergedBeanDefinition(beanName);
        // 从单例缓存中移除相应的bean，如果有的话。通常不应该这样做。
        // 如果是必要的，而不仅仅是用来覆盖上下文的默认 Bean。
        // （例如，在 StaticApplicationContext 中的默认 StaticMessageSource）。
        destroySingleton(beanName);
        // 通知所有后处理器，指定的 Bean 定义已被重置。
        for (MergedBeanDefinitionPostProcessor processor : getBeanPostProcessorCache().mergedDefinition) {
            processor.resetBeanDefinition(beanName);
        }
        // 重置所有以给定bean作为父bean的bean定义（递归）。
        for (String bdName : this.beanDefinitionNames) {
            if (!beanName.equals(bdName)) {
                BeanDefinition bd = this.beanDefinitionMap.get(bdName);
                // 确保bd非空，因为beanDefinitionMap可能存在并发修改的情况。
                if (bd != null && beanName.equals(bd.getParentName())) {
                    resetBeanDefinition(bdName);
                }
            }
        }
    }

    /**
     * 仅允许在允许bean定义覆盖的情况下进行别名覆盖。
     */
    @Override
    protected boolean allowAliasOverriding() {
        return isAllowBeanDefinitionOverriding();
    }

    /**
     * 还检查是否存在别名覆盖了同名的bean定义。
     */
    @Override
    protected void checkForAliasCircle(String name, String alias) {
        super.checkForAliasCircle(name, alias);
        if (!isAllowBeanDefinitionOverriding() && containsBeanDefinition(alias)) {
            throw new IllegalStateException("Cannot register alias '" + alias + "' for name '" + name + "': Alias would override bean definition '" + alias + "'");
        }
    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
        super.registerSingleton(beanName, singletonObject);
        updateManualSingletonNames(set -> set.add(beanName), set -> !this.beanDefinitionMap.containsKey(beanName));
        clearByTypeCache();
    }

    @Override
    public void destroySingletons() {
        super.destroySingletons();
        updateManualSingletonNames(Set::clear, set -> !set.isEmpty());
        clearByTypeCache();
    }

    @Override
    public void destroySingleton(String beanName) {
        super.destroySingleton(beanName);
        removeManualSingletonName(beanName);
        clearByTypeCache();
    }

    private void removeManualSingletonName(String beanName) {
        updateManualSingletonNames(set -> set.remove(beanName), set -> set.contains(beanName));
    }

    /**
     * 更新工厂的内部手动单例名称集合。
     * @param action 修改操作
     * @param condition 修改操作的先决条件
     * (如果此条件不适用，则可以跳过该操作)
     */
    private void updateManualSingletonNames(Consumer<Set<String>> action, Predicate<Set<String>> condition) {
        if (hasBeanCreationStarted()) {
            // 无法再修改启动时集合的元素（以保证稳定迭代）
            synchronized (this.beanDefinitionMap) {
                if (condition.test(this.manualSingletonNames)) {
                    Set<String> updatedSingletons = new LinkedHashSet<>(this.manualSingletonNames);
                    action.accept(updatedSingletons);
                    this.manualSingletonNames = updatedSingletons;
                }
            }
        } else {
            // 仍在启动注册阶段
            if (condition.test(this.manualSingletonNames)) {
                action.accept(this.manualSingletonNames);
            }
        }
    }

    /**
     * 移除对按类型映射的任何假设。
     */
    private void clearByTypeCache() {
        this.allBeanNamesByType.clear();
        this.singletonBeanNamesByType.clear();
    }

    // 由于您只提供了代码注释的分割线（"---------------------------------------------------------------------"），并没有提供实际的代码注释内容，我无法进行翻译。请提供具体的代码注释内容，我才能帮您翻译成中文。
    // 依赖解析功能
    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供您希望翻译的 Java 代码注释，我将为您翻译成中文。
    @Override
    public <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException {
        Assert.notNull(requiredType, "Required type must not be null");
        NamedBeanHolder<T> namedBean = resolveNamedBean(ResolvableType.forRawClass(requiredType), null, false);
        if (namedBean != null) {
            return namedBean;
        }
        BeanFactory parent = getParentBeanFactory();
        if (parent instanceof AutowireCapableBeanFactory acbf) {
            return acbf.resolveNamedBean(requiredType);
        }
        throw new NoSuchBeanDefinitionException(requiredType);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> NamedBeanHolder<T> resolveNamedBean(ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) throws BeansException {
        Assert.notNull(requiredType, "Required type must not be null");
        String[] candidateNames = getBeanNamesForType(requiredType);
        if (candidateNames.length > 1) {
            List<String> autowireCandidates = new ArrayList<>(candidateNames.length);
            for (String beanName : candidateNames) {
                if (!containsBeanDefinition(beanName) || getBeanDefinition(beanName).isAutowireCandidate()) {
                    autowireCandidates.add(beanName);
                }
            }
            if (!autowireCandidates.isEmpty()) {
                candidateNames = StringUtils.toStringArray(autowireCandidates);
            }
        }
        if (candidateNames.length == 1) {
            return resolveNamedBean(candidateNames[0], requiredType, args);
        } else if (candidateNames.length > 1) {
            Map<String, Object> candidates = CollectionUtils.newLinkedHashMap(candidateNames.length);
            for (String beanName : candidateNames) {
                if (containsSingleton(beanName) && args == null) {
                    Object beanInstance = getBean(beanName);
                    candidates.put(beanName, (beanInstance instanceof NullBean ? null : beanInstance));
                } else {
                    candidates.put(beanName, getType(beanName));
                }
            }
            String candidateName = determinePrimaryCandidate(candidates, requiredType.toClass());
            if (candidateName == null) {
                candidateName = determineHighestPriorityCandidate(candidates, requiredType.toClass());
            }
            if (candidateName != null) {
                Object beanInstance = candidates.get(candidateName);
                if (beanInstance == null) {
                    return null;
                }
                if (beanInstance instanceof Class) {
                    return resolveNamedBean(candidateName, requiredType, args);
                }
                return new NamedBeanHolder<>(candidateName, (T) beanInstance);
            }
            if (!nonUniqueAsNull) {
                throw new NoUniqueBeanDefinitionException(requiredType, candidates.keySet());
            }
        }
        return null;
    }

    @Nullable
    private <T> NamedBeanHolder<T> resolveNamedBean(String beanName, ResolvableType requiredType, @Nullable Object[] args) throws BeansException {
        Object bean = getBean(beanName, null, args);
        if (bean instanceof NullBean) {
            return null;
        }
        return new NamedBeanHolder<>(beanName, adaptBeanInstance(beanName, bean, requiredType.toClass()));
    }

    @Override
    @Nullable
    public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName, @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {
        descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());
        if (Optional.class == descriptor.getDependencyType()) {
            return createOptionalDependency(descriptor, requestingBeanName);
        } else if (ObjectFactory.class == descriptor.getDependencyType() || ObjectProvider.class == descriptor.getDependencyType()) {
            return new DependencyObjectProvider(descriptor, requestingBeanName);
        } else if (jakartaInjectProviderClass == descriptor.getDependencyType()) {
            return new Jsr330Factory().createDependencyProvider(descriptor, requestingBeanName);
        } else {
            Object result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(descriptor, requestingBeanName);
            if (result == null) {
                result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter);
            }
            return result;
        }
    }

    @Nullable
    public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName, @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {
        InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
        try {
            Object shortcut = descriptor.resolveShortcut(this);
            if (shortcut != null) {
                return shortcut;
            }
            Class<?> type = descriptor.getDependencyType();
            Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
            if (value != null) {
                if (value instanceof String strValue) {
                    String resolvedValue = resolveEmbeddedValue(strValue);
                    BeanDefinition bd = (beanName != null && containsBean(beanName) ? getMergedBeanDefinition(beanName) : null);
                    value = evaluateBeanDefinitionString(resolvedValue, bd);
                }
                TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
                try {
                    return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
                } catch (UnsupportedOperationException ex) {
                    // 一个自定义的类型转换器，它不支持类型描述符解析...
                    return (descriptor.getField() != null ? converter.convertIfNecessary(value, type, descriptor.getField()) : converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
                }
            }
            Object multipleBeans = resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
            if (multipleBeans != null) {
                return multipleBeans;
            }
            Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
            if (matchingBeans.isEmpty()) {
                if (isRequired(descriptor)) {
                    raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
                }
                return null;
            }
            String autowiredBeanName;
            Object instanceCandidate;
            if (matchingBeans.size() > 1) {
                autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
                if (autowiredBeanName == null) {
                    if (isRequired(descriptor) || !indicatesMultipleBeans(type)) {
                        return descriptor.resolveNotUnique(descriptor.getResolvableType(), matchingBeans);
                    } else {
                        // 在可选的集合/映射的情况下，对于非唯一的情况，静默忽略：
                        // 可能原本意图创建一个包含多个常规对象的空集合
                        // （特别是在4.3版本之前，我们甚至没有寻找集合bean）。
                        return null;
                    }
                }
                instanceCandidate = matchingBeans.get(autowiredBeanName);
            } else {
                // 我们正好有一个匹配项。
                Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
                autowiredBeanName = entry.getKey();
                instanceCandidate = entry.getValue();
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.add(autowiredBeanName);
            }
            if (instanceCandidate instanceof Class) {
                instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this);
            }
            Object result = instanceCandidate;
            if (result instanceof NullBean) {
                if (isRequired(descriptor)) {
                    raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
                }
                result = null;
            }
            if (!ClassUtils.isAssignableValue(type, result)) {
                throw new BeanNotOfRequiredTypeException(autowiredBeanName, type, instanceCandidate.getClass());
            }
            return result;
        } finally {
            ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
        }
    }

    @Nullable
    private Object resolveMultipleBeans(DependencyDescriptor descriptor, @Nullable String beanName, @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) {
        Class<?> type = descriptor.getDependencyType();
        if (descriptor instanceof StreamDependencyDescriptor streamDependencyDescriptor) {
            Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
            if (autowiredBeanNames != null) {
                autowiredBeanNames.addAll(matchingBeans.keySet());
            }
            Stream<Object> stream = matchingBeans.keySet().stream().map(name -> descriptor.resolveCandidate(name, type, this)).filter(bean -> !(bean instanceof NullBean));
            if (streamDependencyDescriptor.isOrdered()) {
                stream = stream.sorted(adaptOrderComparator(matchingBeans));
            }
            return stream;
        } else if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            ResolvableType resolvableType = descriptor.getResolvableType();
            Class<?> resolvedArrayType = resolvableType.resolve(type);
            if (resolvedArrayType != type) {
                componentType = resolvableType.getComponentType().resolve();
            }
            if (componentType == null) {
                return null;
            }
            Map<String, Object> matchingBeans = findAutowireCandidates(beanName, componentType, new MultiElementDescriptor(descriptor));
            if (matchingBeans.isEmpty()) {
                return null;
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.addAll(matchingBeans.keySet());
            }
            TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
            Object result = converter.convertIfNecessary(matchingBeans.values(), resolvedArrayType);
            if (result instanceof Object[] array && array.length > 1) {
                Comparator<Object> comparator = adaptDependencyComparator(matchingBeans);
                if (comparator != null) {
                    Arrays.sort(array, comparator);
                }
            }
            return result;
        } else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
            Class<?> elementType = descriptor.getResolvableType().asCollection().resolveGeneric();
            if (elementType == null) {
                return null;
            }
            Map<String, Object> matchingBeans = findAutowireCandidates(beanName, elementType, new MultiElementDescriptor(descriptor));
            if (matchingBeans.isEmpty()) {
                return null;
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.addAll(matchingBeans.keySet());
            }
            TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
            Object result = converter.convertIfNecessary(matchingBeans.values(), type);
            if (result instanceof List<?> list && list.size() > 1) {
                Comparator<Object> comparator = adaptDependencyComparator(matchingBeans);
                if (comparator != null) {
                    list.sort(comparator);
                }
            }
            return result;
        } else if (Map.class == type) {
            ResolvableType mapType = descriptor.getResolvableType().asMap();
            Class<?> keyType = mapType.resolveGeneric(0);
            if (String.class != keyType) {
                return null;
            }
            Class<?> valueType = mapType.resolveGeneric(1);
            if (valueType == null) {
                return null;
            }
            Map<String, Object> matchingBeans = findAutowireCandidates(beanName, valueType, new MultiElementDescriptor(descriptor));
            if (matchingBeans.isEmpty()) {
                return null;
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.addAll(matchingBeans.keySet());
            }
            return matchingBeans;
        } else {
            return null;
        }
    }

    private boolean isRequired(DependencyDescriptor descriptor) {
        return getAutowireCandidateResolver().isRequired(descriptor);
    }

    private boolean indicatesMultipleBeans(Class<?> type) {
        return (type.isArray() || (type.isInterface() && (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type))));
    }

    @Nullable
    private Comparator<Object> adaptDependencyComparator(Map<String, ?> matchingBeans) {
        Comparator<Object> comparator = getDependencyComparator();
        if (comparator instanceof OrderComparator orderComparator) {
            return orderComparator.withSourceProvider(createFactoryAwareOrderSourceProvider(matchingBeans));
        } else {
            return comparator;
        }
    }

    private Comparator<Object> adaptOrderComparator(Map<String, ?> matchingBeans) {
        Comparator<Object> dependencyComparator = getDependencyComparator();
        OrderComparator comparator = (dependencyComparator instanceof OrderComparator orderComparator ? orderComparator : OrderComparator.INSTANCE);
        return comparator.withSourceProvider(createFactoryAwareOrderSourceProvider(matchingBeans));
    }

    private OrderComparator.OrderSourceProvider createFactoryAwareOrderSourceProvider(Map<String, ?> beans) {
        IdentityHashMap<Object, String> instancesToBeanNames = new IdentityHashMap<>();
        beans.forEach((beanName, instance) -> instancesToBeanNames.put(instance, beanName));
        return new FactoryAwareOrderSourceProvider(instancesToBeanNames);
    }

    /**
     * 查找匹配所需类型的bean实例。
     * 在自动装配指定bean时调用。
     * @param beanName 即将装配的bean的名称
     * @param requiredType 要查找的bean的实际类型（可能是一个数组组件类型或集合元素类型）
     * @param descriptor 要解决的依赖项的描述符
     * @return 一个Map，包含匹配所需类型的候选名称和候选实例（永远不会为{@code null}）
     * @throws BeansException 如果出现错误
     * @see #autowireByType
     * @see #autowireConstructor
     */
    protected Map<String, Object> findAutowireCandidates(@Nullable String beanName, Class<?> requiredType, DependencyDescriptor descriptor) {
        String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType, true, descriptor.isEager());
        Map<String, Object> result = CollectionUtils.newLinkedHashMap(candidateNames.length);
        for (Map.Entry<Class<?>, Object> classObjectEntry : this.resolvableDependencies.entrySet()) {
            Class<?> autowiringType = classObjectEntry.getKey();
            if (autowiringType.isAssignableFrom(requiredType)) {
                Object autowiringValue = classObjectEntry.getValue();
                autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
                if (requiredType.isInstance(autowiringValue)) {
                    result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
                    break;
                }
            }
        }
        for (String candidate : candidateNames) {
            if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, descriptor)) {
                addCandidateEntry(result, candidate, descriptor, requiredType);
            }
        }
        if (result.isEmpty()) {
            boolean multiple = indicatesMultipleBeans(requiredType);
            // 考虑回退匹配，如果第一次尝试未能找到任何内容...
            DependencyDescriptor fallbackDescriptor = descriptor.forFallbackMatch();
            for (String candidate : candidateNames) {
                if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, fallbackDescriptor) && (!multiple || getAutowireCandidateResolver().hasQualifier(descriptor))) {
                    addCandidateEntry(result, candidate, descriptor, requiredType);
                }
            }
            if (result.isEmpty() && !multiple) {
                // 将自我引用视为最后一步...
                // 但是在这种情况下，是指依赖收集，而不是指同一个Bean本身。
                for (String candidate : candidateNames) {
                    if (isSelfReference(beanName, candidate) && (!(descriptor instanceof MultiElementDescriptor) || !beanName.equals(candidate)) && isAutowireCandidate(candidate, fallbackDescriptor)) {
                        addCandidateEntry(result, candidate, descriptor, requiredType);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 向候选人映射中添加一个条目：如果可用，则为 bean 实例，否则仅为解析后的类型，以防止在主要候选者选择之前进行早期 bean 初始化。
     */
    private void addCandidateEntry(Map<String, Object> candidates, String candidateName, DependencyDescriptor descriptor, Class<?> requiredType) {
        if (descriptor instanceof MultiElementDescriptor) {
            Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
            if (!(beanInstance instanceof NullBean)) {
                candidates.put(candidateName, beanInstance);
            }
        } else if (containsSingleton(candidateName) || (descriptor instanceof StreamDependencyDescriptor streamDescriptor && streamDescriptor.isOrdered())) {
            Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
            candidates.put(candidateName, (beanInstance instanceof NullBean ? null : beanInstance));
        } else {
            candidates.put(candidateName, getType(candidateName));
        }
    }

    /**
     * 确定给定集合中的自动装配候选者。
     * <p>按顺序查找 {@code @Primary} 和 {@code @Priority}。
     * @param candidates 候选者名称和候选者实例的映射，由 {@link #findAutowireCandidates} 返回
     * @param descriptor 要匹配的目标依赖项
     * @return 自动装配候选者的名称，如果没有找到则返回 {@code null}
     */
    @Nullable
    protected String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
        Class<?> requiredType = descriptor.getDependencyType();
        String primaryCandidate = determinePrimaryCandidate(candidates, requiredType);
        if (primaryCandidate != null) {
            return primaryCandidate;
        }
        String priorityCandidate = determineHighestPriorityCandidate(candidates, requiredType);
        if (priorityCandidate != null) {
            return priorityCandidate;
        }
        // 回退：直接选择已注册的依赖项或匹配的合格bean名称
        for (Map.Entry<String, Object> entry : candidates.entrySet()) {
            String candidateName = entry.getKey();
            Object beanInstance = entry.getValue();
            if ((beanInstance != null && this.resolvableDependencies.containsValue(beanInstance)) || matchesBeanName(candidateName, descriptor.getDependencyName())) {
                return candidateName;
            }
        }
        return null;
    }

    /**
     * 确定给定集合中的主候选者。
     * @param candidates 一个包含候选者名称和候选者实例（或候选者类，如果尚未创建）的映射，这些实例或类与所需类型相匹配
     * @param requiredType 要与之匹配的目标依赖类型
     * @return 主候选者的名称，如果没有找到则返回 {@code null}
     * @see #isPrimary(String, Object)
     */
    @Nullable
    protected String determinePrimaryCandidate(Map<String, Object> candidates, Class<?> requiredType) {
        String primaryBeanName = null;
        for (Map.Entry<String, Object> entry : candidates.entrySet()) {
            String candidateBeanName = entry.getKey();
            Object beanInstance = entry.getValue();
            if (isPrimary(candidateBeanName, beanInstance)) {
                if (primaryBeanName != null) {
                    boolean candidateLocal = containsBeanDefinition(candidateBeanName);
                    boolean primaryLocal = containsBeanDefinition(primaryBeanName);
                    if (candidateLocal && primaryLocal) {
                        throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(), "more than one 'primary' bean found among candidates: " + candidates.keySet());
                    } else if (candidateLocal) {
                        primaryBeanName = candidateBeanName;
                    }
                } else {
                    primaryBeanName = candidateBeanName;
                }
            }
        }
        return primaryBeanName;
    }

    /**
     * 确定给定集合中优先级最高的候选者。
     * <p>基于 {@code @jakarta.annotation.Priority}。根据相关的
     * {@link org.springframework.core.Ordered} 接口定义，值越低优先级越高。
     * @param candidates 候选者名称和候选者实例（或尚未创建的候选者类）的映射，这些实例与所需的类型匹配
     * @param requiredType 要匹配的目标依赖类型
     * @return 优先级最高的候选者名称，如果没有找到则返回 {@code null}
     * @see #getPriority(Object)
     */
    @Nullable
    protected String determineHighestPriorityCandidate(Map<String, Object> candidates, Class<?> requiredType) {
        String highestPriorityBeanName = null;
        Integer highestPriority = null;
        for (Map.Entry<String, Object> entry : candidates.entrySet()) {
            String candidateBeanName = entry.getKey();
            Object beanInstance = entry.getValue();
            if (beanInstance != null) {
                Integer candidatePriority = getPriority(beanInstance);
                if (candidatePriority != null) {
                    if (highestPriority != null) {
                        if (candidatePriority.equals(highestPriority)) {
                            throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(), "Multiple beans found with the same priority ('" + highestPriority + "') among candidates: " + candidates.keySet());
                        } else if (candidatePriority < highestPriority) {
                            highestPriorityBeanName = candidateBeanName;
                            highestPriority = candidatePriority;
                        }
                    } else {
                        highestPriorityBeanName = candidateBeanName;
                        highestPriority = candidatePriority;
                    }
                }
            }
        }
        return highestPriorityBeanName;
    }

    /**
     * 返回给定 bean 名称的 bean 定义是否被标记为首选 bean。
     * @param beanName bean 的名称
     * @param beanInstance 相应的 bean 实例（可以为 null）
     * @return 给定的 bean 是否符合首选条件
     */
    protected boolean isPrimary(String beanName, Object beanInstance) {
        String transformedBeanName = transformedBeanName(beanName);
        if (containsBeanDefinition(transformedBeanName)) {
            return getMergedLocalBeanDefinition(transformedBeanName).isPrimary();
        }
        return (getParentBeanFactory() instanceof DefaultListableBeanFactory parent && parent.isPrimary(transformedBeanName, beanInstance));
    }

    /**
     * 返回由给定的bean实例上的{@code jakarta.annotation.Priority}注解指定的优先级。
     * 默认实现委托给指定的{@link #setDependencyComparator 依赖比较器}，如果它是Spring的通用{@link OrderComparator}的扩展（通常是一个
     * {@link org.springframework.core.annotation.AnnotationAwareOrderComparator}），则检查其
     * {@link OrderComparator#getPriority 方法}。如果没有此类比较器，此实现将返回{@code null}。
     * @param beanInstance 要检查的bean实例（可以是{@code null}）
     * @return 分配给该bean的优先级，或者如果没有设置则返回{@code null}
     */
    @Nullable
    protected Integer getPriority(Object beanInstance) {
        Comparator<Object> comparator = getDependencyComparator();
        if (comparator instanceof OrderComparator orderComparator) {
            return orderComparator.getPriority(beanInstance);
        }
        return null;
    }

    /**
     * 判断给定的候选名称是否与存储在此bean定义中的bean名称或别名匹配
     */
    protected boolean matchesBeanName(String beanName, @Nullable String candidateName) {
        return (candidateName != null && (candidateName.equals(beanName) || ObjectUtils.containsElement(getAliases(beanName), candidateName)));
    }

    /**
     * 判断给定的beanName/candidateName对是否表示自引用，
     * 即候选者是否指向原始bean，或者指向原始bean上的工厂方法。
     */
    private boolean isSelfReference(@Nullable String beanName, @Nullable String candidateName) {
        return (beanName != null && candidateName != null && (beanName.equals(candidateName) || (containsBeanDefinition(candidateName) && beanName.equals(getMergedLocalBeanDefinition(candidateName).getFactoryBeanName()))));
    }

    /**
     * 对于无法解决的依赖，抛出NoSuchBeanDefinitionException或BeanNotOfRequiredTypeException异常。
     */
    private void raiseNoMatchingBeanFound(Class<?> type, ResolvableType resolvableType, DependencyDescriptor descriptor) throws BeansException {
        checkBeanNotOfRequiredType(type, descriptor);
        throw new NoSuchBeanDefinitionException(resolvableType, "expected at least 1 bean which qualifies as autowire candidate. " + "Dependency annotations: " + ObjectUtils.nullSafeToString(descriptor.getAnnotations()));
    }

    /**
     * 如果适用，为无法解决的依赖抛出 BeanNotOfRequiredTypeException 异常，
     * 即如果bean的目标类型匹配，但暴露的代理不匹配。
     */
    private void checkBeanNotOfRequiredType(Class<?> type, DependencyDescriptor descriptor) {
        for (String beanName : this.beanDefinitionNames) {
            try {
                RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                Class<?> targetType = mbd.getTargetType();
                if (targetType != null && type.isAssignableFrom(targetType) && isAutowireCandidate(beanName, mbd, descriptor, getAutowireCandidateResolver())) {
                    // 可能是一个代理对象干扰了目标类型的匹配 -> 抛出一个有意义的异常。
                    Object beanInstance = getSingleton(beanName, false);
                    Class<?> beanType = (beanInstance != null && beanInstance.getClass() != NullBean.class ? beanInstance.getClass() : predictBeanType(beanName, mbd));
                    if (beanType != null && !type.isAssignableFrom(beanType)) {
                        throw new BeanNotOfRequiredTypeException(beanName, type, beanType);
                    }
                }
            } catch (NoSuchBeanDefinitionException ex) {
                // 在迭代过程中移除了 Bean 定义 -> 忽略。
            }
        }
        if (getParentBeanFactory() instanceof DefaultListableBeanFactory parent) {
            parent.checkBeanNotOfRequiredType(type, descriptor);
        }
    }

    /**
     * 为指定的依赖项创建一个 {@link Optional} 包装器。
     */
    private Optional<?> createOptionalDependency(DependencyDescriptor descriptor, @Nullable String beanName, final Object... args) {
        DependencyDescriptor descriptorToUse = new NestedDependencyDescriptor(descriptor) {

            @Override
            public boolean isRequired() {
                return false;
            }

            @Override
            public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
                return (!ObjectUtils.isEmpty(args) ? beanFactory.getBean(beanName, args) : super.resolveCandidate(beanName, requiredType, beanFactory));
            }
        };
        Object result = doResolveDependency(descriptorToUse, beanName, null, null);
        return (result instanceof Optional<?> optional ? optional : Optional.ofNullable(result));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ObjectUtils.identityToString(this));
        sb.append(": defining beans [");
        sb.append(StringUtils.collectionToCommaDelimitedString(this.beanDefinitionNames));
        sb.append("]; ");
        BeanFactory parent = getParentBeanFactory();
        if (parent == null) {
            sb.append("root of factory hierarchy");
        } else {
            sb.append("parent: ").append(ObjectUtils.identityToString(parent));
        }
        return sb.toString();
    }

    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您进行准确的翻译。
    // 序列化支持
    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的Java代码注释，我将为您翻译成中文。
    @Serial
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        throw new NotSerializableException("DefaultListableBeanFactory itself is not deserializable - " + "just a SerializedBeanFactoryReference is");
    }

    @Serial
    protected Object writeReplace() throws ObjectStreamException {
        if (this.serializationId != null) {
            return new SerializedBeanFactoryReference(this.serializationId);
        } else {
            throw new NotSerializableException("DefaultListableBeanFactory has no serialization id");
        }
    }

    /**
     * 最小的 ID 引用指向工厂。
     * 在反序列化时解析为实际的工厂实例。
     */
    private static class SerializedBeanFactoryReference implements Serializable {

        private final String id;

        public SerializedBeanFactoryReference(String id) {
            this.id = id;
        }

        private Object readResolve() {
            Reference<?> ref = serializableFactories.get(this.id);
            if (ref != null) {
                Object result = ref.get();
                if (result != null) {
                    return result;
                }
            }
            // 宽松回退：在找不到原始工厂时使用占位符工厂...
            DefaultListableBeanFactory dummyFactory = new DefaultListableBeanFactory();
            dummyFactory.serializationId = this.id;
            return dummyFactory;
        }
    }

    /**
     * 用于嵌套元素的依赖描述符标记。
     */
    private static class NestedDependencyDescriptor extends DependencyDescriptor {

        public NestedDependencyDescriptor(DependencyDescriptor original) {
            super(original);
            increaseNestingLevel();
        }
    }

    /**
     * 多元素声明中嵌套元素的依赖描述符。
     */
    private static class MultiElementDescriptor extends NestedDependencyDescriptor {

        public MultiElementDescriptor(DependencyDescriptor original) {
            super(original);
        }
    }

    /**
     * 用于访问多个元素的流访问依赖描述符标记。
     */
    private static class StreamDependencyDescriptor extends DependencyDescriptor {

        private final boolean ordered;

        public StreamDependencyDescriptor(DependencyDescriptor original, boolean ordered) {
            super(original);
            this.ordered = ordered;
        }

        public boolean isOrdered() {
            return this.ordered;
        }
    }

    private interface BeanObjectProvider<T> extends ObjectProvider<T>, Serializable {
    }

    /**
     * 可序列化的对象工厂/对象提供者，用于延迟解析依赖关系。
     */
    private class DependencyObjectProvider implements BeanObjectProvider<Object> {

        private final DependencyDescriptor descriptor;

        private final boolean optional;

        @Nullable
        private final String beanName;

        public DependencyObjectProvider(DependencyDescriptor descriptor, @Nullable String beanName) {
            this.descriptor = new NestedDependencyDescriptor(descriptor);
            this.optional = (this.descriptor.getDependencyType() == Optional.class);
            this.beanName = beanName;
        }

        @Override
        public Object getObject() throws BeansException {
            if (this.optional) {
                return createOptionalDependency(this.descriptor, this.beanName);
            } else {
                Object result = doResolveDependency(this.descriptor, this.beanName, null, null);
                if (result == null) {
                    throw new NoSuchBeanDefinitionException(this.descriptor.getResolvableType());
                }
                return result;
            }
        }

        @Override
        public Object getObject(final Object... args) throws BeansException {
            if (this.optional) {
                return createOptionalDependency(this.descriptor, this.beanName, args);
            } else {
                DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {

                    @Override
                    public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
                        return beanFactory.getBean(beanName, args);
                    }
                };
                Object result = doResolveDependency(descriptorToUse, this.beanName, null, null);
                if (result == null) {
                    throw new NoSuchBeanDefinitionException(this.descriptor.getResolvableType());
                }
                return result;
            }
        }

        @Override
        @Nullable
        public Object getIfAvailable() throws BeansException {
            try {
                if (this.optional) {
                    return createOptionalDependency(this.descriptor, this.beanName);
                } else {
                    DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {

                        @Override
                        public boolean isRequired() {
                            return false;
                        }
                    };
                    return doResolveDependency(descriptorToUse, this.beanName, null, null);
                }
            } catch (ScopeNotActiveException ex) {
                // 忽略非活动作用域中的已解析的Bean
                return null;
            }
        }

        @Override
        public void ifAvailable(Consumer<Object> dependencyConsumer) throws BeansException {
            Object dependency = getIfAvailable();
            if (dependency != null) {
                try {
                    dependencyConsumer.accept(dependency);
                } catch (ScopeNotActiveException ex) {
                    // 忽略非活动作用域中的已解析Bean，即使在作用域代理调用中也是如此
                }
            }
        }

        @Override
        @Nullable
        public Object getIfUnique() throws BeansException {
            DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {

                @Override
                public boolean isRequired() {
                    return false;
                }

                @Override
                @Nullable
                public Object resolveNotUnique(ResolvableType type, Map<String, Object> matchingBeans) {
                    return null;
                }
            };
            try {
                if (this.optional) {
                    return createOptionalDependency(descriptorToUse, this.beanName);
                } else {
                    return doResolveDependency(descriptorToUse, this.beanName, null, null);
                }
            } catch (ScopeNotActiveException ex) {
                // 忽略非活动作用域中的已解析Bean
                return null;
            }
        }

        @Override
        public void ifUnique(Consumer<Object> dependencyConsumer) throws BeansException {
            Object dependency = getIfUnique();
            if (dependency != null) {
                try {
                    dependencyConsumer.accept(dependency);
                } catch (ScopeNotActiveException ex) {
                    // 忽略非活动作用域中已解析的Bean，即使在作用域代理调用时也是如此。
                }
            }
        }

        @Nullable
        protected Object getValue() throws BeansException {
            if (this.optional) {
                return createOptionalDependency(this.descriptor, this.beanName);
            } else {
                return doResolveDependency(this.descriptor, this.beanName, null, null);
            }
        }

        @Override
        public Stream<Object> stream() {
            return resolveStream(false);
        }

        @Override
        public Stream<Object> orderedStream() {
            return resolveStream(true);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private Stream<Object> resolveStream(boolean ordered) {
            DependencyDescriptor descriptorToUse = new StreamDependencyDescriptor(this.descriptor, ordered);
            Object result = doResolveDependency(descriptorToUse, this.beanName, null, null);
            return (result instanceof Stream stream ? stream : Stream.of(result));
        }
    }

    /**
     * 分离的内部类，以避免对 {@code jakarta.inject} API 的硬依赖。
     * 实际的 {@code jakarta.inject.Provider} 实现嵌套在此处，以便使其对 Graal 对 DefaultListableBeanFactory 的嵌套类的反射分析不可见。
     */
    private class Jsr330Factory implements Serializable {

        public Object createDependencyProvider(DependencyDescriptor descriptor, @Nullable String beanName) {
            return new Jsr330Provider(descriptor, beanName);
        }

        private class Jsr330Provider extends DependencyObjectProvider implements Provider<Object> {

            public Jsr330Provider(DependencyDescriptor descriptor, @Nullable String beanName) {
                super(descriptor, beanName);
            }

            @Override
            @Nullable
            public Object get() throws BeansException {
                return getValue();
            }
        }
    }

    /**
     * 一个实现 {@link org.springframework.core.OrderComparator.OrderSourceProvider} 的类，它能够识别要排序的实例的bean元数据。
     * <p>查找要排序的实例的方法工厂（如果有），并让比较器检索其上定义的 {@link org.springframework.core.annotation.Order} 值。这本质上允许以下结构：
     */
    private class FactoryAwareOrderSourceProvider implements OrderComparator.OrderSourceProvider {

        private final Map<Object, String> instancesToBeanNames;

        public FactoryAwareOrderSourceProvider(Map<Object, String> instancesToBeanNames) {
            this.instancesToBeanNames = instancesToBeanNames;
        }

        @Override
        @Nullable
        public Object getOrderSource(Object obj) {
            String beanName = this.instancesToBeanNames.get(obj);
            if (beanName == null) {
                return null;
            }
            try {
                RootBeanDefinition beanDefinition = (RootBeanDefinition) getMergedBeanDefinition(beanName);
                List<Object> sources = new ArrayList<>(2);
                Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
                if (factoryMethod != null) {
                    sources.add(factoryMethod);
                }
                Class<?> targetType = beanDefinition.getTargetType();
                if (targetType != null && targetType != obj.getClass()) {
                    sources.add(targetType);
                }
                return sources.toArray();
            } catch (NoSuchBeanDefinitionException ex) {
                return null;
            }
        }
    }
}
