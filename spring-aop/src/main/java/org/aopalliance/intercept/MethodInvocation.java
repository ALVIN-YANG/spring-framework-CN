// 翻译完成 glm-4-flash
/*版权所有 2002-2016 原作者或作者。
 
根据Apache许可证版本2.0（“许可证”）授权；
除非遵守许可证，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为还是适用性。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.aopalliance.intercept;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;

/**
 * 对方法调用的描述，在方法调用时传递给拦截器。
 *
 * <p>方法调用是一个连接点，可以被方法拦截器拦截。
 *
 * @author Rod Johnson
 * @see MethodInterceptor
 */
public interface MethodInvocation extends Invocation {

    /**
     * 获取被调用的方法。
     * <p>此方法是对 {@link Joinpoint#getStaticPart()} 方法的友好实现（结果相同）。
     * @return 被调用的方法
     */
    @Nonnull
    Method getMethod();
}
