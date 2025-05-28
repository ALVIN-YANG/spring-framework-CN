// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditor;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @作者 Rick Evans
 * @作者 Chris Beams
 */
public class URLEditorTests {

    @Test
    public void testCtorWithNullResourceEditor() {
        assertThatIllegalArgumentException().isThrownBy(() -> new URLEditor(null));
    }

    @Test
    public void testStandardURI() {
        PropertyEditor urlEditor = new URLEditor();
        urlEditor.setAsText("mailto:juergen.hoeller@interface21.com");
        Object value = urlEditor.getValue();
        assertThat(value instanceof URL).isTrue();
        URL url = (URL) value;
        assertThat(urlEditor.getAsText()).isEqualTo(url.toExternalForm());
    }

    @Test
    public void testStandardURL() {
        PropertyEditor urlEditor = new URLEditor();
        urlEditor.setAsText("https://www.springframework.org");
        Object value = urlEditor.getValue();
        assertThat(value instanceof URL).isTrue();
        URL url = (URL) value;
        assertThat(urlEditor.getAsText()).isEqualTo(url.toExternalForm());
    }

    @Test
    public void testClasspathURL() {
        PropertyEditor urlEditor = new URLEditor();
        urlEditor.setAsText("classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class");
        Object value = urlEditor.getValue();
        assertThat(value instanceof URL).isTrue();
        URL url = (URL) value;
        assertThat(urlEditor.getAsText()).isEqualTo(url.toExternalForm());
        assertThat(url.getProtocol()).doesNotStartWith("classpath");
    }

    @Test
    public void testWithNonExistentResource() {
        PropertyEditor urlEditor = new URLEditor();
        assertThatIllegalArgumentException().isThrownBy(() -> urlEditor.setAsText("gonna:/freak/in/the/morning/freak/in/the.evening"));
    }

    @Test
    public void testSetAsTextWithNull() {
        PropertyEditor urlEditor = new URLEditor();
        urlEditor.setAsText(null);
        assertThat(urlEditor.getValue()).isNull();
        assertThat(urlEditor.getAsText()).isEmpty();
    }

    @Test
    public void testGetAsTextReturnsEmptyStringIfValueNotSet() {
        PropertyEditor urlEditor = new URLEditor();
        assertThat(urlEditor.getAsText()).isEmpty();
    }
}
