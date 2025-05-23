// 翻译完成 glm-4-flash
/** 版权所有 2002-2016，原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，除非适用法律要求或经书面同意，否则您不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体管理权限和限制的内容。*/
package org.aopalliance.intercept;

import javax.annotation.Nonnull;

/**
 * 拦截新对象的构造过程。
 *
 * <p>用户应实现 {@link #construct(ConstructorInvocation)} 方法来修改原始行为。例如，以下类实现了一个单例拦截器（只允许被拦截类的唯一实例）：
 *
 * <pre class=code>
 * class DebuggingInterceptor implements ConstructorInterceptor {
 *   Object instance=null;
 *
 *   Object construct(ConstructorInvocation i) throws Throwable {
 *     if(instance==null) {
 *       return instance=i.proceed();
 *     } else {
 *       throw new Exception("singleton does not allow multiple instances");
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Rod Johnson
 */
public interface ConstructorInterceptor extends Interceptor {

    /**
     * 实现此方法以在新建对象构造前后执行额外处理。礼貌的实现当然会调用{@link Joinpoint#proceed()}。
     * @param invocation 构造连接点
     * @return 新创建的对象，也是调用{@link Joinpoint#proceed()}的结果；可能被拦截器替换
     * @throws Throwable 如果拦截器或目标对象抛出异常
     */
    @Nonnull
    Object construct(ConstructorInvocation invocation) throws Throwable;
}
