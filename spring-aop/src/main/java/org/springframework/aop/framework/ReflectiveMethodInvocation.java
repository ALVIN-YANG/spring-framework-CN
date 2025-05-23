// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）许可；
除非遵守许可证，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或书面同意，否则在许可证下分发的软件
是以“现状”为基础分发的，不提供任何明示或暗示的保证或条件。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.lang.Nullable;

/**
 * Spring 对 AOP Alliance 的实现
 * 实现 {@link org.aopalliance.intercept.MethodInvocation} 接口，
 * 扩展实现 {@link org.springframework.aop.ProxyMethodInvocation} 接口。
 *
 * <p>使用反射调用目标对象。子类可以覆盖 {@link #invokeJoinpoint()} 方法来改变这种行为，
 * 因此，这个类也是一个有用的基类，用于更专门的 MethodInvocation 实现。
 *
 * <p>可以通过使用 {@link #invocableClone()} 方法克隆调用，重复调用 {@link #proceed()}（每次克隆调用一次）。
 * 还可以使用 {@link #setUserAttribute} / {@link #getUserAttribute} 方法将自定义属性附加到调用上。
 *
 * <p><b>注意：</b>这个类被认为是内部的，不应直接访问。它之所以是公开的，唯一原因是为了与现有的框架集成保持兼容（例如 Pitchfork）。
 * 对于任何其他用途，请使用 {@link ProxyMethodInvocation} 接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @see #invokeJoinpoint
 * @see #proceed
 * @see #invocableClone
 * @see #setUserAttribute
 * @see #getUserAttribute
 */
public class ReflectiveMethodInvocation implements ProxyMethodInvocation, Cloneable {

    protected final Object proxy;

    @Nullable
    protected final Object target;

    protected final Method method;

    protected Object[] arguments;

    @Nullable
    private final Class<?> targetClass;

    /**
     * 懒加载的用于此调用特定用户属性的映射。
     */
    @Nullable
    private Map<String, Object> userAttributes;

    /**
     * 需要动态检查的 MethodInterceptor 和 InterceptorAndDynamicMethodMatcher 列表
     */
    protected final List<?> interceptorsAndDynamicMethodMatchers;

    /**
     * 当前正在调用的拦截器的索引，从0开始。
     * -1，直到我们调用：然后是当前拦截器。
     */
    private int currentInterceptorIndex = -1;

    /**
     * 使用给定的参数构建一个新的ReflectiveMethodInvocation。
     * @param proxy 执行调用时的代理对象
     * @param target 要调用的目标对象
     * @param method 要调用的方法
     * @param arguments 调用方法时使用的参数
     * @param targetClass 目标类，用于MethodMatcher调用
     * @param interceptorsAndDynamicMethodMatchers 应该应用的中介，以及任何需要在运行时评估的InterceptorAndDynamicMethodMatchers。
     * 包含在此结构中的MethodMatchers必须已经被发现已经匹配，就静态而言尽可能匹配。传递一个数组可能会快大约10%，但会使代码复杂化。
     * 而且它只适用于静态切入点。
     */
    protected ReflectiveMethodInvocation(Object proxy, @Nullable Object target, Method method, @Nullable Object[] arguments, @Nullable Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {
        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = BridgeMethodResolver.findBridgedMethod(method);
        this.arguments = AopProxyUtils.adaptArgumentsIfNecessary(method, arguments);
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    @Override
    public final Object getProxy() {
        return this.proxy;
    }

    @Override
    @Nullable
    public final Object getThis() {
        return this.target;
    }

    @Override
    public final AccessibleObject getStaticPart() {
        return this.method;
    }

    /**
     * 返回在代理接口上调用的方法。
     * 可能与在接口的底层实现上调用该方法相对应，也可能不对应。
     */
    @Override
    public final Method getMethod() {
        return this.method;
    }

    @Override
    public final Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public void setArguments(Object... arguments) {
        this.arguments = arguments;
    }

    @Override
    @Nullable
    public Object proceed() throws Throwable {
        // 我们从索引 -1 开始，并且提前进行增加。
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return invokeJoinpoint();
        }
        Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
        if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher dm) {
            // 在此评估动态方法匹配器：静态部分将已存在
            // 经过评估，发现与匹配。
            Class<?> targetClass = (this.targetClass != null ? this.targetClass : this.method.getDeclaringClass());
            if (dm.matcher().matches(this.method, targetClass, this.arguments)) {
                return dm.interceptor().invoke(this);
            } else {
                // 动态匹配失败。
                // 跳过此拦截器并调用链中的下一个拦截器。
                return proceed();
            }
        } else {
            // 它是一个拦截器，所以我们只需调用它：切入点将具有
            // 在构造此对象之前，它已经被静态评估过了。
            return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
        }
    }

