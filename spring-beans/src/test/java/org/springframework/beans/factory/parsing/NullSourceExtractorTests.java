// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发软件是基于“现状”和“按原样”提供的，
* 不提供任何明示或暗示的保证或条件，有关权限和限制，请参阅许可证的具体语言。*/
package org.springframework.beans.factory.parsing;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick Evans
 * @author Chris Beams
 *
 * 作者：Rick Evans
 * 作者：Chris Beams
 */
public class NullSourceExtractorTests {

    @Test
    public void testPassThroughContract() throws Exception {
        Object source = new Object();
        Object extractedSource = new NullSourceExtractor().extractSource(source, null);
        assertThat(extractedSource).as("The contract of NullSourceExtractor states that the extraction *always* return null").isNull();
    }

    @Test
    public void testPassThroughContractEvenWithNull() throws Exception {
        Object extractedSource = new NullSourceExtractor().extractSource(null, null);
        assertThat(extractedSource).as("The contract of NullSourceExtractor states that the extraction *always* return null").isNull();
    }
}
