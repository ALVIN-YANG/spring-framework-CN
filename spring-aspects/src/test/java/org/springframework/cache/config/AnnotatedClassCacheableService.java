// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.cache.config;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

/**
 * 共享的 {@code AbstractCacheAnnotationTests} 的副本：由于 Gradle 测试固定装置和 AspectJ 配置在 Gradle 构建中存在的问题而必需。
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
@Cacheable("testCache")
public class AnnotatedClassCacheableService implements CacheableService<Object> {

    private final AtomicLong counter = new AtomicLong();

    public static final AtomicLong nullInvocations = new AtomicLong();

    @Override
    public Object cache(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    public Object cacheNull(Object arg1) {
        return null;
    }

    @Override
    @Cacheable(cacheNames = "testCache", sync = true)
    public Object cacheSync(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Cacheable(cacheNames = "testCache", sync = true)
    public Object cacheSyncNull(Object arg1) {
        return null;
    }

    @Override
    public Object conditional(int field) {
        return null;
    }

    @Override
    public Object conditionalSync(int field) {
        return null;
    }

    @Override
    @Cacheable(cacheNames = "testCache", unless = "#result > 10")
    public Object unless(int arg) {
        return arg;
    }

    @Override
    @CacheEvict(cacheNames = "testCache", key = "#p0")
    public void evict(Object arg1, Object arg2) {
    }

    @Override
    @CacheEvict("testCache")
    public void evictWithException(Object arg1) {
        throw new RuntimeException("exception thrown - evict should NOT occur");
    }

    @Override
    @CacheEvict(cacheNames = "testCache", beforeInvocation = true)
    public void evictEarly(Object arg1) {
        throw new RuntimeException("exception thrown - evict should still occur");
    }

    @Override
    @CacheEvict(cacheNames = "testCache", allEntries = true)
    public void evictAll(Object arg1) {
    }

    @Override
    @CacheEvict(cacheNames = "testCache", allEntries = true, beforeInvocation = true)
    public void evictAllEarly(Object arg1) {
        throw new RuntimeException("exception thrown - evict should still occur");
    }

    @Override
    @Cacheable(cacheNames = "testCache", key = "#p0")
    public Object key(Object arg1, Object arg2) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Cacheable("testCache")
    public Object varArgsKey(Object... args) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Cacheable(cacheNames = "testCache", key = "#root.methodName + #root.caches[0].name")
    public Object name(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Cacheable(cacheNames = "testCache", key = "#root.methodName + #root.method.name + #root.targetClass + #root.target")
    public Object rootVars(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Cacheable(cacheNames = "testCache", keyGenerator = "customKyeGenerator")
    public Object customKeyGenerator(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Cacheable(cacheNames = "testCache", keyGenerator = "unknownBeanName")
    public Object unknownCustomKeyGenerator(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Cacheable(cacheNames = "testCache", cacheManager = "customCacheManager")
    public Object customCacheManager(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Cacheable(cacheNames = "testCache", cacheManager = "unknownBeanName")
    public Object unknownCustomCacheManager(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @CachePut("testCache")
    public Object update(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @CachePut(cacheNames = "testCache", condition = "#arg.equals(3)")
    public Object conditionalUpdate(Object arg) {
        return arg;
    }

    @Override
    public Object nullValue(Object arg1) {
        nullInvocations.incrementAndGet();
        return null;
    }

    @Override
    public Number nullInvocations() {
        return nullInvocations.get();
    }

    @Override
    public Long throwChecked(Object arg1) throws Exception {
        throw new IOException(arg1.toString());
    }

    @Override
    public Long throwUnchecked(Object arg1) {
        throw new UnsupportedOperationException(arg1.toString());
    }

    @Override
    @Cacheable(cacheNames = "testCache", sync = true)
    public Object throwCheckedSync(Object arg1) throws Exception {
        throw new IOException(arg1.toString());
    }

    @Override
    @Cacheable(cacheNames = "testCache", sync = true)
    public Object throwUncheckedSync(Object arg1) {
        throw new UnsupportedOperationException(arg1.toString());
    }

    // 多注解
    @Override
    @Caching(cacheable = { @Cacheable("primary"), @Cacheable("secondary") })
    public Object multiCache(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Caching(evict = { @CacheEvict("primary"), @CacheEvict(cacheNames = "secondary", key = "#a0"), @CacheEvict(cacheNames = "primary", key = "#p0 + 'A'") })
    public Object multiEvict(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Caching(cacheable = { @Cacheable(cacheNames = "primary", key = "#root.methodName") }, evict = { @CacheEvict("secondary") })
    public Object multiCacheAndEvict(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Caching(cacheable = { @Cacheable(cacheNames = "primary", condition = "#a0 == 3") }, evict = { @CacheEvict("secondary") })
    public Object multiConditionalCacheAndEvict(Object arg1) {
        return this.counter.getAndIncrement();
    }

    @Override
    @Caching(put = { @CachePut("primary"), @CachePut("secondary") })
    public Object multiUpdate(Object arg1) {
        return arg1;
    }

    @Override
    @CachePut(cacheNames = "primary", key = "#result.id")
    public TestEntity putRefersToResult(TestEntity arg1) {
        arg1.setId(Long.MIN_VALUE);
        return arg1;
    }
}
