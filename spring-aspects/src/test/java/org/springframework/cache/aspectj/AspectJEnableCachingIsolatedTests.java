// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.cache.aspectj;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.testfixture.cache.CacheTestUtils;
import org.springframework.context.testfixture.cache.SomeCustomKeyGenerator;
import org.springframework.context.testfixture.cache.SomeKeyGenerator;
import org.springframework.context.testfixture.cache.beans.AnnotatedClassCacheableService;
import org.springframework.context.testfixture.cache.beans.CacheableService;
import org.springframework.context.testfixture.cache.beans.DefaultCacheableService;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Stephane Nicoll
 */
public class AspectJEnableCachingIsolatedTests {

    private ConfigurableApplicationContext ctx;

    private void load(Class<?>... config) {
        this.ctx = new AnnotationConfigApplicationContext(config);
    }

    @AfterEach
    public void closeContext() {
        if (this.ctx != null) {
            this.ctx.close();
        }
    }

    @Test
    public void testKeyStrategy() {
        load(EnableCachingConfig.class);
        AnnotationCacheAspect aspect = this.ctx.getBean(AnnotationCacheAspect.class);
        assertThat(aspect.getKeyGenerator()).isSameAs(this.ctx.getBean("keyGenerator", KeyGenerator.class));
    }

    @Test
    public void testCacheErrorHandler() {
        load(EnableCachingConfig.class);
        AnnotationCacheAspect aspect = this.ctx.getBean(AnnotationCacheAspect.class);
        assertThat(aspect.getErrorHandler()).isSameAs(this.ctx.getBean("errorHandler", CacheErrorHandler.class));
    }

    // --- 本地测试 -------
    @Test
    public void singleCacheManagerBean() {
        load(SingleCacheManagerConfig.class);
    }

    @Test
    public void multipleCacheManagerBeans() {
        try {
            load(MultiCacheManagerConfig.class);
        } catch (IllegalStateException ex) {
            assertThat(ex.getMessage()).contains("bean of type CacheManager");
        }
    }

    @Test
    public void multipleCacheManagerBeans_implementsCachingConfigurer() {
        // 不会抛出异常
        load(MultiCacheManagerConfigurer.class);
    }

    @Test
    public void multipleCachingConfigurers() {
        try {
            load(MultiCacheManagerConfigurer.class, EnableCachingConfig.class);
        } catch (IllegalStateException ex) {
            assertThat(ex.getMessage()).contains("implementations of CachingConfigurer");
        }
    }

    @Test
    public void noCacheManagerBeans() {
        try {
            load(EmptyConfig.class);
        } catch (IllegalStateException ex) {
            assertThat(ex.getMessage()).contains("no bean of type CacheManager");
        }
    }

    @Test
    @Disabled("AspectJ has some sort of caching that makes this one fail")
    public void emptyConfigSupport() {
        load(EmptyConfigSupportConfig.class);
        AnnotationCacheAspect aspect = this.ctx.getBean(AnnotationCacheAspect.class);
        assertThat(aspect.getCacheResolver()).isNotNull();
        assertThat(aspect.getCacheResolver().getClass()).isEqualTo(SimpleCacheResolver.class);
        assertThat(((SimpleCacheResolver) aspect.getCacheResolver()).getCacheManager()).isSameAs(this.ctx.getBean(CacheManager.class));
    }

    @Test
    public void bothSetOnlyResolverIsUsed() {
        load(FullCachingConfig.class);
        AnnotationCacheAspect aspect = this.ctx.getBean(AnnotationCacheAspect.class);
        assertThat(aspect.getCacheResolver()).isSameAs(this.ctx.getBean("cacheResolver"));
        assertThat(aspect.getKeyGenerator()).isSameAs(this.ctx.getBean("keyGenerator"));
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

    @Configuration
    @EnableCaching(mode = AdviceMode.ASPECTJ)
    static class EmptyConfig {
    }

    @Configuration
    @EnableCaching(mode = AdviceMode.ASPECTJ)
    static class SingleCacheManagerConfig {

        @Bean
        public CacheManager cm1() {
            return new NoOpCacheManager();
        }
    }

    @Configuration
    @EnableCaching(mode = AdviceMode.ASPECTJ)
    static class MultiCacheManagerConfig {

        @Bean
        public CacheManager cm1() {
            return new NoOpCacheManager();
        }

        @Bean
        public CacheManager cm2() {
            return new NoOpCacheManager();
        }
    }

    @Configuration
    @EnableCaching(mode = AdviceMode.ASPECTJ)
    static class MultiCacheManagerConfigurer implements CachingConfigurer {

        @Bean
        public CacheManager cm1() {
            return new NoOpCacheManager();
        }

        @Bean
        public CacheManager cm2() {
            return new NoOpCacheManager();
        }

        @Override
        public CacheManager cacheManager() {
            return cm1();
        }

        @Override
        public KeyGenerator keyGenerator() {
            return null;
        }
    }

    @Configuration
    @EnableCaching(mode = AdviceMode.ASPECTJ)
    static class EmptyConfigSupportConfig implements CachingConfigurer {

        @Bean
        public CacheManager cm() {
            return new NoOpCacheManager();
        }
    }

    @Configuration
    @EnableCaching(mode = AdviceMode.ASPECTJ)
    static class FullCachingConfig implements CachingConfigurer {

        @Override
        @Bean
        public CacheManager cacheManager() {
            return new NoOpCacheManager();
        }

        @Override
        @Bean
        public KeyGenerator keyGenerator() {
            return new SomeKeyGenerator();
        }

        @Override
        @Bean
        public CacheResolver cacheResolver() {
            return new NamedCacheResolver(cacheManager(), "foo");
        }
    }
}
