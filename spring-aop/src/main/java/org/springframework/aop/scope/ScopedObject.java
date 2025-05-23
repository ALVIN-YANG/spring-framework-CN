// 翻译完成 glm-4-flash
/** 版权所有 2002-2012，原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证，了解具体管理许可权限和限制的语言。*/
package org.springframework.aop.scope;

import org.springframework.aop.RawTargetAccess;

/**
 * AOP（面向切面编程）中用于作用域对象的引入接口。
 *
 * <p>从{@link ScopedProxyFactoryBean}创建的对象可以转换为这个接口，从而能够访问原始目标对象，并能够以编程方式移除目标对象。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see ScopedProxyFactoryBean
 */
public interface ScopedObject extends RawTargetAccess {

    /**
     * 返回此作用域对象代理背后的当前目标对象，以原始形式（如存储在目标作用域中）返回。
     * <p>原始目标对象可以传递给无法处理作用域代理对象的持久化提供者。
     * @return 此作用域对象代理背后的当前目标对象
     */
    Object getTargetObject();

    /**
     * 从其目标作用域中移除此对象，例如从后端会话中移除。
     * <p>注意，之后不再可以对作用域内的对象进行调用（至少在当前线程中如此，即目标作用域中的目标对象完全相同）。
     */
    void removeFromScope();
}
