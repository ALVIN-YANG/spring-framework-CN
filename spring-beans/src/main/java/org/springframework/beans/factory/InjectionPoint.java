// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 一个简单的注入点描述符，指向方法/构造函数参数或字段。由 {@link UnsatisfiedDependencyException} 暴露。
 * 同时也可作为工厂方法的参数，响应请求的注入点以构建定制的Bean实例。
 *
 * @author Juergen Hoeller
 * @since 4.3
 * @see UnsatisfiedDependencyException#getInjectionPoint()
 * @see org.springframework.beans.factory.config.DependencyDescriptor
 */
public class InjectionPoint {

    @Nullable
    protected MethodParameter methodParameter;

    @Nullable
    protected Field field;

    @Nullable
    private volatile Annotation[] fieldAnnotations;

    /**
     * 为方法或构造函数参数创建一个注入点描述符。
     * @param methodParameter 要包装的 MethodParameter
     */
    public InjectionPoint(MethodParameter methodParameter) {
        Assert.notNull(methodParameter, "MethodParameter must not be null");
        this.methodParameter = methodParameter;
    }

    /**
     * 为一个字段创建一个注入点描述符。
     * @param field 需要包装的字段
     */
    public InjectionPoint(Field field) {
        Assert.notNull(field, "Field must not be null");
        this.field = field;
    }

    /**
     * 复制构造函数。
     * @param original 要从中创建副本的原始描述符
     */
    protected InjectionPoint(InjectionPoint original) {
        this.methodParameter = (original.methodParameter != null ? new MethodParameter(original.methodParameter) : null);
        this.field = original.field;
        this.fieldAnnotations = original.fieldAnnotations;
    }

    /**
     * 仅用于子类中的序列化目的。
     */
    protected InjectionPoint() {
    }

    /**
     * 返回（如果有的话）包装的 MethodParameter。
     * <p>注意：MethodParameter 或 Field 中任一可用。
     * @return 返回 MethodParameter，如果没有则返回 {@code null}
     */
    @Nullable
    public MethodParameter getMethodParameter() {
        return this.methodParameter;
    }

    /**
     * 返回包装后的 Field，如果有的话。
     * <p>注意：MethodParameter 或 Field 中至少有一个是可用的。
     * @return 返回 Field，如果没有则返回 {@code null}
     */
    @Nullable
    public Field getField() {
        return this.field;
    }

    /**
     * 返回包装后的 MethodParameter，假设其存在。
     * @return 返回 MethodParameter（从不为 {@code null}）
     * @throws IllegalStateException 如果没有可用的 MethodParameter
     * @since 5.0
     */
    protected final MethodParameter obtainMethodParameter() {
        Assert.state(this.methodParameter != null, "MethodParameter is not available");
        return this.methodParameter;
    }

    /**
     * 获取与包装的字段或方法/构造函数参数关联的注解。
     */
    public Annotation[] getAnnotations() {
        if (this.field != null) {
            Annotation[] fieldAnnotations = this.fieldAnnotations;
            if (fieldAnnotations == null) {
                fieldAnnotations = this.field.getAnnotations();
                this.fieldAnnotations = fieldAnnotations;
            }
            return fieldAnnotations;
        } else {
            return obtainMethodParameter().getParameterAnnotations();
        }
    }

    /**
     * 获取给定类型的字段/参数注解，如果有的话。
     * @param annotationType 要获取的注解类型
     * @return 注解实例，如果没有找到则返回 {@code null}
     * @since 4.3.9
     */
    @Nullable
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return (this.field != null ? this.field.getAnnotation(annotationType) : obtainMethodParameter().getParameterAnnotation(annotationType));
    }

    /**
     * 返回底层字段或方法/构造函数参数声明的类型，
     * 以指示注入类型。
     */
    public Class<?> getDeclaredType() {
        return (this.field != null ? this.field.getType() : obtainMethodParameter().getParameterType());
    }

    /**
     * 返回被包装的成员，包含注入点。
     * @return 字段/方法/构造函数作为成员
     */
    public Member getMember() {
        return (this.field != null ? this.field : obtainMethodParameter().getMember());
    }

    /**
     * 返回被包装的注解元素。
     * <p>注意：对于方法/构造函数的参数，这将暴露方法或构造函数本身声明的注解
     * （即方法/构造函数级别，而不是参数级别）。在这种情况下，请使用
     * {@link #getAnnotations()} 获取参数级别的注解，与对应的字段注解透明地结合。
     * @return 字段 / 方法 / 构造函数作为 AnnotatedElement
     */
    public AnnotatedElement getAnnotatedElement() {
        return (this.field != null ? this.field : obtainMethodParameter().getAnnotatedElement());
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        InjectionPoint otherPoint = (InjectionPoint) other;
        return (ObjectUtils.nullSafeEquals(this.field, otherPoint.field) && ObjectUtils.nullSafeEquals(this.methodParameter, otherPoint.methodParameter));
    }

    @Override
    public int hashCode() {
        return (this.field != null ? this.field.hashCode() : ObjectUtils.nullSafeHashCode(this.methodParameter));
    }

    @Override
    public String toString() {
        return (this.field != null ? "field '" + this.field.getName() + "'" : String.valueOf(this.methodParameter));
    }
}
