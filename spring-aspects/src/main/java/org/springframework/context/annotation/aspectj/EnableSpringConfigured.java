// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.context.annotation.aspectj;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 *  通知当前应用程序上下文对在 Spring 容器外部实例化的非托管类应用依赖注入
 * （通常是指带有
 *  {@link org.springframework.beans.factory.annotation.Configurable @Configurable}
 *  注解的类）。
 *
 *  <p>类似于 Spring 的
 *  {@code <context:spring-configured>} XML 元素的功能。通常与
 *  {@link org.springframework.context.annotation.EnableLoadTimeWeaving @EnableLoadTimeWeaving}
 *  一起使用。
 *
 *  @author Chris Beams
 *  @since 3.1
 *  @see org.springframework.context.annotation.EnableLoadTimeWeaving
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SpringConfiguredConfiguration.class)
public @interface EnableSpringConfigured {
}
