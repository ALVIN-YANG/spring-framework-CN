// 翻译完成 glm-4-flash
/** 版权所有 2002-2007 原作者或原作者。
*
* 根据 Apache License 2.0 版本（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.parsing;

import java.util.EventListener;

/**
 * 接口，用于在读取bean定义过程中接收组件、别名和导入注册的回调。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see ReaderContext
 */
public interface ReaderEventListener extends EventListener {

    /**
     * 通知已注册给定的默认值。
     * @param defaultsDefinition 默认值的描述符
     * @see org.springframework.beans.factory.xml.DocumentDefaultsDefinition
     */
    void defaultsRegistered(DefaultsDefinition defaultsDefinition);

    /**
     * 通知指定组件已注册。
     * @param componentDefinition 新组件的描述符
     * @see BeanComponentDefinition
     */
    void componentRegistered(ComponentDefinition componentDefinition);

    /**
     * 通知已注册给定的别名。
     * @param aliasDefinition 新别名的描述符
     */
    void aliasRegistered(AliasDefinition aliasDefinition);

    /**
     * 通知已处理指定的导入。
     * @param importDefinition 导入的描述符
     */
    void importProcessed(ImportDefinition importDefinition);
}
