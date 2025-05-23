// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import kotlin.reflect.KProperty;
import kotlin.reflect.jvm.ReflectJvmMapping;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * 用于描述即将注入的特定依赖的描述符。
 * 包装一个构造函数参数、方法参数或字段，
 * 允许统一访问它们的元数据。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
@SuppressWarnings("serial")
public class DependencyDescriptor extends InjectionPoint implements Serializable {

    private final Class<?> declaringClass;

    @Nullable
    private String methodName;

    @Nullable
    private Class<?>[] parameterTypes;

    private int parameterIndex;

    @Nullable
    private String fieldName;

    private final boolean required;

    private final boolean eager;

    private int nestingLevel = 1;

    @Nullable
    private Class<?> containingClass;

    @Nullable
    private transient volatile ResolvableType resolvableType;

    @Nullable
    private transient volatile TypeDescriptor typeDescriptor;

    /**
     * 创建一个用于方法或构造函数参数的新描述符。
     * 将依赖视为'急切'（eager）。
     * @param methodParameter 需要包装的MethodParameter
     * @param required 该依赖是否为必需的
     */
    public DependencyDescriptor(MethodParameter methodParameter, boolean required) {
        this(methodParameter, required, true);
    }

    /**
     * 创建一个方法或构造函数参数的新描述符。
     * @param methodParameter 需要包装的 MethodParameter
     * @param required 是否需要这个依赖项
     * @param eager 是否这个依赖项在“急切”意义上，即急切地解决潜在目标bean以进行类型匹配
     */
    public DependencyDescriptor(MethodParameter methodParameter, boolean required, boolean eager) {
        super(methodParameter);
        this.declaringClass = methodParameter.getDeclaringClass();
        if (methodParameter.getMethod() != null) {
            this.methodName = methodParameter.getMethod().getName();
        }
        this.parameterTypes = methodParameter.getExecutable().getParameterTypes();
        this.parameterIndex = methodParameter.getParameterIndex();
        this.containingClass = methodParameter.getContainingClass();
        this.required = required;
        this.eager = eager;
    }

    /**
     * 创建一个新的字段描述符。
     * 将依赖视为'急切'。
     * @param field 要包装的字段
     * @param required 依赖是否必需
     */
    public DependencyDescriptor(Field field, boolean required) {
        this(field, required, true);
    }

    /**
     * 创建一个新的字段描述符。
     * @param field 要包装的字段
     * @param required 该依赖项是否为必需的
     * @param eager 是否在“急切”意义上解析潜在的匹配目标bean，即急切地解决类型匹配的bean
     */
    public DependencyDescriptor(Field field, boolean required, boolean eager) {
        super(field);
        this.declaringClass = field.getDeclaringClass();
        this.fieldName = field.getName();
        this.required = required;
        this.eager = eager;
    }

    /**
     * 复制构造函数。
     * @param original 要从中创建副本的原始描述符
     */
    public DependencyDescriptor(DependencyDescriptor original) {
        super(original);
        this.declaringClass = original.declaringClass;
        this.methodName = original.methodName;
        this.parameterTypes = original.parameterTypes;
        this.parameterIndex = original.parameterIndex;
        this.fieldName = original.fieldName;
        this.containingClass = original.containingClass;
        this.required = original.required;
        this.eager = original.eager;
        this.nestingLevel = original.nestingLevel;
    }

    /**
     * 返回此依赖是否必需。
     * <p>可选语义源自Java 8的{@link java.util.Optional}、参数级别的任何一种名为{@code Nullable}的注解（例如来自JSR-305或FindBugs注解集），或在Kotlin中的语言级别可空类型声明。
     */
    public boolean isRequired() {
        if (!this.required) {
            return false;
        }
        if (this.field != null) {
            return !(this.field.getType() == Optional.class || hasNullableAnnotation() || (KotlinDetector.isKotlinReflectPresent() && KotlinDetector.isKotlinType(this.field.getDeclaringClass()) && KotlinDelegate.isNullable(this.field)));
        } else {
            return !obtainMethodParameter().isOptional();
        }
    }

