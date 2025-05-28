// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则根据许可证分发的软件是在“现状”基础上分发的，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.propertyeditors;

import java.io.IOException;
import java.io.Reader;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 对 {@link ReaderEditor} 类的单元测试。
 *
 * @author Juergen Hoeller
 * @since 4.2
 */
public class ReaderEditorTests {

    @Test
    public void testCtorWithNullResourceEditor() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ReaderEditor(null));
    }

    @Test
    public void testSunnyDay() throws IOException {
        Reader reader = null;
        try {
            String resource = "classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class";
            ReaderEditor editor = new ReaderEditor();
            editor.setAsText(resource);
            Object value = editor.getValue();
            assertThat(value).isNotNull();
            assertThat(value instanceof Reader).isTrue();
            reader = (Reader) value;
            assertThat(reader.ready()).isTrue();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Test
    public void testWhenResourceDoesNotExist() {
        String resource = "classpath:bingo!";
        ReaderEditor editor = new ReaderEditor();
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setAsText(resource));
    }

    @Test
    public void testGetAsTextReturnsNullByDefault() {
        assertThat(new ReaderEditor().getAsText()).isNull();
        String resource = "classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class";
        ReaderEditor editor = new ReaderEditor();
        editor.setAsText(resource);
        assertThat(editor.getAsText()).isNull();
    }
}
