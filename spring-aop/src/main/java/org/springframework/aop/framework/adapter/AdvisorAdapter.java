// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或原作者。

根据Apache License, Version 2.0（“许可证”），除非法律要求或书面同意，否则您不得使用此文件，除非符合许可证。

您可以在以下网址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非根据适用法律或书面同意，否则在许可证下分发的软件按照“原样”分发，不提供任何形式的明示或暗示保证。
有关许可证下管理许可权和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework.adapter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;

/**
 * 接口，允许扩展 Spring AOP 框架以处理新的 Advisor 和 Advice 类型。
 *
 * <p>实现该接口的对象可以从自定义的 Advice 类型创建 AOP Alliance 拦截器，从而使这些 Advice 类型能够在 Spring AOP 框架中使用，Spring AOP 框架底层使用拦截机制。
 *
 * <p>大多数 Spring 用户不需要实现此接口；只有当你需要向 Spring 引入更多 Advisor 或 Advice 类型时才需要这样做。
 *
 * @author Rod Johnson
 */
public interface AdvisorAdapter {

    /**
     * 此适配器是否理解这个建议对象？使用包含此建议作为参数的顾问调用
     * {@code getInterceptors} 方法是否有效？
     * @param advice 一个建议，如一个 BeforeAdvice
     * @return 是否此适配器理解给定的建议对象
     * @see #getInterceptor(org.springframework.aop.Advisor)
     * @see org.springframework.aop.BeforeAdvice
     */
    boolean supportsAdvice(Advice advice);

    /**
     * 返回一个 AOP Alliance 方法拦截器，将给定建议的行为暴露给基于拦截的 AOP 框架。
     * <p>无需担心 Advisor 中包含的任何切入点；AOP 框架将负责检查切入点。
     * @param advisor Advisor 对象。此对象的 supportsAdvice() 方法必须返回 true
     * @return 为此 Advisor 提供的 AOP Alliance 拦截器。无需为了效率缓存实例，因为 AOP 框架会缓存建议链。
     */
    MethodInterceptor getInterceptor(Advisor advisor);
}
