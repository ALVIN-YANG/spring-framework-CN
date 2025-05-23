// 翻译完成 glm-4-flash
/** 版权所有 2002-2021，原作者或原作者。
*
* 根据 Apache License 2.0（“许可证”），除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.aopalliance.intercept;

import java.lang.reflect.AccessibleObject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 此接口表示一个通用的运行时连接点（在面向切面编程术语中）。
 *
 * <p>运行时连接点是在静态连接点（即程序中的一个位置）上发生的<i>事件</i>。例如，方法的调用是方法（静态连接点）上的运行时连接点。可以使用{@link #getStaticPart()}方法通用地检索给定连接点的静态部分。
 *
 * <p>在拦截器框架的上下文中，运行时连接点是对可访问对象（方法、构造函数、字段）的访问的具象化，即连接点的静态部分。它被传递给安装在静态连接点上的拦截器。
 *
 * @author Rod Johnson
 * @see Interceptor
 */
public interface Joinpoint {

    /**
     * 继续处理链中的下一个拦截器。
     * <p>此方法的具体实现和语义取决于实际的连接点类型（请参阅子接口）。
     * @return 请参考子接口中proceed的定义
     * @throws Throwable 如果连接点抛出异常
     */
    @Nullable
    Object proceed() throws Throwable;

    /**
     * 返回包含当前连接点静态部分的对象。
     * <p>例如，方法调用的目标对象。
     * @return 对象（如果可访问的对象是静态的，则可能为null）
     */
    @Nullable
    Object getThis();

    /**
     * 返回此连接点的静态部分。
     * <p>静态部分是一个可访问的对象，在其上安装了一连串的拦截器。
     */
    @Nonnull
    AccessibleObject getStaticPart();
}
