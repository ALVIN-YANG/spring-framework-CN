// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.propertyeditors;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 对 {@link InputStreamEditor} 类的单元测试。
 *
 * @author Rick Evans
 * @author Chris Beams
 */
public class InputStreamEditorTests {

    @Test
    public void testCtorWithNullResourceEditor() {
        assertThatIllegalArgumentException().isThrownBy(() -> new InputStreamEditor(null));
    }

    @Test
    public void testSunnyDay() throws IOException {
        InputStream stream = null;
        try {
            String resource = "classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class";
            InputStreamEditor editor = new InputStreamEditor();
            editor.setAsText(resource);
            Object value = editor.getValue();
            assertThat(value).isNotNull();
            assertThat(value instanceof InputStream).isTrue();
            stream = (InputStream) value;
            assertThat(stream.available()).isGreaterThan(0);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    @Test
    public void testWhenResourceDoesNotExist() {
        InputStreamEditor editor = new InputStreamEditor();
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setAsText("classpath:bingo!"));
    }

    @Test
    public void testGetAsTextReturnsNullByDefault() {
        assertThat(new InputStreamEditor().getAsText()).isNull();
        String resource = "classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class";
        InputStreamEditor editor = new InputStreamEditor();
        editor.setAsText(resource);
        assertThat(editor.getAsText()).isNull();
    }
}
