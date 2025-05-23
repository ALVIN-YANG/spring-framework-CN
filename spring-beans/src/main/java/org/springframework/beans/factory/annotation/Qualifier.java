// 翻译完成 glm-4-flash
/** 版权所有 2002-2011，原作者或原作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能无法使用此文件，除非遵守许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解可用于字段或参数，作为自动装配候选bean的限定符。它也可以用来注解其他自定义注解，然后这些注解反过来可以用作限定符。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see Autowired
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Qualifier {

    String value() default "";
}
