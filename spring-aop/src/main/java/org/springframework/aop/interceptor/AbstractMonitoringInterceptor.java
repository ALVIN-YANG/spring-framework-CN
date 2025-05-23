// 翻译完成 glm-4-flash
/*版权所有 2002-2020 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可；
除非根据法律规定或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下链接处获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵权。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.interceptor;

import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.Nullable;

/**
 * 监控拦截器的基础类，例如性能监控器。
 * 提供可配置的“前缀”和“后缀”属性，有助于对性能监控结果进行分类/分组。
 *
 * <p>在其 {@link #invokeUnderTrace} 实现中，子类应调用 {@link #createInvocationTraceName} 方法为给定的跟踪创建一个名称，包括关于方法调用的信息以及前缀/后缀。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2.7
 * @see #setPrefix
 * @see #setSuffix
 * @see #createInvocationTraceName
 */
@SuppressWarnings("serial")
public abstract class AbstractMonitoringInterceptor extends AbstractTraceInterceptor {

    private String prefix = "";

    private String suffix = "";

    private boolean logTargetClassInvocation = false;

    /**
     * 设置将被追加到跟踪数据中的文本。
     * <p>默认值为无。
     */
    public void setPrefix(@Nullable String prefix) {
        this.prefix = (prefix != null ? prefix : "");
    }

    /**
     * 返回将附加到跟踪数据中的文本。
     */
    protected String getPrefix() {
        return this.prefix;
    }

    /**
     * 设置将要附加到跟踪数据前的文本。
     * <p>默认为无。
     */
    public void setSuffix(@Nullable String suffix) {
        this.suffix = (suffix != null ? suffix : "");
    }

    /**
     * 返回将要附加到跟踪数据前的文本。
     */
    protected String getSuffix() {
        return this.suffix;
    }

    /**
     * 设置是否在目标类上记录调用，如果适用（即如果方法实际上被委托给目标类）。
     * <p>默认为"false"，根据代理接口/类名记录调用。
     */
    public void setLogTargetClassInvocation(boolean logTargetClassInvocation) {
        this.logTargetClassInvocation = logTargetClassInvocation;
    }

    /**
     * 为给定的 {@code MethodInvocation} 创建一个用于跟踪/日志记录的 {@code String} 名称。此名称由配置的前缀、正在调用方法的完全限定名称以及配置的后缀组成。
     * @see #setPrefix
     * @see #setSuffix
     */
    protected String createInvocationTraceName(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        Class<?> clazz = method.getDeclaringClass();
        if (this.logTargetClassInvocation && clazz.isInstance(invocation.getThis())) {
            clazz = invocation.getThis().getClass();
        }
        String className = clazz.getName();
        return getPrefix() + className + '.' + method.getName() + getSuffix();
    }
}
