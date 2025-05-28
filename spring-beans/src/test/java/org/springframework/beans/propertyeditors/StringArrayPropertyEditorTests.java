// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.propertyeditors;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick Evans
 * @author Juergen Hoeller
 * @author Sam Brannen
 *
 * 作者：Rick Evans
 * 作者：Juergen Hoeller
 * 作者：Sam Brannen
 */
class StringArrayPropertyEditorTests {

    @Test
    void withDefaultSeparator() {
        StringArrayPropertyEditor editor = new StringArrayPropertyEditor();
        editor.setAsText("0,1,2");
        Object value = editor.getValue();
        assertTrimmedElements(value);
        assertThat(editor.getAsText()).isEqualTo("0,1,2");
    }

    @Test
    void trimByDefault() {
        StringArrayPropertyEditor editor = new StringArrayPropertyEditor();
        editor.setAsText(" 0,1 , 2 ");
        Object value = editor.getValue();
        assertTrimmedElements(value);
        assertThat(editor.getAsText()).isEqualTo("0,1,2");
    }

    @Test
    void noTrim() {
        StringArrayPropertyEditor editor = new StringArrayPropertyEditor(",", false, false);
        editor.setAsText("  0,1  , 2 ");
        Object value = editor.getValue();
        String[] array = (String[]) value;
        for (int i = 0; i < array.length; ++i) {
            assertThat(array[i].length()).isEqualTo(3);
            assertThat(array[i].trim()).isEqualTo(("" + i));
        }
        assertThat(editor.getAsText()).isEqualTo("  0,1  , 2 ");
    }

    @Test
    void withCustomSeparator() {
        StringArrayPropertyEditor editor = new StringArrayPropertyEditor(":");
        editor.setAsText("0:1:2");
        Object value = editor.getValue();
        assertTrimmedElements(value);
        assertThat(editor.getAsText()).isEqualTo("0:1:2");
    }

    @Test
    void withCharsToDelete() {
        StringArrayPropertyEditor editor = new StringArrayPropertyEditor(",", "\r\n", false);
        editor.setAsText("0\r,1,\n2");
        Object value = editor.getValue();
        assertTrimmedElements(value);
        assertThat(editor.getAsText()).isEqualTo("0,1,2");
    }

    @Test
    void withEmptyArray() {
        StringArrayPropertyEditor editor = new StringArrayPropertyEditor();
        editor.setAsText("");
        Object value = editor.getValue();
        assertThat(value).isInstanceOf(String[].class);
        assertThat((String[]) value).isEmpty();
    }

    @Test
    void withEmptyArrayAsNull() {
        StringArrayPropertyEditor editor = new StringArrayPropertyEditor(",", true);
        editor.setAsText("");
        assertThat(editor.getValue()).isNull();
    }

    private static void assertTrimmedElements(Object value) {
        assertThat(value).isInstanceOf(String[].class);
        String[] array = (String[]) value;
        for (int i = 0; i < array.length; ++i) {
            assertThat(array[i]).isEqualTo(("" + i));
        }
    }
}
