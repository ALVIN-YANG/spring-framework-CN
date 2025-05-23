// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。

根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下网址获得许可证副本：

      https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权性的保证。
有关许可的具体语言，请参阅许可证，了解权限和限制。*/
package org.springframework.aop.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Interceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.UnknownAdviceTypeException;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 实现了 `org.springframework.beans.factory.FactoryBean` 的类，基于 Spring `org.springframework.beans.factory.BeanFactory` 中的 Bean 构建 AOP 代理。
 *
 * <p>`org.aopalliance.intercept.MethodInterceptor` 方法拦截器和 `org.springframework.aop.Advisor` 顾问通过当前 Bean 工厂中指定的一组 Bean 名称列表进行识别，通过 "interceptorNames" 属性指定。列表中的最后一个条目可以是目标 Bean 的名称或 `org.springframework.aop.TargetSource`；然而，通常更推荐使用 "targetName"/"target"/"targetSource" 属性。
 *
 * <p>全局拦截器和顾问可以在工厂级别添加。指定的那些在拦截器列表中展开，其中包含一个 "xxx*" 条目，匹配给定的前缀与 Bean 名称——例如，"global*" 将匹配 "globalBean1" 和 "globalBean2"；而 "*" 将匹配所有定义的拦截器。匹配的拦截器将根据它们返回的顺序值应用，如果它们实现了 `org.springframework.core.Ordered` 接口。
 *
 * <p>当提供了代理接口时，创建 JDK 代理；如果没有提供，则为目标实际类创建 CGLIB 代理。注意，后者仅在目标类没有 final 方法的情况下才能正常工作，因为运行时会动态创建一个子类。
 *
 * <p>可以从这个工厂将获得的代理转换为 `Advised`，或者获取 `ProxyFactoryBean` 引用并对其进行程序化操作。这对于现有的原型引用是不工作的，因为它们是独立的。然而，对于随后从工厂获得的原型是有效的。拦截器的更改将立即对单例（包括现有引用）生效。但是，要更改接口或目标，需要从工厂获取一个新的实例。这意味着从工厂获取的单例实例不具有相同的对象标识。然而，它们具有相同的拦截器和目标，更改任何引用都将更改所有对象。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setInterceptorNames
 * @see #setProxyInterfaces
 * @see org.aopalliance.intercept.MethodInterceptor
 * @see org.springframework.aop.Advisor
 * @see Advised
 */
@SuppressWarnings("serial")
public class ProxyFactoryBean extends ProxyCreatorSupport implements FactoryBean<Object>, BeanClassLoaderAware, BeanFactoryAware {

    /**
     * 在拦截器列表中的值后缀表示要扩展全局变量。
     */
    public static final String GLOBAL_SUFFIX = "*";

    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private String[] interceptorNames;

    @Nullable
    private String targetName;

    private boolean autodetectInterfaces = true;

    private boolean singleton = true;

    private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

    private boolean freezeProxy = false;

    @Nullable
    private transient ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

    private transient boolean classLoaderConfigured = false;

    @Nullable
    private transient BeanFactory beanFactory;

    /**
     * 是否顾问链已经初始化。
     */
    private boolean advisorChainInitialized = false;

    /**
     * 如果这是一个单例，则缓存的单例代理实例。
     */
    @Nullable
    private Object singletonInstance;

    /**
     * 设置我们要代理的接口名称。如果没有提供接口，将创建实际类的CGLIB代理。
     * <p>这本质上等同于“setInterfaces”方法，但与TransactionProxyFactoryBean的“setProxyInterfaces”方法相对应。
     * @see #setInterfaces
     * @see AbstractSingletonProxyFactoryBean#setProxyInterfaces
     */
    public void setProxyInterfaces(Class<?>[] proxyInterfaces) throws ClassNotFoundException {
        setInterfaces(proxyInterfaces);
    }

