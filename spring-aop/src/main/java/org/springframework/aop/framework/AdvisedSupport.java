// 翻译完成 glm-4-flash
/*版权所有 2002-2024 原作者或原作者。

根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInfo;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * AOP代理配置管理器的基类。
 *
 * 这些本身不是AOP代理，但这个类的子类通常是直接获取AOP代理实例的工厂。
 *
 * 这个类释放了子类对Advices和Advisors的管理工作，但并没有实际实现代理创建方法，这些方法由子类提供。
 *
 * 这个类是可序列化的；子类不需要是可序列化的。
 *
 * 这个类用于保存代理的快照。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see org.springframework.aop.framework.AopProxy
 */
public class AdvisedSupport extends ProxyConfig implements Advised {

    /**
     * 使用自 Spring 2.0 以来版本的 serialVersionUID 以确保互操作性。
     */
    private static final long serialVersionUID = 2651364800145442165L;

    /**
     * 当没有目标时，提供由顾问提供的行为的规范目标源。
     */
    public static final TargetSource EMPTY_TARGET_SOURCE = EmptyTargetSource.INSTANCE;

    /**
     * 包级保护，以允许为了效率而直接访问。
     */
    TargetSource targetSource = EMPTY_TARGET_SOURCE;

    /**
     * 是否已经针对特定目标类对顾问进行了筛选。
     */
    private boolean preFiltered = false;

    /**
     * 要使用的 AdvisorChainFactory。
     */
    private AdvisorChainFactory advisorChainFactory;

    /**
     * 使用方法作为键，顾问链列表作为值的缓存。
     */
    private transient Map<MethodCacheKey, List<Object>> methodCache;

    /**
     * 代理需要实现的接口。保存在列表中以保持注册顺序，用于创建具有指定接口顺序的 JDK 代理。
     */
    private List<Class<?>> interfaces = new ArrayList<>();

    /**
     * 顾问列表。如果添加了一个建议（Advice），它将在被添加到这个列表之前被包裹在一个顾问（Advisor）对象中。
     */
    private List<Advisor> advisors = new ArrayList<>();

    /**
     * 最小化的 {@link AdvisorKeyEntry} 实例列表，
     * 在缩减时将被分配到 {@link #advisors} 字段。
     * @since 6.0.10
     * @see #reduceToAdvisorKey
     */
    private List<Advisor> advisorKey = this.advisors;

    /**
     * 无参构造函数，用于作为JavaBean使用。
     */
    public AdvisedSupport() {
        this.advisorChainFactory = DefaultAdvisorChainFactory.INSTANCE;
        this.methodCache = new ConcurrentHashMap<>(32);
    }

    /**
     * 使用给定的参数创建一个 {@code AdvisedSupport} 实例。
     * @param interfaces 被代理的接口
     */
    public AdvisedSupport(Class<?>... interfaces) {
        this();
        setInterfaces(interfaces);
    }

    /**
     * 用于 {@link #getConfigurationOnlyCopy()} 的内部构造函数。
     * @since 6.0.10
     */
    private AdvisedSupport(AdvisorChainFactory advisorChainFactory, Map<MethodCacheKey, List<Object>> methodCache) {
        this.advisorChainFactory = advisorChainFactory;
        this.methodCache = methodCache;
    }

    /**
     * 将给定对象设置为目标。
     * <p>将为该对象创建一个SingletonTargetSource。
     * @see #setTargetSource
     * @see org.springframework.aop.target.SingletonTargetSource
     */
    public void setTarget(Object target) {
        setTargetSource(new SingletonTargetSource(target));
    }

    @Override
    public void setTargetSource(@Nullable TargetSource targetSource) {
        this.targetSource = (targetSource != null ? targetSource : EMPTY_TARGET_SOURCE);
    }

    @Override
    public TargetSource getTargetSource() {
        return this.targetSource;
    }

