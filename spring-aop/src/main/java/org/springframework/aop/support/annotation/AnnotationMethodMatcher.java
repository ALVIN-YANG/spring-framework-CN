// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License 2.0（以下简称“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcher;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 简单的 {@link org.springframework.aop.MethodMatcher MethodMatcher}，用于查找方法上是否存在特定的注解（同时检查被调用接口上的方法，如果有的话，以及目标类上对应的方法）。
 *
 * 作者：Juergen Hoeller
 * 作者：Sam Brannen
 * 自 2.0 版本以来
 * 查看：AnnotationMatchingPointcut
 */
public class AnnotationMethodMatcher extends StaticMethodMatcher {

    private final Class<? extends Annotation> annotationType;

    private final boolean checkInherited;

    /**
     * 为给定的注解类型创建一个新的AnnotationClassFilter。
     * @param annotationType 要查找的注解类型
     */
    public AnnotationMethodMatcher(Class<? extends Annotation> annotationType) {
        this(annotationType, false);
    }

    /**
     * 为给定的注解类型创建一个新的 AnnotationClassFilter。
     * @param annotationType 要查找的注解类型
     * @param checkInherited 是否也要检查该注解类型的超类和接口，以及元注解
     * （即是否使用 {@link AnnotatedElementUtils#hasAnnotation} 的语义，而不是标准的 Java
     * {@link Method#isAnnotationPresent}）
     * @since 5.0
     */
    public AnnotationMethodMatcher(Class<? extends Annotation> annotationType, boolean checkInherited) {
        Assert.notNull(annotationType, "Annotation type must not be null");
        this.annotationType = annotationType;
        this.checkInherited = checkInherited;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (matchesMethod(method)) {
            return true;
        }
        // 代理类永远不会在其重新声明的方方法上使用注解。
        if (Proxy.isProxyClass(targetClass)) {
            return false;
        }
        // 该方法可能位于接口上，因此我们也要在目标类上检查一下。
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        return (specificMethod != method && matchesMethod(specificMethod));
    }

    private boolean matchesMethod(Method method) {
        return (this.checkInherited ? AnnotatedElementUtils.hasAnnotation(method, this.annotationType) : method.isAnnotationPresent(this.annotationType));
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof AnnotationMethodMatcher otherMm && this.annotationType.equals(otherMm.annotationType) && this.checkInherited == otherMm.checkInherited));
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
