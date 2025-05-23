// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。

根据Apache许可证版本2.0（以下简称“许可证”）许可；除非符合许可证规定，否则不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可证下权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 咨询链的工厂接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface AdvisorChainFactory {

    /**
     * 确定给定顾问链配置的一组 {@link org.aopalliance.intercept.MethodInterceptor} 对象。
     * @param config 以顾问对象的形式的 AOP 配置
     * @param method 被代理的方法
     * @param targetClass 目标类（可能为 {@code null} 以指示没有目标对象的代理，在这种情况下，方法的声明类是下一个最佳选项）
     * @return 方法拦截器的列表（也可能包括 InterceptorAndDynamicMethodMatchers）
     */
    List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, @Nullable Class<?> targetClass);
}
