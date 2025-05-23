// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * 用于编辑 {@code java.io.File} 的编辑器，可以直接从 Spring 资源位置填充 File 属性。
 *
 * <p>支持 Spring 风格的 URL 表示法：任何完全限定的标准 URL（"file:"、"http:" 等）以及 Spring 的特殊 "classpath:" 伪 URL。
 *
 * <p><b>注意：</b>此编辑器的行为在 Spring 2.0 中已更改。之前，它直接从文件名创建 File 实例。
 * 从 Spring 2.0 开始，它接受标准的 Spring 资源位置作为输入；这与 URLEditor 和 InputStreamEditor 现在的行为一致。
 *
 * <p><b>注意：</b>在 Spring 2.5 中进行了以下修改。
 * 如果指定了没有 URL 前缀或没有绝对路径的文件名，则我们尝试使用标准的 ResourceLoader 语义定位文件。
 * 如果文件未找到，则创建一个 File 实例，假设文件名指的是相对文件位置。
 *
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @since 09.12.2003
 * @see java.io.File
 * @see org.springframework.core.io.ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see URLEditor
 * @see InputStreamEditor
 */
public class FileEditor extends PropertyEditorSupport {

    private final ResourceEditor resourceEditor;

    /**
     * 创建一个新的 FileEditor，其下使用默认的 ResourceEditor。
     */
    public FileEditor() {
        this.resourceEditor = new ResourceEditor();
    }

    /**
     * 创建一个新的 FileEditor，使用给定的 ResourceEditor。
     * @param resourceEditor 要使用的 ResourceEditor
     */
    public FileEditor(ResourceEditor resourceEditor) {
        Assert.notNull(resourceEditor, "ResourceEditor must not be null");
        this.resourceEditor = resourceEditor;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            setValue(null);
            return;
        }
        // 检查我们是否获得了不带 "file:" 前缀的绝对文件路径。
        // 为了保持向后兼容性，我们将把这些视为直接文件路径。
        File file = null;
        if (!ResourceUtils.isUrl(text)) {
            file = new File(text);
            if (file.isAbsolute()) {
                setValue(file);
                return;
            }
        }
        // 继续进行标准的资源位置解析。
        this.resourceEditor.setAsText(text);
        Resource resource = (Resource) this.resourceEditor.getValue();
        // 如果它是一个指向现有资源的 URL 或路径，则直接使用它。
        if (file == null || resource.exists()) {
            try {
                setValue(resource.getFile());
            } catch (IOException ex) {
                throw new IllegalArgumentException("Could not retrieve file for " + resource + ": " + ex.getMessage());
            }
        } else {
            // 设置一个相对文件引用，并希望一切顺利。
            setValue(file);
        }
    }

    @Override
    public String getAsText() {
        File value = (File) getValue();
        return (value != null ? value.getPath() : "");
    }
}
