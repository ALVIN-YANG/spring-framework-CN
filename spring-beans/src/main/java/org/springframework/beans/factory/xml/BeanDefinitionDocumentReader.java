// 翻译完成 glm-4-flash
/** 版权所有 2002-2015 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），除非适用法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证以了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;
import org.springframework.beans.factory.BeanDefinitionStoreException;

/**
 *  用于解析包含 Spring 容器配置定义的 XML 文档的 SPI。
 *  由 {@link XmlBeanDefinitionReader} 用于实际解析 DOM 文档。
 *
 * <p>每个文档实例化一次：实现可以在执行 {@code registerBeanDefinitions} 方法时保留实例变量中的状态 —— 例如，为文档中所有 bean 定义定义的全局设置。
 *
 *  @author Juergen Hoeller
 *  @author Rob Harrop
 *  @since 2003年12月18日
 *  @see XmlBeanDefinitionReader#setDocumentReaderClass
 */
public interface BeanDefinitionDocumentReader {

    /**
     * 从给定的 DOM 文档中读取 Bean 定义，并将它们注册到给定读取上下文中的注册器中。
     * @param doc 给定的 DOM 文档
     * @param readerContext 当前的读取上下文（包括目标注册器和正在解析的资源）
     * @throws BeanDefinitionStoreException 在解析错误的情况下
     */
    void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) throws BeanDefinitionStoreException;
}
