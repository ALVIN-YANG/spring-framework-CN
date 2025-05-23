// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache许可证2.0版（以下简称“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.DecoratingProxy;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 基于JDK的Spring AOP框架的 {@link AopProxy} 实现，基于JDK的 {@link java.lang.reflect.Proxy} 动态代理。
 *
 * <p>创建一个动态代理，实现 AopProxy 暴露的接口。动态代理 <i>不能</i> 用于代理定义在类中的方法，而不是接口中的方法。
 *
 * <p>此类对象应通过代理工厂获取，由一个 {@link AdvisedSupport} 类进行配置。这个类是Spring AOP框架的内部类，不需要由客户端代码直接使用。
 *
 * <p>使用此类创建的代理将是线程安全的，前提是底层的（目标）类是线程安全的。
 *
 * <p>只要所有的顾问（包括建议和切入点）以及目标源都是可序列化的，代理就是可序列化的。
 *
 * @作者 Rod Johnson
 * @作者 Juergen Hoeller
 * @作者 Rob Harrop
 * @作者 Dave Syer
 * @作者 Sergey Tsypanov
 * @see java.lang.reflect.Proxy
 * @see AdvisedSupport
 * @see ProxyFactory
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

    /**
     * 使用 Spring 1.2 中的 serialVersionUID 以确保互操作性。
     */
    private static final long serialVersionUID = 5531744639992436476L;

    /** 注意：我们可以通过将“invoke”方法重构为一个模板方法来避免这个类和CGLIB代理之间的代码重复。然而，这种方法与复制粘贴的解决方案相比，至少会增加10%的性能开销，因此我们牺牲了优雅性以换取性能（我们有一个良好的测试套件来确保不同的代理表现相同 :-)))。
	 * 这样，我们还可以更轻松地利用每个类中的微小优化。*/
    /**
     * 我们使用静态Log以避免序列化问题。
     */
    private static final Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);

    /**
     * 配置此代理所使用的配置。
     */
    private final AdvisedSupport advised;

    private final Class<?>[] proxiedInterfaces;

    /**
     * 是否在代理接口上定义了 {@link #equals} 方法？
     */
    private boolean equalsDefined;

    /**
     * 是否在代理接口上定义了 {@link #hashCode} 方法？
     */
    private boolean hashCodeDefined;

    /**
     * 为给定的 AOP 配置构建一个新的 JdkDynamicAopProxy。
     * @param config AOP 配置，作为 AdvisedSupport 对象
     * @throws AopConfigException 如果配置无效。在这种情况下，我们尝试抛出一个信息丰富的异常，而不是让后续发生神秘的失败。
     */
    public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
        Assert.notNull(config, "AdvisedSupport must not be null");
        this.advised = config;
        this.proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised, true);
        findDefinedEqualsAndHashCodeMethods(this.proxiedInterfaces);
    }

    @Override
    public Object getProxy() {
        return getProxy(ClassUtils.getDefaultClassLoader());
    }

    @Override
    public Object getProxy(@Nullable ClassLoader classLoader) {
        if (logger.isTraceEnabled()) {
            logger.trace("Creating JDK dynamic proxy: " + this.advised.getTargetSource());
        }
        return Proxy.newProxyInstance(determineClassLoader(classLoader), this.proxiedInterfaces, this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Class<?> getProxyClass(@Nullable ClassLoader classLoader) {
        return Proxy.getProxyClass(determineClassLoader(classLoader), this.proxiedInterfaces);
    }

    /**
     * 判断是否建议使用 JDK 引导类加载器或平台类加载器 ->
     * 使用更高层次的类加载器，该类加载器可以查看 Spring 基础设施类。
     */
    private ClassLoader determineClassLoader(@Nullable ClassLoader classLoader) {
        if (classLoader == null) {
            // JDK引导加载器 -> 使用 spring-aop 类加载器代替。
            return getClass().getClassLoader();
        }
        if (classLoader.getParent() == null) {
            // 可能是在 JDK 9 及以上版本的 JDK 平台加载器上
            ClassLoader aopClassLoader = getClass().getClassLoader();
            ClassLoader aopParent = aopClassLoader.getParent();
            while (aopParent != null) {
                if (classLoader == aopParent) {
                    // 建议的类加载器是 spring-aop 类加载器的父类
                    // -> 使用 spring-aop 自身的 ClassLoader。
                    return aopClassLoader;
                }
                aopParent = aopParent.getParent();
            }
        }
        // 常规情况：直接使用建议的 ClassLoader。
        return classLoader;
    }

    /**
     * 查找在提供的接口集合上可能定义的任何 {@link #equals} 或 {@link #hashCode} 方法。
     * @param proxiedInterfaces 要反射的接口
     */
    private void findDefinedEqualsAndHashCodeMethods(Class<?>[] proxiedInterfaces) {
        for (Class<?> proxiedInterface : proxiedInterfaces) {
            Method[] methods = proxiedInterface.getDeclaredMethods();
            for (Method method : methods) {
                if (AopUtils.isEqualsMethod(method)) {
                    this.equalsDefined = true;
                }
                if (AopUtils.isHashCodeMethod(method)) {
                    this.hashCodeDefined = true;
                }
                if (this.equalsDefined && this.hashCodeDefined) {
                    return;
                }
            }
        }
    }

    /**
     * 实现 {@code InvocationHandler.invoke}。
     * <p>调用者将看到目标抛出的确切异常，
     * 除非一个钩子方法抛出了异常。
     */
    @Override
    @Nullable
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object oldProxy = null;
        boolean setProxyContext = false;
        TargetSource targetSource = this.advised.targetSource;
        Object target = null;
        try {
            if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
                // 目标对象本身没有实现 equals(Object) 方法。
                return equals(args[0]);
            } else if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
                // 目标对象没有自己实现 hashCode() 方法。
                return hashCode();
            } else if (method.getDeclaringClass() == DecoratingProxy.class) {
                // 只声明了getDecoratedClass()方法 -> 分派到代理配置。
                return AopProxyUtils.ultimateTargetClass(this.advised);
            } else if (!this.advised.opaque && method.getDeclaringClass().isInterface() && method.getDeclaringClass().isAssignableFrom(Advised.class)) {
                // 在 ProxyConfig 上使用代理配置进行服务调用...
                return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
            }
            Object retVal;
            if (this.advised.exposeProxy) {
                // 在必要时使调用可用。
                oldProxy = AopContext.setCurrentProxy(proxy);
                setProxyContext = true;
            }
            // 尽可能晚地获取，以最小化我们“拥有”目标的时间。
            // 如果它来自一个池（pool）。
            target = targetSource.getTarget();
            Class<?> targetClass = (target != null ? target.getClass() : null);
            // 获取此方法的拦截链。
            List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
            // 检查我们是否有任何建议。如果没有，我们可以退回到直接
            // 反射调用目标，并避免创建一个 MethodInvocation 对象。
            if (chain.isEmpty()) {
                // 我们可以跳过创建一个 MethodInvocation：直接调用目标即可
                // 请注意，最终的调用者必须是 InvokerInterceptor，这样我们才能确信它确实
                // 这只是对目标对象的反射操作，没有热交换或复杂的代理。
                Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
                retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
            } else {
                // 我们需要创建一个方法调用...
                MethodInvocation invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
                // 通过拦截器链进入切入点。
                retVal = invocation.proceed();
            }
            // 如有必要，请调整按摩返回值。
            Class<?> returnType = method.getReturnType();
            if (retVal != null && retVal == target && returnType != Object.class && returnType.isInstance(proxy) && !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
                // 特殊情况：它返回了"this"并且方法的返回类型是
                // 请注意，我们无法帮助如果目标集是类型兼容的。
                // 一个指向另一个返回对象的自身引用。
                retVal = proxy;
            } else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
                throw new AopInvocationException("Null return value from advice does not match primitive return type for: " + method);
            }
            return retVal;
        } finally {
            if (target != null && !targetSource.isStatic()) {
                // 必须来自TargetSource。
                targetSource.releaseTarget(target);
            }
            if (setProxyContext) {
                // 恢复旧代理。
                AopContext.setCurrentProxy(oldProxy);
            }
        }
    }

    /**
     * 等价性意味着接口、顾问和目标源是相等的。
     * <p>被比较的对象可能是 JdkDynamicAopProxy 的实例本身
     * 或者是包装了一个 JdkDynamicAopProxy 实例的动态代理。
     */
    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        JdkDynamicAopProxy otherProxy;
        if (other instanceof JdkDynamicAopProxy jdkDynamicAopProxy) {
            otherProxy = jdkDynamicAopProxy;
        } else if (Proxy.isProxyClass(other.getClass())) {
            InvocationHandler ih = Proxy.getInvocationHandler(other);
            if (!(ih instanceof JdkDynamicAopProxy jdkDynamicAopProxy)) {
                return false;
            }
            otherProxy = jdkDynamicAopProxy;
        } else {
            // 不是一个有效的比较...
            return false;
        }
        // 如果我们到达这里，otherProxy 是另一个 AopProxy。
        return AopProxyUtils.equalsInProxy(this.advised, otherProxy.advised);
    }

    /**
     * 代理使用目标源（TargetSource）的哈希码。
     */
    @Override
    public int hashCode() {
        return JdkDynamicAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
    }
}
