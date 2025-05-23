// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。

根据Apache License, Version 2.0（以下简称“许可证”）许可；
除非适用法律要求或经书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下链接获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop;

import java.lang.reflect.Method;

/**
 * 这是 {@link Pointcut} 的一部分：检查目标方法是否有资格接受建议。
 *
 * <p>一个 {@code MethodMatcher} 可以被评估为 <b>静态</b> 或在 <b>运行时</b>（动态）。静态匹配涉及方法和（可能）方法属性。动态匹配还会提供特定调用的参数，以及应用在连接点之前任何建议的任何效果。
 *
 * <p>如果实现从其 {@link #isRuntime()} 方法返回 {@code false}，则评估可以静态进行，并且结果将适用于所有调用此方法的实例，无论它们的参数如何。这意味着如果 {@link #isRuntime()} 方法返回 {@code false}，则永远不会调用 3 个参数的 {@link #matches(Method, Class, Object[])} 方法。
 *
 * <p>如果一个实现从其 2 个参数的 {@link #matches(Method, Class)} 方法返回 {@code true} 并且其 {@link #isRuntime()} 方法返回 {@code true}，则 3 个参数的 {@link #matches(Method, Class, Object[])} 方法将在 <i>每个潜在的建议执行之前立即调用</i>，以决定是否应该运行建议。所有之前的建议，例如拦截器链中的早期拦截器，都将已运行，因此它们在参数或 {@code ThreadLocal} 状态中产生的任何状态变化都将可用于评估时。
 *
 * <p><strong>警告</strong>：此接口的具体实现必须提供适当的 {@link Object#equals(Object)}、{@link Object#hashCode()} 和 {@link Object#toString()} 实现，以便在缓存场景中使用匹配器——例如，在 CGLIB 生成的代理中。截至 Spring 框架 6.0.13，必须生成与实现 {@code equals()} 逻辑一致的唯一字符串表示形式的 {@code toString()} 实现。请参阅框架中此接口的具体实现示例。
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 11.11.2003
 * @see Pointcut
 * @see ClassFilter
 */
public interface MethodMatcher {

    /**
     * 执行静态检查以确定给定的方法是否匹配。
     * <p>如果此方法返回 {@code false} 或者如果 {@link #isRuntime()} 返回 {@code false}，则不会进行运行时检查（即不会调用
     * {@link #matches(Method, Class, Object[])}）。
     * @param method 候选方法
     * @param targetClass 目标类
     * @return 是否在静态上匹配
     */
    boolean matches(Method method, Class<?> targetClass);

    /**
     * 这个 {@code MethodMatcher} 是否是动态的，也就是说，即使通过 {@link #matches(Method, Class)} 返回了 {@code true}，是否还需要在运行时通过 {@link #matches(Method, Class, Object[])} 方法进行最终检查？
     * <p>可以在创建 AOP 代理时调用，无需在每次方法调用之前再次调用。
     * @return 如果静态匹配通过，是否需要通过 {@link #matches(Method, Class, Object[])} 进行运行时匹配
     */
    boolean isRuntime();

    /**
     * 检查此方法是否存在运行时（动态）匹配，该匹配必须已在静态时匹配成功。
     * <p>此方法仅在调用者通过 {@link #matches(Method, Class)} 方法对给定方法和目标类返回值为 {@code true}，并且通过 {@link #isRuntime()} 方法返回值为 {@code true} 时被调用。
     * <p>在潜在运行建议之前立即调用，在 advice 链中任何较早的建议运行之后。
     * @param method 候选方法
     * @param targetClass 目标类
     * @param args 方法参数
     * @return 是否存在运行时匹配
     * @see #matches(Method, Class)
     */
    boolean matches(Method method, Class<?> targetClass, Object... args);

    /**
     * 一个匹配所有方法的 {@code MethodMatcher} 的规范实例。
     */
    MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;
}
