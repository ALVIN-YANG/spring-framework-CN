// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml;

import org.springframework.lang.Nullable;

/**
 * 由{@link org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader}使用，以定位特定命名空间URI的{@link NamespaceHandler}实现。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader
 */
@FunctionalInterface
public interface NamespaceHandlerResolver {

    /**
     * 解析命名空间URI并返回找到的 {@link NamespaceHandler} 实现类。
     * @param namespaceUri 相关的命名空间URI
     * @return 找到的 {@link NamespaceHandler}（可能为 {@code null}）
     */
    @Nullable
    NamespaceHandler resolve(String namespaceUri);
}