    /**
     * 使用反射调用连接点。
     * 子类可以重写此方法以使用自定义调用。
     * @return 连接点的返回值
     * @throws Throwable 如果调用连接点导致异常抛出
     */
    @Nullable
    protected Object invokeJoinpoint() throws Throwable {
        return AopUtils.invokeJoinpointUsingReflection(this.target, this.method, this.arguments);
    }

    /**
     * 此实现返回此调用对象的浅拷贝，包括原始参数数组的独立拷贝。
     * <p>在这种情况下，我们希望得到一个浅拷贝：我们希望使用相同的拦截器链和其他对象引用，但我们希望当前拦截器索引具有独立的值。
     * @see java.lang.Object#clone()
     */
    @Override
    public MethodInvocation invocableClone() {
        Object[] cloneArguments = this.arguments;
        if (this.arguments.length > 0) {
            // 构建一个 arguments 数组的独立副本。
            cloneArguments = this.arguments.clone();
        }
        return invocableClone(cloneArguments);
    }

    /**
     * 此实现返回此调用对象的浅拷贝，
     * 使用给定的参数数组进行克隆。
     * <p>在这种情况下，我们希望得到一个浅拷贝：我们希望使用相同的拦截器链和其他对象引用，
     * 但我们希望当前拦截器索引有一个独立的价值。
     * @see java.lang.Object#clone()
     */
    @Override
    public MethodInvocation invocableClone(Object... arguments) {
        // 强制初始化用户属性 Map
        // 为了在克隆体中具有共享的 Map 引用。
        if (this.userAttributes == null) {
            this.userAttributes = new HashMap<>();
        }
        // 创建 MethodInvocation 的克隆。
        try {
            ReflectiveMethodInvocation clone = (ReflectiveMethodInvocation) clone();
            clone.arguments = arguments;
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new IllegalStateException("Should be able to clone object of type [" + getClass() + "]: " + ex);
        }
    }

    @Override
    public void setUserAttribute(String key, @Nullable Object value) {
        if (value != null) {
            if (this.userAttributes == null) {
                this.userAttributes = new HashMap<>();
            }
            this.userAttributes.put(key, value);
        } else {
            if (this.userAttributes != null) {
                this.userAttributes.remove(key);
            }
        }
    }

    @Override
    @Nullable
    public Object getUserAttribute(String key) {
        return (this.userAttributes != null ? this.userAttributes.get(key) : null);
    }

    /**
     * 返回与此次调用关联的用户属性。
     * 此方法提供了一种与ThreadLocal绑定的调用范围替代方案。
     * <p>此映射是延迟初始化的，并且在AOP框架本身中不被使用。
     * @return 与此次调用关联的任何用户属性
     * （永远不会为{@code null}）
     */
    public Map<String, Object> getUserAttributes() {
        if (this.userAttributes == null) {
            this.userAttributes = new HashMap<>();
        }
        return this.userAttributes;
    }

    @Override
    public String toString() {
        // 不要对目标对象调用toString方法，因为它可能被代理。
        StringBuilder sb = new StringBuilder("ReflectiveMethodInvocation: ");
        sb.append(this.method).append("; ");
        if (this.target == null) {
            sb.append("target is null");
        } else {
            sb.append("target is of class [").append(this.target.getClass().getName()).append(']');
        }
        return sb.toString();
    }
}
