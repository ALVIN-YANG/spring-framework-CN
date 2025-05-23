// 翻译完成 glm-4-flash
/*版权所有 2002-2015 原作者或作者。
 
根据Apache许可证版本2.0（“许可证”）许可；
除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性还是其特定用途的适用性。
有关许可的具体语言和限制，请参阅许可证。*/
package org.springframework.aop;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.Nullable;

/**
 * 扩展 AOP 联盟（@link org.aopalliance.intercept.MethodInvocation）接口，
 * 允许访问通过该方法调用所使用的代理。
 *
 * <p>如果需要，能够用代理替换返回值是有用的，例如如果调用目标返回了自身。
 *
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @since 1.1.3
 * @see org.springframework.aop.framework.ReflectiveMethodInvocation
 * @see org.springframework.aop.support.DelegatingIntroductionInterceptor
 */
public interface ProxyMethodInvocation extends MethodInvocation {

    /**
     * 返回通过该方法调用所使用的代理。
     * @return 原始代理对象
     */
    Object getProxy();

    /**
     * 创建此对象的副本。如果在调用此对象的 {@code proceed()} 方法之前进行克隆，
     * 则每个克隆可以调用一次 {@code proceed()} 来调用连接点（以及整个通知链）超过一次。
     * @return 此调用的可调用克隆。
     * 每个克隆可以调用一次 {@code proceed()}。
     */
    MethodInvocation invocableClone();

    /**
     * 创建此对象的副本。如果在调用此对象的 {@code proceed()} 方法之前进行克隆，
     * 则每个克隆都可以调用一次 {@code proceed()} 来执行连接点（以及剩余的咨询链）多次。
     * @param arguments 应用于克隆调用的参数，覆盖原始参数
     * @return 此调用的可调用克隆。
     * 每个克隆可以调用一次 {@code proceed()}。
     */
    MethodInvocation invocableClone(Object... arguments);

    /**
     * 设置后续调用此链中任何增强（Advice）时要使用的参数。
     * @param arguments 参数数组
     */
    void setArguments(Object... arguments);

    /**
     * 将指定的用户属性及其给定值添加到这次调用中。
     * <p>这些属性在AOP框架本身中不被使用。它们只是作为调用对象的一部分保留，以便在特殊的拦截器中使用。
     * @param key 属性的名称
     * @param value 属性的值，或者使用{@code null}来重置它
     */
    void setUserAttribute(String key, @Nullable Object value);

    /**
     * 返回指定用户属性的值。
     * @param key 属性的名称
     * @return 属性的值，如果没有设置则返回 {@code null}
     * @see #setUserAttribute
     */
    @Nullable
    Object getUserAttribute(String key);
}
