// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非根据法律要求或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“现状”分发，
不提供任何明示或暗示的保证或条件。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support.annotation;

import java.lang.annotation.Annotation;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 简单的 {@link Pointcut}，用于查找在指定类（通过 {@linkplain #forClassAnnotation}）或方法（通过 {@linkplain #forMethodAnnotation}）上存在特定注解。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 * @see AnnotationClassFilter
 * @see AnnotationMethodMatcher
 */
public class AnnotationMatchingPointcut implements Pointcut {

    private final ClassFilter classFilter;

    private final MethodMatcher methodMatcher;

    /**
     * 为指定的注解类型创建一个新的 AnnotationMatchingPointcut。
     * @param classAnnotationType 要在类级别查找的注解类型
     */
    public AnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType) {
        this(classAnnotationType, false);
    }

    /**
     * 为给定的注解类型创建一个新的 AnnotationMatchingPointcut。
     * @param classAnnotationType 要在类级别查找的注解类型
     * @param checkInherited 是否还要检查超类和接口，以及注解类型的元注解
     * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)
     */
    public AnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType, boolean checkInherited) {
        this.classFilter = new AnnotationClassFilter(classAnnotationType, checkInherited);
        this.methodMatcher = MethodMatcher.TRUE;
    }

    /**
     * 为给定的注解类型创建一个新的 AnnotationMatchingPointcut。
     * @param classAnnotationType 在类级别查找的注解类型（可以为 {@code null}）
     * @param methodAnnotationType 在方法级别查找的注解类型（可以为 {@code null}）
     */
    public AnnotationMatchingPointcut(@Nullable Class<? extends Annotation> classAnnotationType, @Nullable Class<? extends Annotation> methodAnnotationType) {
        this(classAnnotationType, methodAnnotationType, false);
    }

    /**
     * 为给定的注解类型创建一个新的 AnnotationMatchingPointcut。
     * @param classAnnotationType 要在类级别查找的注解类型（可以为 null）
     * @param methodAnnotationType 要在方法级别查找的注解类型（可以为 null）
     * @param checkInherited 是否还要检查超类和接口以及注解类型的元注解
     * @since 5.0
     * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)
     * @see AnnotationMethodMatcher#AnnotationMethodMatcher(Class, boolean)
     */
    public AnnotationMatchingPointcut(@Nullable Class<? extends Annotation> classAnnotationType, @Nullable Class<? extends Annotation> methodAnnotationType, boolean checkInherited) {
        Assert.isTrue((classAnnotationType != null || methodAnnotationType != null), "Either Class annotation type or Method annotation type needs to be specified (or both)");
        if (classAnnotationType != null) {
            this.classFilter = new AnnotationClassFilter(classAnnotationType, checkInherited);
        } else {
            this.classFilter = new AnnotationCandidateClassFilter(methodAnnotationType);
        }
        if (methodAnnotationType != null) {
            this.methodMatcher = new AnnotationMethodMatcher(methodAnnotationType, checkInherited);
        } else {
            this.methodMatcher = MethodMatcher.TRUE;
        }
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
        return (this == other || (other instanceof AnnotationMatchingPointcut otherPointcut && this.classFilter.equals(otherPointcut.classFilter) && this.methodMatcher.equals(otherPointcut.methodMatcher)));
    }

    @Override
    public int hashCode() {
        return this.classFilter.hashCode() * 37 + this.methodMatcher.hashCode();
    }

    @Override
    public String toString() {
        return "AnnotationMatchingPointcut: " + this.classFilter + ", " + this.methodMatcher;
    }

    /**
     * 用于创建一个用于匹配指定类级别注解的AnnotationMatchingPointcut的工厂方法。
     * @param annotationType 要在类级别查找的注解类型
     * @return 对应的AnnotationMatchingPointcut
     */
    public static AnnotationMatchingPointcut forClassAnnotation(Class<? extends Annotation> annotationType) {
        Assert.notNull(annotationType, "Annotation type must not be null");
        return new AnnotationMatchingPointcut(annotationType);
    }

    /**
     * 用于创建一个匹配指定注解的AnnotationMatchingPointcut的工厂方法，该注解位于方法级别。
     * @param annotationType 要在方法级别查找的注解类型
     * @return 相应的AnnotationMatchingPointcut
     */
    public static AnnotationMatchingPointcut forMethodAnnotation(Class<? extends Annotation> annotationType) {
        Assert.notNull(annotationType, "Annotation type must not be null");
        return new AnnotationMatchingPointcut(null, annotationType);
    }

    /**
     * 用于委托给 {@link AnnotationUtils#isCandidateClass} 的 {@link ClassFilter}，
     * 用于过滤那些其方法一开始就无需搜索的类。
     * @since 5.2
     */
    private static class AnnotationCandidateClassFilter implements ClassFilter {

        private final Class<? extends Annotation> annotationType;

        AnnotationCandidateClassFilter(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        @Override
        public boolean matches(Class<?> clazz) {
            return AnnotationUtils.isCandidateClass(clazz, this.annotationType);
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof AnnotationCandidateClassFilter that && this.annotationType.equals(that.annotationType)));
        }

        @Override
        public int hashCode() {
            return this.annotationType.hashCode();
        }

        @Override
        public String toString() {
            return getClass().getName() + ": " + this.annotationType;
        }
    }
}
