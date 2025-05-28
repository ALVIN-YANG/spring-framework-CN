// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 您不得使用此文件除非遵守许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author 尼古拉斯·威廉姆斯
 * @author 山姆·布兰宁
 */
class ZoneIdEditorTests {

    private final ZoneIdEditor editor = new ZoneIdEditor();

    @ParameterizedTest(name = "[{index}] text = ''{0}''")
    @ValueSource(strings = { "America/Chicago", "   America/Chicago   " })
    void americaChicago(String text) {
        editor.setAsText(text);
        ZoneId zoneId = (ZoneId) editor.getValue();
        assertThat(zoneId).as("The zone ID should not be null.").isNotNull();
        assertThat(zoneId).as("The zone ID is not correct.").isEqualTo(ZoneId.of("America/Chicago"));
        assertThat(editor.getAsText()).as("The text version is not correct.").isEqualTo("America/Chicago");
    }

    @Test
    void americaLosAngeles() {
        editor.setAsText("America/Los_Angeles");
        ZoneId zoneId = (ZoneId) editor.getValue();
        assertThat(zoneId).as("The zone ID should not be null.").isNotNull();
        assertThat(zoneId).as("The zone ID is not correct.").isEqualTo(ZoneId.of("America/Los_Angeles"));
        assertThat(editor.getAsText()).as("The text version is not correct.").isEqualTo("America/Los_Angeles");
    }

    @Test
    void getNullAsText() {
        assertThat(editor.getAsText()).as("The returned value is not correct.").isEmpty();
    }

    @Test
    void getValueAsText() {
        editor.setValue(ZoneId.of("America/New_York"));
        assertThat(editor.getAsText()).as("The text version is not correct.").isEqualTo("America/New_York");
    }
}
