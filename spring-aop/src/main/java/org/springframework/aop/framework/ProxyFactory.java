// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache许可证2.0版（以下简称“许可证”）授权；除非符合许可证规定，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.TargetSource;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * AOP代理的工厂，用于程序化使用，而不是通过在bean工厂中的声明性设置。此类为在自定义用户代码中获取和配置AOP代理实例提供了一种简单的方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2003年3月14日
 */
@SuppressWarnings("serial")
public class ProxyFactory extends ProxyCreatorSupport {

    /**
     * 创建一个新的ProxyFactory。
     */
    public ProxyFactory() {
    }

    /**
     * 创建一个新的ProxyFactory。
     * <p>将代理给定目标对象实现的全部接口。
     * @param target 需要被代理的目标对象
     */
    public ProxyFactory(Object target) {
        setTarget(target);
        setInterfaces(ClassUtils.getAllInterfaces(target));
    }

    /**
     * 创建一个新的ProxyFactory。
     * <p>没有目标对象，只有接口。必须添加拦截器。
     * @param proxyInterfaces 代理应实现的接口
     */
    public ProxyFactory(Class<?>... proxyInterfaces) {
        setInterfaces(proxyInterfaces);
    }

    /**
     * 为给定的接口和拦截器创建一个新的 ProxyFactory。
     * <p>用于创建单个拦截器代理的便利方法，
     * 假设拦截器自己处理所有调用，而不是将调用委托给目标，例如在远程代理的情况下。
     * @param proxyInterface 代理应实现的接口
     * @param interceptor 代理应调用的拦截器
     */
    public ProxyFactory(Class<?> proxyInterface, Interceptor interceptor) {
        addInterface(proxyInterface);
        addAdvice(interceptor);
    }

    /**
     * 为指定的 {@code TargetSource} 创建一个 ProxyFactory，使代理实现指定的接口。
     * @param proxyInterface 代理应实现的接口
     * @param targetSource 代理应调用的 TargetSource
     */
    public ProxyFactory(Class<?> proxyInterface, TargetSource targetSource) {
        addInterface(proxyInterface);
        setTargetSource(targetSource);
    }

    /**
     * 根据该工厂中的设置创建一个新的代理。
     * <p>可重复调用。如果已添加或删除了接口，则效果可能不同。可以添加和移除拦截器。
     * <p>使用默认类加载器：通常，是线程上下文类加载器（如果创建代理时必要）。
     * @return 代理对象
     */
    public Object getProxy() {
        return createAopProxy().getProxy();
    }

    /**
     * 根据本工厂中的设置创建一个新的代理。
     * <p>可以重复调用。如果添加或删除了接口，效果可能会有所不同。可以添加和删除拦截器。
     * <p>使用给定的类加载器（如果代理创建需要的话）。
     * @param classLoader 使用该类加载器创建代理（或为低级代理设施的默认值）
     * @return 代理对象
     */
    public Object getProxy(@Nullable ClassLoader classLoader) {
        return createAopProxy().getProxy(classLoader);
    }

    /**
     * 根据本工厂中的设置确定代理类。
     * @param classLoader 用于创建代理类的类加载器（或为低级代理设施默认值的 {@code null}）
     * @return 代理类
     * @since 6.0
     */
    public Class<?> getProxyClass(@Nullable ClassLoader classLoader) {
        return createAopProxy().getProxyClass(classLoader);
    }

    /**
     * 为给定接口和拦截器创建一个新的代理。
     * <p>创建单个拦截器代理的便捷方法，假设拦截器自己处理所有调用，而不是委托给目标，例如在远程代理的情况下。
     * @param proxyInterface 代理应实现的接口
     * @param interceptor 代理应调用的拦截器
     * @return 代理对象
     * @see #ProxyFactory(Class, org.aopalliance.intercept.Interceptor)
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<T> proxyInterface, Interceptor interceptor) {
        return (T) new ProxyFactory(proxyInterface, interceptor).getProxy();
    }

    /**
     * 为指定的 {@code TargetSource} 创建一个代理，实现指定的接口。
     * @param proxyInterface 代理应该实现的接口
     * @param targetSource 代理应该调用的 TargetSource
     * @return 代理对象
     * @see #ProxyFactory(Class, org.springframework.aop.TargetSource)
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<T> proxyInterface, TargetSource targetSource) {
        return (T) new ProxyFactory(proxyInterface, targetSource).getProxy();
    }

    /**
     * 为指定的 {@code TargetSource} 创建一个代理，该代理扩展了 {@code TargetSource} 的目标类。
     * @param targetSource 应该调用该代理的 TargetSource
     * @return 代理对象
     */
    public static Object getProxy(TargetSource targetSource) {
        if (targetSource.getTargetClass() == null) {
            throw new IllegalArgumentException("Cannot create class proxy for TargetSource with null target class");
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetSource(targetSource);
        proxyFactory.setProxyTargetClass(true);
        return proxyFactory.getProxy();
    }
}
