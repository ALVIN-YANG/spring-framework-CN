// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按照“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.parsing;

import org.apache.commons.logging.Log;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DescriptiveResource;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Rick Evans
 * @author Juergen Hoeller
 * @author Chris Beams
 *
 * 作者：Rick Evans
 * 作者：Juergen Hoeller
 * 作者：Chris Beams
 */
public class FailFastProblemReporterTests {

    @Test
    public void testError() throws Exception {
        FailFastProblemReporter reporter = new FailFastProblemReporter();
        assertThatExceptionOfType(BeanDefinitionParsingException.class).isThrownBy(() -> reporter.error(new Problem("VGER", new Location(new DescriptiveResource("here")), null, new IllegalArgumentException())));
    }

    @Test
    public void testWarn() throws Exception {
        Problem problem = new Problem("VGER", new Location(new DescriptiveResource("here")), null, new IllegalArgumentException());
        Log log = mock();
        FailFastProblemReporter reporter = new FailFastProblemReporter();
        reporter.setLogger(log);
        reporter.warning(problem);
        verify(log).warn(any(), isA(IllegalArgumentException.class));
    }
}
