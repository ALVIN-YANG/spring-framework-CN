// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。

根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于组合 {@link MethodMatcher MethodMatchers} 的静态实用方法。
 *
 * <p>方法匹配器可以静态评估（基于方法和目标类）或者需要在方法调用时进一步动态评估（基于调用时的参数）。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2003年11月11日
 * @see ClassFilters
 * @see Pointcuts
 */
public abstract class MethodMatchers {

    /**
     * 匹配所有被给定 MethodMatchers 中 <i>任何一个</i>（或两个都）匹配的方法。
     * @param mm1 第一个 MethodMatcher
     * @param mm2 第二个 MethodMatcher
     * @return 一个独立的 MethodMatcher，该匹配器匹配所有给定 MethodMatchers 中任何一个匹配的方法
     */
    public static MethodMatcher union(MethodMatcher mm1, MethodMatcher mm2) {
        return (mm1 instanceof IntroductionAwareMethodMatcher || mm2 instanceof IntroductionAwareMethodMatcher ? new UnionIntroductionAwareMethodMatcher(mm1, mm2) : new UnionMethodMatcher(mm1, mm2));
    }

    /**
     * 匹配所有给定 MethodMatchers 中的 <i>任一</i>（或两者）匹配的方法。
     * @param mm1 第一个 MethodMatcher
     * @param cf1 对应于第一个 MethodMatcher 的 ClassFilter
     * @param mm2 第二个 MethodMatcher
     * @param cf2 对应于第二个 MethodMatcher 的 ClassFilter
     * @return 一个独特的 MethodMatcher，该匹配器匹配所有给定 MethodMatchers 中任一匹配的方法
     */
    static MethodMatcher union(MethodMatcher mm1, ClassFilter cf1, MethodMatcher mm2, ClassFilter cf2) {
        return (mm1 instanceof IntroductionAwareMethodMatcher || mm2 instanceof IntroductionAwareMethodMatcher ? new ClassFilterAwareUnionIntroductionAwareMethodMatcher(mm1, cf1, mm2, cf2) : new ClassFilterAwareUnionMethodMatcher(mm1, cf1, mm2, cf2));
    }

    /**
     * 匹配所有同时满足给定两个 MethodMatchers 的方法。
     * @param mm1 第一个 MethodMatcher
     * @param mm2 第二个 MethodMatcher
     * @return 一个独特的 MethodMatcher，匹配所有同时满足给定两个 MethodMatchers 的方法
     */
    public static MethodMatcher intersection(MethodMatcher mm1, MethodMatcher mm2) {
        return (mm1 instanceof IntroductionAwareMethodMatcher || mm2 instanceof IntroductionAwareMethodMatcher ? new IntersectionIntroductionAwareMethodMatcher(mm1, mm2) : new IntersectionMethodMatcher(mm1, mm2));
    }

    /**
     * 将给定的MethodMatcher应用于给定的Method，支持（如果适用）使用{@link org.springframework.aop.IntroductionAwareMethodMatcher}。
     * @param mm 要应用的方法匹配器（可能是一个IntroductionAwareMethodMatcher）
     * @param method 候选方法
     * @param targetClass 目标类
     * @param hasIntroductions 如果我们提出请求的对象是受一个或多个引入的实体，则为true；否则为false
     * @return 此方法是否静态匹配
     */
    public static boolean matches(MethodMatcher mm, Method method, Class<?> targetClass, boolean hasIntroductions) {
        Assert.notNull(mm, "MethodMatcher must not be null");
        return (mm instanceof IntroductionAwareMethodMatcher iamm ? iamm.matches(method, targetClass, hasIntroductions) : mm.matches(method, targetClass));
    }

    /**
     * 用于两个给定MethodMatcher的并集的MethodMatcher实现。
     */
    @SuppressWarnings("serial")
    private static class UnionMethodMatcher implements MethodMatcher, Serializable {

        protected final MethodMatcher mm1;

        protected final MethodMatcher mm2;

        public UnionMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
            Assert.notNull(mm1, "First MethodMatcher must not be null");
            Assert.notNull(mm2, "Second MethodMatcher must not be null");
            this.mm1 = mm1;
            this.mm2 = mm2;
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return (matchesClass1(targetClass) && this.mm1.matches(method, targetClass)) || (matchesClass2(targetClass) && this.mm2.matches(method, targetClass));
        }

        protected boolean matchesClass1(Class<?> targetClass) {
            return true;
        }

        protected boolean matchesClass2(Class<?> targetClass) {
            return true;
        }

