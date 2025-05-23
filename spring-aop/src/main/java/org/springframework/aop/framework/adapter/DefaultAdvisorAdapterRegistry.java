// 翻译完成 glm-4-flash
/*版权所有 2002-2022，原作者或作者。

根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则根据许可证分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，有关权限和限制的具体语言由许可证规定。*/
package org.springframework.aop.framework.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * {@link AdvisorAdapterRegistry} 接口的默认实现。
 * 支持 {@link org.aopalliance.intercept.MethodInterceptor}、
 * {@link org.springframework.aop.MethodBeforeAdvice}、
 * {@link org.springframework.aop.AfterReturningAdvice}、
 * {@link org.springframework.aop.ThrowsAdvice}。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry, Serializable {

    private final List<AdvisorAdapter> adapters = new ArrayList<>(3);

    /**
     * 创建一个新的 DefaultAdvisorAdapterRegistry，并注册已知的适配器。
     */
    public DefaultAdvisorAdapterRegistry() {
        registerAdvisorAdapter(new MethodBeforeAdviceAdapter());
        registerAdvisorAdapter(new AfterReturningAdviceAdapter());
        registerAdvisorAdapter(new ThrowsAdviceAdapter());
    }

    @Override
    public Advisor wrap(Object adviceObject) throws UnknownAdviceTypeException {
        if (adviceObject instanceof Advisor advisor) {
            return advisor;
        }
        if (!(adviceObject instanceof Advice advice)) {
            throw new UnknownAdviceTypeException(adviceObject);
        }
        if (advice instanceof MethodInterceptor) {
            // 如此知名，甚至不需要适配器。
            return new DefaultPointcutAdvisor(advice);
        }
        for (AdvisorAdapter adapter : this.adapters) {
            // 检查其是否受支持。
            if (adapter.supportsAdvice(advice)) {
                return new DefaultPointcutAdvisor(advice);
            }
        }
        throw new UnknownAdviceTypeException(advice);
    }

    @Override
    public MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException {
        List<MethodInterceptor> interceptors = new ArrayList<>(3);
        Advice advice = advisor.getAdvice();
        if (advice instanceof MethodInterceptor methodInterceptor) {
            interceptors.add(methodInterceptor);
        }
        for (AdvisorAdapter adapter : this.adapters) {
            if (adapter.supportsAdvice(advice)) {
                interceptors.add(adapter.getInterceptor(advisor));
            }
        }
        if (interceptors.isEmpty()) {
            throw new UnknownAdviceTypeException(advisor.getAdvice());
        }
        return interceptors.toArray(new MethodInterceptor[0]);
    }

    @Override
    public void registerAdvisorAdapter(AdvisorAdapter adapter) {
        this.adapters.add(adapter);
    }
}
