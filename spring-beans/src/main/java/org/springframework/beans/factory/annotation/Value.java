// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（"许可证"）授权；
* 除非根据法律要求或书面同意，否则您不得使用此文件，除非符合许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按"现状"提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、 merchantability 或对特定目的的适用性。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于字段或方法/构造函数参数级别的注解，表示被注解元素默认值表达式的注解。
 *
 * <p>通常用于表达式驱动或属性驱动的依赖注入。也支持动态解析处理器方法参数 - 例如，在Spring MVC中。
 *
 * <p>一个常见的用例是使用类似 <code>#{systemProperties.myProp}</code> 风格的SpEL（Spring表达式语言）表达式来注入值。或者，可以使用类似 <code>${my.app.myProp}</code> 风格的属性占位符来注入值。
 *
 * <p>请注意，实际的 {@code @Value} 注解处理是由一个 {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor} 执行的，这意味着你 <em>不能</em> 在
 * {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor} 或
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor} 类型中使用 {@code @Value}。请参阅 {@link AutowiredAnnotationBeanPostProcessor} 类的javadoc（默认情况下，该类会检查此注解的存在）。
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see AutowiredAnnotationBeanPostProcessor
 * @see Autowired
 * @see org.springframework.beans.factory.config.BeanExpressionResolver
 * @see org.springframework.beans.factory.support.AutowireCandidateResolver#getSuggestedValue
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {

    /**
     * 实际的值表达式，例如 <code>#{systemProperties.myProp}</code>
     * 或者属性占位符，例如 <code>${my.app.myProp}</code>.
     */
    String value();
}
