// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途的适用性或不侵犯第三方权利。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.core.DecoratingProxy;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * AOP代理工厂的实用方法。
 *
 * <p>主要用于AOP框架内部的内部使用。
 *
 * <p>有关不依赖于AOP框架内部实现的通用AOP实用方法的集合，请参阅{@link org.springframework.aop.support.AopUtils}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see org.springframework.aop.support.AopUtils
 */
public abstract class AopProxyUtils {

    /**
     * 获取给定代理背后的单例目标对象（如果存在）。
     * @param candidate 要检查的（潜在的）代理
     * @return 在一个 {@link SingletonTargetSource} 中管理的单例目标对象，
     * 或在其他任何情况下返回 {@code null}（不是代理，也不是现有的单例目标）
     * @since 4.3.8
     * @see Advised#getTargetSource()
     * @see SingletonTargetSource#getTarget()
     */
    @Nullable
    public static Object getSingletonTarget(Object candidate) {
        if (candidate instanceof Advised advised) {
            TargetSource targetSource = advised.getTargetSource();
            if (targetSource instanceof SingletonTargetSource singleTargetSource) {
                return singleTargetSource.getTarget();
            }
        }
        return null;
    }

    /**
     * 确定给定bean实例的最终目标类，不仅要遍历顶级代理，还要遍历任意数量的嵌套代理，只要没有副作用，即仅针对单例目标。
     * @param candidate 要检查的实例（可能是一个AOP代理）
     * @return 最终的目标类（或者在给定对象的普通类作为后备；永远不会为null）
     * @see org.springframework.aop.TargetClassAware#getTargetClass()
     * @see Advised#getTargetSource()
     */
    public static Class<?> ultimateTargetClass(Object candidate) {
        Assert.notNull(candidate, "Candidate object must not be null");
        Object current = candidate;
        Class<?> result = null;
        while (current instanceof TargetClassAware targetClassAware) {
            result = targetClassAware.getTargetClass();
            current = getSingletonTarget(current);
        }
        if (result == null) {
            result = (AopUtils.isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
        }
        return result;
    }

    /**
     * 完成在由 Spring AOP 生成的 JDK 动态代理中通常所需的接口集合。
     * <p>具体来说，将添加 {@link SpringProxy}、{@link Advised} 和 {@link DecoratingProxy} 到用户指定的接口集合中。
     * <p>此方法在注册 Spring 的 AOT 支持的代理提示（proxy hints）时非常有用，如下例所示，该例通过静态导入使用此方法。
     * <pre class="code">
     * RuntimeHints hints = ...
     * hints.proxies().registerJdkProxy(completeJdkProxyInterfaces(MyInterface.class));
     * </pre>
     * @param userInterfaces 要代理的组件实现的用户指定的接口集合
     * @return 代理应实现的完整接口集合
     * @throws IllegalArgumentException 如果提供的 {@code Class} 为空，不是一个接口，或者是一个
     * {@linkplain Class#isSealed() 封闭的接口}
     * @since 6.0
     * @see SpringProxy
     * @see Advised
     * @see DecoratingProxy
     * @see org.springframework.aot.hint.RuntimeHints#proxies()
     * @see org.springframework.aot.hint.ProxyHints#registerJdkProxy(Class...)
     */
    public static Class<?>[] completeJdkProxyInterfaces(Class<?>... userInterfaces) {
        List<Class<?>> completedInterfaces = new ArrayList<>(userInterfaces.length + 3);
        for (Class<?> ifc : userInterfaces) {
            Assert.notNull(ifc, "'userInterfaces' must not contain null values");
            Assert.isTrue(ifc.isInterface() && !ifc.isSealed(), () -> ifc.getName() + " must be a non-sealed interface");
            completedInterfaces.add(ifc);
        }
        completedInterfaces.add(SpringProxy.class);
        completedInterfaces.add(Advised.class);
        completedInterfaces.add(DecoratingProxy.class);
        return completedInterfaces.toArray(Class<?>[]::new);
    }

    /**
     * 确定针对给定 AOP 配置需要代理的完整接口集。
     * <p>这始终会添加 {@link Advised} 接口，除非 AdvisedSupport 的 {@link AdvisedSupport#setOpaque "opaque"} 标志被启用。始终添加 {@link org.springframework.aop.SpringProxy} 标记接口。
     * @param advised 代理配置
     * @return 要代理的完整接口集
     * @see SpringProxy
     * @see Advised
     */
    public static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised) {
        return completeProxiedInterfaces(advised, false);
    }