    /**
     * 设置Advice/Advisor Bean的名称列表。在Bean工厂中使用此工厂Bean时，此列表必须始终设置。
     * <p>引用的Bean应该是Interceptor、Advisor或Advice类型。
     * 列表中的最后一个条目可以是工厂中任何Bean的名称。
     * 如果它既不是Advice也不是Advisor，则将添加一个新的SingletonTargetSource来包装它。
     * 如果设置了"target"、"targetSource"或"targetName"属性，则此类目标Bean不能使用，
     * 在这种情况下，"interceptorNames"数组必须只包含Advice/Advisor Bean名称。
     * <p><b>注意：</b>在"interceptorNames"列表中指定目标Bean作为最终名称已被弃用，将在未来的Spring版本中删除。
     * 请使用`targetName`属性代替。
     * @see org.aopalliance.intercept.MethodInterceptor
     * @see org.springframework.aop.Advisor
     * @see org.aopalliance.aop.Advice
     * @see org.springframework.aop.target.SingletonTargetSource
     */
    public void setInterceptorNames(String... interceptorNames) {
        this.interceptorNames = interceptorNames;
    }

    /**
     * 设置目标Bean的名称。这是在"interceptorNames"数组末尾指定目标名称的替代方法。
     * <p>您还可以通过"target"或"targetSource"属性，直接指定目标对象或TargetSource对象。
     * @see #setInterceptorNames(String[])
     * @see #setTarget(Object)
     * @see #setTargetSource(org.springframework.aop.TargetSource)
     */
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    /**
     * 设置是否在未指定代理接口时自动检测代理接口。
     * <p>默认值为 "true"。关闭此标志将创建一个针对完整目标类的CGLIB代理，如果未指定接口。
     * @see #setProxyTargetClass
     */
    public void setAutodetectInterfaces(boolean autodetectInterfaces) {
        this.autodetectInterfaces = autodetectInterfaces;
    }

