// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式，无论是明示的、暗示的还是其他方面的保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * 表示对在相同IoC上下文中通过bean名称或bean类型（基于声明的方法返回类型）查找对象的方法的覆盖。
 *
 * <p>适用于查找覆盖的方法可能声明参数，在这种情况下，给定的参数将被传递给bean检索操作。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.beans.factory.BeanFactory#getBean(String)
 * @see org.springframework.beans.factory.BeanFactory#getBean(Class)
 * @see org.springframework.beans.factory.BeanFactory#getBean(String, Object...)
 * @see org.springframework.beans.factory.BeanFactory#getBean(Class, Object...)
 * @see org.springframework.beans.factory.BeanFactory#getBeanProvider(ResolvableType)
 */
public class LookupOverride extends MethodOverride {

    @Nullable
    private final String beanName;

    @Nullable
    private Method method;

    /**
     * 构造一个新的 LookupOverride。
     * @param methodName 要重写的方法的名称
     * @param beanName 当前 {@code BeanFactory} 中被重写方法应返回的 bean 的名称（对于基于类型的 bean 检索，可能为 {@code null}）
     */
    public LookupOverride(String methodName, @Nullable String beanName) {
        super(methodName);
        this.beanName = beanName;
    }

    /**
     * 构造一个新的 LookupOverride。
     * @param method 要重写的的方法声明
     * @param beanName 当前 {@code BeanFactory} 中被重写方法应返回的 bean 名称（对于基于类型的 bean 检索，可能为 {@code null}）
     */
    public LookupOverride(Method method, @Nullable String beanName) {
        super(method.getName());
        this.method = method;
        this.beanName = beanName;
    }

    /**
     * 返回此方法应返回的 bean 名称。
     */
    @Nullable
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * 通过方法引用或方法名称匹配指定的方法。
     * 由于向后兼容性原因，在具有给定名称的重载非抽象方法的场景中，只有无参方法的形式会被转换为由容器驱动的查找方法。
     * 在提供了 {@link Method} 的情况下，只有直接匹配将被考虑，通常由 {@code @Lookup} 注解来界定。
     */
    @Override
    public boolean matches(Method method) {
        if (this.method != null) {
            return method.equals(this.method);
        } else {
            return (method.getName().equals(getMethodName()) && (!isOverloaded() || Modifier.isAbstract(method.getModifiers()) || method.getParameterCount() == 0));
        }
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (other instanceof LookupOverride that && super.equals(other) && ObjectUtils.nullSafeEquals(this.method, that.method) && ObjectUtils.nullSafeEquals(this.beanName, that.beanName));
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.beanName);
    }

    @Override
    public String toString() {
        return "LookupOverride for method '" + getMethodName() + "'";
    }
}
