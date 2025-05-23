// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * 加载 XML {@link Document} 的策略接口。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see DefaultDocumentLoader
 */
public interface DocumentLoader {

    /**
     * 从提供的 {@link InputSource source} 加载一个 {@link Document document}。
     * @param inputSource 要加载的文档的来源
     * @param entityResolver 用于解析任何实体的解析器
     * @param errorHandler 用于报告文档加载过程中的任何错误
     * @param validationMode 验证类型
     * {@link org.springframework.util.xml.XmlValidationModeDetector#VALIDATION_DTD DTD}
     * 或 {@link org.springframework.util.xml.XmlValidationModeDetector#VALIDATION_XSD XSD})
     * @param namespaceAware 如果需要提供对 XML 命名空间的支持则为 {@code true}
     * @return 加载的 {@link Document document}
     * @throws Exception 如果发生错误
     */
    Document loadDocument(InputSource inputSource, EntityResolver entityResolver, ErrorHandler errorHandler, int validationMode, boolean namespaceAware) throws Exception;
}
