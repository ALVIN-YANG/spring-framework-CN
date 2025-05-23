// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache许可证版本2.0（以下简称“许可证”）授权；
除非适用法律要求或经书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下网址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为还是适销性。
有关许可的具体语言和限制，请参阅许可证。*/
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;

/**
 * 顾问适配器注册表的接口。
 *
 * <p><i>这是一个SPI接口，任何Spring用户都不应实现。</i>
 *
 * @author Rod Johnson
 * @author Rob Harrop
 */
public interface AdvisorAdapterRegistry {

    /**
     * 返回一个包装给定咨询（advice）的 {@link Advisor}。
     * <p>默认情况下，至少应该支持
     * {@link org.aopalliance.intercept.MethodInterceptor},
     * {@link org.springframework.aop.MethodBeforeAdvice},
     * {@link org.springframework.aop.AfterReturningAdvice},
     * {@link org.springframework.aop.ThrowsAdvice}。
     * @param advice 应该是一个咨询的对象
     * @return 包裹给定咨询的 Advisor（永不返回 {@code null}；
     * 如果咨询参数是一个 Advisor，则应原样返回）
     * @throws UnknownAdviceTypeException 如果没有注册的咨询适配器
     * 可以包装假定的咨询
     */
    Advisor wrap(Object advice) throws UnknownAdviceTypeException;

    /**
     * 返回一个 AOP Alliance 方法拦截器数组，以便在基于拦截的框架中使用给定的 Advisor。
     * <p>如果与 {@link Advisor} 关联的点是切点（Pointcut），且它是一个 {@link org.springframework.aop.PointcutAdvisor}，则无需担心：只需返回一个拦截器。
     * @param advisor 要为其查找拦截器的 Advisor
     * @return 一个方法拦截器数组，用于暴露此 Advisor 的行为
     * @throws UnknownAdviceTypeException 如果 Advisor 类型不被任何已注册的 AdvisorAdapter 所理解
     */
    MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException;

    /**
     * 注册给定的 {@link AdvisorAdapter}。注意，对于 AOP Alliance Interceptors 或 Spring Advices，不需要注册适配器：这些必须由一个 {@code AdvisorAdapterRegistry} 实现自动识别。
     * @param adapter 理解特定 Advisor 或 Advice 类型的 AdvisorAdapter
     */
    void registerAdvisorAdapter(AdvisorAdapter adapter);
}
