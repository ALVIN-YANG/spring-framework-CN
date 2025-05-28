// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“许可证”）；除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可协议具体规定的权限和限制，请参阅许可证。*/
package org.springframework.beans.factory.parsing;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 对{@link ConstructorArgumentEntry}的单元测试。
 *
 * @author Rick Evans
 * @author Chris Beams
 */
public class ConstructorArgumentEntryTests {

    @Test
    public void testCtorBailsOnNegativeCtorIndexArgument() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ConstructorArgumentEntry(-1));
    }
}
