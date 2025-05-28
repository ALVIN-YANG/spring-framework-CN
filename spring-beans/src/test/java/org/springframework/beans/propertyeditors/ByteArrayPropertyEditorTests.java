// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditor;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 对 {@link ByteArrayPropertyEditor} 类的单元测试。
 *
 * @author Rick Evans
 */
public class ByteArrayPropertyEditorTests {

    private final PropertyEditor byteEditor = new ByteArrayPropertyEditor();

    @Test
    public void sunnyDaySetAsText() throws Exception {
        final String text = "Hideous towns make me throw... up";
        byteEditor.setAsText(text);
        Object value = byteEditor.getValue();
        assertThat(value).isInstanceOf(byte[].class);
        byte[] bytes = (byte[]) value;
        for (int i = 0; i < text.length(); ++i) {
            assertThat(bytes[i]).as("cyte[] differs at index '" + i + "'").isEqualTo((byte) text.charAt(i));
        }
        assertThat(byteEditor.getAsText()).isEqualTo(text);
    }

    @Test
    public void getAsTextReturnsEmptyStringIfValueIsNull() throws Exception {
        assertThat(byteEditor.getAsText()).isEmpty();
        byteEditor.setAsText(null);
        assertThat(byteEditor.getAsText()).isEmpty();
    }
}
