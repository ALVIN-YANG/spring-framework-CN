// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。

根据Apache License, Version 2.0（以下简称“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按照“现状”分发，不提供任何形式的保证或条件，无论是明示的还是隐含的。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support.annotation;

import java.lang.annotation.Annotation;
import org.springframework.aop.ClassFilter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 简单的 ClassFilter，用于查找类上是否存在特定的注解。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see AnnotationMatchingPointcut
 */
public class AnnotationClassFilter implements ClassFilter {

    private final Class<? extends Annotation> annotationType;

    private final boolean checkInherited;

    /**
     * 为指定的注解类型创建一个新的AnnotationClassFilter。
     * @param annotationType 要查找的注解类型
     */
    public AnnotationClassFilter(Class<? extends Annotation> annotationType) {
        this(annotationType, false);
    }

    /**
     * 为给定的注解类型创建一个新的 AnnotationClassFilter。
     * @param annotationType 要查找的注解类型
     * @param checkInherited 是否还要检查该注解类型的超类和接口，以及元注解
     * （即是否使用 {@link AnnotatedElementUtils#hasAnnotation} 的语义，而不是标准的 Java {@link Class#isAnnotationPresent}）
     */
    public AnnotationClassFilter(Class<? extends Annotation> annotationType, boolean checkInherited) {
        Assert.notNull(annotationType, "Annotation type must not be null");
        this.annotationType = annotationType;
        this.checkInherited = checkInherited;
    }

    @Override
    public boolean matches(Class<?> clazz) {
        return (this.checkInherited ? AnnotatedElementUtils.hasAnnotation(clazz, this.annotationType) : clazz.isAnnotationPresent(this.annotationType));
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof AnnotationClassFilter otherCf && this.annotationType.equals(otherCf.annotationType) && this.checkInherited == otherCf.checkInherited));
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
