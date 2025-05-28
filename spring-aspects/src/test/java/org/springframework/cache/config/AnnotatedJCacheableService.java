// 翻译完成 glm-4-flash
/** 版权所有 2002-2015 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可协议") 许可，您可能不遵守许可协议使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按照"现状"分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或适用于特定目的的保证。
* 请参阅许可协议以了解具体规定许可权限和限制。*/
package org.springframework.cache.config;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.contextsupport.testfixture.cache.TestableCacheKeyGenerator;
import org.springframework.contextsupport.testfixture.cache.TestableCacheResolverFactory;
import org.springframework.contextsupport.testfixture.jcache.JCacheableService;

/**
 * 带有 @CacheDefaults 注解的存储库示例
 *
 * <p>注意：由于需要通过 AspectJ 编译器处理以应用所需方面，因此是从原始编译中复制粘贴的。
 *
 * @author Stephane Nicoll
 */
@CacheDefaults(cacheName = "default")
public class AnnotatedJCacheableService implements JCacheableService<Long> {

    private final AtomicLong counter = new AtomicLong();

    private final AtomicLong exceptionCounter = new AtomicLong();

    private final Cache defaultCache;

    public AnnotatedJCacheableService(Cache defaultCache) {
        this.defaultCache = defaultCache;
    }

    @Override
    @CacheResult
    public Long cache(String id) {
        return counter.getAndIncrement();
    }

    @Override
    @CacheResult
    public Long cacheNull(String id) {
        return null;
    }

    @Override
    @CacheResult(exceptionCacheName = "exception", nonCachedExceptions = NullPointerException.class)
    public Long cacheWithException(@CacheKey String id, boolean matchFilter) {
        throwException(matchFilter);
        // 永不到达
        return 0L;
    }

    @Override
    @CacheResult(exceptionCacheName = "exception", nonCachedExceptions = NullPointerException.class)
    public Long cacheWithCheckedException(@CacheKey String id, boolean matchFilter) throws IOException {
        throwCheckedException(matchFilter);
        // 无法到达
        return 0L;
    }

    @Override
    @CacheResult(skipGet = true)
    public Long cacheAlwaysInvoke(String id) {
        return counter.getAndIncrement();
    }

    @Override
    @CacheResult
    public Long cacheWithPartialKey(@CacheKey String id, boolean notUsed) {
        return counter.getAndIncrement();
    }

    @Override
    @CacheResult(cacheResolverFactory = TestableCacheResolverFactory.class)
    public Long cacheWithCustomCacheResolver(String id) {
        return counter.getAndIncrement();
    }

    @Override
    @CacheResult(cacheKeyGenerator = TestableCacheKeyGenerator.class)
    public Long cacheWithCustomKeyGenerator(String id, String anotherId) {
        return counter.getAndIncrement();
    }

    @Override
    @CachePut
    public void put(String id, @CacheValue Object value) {
    }

    @Override
    @CachePut(cacheFor = UnsupportedOperationException.class)
    public void putWithException(@CacheKey String id, @CacheValue Object value, boolean matchFilter) {
        throwException(matchFilter);
    }

    @Override
    @CachePut(afterInvocation = false)
    public void earlyPut(String id, @CacheValue Object value) {
        Object key = SimpleKeyGenerator.generateKey(id);
        Cache.ValueWrapper valueWrapper = defaultCache.get(key);
        if (valueWrapper == null) {
            throw new AssertionError("Excepted value to be put in cache with key " + key);
        }
        Object actual = valueWrapper.get();
        if (value != actual) {
            // 实例检查有意为之
            throw new AssertionError("Wrong value set in cache with key " + key + ". " + "Expected=" + value + ", but got=" + actual);
        }
    }

    @Override
    @CachePut(afterInvocation = false)
    public void earlyPutWithException(@CacheKey String id, @CacheValue Object value, boolean matchFilter) {
        throwException(matchFilter);
    }

    @Override
    @CacheRemove
    public void remove(String id) {
    }

    @Override
    @CacheRemove(noEvictFor = NullPointerException.class)
    public void removeWithException(@CacheKey String id, boolean matchFilter) {
        throwException(matchFilter);
    }

    @Override
    @CacheRemove(afterInvocation = false)
    public void earlyRemove(String id) {
        Object key = SimpleKeyGenerator.generateKey(id);
        Cache.ValueWrapper valueWrapper = defaultCache.get(key);
        if (valueWrapper != null) {
            throw new AssertionError("Value with key " + key + " expected to be already remove from cache");
        }
    }

    @Override
    @CacheRemove(afterInvocation = false, evictFor = UnsupportedOperationException.class)
    public void earlyRemoveWithException(@CacheKey String id, boolean matchFilter) {
        throwException(matchFilter);
    }

    @Override
    @CacheRemoveAll
    public void removeAll() {
    }

    @Override
    @CacheRemoveAll(noEvictFor = NullPointerException.class)
    public void removeAllWithException(boolean matchFilter) {
        throwException(matchFilter);
    }

    @Override
    @CacheRemoveAll(afterInvocation = false)
    public void earlyRemoveAll() {
        ConcurrentHashMap<?, ?> nativeCache = (ConcurrentHashMap<?, ?>) defaultCache.getNativeCache();
        if (!nativeCache.isEmpty()) {
            throw new AssertionError("Cache was expected to be empty");
        }
    }

    @Override
    @CacheRemoveAll(afterInvocation = false, evictFor = UnsupportedOperationException.class)
    public void earlyRemoveAllWithException(boolean matchFilter) {
        throwException(matchFilter);
    }

    @Deprecated
    public void noAnnotation() {
    }

    @Override
    public long exceptionInvocations() {
        return exceptionCounter.get();
    }

    private void throwException(boolean matchFilter) {
        long count = exceptionCounter.getAndIncrement();
        if (matchFilter) {
            throw new UnsupportedOperationException("Expected exception (" + count + ")");
        } else {
            throw new NullPointerException("Expected exception (" + count + ")");
        }
    }

    private void throwCheckedException(boolean matchFilter) throws IOException {
        long count = exceptionCounter.getAndIncrement();
        if (matchFilter) {
            throw new IOException("Expected exception (" + count + ")");
        } else {
            throw new NullPointerException("Expected exception (" + count + ")");
        }
    }
}