        @Override
        public boolean isRuntime() {
            return this.mm1.isRuntime() || this.mm2.isRuntime();
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass, Object... args) {
            return this.mm1.matches(method, targetClass, args) || this.mm2.matches(method, targetClass, args);
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof UnionMethodMatcher that && this.mm1.equals(that.mm1) && this.mm2.equals(that.mm2)));
        }

        @Override
        public int hashCode() {
            return 37 * this.mm1.hashCode() + this.mm2.hashCode();
        }

        @Override
        public String toString() {
            return getClass().getName() + ": " + this.mm1 + ", " + this.mm2;
        }
    }

    /**
     * 用于两个给定 MethodMatcher 的并集的 MethodMatcher 实现，其中至少一个是 IntroductionAwareMethodMatcher。
     * @since 5.1
     */
    @SuppressWarnings("serial")
    private static class UnionIntroductionAwareMethodMatcher extends UnionMethodMatcher implements IntroductionAwareMethodMatcher {

        public UnionIntroductionAwareMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
            super(mm1, mm2);
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
            return (matchesClass1(targetClass) && MethodMatchers.matches(this.mm1, method, targetClass, hasIntroductions)) || (matchesClass2(targetClass) && MethodMatchers.matches(this.mm2, method, targetClass, hasIntroductions));
        }
    }

    /**
     * 用于两个给定 MethodMatcher 的并集的 MethodMatcher 实现，
     * 支持每个 MethodMatcher 都关联一个 ClassFilter。
     */
    @SuppressWarnings("serial")
    private static class ClassFilterAwareUnionMethodMatcher extends UnionMethodMatcher {

        private final ClassFilter cf1;

        private final ClassFilter cf2;

        public ClassFilterAwareUnionMethodMatcher(MethodMatcher mm1, ClassFilter cf1, MethodMatcher mm2, ClassFilter cf2) {
            super(mm1, mm2);
            this.cf1 = cf1;
            this.cf2 = cf2;
        }

        @Override
        protected boolean matchesClass1(Class<?> targetClass) {
            return this.cf1.matches(targetClass);
        }

        @Override
        protected boolean matchesClass2(Class<?> targetClass) {
            return this.cf2.matches(targetClass);
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            ClassFilter otherCf1 = ClassFilter.TRUE;
            ClassFilter otherCf2 = ClassFilter.TRUE;
            if (other instanceof ClassFilterAwareUnionMethodMatcher cfa) {
                otherCf1 = cfa.cf1;
                otherCf2 = cfa.cf2;
            }
            return (this.cf1.equals(otherCf1) && this.cf2.equals(otherCf2));
        }

        @Override
        public int hashCode() {
            // 允许通过提供相同的哈希值与正则 UnionMethodMatcher 进行匹配...
            return super.hashCode();
        }

        @Override
        public String toString() {
            return getClass().getName() + ": " + this.cf1 + ", " + this.mm1 + ", " + this.cf2 + ", " + this.mm2;
        }
    }

    /**
     * 用于两个给定 MethodMatcher 的并集的 MethodMatcher 实现，其中至少一个是 IntroductionAwareMethodMatcher，
     * 支持每个 MethodMatcher 关联一个 ClassFilter。
     * @since 5.1
     */
    @SuppressWarnings("serial")
    private static class ClassFilterAwareUnionIntroductionAwareMethodMatcher extends ClassFilterAwareUnionMethodMatcher implements IntroductionAwareMethodMatcher {

        public ClassFilterAwareUnionIntroductionAwareMethodMatcher(MethodMatcher mm1, ClassFilter cf1, MethodMatcher mm2, ClassFilter cf2) {
            super(mm1, cf1, mm2, cf2);
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
            return (matchesClass1(targetClass) && MethodMatchers.matches(this.mm1, method, targetClass, hasIntroductions)) || (matchesClass2(targetClass) && MethodMatchers.matches(this.mm2, method, targetClass, hasIntroductions));
        }
    }

    /**
     * 用于两个给定MethodMatcher交集的方法匹配器实现。
     */
    @SuppressWarnings("serial")
    private static class IntersectionMethodMatcher implements MethodMatcher, Serializable {

        protected final MethodMatcher mm1;

        protected final MethodMatcher mm2;

        public IntersectionMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
            Assert.notNull(mm1, "First MethodMatcher must not be null");
            Assert.notNull(mm2, "Second MethodMatcher must not be null");
            this.mm1 = mm1;
            this.mm2 = mm2;
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return (this.mm1.matches(method, targetClass) && this.mm2.matches(method, targetClass));
        }

        @Override
        public boolean isRuntime() {
            return (this.mm1.isRuntime() || this.mm2.isRuntime());
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass, Object... args) {
            // 因为一个动态交点可能由静态部分和动态部分组成，
            // 我们必须避免在动态匹配器上调用带3个参数的matches方法，因为
            // 这可能会是一个不被支持的运算。
            boolean aMatches = (this.mm1.isRuntime() ? this.mm1.matches(method, targetClass, args) : this.mm1.matches(method, targetClass));
            boolean bMatches = (this.mm2.isRuntime() ? this.mm2.matches(method, targetClass, args) : this.mm2.matches(method, targetClass));
            return aMatches && bMatches;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof IntersectionMethodMatcher that && this.mm1.equals(that.mm1) && this.mm2.equals(that.mm2)));
        }

        @Override
        public int hashCode() {
            return 37 * this.mm1.hashCode() + this.mm2.hashCode();
        }

        @Override
        public String toString() {
            return getClass().getName() + ": " + this.mm1 + ", " + this.mm2;
        }
    }

    /**
     * 用于两个给定 MethodMatcher 的交集的方法匹配器实现，其中至少一个是 IntroductionAwareMethodMatcher。
     * @since 5.1
     */
    @SuppressWarnings("serial")
    private static class IntersectionIntroductionAwareMethodMatcher extends IntersectionMethodMatcher implements IntroductionAwareMethodMatcher {

        public IntersectionIntroductionAwareMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
            super(mm1, mm2);
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
            return (MethodMatchers.matches(this.mm1, method, targetClass, hasIntroductions) && MethodMatchers.matches(this.mm2, method, targetClass, hasIntroductions));
        }
    }
}
