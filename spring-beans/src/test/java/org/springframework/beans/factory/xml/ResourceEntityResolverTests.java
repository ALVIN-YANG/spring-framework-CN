// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途适用性的保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.InputSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

/**
 * ResourceEntityResolver 的单元测试。
 *
 * @author Simon Baslé
 * @author Sam Brannen
 * @since 6.0.4
 */
class ResourceEntityResolverTests {

    @ParameterizedTest
    @ValueSource(strings = { "https://example.org/schema/", "https://example.org/schema.xml" })
    void resolveEntityDoesNotCallFallbackIfNotSchema(String systemId) throws Exception {
        ConfigurableFallbackEntityResolver resolver = new ConfigurableFallbackEntityResolver(true);
        assertThat(resolver.resolveEntity("testPublicId", systemId)).isNull();
        assertThat(resolver.fallbackInvoked).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.org/schema.dtd", "https://example.org/schema.xsd" })
    void resolveEntityCallsFallbackThatReturnsNull(String systemId) throws Exception {
        ConfigurableFallbackEntityResolver resolver = new ConfigurableFallbackEntityResolver(null);
        assertThat(resolver.resolveEntity("testPublicId", systemId)).isNull();
        assertThat(resolver.fallbackInvoked).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.org/schema.dtd", "https://example.org/schema.xsd" })
    void resolveEntityCallsFallbackThatThrowsException(String systemId) {
        ConfigurableFallbackEntityResolver resolver = new ConfigurableFallbackEntityResolver(true);
        assertThatExceptionOfType(ResolutionRejectedException.class).isThrownBy(() -> resolver.resolveEntity("testPublicId", systemId));
        assertThat(resolver.fallbackInvoked).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.org/schema.dtd", "https://example.org/schema.xsd" })
    void resolveEntityCallsFallbackThatReturnsInputSource(String systemId) throws Exception {
        InputSource expected = mock();
        ConfigurableFallbackEntityResolver resolver = new ConfigurableFallbackEntityResolver(expected);
        assertThat(resolver.resolveEntity("testPublicId", systemId)).isSameAs(expected);
        assertThat(resolver.fallbackInvoked).isTrue();
    }

    private static final class NoOpResourceLoader implements ResourceLoader {

        @Override
        public Resource getResource(String location) {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return ResourceEntityResolverTests.class.getClassLoader();
        }
    }

    private static class ConfigurableFallbackEntityResolver extends ResourceEntityResolver {

        private final boolean shouldThrow;

        @Nullable
        private final InputSource returnValue;

        boolean fallbackInvoked = false;

        private ConfigurableFallbackEntityResolver(boolean shouldThrow) {
            super(new NoOpResourceLoader());
            this.shouldThrow = shouldThrow;
            this.returnValue = null;
        }

        private ConfigurableFallbackEntityResolver(@Nullable InputSource returnValue) {
            super(new NoOpResourceLoader());
            this.shouldThrow = false;
            this.returnValue = returnValue;
        }

        @Override
        @Nullable
        protected InputSource resolveSchemaEntity(String publicId, String systemId) {
            this.fallbackInvoked = true;
            if (this.shouldThrow) {
                throw new ResolutionRejectedException();
            }
            return this.returnValue;
        }
    }

    @SuppressWarnings("serial")
    static class ResolutionRejectedException extends RuntimeException {
    }
}
