// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  标记构造函数、字段、setter 方法或配置方法，以便由 Spring 的依赖注入设施自动装配。这是 JSR-330 的 {@link jakarta.inject.Inject} 注解的替代方案，增加了必需与可选的语义。
 *
 * <h3>自动装配构造函数</h3>
 * <p>任何给定的 bean 类只能有一个构造函数可以声明此注解，并设置 {@link #required} 属性为 {@code true}，指示当用作 Spring bean 时自动装配的构造函数。此外，如果将 {@code required} 属性设置为 {@code true}，则只能有一个构造函数可以注解为 {@code @Autowired}。如果有多个非必需的构造函数声明了此注解，它们将被视为自动装配的候选者。具有最多依赖关系的构造函数将被选择，这些依赖关系可以通过匹配 Spring 容器中的 bean 来满足。如果所有候选者都无法满足，则将使用主要/默认构造函数（如果存在）。同样，如果一个类声明了多个构造函数，但没有一个注解为 {@code @Autowired}，则将使用主要/默认构造函数（如果存在）。如果一个类最初只声明了一个构造函数，它将始终被使用，即使没有注解。注解的构造函数不必是公共的。
 *
 * <h3>自动装配字段</h3>
 * <p>字段在 bean 构造之后立即注入，在调用任何配置方法之前。此类配置字段不必是公共的。
 *
 * <h3>自动装配方法</h3>
 * <p>配置方法可以具有任意名称和任意数量的参数；这些参数中的每一个都将与 Spring 容器中的匹配 bean 自动装配。bean 属性 setter 方法实际上是此类通用配置方法的一个特例。此类配置方法不必是公共的。
 *
 * <h3>自动装配参数</h3>
 * <p>虽然从 Spring Framework 5.0 开始，技术上可以在单个方法或构造函数参数上声明 {@code @Autowired}，但框架的大部分部分都忽略了此类声明。唯一积极支持自动装配参数的核心 Spring 框架部分是位于 {@code spring-test} 模块中的 JUnit Jupiter 支持（有关详细信息，请参阅 <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-junit-jupiter-di">TestContext 框架</a> 参考文档）。
 *
 * <h3>多个参数和 'required' 语义</h3>
 * <p>在多参数构造函数或方法的情况下，{@link #required} 属性适用于所有参数。单个参数可以声明为 Java-8 风格的 {@link java.util.Optional}，或者从 Spring Framework 5.0 开始，也可以声明为 {@code @Nullable} 或 Kotlin 中的非空参数类型，以覆盖基本 'required' 语义。
 *
 * <h3>自动装配数组、集合和映射</h3>
 * <p>在数组、{@link java.util.Collection} 或 {@link java.util.Map} 依赖类型的情况下，容器自动装配所有与声明值类型匹配的 bean。为此目的，映射键必须声明为类型 {@code String}，它将被解析为相应的 bean 名称。此类由容器提供的集合将是有序的，考虑到目标组件的 {@link org.springframework.core.Ordered Ordered} 和 {@link org.springframework.core.annotation.Order @Order} 值，否则遵循容器中的注册顺序。或者，单个匹配的目标 bean 也可能是一个通用的类型化的 {@code Collection} 或 {@code Map}，它将被注入为这样的类型。
 *
 * <h3>在 {@code BeanPostProcessor} 或 {@code BeanFactoryPostProcessor} 中不支持</h3>
 * <p>请注意，实际的注入是通过一个 {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor} 实现的，这意味着您不能使用 {@code @Autowired} 向 {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor} 或 {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor} 类型注入引用。请参阅 {@link AutowiredAnnotationBeanPostProcessor} 类的 javadoc（默认情况下，该类检查此注解的存在）。
 *
 * @作者 Juergen Hoeller
 * @作者 Mark Fisher
 * @作者 Sam Brannen
 * @自 2.5 版本以来
 * @see AutowiredAnnotationBeanPostProcessor
 * @see Qualifier
 * @see Value
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

    /**
     * 声明被注解的依赖项是否必需。
     * <p>默认为 {@code true}。
     */
    boolean required() default true;
}
