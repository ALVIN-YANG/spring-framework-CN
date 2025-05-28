// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获得许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件是基于"现状"提供的，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵权。
* 请参阅许可协议，了解具体规定许可协议下对权限和限制的语言。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditor;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 对 {@link CharArrayPropertyEditor} 类的单元测试。
 *
 * @author Rick Evans
 */
public class CharArrayPropertyEditorTests {

    private final PropertyEditor charEditor = new CharArrayPropertyEditor();

    @Test
    public void sunnyDaySetAsText() throws Exception {
        final String text = "Hideous towns make me throw... up";
        charEditor.setAsText(text);
        Object value = charEditor.getValue();
        assertThat(value).isInstanceOf(char[].class);
        char[] chars = (char[]) value;
        for (int i = 0; i < text.length(); ++i) {
            assertThat(chars[i]).as("char[] differs at index '" + i + "'").isEqualTo(text.charAt(i));
        }
        assertThat(charEditor.getAsText()).isEqualTo(text);
    }

    @Test
    public void getAsTextReturnsEmptyStringIfValueIsNull() throws Exception {
        assertThat(charEditor.getAsText()).isEmpty();
        charEditor.setAsText(null);
        assertThat(charEditor.getAsText()).isEmpty();
    }
}
