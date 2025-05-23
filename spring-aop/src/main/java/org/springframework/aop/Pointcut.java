// 翻译完成 glm-4-flash
/*版权所有 2002-2012，原作者或作者。

根据Apache License，版本2.0（“许可证”）授权；
除非根据法律规定或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律规定或书面同意，否则在许可证下分发的软件按照“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是隐含的。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop;

/**
 * 核心Spring切入点抽象。
 *
 * <p>切入点由一个 {@link ClassFilter} 和一个 {@link MethodMatcher} 组成。这两个基本术语以及切入点本身都可以组合起来构建组合（例如，通过 {@link org.springframework.aop.support.ComposablePointcut}）。
 *
 * @author Rod Johnson
 * @see ClassFilter
 * @see MethodMatcher
 * @see org.springframework.aop.support.Pointcuts
 * @see org.springframework.aop.support.ClassFilters
 * @see org.springframework.aop.support.MethodMatchers
 */
public interface Pointcut {

    /**
     * 返回此切入点对应的类过滤器。
     * @return 类过滤器（永远不会为null）
     */
    ClassFilter getClassFilter();

    /**
     * 返回此切入点对应的 MethodMatcher。
     * @return MethodMatcher（从不为 {@code null}）
     */
    MethodMatcher getMethodMatcher();

    /**
     * 始终匹配的规范切入点实例。
     */
    Pointcut TRUE = TruePointcut.INSTANCE;
}
