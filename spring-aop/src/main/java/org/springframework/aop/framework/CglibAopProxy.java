// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0 ("许可证")授权；
除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性和非侵权性。
有关许可证的具体语言和限制，请参阅许可证。*/
package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;
import org.springframework.cglib.core.CodeGenerationException;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Dispatcher;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.core.KotlinDetector;
import org.springframework.core.SmartClassLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 基于CGLIB的Spring AOP框架的AopProxy实现。
 *
 * <p>此类对象应通过代理工厂获取，由一个 {@link AdvisedSupport} 对象配置。此类是Spring AOP框架的内部类，不需要客户端代码直接使用。
 *
 * <p>当需要时，例如代理一个目标类时，{@link DefaultAopProxyFactory} 将自动创建基于CGLIB的代理（有关详细信息，请参阅 {@link DefaultAopProxyFactory} 的辅助javadoc）。
 *
 * <p>使用此类创建的代理如果底层（目标）类是线程安全的，则线程安全。
 *
 * @作者 Rod Johnson
 * @作者 Rob Harrop
 * @作者 Juergen Hoeller
 * @作者 Ramnivas Laddad
 * @作者 Chris Beams
 * @作者 Dave Syer
 * @see org.springframework.cglib.proxy.Enhancer
 * @see AdvisedSupport#setProxyTargetClass
 * @see DefaultAopProxyFactory
 */
@SuppressWarnings("serial")
class CglibAopProxy implements AopProxy, Serializable {

    // CGLIB 回调数组索引的常量
    private static final int AOP_PROXY = 0;

    private static final int INVOKE_TARGET = 1;

    private static final int NO_OVERRIDE = 2;

    private static final int DISPATCH_TARGET = 3;

    private static final int DISPATCH_ADVISED = 4;

    private static final int INVOKE_EQUALS = 5;

    private static final int INVOKE_HASHCODE = 6;

    /**
     * 为子类提供 Logger；静态以优化序列化。
     */
    protected static final Log logger = LogFactory.getLog(CglibAopProxy.class);

    /**
     * 跟踪我们已验证的用于最终方法的类。
     */
    private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<>();

    /**
     * 用于配置此代理的配置。
     */
    protected final AdvisedSupport advised;

    @Nullable
    protected Object[] constructorArgs;

    @Nullable
    protected Class<?>[] constructorArgTypes;

    /**
     * 用于Advised上方法的分发器。
     */
    private final transient AdvisedDispatcher advisedDispatcher;

    private transient Map<Method, Integer> fixedInterceptorMap = Collections.emptyMap();

    private transient int fixedInterceptorOffset;

    /**
     * 为给定的 AOP 配置创建一个新的 CglibAopProxy。
     * @param config AOP 配置，以 AdvisedSupport 对象形式提供
     * @throws AopConfigException 如果配置无效。在这种情况下，我们尝试抛出一个信息丰富的异常，而不是让后续发生神秘的失败。
     */
    public CglibAopProxy(AdvisedSupport config) throws AopConfigException {
        Assert.notNull(config, "AdvisedSupport must not be null");
        this.advised = config;
        this.advisedDispatcher = new AdvisedDispatcher(this.advised);
    }

    /**
     * 设置用于创建代理的构造函数参数。
     * @param constructorArgs 构造函数参数值
     * @param constructorArgTypes 构造函数参数类型
     */
    public void setConstructorArguments(@Nullable Object[] constructorArgs, @Nullable Class<?>[] constructorArgTypes) {
        if (constructorArgs == null || constructorArgTypes == null) {
            throw new IllegalArgumentException("Both 'constructorArgs' and 'constructorArgTypes' need to be specified");
        }
        if (constructorArgs.length != constructorArgTypes.length) {
            throw new IllegalArgumentException("Number of 'constructorArgs' (" + constructorArgs.length + ") must match number of 'constructorArgTypes' (" + constructorArgTypes.length + ")");
        }
        this.constructorArgs = constructorArgs;
        this.constructorArgTypes = constructorArgTypes;
    }

    @Override
    public Object getProxy() {
        return buildProxy(null, false);
    }

