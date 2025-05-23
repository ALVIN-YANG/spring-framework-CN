// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或原作者。

根据Apache许可证2.0版本（以下简称“许可证”）许可；除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.framework;

import org.springframework.lang.Nullable;

/**
 * 配置的 AOP 代理的委托接口，允许创建实际的代理对象。
 *
 * <p>默认提供的实现包括 JDK 动态代理和 CGLIB 代理，由 {@link DefaultAopProxyFactory} 应用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DefaultAopProxyFactory
 */
public interface AopProxy {

    /**
     * 创建一个新的代理对象。
     * <p>使用AopProxy的默认类加载器（如果代理创建需要的话）：通常，是线程上下文类加载器。
     * @return 新的代理对象（永远不会为null）
     * @see Thread#getContextClassLoader()
     */
    Object getProxy();

    /**
     * 创建一个新的代理对象。
     * <p>使用给定的类加载器（如果创建代理时需要）。
     * 如果传入值为 {@code null}，将直接传递下去，从而导致低级代理设施的默认设置，
     * 通常这与 AopProxy 实现的 {@link #getProxy()} 方法选择的默认设置不同。
     * @param classLoader 创建代理时要使用的类加载器
     * （或为低级代理设施的默认设置传入 {@code null}）
     * @return 新的代理对象（绝不会为 {@code null}）
     */
    Object getProxy(@Nullable ClassLoader classLoader);

    /**
     * 确定代理类。
     * @param classLoader 使用该类加载器创建代理类（或为低级代理设施默认值）
     * @return 代理类
     * @since 6.0
     */
    Class<?> getProxyClass(@Nullable ClassLoader classLoader);
}
