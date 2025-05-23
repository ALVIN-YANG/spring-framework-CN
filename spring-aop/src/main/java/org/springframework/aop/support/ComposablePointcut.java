// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的明示或暗示保证。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于构建切入点的一个便捷类。
 *
 * 所有方法都返回 {@code ComposablePointcut}，因此我们可以使用如以下示例中的简洁语法。
 *
 * <pre class="code">Pointcut pc = new ComposablePointcut()
 *                      .union(classFilter)
 *                      .intersection(methodMatcher)
 *                      .intersection(pointcut);</pre>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 11.11.2003
 * @see Pointcuts
 */
public class ComposablePointcut implements Pointcut, Serializable {

    /**
     * 使用 Spring 1.2 中的 serialVersionUID 以实现互操作性。
     */
    private static final long serialVersionUID = -2743223737633663832L;

    @SuppressWarnings("serial")
    private ClassFilter classFilter;

    @SuppressWarnings("serial")
    private MethodMatcher methodMatcher;

    /**
     * 创建一个默认的 ComposablePointcut，使用 {@code ClassFilter.TRUE}
     * 和 {@code MethodMatcher.TRUE}。
     */
    public ComposablePointcut() {
        this.classFilter = ClassFilter.TRUE;
        this.methodMatcher = MethodMatcher.TRUE;
    }

    /**
     * 根据给定的 Pointcut 创建一个 ComposablePointcut。
     * @param pointcut 原始的 Pointcut
     */
    public ComposablePointcut(Pointcut pointcut) {
        Assert.notNull(pointcut, "Pointcut must not be null");
        this.classFilter = pointcut.getClassFilter();
        this.methodMatcher = pointcut.getMethodMatcher();
    }

    /**
     * 为给定的 ClassFilter 创建一个 ComposablePointcut，使用 {@code MethodMatcher.TRUE}。
     * @param classFilter 要使用的 ClassFilter
     */
    public ComposablePointcut(ClassFilter classFilter) {
        Assert.notNull(classFilter, "ClassFilter must not be null");
        this.classFilter = classFilter;
        this.methodMatcher = MethodMatcher.TRUE;
    }

    /**
     * 为给定的 MethodMatcher 创建一个 ComposablePointcut，
     * 使用 {@code ClassFilter.TRUE}。
     * @param methodMatcher 要使用的 MethodMatcher
     */
    public ComposablePointcut(MethodMatcher methodMatcher) {
        Assert.notNull(methodMatcher, "MethodMatcher must not be null");
        this.classFilter = ClassFilter.TRUE;
        this.methodMatcher = methodMatcher;
    }

    /**
     * 为给定的 ClassFilter 和 MethodMatcher 创建一个 ComposablePointcut。
     * @param classFilter 要使用的 ClassFilter
     * @param methodMatcher 要使用的 MethodMatcher
     */
    public ComposablePointcut(ClassFilter classFilter, MethodMatcher methodMatcher) {
        Assert.notNull(classFilter, "ClassFilter must not be null");
        Assert.notNull(methodMatcher, "MethodMatcher must not be null");
        this.classFilter = classFilter;
        this.methodMatcher = methodMatcher;
    }

    /**
     * 使用给定的ClassFilter应用并集操作。
     * @param other 要应用并集操作的ClassFilter
     * @return 此可组合的切入点（用于调用链式操作）
     */
    public ComposablePointcut union(ClassFilter other) {
        this.classFilter = ClassFilters.union(this.classFilter, other);
        return this;
    }

    /**
     * 使用给定的 ClassFilter 应用交集。
     * @param other 要与之应用交集的 ClassFilter
     * @return 此可组合的切入点（用于调用链式调用）
     */
    public ComposablePointcut intersection(ClassFilter other) {
        this.classFilter = ClassFilters.intersection(this.classFilter, other);
        return this;
    }

    /**
     * 使用给定的 MethodMatcher 应用并集操作。
     * @param other 要应用并集操作的 MethodMatcher
     * @return 此可组合的切入点（用于调用链式操作）
     */
    public ComposablePointcut union(MethodMatcher other) {
        this.methodMatcher = MethodMatchers.union(this.methodMatcher, other);
        return this;
    }

    /**
     * 使用给定的 MethodMatcher 应用交集。
     * @param other 要应用交集的 MethodMatcher
     * @return 此可组合的切入点（用于调用链式调用）
     */
    public ComposablePointcut intersection(MethodMatcher other) {
        this.methodMatcher = MethodMatchers.intersection(this.methodMatcher, other);
        return this;
    }

    /**
     * 使用给定的Pointcut进行合并。
     * <p>注意，对于Pointcut合并，方法只有在它们的原始ClassFilter（来自原始Pointcut）也匹配的情况下才会匹配。
     * 来自不同Pointcut的方法匹配器和ClassFilter永远不会相互交织。
     * @param other 要与其应用合并的Pointcut
     * @return 此可组合的pointcut（用于调用链式调用）
     */
    public ComposablePointcut union(Pointcut other) {
        this.methodMatcher = MethodMatchers.union(this.methodMatcher, this.classFilter, other.getMethodMatcher(), other.getClassFilter());
        this.classFilter = ClassFilters.union(this.classFilter, other.getClassFilter());
        return this;
    }

    /**
     * 应用与给定切入点（Pointcut）的交集。
     * @param other 要应用交集的切入点
     * @return 此可组合的切入点（用于调用链式调用）
     */
    public ComposablePointcut intersection(Pointcut other) {
        this.classFilter = ClassFilters.intersection(this.classFilter, other.getClassFilter());
        this.methodMatcher = MethodMatchers.intersection(this.methodMatcher, other.getMethodMatcher());
        return this;
    }

    @Override
    public ClassFilter getClassFilter() {
        return this.classFilter;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this.methodMatcher;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof ComposablePointcut otherPointcut && this.classFilter.equals(otherPointcut.classFilter) && this.methodMatcher.equals(otherPointcut.methodMatcher)));
    }

    @Override
    public int hashCode() {
        return this.classFilter.hashCode() * 37 + this.methodMatcher.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + this.classFilter + ", " + this.methodMatcher;
    }
}
