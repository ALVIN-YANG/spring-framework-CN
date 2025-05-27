// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.cache.aspectj;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.config.CacheManagementConfigUtils;
import org.springframework.cache.jcache.config.AbstractJCacheConfiguration;
import org.springframework.cache.jcache.interceptor.JCacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * 使用 {@code @Configuration} 注解的类，该类注册了Spring基础设施Bean，这些Bean对于启用基于AspectJ的注解驱动的缓存管理（针对标准的JSR-107注解）是必要的。
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.cache.annotation.CachingConfigurationSelector
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AspectJJCacheConfiguration extends AbstractJCacheConfiguration {

    @Bean(name = CacheManagementConfigUtils.JCACHE_ASPECT_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public JCacheCacheAspect cacheAspect(JCacheOperationSource jCacheOperationSource) {
        JCacheCacheAspect cacheAspect = JCacheCacheAspect.aspectOf();
        cacheAspect.setCacheOperationSource(jCacheOperationSource);
        return cacheAspect;
    }
}
