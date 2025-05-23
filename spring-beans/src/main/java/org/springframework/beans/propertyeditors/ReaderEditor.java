// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 *  单向属性编辑器，可以将文本字符串转换为 {@code java.io.Reader}，将给定的字符串解释为 Spring 资源位置（例如 URL 字符串）。
 *
 * <p>支持 Spring 风格的 URL 语法：任何完全限定的标准 URL（"file:"、"http:" 等）以及 Spring 的特殊 "classpath:" 伪 URL。
 *
 * <p>请注意，此类读取器通常不会被 Spring 本身关闭！
 *
 *  作者：Juergen Hoeller
 *  自 4.2 版本以来
 *  @see java.io.Reader
 *  @see org.springframework.core.io.ResourceEditor
 *  @see org.springframework.core.io.ResourceLoader
 *  @see InputStreamEditor
 */
public class ReaderEditor extends PropertyEditorSupport {

    private final ResourceEditor resourceEditor;

    /**
     * 创建一个新的ReaderEditor，使用默认的ResourceEditor作为其下层。
     */
    public ReaderEditor() {
        this.resourceEditor = new ResourceEditor();
    }

    /**
     * 创建一个新的ReaderEditor，使用给定的ResourceEditor作为其底层。
     * @param resourceEditor 要使用的ResourceEditor
     */
    public ReaderEditor(ResourceEditor resourceEditor) {
        Assert.notNull(resourceEditor, "ResourceEditor must not be null");
        this.resourceEditor = resourceEditor;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.resourceEditor.setAsText(text);
        Resource resource = (Resource) this.resourceEditor.getValue();
        try {
            setValue(resource != null ? new EncodedResource(resource).getReader() : null);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to retrieve Reader for " + resource, ex);
        }
    }

    /**
     * 此实现返回 {@code null} 以指示没有合适的文本表示形式。
     */
    @Override
    @Nullable
    public String getAsText() {
        return null;
    }
}
