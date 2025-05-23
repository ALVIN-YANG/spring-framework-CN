// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。

根据Apache License, Version 2.0（“许可证”）许可；
除非根据法律规定或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下网址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
有关许可的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.aop;

import org.springframework.lang.Nullable;

/**
 * 一个{@code TargetSource}用于获取AOP调用的当前"目标"，如果没有环绕通知选择结束拦截器链，将通过反射调用。
 *
 * <p>如果{@code TargetSource}是"静态"的，它将始终返回相同的对象，这允许AOP框架进行优化。动态目标源可以支持池化、热插拔等功能。
 *
 * <p>应用开发者通常不需要直接与{@code TargetSources}打交道：这是一个AOP框架接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface TargetSource extends TargetClassAware {

    /**
     * 返回此 {@link TargetSource} 返回的目标类型。
     * <p>可以返回 {@code null}，尽管某些对 {@code TargetSource} 的使用可能仅适用于预定的目标类。
     * @return 此 {@link TargetSource} 返回的目标类型
     */
    @Override
    @Nullable
    Class<?> getTargetClass();

    /**
     * 所有调用{@link #getTarget()}是否会返回相同的对象？
     * <p>在这种情况下，将无需调用{@link #releaseTarget(Object)}，
     * AOP框架可以缓存{@link #getTarget()}的返回值。
     * @return 如果目标对象不可变，则返回{@code true}
     * @see #getTarget
     */
    boolean isStatic();

    /**
     * 返回目标实例。在 AOP 框架调用 AOP 方法调用的“目标”之前立即调用。
     * @return 包含连接点的目标对象，或者如果不存在实际的目标实例，则返回 {@code null}
     * @throws Exception 如果无法解析目标对象
     */
    @Nullable
    Object getTarget() throws Exception;

    /**
     * 释放从 {@link #getTarget()} 方法获取的指定目标对象（如果有的话）。
     * @param target 从调用 {@link #getTarget()} 获取的对象
     * @throws Exception 如果对象无法被释放时抛出异常
     */
    void releaseTarget(Object target) throws Exception;
}
