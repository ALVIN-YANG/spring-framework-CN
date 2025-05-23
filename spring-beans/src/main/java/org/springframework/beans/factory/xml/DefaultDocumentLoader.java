// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体规定许可和限制的内容。*/
package org.springframework.beans.factory.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.springframework.lang.Nullable;
import org.springframework.util.xml.XmlValidationModeDetector;

/**
 * Spring 默认的 {@link DocumentLoader} 实现。
 *
 * <p>简单地说，使用标准的 JAXP 配置的 XML 解析器加载 {@link Document 文档}。如果您想更改用于加载文档的 {@link DocumentBuilder}，那么一种策略是在启动 JVM 时定义相应的 Java 系统属性。例如，要使用 Oracle 的 {@link DocumentBuilder}，您可能需要像以下这样启动您的应用程序：
 *
 * <pre class="code">java -Djavax.xml.parsers.DocumentBuilderFactory=oracle.xml.jaxp.JXDocumentBuilderFactory MyMainClass</pre>
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class DefaultDocumentLoader implements DocumentLoader {

    /**
     * 用于配置验证时模式语言的 JAXP 属性。
     */
    private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * JAXP 属性值，表示 XSD 架构的语言。
     */
    private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    private static final Log logger = LogFactory.getLog(DefaultDocumentLoader.class);

    /**
     * 使用标准JAXP配置的XML解析器，从提供的{@link InputSource}加载{@link Document}。
     */
    @Override
    public Document loadDocument(InputSource inputSource, EntityResolver entityResolver, ErrorHandler errorHandler, int validationMode, boolean namespaceAware) throws Exception {
        DocumentBuilderFactory factory = createDocumentBuilderFactory(validationMode, namespaceAware);
        if (logger.isTraceEnabled()) {
            logger.trace("Using JAXP provider [" + factory.getClass().getName() + "]");
        }
        DocumentBuilder builder = createDocumentBuilder(factory, entityResolver, errorHandler);
        return builder.parse(inputSource);
    }

    /**
     * 创建 {@link DocumentBuilderFactory} 实例。
     * @param validationMode 验证类型：{@link XmlValidationModeDetector#VALIDATION_DTD DTD}
     * 或 {@link XmlValidationModeDetector#VALIDATION_XSD XSD}
     * @param namespaceAware 是否返回的工厂提供对 XML 命名空间的支撑
     * @return JAXP DocumentBuilderFactory
     * @throws ParserConfigurationException 如果我们未能构建一个合适的 DocumentBuilderFactory
     */
    protected DocumentBuilderFactory createDocumentBuilderFactory(int validationMode, boolean namespaceAware) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaceAware);
        if (validationMode != XmlValidationModeDetector.VALIDATION_NONE) {
            factory.setValidating(true);
            if (validationMode == XmlValidationModeDetector.VALIDATION_XSD) {
                // 强制执行对 XSD 的命名空间感知...
                factory.setNamespaceAware(true);
                try {
                    factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
                } catch (IllegalArgumentException ex) {
                    ParserConfigurationException pcex = new ParserConfigurationException("Unable to validate using XSD: Your JAXP provider [" + factory + "] does not support XML Schema. Are you running on Java 1.4 with Apache Crimson? " + "Upgrade to Apache Xerces (or Java 1.5) for full XSD support.");
                    pcex.initCause(ex);
                    throw pcex;
                }
            }
        }
        return factory;
    }

    /**
     * 创建一个 JAXP DocumentBuilder，该定义读取器将使用它来解析 XML 文档。可以在子类中重写，以添加对构建器的进一步初始化。
     * @param factory 应用于创建 DocumentBuilder 的 JAXP DocumentBuilderFactory
     * @param entityResolver 要使用的 SAX EntityResolver
     * @param errorHandler 要使用的 SAX ErrorHandler
     * @return JAXP DocumentBuilder
     * @throws ParserConfigurationException 如果由 JAXP 方法抛出
     */
    protected DocumentBuilder createDocumentBuilder(DocumentBuilderFactory factory, @Nullable EntityResolver entityResolver, @Nullable ErrorHandler errorHandler) throws ParserConfigurationException {
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        if (entityResolver != null) {
            docBuilder.setEntityResolver(entityResolver);
        }
        if (errorHandler != null) {
            docBuilder.setErrorHandler(errorHandler);
        }
        return docBuilder;
    }
}
