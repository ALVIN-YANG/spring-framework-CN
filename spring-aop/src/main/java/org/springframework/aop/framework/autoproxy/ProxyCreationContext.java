// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache License, Version 2.0 ("许可证")进行许可；
除非法律要求或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下网址获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按"原样"提供，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy;

import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;

/**
 * 用于存储当前代理创建上下文的持有者，由自动代理创建者如 {@link AbstractAdvisorAutoProxyCreator} 公开。
 *
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.5
 */
public final class ProxyCreationContext {

    /**
     * ThreadLocal 用于在 Advisor 匹配期间保存当前代理的 bean 名称。
     */
    private static final ThreadLocal<String> currentProxiedBeanName = new NamedThreadLocal<>("Name of currently proxied bean");

    private ProxyCreationContext() {
    }

    /**
     * 返回当前代理的 Bean 实例的名称。
     * @return 返回 Bean 的名称，或如果不可用则返回 {@code null}
     */
    @Nullable
    public static String getCurrentProxiedBeanName() {
        return currentProxiedBeanName.get();
    }

    /**
     * 设置当前代理的 bean 实例的名称。
     * @param beanName bean 的名称，或使用 {@code null} 来重置它
     */
    static void setCurrentProxiedBeanName(@Nullable String beanName) {
        if (beanName != null) {
            currentProxiedBeanName.set(beanName);
        } else {
            currentProxiedBeanName.remove();
        }
    }
}
