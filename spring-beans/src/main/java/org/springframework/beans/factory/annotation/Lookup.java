// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，
* 在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  表示“查找”方法的注解，由容器重写以将它们重定向回 {@link org.springframework.beans.factory.BeanFactory} 进行 {@code getBean} 调用。这本质上是一个基于注解的 XML 中 {@code lookup-method} 属性版本，导致相同的运行时安排。
 *
 * <p>目标 Bean 的解析可以基于返回类型（{@code getBean(Class)}）或基于建议的 Bean 名称（{@code getBean(String)}），在两种情况下都将方法的参数传递给 {@code getBean} 调用，以将它们应用于目标工厂方法参数或构造函数参数。
 *
 * <p>这样的查找方法可以有默认（占位符）实现，这些实现将被容器替换，或者它们可以声明为抽象的——由容器在运行时填充。在两种情况下，容器将通过 CGLIB 生成方法所在类的运行时子类，这就是为什么这样的查找方法只能用于容器通过常规构造函数实例化的 Bean：即查找方法不能替换从工厂方法返回的 Bean，因为在这些情况下我们不能为它们动态提供一个子类。
 *
 * <p><b>典型 Spring 配置场景的建议：</b>当在特定场景下可能需要具体类时，请考虑提供查找方法的占位符实现。并且请记住，查找方法不会在配置类中从 {@code @Bean} 方法返回的 Bean 上工作；您将不得不求助于 {@code @Inject Provider<TargetBean>} 或类似的方法。
 *
 * @since 4.1
 * @see org.springframework.beans.factory.BeanFactory#getBean(Class, Object...)
 * @see org.springframework.beans.factory.BeanFactory#getBean(String, Object...)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lookup {

    /**
     * 此注解属性可能建议一个目标 Bean 名称以进行查找。
     * 如果未指定，则将根据注解方法的返回类型声明来解析目标 Bean。
     */
    String value() default "";
}
