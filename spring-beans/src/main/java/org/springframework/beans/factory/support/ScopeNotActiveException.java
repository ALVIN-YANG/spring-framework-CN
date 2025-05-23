// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanCreationException;

/**
 * {@link BeanCreationException} 的一个子类，表示目标作用域不可用，例如在请求或会话作用域的情况下。
 *
 * @author Juergen Hoeller
 * @since 5.3
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see org.springframework.beans.factory.config.Scope
 * @see AbstractBeanDefinition#setScope
 */
@SuppressWarnings("serial")
public class ScopeNotActiveException extends BeanCreationException {

    /**
     * 创建一个新的 ScopeNotActiveException 对象。
     * @param beanName 所请求的 Bean 名称
     * @param scopeName 目标作用域的名称
     * @param cause 根本原因，通常来自 {@link org.springframework.beans.factory.config.Scope#get}
     */
    public ScopeNotActiveException(String beanName, String scopeName, IllegalStateException cause) {
        super(beanName, "Scope '" + scopeName + "' is not active for the current thread; consider " + "defining a scoped proxy for this bean if you intend to refer to it from a singleton", cause);
    }
}
