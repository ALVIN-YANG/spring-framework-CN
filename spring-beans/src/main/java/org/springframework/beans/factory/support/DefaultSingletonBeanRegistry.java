// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据Apache License，版本2.0（“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.core.SimpleAliasRegistry;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 用于共享Bean实例的通用注册表，实现了
 * {@link org.springframework.beans.factory.config.SingletonBeanRegistry}。
 * 允许注册应供所有调用注册表的调用者共享的单例实例，可以通过Bean名称获取。
 *
 * <p>还支持注册
 * {@link org.springframework.beans.factory.DisposableBean} 实例，
 * （这些实例可能与注册的单例实例相对应，也可能不对应），
 * 以便在注册表关闭时被销毁。可以在Bean之间注册依赖关系，以强制执行适当的关闭顺序。
 *
 * <p>此类主要作为
 * {@link org.springframework.beans.factory.BeanFactory} 实现的基类，
 * 将单例Bean实例的通用管理抽象出来。请注意，
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}
 * 接口扩展了
 * {@link SingletonBeanRegistry} 接口。
 *
 * <p>请注意，此类假设既没有Bean定义概念，也没有Bean实例的具体创建过程，
 * 与
 * {@link AbstractBeanFactory} 和
 * {@link DefaultListableBeanFactory}
 * （它们从它继承）不同。也可以用作嵌套的辅助工具来委托。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #registerSingleton
 * @see #registerDisposableBean
 * @see org.springframework.beans.factory.DisposableBean
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory
 */
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

    /**
     * 保留的最大抑制异常数。
     */
    private static final int SUPPRESSED_EXCEPTIONS_LIMIT = 100;

    /**
     * 单例对象的缓存：bean名称到bean实例的映射。
     */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * 单例工厂的缓存：bean 名称到 ObjectFactory 的映射。
     */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

    /**
     * 早期单例对象的缓存：bean名称到bean实例的映射。
     */
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

    /**
     * 注册单例的集合，包含按注册顺序排列的bean名称。
     */
    private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

    /**
     * 当前正在创建的 Bean 的名称。
     */
    private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /**
     * 当前排除在创建检查之外的Bean名称。
     */
    private final Set<String> inCreationCheckExclusions = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /**
     * 被抑制的异常集合，可用于关联相关原因。
     */
    @Nullable
    private Set<Exception> suppressedExceptions;

    /**
     * 标志位，指示我们当前是否处于destroySingletons过程中。
     */
    private boolean singletonsCurrentlyInDestruction = false;

    /**
     * 可回收的bean实例：bean名称到可回收实例的映射。
     */
    private final Map<String, DisposableBean> disposableBeans = new LinkedHashMap<>();

    /**
     * 包含的 Bean 名称之间的映射：Bean 名称到该 Bean 包含的 Bean 名称集合的映射。
     */
    private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<>(16);

    /**
     * 依赖 Bean 名称之间的映射：Bean 名称到依赖 Bean 名称集合的映射。
     */
    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

    /**
     * 依赖 Bean 名称之间的映射：Bean 名称到 Bean 依赖的 Bean 名称集合的映射。
     */
    private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);

    @Override
    public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
        Assert.notNull(beanName, "Bean name must not be null");
        Assert.notNull(singletonObject, "Singleton object must not be null");
        synchronized (this.singletonObjects) {
            Object oldObject = this.singletonObjects.get(beanName);
            if (oldObject != null) {
                throw new IllegalStateException("Could not register object [" + singletonObject + "] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
            }
            addSingleton(beanName, singletonObject);
        }
    }

    /**
     * 将给定的单例对象添加到本工厂的单例缓存中。
     * <p>用于单例的急切注册。
     * @param beanName Bean的名称
     * @param singletonObject 单例对象
     */
    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }

    /**
     * 添加给定的单例工厂，用于构建指定的单例（如果需要的话）。
     * <p>用于单例的提前注册，例如，以便能够解决循环引用。
     * @param beanName Bean的名称
     * @param singletonFactory 单例对象的工厂
     */
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(singletonFactory, "Singleton factory must not be null");
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
                this.registeredSingletons.add(beanName);
            }
        }
    }

    @Override
    @Nullable
    public Object getSingleton(String beanName) {
        return getSingleton(beanName, true);
    }

    /**
     * 返回给定名称下注册的（原始）单例对象。
     * <p>检查已实例化的单例，并允许对当前创建的单例进行早期引用（解决循环引用）。
     * @param beanName 要查找的bean名称
     * @param allowEarlyReference 是否应该创建早期引用
     * @return 已注册的单例对象，如果没有找到则返回 {@code null}
     */
    @Nullable
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        // 快速检查现有实例而不使用完整的单例锁
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            singletonObject = this.earlySingletonObjects.get(beanName);
            if (singletonObject == null && allowEarlyReference) {
                synchronized (this.singletonObjects) {
                    // 在完整的单例锁中一致地创建早期引用
                    singletonObject = this.singletonObjects.get(beanName);
                    if (singletonObject == null) {
                        singletonObject = this.earlySingletonObjects.get(beanName);
                        if (singletonObject == null) {
                            ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                            if (singletonFactory != null) {
                                singletonObject = singletonFactory.getObject();
                                this.earlySingletonObjects.put(beanName, singletonObject);
                                this.singletonFactories.remove(beanName);
                            }
                        }
                    }
                }
            }
        }
        return singletonObject;
    }

    /**
     * 返回给定名称下注册的（原始）单例对象，
     * 如果尚未注册，则创建并注册一个新的单例。
     * @param beanName bean的名称
     * @param singletonFactory 需要时用于懒加载创建单例的ObjectFactory
     * @return 注册的单例对象
     */
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "Bean name must not be null");
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                if (this.singletonsCurrentlyInDestruction) {
                    throw new BeanCreationNotAllowedException(beanName, "Singleton bean creation not allowed while singletons of this factory are in destruction " + "(Do not request a bean from a BeanFactory in a destroy method implementation!)");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
                }
                beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = new LinkedHashSet<>();
                }
                try {
                    singletonObject = singletonFactory.getObject();
                    newSingleton = true;
                } catch (IllegalStateException ex) {
                    // 在此期间单例对象是否隐式出现？
                    // 如果为是，则继续执行，因为异常表明了该状态。
                    singletonObject = this.singletonObjects.get(beanName);
                    if (singletonObject == null) {
                        throw ex;
                    }
                } catch (BeanCreationException ex) {
                    if (recordSuppressedExceptions) {
                        for (Exception suppressedException : this.suppressedExceptions) {
                            ex.addRelatedCause(suppressedException);
                        }
                    }
                    throw ex;
                } finally {
                    if (recordSuppressedExceptions) {
                        this.suppressedExceptions = null;
                    }
                    afterSingletonCreation(beanName);
                }
                if (newSingleton) {
                    addSingleton(beanName, singletonObject);
                }
            }
            return singletonObject;
        }
    }

    /**
     * 注册在创建单例bean实例过程中被抑制的异常，例如临时循环引用解析问题。
     * <p>默认实现将任何给定的异常保留在这个注册表中抑制异常的集合中，最多100个异常，并将它们作为相关原因添加到最终的顶级{@link BeanCreationException}。
     * @param ex 要注册的异常
     * @see BeanCreationException#getRelatedCauses()
     */
    protected void onSuppressedException(Exception ex) {
        synchronized (this.singletonObjects) {
            if (this.suppressedExceptions != null && this.suppressedExceptions.size() < SUPPRESSED_EXCEPTIONS_LIMIT) {
                this.suppressedExceptions.add(ex);
            }
        }
    }

    /**
     * 从本工厂的单例缓存中移除具有给定名称的bean，
     * 以便在创建失败时清理单例的提前注册。
     * @param beanName bean的名称
     * @see #getSingletonMutex()
     */
    protected void removeSingleton(String beanName) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.remove(beanName);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.remove(beanName);
        }
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return this.singletonObjects.containsKey(beanName);
    }

    @Override
    public String[] getSingletonNames() {
        synchronized (this.singletonObjects) {
            return StringUtils.toStringArray(this.registeredSingletons);
        }
    }

    @Override
    public int getSingletonCount() {
        synchronized (this.singletonObjects) {
            return this.registeredSingletons.size();
        }
    }

    public void setCurrentlyInCreation(String beanName, boolean inCreation) {
        Assert.notNull(beanName, "Bean name must not be null");
        if (!inCreation) {
            this.inCreationCheckExclusions.add(beanName);
        } else {
            this.inCreationCheckExclusions.remove(beanName);
        }
    }

    public boolean isCurrentlyInCreation(String beanName) {
        Assert.notNull(beanName, "Bean name must not be null");
        return (!this.inCreationCheckExclusions.contains(beanName) && isActuallyInCreation(beanName));
    }

    protected boolean isActuallyInCreation(String beanName) {
        return isSingletonCurrentlyInCreation(beanName);
    }

    /**
     * 返回指定的单例bean是否当前正在创建中（在整个工厂范围内）。
     * @param beanName bean的名称
     */
    public boolean isSingletonCurrentlyInCreation(@Nullable String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }

    /**
     * 单例创建前的回调。
     * <p>默认实现将正在创建的单例注册。
     * @param beanName 即将创建的单例的名称
     * @see #isSingletonCurrentlyInCreation
     */
    protected void beforeSingletonCreation(String beanName) {
        if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }
    }

    /**
     * 单例创建后的回调。
     * <p>默认实现将单例标记为不再处于创建状态。
     * @param beanName 已创建的单例的名称
     * @see #isSingletonCurrentlyInCreation
     */
    protected void afterSingletonCreation(String beanName) {
        if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.remove(beanName)) {
            throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
        }
    }

    /**
     * 将给定的bean添加到本注册表中可丢弃的bean列表中。
     * <p>可丢弃的bean通常对应于已注册的单例bean，
     * 名称匹配，但可能是一个不同的实例
     * （例如，对于未自然实现Spring的DisposableBean接口的单例，一个可丢弃的bean适配器）。
     * @param beanName bean的名称
     * @param bean bean实例
     */
    public void registerDisposableBean(String beanName, DisposableBean bean) {
        synchronized (this.disposableBeans) {
            this.disposableBeans.put(beanName, bean);
        }
    }

    /**
     * 在两个豆（Bean）之间注册一个包含关系，
     * 例如，在内部豆和其包含的外部豆之间。
     * <p>同时将包含的豆注册为依赖于被包含的豆，
     * 在销毁顺序方面。
     * @param containedBeanName 被包含（内部）豆的名称
     * @param containingBeanName 包含（外部）豆的名称
     * @see #registerDependentBean
     */
    public void registerContainedBean(String containedBeanName, String containingBeanName) {
        synchronized (this.containedBeanMap) {
            Set<String> containedBeans = this.containedBeanMap.computeIfAbsent(containingBeanName, k -> new LinkedHashSet<>(8));
            if (!containedBeans.add(containedBeanName)) {
                return;
            }
        }
        registerDependentBean(containedBeanName, containingBeanName);
    }

    /**
     * 为指定的bean注册一个依赖bean，
     * 在指定bean销毁之前销毁。
     * @param beanName bean的名称
     * @param dependentBeanName 依赖bean的名称
     */
    public void registerDependentBean(String beanName, String dependentBeanName) {
        String canonicalName = canonicalName(beanName);
        synchronized (this.dependentBeanMap) {
            Set<String> dependentBeans = this.dependentBeanMap.computeIfAbsent(canonicalName, k -> new LinkedHashSet<>(8));
            if (!dependentBeans.add(dependentBeanName)) {
                return;
            }
        }
        synchronized (this.dependenciesForBeanMap) {
            Set<String> dependenciesForBean = this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName, k -> new LinkedHashSet<>(8));
            dependenciesForBean.add(canonicalName);
        }
    }

    /**
     * 判断指定的依赖bean是否已经注册为依赖于给定的bean或其任何传递依赖。
     * @param beanName 要检查的bean的名称
     * @param dependentBeanName 依赖bean的名称
     * @since 4.0
     */
    protected boolean isDependent(String beanName, String dependentBeanName) {
        synchronized (this.dependentBeanMap) {
            return isDependent(beanName, dependentBeanName, null);
        }
    }

    private boolean isDependent(String beanName, String dependentBeanName, @Nullable Set<String> alreadySeen) {
        if (alreadySeen != null && alreadySeen.contains(beanName)) {
            return false;
        }
        String canonicalName = canonicalName(beanName);
        Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
        if (dependentBeans == null || dependentBeans.isEmpty()) {
            return false;
        }
        if (dependentBeans.contains(dependentBeanName)) {
            return true;
        }
        if (alreadySeen == null) {
            alreadySeen = new HashSet<>();
        }
        alreadySeen.add(beanName);
        for (String transitiveDependency : dependentBeans) {
            if (isDependent(transitiveDependency, dependentBeanName, alreadySeen)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否已为给定的名称注册了依赖的bean。
     * @param beanName 要检查的bean的名称
     */
    protected boolean hasDependentBean(String beanName) {
        return this.dependentBeanMap.containsKey(beanName);
    }

    /**
     * 返回所有依赖于指定bean的名称，如果有的话。
     * @param beanName bean的名称
     * @return 依赖于bean名称的数组，如果没有则返回空数组
     */
    public String[] getDependentBeans(String beanName) {
        Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
        if (dependentBeans == null) {
            return new String[0];
        }
        synchronized (this.dependentBeanMap) {
            return StringUtils.toStringArray(dependentBeans);
        }
    }

    /**
     * 返回指定Bean依赖的所有Bean的名称，如果有的话。
     * @param beanName Bean的名称
     * @return 返回依赖于该Bean的Bean名称数组，
     * 或空数组如果没有任何依赖
     */
    public String[] getDependenciesForBean(String beanName) {
        Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(beanName);
        if (dependenciesForBean == null) {
            return new String[0];
        }
        synchronized (this.dependenciesForBeanMap) {
            return StringUtils.toStringArray(dependenciesForBean);
        }
    }

    public void destroySingletons() {
        if (logger.isTraceEnabled()) {
            logger.trace("Destroying singletons in " + this);
        }
        synchronized (this.singletonObjects) {
            this.singletonsCurrentlyInDestruction = true;
        }
        String[] disposableBeanNames;
        synchronized (this.disposableBeans) {
            disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
        }
        for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
            destroySingleton(disposableBeanNames[i]);
        }
        this.containedBeanMap.clear();
        this.dependentBeanMap.clear();
        this.dependenciesForBeanMap.clear();
        clearSingletonCache();
    }

    /**
     * 清除此注册表中所有缓存的单例实例。
     * @since 4.3.15
     */
    protected void clearSingletonCache() {
        synchronized (this.singletonObjects) {
            this.singletonObjects.clear();
            this.singletonFactories.clear();
            this.earlySingletonObjects.clear();
            this.registeredSingletons.clear();
            this.singletonsCurrentlyInDestruction = false;
        }
    }

    /**
     * 销毁指定的Bean。如果找到相应的可销毁的Bean实例，则委托给 {@code destroyBean} 方法。
     * @param beanName Bean的名称
     * @see #destroyBean
     */
    public void destroySingleton(String beanName) {
        // 如果存在，则移除给定名称注册的单例。
        removeSingleton(beanName);
        // 销毁相应的 DisposableBean 实例。
        DisposableBean disposableBean;
        synchronized (this.disposableBeans) {
            disposableBean = this.disposableBeans.remove(beanName);
        }
        destroyBean(beanName, disposableBean);
    }

    /**
     * 销毁指定的 Bean。必须在销毁 Bean 本身之前销毁依赖该 Bean 的所有 Bean。不应抛出任何异常。
     * @param beanName Bean 的名称
     * @param bean 要销毁的 Bean 实例
     */
    protected void destroyBean(String beanName, @Nullable DisposableBean bean) {
        // 首先触发依赖 Bean 的销毁...
        Set<String> dependentBeanNames;
        synchronized (this.dependentBeanMap) {
            // 在完全同步中，以确保一个断开连接的集合
            dependentBeanNames = this.dependentBeanMap.remove(beanName);
        }
        if (dependentBeanNames != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Retrieved dependent beans for bean '" + beanName + "': " + dependentBeanNames);
            }
            for (String dependentBeanName : dependentBeanNames) {
                destroySingleton(dependentBeanName);
            }
        }
        // 实际上现在销毁该 Bean...
        if (bean != null) {
            try {
                bean.destroy();
            } catch (Throwable ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Destruction of bean with name '" + beanName + "' threw an exception", ex);
                }
            }
        }
        // 触发包含的 bean 的销毁...
        Set<String> containedBeans;
        synchronized (this.containedBeanMap) {
            // 在完全同步中，以确保一个断开连接的集合
            containedBeans = this.containedBeanMap.remove(beanName);
        }
        if (containedBeans != null) {
            for (String containedBeanName : containedBeans) {
                destroySingleton(containedBeanName);
            }
        }
        // 从其他 bean 的依赖关系中移除已销毁的 bean。
        synchronized (this.dependentBeanMap) {
            for (Iterator<Map.Entry<String, Set<String>>> it = this.dependentBeanMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Set<String>> entry = it.next();
                Set<String> dependenciesToClean = entry.getValue();
                dependenciesToClean.remove(beanName);
                if (dependenciesToClean.isEmpty()) {
                    it.remove();
                }
            }
        }
        // 移除已销毁的 Bean 的预准备依赖信息。
        this.dependenciesForBeanMap.remove(beanName);
    }

    /**
     * 向子类和外部协作者公开单例互斥锁。
     * <p>子类如果执行任何类型的扩展单例创建阶段，应在该给定的对象上同步。特别是，子类
     * <i>不应</i> 在单例创建过程中使用自己的互斥锁，以避免在懒加载情况下出现死锁的可能性。
     */
    @Override
    public final Object getSingletonMutex() {
        return this.singletonObjects;
    }
}
