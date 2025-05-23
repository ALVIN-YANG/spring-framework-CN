// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import org.xml.sax.InputSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;

/**
 * 用于编辑 {@code org.xml.sax.InputSource} 的编辑器，将 Spring 资源位置字符串转换为 SAX 输入源对象。
 *
 * <p>支持 Spring 风格的 URL 表示法：任何完全限定的标准 URL（"file:"、"http:" 等）以及 Spring 的特殊 "classpath:" 伪 URL。
 *
 * @author Juergen Hoeller
 * @since 3.0.3
 * @see org.xml.sax.InputSource
 * @see org.springframework.core.io.ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see URLEditor
 * @see FileEditor
 */
public class InputSourceEditor extends PropertyEditorSupport {

    private final ResourceEditor resourceEditor;

    /**
     * 创建一个新的 InputSourceEditor，
     * 在其下方使用默认的 ResourceEditor。
     */
    public InputSourceEditor() {
        this.resourceEditor = new ResourceEditor();
    }

    /**
     * 创建一个新的 InputSourceEditor，
     * 使用给定的 ResourceEditor 作为其下级。
     * @param resourceEditor 要使用的 ResourceEditor
     */
    public InputSourceEditor(ResourceEditor resourceEditor) {
        Assert.notNull(resourceEditor, "ResourceEditor must not be null");
        this.resourceEditor = resourceEditor;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.resourceEditor.setAsText(text);
        Resource resource = (Resource) this.resourceEditor.getValue();
        try {
            setValue(resource != null ? new InputSource(resource.getURL().toString()) : null);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not retrieve URL for " + resource + ": " + ex.getMessage());
        }
    }

    @Override
    public String getAsText() {
        InputSource value = (InputSource) getValue();
        return (value != null ? value.getSystemId() : "");
    }
}
