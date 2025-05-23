// 翻译完成 glm-4-flash
/*版权所有 2002-2020，原作者或原作者们。

根据Apache License，版本2.0（“许可证”）授权；
除非适用法律要求或经书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下地址获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律规定或经书面同意，否则在许可证下分发的软件
按“现状”分发，不提供任何明示或暗示的保证或条件。
有关权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.interceptor;

import java.io.Serializable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * 拦截器，它将当前的{@link org.aopalliance.intercept.MethodInvocation}暴露为一个线程局部对象。我们偶尔需要这样做；例如，当一个切点（例如，一个AspectJ表达式切点）需要知道完整的调用上下文时。
 *
 * <p>除非确实有必要，否则请勿使用此拦截器。目标对象通常不应了解Spring AOP，因为这会创建对Spring API的依赖。尽可能地将目标对象保持为普通的POJO。
 *
 * <p>如果使用，此拦截器通常将是拦截链中的第一个。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public final class ExposeInvocationInterceptor implements MethodInterceptor, PriorityOrdered, Serializable {

    /**
     * 该类的单例实例。
     */
    public static final ExposeInvocationInterceptor INSTANCE = new ExposeInvocationInterceptor();

    /**
     * 该类的单例顾问。当使用 Spring AOP 时，优先使用它而不是 INSTANCE，因为它可以避免创建一个新的顾问来包装实例。
     */
    public static final Advisor ADVISOR = new DefaultPointcutAdvisor(INSTANCE) {

        @Override
        public String toString() {
            return ExposeInvocationInterceptor.class.getName() + ".ADVISOR";
        }
    };

    private static final ThreadLocal<MethodInvocation> invocation = new NamedThreadLocal<>("Current AOP method invocation");

    /**
     * 返回与当前调用关联的 AOP Alliance MethodInvocation 对象。
     * @return 与当前调用关联的调用对象
     * @throws IllegalStateException 如果没有正在进行的 AOP 调用，
     * 或者如果未将 ExposeInvocationInterceptor 添加到此拦截器链中
     */
    public static MethodInvocation currentInvocation() throws IllegalStateException {
        MethodInvocation mi = invocation.get();
        if (mi == null) {
            throw new IllegalStateException("No MethodInvocation found: Check that an AOP invocation is in progress and that the " + "ExposeInvocationInterceptor is upfront in the interceptor chain. Specifically, note that " + "advices with order HIGHEST_PRECEDENCE will execute before ExposeInvocationInterceptor! " + "In addition, ExposeInvocationInterceptor and ExposeInvocationInterceptor.currentInvocation() " + "must be invoked from the same thread.");
        }
        return mi;
    }

    /**
     * 确保只能创建规范实例。
     */
    private ExposeInvocationInterceptor() {
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation mi) throws Throwable {
        MethodInvocation oldInvocation = invocation.get();
        invocation.set(mi);
        try {
            return mi.proceed();
        } finally {
            invocation.set(oldInvocation);
        }
    }

    @Override
    public int getOrder() {
        return PriorityOrdered.HIGHEST_PRECEDENCE + 1;
    }

    /**
     * 用于支持序列化。在反序列化时替换为规范实例，保护单例模式。
     * <p>是重写{@code equals}方法的替代方案。
     */
    private Object readResolve() {
        return INSTANCE;
    }
}
