// 翻译完成 glm-4-flash
/** 版权所有 2002-2023，原作者或原作者。
*
* 根据 Apache License，版本 2.0（“许可证”），除非根据法律规定或书面同意，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证以了解具体的管理权限和限制。*/
package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * 基础接口，用于持有 AOP <b>建议</b>（在连接点处执行的动作）以及确定建议适用性的过滤器（例如，切入点）。<i>此接口不是供 Spring 用户使用，而是为了允许支持不同类型的建议时的通用性。</i>
 *
 * <p>Spring AOP 依赖于通过方法 <b>拦截</b>提供的 <b>环绕建议</b>，符合 AOP Alliance 拦截 API。Advisor 接口允许支持不同类型的建议，如 <b>前置</b>和 <b>后置</b>建议，这些建议无需使用拦截来实现。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface Advisor {

    /**
     * 通用的占位符，用于在未配置适当建议（尚无）的情况下从
     * {@link #getAdvice()} 返回一个空的 {@code Advice}。
     * @since 5.0
     */
    Advice EMPTY_ADVICE = new Advice() {
    };

    /**
     * 返回此方面的建议部分。建议可能是一个拦截器、一个前置建议、一个抛出异常建议等。
     * @return 如果切入点匹配，则应应用的建议
     * @see org.aopalliance.intercept.MethodInterceptor
     * @see BeforeAdvice
     * @see ThrowsAdvice
     * @see AfterReturningAdvice
     */
    Advice getAdvice();

    /**
     * 返回此建议是否与特定实例相关联（例如，创建一个混入类）或与从同一Spring bean工厂获取的所有建议类的实例共享。
     * <p><b>注意：此方法目前未被框架使用。</b>
     * 典型的Advisor实现总是返回{@code true}。
     * 使用单例/原型bean定义或适当的程序性代理创建来确保Advisors具有正确的生命周期模型。
     * <p>自6.0.10版本起，默认实现返回{@code true}。
     * @return 是否此建议与特定目标实例相关联
     */
    default boolean isPerInstance() {
        return true;
    }
}
