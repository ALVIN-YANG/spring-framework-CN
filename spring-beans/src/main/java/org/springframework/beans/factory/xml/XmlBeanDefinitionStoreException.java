// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体语言规定许可权限和限制。*/
package org.springframework.beans.factory.xml;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.springframework.beans.factory.BeanDefinitionStoreException;

/**
 * 特定的 XML BeanDefinitionStoreException 子类，它封装了一个
 * {@link org.xml.sax.SAXException}，通常是包含错误位置信息的
 * {@link org.xml.sax.SAXParseException}。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see #getLineNumber()
 * @see org.xml.sax.SAXParseException
 */
@SuppressWarnings("serial")
public class XmlBeanDefinitionStoreException extends BeanDefinitionStoreException {

    /**
     * 创建一个新的 XmlBeanDefinitionStoreException。
     * @param resourceDescription 从中获取bean定义的资源描述
     * @param msg 详细消息（直接用作异常消息）
     * @param cause SAX异常（通常是SAXParseException）的根本原因
     * @see org.xml.sax.SAXParseException
     */
    public XmlBeanDefinitionStoreException(String resourceDescription, String msg, SAXException cause) {
        super(resourceDescription, msg, cause);
    }

    /**
     * 返回失败 XML 资源中的行号。
     * @return 如果可用（在出现 SAXParseException 的情况下），则返回行号；否则返回 -1
     * @see org.xml.sax.SAXParseException#getLineNumber()
     */
    public int getLineNumber() {
        Throwable cause = getCause();
        if (cause instanceof SAXParseException parseEx) {
            return parseEx.getLineNumber();
        }
        return -1;
    }
}
