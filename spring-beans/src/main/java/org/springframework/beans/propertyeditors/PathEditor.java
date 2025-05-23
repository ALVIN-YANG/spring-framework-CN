// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按 "原样" 提供分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵犯知识产权。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

/**
 * 用于编辑 {@code java.nio.file.Path} 的编辑器，可以直接填充 Path 属性，而不是使用字符串属性作为桥梁。
 *
 * <p>基于 {@link Paths#get(URI)} 的解析算法，检查已注册的 NIO 文件系统提供者，包括 "file:..." 路径的默认文件系统。也支持 Spring 风格的 URL 表示法：任何完全限定的标准 URL 和 Spring 的特殊 "classpath:" 伪 URL，以及 Spring 的上下文特定相对文件路径。作为备用方案，如果找不到现有的上下文相对资源，将通过 {@code Paths#get(String)} 在文件系统中解析路径。
 *
 * @author Juergen Hoeller
 * @since 4.3.2
 * @see java.nio.file.Path
 * @see Paths#get(URI)
 * @see ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see FileEditor
 * @see URLEditor
 */
public class PathEditor extends PropertyEditorSupport {

    private final ResourceEditor resourceEditor;

    /**
     * 创建一个新的 PathEditor，使用默认的 ResourceEditor 作为其底层。
     */
    public PathEditor() {
        this.resourceEditor = new ResourceEditor();
    }

    /**
     * 创建一个新的 PathEditor，使用给定的 ResourceEditor 作为其底层。
     * @param resourceEditor 要使用的 ResourceEditor
     */
    public PathEditor(ResourceEditor resourceEditor) {
        Assert.notNull(resourceEditor, "ResourceEditor must not be null");
        this.resourceEditor = resourceEditor;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        boolean nioPathCandidate = !text.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX);
        if (nioPathCandidate && !text.startsWith("/")) {
            try {
                URI uri = ResourceUtils.toURI(text);
                String scheme = uri.getScheme();
                if (scheme != null) {
                    // 除了 "C:" 风格的驱动器字母外，没有 NIO 候选者
                    nioPathCandidate = (scheme.length() == 1);
                    // 让我们尝试使用 Paths.get(URI) 通过 NIO 文件系统提供者
                    setValue(Paths.get(uri).normalize());
                    return;
                }
            } catch (URISyntaxException ex) {
                // 不是一个有效的URI；可能是一个Windows风格的路径之后
                // 一个文件前缀（让我们尝试作为Spring资源位置）
                nioPathCandidate = !text.startsWith(ResourceUtils.FILE_URL_PREFIX);
            } catch (FileSystemNotFoundException | IllegalArgumentException ex) {
                // NIO 或 Paths 要求未注册 URI 方案：
                // 让我们尝试通过 Spring 的资源机制来使用 URL 协议处理器。
            }
        }
        this.resourceEditor.setAsText(text);
        Resource resource = (Resource) this.resourceEditor.getValue();
        if (resource == null) {
            setValue(null);
        } else if (nioPathCandidate && !resource.exists()) {
            setValue(Paths.get(text).normalize());
        } else {
            try {
                setValue(resource.getFile().toPath());
            } catch (IOException ex) {
                String msg = "Could not resolve \"" + text + "\" to 'java.nio.file.Path' for " + resource + ": " + ex.getMessage();
                if (nioPathCandidate) {
                    msg += " - In case of ambiguity, consider adding the 'file:' prefix for an explicit reference " + "to a file system resource of the same name: \"file:" + text + "\"";
                }
                throw new IllegalArgumentException(msg);
            }
        }
    }

    @Override
    public String getAsText() {
        Path value = (Path) getValue();
        return (value != null ? value.toString() : "");
    }
}
