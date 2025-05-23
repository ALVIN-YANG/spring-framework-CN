// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者们。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性或非侵权性。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个类为可被Spring驱动的配置类。
 *
 * <p>通常与AspectJ的{@code AnnotationBeanConfigurerAspect}一起使用。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Ramnivas Laddad
 * @since 2.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Configurable {

    /**
     * 作为配置模板使用的bean定义的名称。
     */
    String value() default "";

    /**
     * 是否需要通过自动装配来注入依赖项？
     */
    Autowire autowire() default Autowire.NO;

    /**
     * 是否需要对配置的对象执行依赖性检查？
     */
    boolean dependencyCheck() default false;

    /**
     * 是否在构建对象之前注入依赖？
     */
    boolean preConstruction() default false;
}
