// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.aopalliance.intercept;

import java.lang.reflect.Constructor;
import javax.annotation.Nonnull;

/**
 * 构造函数调用的描述，在构造函数调用时传递给拦截器。
 *
 * <p>构造函数调用是一个连接点，可以被构造函数拦截器拦截。
 *
 * @author Rod Johnson
 * @see ConstructorInterceptor
 */
public interface ConstructorInvocation extends Invocation {

    /**
     * 获取正在被调用的构造函数。
     * <p>此方法是对 {@link Joinpoint#getStaticPart()} 方法的友好实现（结果相同）。
     * @return 正在被调用的构造函数
     */
    @Nonnull
    Constructor<?> getConstructor();
}
