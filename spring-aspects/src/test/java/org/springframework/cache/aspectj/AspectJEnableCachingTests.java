// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.cache.aspectj;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.config.AnnotatedClassCacheableService;
import org.springframework.cache.config.CacheableService;
import org.springframework.cache.config.DefaultCacheableService;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.testfixture.cache.CacheTestUtils;
import org.springframework.context.testfixture.cache.SomeCustomKeyGenerator;
import org.springframework.context.testfixture.cache.SomeKeyGenerator;

/**
 * @作者 Stephane Nicoll
 */
public class AspectJEnableCachingTests extends AbstractCacheAnnotationTests {

    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return new AnnotationConfigApplicationContext(EnableCachingConfig.class);
    }

    @Configuration
    @EnableCaching(mode = AdviceMode.ASPECTJ)
    static class EnableCachingConfig implements CachingConfigurer {

        @Override
        @Bean
        public CacheManager cacheManager() {
            return CacheTestUtils.createSimpleCacheManager("testCache", "primary", "secondary");
        }

        @Bean
        public CacheableService<?> service() {
            return new DefaultCacheableService();
        }

        @Bean
        public CacheableService<?> classService() {
            return new AnnotatedClassCacheableService();
        }

        @Override
        @Bean
        public KeyGenerator keyGenerator() {
            return new SomeKeyGenerator();
        }

        @Override
        @Bean
        public CacheErrorHandler errorHandler() {
            return new SimpleCacheErrorHandler();
        }

        @Bean
        public KeyGenerator customKeyGenerator() {
            return new SomeCustomKeyGenerator();
        }

        @Bean
        public CacheManager customCacheManager() {
            return CacheTestUtils.createSimpleCacheManager("testCache");
        }
    }
}