    /**
     * 设置要被代理的目标类，表示代理可以被转换为指定的类。
     * <p>内部，将使用一个针对给定目标类的 {@link org.springframework.aop.target.EmptyTargetSource}。
     * 需要的代理类型将在代理实际创建时确定。
     * <p>此方法用于替代设置 "targetSource" 或 "target"，在这种情况下，我们希望基于目标类（可以是接口或具体类）创建代理
     * （而不需要完全可用的TargetSource）。
     * @see #setTargetSource
     * @see #setTarget
     */
    public void setTargetClass(@Nullable Class<?> targetClass) {
        this.targetSource = EmptyTargetSource.forClass(targetClass);
    }

    @Override
    @Nullable
    public Class<?> getTargetClass() {
        return this.targetSource.getTargetClass();
    }

    @Override
    public void setPreFiltered(boolean preFiltered) {
        this.preFiltered = preFiltered;
    }

    @Override
    public boolean isPreFiltered() {
        return this.preFiltered;
    }

    /**
     * 设置要使用的顾问链工厂。
     * <p>默认为 {@link DefaultAdvisorChainFactory}。
     */
    public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
        Assert.notNull(advisorChainFactory, "AdvisorChainFactory must not be null");
        this.advisorChainFactory = advisorChainFactory;
    }

    /**
     * 返回要使用的顾问链工厂（决不为空）。
     */
    public AdvisorChainFactory getAdvisorChainFactory() {
        return this.advisorChainFactory;
    }

    /**
     * 设置要代理的接口。
     */
    public void setInterfaces(Class<?>... interfaces) {
        Assert.notNull(interfaces, "Interfaces must not be null");
        this.interfaces.clear();
        for (Class<?> ifc : interfaces) {
            addInterface(ifc);
        }
    }

    /**
     * 添加一个新的代理接口。
     * @param intf 要代理的额外接口
     */
    public void addInterface(Class<?> intf) {
        Assert.notNull(intf, "Interface must not be null");
        if (!intf.isInterface()) {
            throw new IllegalArgumentException("[" + intf.getName() + "] is not an interface");
        }
        if (!this.interfaces.contains(intf)) {
            this.interfaces.add(intf);
            adviceChanged();
        }
    }

    /**
     * 移除代理接口。
     * <p>如果给定的接口没有被代理，则不执行任何操作。
     * @param intf 要从代理中移除的接口
     * @return 如果接口被移除，则返回 {@code true}；如果接口未找到，因此无法移除，则返回 {@code false}
     */
    public boolean removeInterface(Class<?> intf) {
        return this.interfaces.remove(intf);
    }

    @Override
    public Class<?>[] getProxiedInterfaces() {
        return ClassUtils.toClassArray(this.interfaces);
    }

    @Override
    public boolean isInterfaceProxied(Class<?> intf) {
        for (Class<?> proxyIntf : this.interfaces) {
            if (intf.isAssignableFrom(proxyIntf)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final Advisor[] getAdvisors() {
        return this.advisors.toArray(new Advisor[0]);
    }

    @Override
    public int getAdvisorCount() {
        return this.advisors.size();
    }

    @Override
    public void addAdvisor(Advisor advisor) {
        int pos = this.advisors.size();
        addAdvisor(pos, advisor);
    }

    @Override
    public void addAdvisor(int pos, Advisor advisor) throws AopConfigException {
        if (advisor instanceof IntroductionAdvisor introductionAdvisor) {
            validateIntroductionAdvisor(introductionAdvisor);
        }
        addAdvisorInternal(pos, advisor);
    }

    @Override
    public boolean removeAdvisor(Advisor advisor) {
        int index = indexOf(advisor);
        if (index == -1) {
            return false;
        } else {
            removeAdvisor(index);
            return true;
        }
    }

    @Override
    public void removeAdvisor(int index) throws AopConfigException {
        if (isFrozen()) {
            throw new AopConfigException("Cannot remove Advisor: Configuration is frozen.");
        }
        if (index < 0 || index > this.advisors.size() - 1) {
            throw new AopConfigException("Advisor index " + index + " is out of bounds: " + "This configuration only has " + this.advisors.size() + " advisors.");
        }
        Advisor advisor = this.advisors.remove(index);
        if (advisor instanceof IntroductionAdvisor introductionAdvisor) {
            // 我们需要移除引入接口。
            for (Class<?> ifc : introductionAdvisor.getInterfaces()) {
                removeInterface(ifc);
            }
        }
        adviceChanged();
    }

    @Override
    public int indexOf(Advisor advisor) {
        Assert.notNull(advisor, "Advisor must not be null");
        return this.advisors.indexOf(advisor);
    }

    @Override
    public boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException {
        Assert.notNull(a, "Advisor a must not be null");
        Assert.notNull(b, "Advisor b must not be null");
        int index = indexOf(a);
        if (index == -1) {
            return false;
        }
        removeAdvisor(index);
        addAdvisor(index, b);
        return true;
    }

    /**
     * 将所有给定的顾问添加到这个代理配置中。
     * @param advisors 要注册的顾问
     */
    public void addAdvisors(Advisor... advisors) {
        addAdvisors(Arrays.asList(advisors));
    }

    /**
     * 将所有给定的顾问添加到这个代理配置中。
     * @param advisors 要注册的顾问
     */
    public void addAdvisors(Collection<Advisor> advisors) {
        if (isFrozen()) {
            throw new AopConfigException("Cannot add advisor: Configuration is frozen.");
        }
        if (!CollectionUtils.isEmpty(advisors)) {
            for (Advisor advisor : advisors) {
                if (advisor instanceof IntroductionAdvisor introductionAdvisor) {
                    validateIntroductionAdvisor(introductionAdvisor);
                }
                Assert.notNull(advisor, "Advisor must not be null");
                this.advisors.add(advisor);
            }
            adviceChanged();
        }
    }

    private void validateIntroductionAdvisor(IntroductionAdvisor advisor) {
        advisor.validateInterfaces();
        // 如果导师通过了验证，我们就可以进行更改。
        for (Class<?> ifc : advisor.getInterfaces()) {
            addInterface(ifc);
        }
    }

    private void addAdvisorInternal(int pos, Advisor advisor) throws AopConfigException {
        Assert.notNull(advisor, "Advisor must not be null");
        if (isFrozen()) {
            throw new AopConfigException("Cannot add advisor: Configuration is frozen.");
        }
        if (pos > this.advisors.size()) {
            throw new IllegalArgumentException("Illegal position " + pos + " in advisor list with size " + this.advisors.size());
        }
        this.advisors.add(pos, advisor);
        adviceChanged();
    }

    /**
     * 允许无限制访问 {@link List} 中的 {@link Advisor Advisors} 列表。
     * <p>请谨慎使用，并在进行任何修改时记得调用 {@link #adviceChanged() 触发建议更改事件}。
     */
    protected final List<Advisor> getAdvisorsInternal() {
        return this.advisors;
    }

    @Override
    public void addAdvice(Advice advice) throws AopConfigException {
        int pos = this.advisors.size();
        addAdvice(pos, advice);
    }

    /**
     * 无法通过这种方式添加介绍，除非建议实现 IntroductionInfo 接口。
     */
    @Override
    public void addAdvice(int pos, Advice advice) throws AopConfigException {
        Assert.notNull(advice, "Advice must not be null");
        if (advice instanceof IntroductionInfo introductionInfo) {
            // 我们不需要为这种介绍使用 IntroductionAdvisor：
            // 它完全自描述。
            addAdvisor(pos, new DefaultIntroductionAdvisor(advice, introductionInfo));
        } else if (advice instanceof DynamicIntroductionAdvice) {
            // 我们需要为这种介绍使用一个 IntroductionAdvisor。
            throw new AopConfigException("DynamicIntroductionAdvice may only be added as part of IntroductionAdvisor");
        } else {
            addAdvisor(pos, new DefaultPointcutAdvisor(advice));
        }
    }

    @Override
    public boolean removeAdvice(Advice advice) throws AopConfigException {
        int index = indexOf(advice);
        if (index == -1) {
            return false;
        } else {
            removeAdvisor(index);
            return true;
        }
    }

    @Override
    public int indexOf(Advice advice) {
        Assert.notNull(advice, "Advice must not be null");
        for (int i = 0; i < this.advisors.size(); i++) {
            Advisor advisor = this.advisors.get(i);
            if (advisor.getAdvice() == advice) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 给定的建议是否包含在此代理配置的任何顾问中？
     * @param advice 要检查包含的建议
     * @return 是否包含此建议实例
     */
    public boolean adviceIncluded(@Nullable Advice advice) {
        if (advice != null) {
            for (Advisor advisor : this.advisors) {
                if (advisor.getAdvice() == advice) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 统计给定类的建议数量。
     * @param adviceClass 要检查的建议类
     * @return 该类及其子类的拦截器数量
     */
    public int countAdvicesOfType(@Nullable Class<?> adviceClass) {
        int count = 0;
        if (adviceClass != null) {
            for (Advisor advisor : this.advisors) {
                if (adviceClass.isInstance(advisor.getAdvice())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 根据当前配置确定给定方法的 {@link org.aopalliance.intercept.MethodInterceptor} 对象列表。
     * @param method 被代理的方法
     * @param targetClass 目标类
     * @return 一个包含方法拦截器（可能还包括 InterceptorAndDynamicMethodMatchers）的列表
     */
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, @Nullable Class<?> targetClass) {
        MethodCacheKey cacheKey = new MethodCacheKey(method);
        List<Object> cached = this.methodCache.get(cacheKey);
        if (cached == null) {
            cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(this, method, targetClass);
            this.methodCache.put(cacheKey, cached);
        }
        return cached;
    }

    /**
     * 当通知（Advice）发生变化时被调用。
     */
    protected void adviceChanged() {
        this.methodCache.clear();
    }

    /**
     * 在使用无参构造函数创建的新实例上调用此方法，可以从给定对象创建配置的独立副本。
     * @param other 要从中复制配置的AdvisedSupport对象
     */
    protected void copyConfigurationFrom(AdvisedSupport other) {
        copyConfigurationFrom(other, other.targetSource, new ArrayList<>(other.advisors));
    }

    /**
     * 从给定的 {@link AdvisedSupport} 对象复制 AOP 配置，
     * 但允许替换一个新的 {@link TargetSource} 和指定的拦截器链。
     * @param other 从该 {@code AdvisedSupport} 对象获取代理配置
     * @param targetSource 新的 TargetSource
     * @param advisors 拦截器链的 Advisors
     */
    protected void copyConfigurationFrom(AdvisedSupport other, TargetSource targetSource, List<Advisor> advisors) {
        copyFrom(other);
        this.targetSource = targetSource;
        this.advisorChainFactory = other.advisorChainFactory;
        this.interfaces = new ArrayList<>(other.interfaces);
        for (Advisor advisor : advisors) {
            if (advisor instanceof IntroductionAdvisor introductionAdvisor) {
                validateIntroductionAdvisor(introductionAdvisor);
            }
            Assert.notNull(advisor, "Advisor must not be null");
            this.advisors.add(advisor);
        }
        adviceChanged();
    }

    /**
     * 构建一个仅包含配置的此 {@link AdvisedSupport} 的副本，
     * 替换掉 {@link TargetSource}。
     */
    AdvisedSupport getConfigurationOnlyCopy() {
        AdvisedSupport copy = new AdvisedSupport(this.advisorChainFactory, this.methodCache);
        copy.copyFrom(this);
        copy.targetSource = EmptyTargetSource.forClass(getTargetClass(), getTargetSource().isStatic());
        copy.interfaces = new ArrayList<>(this.interfaces);
        copy.advisors = new ArrayList<>(this.advisors);
        copy.advisorKey = new ArrayList<>(this.advisors.size());
        for (Advisor advisor : this.advisors) {
            copy.advisorKey.add(new AdvisorKeyEntry(advisor));
        }
        return copy;
    }

    void reduceToAdvisorKey() {
        this.advisors = this.advisorKey;
        this.methodCache = Collections.emptyMap();
    }

    Object getAdvisorKey() {
        return this.advisorKey;
    }

    @Override
    public String toProxyConfigString() {
        return toString();
    }

    /**
     * 用于调试/诊断。
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append(": ").append(this.interfaces.size()).append(" interfaces ");
        sb.append(ClassUtils.classNamesToString(this.interfaces)).append("; ");
        sb.append(this.advisors.size()).append(" advisors ");
        sb.append(this.advisors).append("; ");
        sb.append("targetSource [").append(this.targetSource).append("]; ");
        sb.append(super.toString());
        return sb.toString();
    }

    // 很抱歉，您只提供了一个代码注释的分隔符（"---------------------------------------------------------------------"），没有提供实际的代码注释内容。请提供需要翻译的代码注释部分，以便我能够为您翻译。
    // 序列化支持
    // 您没有提供具体的Java代码注释内容，因此我无法进行翻译。请提供需要翻译的代码注释内容，我将为您翻译成中文。
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // 依赖默认序列化；仅在反序列化后初始化状态。
        ois.defaultReadObject();
        // 初始化 transient 字段。
        this.methodCache = new ConcurrentHashMap<>(32);
    }

    /**
     * /**
     *  简单的 Method 包装类。用作缓存方法时的键，以提高 equals 和 hashCode 的比较效率。
     * /
     */
    private static final class MethodCacheKey implements Comparable<MethodCacheKey> {

        private final Method method;

        private final int hashCode;

        public MethodCacheKey(Method method) {
            this.method = method;
            this.hashCode = method.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof MethodCacheKey that && this.method == that.method));
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }

        @Override
        public String toString() {
            return this.method.toString();
        }

        @Override
        public int compareTo(MethodCacheKey other) {
            int result = this.method.getName().compareTo(other.method.getName());
            if (result == 0) {
                result = this.method.toString().compareTo(other.method.toString());
            }
            return result;
        }
    }

    /**
     * 用于关键目的的 {@link Advisor} 实例的存根，允许对建议类和切入点进行高效的 equals 和 hashCode 比较。
     * @since 6.0.10
     * @see #getConfigurationOnlyCopy()
     * @see #getAdvisorKey()
     */
    private static final class AdvisorKeyEntry implements Advisor {

        private final Class<?> adviceType;

        @Nullable
        private final String classFilterKey;

        @Nullable
        private final String methodMatcherKey;

        public AdvisorKeyEntry(Advisor advisor) {
            this.adviceType = advisor.getAdvice().getClass();
            if (advisor instanceof PointcutAdvisor pointcutAdvisor) {
                Pointcut pointcut = pointcutAdvisor.getPointcut();
                this.classFilterKey = pointcut.getClassFilter().toString();
                this.methodMatcherKey = pointcut.getMethodMatcher().toString();
            } else {
                this.classFilterKey = null;
                this.methodMatcherKey = null;
            }
        }

        @Override
        public Advice getAdvice() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object other) {
            return (this == other || (other instanceof AdvisorKeyEntry that && this.adviceType == that.adviceType && ObjectUtils.nullSafeEquals(this.classFilterKey, that.classFilterKey) && ObjectUtils.nullSafeEquals(this.methodMatcherKey, that.methodMatcherKey)));
        }

        @Override
        public int hashCode() {
            return this.adviceType.hashCode();
        }
    }
}
