// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditor;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Juergen Hoeller
 * @作者 Arjen Poutsma
 */
public class URIEditorTests {

    @Test
    public void standardURI() {
        doTestURI("mailto:juergen.hoeller@interface21.com");
    }

    @Test
    public void withNonExistentResource() {
        doTestURI("gonna:/freak/in/the/morning/freak/in/the.evening");
    }

    @Test
    public void standardURL() {
        doTestURI("https://www.springframework.org");
    }

    @Test
    public void standardURLWithFragment() {
        doTestURI("https://www.springframework.org#1");
    }

    @Test
    public void standardURLWithWhitespace() {
        PropertyEditor uriEditor = new URIEditor();
        uriEditor.setAsText("  https://www.springframework.org  ");
        Object value = uriEditor.getValue();
        assertThat(value instanceof URI).isTrue();
        URI uri = (URI) value;
        assertThat(uri.toString()).isEqualTo("https://www.springframework.org");
    }

    @Test
    public void classpathURL() {
        PropertyEditor uriEditor = new URIEditor(getClass().getClassLoader());
        uriEditor.setAsText("classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class");
        Object value = uriEditor.getValue();
        assertThat(value instanceof URI).isTrue();
        URI uri = (URI) value;
        assertThat(uriEditor.getAsText()).isEqualTo(uri.toString());
        assertThat(uri.getScheme()).doesNotStartWith("classpath");
    }

    @Test
    public void classpathURLWithWhitespace() {
        PropertyEditor uriEditor = new URIEditor(getClass().getClassLoader());
        uriEditor.setAsText("  classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class  ");
        Object value = uriEditor.getValue();
        assertThat(value instanceof URI).isTrue();
        URI uri = (URI) value;
        assertThat(uriEditor.getAsText()).isEqualTo(uri.toString());
        assertThat(uri.getScheme()).doesNotStartWith("classpath");
    }

    @Test
    public void classpathURLAsIs() {
        PropertyEditor uriEditor = new URIEditor();
        uriEditor.setAsText("classpath:test.txt");
        Object value = uriEditor.getValue();
        assertThat(value instanceof URI).isTrue();
        URI uri = (URI) value;
        assertThat(uriEditor.getAsText()).isEqualTo(uri.toString());
        assertThat(uri.getScheme()).startsWith("classpath");
    }

    @Test
    public void setAsTextWithNull() {
        PropertyEditor uriEditor = new URIEditor();
        uriEditor.setAsText(null);
        assertThat(uriEditor.getValue()).isNull();
        assertThat(uriEditor.getAsText()).isEmpty();
    }

    @Test
    public void getAsTextReturnsEmptyStringIfValueNotSet() {
        PropertyEditor uriEditor = new URIEditor();
        assertThat(uriEditor.getAsText()).isEmpty();
    }

    @Test
    public void encodeURI() {
        PropertyEditor uriEditor = new URIEditor();
        uriEditor.setAsText("https://example.com/spaces and \u20AC");
        Object value = uriEditor.getValue();
        assertThat(value instanceof URI).isTrue();
        URI uri = (URI) value;
        assertThat(uriEditor.getAsText()).isEqualTo(uri.toString());
        assertThat(uri.toASCIIString()).isEqualTo("https://example.com/spaces%20and%20%E2%82%AC");
    }

    @Test
    public void encodeAlreadyEncodedURI() {
        PropertyEditor uriEditor = new URIEditor(false);
        uriEditor.setAsText("https://example.com/spaces%20and%20%E2%82%AC");
        Object value = uriEditor.getValue();
        assertThat(value instanceof URI).isTrue();
        URI uri = (URI) value;
        assertThat(uriEditor.getAsText()).isEqualTo(uri.toString());
        assertThat(uri.toASCIIString()).isEqualTo("https://example.com/spaces%20and%20%E2%82%AC");
    }

    private void doTestURI(String uriSpec) {
        PropertyEditor uriEditor = new URIEditor();
        uriEditor.setAsText(uriSpec);
        Object value = uriEditor.getValue();
        assertThat(value instanceof URI).isTrue();
        URI uri = (URI) value;
        assertThat(uri.toString()).isEqualTo(uriSpec);
    }
}
