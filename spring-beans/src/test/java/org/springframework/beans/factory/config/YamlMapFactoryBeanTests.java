// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可；
* 除非遵守许可协议，否则不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件
* 是按“现状”提供的，不提供任何形式的明示或暗示保证。
* 请参阅许可协议了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * 测试 {@link YamlMapFactoryBean}。
 *
 * @author Dave Syer
 * @author Juergen Hoeller
 */
public class YamlMapFactoryBeanTests {

    private final YamlMapFactoryBean factory = new YamlMapFactoryBean();

    @Test
    public void testSetIgnoreResourceNotFound() {
        this.factory.setResolutionMethod(YamlMapFactoryBean.ResolutionMethod.OVERRIDE_AND_IGNORE);
        this.factory.setResources(new FileSystemResource("non-exsitent-file.yml"));
        assertThat(this.factory.getObject()).isEmpty();
    }

    @Test
    public void testSetBarfOnResourceNotFound() {
        assertThatIllegalStateException().isThrownBy(() -> {
            this.factory.setResources(new FileSystemResource("non-exsitent-file.yml"));
            this.factory.getObject().size();
        });
    }

    @Test
    public void testGetObject() {
        this.factory.setResources(new ByteArrayResource("foo: bar".getBytes()));
        assertThat(this.factory.getObject()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOverrideAndRemoveDefaults() {
        this.factory.setResources(new ByteArrayResource("foo:\n  bar: spam".getBytes()), new ByteArrayResource("foo:\n  spam: bar".getBytes()));
        assertThat(this.factory.getObject()).hasSize(1);
        assertThat(((Map<String, Object>) this.factory.getObject().get("foo"))).hasSize(2);
    }

    @Test
    public void testFirstFound() {
        this.factory.setResolutionMethod(YamlProcessor.ResolutionMethod.FIRST_FOUND);
        this.factory.setResources(new AbstractResource() {

            @Override
            public String getDescription() {
                return "non-existent";
            }

            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("planned");
            }
        }, new ByteArrayResource("foo:\n  spam: bar".getBytes()));
        assertThat(this.factory.getObject()).hasSize(1);
    }

    @Test
    public void testMapWithPeriodsInKey() {
        this.factory.setResources(new ByteArrayResource("foo:\n  ? key1.key2\n  : value".getBytes()));
        Map<String, Object> map = this.factory.getObject();
        assertThat(map).hasSize(1);
        assertThat(map.containsKey("foo")).isTrue();
        Object object = map.get("foo");
        assertThat(object instanceof LinkedHashMap).isTrue();
        @SuppressWarnings("unchecked")
        Map<String, Object> sub = (Map<String, Object>) object;
        assertThat(sub.containsKey("key1.key2")).isTrue();
        assertThat(sub.get("key1.key2")).isEqualTo("value");
    }

    @Test
    public void testMapWithIntegerValue() {
        this.factory.setResources(new ByteArrayResource("foo:\n  ? key1.key2\n  : 3".getBytes()));
        Map<String, Object> map = this.factory.getObject();
        assertThat(map).hasSize(1);
        assertThat(map.containsKey("foo")).isTrue();
        Object object = map.get("foo");
        assertThat(object instanceof LinkedHashMap).isTrue();
        @SuppressWarnings("unchecked")
        Map<String, Object> sub = (Map<String, Object>) object;
        assertThat(sub).hasSize(1);
        assertThat(sub.get("key1.key2")).isEqualTo(3);
    }

    @Test
    public void testDuplicateKey() {
        this.factory.setResources(new ByteArrayResource("mymap:\n  foo: bar\nmymap:\n  bar: foo".getBytes()));
        assertThatExceptionOfType(DuplicateKeyException.class).isThrownBy(() -> this.factory.getObject().get("mymap"));
    }
}
