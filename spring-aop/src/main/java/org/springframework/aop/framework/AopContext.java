// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非您遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。有关许可的特定语言规定权限和
* 限制，请参阅许可证。*/
package org.springframework.aop.framework;

import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;

/**
 * 包含用于获取当前AOP调用信息的静态方法的类。
 *
 * <p>如果AOP框架配置为公开当前代理（非默认），则可以使用`currentProxy()`方法。它返回正在使用的AOP代理。目标对象或通知可以使用它来进行通知调用，就像在EJB中使用`getEJBObject()`一样。它们还可以用它来查找通知配置。
 *
 * <p>Spring的AOP框架默认不公开代理，因为这样做会有性能开销。
 *
 * <p>此类中的功能可能被需要访问调用中资源的目标对象使用。然而，当有合理的替代方案时，不应使用这种方法，因为它会使应用程序代码依赖于AOP的使用，特别是Spring AOP框架的使用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13.03.2003
 */
public final class AopContext {

    /**
     * 与此线程关联的 AOP 代理的 ThreadLocal 持有者。
     * 只有在控制代理配置的 "exposeProxy" 属性被设置为 "true" 时，才会包含非空值。
     * @see ProxyConfig#setExposeProxy
     */
    private static final ThreadLocal<Object> currentProxy = new NamedThreadLocal<>("Current AOP proxy");

    private AopContext() {
    }

    /**
     * 尝试返回当前的 AOP 代理。此方法仅当调用方法是通过 AOP 调用并且 AOP 框架已被设置为暴露代理时才可用。否则，此方法将抛出 IllegalStateException。
     * @return 当前 AOP 代理（从不返回 null）
     * @throws IllegalStateException 如果无法找到代理，因为方法是在 AOP 调用上下文之外调用的，或者因为 AOP 框架尚未配置为暴露代理
     */
    public static Object currentProxy() throws IllegalStateException {
        Object proxy = currentProxy.get();
        if (proxy == null) {
            throw new IllegalStateException("Cannot find current proxy: Set 'exposeProxy' property on Advised to 'true' to make it available, and " + "ensure that AopContext.currentProxy() is invoked in the same thread as the AOP invocation context.");
        }
        return proxy;
    }

    /**
     * 使给定的代理通过 {@code currentProxy()} 方法可用。
     * <p>注意调用者应小心保留适当的旧值。
     * @param proxy 要暴露的代理（或 {@code null} 以重置它）
     * @return 旧的代理，可能为 {@code null} 如果未绑定任何代理
     * @see #currentProxy()
     */
    @Nullable
    static Object setCurrentProxy(@Nullable Object proxy) {
        Object old = currentProxy.get();
        if (proxy != null) {
            currentProxy.set(proxy);
        } else {
            currentProxy.remove();
        }
        return old;
    }
}
