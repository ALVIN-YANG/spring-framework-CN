// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，除非法律要求或经书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件按“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议，了解具体规定许可权限和限制的条款。*/
package org.springframework.context.annotation.aspectj;

import org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * 一个使用 {@code @Configuration} 注解的类，该类注册了一个能够为使用 @{@link org.springframework.beans.factory.annotation.Configurable Configurable} 注解的、非 Spring 管理对象执行依赖注入服务的 {@code AnnotationBeanConfigurerAspect}。
 *
 * <p>当使用 {@link EnableSpringConfigured @EnableSpringConfigured} 注解时，此配置类会自动导入。有关完整使用详情，请参阅 {@code @EnableSpringConfigured} 的 javadoc。
 *
 * @author Chris Beams
 * @since 3.1
 * @see EnableSpringConfigured
 */
@Configuration
public class SpringConfiguredConfiguration {

    /**
     * 用于配置器切面的Bean名称。
     */
    public static final String BEAN_CONFIGURER_ASPECT_BEAN_NAME = "org.springframework.context.config.internalBeanConfigurerAspect";

    @Bean(name = BEAN_CONFIGURER_ASPECT_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public AnnotationBeanConfigurerAspect beanConfigurerAspect() {
        return AnnotationBeanConfigurerAspect.aspectOf();
    }
}
