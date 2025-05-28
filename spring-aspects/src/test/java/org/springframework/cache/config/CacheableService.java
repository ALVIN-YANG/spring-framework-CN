// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证，了解管理许可权和限制的具体语言。*/
package org.springframework.cache.config;

/**
 * 共享的{@code CacheableService}的副本：由于Gradle测试固定装置和Gradle构建中AspectJ配置的问题而必需。
 *
 * <p>缓存测试的基本服务接口。
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
public interface CacheableService<T> {

    T cache(Object arg1);

    T cacheNull(Object arg1);

    T cacheSync(Object arg1);

    T cacheSyncNull(Object arg1);

    void evict(Object arg1, Object arg2);

    void evictWithException(Object arg1);

    void evictEarly(Object arg1);

    void evictAll(Object arg1);

    void evictAllEarly(Object arg1);

    T conditional(int field);

    T conditionalSync(int field);

    T unless(int arg);

    T key(Object arg1, Object arg2);

    T varArgsKey(Object... args);

    T name(Object arg1);

    T nullValue(Object arg1);

    T update(Object arg1);

    T conditionalUpdate(Object arg2);

    Number nullInvocations();

    T rootVars(Object arg1);

    T customKeyGenerator(Object arg1);

    T unknownCustomKeyGenerator(Object arg1);

    T customCacheManager(Object arg1);

    T unknownCustomCacheManager(Object arg1);

    T throwChecked(Object arg1) throws Exception;

    T throwUnchecked(Object arg1);

    T throwCheckedSync(Object arg1) throws Exception;

    T throwUncheckedSync(Object arg1);

    T multiCache(Object arg1);

    T multiEvict(Object arg1);

    T multiCacheAndEvict(Object arg1);

    T multiConditionalCacheAndEvict(Object arg1);

    T multiUpdate(Object arg1);

    TestEntity putRefersToResult(TestEntity arg1);
}
