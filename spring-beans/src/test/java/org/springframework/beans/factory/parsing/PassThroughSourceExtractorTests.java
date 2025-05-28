// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.parsing;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 对{@link PassThroughSourceExtractor}的单元测试。
 *
 * @author Rick Evans
 * @author Chris Beams
 */
public class PassThroughSourceExtractorTests {

    @Test
    public void testPassThroughContract() throws Exception {
        Object source = new Object();
        Object extractedSource = new PassThroughSourceExtractor().extractSource(source, null);
        assertThat(extractedSource).as("The contract of PassThroughSourceExtractor states that the supplied " + "source object *must* be returned as-is").isSameAs(source);
    }

    @Test
    public void testPassThroughContractEvenWithNull() throws Exception {
        Object extractedSource = new PassThroughSourceExtractor().extractSource(null, null);
        assertThat(extractedSource).as("The contract of PassThroughSourceExtractor states that the supplied " + "source object *must* be returned as-is (even if null)").isNull();
    }
}
