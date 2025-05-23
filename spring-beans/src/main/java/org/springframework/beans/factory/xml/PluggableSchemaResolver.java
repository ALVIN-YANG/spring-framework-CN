// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“许可协议”）进行许可；
* 除非遵守许可协议，否则不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是“按原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明示或暗示。有关许可协议中规定的权限和限制的具体语言，
* 请参阅许可协议。*/
package org.springframework.beans.factory.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 *  实现 {@link EntityResolver}，尝试使用一组映射文件将模式 URL 解析为本地 {@link ClassPathResource} 类路径资源。
 *
 * <p>默认情况下，此类将在类路径中查找映射文件，使用模式：{@code META-INF/spring.schemas}，允许在任何时候存在多个文件在类路径上。
 *
 * <p>文件 {@code META-INF/spring.schemas} 的格式是一个属性文件，其中每一行应为形式为 {@code systemId=schema-location}，其中 {@code schema-location} 也应该是类路径中的模式文件。由于 {@code systemId} 通常是一个 URL，必须小心转义任何在属性文件中被视为分隔符的 ':' 字符。
 *
 * <p>可以使用 {@link #PluggableSchemaResolver(ClassLoader, String)} 构造函数覆盖映射文件的模式。
 *
 *  @author Rob Harrop
 *  @author Juergen Hoeller
 *  @since 2.0
 */
public class PluggableSchemaResolver implements EntityResolver {

    /**
     * 定义模式映射的文件位置。
     * 可以存在于多个 JAR 文件中。
     */
    public static final String DEFAULT_SCHEMA_MAPPINGS_LOCATION = "META-INF/spring.schemas";

    private static final Log logger = LogFactory.getLog(PluggableSchemaResolver.class);

    @Nullable
    private final ClassLoader classLoader;

    private final String schemaMappingsLocation;

    /**
     * 存储模式URL与本地模式路径的映射。
     */
    @Nullable
    private volatile Map<String, String> schemaMappings;

    /**
     * 加载模式 URL 与模式文件位置映射，使用默认映射文件模式 "META-INF/spring.schemas"。
     * @param classLoader 要使用的 ClassLoader（可以为 null 以使用默认的 ClassLoader）
     * @see PropertiesLoaderUtils#loadAllProperties(String, ClassLoader)
     */
    public PluggableSchemaResolver(@Nullable ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.schemaMappingsLocation = DEFAULT_SCHEMA_MAPPINGS_LOCATION;
    }

    /**
     * 使用给定的映射文件模式加载模式 URL 与模式文件位置映射。
     * @param classLoader 要使用的 ClassLoader（可以为 null 以使用默认的 ClassLoader）
     * @param schemaMappingsLocation 定义模式映射的文件位置
     * （不能为空）
     * @see PropertiesLoaderUtils#loadAllProperties(String, ClassLoader)
     */
    public PluggableSchemaResolver(@Nullable ClassLoader classLoader, String schemaMappingsLocation) {
        Assert.hasText(schemaMappingsLocation, "'schemaMappingsLocation' must not be empty");
        this.classLoader = classLoader;
        this.schemaMappingsLocation = schemaMappingsLocation;
    }

    @Override
    @Nullable
    public InputSource resolveEntity(@Nullable String publicId, @Nullable String systemId) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("Trying to resolve XML entity with public id [" + publicId + "] and system id [" + systemId + "]");
        }
        if (systemId != null) {
            String resourceLocation = getSchemaMappings().get(systemId);
            if (resourceLocation == null && systemId.startsWith("https:")) {
                // 检索即使是https声明时的规范http方案映射
                resourceLocation = getSchemaMappings().get("http:" + systemId.substring(6));
            }
            if (resourceLocation != null) {
                Resource resource = new ClassPathResource(resourceLocation, this.classLoader);
                try {
                    InputSource source = new InputSource(resource.getInputStream());
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Found XML schema [" + systemId + "] in classpath: " + resourceLocation);
                    }
                    return source;
                } catch (FileNotFoundException ex) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Could not find XML schema [" + systemId + "]: " + resource, ex);
                    }
                }
            }
        }
        // 回退到解析器的默认行为。
        return null;
    }

    /**
     * 懒加载指定的模式映射。
     */
    private Map<String, String> getSchemaMappings() {
        Map<String, String> schemaMappings = this.schemaMappings;
        if (schemaMappings == null) {
            synchronized (this) {
                schemaMappings = this.schemaMappings;
                if (schemaMappings == null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Loading schema mappings from [" + this.schemaMappingsLocation + "]");
                    }
                    try {
                        Properties mappings = PropertiesLoaderUtils.loadAllProperties(this.schemaMappingsLocation, this.classLoader);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Loaded schema mappings: " + mappings);
                        }
                        schemaMappings = new ConcurrentHashMap<>(mappings.size());
                        CollectionUtils.mergePropertiesIntoMap(mappings, schemaMappings);
                        this.schemaMappings = schemaMappings;
                    } catch (IOException ex) {
                        throw new IllegalStateException("Unable to load schema mappings from location [" + this.schemaMappingsLocation + "]", ex);
                    }
                }
            }
        }
        return schemaMappings;
    }

    @Override
    public String toString() {
        return "EntityResolver using schema mappings " + getSchemaMappings();
    }
}
