// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非根据法律要求或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为还是适销性。
有关许可的具体语言、权限和限制，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import java.util.Arrays;
import org.springframework.aop.ClassFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 用于组合 {@link ClassFilter ClassFilters} 的静态实用方法。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2003年11月11日
 * @see MethodMatchers
 * @see Pointcuts
 */
public abstract class ClassFilters {

    /**
     * 匹配所有给定 ClassFilters 中<i>任一</i>（或两者）匹配的类。
     * @param cf1 第一个 ClassFilter
     * @param cf2 第二个 ClassFilter
     * @return 一个独特的 ClassFilter，匹配所有给定 ClassFilter 中任一匹配的类
     */
    public static ClassFilter union(ClassFilter cf1, ClassFilter cf2) {
        Assert.notNull(cf1, "First ClassFilter must not be null");
        Assert.notNull(cf2, "Second ClassFilter must not be null");
        return new UnionClassFilter(new ClassFilter[] { cf1, cf2 });
    }

    /**
     * 匹配所有给定 ClassFilters 中 <i>任一</i>（或所有）匹配的类。
     * @param classFilters 要匹配的 ClassFilters
     * @return 一个独特的 ClassFilter，它匹配所有给定 ClassFilter 中任一匹配的类
     */
    public static ClassFilter union(ClassFilter[] classFilters) {
        Assert.notEmpty(classFilters, "ClassFilter array must not be empty");
        return new UnionClassFilter(classFilters);
    }

    /**
     * 匹配所有同时满足给定两个ClassFilters的类。
     * @param cf1 第一个ClassFilter
     * @param cf2 第二个ClassFilter
     * @return 一个独特的ClassFilter，它匹配所有同时满足给定两个ClassFilter的类
     */
    public static ClassFilter intersection(ClassFilter cf1, ClassFilter cf2) {
        Assert.notNull(cf1, "First ClassFilter must not be null");
        Assert.notNull(cf2, "Second ClassFilter must not be null");
        return new IntersectionClassFilter(new ClassFilter[] { cf1, cf2 });
    }

    /**
     * 匹配所有给定的 ClassFilters 都满足的类。
     * @param classFilters 要匹配的 ClassFilters
     * @return 一个匹配所有给定 ClassFilter 都满足的类的独特的 ClassFilter
     */
    public static ClassFilter intersection(ClassFilter[] classFilters) {
        Assert.notEmpty(classFilters, "ClassFilter array must not be empty");
        return new IntersectionClassFilter(classFilters);
    }

    /**
     * 为给定类过滤器的并集实现的 ClassFilter 实现。
     */
    @SuppressWarnings("serial")
    private static class UnionClassFilter implements ClassFilter, Serializable {

        private final ClassFilter[] filters;

        UnionClassFilter(ClassFilter[] filters) {
            this.filters = filters;
        }

        @Override
        public boolean matches(Class<?> clazz) {
            for (ClassFilter filter : this.filters) {
                if (filter.matches(clazz)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof UnionClassFilter that && ObjectUtils.nullSafeEquals(this.filters, that.filters)));
        }

        @Override
        public int hashCode() {
            return ObjectUtils.nullSafeHashCode(this.filters);
        }

        @Override
        public String toString() {
            return getClass().getName() + ": " + Arrays.toString(this.filters);
        }
    }

    /**
     * 给定 ClassFilters 交集的 ClassFilter 实现。
     */
    @SuppressWarnings("serial")
    private static class IntersectionClassFilter implements ClassFilter, Serializable {

        private final ClassFilter[] filters;

        IntersectionClassFilter(ClassFilter[] filters) {
            this.filters = filters;
        }

        @Override
        public boolean matches(Class<?> clazz) {
            for (ClassFilter filter : this.filters) {
                if (!filter.matches(clazz)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof IntersectionClassFilter that && ObjectUtils.nullSafeEquals(this.filters, that.filters)));
        }

        @Override
        public int hashCode() {
            return ObjectUtils.nullSafeHashCode(this.filters);
        }

        @Override
        public String toString() {
            return getClass().getName() + ": " + Arrays.toString(this.filters);
        }
    }
}
