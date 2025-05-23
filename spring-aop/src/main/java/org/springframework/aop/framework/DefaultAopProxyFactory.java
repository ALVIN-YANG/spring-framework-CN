// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。

根据Apache License, Version 2.0（以下简称“许可证”）许可；除非符合许可证规定，否则不得使用此文件。
您可以在以下链接处获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import org.springframework.aop.SpringProxy;
import org.springframework.util.ClassUtils;

/**
 * 默认的 {@link AopProxyFactory} 实现类，创建 CGLIB 代理或 JDK 动态代理。
 *
 * <p>如果以下任一条件对于给定的 {@link AdvisedSupport} 实例成立，则创建 CGLIB 代理：
 * <ul>
 * <li>设置了 {@code optimize} 标志
 * <li>设置了 {@code proxyTargetClass} 标志
 * <li>没有指定任何代理接口
 * </ul>
 *
 * <p>通常，通过指定 {@code proxyTargetClass} 来强制使用 CGLIB 代理，或者指定一个或多个接口来使用 JDK 动态代理。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @author Sam Brannen
 * @since 2004年3月12日
 * @see AdvisedSupport#setOptimize
 * @see AdvisedSupport#setProxyTargetClass
 * @see AdvisedSupport#setInterfaces
 */
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {

    /**
     * 本类的单例实例。
     * @since 6.0.10
     */
    public static final DefaultAopProxyFactory INSTANCE = new DefaultAopProxyFactory();

    private static final long serialVersionUID = 7930414337282325166L;

    @Override
    public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
        if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
            Class<?> targetClass = config.getTargetClass();
            if (targetClass == null) {
                throw new AopConfigException("TargetSource cannot determine target class: " + "Either an interface or a target is required for proxy creation.");
            }
            if (targetClass.isInterface() || Proxy.isProxyClass(targetClass) || ClassUtils.isLambdaClass(targetClass)) {
                return new JdkDynamicAopProxy(config);
            }
            return new ObjenesisCglibAopProxy(config);
        } else {
            return new JdkDynamicAopProxy(config);
        }
    }

    /**
     * 判断提供的 {@link AdvisedSupport} 是否只指定了 {@link org.springframework.aop.SpringProxy} 接口（或者根本未指定任何代理接口）。
     */
    private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
        Class<?>[] ifcs = config.getProxiedInterfaces();
        return (ifcs.length == 0 || (ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0])));
    }
}