    @Override
    public Object getProxy(@Nullable ClassLoader classLoader) {
        return buildProxy(classLoader, false);
    }

    @Override
    public Class<?> getProxyClass(@Nullable ClassLoader classLoader) {
        return (Class<?>) buildProxy(classLoader, true);
    }

    private Object buildProxy(@Nullable ClassLoader classLoader, boolean classOnly) {
        if (logger.isTraceEnabled()) {
            logger.trace("Creating CGLIB proxy: " + this.advised.getTargetSource());
        }
        try {
            Class<?> rootClass = this.advised.getTargetClass();
            Assert.state(rootClass != null, "Target class must be available for creating a CGLIB proxy");
            Class<?> proxySuperClass = rootClass;
            if (rootClass.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
                proxySuperClass = rootClass.getSuperclass();
                Class<?>[] additionalInterfaces = rootClass.getInterfaces();
                for (Class<?> additionalInterface : additionalInterfaces) {
                    this.advised.addInterface(additionalInterface);
                }
            }
            // 验证类，根据需要写入日志消息。
            validateClassIfNecessary(proxySuperClass, classLoader);
            // 配置 CGLIB Enhancer...
            Enhancer enhancer = createEnhancer();
            if (classLoader != null) {
                enhancer.setClassLoader(classLoader);
                if (classLoader instanceof SmartClassLoader smartClassLoader && smartClassLoader.isClassReloadable(proxySuperClass)) {
                    enhancer.setUseCache(false);
                }
            }
            enhancer.setSuperclass(proxySuperClass);
            enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));
            enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
            enhancer.setAttemptLoad(true);
            enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(classLoader));
            Callback[] callbacks = getCallbacks(rootClass);
            Class<?>[] types = new Class<?>[callbacks.length];
            for (int x = 0; x < types.length; x++) {
                types[x] = callbacks[x].getClass();
            }
            // fixedInterceptorMap 只在上述 getCallbacks 调用之后，在此点填充
            ProxyCallbackFilter filter = new ProxyCallbackFilter(this.advised.getConfigurationOnlyCopy(), this.fixedInterceptorMap, this.fixedInterceptorOffset);
            enhancer.setCallbackFilter(filter);
            enhancer.setCallbackTypes(types);
            // 生成代理类并创建代理实例。
            // ProxyCallbackFilter 具有方法反射能力，并可通过 Advisor 访问。
            try {
                return (classOnly ? createProxyClass(enhancer) : createProxyClassAndInstance(enhancer, callbacks));
            } finally {
                // 将 ProxyCallbackFilter 缩减为仅键的状态，以发挥其类缓存作用
                // 在 CGLIB$CALLBACK_FILTER 字段中，没有泄露任何 Advisor 状态...
                filter.advised.reduceToAdvisorKey();
            }
        } catch (CodeGenerationException | IllegalArgumentException ex) {
            throw new AopConfigException("Could not generate CGLIB subclass of " + this.advised.getTargetClass() + ": Common causes of this problem include using a final class or a non-visible class", ex);
        } catch (Throwable ex) {
            // 获取目标源 (TargetSource.getTarget()) 失败
            throw new AopConfigException("Unexpected AOP exception", ex);
        }
    }

    protected Class<?> createProxyClass(Enhancer enhancer) {
        enhancer.setInterceptDuringConstruction(false);
        return enhancer.createClass();
    }

    protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
        enhancer.setInterceptDuringConstruction(false);
        enhancer.setCallbacks(callbacks);
        return (this.constructorArgs != null && this.constructorArgTypes != null ? enhancer.create(this.constructorArgTypes, this.constructorArgs) : enhancer.create());
    }

    /**
     * 创建CGLIB的Enhancer。子类可能希望覆盖此方法以返回自定义的Enhancer实现。
     */
    protected Enhancer createEnhancer() {
        return new Enhancer();
    }

    /**
     * 检查提供的 {@code Class} 是否已经被验证，如果没有则进行验证。
     */
    private void validateClassIfNecessary(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader) {
        if (!this.advised.isOptimize() && logger.isInfoEnabled()) {
            synchronized (validatedClasses) {
                validatedClasses.computeIfAbsent(proxySuperClass, clazz -> {
                    doValidateClass(clazz, proxyClassLoader, ClassUtils.getAllInterfacesForClassAsSet(clazz));
                    return Boolean.TRUE;
                });
            }
        }
    }

    /**
     * 检查给定类（Class）中的final方法，以及跨类加载器（ClassLoader）的包可见方法，并为每个找到的方法记录警告到日志中。
     */
    private void doValidateClass(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader, Set<Class<?>> ifcs) {
        if (proxySuperClass != Object.class) {
            Method[] methods = proxySuperClass.getDeclaredMethods();
            for (Method method : methods) {
                int mod = method.getModifiers();
                if (!Modifier.isStatic(mod) && !Modifier.isPrivate(mod)) {
                    if (Modifier.isFinal(mod)) {
                        if (logger.isInfoEnabled() && implementsInterface(method, ifcs)) {
                            logger.info("Unable to proxy interface-implementing method [" + method + "] because " + "it is marked as final: Consider using interface-based JDK proxies instead!");
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("Final method [" + method + "] cannot get proxied via CGLIB: " + "Calls to this method will NOT be routed to the target instance and " + "might lead to NPEs against uninitialized fields in the proxy instance.");
                        }
                    } else if (logger.isDebugEnabled() && !Modifier.isPublic(mod) && !Modifier.isProtected(mod) && proxyClassLoader != null && proxySuperClass.getClassLoader() != proxyClassLoader) {
                        logger.debug("Method [" + method + "] is package-visible across different ClassLoaders " + "and cannot get proxied via CGLIB: Declare this method as public or protected " + "if you need to support invocations through the proxy.");
                    }
                }
            }
            doValidateClass(proxySuperClass.getSuperclass(), proxyClassLoader, ifcs);
        }
    }

    private Callback[] getCallbacks(Class<?> rootClass) throws Exception {
        // 用于优化选择的参数...
        boolean isStatic = this.advised.getTargetSource().isStatic();
        boolean isFrozen = this.advised.isFrozen();
        boolean exposeProxy = this.advised.isExposeProxy();
        // 选择一个 "aop" 拦截器（用于 AOP 调用）。
        Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);
        // 选择一个“直接跳转到目标”拦截器。（用于调用那些
        // 不建议这样做，但可以返回这个对象（可能需要返回以暴露代理）。可能需要公开代理。
        Callback targetInterceptor;
        if (exposeProxy) {
            targetInterceptor = (isStatic ? new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) : new DynamicUnadvisedExposedInterceptor(this.advised.getTargetSource()));
        } else {
            targetInterceptor = (isStatic ? new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) : new DynamicUnadvisedInterceptor(this.advised.getTargetSource()));
        }
        // 选择一个“直接到目标”的调度器（用于
        // 不建议调用无法返回 `this` 的静态目标。
        Callback targetDispatcher = (isStatic ? new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp());
        Callback[] mainCallbacks = new Callback[] { // for常规建议
        aopInterceptor, // 如果优化，则忽略建议直接调用目标
        targetInterceptor, // 未为映射到此的该方法提供重写
        new SerializableNoOp(), targetDispatcher, this.advisedDispatcher, new EqualsInterceptor(this.advised), new HashCodeInterceptor(this.advised) };
        Callback[] callbacks;
        // 如果目标是静态的，并且建议链已冻结，
        // 然后我们可以通过发送 AOP 调用来进行一些优化
        // 直接使用该方法的固定链路到达目标。
        if (isStatic && isFrozen) {
            Method[] methods = rootClass.getMethods();
            Callback[] fixedCallbacks = new Callback[methods.length];
            this.fixedInterceptorMap = CollectionUtils.newHashMap(methods.length);
            // TODO：此处进行小范围内存优化（对于没有建议的方法可以跳过创建）
            for (int x = 0; x < methods.length; x++) {
                Method method = methods[x];
                List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, rootClass);
                fixedCallbacks[x] = new FixedChainStaticTargetInterceptor(chain, this.advised.getTargetSource().getTarget(), this.advised.getTargetClass());
                this.fixedInterceptorMap.put(method, x);
            }
            // 现在复制 mainCallbacks 中的两个回调
            // 将 fixedCallbacks 添加到 callbacks 数组中。
            callbacks = new Callback[mainCallbacks.length + fixedCallbacks.length];
            System.arraycopy(mainCallbacks, 0, callbacks, 0, mainCallbacks.length);
            System.arraycopy(fixedCallbacks, 0, callbacks, mainCallbacks.length, fixedCallbacks.length);
            this.fixedInterceptorOffset = mainCallbacks.length;
        } else {
            callbacks = mainCallbacks;
        }
        return callbacks;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof CglibAopProxy that && AopProxyUtils.equalsInProxy(this.advised, that.advised)));
    }

    @Override
    public int hashCode() {
        return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
    }

    /**
     * 检查给定的方法是否在给定的任何接口中声明。
     */
    private static boolean implementsInterface(Method method, Set<Class<?>> ifcs) {
        for (Class<?> ifc : ifcs) {
            if (ClassUtils.hasMethod(ifc, method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理返回值。如果需要，将返回的{@code this}包装成代理，并验证返回值不是原始类型的null。
     */
    @Nullable
    private static Object processReturnType(Object proxy, @Nullable Object target, Method method, @Nullable Object returnValue) {
        // 如有必要，则返回按摩值
        if (returnValue != null && returnValue == target && !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
            // 特殊情况：它返回了"this"。请注意，我们无法帮助
            // 如果目标对象在另一个返回的对象中设置了对自身的引用。
            returnValue = proxy;
        }
        Class<?> returnType = method.getReturnType();
        if (returnValue == null && returnType != Void.TYPE && returnType.isPrimitive()) {
            throw new AopInvocationException("Null return value from advice does not match primitive return type for: " + method);
        }
        return returnValue;
    }

    /**
     * 用于CGLIB的NoOp接口的可序列化替代方案。
     * 公共的，以允许在框架的其它地方使用。
     */
    public static class SerializableNoOp implements NoOp, Serializable {
    }

    /**
     * 用于静态目标且没有建议链的方法拦截器。调用直接传递回目标。当代理需要被暴露且无法确定该方法不会返回{@code this}时使用。
     */
    private static class StaticUnadvisedInterceptor implements MethodInterceptor, Serializable {

        @Nullable
        private final Object target;

        public StaticUnadvisedInterceptor(@Nullable Object target) {
            this.target = target;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object retVal = AopUtils.invokeJoinpointUsingReflection(this.target, method, args);
            return processReturnType(proxy, this.target, method, retVal);
        }
    }

    /**
     * 用于静态目标且没有通知链的方法拦截器，当代理需要被暴露时使用。
     */
    private static class StaticUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

        @Nullable
        private final Object target;

        public StaticUnadvisedExposedInterceptor(@Nullable Object target) {
            this.target = target;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object oldProxy = null;
            try {
                oldProxy = AopContext.setCurrentProxy(proxy);
                Object retVal = AopUtils.invokeJoinpointUsingReflection(this.target, method, args);
                return processReturnType(proxy, this.target, method, retVal);
            } finally {
                AopContext.setCurrentProxy(oldProxy);
            }
        }
    }

    /**
     * 用于调用动态目标而不创建方法调用或评估通知链的拦截器。（我们知道此方法没有通知。）
     */
    private static class DynamicUnadvisedInterceptor implements MethodInterceptor, Serializable {

        private final TargetSource targetSource;

        public DynamicUnadvisedInterceptor(TargetSource targetSource) {
            this.targetSource = targetSource;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object target = this.targetSource.getTarget();
            try {
                Object retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
                return processReturnType(proxy, target, method, retVal);
            } finally {
                if (target != null) {
                    this.targetSource.releaseTarget(target);
                }
            }
        }
    }

    /**
     * 用于需要暴露代理时对未经建议的动态目标进行拦截的拦截器。
     */
    private static class DynamicUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

        private final TargetSource targetSource;

        public DynamicUnadvisedExposedInterceptor(TargetSource targetSource) {
            this.targetSource = targetSource;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object oldProxy = null;
            Object target = this.targetSource.getTarget();
            try {
                oldProxy = AopContext.setCurrentProxy(proxy);
                Object retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
                return processReturnType(proxy, target, method, retVal);
            } finally {
                AopContext.setCurrentProxy(oldProxy);
                if (target != null) {
                    this.targetSource.releaseTarget(target);
                }
            }
        }
    }

    /**
     * /**
     *  用于静态目标的调度器。调度器比拦截器运行得更快。当确定一个方法绝对不返回"this"时，将使用此调度器。
     */
    private static class StaticDispatcher implements Dispatcher, Serializable {

        @Nullable
        private final Object target;

        public StaticDispatcher(@Nullable Object target) {
            this.target = target;
        }

        @Override
        @Nullable
        public Object loadObject() {
            return this.target;
        }
    }

    /**
     * 该类是用于处理在Adviced类中声明的任何方法的分发器。
     */
    private static class AdvisedDispatcher implements Dispatcher, Serializable {

        private final AdvisedSupport advised;

        public AdvisedDispatcher(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object loadObject() {
            return this.advised;
        }
    }

    /**
     * 用于 `equals` 方法的调度器。
     * 确保方法调用始终由此类处理。
     */
    private static class EqualsInterceptor implements MethodInterceptor, Serializable {

        private final AdvisedSupport advised;

        public EqualsInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
            Object other = args[0];
            if (proxy == other) {
                return true;
            }
            if (other instanceof Factory factory) {
                Callback callback = factory.getCallback(INVOKE_EQUALS);
                return (callback instanceof EqualsInterceptor that && AopProxyUtils.equalsInProxy(this.advised, that.advised));
            }
            return false;
        }
    }

    /**
     * 用于 {@code hashCode} 方法的调度器。
     * 确保方法调用始终由此类处理。
     */
    private static class HashCodeInterceptor implements MethodInterceptor, Serializable {

        private final AdvisedSupport advised;

        public HashCodeInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
            return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
        }
    }

    /**
     * 用于特定于冻结、静态代理上建议方法的拦截器。
     */
    private static class FixedChainStaticTargetInterceptor implements MethodInterceptor, Serializable {

        private final List<Object> adviceChain;

        @Nullable
        private final Object target;

        @Nullable
        private final Class<?> targetClass;

        public FixedChainStaticTargetInterceptor(List<Object> adviceChain, @Nullable Object target, @Nullable Class<?> targetClass) {
            this.adviceChain = adviceChain;
            this.target = target;
            this.targetClass = targetClass;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            MethodInvocation invocation = new CglibMethodInvocation(proxy, this.target, method, args, this.targetClass, this.adviceChain, methodProxy);
            // 如果我们到达这里，我们需要创建一个 MethodInvocation。
            Object retVal = invocation.proceed();
            retVal = processReturnType(proxy, this.target, method, retVal);
            return retVal;
        }
    }

    /**
     * 通用目的的AOP回调。当目标对象是动态的或代理对象未冻结时使用。
     */
    private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

        private final AdvisedSupport advised;

        public DynamicAdvisedInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object oldProxy = null;
            boolean setProxyContext = false;
            Object target = null;
            TargetSource targetSource = this.advised.getTargetSource();
            try {
                if (this.advised.exposeProxy) {
                    // 根据需要使调用可用。
                    oldProxy = AopContext.setCurrentProxy(proxy);
                    setProxyContext = true;
                }
                // 尽可能晚地获取，以最小化我们“拥有”目标的时间，以防它来自一个池子...
                target = targetSource.getTarget();
                Class<?> targetClass = (target != null ? target.getClass() : null);
                List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
                Object retVal;
                // 检查我们是否只有一个 InvokerInterceptor：即，
                // 没有实际的建议，只是对目标对象的反射调用。
                if (chain.isEmpty()) {
                    // 我们可以跳过创建一个 MethodInvocation：直接调用目标即可。
                    // 请注意，最终的调用者必须是 InvokerInterceptor，因此我们知道
                    // 它只是对目标进行了一次反射操作，并没有进行热操作。
                    // 交换或花哨的代理。
                    Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
                    retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
                } else {
                    // 我们需要创建一个方法调用...
                    retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed();
                }
                return processReturnType(proxy, target, method, retVal);
            } finally {
                if (target != null && !targetSource.isStatic()) {
                    targetSource.releaseTarget(target);
                }
                if (setProxyContext) {
                    // 恢复旧代理。
                    AopContext.setCurrentProxy(oldProxy);
                }
            }
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof DynamicAdvisedInterceptor dynamicAdvisedInterceptor && this.advised.equals(dynamicAdvisedInterceptor.advised)));
        }

        /**
         * CGLIB 使用此方法来驱动代理的创建。
         */
        @Override
        public int hashCode() {
            return this.advised.hashCode();
        }
    }

    /**
     * 此 AOP 代理使用的 AOP Alliance MethodInvocation 的实现。
     */
    private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

        public CglibMethodInvocation(Object proxy, @Nullable Object target, Method method, Object[] arguments, @Nullable Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers, MethodProxy methodProxy) {
            super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
        }

        @Override
        @Nullable
        public Object proceed() throws Throwable {
            try {
                return super.proceed();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                if (ReflectionUtils.declaresException(getMethod(), ex.getClass()) || KotlinDetector.isKotlinType(getMethod().getDeclaringClass())) {
                    // 如果目标方法上声明了原始异常，则传播该异常
                    // （调用者预期如此）。对于 Kotlin 代码，始终传播它
                    // 因为检查型异常不需要显式声明。
                    throw ex;
                } else {
                    // 在拦截器中抛出了受检异常，但未在
                    // 目标方法签名 -> 应用一个未声明的异常（UndeclaredThrowableException）。
                    // 与标准 JDK 动态代理行为保持一致。
                    throw new UndeclaredThrowableException(ex);
                }
            }
        }
    }

    /**
     * 回调过滤器，用于将回调分配给方法。
     */
    private static class ProxyCallbackFilter implements CallbackFilter {

        final AdvisedSupport advised;

        private final Map<Method, Integer> fixedInterceptorMap;

        private final int fixedInterceptorOffset;

        public ProxyCallbackFilter(AdvisedSupport advised, Map<Method, Integer> fixedInterceptorMap, int fixedInterceptorOffset) {
            this.advised = advised;
            this.fixedInterceptorMap = fixedInterceptorMap;
            this.fixedInterceptorOffset = fixedInterceptorOffset;
        }

        /**
         * 实现 CallbackFilter.accept() 方法，以返回所需的回调索引。
         * <p>每个代理的回调由一组用于通用目的的固定回调和一组特定于方法且用于静态目标固定建议链的回调组成。
         * <p>使用的回调确定如下：
         * <dl>
         * <dt>对于公开的代理</dt>
         * <dd>公开代理需要在方法/链调用前后执行代码。这意味着我们必须使用 DynamicAdvisedInterceptor，因为所有其他拦截器都可以避免需要 try/catch 块。</dd>
         * <dt>对于 Object.finalize()：</dt>
         * <dd>不使用此方法的任何覆盖。</dd>
         * <dt>对于 equals()：</dt>
         * <dd>使用 EqualsInterceptor 将 equals() 调用重定向到针对此代理的特殊处理程序。</dd>
         * <dt>对于 Advised 类上的方法：</dt>
         * <dd>使用 AdvisedDispatcher 直接将调用调度到目标。</dd>
         * <dt>对于建议方法：</dt>
         * <dd>如果目标是静态的并且建议链已冻结，则使用特定于方法的 FixedChainStaticTargetInterceptor 来调用建议链。否则，使用 DynamicAdvisedInterceptor。</dd>
         * <dt>对于非建议方法：</dt>
         * <dd>如果可以确定该方法不会返回 {@code this} 或者当 ProxyFactory.getExposeProxy() 返回 {@code false} 时，则使用 Dispatcher。对于静态目标，使用 StaticDispatcher；对于动态目标，使用 DynamicUnadvisedInterceptor。如果方法可能返回 {@code this}，则对于静态目标使用 StaticUnadvisedInterceptor - DynamicUnadvisedInterceptor 已经考虑了这一点。</dd>
         * </dl>
         */
        @Override
        public int accept(Method method) {
            if (AopUtils.isFinalizeMethod(method)) {
                logger.trace("Found finalize() method - using NO_OVERRIDE");
                return NO_OVERRIDE;
            }
            if (!this.advised.isOpaque() && method.getDeclaringClass().isInterface() && method.getDeclaringClass().isAssignableFrom(Advised.class)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Method is declared on Advised interface: " + method);
                }
                return DISPATCH_ADVISED;
            }
            // 我们必须始终代理equals方法，以便直接调用此方法。
            if (AopUtils.isEqualsMethod(method)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Found 'equals' method: " + method);
                }
                return INVOKE_EQUALS;
            }
            // 我们必须始终根据代理对象来计算hashCode。
            if (AopUtils.isHashCodeMethod(method)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Found 'hashCode' method: " + method);
                }
                return INVOKE_HASHCODE;
            }
            Class<?> targetClass = this.advised.getTargetClass();
            // 代理尚未可用，但这不应影响任何事情。
            List<?> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
            boolean haveAdvice = !chain.isEmpty();
            boolean isStatic = this.advised.getTargetSource().isStatic();
            boolean isFrozen = this.advised.isFrozen();
            boolean exposeProxy = this.advised.isExposeProxy();
            if (haveAdvice || !isFrozen) {
                // 如果需要公开代理，则必须使用 AOP_PROXY。
                if (exposeProxy) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Must expose proxy on advised method: " + method);
                    }
                    return AOP_PROXY;
                }
                // 检查我们是否已经固定了用于处理此方法的拦截器。
                // 否则使用 AOP_PROXY。
                if (isStatic && isFrozen && this.fixedInterceptorMap.containsKey(method)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Method has advice and optimizations are enabled: " + method);
                    }
                    // 我们知道我们正在进行优化，因此可以使用FixedStaticChainInterceptors。
                    int index = this.fixedInterceptorMap.get(method);
                    return (index + this.fixedInterceptorOffset);
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Unable to apply any optimizations to advised method: " + method);
                    }
                    return AOP_PROXY;
                }
            } else {
                // 检查该方法返回类型是否位于目标类型的类层次结构之外。
                // 如果这样的话，我们就知道它永远不需要有返回类型 `massage`，可以使用一个分发器。
                // 如果代理正在被暴露，那么必须使用正确的拦截器，它已经被指定了。
                // 配置完成。如果目标是非静态的，那么我们不能使用分发器（dispatcher）因为……
                // 在调用之后，需要显式释放 target。
                if (exposeProxy || !isStatic) {
                    return INVOKE_TARGET;
                }
                Class<?> returnType = method.getReturnType();
                if (targetClass != null && returnType.isAssignableFrom(targetClass)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Method return type is assignable from target type and " + "may therefore return 'this' - using INVOKE_TARGET: " + method);
                    }
                    return INVOKE_TARGET;
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Method return type ensures 'this' cannot be returned - " + "using DISPATCH_TARGET: " + method);
                    }
                    return DISPATCH_TARGET;
                }
            }
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof ProxyCallbackFilter that && this.advised.getAdvisorKey().equals(that.advised.getAdvisorKey()) && AopProxyUtils.equalsProxiedInterfaces(this.advised, that.advised) && ObjectUtils.nullSafeEquals(this.advised.getTargetClass(), that.advised.getTargetClass()) && this.advised.getTargetSource().isStatic() == that.advised.getTargetSource().isStatic() && this.advised.isFrozen() == that.advised.isFrozen() && this.advised.isExposeProxy() == that.advised.isExposeProxy() && this.advised.isOpaque() == that.advised.isOpaque()));
        }

        @Override
        public int hashCode() {
            return this.advised.getAdvisorKey().hashCode();
        }
    }
}
