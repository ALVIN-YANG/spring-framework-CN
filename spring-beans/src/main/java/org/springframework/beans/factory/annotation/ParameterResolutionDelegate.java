// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 您只能在不违反许可证的情况下使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件
* 是按“现状”分发的，不提供任何形式（明示或暗示）的保证或条件。
* 请参阅许可证以了解具体的管理权限和限制。*/
package org.springframework.beans.factory.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 公共委托，用于在外部管理的构造器和方法上解析可自动装配的参数。
 *
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 5.2
 * @see #isAutowirable
 * @see #resolveDependency
 */
public final class ParameterResolutionDelegate {

    private static final AnnotatedElement EMPTY_ANNOTATED_ELEMENT = new AnnotatedElement() {

        @Override
        @Nullable
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }
    };

    private ParameterResolutionDelegate() {
    }

    /**
     * 判断提供的 {@link Parameter} 是否 <em>可能</em> 可以由一个 {@link AutowireCapableBeanFactory} 进行自动装配。
     * <p>如果提供的参数被注解或元注解为 {@link Autowired @Autowired}、{@link Qualifier @Qualifier} 或 {@link Value @Value}，则返回 {@code true}。
     * <p>注意，即使此方法返回 {@code false}，`#resolveDependency` 仍然可能解决提供的参数的依赖关系。
     * @param parameter 应该自动装配其依赖的参数（不能为 {@code null}）
     * @param parameterIndex 声明参数的构造函数或方法中参数的索引
     * @see #resolveDependency
     */
    public static boolean isAutowirable(Parameter parameter, int parameterIndex) {
        Assert.notNull(parameter, "Parameter must not be null");
        AnnotatedElement annotatedParameter = getEffectiveAnnotatedParameter(parameter, parameterIndex);
        return (AnnotatedElementUtils.hasAnnotation(annotatedParameter, Autowired.class) || AnnotatedElementUtils.hasAnnotation(annotatedParameter, Qualifier.class) || AnnotatedElementUtils.hasAnnotation(annotatedParameter, Value.class));
    }

    /**
     * 从提供的 {@link Parameter} 中解析所需的依赖关系，并从提供的 {@link AutowireCapableBeanFactory} 中获取。
     * 提供与 Spring 对自动装配的字段和方法依赖注入功能相当的综合自动装配支持，包括对 {@link Autowired @Autowired}、
     * {@link Qualifier @Qualifier} 和 {@link Value @Value} 的支持，其中支持在 {@code @Value} 声明中的属性占位符和 SpEL 表达式。
     * 如果参数没有被注解或元注解为具有 {@link Autowired @Autowired} 注解且设置了 {@link Autowired#required required} 标志为 `false`，则依赖关系是必需的。
     * 如果未声明显式的 <em>qualifier</em>，则将使用参数的名称作为解决歧义时的限定符。
     * @param parameter 应解析其依赖关系的参数（不能为 `null`）
     * @param parameterIndex 参数在声明该参数的构造函数或方法中的索引
     * @param containingClass 包含参数的具体类；这可能不同于声明参数的类，因为它可能是该类的子类，可能替换类型变量（不能为 `null`）
     * @param beanFactory 从中解析依赖关系的 `AutowireCapableBeanFactory`（不能为 `null`）
     * @return 解析出的对象，如果没有找到则为 `null`
     * @throws BeansException 如果依赖关系解析失败
     * @see #isAutowirable
     * @see Autowired#required
     * @see SynthesizingMethodParameter#forExecutable(Executable, int)
     * @see AutowireCapableBeanFactory#resolveDependency(DependencyDescriptor, String)
     */
    @Nullable
    public static Object resolveDependency(Parameter parameter, int parameterIndex, Class<?> containingClass, AutowireCapableBeanFactory beanFactory) throws BeansException {
        Assert.notNull(parameter, "Parameter must not be null");
        Assert.notNull(containingClass, "Containing class must not be null");
        Assert.notNull(beanFactory, "AutowireCapableBeanFactory must not be null");
        AnnotatedElement annotatedParameter = getEffectiveAnnotatedParameter(parameter, parameterIndex);
        Autowired autowired = AnnotatedElementUtils.findMergedAnnotation(annotatedParameter, Autowired.class);
        boolean required = (autowired == null || autowired.required());
        MethodParameter methodParameter = SynthesizingMethodParameter.forExecutable(parameter.getDeclaringExecutable(), parameterIndex);
        DependencyDescriptor descriptor = new DependencyDescriptor(methodParameter, required);
        descriptor.setContainingClass(containingClass);
        return beanFactory.resolveDependency(descriptor, null);
    }

    /**
     * 由于 JDK 9 之前版本的 javac 存在一个 bug，直接在 {@link Parameter} 上查找注解会导致内部类构造函数失败。
     * <p>注意：由于 Spring 6 可能还会遇到使用 javac 8 编译的用户代码，因此这个临时解决方案仍然保留。
     * <h4>JDK < 9 中的 javac bug</h4>
     * <p>编译的字节码中的参数注解数组排除了内部类构造函数的隐式 <em>封装实例</em> 参数的条目。
     * <h4>解决方案</h4>
     * <p>此方法通过允许调用者访问前一个 {@link Parameter} 对象上的注解（即，`index - 1`）来解决这个问题。如果提供的 `index` 为零，此方法返回一个空的 `AnnotatedElement`。
     * <h4>警告</h4>
     * <p>此方法返回的 `AnnotatedElement` 应该永远不要被强制转换为 `Parameter` 并作为 `Parameter` 处理，因为元数据（例如，`Parameter#getName()`、`Parameter#getType()` 等）将不会与内部类构造函数中给定索引处声明的参数匹配。
     * @return 提供的 `parameter` 或如果存在上述 bug，则返回 <em>有效的</em> `Parameter`
     */
    private static AnnotatedElement getEffectiveAnnotatedParameter(Parameter parameter, int index) {
        Executable executable = parameter.getDeclaringExecutable();
        if (executable instanceof Constructor && ClassUtils.isInnerClass(executable.getDeclaringClass()) && executable.getParameterAnnotations().length == executable.getParameterCount() - 1) {
            // Java 编译器（javac）在 JDK 9 以下版本中的缺陷：注解数组排除了封装实例参数
            // 对于内部类，因此使用实际参数索引减1来访问它
            return (index == 0 ? EMPTY_ANNOTATED_ELEMENT : executable.getParameters()[index - 1]);
        }
        return parameter;
    }
}
