// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可”）许可，您可能不得使用此文件除非符合许可。
* 您可以在以下链接处获得许可副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可下分发的软件按“原样”提供，
* 不提供任何形式（明示或暗示）的保证或条件。
* 请参阅许可了解具体规定许可权限和限制。*/
package org.springframework.cache.aspectj;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.config.CacheableService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Costin Leau
 */
public class AspectJCacheAnnotationTests extends AbstractCacheAnnotationTests {

    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return new GenericXmlApplicationContext("/org/springframework/cache/config/annotation-cache-aspectj.xml");
    }

    @Test
    public void testKeyStrategy() {
        AnnotationCacheAspect aspect = ctx.getBean("org.springframework.cache.config.internalCacheAspect", AnnotationCacheAspect.class);
        assertThat(aspect.getKeyGenerator()).isSameAs(ctx.getBean("keyGenerator"));
    }

    @Override
    protected void testMultiEvict(CacheableService<?> service) {
        Object o1 = new Object();
        Object r1 = service.multiCache(o1);
        Object r2 = service.multiCache(o1);
        Cache primary = cm.getCache("primary");
        Cache secondary = cm.getCache("secondary");
        assertThat(r2).isSameAs(r1);
        assertThat(primary.get(o1).get()).isSameAs(r1);
        assertThat(secondary.get(o1).get()).isSameAs(r1);
        service.multiEvict(o1);
        assertThat(primary.get(o1)).isNull();
        assertThat(secondary.get(o1)).isNull();
        Object r3 = service.multiCache(o1);
        Object r4 = service.multiCache(o1);
        assertThat(r3).isNotSameAs(r1);
        assertThat(r4).isSameAs(r3);
        assertThat(primary.get(o1).get()).isSameAs(r3);
        assertThat(secondary.get(o1).get()).isSameAs(r4);
    }
}
