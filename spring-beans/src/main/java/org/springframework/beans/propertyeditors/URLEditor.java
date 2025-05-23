// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用，除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体规定许可权和限制。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.net.URL;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;

/**
 * 用于编辑 {@code java.net.URL}，可以直接填充 URL 属性，而不是使用字符串属性作为桥梁。
 *
 * <p>支持 Spring 风格的 URL 表示法：任何完全限定的标准 URL（例如 "file:"、"http:" 等）以及 Spring 的特殊 "classpath:" 伪 URL，以及 Spring 的上下文特定相对文件路径。
 *
 * <p>注意：必须指定有效的协议，否则 URL 将在创建之初就被拒绝。然而，目标资源在 URL 创建时并不一定必须存在；这取决于特定的资源类型。
 *
 * @author Juergen Hoeller
 * @since 15.12.2003
 * @see java.net.URL
 * @see org.springframework.core.io.ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see FileEditor
 * @see InputStreamEditor
 */
public class URLEditor extends PropertyEditorSupport {

    private final ResourceEditor resourceEditor;

    /**
     * 创建一个新的 URLEditor，其下使用默认的 ResourceEditor。
     */
    public URLEditor() {
        this.resourceEditor = new ResourceEditor();
    }

    /**
     * 创建一个新的 URLEditor，使用给定的 ResourceEditor 作为其底层。
     * @param resourceEditor 要使用的 ResourceEditor
     */
    public URLEditor(ResourceEditor resourceEditor) {
        Assert.notNull(resourceEditor, "ResourceEditor must not be null");
        this.resourceEditor = resourceEditor;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.resourceEditor.setAsText(text);
        Resource resource = (Resource) this.resourceEditor.getValue();
        try {
            setValue(resource != null ? resource.getURL() : null);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not retrieve URL for " + resource + ": " + ex.getMessage());
        }
    }

    @Override
    public String getAsText() {
        URL value = (URL) getValue();
        return (value != null ? value.toExternalForm() : "");
    }
}