    /**
     * 设置单例属性的值。控制此工厂是否始终返回相同的代理实例（这暗示了相同的目标），
     * 或者是否返回一个新的原型实例，这暗示了如果从原型bean定义中获取，
     * 目标和拦截器也可能是新实例。这允许对对象图中独立性/唯一性的精细控制。
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    /**
     * 指定要使用的 AdvisorAdapterRegistry。
     * 默认为全局 AdvisorAdapterRegistry。
     * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
     */
    public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
        this.advisorAdapterRegistry = advisorAdapterRegistry;
    }

    @Override
    public void setFrozen(boolean frozen) {
        this.freezeProxy = frozen;
    }

    /**
     * 设置用于生成代理类的类加载器。
     * <p>默认是bean的类加载器，即包含BeanFactory用于加载所有bean类的类加载器。这里可以覆盖默认值以针对特定代理进行设置。
     */
    public void setProxyClassLoader(@Nullable ClassLoader classLoader) {
        this.proxyClassLoader = classLoader;
        this.classLoaderConfigured = (classLoader != null);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        if (!this.classLoaderConfigured) {
            this.proxyClassLoader = classLoader;
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        checkInterceptorNames();
    }

    /**
     * 返回一个代理对象。当客户端从该工厂Bean获取Bean时调用。
     * 创建要由该工厂返回的AOP代理实例。
     * 对于单例，实例将被缓存；对于代理，每次调用`getObject()`时都会创建新实例。
     * @return 一个反映当前工厂状态的全新AOP代理
     */
    @Override
    @Nullable
    public Object getObject() throws BeansException {
        initializeAdvisorChain();
        if (isSingleton()) {
            return getSingletonInstance();
        } else {
            if (this.targetName == null) {
                logger.info("Using non-singleton proxies with singleton targets is often undesirable. " + "Enable prototype proxies by setting the 'targetName' property.");
            }
            return newPrototypeInstance();
        }
    }

    /**
     * 返回代理的类型。如果已经创建了单例实例，将检查该实例；否则，将回退到代理接口（如果是只有一个的话），目标Bean类型，或TargetSource的目标类。
     * @see org.springframework.aop.framework.AopProxy#getProxyClass
     */
    @Override
    @Nullable
    public Class<?> getObjectType() {
        synchronized (this) {
            if (this.singletonInstance != null) {
                return this.singletonInstance.getClass();
            }
        }
        try {
            // 这可能是 incomplete（不完整的），因为它可能遗漏了引入的 interfaces（接口）。
            // 从通过setInterceptorNames懒加载的Advisors。
            return createAopProxy().getProxyClass(this.proxyClassLoader);
        } catch (AopConfigException ex) {
            if (getTargetClass() == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to determine early proxy class: " + ex.getMessage());
                }
                return null;
            } else {
                throw ex;
            }
        }
    }

    @Override
    public boolean isSingleton() {
        return this.singleton;
    }

    /**
     * 返回该类代理对象的单例实例，
     * 如果尚未创建，则懒加载创建。
     * @return 共享的单例代理
     */
    private synchronized Object getSingletonInstance() {
        if (this.singletonInstance == null) {
            this.targetSource = freshTargetSource();
            if (this.autodetectInterfaces && getProxiedInterfaces().length == 0 && !isProxyTargetClass()) {
                // 依赖于AOP（面向切面编程）基础设施来告知我们哪些接口需要代理。
                Class<?> targetClass = getTargetClass();
                if (targetClass == null) {
                    throw new FactoryBeanNotInitializedException("Cannot determine target class for proxy");
                }
                setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
            }
            // 初始化共享单例实例。
            super.setFrozen(this.freezeProxy);
            this.singletonInstance = getProxy(createAopProxy());
        }
        return this.singletonInstance;
    }

    /**
     * 创建此类创建的代理对象的新原型实例，
     * 由一个独立的AdvisedSupport配置支持。
     * @return 一个完全独立的代理，我们可以独立地操作其建议（advice）
     */
    private synchronized Object newPrototypeInstance() {
        // 在原型的情况下，我们需要提供代理
        // 一个配置的独立实例。
        // 在这种情况下，没有任何代理将拥有此对象配置的实例，
        // 但将有一个独立的副本。
        ProxyCreatorSupport copy = new ProxyCreatorSupport(getAopProxyFactory());
        // 复制需要一个全新的顾问链，以及一个全新的目标源。
        TargetSource targetSource = freshTargetSource();
        copy.copyConfigurationFrom(this, targetSource, freshAdvisorChain());
        if (this.autodetectInterfaces && getProxiedInterfaces().length == 0 && !isProxyTargetClass()) {
            // 依赖 AOP（面向切面编程）基础设施来告诉我们需要代理哪些接口。
            Class<?> targetClass = targetSource.getTargetClass();
            if (targetClass != null) {
                copy.setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
            }
        }
        copy.setFrozen(this.freezeProxy);
        return getProxy(copy.createAopProxy());
    }

    /**
     * 返回要暴露的代理对象。
     * <p>默认实现使用工厂的bean类加载器调用 {@code getProxy} 方法。可以覆盖此方法以指定自定义类加载器。
     * @param aopProxy 从中获取代理的已准备AopProxy实例
     * @return 要暴露的代理对象
     * @see AopProxy#getProxy(ClassLoader)
     */
    protected Object getProxy(AopProxy aopProxy) {
        return aopProxy.getProxy(this.proxyClassLoader);
    }

    /**
     * 检查interceptorNames列表是否包含作为最后一个元素的targetName。
     * 如果找到，从列表中移除最后一个名称并将其设置为targetName。
     */
    private void checkInterceptorNames() {
        if (!ObjectUtils.isEmpty(this.interceptorNames)) {
            String finalName = this.interceptorNames[this.interceptorNames.length - 1];
            if (this.targetName == null && this.targetSource == EMPTY_TARGET_SOURCE) {
                // 链中的最后一个名称可能是一个顾问/建议或目标/目标源。
                // 很遗憾我们不知道；我们必须查看该bean的类型。
                if (!finalName.endsWith(GLOBAL_SUFFIX) && !isNamedBeanAnAdvisorOrAdvice(finalName)) {
                    // 目标不是一个拦截器。
                    this.targetName = finalName;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Bean with name '" + finalName + "' concluding interceptor chain " + "is not an advisor class: treating it as a target or TargetSource");
                    }
                    this.interceptorNames = Arrays.copyOf(this.interceptorNames, this.interceptorNames.length - 1);
                }
            }
        }
    }

    /**
     * 查看Bean工厂元数据以确定此bean名称（该名称包含拦截器名称列表的结尾）是一个Advisor、Advice，或者可能是一个目标。
     * @param beanName 要检查的bean名称
     * @return 如果是Advisor或Advice，则返回{@code true}
     */
    private boolean isNamedBeanAnAdvisorOrAdvice(String beanName) {
        Assert.state(this.beanFactory != null, "No BeanFactory set");
        Class<?> namedBeanClass = this.beanFactory.getType(beanName);
        if (namedBeanClass != null) {
            return (Advisor.class.isAssignableFrom(namedBeanClass) || Advice.class.isAssignableFrom(namedBeanClass));
        }
        // 如果无法判断，则将其视为目标 Bean。
        if (logger.isDebugEnabled()) {
            logger.debug("Could not determine type of bean with name '" + beanName + "' - assuming it is neither an Advisor nor an Advice");
        }
        return false;
    }

    /**
     * 创建顾问（拦截器）链。从BeanFactory中获取的顾问每次添加新的原型实例时都会被刷新。通过工厂API程序化添加的拦截器不受此类更改的影响。
     */
    private synchronized void initializeAdvisorChain() throws AopConfigException, BeansException {
        if (!this.advisorChainInitialized && !ObjectUtils.isEmpty(this.interceptorNames)) {
            if (this.beanFactory == null) {
                throw new IllegalStateException("No BeanFactory available anymore (probably due to serialization) " + "- cannot resolve interceptor names " + Arrays.toString(this.interceptorNames));
            }
            // 全局变量不能作为最后一个，除非我们使用该属性指定了targetSource...
            if (this.interceptorNames[this.interceptorNames.length - 1].endsWith(GLOBAL_SUFFIX) && this.targetName == null && this.targetSource == EMPTY_TARGET_SOURCE) {
                throw new AopConfigException("Target required after globals");
            }
            // 从Bean名称中生成Materialize拦截器链。
            for (String name : this.interceptorNames) {
                if (name.endsWith(GLOBAL_SUFFIX)) {
                    if (!(this.beanFactory instanceof ListableBeanFactory lbf)) {
                        throw new AopConfigException("Can only use global advisors or interceptors with a ListableBeanFactory");
                    }
                    addGlobalAdvisors(lbf, name.substring(0, name.length() - GLOBAL_SUFFIX.length()));
                } else {
                    // 如果我们到达这里，我们需要添加一个命名拦截器。
                    // 我们必须检查它是一个单例还是原型。
                    Object advice;
                    if (this.singleton || this.beanFactory.isSingleton(name)) {
                        // 将实际的顾问/建议添加到链中。
                        advice = this.beanFactory.getBean(name);
                    } else {
                        // 这是一个原型 Advice 或 Advisor：替换为原型。
                        // 避免仅为了初始化顾问链而创建不必要的原型bean。
                        advice = new PrototypePlaceholderAdvisor(name);
                    }
                    addAdvisorOnChainCreation(advice);
                }
            }
            this.advisorChainInitialized = true;
        }
    }

    /**
     * 返回一个独立的顾问链。
     * 每次返回新的原型实例时，我们需要这样做，
     * 以返回原型顾问和咨询的独立实例。
     */
    private List<Advisor> freshAdvisorChain() {
        Advisor[] advisors = getAdvisors();
        List<Advisor> freshAdvisors = new ArrayList<>(advisors.length);
        for (Advisor advisor : advisors) {
            if (advisor instanceof PrototypePlaceholderAdvisor ppa) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Refreshing bean named '" + ppa.getBeanName() + "'");
                }
                // 将占位符替换为从getBean查找得到的全新原型实例
                if (this.beanFactory == null) {
                    throw new IllegalStateException("No BeanFactory available anymore (probably due to " + "serialization) - cannot resolve prototype advisor '" + ppa.getBeanName() + "'");
                }
                Object bean = this.beanFactory.getBean(ppa.getBeanName());
                Advisor refreshedAdvisor = namedBeanToAdvisor(bean);
                freshAdvisors.add(refreshedAdvisor);
            } else {
                // 添加共享实例。
                freshAdvisors.add(advisor);
            }
        }
        return freshAdvisors;
    }

    /**
     * 添加所有全局拦截器和切点。
     */
    private void addGlobalAdvisors(ListableBeanFactory beanFactory, String prefix) {
        String[] globalAdvisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, Advisor.class);
        String[] globalInterceptorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, Interceptor.class);
        if (globalAdvisorNames.length > 0 || globalInterceptorNames.length > 0) {
            List<Object> beans = new ArrayList<>(globalAdvisorNames.length + globalInterceptorNames.length);
            for (String name : globalAdvisorNames) {
                if (name.startsWith(prefix)) {
                    beans.add(beanFactory.getBean(name));
                }
            }
            for (String name : globalInterceptorNames) {
                if (name.startsWith(prefix)) {
                    beans.add(beanFactory.getBean(name));
                }
            }
            AnnotationAwareOrderComparator.sort(beans);
            for (Object bean : beans) {
                addAdvisorOnChainCreation(bean);
            }
        }
    }

    /**
     * 当创建通知链时被调用。
     * <p>将给定的通知、顾问或对象添加到拦截器列表中。
     * 由于存在这三种可能性，我们无法更严格地指定签名。
     * @param next 通知、顾问或目标对象
     */
    private void addAdvisorOnChainCreation(Object next) {
        // 如果需要，我们需要转换为 Advisor，以便我们的源引用
        // 与从超类拦截器中找到的内容相匹配。
        addAdvisor(namedBeanToAdvisor(next));
    }

    /**
     * 返回一个用于创建代理的TargetSource。如果在interceptorNames列表的末尾没有指定目标，则TargetSource将是此类中的TargetSource成员。否则，我们将获取目标Bean，并在需要的情况下将其包装在一个TargetSource中。
     */
    private TargetSource freshTargetSource() {
        if (this.targetName == null) {
            // 未刷新目标：在 'interceptorNames' 中未指定bean名称
            return this.targetSource;
        } else {
            if (this.beanFactory == null) {
                throw new IllegalStateException("No BeanFactory available anymore (probably due to serialization) " + "- cannot resolve target with name '" + this.targetName + "'");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Refreshing target with name '" + this.targetName + "'");
            }
            Object target = this.beanFactory.getBean(this.targetName);
            return (target instanceof TargetSource targetSource ? targetSource : new SingletonTargetSource(target));
        }
    }

    /**
     * 将从调用getBean()并在interceptorNames数组中指定名称得到的对象转换为Advisor或TargetSource。
     */
    private Advisor namedBeanToAdvisor(Object next) {
        try {
            return this.advisorAdapterRegistry.wrap(next);
        } catch (UnknownAdviceTypeException ex) {
            // 我们原本期望这是一个顾问或建议。
            // 但它并不是这样。这是一个配置错误。
            throw new AopConfigException("Unknown advisor type " + next.getClass() + "; can only include Advisor or Advice type beans in interceptorNames chain " + "except for last entry which may also be target instance or TargetSource", ex);
        }
    }

    /**
     * 在建议更改时清除并重新缓存单例。
     */
    @Override
    protected void adviceChanged() {
        super.adviceChanged();
        if (this.singleton) {
            logger.debug("Advice has changed; re-caching singleton instance");
            synchronized (this) {
                this.singletonInstance = null;
            }
        }
    }

    // 很抱歉，您提供的内容是一个横线（"---------------------------------------------------------------------"），这不是有效的 Java 代码注释内容。如果您能提供具体的 Java 代码注释，我会很乐意将其翻译成中文。
    // 序列化支持
    // 由于您只提供了代码注释的分隔符“---------------------------------------------------------------------”而没有提供实际的代码注释内容，我无法进行翻译。请提供具体的Java代码注释内容，以便我能够进行准确的翻译。
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // 依赖默认序列化；仅在反序列化后初始化状态。
        ois.defaultReadObject();
        // 初始化 transient 字段。
        this.proxyClassLoader = ClassUtils.getDefaultClassLoader();
    }

    /**
     * 用于拦截器链中，在创建代理时需要用原型实例替换一个Bean的场景。
     */
    private static class PrototypePlaceholderAdvisor implements Advisor, Serializable {

        private final String beanName;

        private final String message;

        public PrototypePlaceholderAdvisor(String beanName) {
            this.beanName = beanName;
            this.message = "Placeholder for prototype Advisor/Advice with bean name '" + beanName + "'";
        }

        public String getBeanName() {
            return this.beanName;
        }

        @Override
        public Advice getAdvice() {
            throw new UnsupportedOperationException("Cannot invoke methods: " + this.message);
        }

        @Override
        public String toString() {
            return this.message;
        }
    }
}
