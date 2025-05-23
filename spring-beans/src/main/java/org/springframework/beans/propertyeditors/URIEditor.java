// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * 用于编辑 {@code java.net.URI} 的编辑器，可以直接填充 URI 属性，而不是使用字符串属性作为桥梁。
 *
 * <p>支持 Spring 风格的 URI 表示法：任何完全限定的标准 URI（"file:"、"http:" 等）以及 Spring 的特殊“classpath:" 伪 URL，它将被解析为相应的 URI。
 *
 * <p>默认情况下，此编辑器将字符串编码为 URI。例如，空格将被编码为 {@code %20}。可以通过调用 {@link #URIEditor(boolean)} 构造函数来改变此行为。
 *
 * <p>注意：URI 比 URL 更宽松，因为它不需要指定有效的协议。任何有效的 URI 语法中的方案都是允许的，即使没有注册匹配的协议处理器。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see java.net.URI
 * @see URLEditor
 */
public class URIEditor extends PropertyEditorSupport {

    @Nullable
    private final ClassLoader classLoader;

    private final boolean encode;

    /**
     * 创建一个新的编码 URIEditor，将 "classpath:" 位置转换为标准 URI（不尝试将其解析为物理资源）。
     */
    public URIEditor() {
        this(true);
    }

    /**
     * 创建一个新的URIEditor，将"classpath:"位置转换为标准URI（不尝试将其解析为物理资源）。
     * @param encode 指示字符串是否将被编码
     * @since 3.0
     */
    public URIEditor(boolean encode) {
        this.classLoader = null;
        this.encode = encode;
    }

    /**
     * 创建一个新的 URIEditor，使用给定的 ClassLoader 将 "classpath:" 位置解析为物理资源 URL。
     * @param classLoader 用于解析 "classpath:" 位置的 ClassLoader（可能为 null，表示默认的 ClassLoader）
     */
    public URIEditor(@Nullable ClassLoader classLoader) {
        this(classLoader, true);
    }

    /**
     * 使用给定的类加载器创建一个新的 URIEditor，将 "classpath:" 位置解析为物理资源 URL。
     * @param classLoader 用于解析 "classpath:" 位置的类加载器（可能为 null，表示默认类加载器）
     * @param encode 指示字符串是否将被编码
     * @since 3.0
     */
    public URIEditor(@Nullable ClassLoader classLoader, boolean encode) {
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
        this.encode = encode;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            String uri = text.trim();
            if (this.classLoader != null && uri.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
                ClassPathResource resource = new ClassPathResource(uri.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()), this.classLoader);
                try {
                    setValue(resource.getURI());
                } catch (IOException ex) {
                    throw new IllegalArgumentException("Could not retrieve URI for " + resource + ": " + ex.getMessage());
                }
            } else {
                try {
                    setValue(createURI(uri));
                } catch (URISyntaxException ex) {
                    throw new IllegalArgumentException("Invalid URI syntax: " + ex.getMessage());
                }
            }
        } else {
            setValue(null);
        }
    }

    /**
     * 为给定的用户指定字符串值创建一个 URI 实例。
     * <p>默认实现将值编码为符合 RFC-2396 的 URI。
     * @param value 要转换为 URI 实例的值
     * @return URI 实例
     * @throws java.net.URISyntaxException 如果 URI 转换失败
     */
    protected URI createURI(String value) throws URISyntaxException {
        int colonIndex = value.indexOf(':');
        if (this.encode && colonIndex != -1) {
            int fragmentIndex = value.indexOf('#', colonIndex + 1);
            String scheme = value.substring(0, colonIndex);
            String ssp = value.substring(colonIndex + 1, (fragmentIndex > 0 ? fragmentIndex : value.length()));
            String fragment = (fragmentIndex > 0 ? value.substring(fragmentIndex + 1) : null);
            return new URI(scheme, ssp, fragment);
        } else {
            // 未进行编码或值不包含方案 - 转换回默认值
            return new URI(value);
        }
    }

    @Override
    public String getAsText() {
        URI value = (URI) getValue();
        return (value != null ? value.toString() : "");
    }
}
