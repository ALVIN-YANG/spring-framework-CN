// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下地址获取许可协议副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议，了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 对 {@link DelegatingEntityResolver} 类的单元测试。
 *
 * @author Rick Evans
 * @author Chris Beams
 */
public class DelegatingEntityResolverTests {

    @Test
    public void testCtorWhereDtdEntityResolverIsNull() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new DelegatingEntityResolver(null, new NoOpEntityResolver()));
    }

    @Test
    public void testCtorWhereSchemaEntityResolverIsNull() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new DelegatingEntityResolver(new NoOpEntityResolver(), null));
    }

    @Test
    public void testCtorWhereEntityResolversAreBothNull() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new DelegatingEntityResolver(null, null));
    }

    private static final class NoOpEntityResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            return null;
        }
    }
}
