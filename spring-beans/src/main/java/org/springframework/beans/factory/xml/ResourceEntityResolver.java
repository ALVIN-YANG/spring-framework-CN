// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可协议") 进行许可；
* 除非遵守许可协议，否则您不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可协议以了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;

/**
 *  实现了 {@code EntityResolver} 的类，尝试通过一个 {@link org.springframework.core.io.ResourceLoader}（通常，相对于 {@code ApplicationContext} 的资源基础）来解析实体引用（如果适用）。
 *  扩展了 {@link DelegatingEntityResolver} 以提供 DTD 和 XSD 的查找功能。
 *
 * <p>允许使用标准的 XML 实体将 XML 片段包含到应用程序上下文定义中，例如将一个大的 XML 文件分割成多个模块。包含路径可以像通常一样相对于应用程序上下文资源基础，而不是相对于 JVM 工作目录（XML 解析器的默认值）。
 *
 * <p>注意：除了相对路径之外，每个指定当前系统根目录中的文件的 URL（即 JVM 工作目录），也将相对于应用程序上下文进行解析。
 *
 * @作者 Juergen Hoeller
 * @since 2003年7月31日
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.context.ApplicationContext
 */
public class ResourceEntityResolver extends DelegatingEntityResolver {

    private static final Log logger = LogFactory.getLog(ResourceEntityResolver.class);

    private final ResourceLoader resourceLoader;

    /**
     * 为指定的资源加载器（通常是 ApplicationContext）创建一个 ResourceEntityResolver。
     * @param resourceLoader 要加载包含在 XML 实体中的资源加载器（或 ApplicationContext）
     */
    public ResourceEntityResolver(ResourceLoader resourceLoader) {
        super(resourceLoader.getClassLoader());
        this.resourceLoader = resourceLoader;
    }

    @Override
    @Nullable
    public InputSource resolveEntity(@Nullable String publicId, @Nullable String systemId) throws SAXException, IOException {
        InputSource source = super.resolveEntity(publicId, systemId);
        if (source == null && systemId != null) {
            String resourcePath = null;
            try {
                String decodedSystemId = URLDecoder.decode(systemId, StandardCharsets.UTF_8);
                String givenUrl = ResourceUtils.toURL(decodedSystemId).toString();
                String systemRootUrl = new File("").toURI().toURL().toString();
                // 如果当前位于系统根目录，则相对于资源基本路径尝试。
                if (givenUrl.startsWith(systemRootUrl)) {
                    resourcePath = givenUrl.substring(systemRootUrl.length());
                }
            } catch (Exception ex) {
                // 通常是一个 MalformedURLException 或 AccessControlException。
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not resolve XML entity [" + systemId + "] against system root URL", ex);
                }
                // 没有 URL（或没有可解析的 URL）-> 尝试相对于资源基本路径进行解析。
                resourcePath = systemId;
            }
            if (resourcePath != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Trying to locate XML entity [" + systemId + "] as resource [" + resourcePath + "]");
                }
                Resource resource = this.resourceLoader.getResource(resourcePath);
                source = new InputSource(resource.getInputStream());
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Found XML entity [" + systemId + "]: " + resource);
                }
            } else if (systemId.endsWith(DTD_SUFFIX) || systemId.endsWith(XSD_SUFFIX)) {
                source = resolveSchemaEntity(publicId, systemId);
            }
        }
        return source;
    }

    /**
     * 这是用于 {@link #resolveEntity(String, String)} 的备用方法，在无法将 "schema" 实体（DTD 或 XSD）解析为本地资源时使用。默认行为是通过 HTTPS 进行远程解析。
     * <p>子类可以覆盖此方法以更改默认行为。
     * <ul>
     * <li>返回 {@code null} 以回退到解析器的
     * {@linkplain org.xml.sax.EntityResolver#resolveEntity(String, String) 默认行为}。</li>
     * <li>抛出异常以阻止对 DTD 或 XSD 的远程解析。</li>
     * </ul>
     * @param publicId 被引用的外部实体的公共标识符，或如果没有提供则为 null
     * @param systemId 被引用的外部实体的系统标识符，代表 DTD 或 XSD 的 URL
     * @return 描述新输入源的 InputSource 对象，或返回 null 以请求解析器打开到系统标识符的常规 URI 连接
     * @since 6.0.4
     */
    @Nullable
    protected InputSource resolveSchemaEntity(@Nullable String publicId, String systemId) {
        InputSource source;
        // 即使对于规范化的HTTP声明，也通过https进行外部DTD/XSD查找
        String url = systemId;
        if (url.startsWith("http:")) {
            url = "https:" + url.substring(5);
        }
        if (logger.isWarnEnabled()) {
            logger.warn("DTD/XSD XML entity [" + systemId + "] not found, falling back to remote https resolution");
        }
        try {
            source = new InputSource(ResourceUtils.toURL(url).openStream());
            source.setPublicId(publicId);
            source.setSystemId(systemId);
        } catch (IOException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not resolve XML entity [" + systemId + "] through URL [" + url + "]", ex);
            }
            // 回退到解析器的默认行为。
            source = null;
        }
        return source;
    }
}
