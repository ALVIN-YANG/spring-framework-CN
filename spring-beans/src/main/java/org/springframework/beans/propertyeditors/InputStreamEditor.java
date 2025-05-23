// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明。有关许可权限和限制的特定语言，
* 请参阅许可证。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 *  单向属性编辑器，可以将文本字符串转换为 {@code java.io.InputStream}，将给定的字符串解释为 Spring 资源位置（例如 URL 字符串）。
 *
 * <p>支持 Spring 风格的 URL 表示法：任何完全限定的标准 URL（例如 "file:"、"http:" 等）和 Spring 的特殊 "classpath:" 伪 URL。
 *
 * <p>请注意，此类流通常不会被 Spring 本身关闭！
 *
 *  作者：Juergen Hoeller
 *  自 1.0.1 版本以来
 *  @see java.io.InputStream
 *  @see org.springframework.core.io.ResourceEditor
 *  @see org.springframework.core.io.ResourceLoader
 *  @see URLEditor
 *  @see FileEditor
 */
public class InputStreamEditor extends PropertyEditorSupport {

    private final ResourceEditor resourceEditor;

    /**
     * 创建一个新的 InputStreamEditor，使用默认的 ResourceEditor 作为其底层。
     */
    public InputStreamEditor() {
        this.resourceEditor = new ResourceEditor();
    }

    /**
     * 创建一个新的 InputStreamEditor，使用给定的 ResourceEditor 作为底层。
     * @param resourceEditor 要使用的 ResourceEditor
     */
    public InputStreamEditor(ResourceEditor resourceEditor) {
        Assert.notNull(resourceEditor, "ResourceEditor must not be null");
        this.resourceEditor = resourceEditor;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.resourceEditor.setAsText(text);
        Resource resource = (Resource) this.resourceEditor.getValue();
        try {
            setValue(resource != null ? resource.getInputStream() : null);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to retrieve InputStream for " + resource, ex);
        }
    }

    /**
     * 此实现返回 {@code null} 以指示没有适当的文本表示。
     */
    @Override
    @Nullable
    public String getAsText() {
        return null;
    }
}