    /**
     * 检查底层字段是否被任何形式的 `Nullable` 注解标注，例如 `jakarta.annotation.Nullable` 或 `edu.umd.cs.findbugs.annotations.Nullable`。
     */
    private boolean hasNullableAnnotation() {
        for (Annotation ann : getAnnotations()) {
            if ("Nullable".equals(ann.annotationType().getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回此依赖是否为“eager”，即在类型匹配时主动解析潜在目标bean的依赖。
     */
    public boolean isEager() {
        return this.eager;
    }

    /**
     * 解决指定的非唯一场景：默认情况下，
     * 抛出 {@link NoUniqueBeanDefinitionException}。
     * <p>子类可以覆盖此方法以选择其中一个实例，或者通过返回 {@code null} 来完全放弃结果。
     * @param type 请求的 Bean 类型
     * @param matchingBeans 一个映射，包含 Bean 名称及其对应的 Bean 实例，这些实例已经预先选择用于给定的类型
     * （已经应用了限定符等）
     * @return 要继续处理的 Bean 实例，或 {@code null} 表示没有实例
     * @throws BeansException 如果非唯一场景是致命的，则抛出异常
     * @since 5.1
     */
    @Nullable
    public Object resolveNotUnique(ResolvableType type, Map<String, Object> matchingBeans) throws BeansException {
        throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
    }

    /**
     * 解析该依赖项针对给定工厂的快捷方式，例如
     * 考虑一些预先解析的信息。
     * <p>解析算法将首先尝试通过此方法解析快捷方式，然后再进入跨所有bean的常规类型匹配算法。
     * 子类可以重写此方法，根据预先缓存的改进解析性能的信息，同时仍然获得对{@link InjectionPoint}的暴露等。
     * @param beanFactory 相关联的工厂
     * @return 如果有，则返回快捷方式结果，否则返回{@code null}
     * @throws BeansException 如果无法获得快捷方式
     * @since 4.3.1
     */
    @Nullable
    public Object resolveShortcut(BeanFactory beanFactory) throws BeansException {
        return null;
    }

    /**
     * 解决指定的bean名称，作为此依赖匹配算法的候选结果，从给定的工厂中映射到一个bean实例。
     * <p>默认实现调用{@link BeanFactory#getBean(String)}。
     * 子类可以提供额外的参数或其他自定义。
     * @param beanName bean名称，作为此依赖的候选结果
     * @param requiredType 预期的bean类型（作为断言）
     * @param beanFactory 相关的工厂
     * @return bean实例（从不为{@code null}）
     * @throws BeansException 如果无法获取bean
     * @since 4.3.2
     * @see BeanFactory#getBean(String)
     */
    public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) throws BeansException {
        return beanFactory.getBean(beanName);
    }

    /**
     * 增加此描述符的嵌套级别。
     */
    public void increaseNestingLevel() {
        this.nestingLevel++;
        this.resolvableType = null;
        if (this.methodParameter != null) {
            this.methodParameter = this.methodParameter.nested();
        }
    }

    /**
     * 可选地设置包含此依赖的具体类。
     * 这可能与声明参数/字段的类不同，因为它可能是其子类，可能会替换类型变量。
     * @since 4.0
     */
    public void setContainingClass(Class<?> containingClass) {
        this.containingClass = containingClass;
        this.resolvableType = null;
        if (this.methodParameter != null) {
            this.methodParameter = this.methodParameter.withContainingClass(containingClass);
        }
    }

    /**
     * 为包装的参数/字段构建一个 {@link ResolvableType} 对象。
     * @since 4.0
     */
    public ResolvableType getResolvableType() {
        ResolvableType resolvableType = this.resolvableType;
        if (resolvableType == null) {
            resolvableType = (this.field != null ? ResolvableType.forField(this.field, this.nestingLevel, this.containingClass) : ResolvableType.forMethodParameter(obtainMethodParameter()));
            this.resolvableType = resolvableType;
        }
        return resolvableType;
    }

    /**
     * 为包装的参数/字段构建一个 {@link TypeDescriptor} 对象。
     * @since 5.1.4
     */
    public TypeDescriptor getTypeDescriptor() {
        TypeDescriptor typeDescriptor = this.typeDescriptor;
        if (typeDescriptor == null) {
            typeDescriptor = (this.field != null ? new TypeDescriptor(getResolvableType(), getDependencyType(), getAnnotations()) : new TypeDescriptor(obtainMethodParameter()));
            this.typeDescriptor = typeDescriptor;
        }
        return typeDescriptor;
    }

    /**
     * 返回是否允许回退匹配。
     * <p>默认为 {@code false}，但可以被重写以返回 {@code true}，以此向一个
     * {@link org.springframework.beans.factory.support.AutowireCandidateResolver}
     * 表明回退匹配也是可接受的。
     * @since 4.0
     */
    public boolean fallbackMatchAllowed() {
        return false;
    }

    /**
     * 返回一个针对后备匹配的此描述符的变体。
     * @since 4.0
     * @see #fallbackMatchAllowed()
     */
    public DependencyDescriptor forFallbackMatch() {
        return new DependencyDescriptor(this) {

            @Override
            public boolean fallbackMatchAllowed() {
                return true;
            }
        };
    }

    /**
     * 初始化对底层方法参数（如果有的话）的参数名称发现。
     * <p>此方法在此点实际上并不尝试检索参数名称；它只是允许在应用程序调用
     * {@link #getDependencyName()}（如果有的话）时发生发现。
     */
    public void initParameterNameDiscovery(@Nullable ParameterNameDiscoverer parameterNameDiscoverer) {
        if (this.methodParameter != null) {
            this.methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
        }
    }

    /**
     * 确定包装参数/字段的名称。
     * @return 返回声明的名称（如果无法解析，则可能为 {@code null}）
     */
    @Nullable
    public String getDependencyName() {
        return (this.field != null ? this.field.getName() : obtainMethodParameter().getParameterName());
    }

    /**
     * 确定包装参数/字段的声明（非泛型）类型。
     * @return 返回声明的类型（绝不会为 {@code null}）
     */
    public Class<?> getDependencyType() {
        if (this.field != null) {
            if (this.nestingLevel > 1) {
                Class<?> clazz = getResolvableType().getRawClass();
                return (clazz != null ? clazz : Object.class);
            } else {
                return this.field.getType();
            }
        } else {
            return obtainMethodParameter().getNestedParameterType();
        }
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        DependencyDescriptor otherDesc = (DependencyDescriptor) other;
        return (this.required == otherDesc.required && this.eager == otherDesc.eager && this.nestingLevel == otherDesc.nestingLevel && this.containingClass == otherDesc.containingClass);
    }

    @Override
    public int hashCode() {
        return (31 * super.hashCode() + ObjectUtils.nullSafeHashCode(this.containingClass));
    }

    // 由于您只提供了一个代码注释的分隔符“---------------------------------------------------------------------”，并没有提供实际的代码注释内容，因此我无法进行翻译。请提供具体的代码注释内容，以便我能够将其翻译成中文。
    // 序列化支持
    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的Java代码注释，我将为您翻译成中文。
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // 依赖默认序列化；只需在反序列化后初始化状态。
        ois.defaultReadObject();
        // 恢复反射性句柄（不幸的是，它们不可序列化）
        try {
            if (this.fieldName != null) {
                this.field = this.declaringClass.getDeclaredField(this.fieldName);
            } else {
                if (this.methodName != null) {
                    this.methodParameter = new MethodParameter(this.declaringClass.getDeclaredMethod(this.methodName, this.parameterTypes), this.parameterIndex);
                } else {
                    this.methodParameter = new MethodParameter(this.declaringClass.getDeclaredConstructor(this.parameterTypes), this.parameterIndex);
                }
                for (int i = 1; i < this.nestingLevel; i++) {
                    this.methodParameter = this.methodParameter.nested();
                }
            }
        } catch (Throwable ex) {
            throw new IllegalStateException("Could not find original class structure", ex);
        }
    }

    /**
     * 内部类，用于避免在运行时对 Kotlin 的硬依赖。
     */
    private static class KotlinDelegate {

        /**
         * 检查指定的 {@link Field} 是否表示一个可空的 Kotlin 类型。
         */
        public static boolean isNullable(Field field) {
            KProperty<?> property = ReflectJvmMapping.getKotlinProperty(field);
            return (property != null && property.getReturnType().isMarkedNullable());
        }
    }
}
