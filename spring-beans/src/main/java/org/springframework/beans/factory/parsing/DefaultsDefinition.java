// 翻译完成 glm-4-flash
/** 版权所有 2002-2007 原作者或原作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“现状”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、特定用途的适用性或非侵权性。
* 请参阅许可证以了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.parsing;

import org.springframework.beans.BeanMetadataElement;

/**
 * 用于默认定义的标记接口，
 * 扩展 BeanMetadataElement 以继承源暴露。
 *
 * <p>具体的实现通常基于 '文档默认值'，
 * 例如在 XML 文档的根标签级别指定。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see org.springframework.beans.factory.xml.DocumentDefaultsDefinition
 * @see ReaderEventListener#defaultsRegistered(DefaultsDefinition)
 */
public interface DefaultsDefinition extends BeanMetadataElement {
}
