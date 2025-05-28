// 翻译完成 glm-4-flash
/** 版权所有 2002-2014 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 您不得使用此文件除非符合许可证要求。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式的明示或暗示保证，
* 包括但不限于适销性、特定用途适用性和非侵权性。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.cache.aspectj;

import java.util.Arrays;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.config.AnnotatedJCacheableService;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.contextsupport.testfixture.jcache.AbstractJCacheAnnotationTests;

/**
 * @作者 Stephane Nicoll
 */
public class JCacheAspectJJavaConfigTests extends AbstractJCacheAnnotationTests {

    @Override
    protected ApplicationContext getApplicationContext() {
        return new AnnotationConfigApplicationContext(EnableCachingConfig.class);
    }

    @Configuration
    @EnableCaching(mode = AdviceMode.ASPECTJ)
    public static class EnableCachingConfig {

        @Bean
        public CacheManager cacheManager() {
            SimpleCacheManager cm = new SimpleCacheManager();
            cm.setCaches(Arrays.asList(defaultCache(), new ConcurrentMapCache("primary"), new ConcurrentMapCache("secondary"), new ConcurrentMapCache("exception")));
            return cm;
        }

        @Bean
        public AnnotatedJCacheableService cacheableService() {
            return new AnnotatedJCacheableService(defaultCache());
        }

        @Bean
        public Cache defaultCache() {
            return new ConcurrentMapCache("default");
        }
    }
}
