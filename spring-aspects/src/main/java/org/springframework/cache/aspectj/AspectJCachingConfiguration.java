// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.cache.aspectj;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.cache.config.CacheManagementConfigUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * 使用 {@code @Configuration} 注解的类，用于注册启用基于 AspectJ 的注解驱动缓存管理所需的 Spring 基础设施 Bean。
 *
 * @author Chris Beams
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 3.1
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.cache.annotation.CachingConfigurationSelector
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AspectJCachingConfiguration extends AbstractCachingConfiguration {

    @Bean(name = CacheManagementConfigUtils.CACHE_ASPECT_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public AnnotationCacheAspect cacheAspect() {
        AnnotationCacheAspect cacheAspect = AnnotationCacheAspect.aspectOf();
        cacheAspect.configure(this.errorHandler, this.keyGenerator, this.cacheResolver, this.cacheManager);
        return cacheAspect;
    }
}