    /**
     * 确定针对给定AOP配置需要代理的完整接口集。
     * <p>这始终会添加 {@link Advised} 接口，除非 AdvisedSupport 的 {@link AdvisedSupport#setOpaque "opaque"} 标志被开启。始终会添加 {@link org.springframework.aop.SpringProxy} 标记接口。
     * @param advised 代理配置
     * @param decoratingProxy 是否暴露 {@link DecoratingProxy} 接口
     * @return 需要代理的完整接口集
     * @since 4.3
     * @see SpringProxy
     * @see Advised
     * @see DecoratingProxy
     */
    static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised, boolean decoratingProxy) {
        Class<?>[] specifiedInterfaces = advised.getProxiedInterfaces();
        if (specifiedInterfaces.length == 0) {
            // 没有用户指定的接口：检查目标类是否为接口。
            Class<?> targetClass = advised.getTargetClass();
            if (targetClass != null) {
                if (targetClass.isInterface()) {
                    advised.setInterfaces(targetClass);
                } else if (Proxy.isProxyClass(targetClass) || ClassUtils.isLambdaClass(targetClass)) {
                    advised.setInterfaces(targetClass.getInterfaces());
                }
                specifiedInterfaces = advised.getProxiedInterfaces();
            }
        }
        List<Class<?>> proxiedInterfaces = new ArrayList<>(specifiedInterfaces.length + 3);
        for (Class<?> ifc : specifiedInterfaces) {
            // 只有非密封接口才真正有资格进行 JDK 代理（在 JDK 17 中）。
            if (!ifc.isSealed()) {
                proxiedInterfaces.add(ifc);
            }
        }
        if (!advised.isInterfaceProxied(SpringProxy.class)) {
            proxiedInterfaces.add(SpringProxy.class);
        }
        if (!advised.isOpaque() && !advised.isInterfaceProxied(Advised.class)) {
            proxiedInterfaces.add(Advised.class);
        }
        if (decoratingProxy && !advised.isInterfaceProxied(DecoratingProxy.class)) {
            proxiedInterfaces.add(DecoratingProxy.class);
        }
        return ClassUtils.toClassArray(proxiedInterfaces);
    }

    /**
     * 提取给定代理实现的用户指定的接口，
     * 即代理实现的非Advised接口。
     * @param proxy 要分析的代理（通常是JDK动态代理）
     * @return 代理实现的全部用户指定接口，
     * 按原始顺序排列（从不为null或空）
     * @see Advised
     */
    public static Class<?>[] proxiedUserInterfaces(Object proxy) {
        Class<?>[] proxyInterfaces = proxy.getClass().getInterfaces();
        int nonUserIfcCount = 0;
        if (proxy instanceof SpringProxy) {
            nonUserIfcCount++;
        }
        if (proxy instanceof Advised) {
            nonUserIfcCount++;
        }
        if (proxy instanceof DecoratingProxy) {
            nonUserIfcCount++;
        }
        Class<?>[] userInterfaces = Arrays.copyOf(proxyInterfaces, proxyInterfaces.length - nonUserIfcCount);
        Assert.notEmpty(userInterfaces, "JDK proxy must implement one or more interfaces");
        return userInterfaces;
    }

    /**
     * 检查给定AdvisedSupport对象背后的代理的相等性。
     * 与AdvisedSupport对象的相等性不同：
     * 而是接口、顾问和目标源相等性的比较。
     */
    public static boolean equalsInProxy(AdvisedSupport a, AdvisedSupport b) {
        return (a == b || (equalsProxiedInterfaces(a, b) && equalsAdvisors(a, b) && a.getTargetSource().equals(b.getTargetSource())));
    }

    /**
     * 检查给定AdvisedSupport对象背后的代理接口的相等性。
     */
    public static boolean equalsProxiedInterfaces(AdvisedSupport a, AdvisedSupport b) {
        return Arrays.equals(a.getProxiedInterfaces(), b.getProxiedInterfaces());
    }

    /**
     * 检查给定AdvisedSupport对象背后的顾问是否相等。
     */
    public static boolean equalsAdvisors(AdvisedSupport a, AdvisedSupport b) {
        return a.getAdvisorCount() == b.getAdvisorCount() && Arrays.equals(a.getAdvisors(), b.getAdvisors());
    }

    /**
     * 将给定的参数适配到给定方法的目标签名中，如果需要的话：特别是，如果给定的可变参数（vararg）参数数组与方法中声明的可变参数的数组类型不匹配。
     * @param method 目标方法
     * @param arguments 给定的参数
     * @return 一个克隆的参数数组，如果不需要适配则返回原始数组
     * @since 4.2.3
     */
    static Object[] adaptArgumentsIfNecessary(Method method, @Nullable Object[] arguments) {
        if (ObjectUtils.isEmpty(arguments)) {
            return new Object[0];
        }
        if (method.isVarArgs() && (method.getParameterCount() == arguments.length)) {
            Class<?>[] paramTypes = method.getParameterTypes();
            int varargIndex = paramTypes.length - 1;
            Class<?> varargType = paramTypes[varargIndex];
            if (varargType.isArray()) {
                Object varargArray = arguments[varargIndex];
                if (varargArray instanceof Object[] && !varargType.isInstance(varargArray)) {
                    Object[] newArguments = new Object[arguments.length];
                    System.arraycopy(arguments, 0, newArguments, 0, varargIndex);
                    Class<?> targetElementType = varargType.getComponentType();
                    int varargLength = Array.getLength(varargArray);
                    Object newVarargArray = Array.newInstance(targetElementType, varargLength);
                    System.arraycopy(varargArray, 0, newVarargArray, 0, varargLength);
                    newArguments[varargIndex] = newVarargArray;
                    return newArguments;
                }
            }
        }
        return arguments;
    }
}
