// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可协议副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可协议下分发的软件按“原样”分发，
* 不提供任何形式（明确或暗示）的保证或条件。有关权限和限制的具体语言，请参阅许可协议。*/
package org.springframework.beans;

import org.junit.jupiter.api.Test;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 特定的 {@link DirectFieldAccessor} 测试。
 *
 * @author Jose Luis Martin
 * @author Chris Beams
 * @author Stephane Nicoll
 */
class DirectFieldAccessorTests extends AbstractPropertyAccessorTests {

    @Override
    protected DirectFieldAccessor createAccessor(Object target) {
        return new DirectFieldAccessor(target);
    }

    @Test
    void withShadowedField() {
        final StringBuilder sb = new StringBuilder();
        TestBean target = new TestBean() {

            @SuppressWarnings("unused")
            StringBuilder name = sb;
        };
        DirectFieldAccessor dfa = createAccessor(target);
        assertThat(dfa.getPropertyType("name")).isEqualTo(StringBuilder.class);
        assertThat(dfa.getPropertyValue("name")).isEqualTo(sb);
    }
}
