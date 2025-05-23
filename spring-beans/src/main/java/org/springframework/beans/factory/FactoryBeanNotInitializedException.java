// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory;

import org.springframework.beans.FatalBeanException;

/**
 * 从 FactoryBean 的 `getObject()` 方法抛出的异常，如果该 Bean 还未完全初始化，例如因为它参与了循环引用。
 *
 * <p>注意：与普通 Bean 不同，不能通过积极缓存单例实例来解决 FactoryBean 的循环引用问题。原因是每个 FactoryBean 在返回创建的 Bean 之前都需要完全初始化，而只有特定的普通 Bean 需要初始化——也就是说，如果协作的 Bean 在初始化时实际调用它们，而不是仅仅存储引用。
 *
 * @author Juergen Hoeller
 * @since 30.10.2003
 * @see FactoryBean#getObject()
 */
@SuppressWarnings("serial")
public class FactoryBeanNotInitializedException extends FatalBeanException {

    /**
     * 创建一个新的 FactoryBeanNotInitializedException，使用默认消息。
     */
    public FactoryBeanNotInitializedException() {
        super("FactoryBean is not fully initialized yet");
    }

    /**
     * 创建一个新的 FactoryBeanNotInitializedException，带有给定的消息。
     * @param msg 详细消息
     */
    public FactoryBeanNotInitializedException(String msg) {
        super(msg);
    }
}
