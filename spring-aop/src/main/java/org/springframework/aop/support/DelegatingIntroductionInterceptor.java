// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式，无论是明示的还是暗示的。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.aop.support;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 简便实现 {@link org.springframework.aop.IntroductionInterceptor} 接口。
 *
 * <p>子类只需扩展此类并实现要引入的接口。在这种情况下，代理者是子类实例本身。或者，一个单独的代理者可以实拟接口，并通过 delegate bean 属性设置。
 *
 * <p>代理者或子类可以实现任意数量的接口。除了 IntroductionInterceptor 之外的所有接口默认由子类或代理者获取。
 *
 * <p>可以使用 {@code suppressInterface} 方法来抑制代理者实现的但不应引入到所属 AOP 代理中的接口。
 *
 * <p>如果代理者是可序列化的，则此类的实例也是可序列化的。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16.11.2003
 * @see #suppressInterface
 * @see DelegatePerTargetObjectIntroductionInterceptor
 */
@SuppressWarnings("serial")
public class DelegatingIntroductionInterceptor extends IntroductionInfoSupport implements IntroductionInterceptor {

    /**
     * 实际实现接口的对象。
     * 如果子类实现了引入的接口，则可能是 "this"。
     */
    @Nullable
    private Object delegate;

    /**
     * 构造一个新的DelegatingIntroductionInterceptor，提供一个实现将要引入的接口的代理。
     * @param delegate 实现引入的接口的代理
     */
    public DelegatingIntroductionInterceptor(Object delegate) {
        init(delegate);
    }

    /**
     * 构造一个新的 DelegatingIntroductionInterceptor 实例。
     * 代理将是一个子类，它必须实现额外的接口。
     */
    protected DelegatingIntroductionInterceptor() {
        init(this);
    }

    /**
     * 两个构造函数都使用这个初始化方法，因为无法从一个构造函数传递“this”引用到另一个构造函数。
     * @param delegate 被委托的对象
     */
    private void init(Object delegate) {
        Assert.notNull(delegate, "Delegate must not be null");
        this.delegate = delegate;
        implementInterfacesOnObject(delegate);
        // 我们不希望暴露控制接口
        suppressInterface(IntroductionInterceptor.class);
        suppressInterface(DynamicIntroductionAdvice.class);
    }

    /**
     * 子类可能需要重写此方法，如果它们想在环绕通知（around advice）中执行自定义行为。然而，子类应调用此方法，该方法处理引入的接口以及转发到目标对象。
     */
    @Override
    @Nullable
    public Object invoke(MethodInvocation mi) throws Throwable {
        if (isMethodOnIntroducedInterface(mi)) {
            // 使用以下方法而不是直接使用反射，我们
            // 获取正确处理 InvocationTargetException 的方式
            // 如果引入的方法抛出异常。
            Object retVal = AopUtils.invokeJoinpointUsingReflection(this.delegate, mi.getMethod(), mi.getArguments());
            // 如果可能，修改按摩（Massage）返回值：如果委托者返回了自身，
            // 我们确实希望返回代理。
            if (retVal == this.delegate && mi instanceof ProxyMethodInvocation pmi) {
                Object proxy = pmi.getProxy();
                if (mi.getMethod().getReturnType().isInstance(proxy)) {
                    retVal = proxy;
                }
            }
            return retVal;
        }
        return doProceed(mi);
    }

    /**
     * 使用提供的 {@link org.aopalliance.intercept.MethodInterceptor} 进行操作。
     * 子类可以重写此方法以拦截目标对象的 方法调用，这在需要监控被引入的对象时非常有用。此方法对于在引入的接口上的 {@link MethodInvocation 方法调用} <strong>从不</strong> 被调用。
     */
    @Nullable
    protected Object doProceed(MethodInvocation mi) throws Throwable {
        // 如果我们到达这里，只需将调用传递下去。
        return mi.proceed();
    }
}
