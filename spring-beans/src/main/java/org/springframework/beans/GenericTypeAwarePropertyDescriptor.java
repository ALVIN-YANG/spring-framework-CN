// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 您不得使用此文件除非符合许可证规定。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证发布的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性或非侵权的保证。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 标准JavaBeans {@link PropertyDescriptor} 类的扩展，
 * 覆盖了 {@code getPropertyType()} 方法，使得泛型声明的类型变量将与包含的bean类进行解析。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
final class GenericTypeAwarePropertyDescriptor extends PropertyDescriptor {

    private final Class<?> beanClass;

    @Nullable
    private final Method readMethod;

    @Nullable
    private final Method writeMethod;

    @Nullable
    private volatile Set<Method> ambiguousWriteMethods;

    @Nullable
    private MethodParameter writeMethodParameter;

    @Nullable
    private Class<?> propertyType;

    @Nullable
    private final Class<?> propertyEditorClass;

    public GenericTypeAwarePropertyDescriptor(Class<?> beanClass, String propertyName, @Nullable Method readMethod, @Nullable Method writeMethod, @Nullable Class<?> propertyEditorClass) throws IntrospectionException {
        super(propertyName, null, null);
        this.beanClass = beanClass;
        Method readMethodToUse = (readMethod != null ? BridgeMethodResolver.findBridgedMethod(readMethod) : null);
        Method writeMethodToUse = (writeMethod != null ? BridgeMethodResolver.findBridgedMethod(writeMethod) : null);
        if (writeMethodToUse == null && readMethodToUse != null) {
            // 回退：原始的JavaBeans反射可能未找到匹配的setter
            // 由于缺少桥梁方法解析，如果获取器使用了一个
            // 协变返回类型，而setter是为具体属性类型定义的。
            Method candidate = ClassUtils.getMethodIfAvailable(this.beanClass, "set" + StringUtils.capitalize(getName()), (Class<?>[]) null);
            if (candidate != null && candidate.getParameterCount() == 1) {
                writeMethodToUse = candidate;
            }
        }
        this.readMethod = readMethodToUse;
        this.writeMethod = writeMethodToUse;
        if (this.writeMethod != null) {
            if (this.readMethod == null) {
                // 编写的方法与读取方法不匹配：可能存在歧义
                // 几个重载变体，在这种情况下，已经选择了一个任意的获胜者
                // 通过 JDK 的 JavaBeans 反射器...
                Set<Method> ambiguousCandidates = new HashSet<>();
                for (Method method : beanClass.getMethods()) {
                    if (method.getName().equals(writeMethodToUse.getName()) && !method.equals(writeMethodToUse) && !method.isBridge() && method.getParameterCount() == writeMethodToUse.getParameterCount()) {
                        ambiguousCandidates.add(method);
                    }
                }
                if (!ambiguousCandidates.isEmpty()) {
                    this.ambiguousWriteMethods = ambiguousCandidates;
                }
            }
            this.writeMethodParameter = new MethodParameter(this.writeMethod, 0).withContainingClass(this.beanClass);
        }
        if (this.readMethod != null) {
            this.propertyType = GenericTypeResolver.resolveReturnType(this.readMethod, this.beanClass);
        } else if (this.writeMethodParameter != null) {
            this.propertyType = this.writeMethodParameter.getParameterType();
        }
        this.propertyEditorClass = propertyEditorClass;
    }

    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    @Override
    @Nullable
    public Method getReadMethod() {
        return this.readMethod;
    }

    @Override
    @Nullable
    public Method getWriteMethod() {
        return this.writeMethod;
    }

    public Method getWriteMethodForActualAccess() {
        Assert.state(this.writeMethod != null, "No write method available");
        Set<Method> ambiguousCandidates = this.ambiguousWriteMethods;
        if (ambiguousCandidates != null) {
            this.ambiguousWriteMethods = null;
            LogFactory.getLog(GenericTypeAwarePropertyDescriptor.class).debug("Non-unique JavaBean property '" + getName() + "' being accessed! Ambiguous write methods found next to actually used [" + this.writeMethod + "]: " + ambiguousCandidates);
        }
        return this.writeMethod;
    }

    public MethodParameter getWriteMethodParameter() {
        Assert.state(this.writeMethodParameter != null, "No write method available");
        return this.writeMethodParameter;
    }

    @Override
    @Nullable
    public Class<?> getPropertyType() {
        return this.propertyType;
    }

    @Override
    @Nullable
    public Class<?> getPropertyEditorClass() {
        return this.propertyEditorClass;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof GenericTypeAwarePropertyDescriptor that && getBeanClass().equals(that.getBeanClass()) && PropertyDescriptorUtils.equals(this, that)));
    }

    @Override
    public int hashCode() {
        int hashCode = getBeanClass().hashCode();
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getReadMethod());
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getWriteMethod());
        return hashCode;
    }
}
