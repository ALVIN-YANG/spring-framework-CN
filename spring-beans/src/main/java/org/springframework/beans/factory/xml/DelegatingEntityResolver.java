// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非遵守许可证。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵犯知识产权。
* 请参阅许可证以了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.xml;

import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 实现 {@link EntityResolver}，它分别委托给一个 {@link BeansDtdResolver} 和一个 {@link PluggableSchemaResolver} 来处理 DTD 和 XML 架构。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 * @see BeansDtdResolver
 * @see PluggableSchemaResolver
 */
public class DelegatingEntityResolver implements EntityResolver {

    /**
     * DTD文件的后缀。
     */
    public static final String DTD_SUFFIX = ".dtd";

    /**
     * 模式定义文件的后缀。
     */
    public static final String XSD_SUFFIX = ".xsd";

    private final EntityResolver dtdResolver;

    private final EntityResolver schemaResolver;

    /**
     * 创建一个新的 DelegatingEntityResolver，该解析器委托给默认的 {@link BeansDtdResolver} 和默认的 {@link PluggableSchemaResolver}。
     * <p>使用提供的 {@link ClassLoader} 配置 {@link PluggableSchemaResolver}。
     * @param classLoader 用于加载的 ClassLoader，可以是 {@code null} 以使用默认的 ClassLoader
     */
    public DelegatingEntityResolver(@Nullable ClassLoader classLoader) {
        this.dtdResolver = new BeansDtdResolver();
        this.schemaResolver = new PluggableSchemaResolver(classLoader);
    }

    /**
     * 创建一个新的DelegatingEntityResolver，该解析器将委托给指定的
     * {@link EntityResolver EntityResolvers}。
     * @param dtdResolver 用于解析DTD的EntityResolver
     * @param schemaResolver 用于解析XML模式的EntityResolver
     */
    public DelegatingEntityResolver(EntityResolver dtdResolver, EntityResolver schemaResolver) {
        Assert.notNull(dtdResolver, "'dtdResolver' is required");
        Assert.notNull(schemaResolver, "'schemaResolver' is required");
        this.dtdResolver = dtdResolver;
        this.schemaResolver = schemaResolver;
    }

    @Override
    @Nullable
    public InputSource resolveEntity(@Nullable String publicId, @Nullable String systemId) throws SAXException, IOException {
        if (systemId != null) {
            if (systemId.endsWith(DTD_SUFFIX)) {
                return this.dtdResolver.resolveEntity(publicId, systemId);
            } else if (systemId.endsWith(XSD_SUFFIX)) {
                return this.schemaResolver.resolveEntity(publicId, systemId);
            }
        }
        // 回退到解析器的默认行为。
        return null;
    }

    @Override
    public String toString() {
        return "EntityResolver delegating " + XSD_SUFFIX + " to " + this.schemaResolver + " and " + DTD_SUFFIX + " to " + this.dtdResolver;
    }
}
