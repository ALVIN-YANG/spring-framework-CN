// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。

根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下网址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于对适销性、适用性和非侵权的保证。
有关许可证的具体语言管理权限和限制，请参阅许可证。*/
package org.springframework.aop.support;

import java.util.Map;
import java.util.WeakHashMap;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * 这是 {@link org.springframework.aop.IntroductionInterceptor} 接口的便捷实现。
 *
 * <p>与 {@link DelegatingIntroductionInterceptor} 不同，此类的一个实例可以被用来建议多个目标对象，并且每个目标对象将拥有其 <i>自己的</i> 委托（而 DelegatingIntroductionInterceptor 共享相同的委托，因此所有目标共享相同的状态）。
 *
 * <p>可以使用 {@code suppressInterface} 方法来抑制委托类实现的接口，但这些接口不应该被引入到拥有的 AOP 代理中。
 *
 * <p>如果委托是可序列化的，则此类的一个实例也是可序列化的。
 *
 * <p><i>注意：此类与 {@link DelegatingIntroductionInterceptor} 之间有一些实现上的相似性，这表明未来可能需要重构以提取一个公共的父类。</i>
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 * @see #suppressInterface
 * @see DelegatingIntroductionInterceptor
 */
@SuppressWarnings("serial")
public class DelegatePerTargetObjectIntroductionInterceptor extends IntroductionInfoSupport implements IntroductionInterceptor {

    /**
     * 保留对键的弱引用，因为我们不想干扰垃圾回收过程。
     */
    private final Map<Object, Object> delegateMap = new WeakHashMap<>();

    private final Class<?> defaultImplType;

    private final Class<?> interfaceType;

    public DelegatePerTargetObjectIntroductionInterceptor(Class<?> defaultImplType, Class<?> interfaceType) {
        this.defaultImplType = defaultImplType;
        this.interfaceType = interfaceType;
        // 现在创建一个新的委托（但不要将其存储在映射中）。
        // 我们这样做有两个原因：
        // 1) 在实例化代理时，如果出现问题，则尽早失败
        // 2) 仅一次填充接口映射表
        Object delegate = createNewDelegate();
        implementInterfacesOnObject(delegate);
        suppressInterface(IntroductionInterceptor.class);
        suppressInterface(DynamicIntroductionAdvice.class);
    }

    /**
     * /**
     *  子类可能需要覆盖此方法，如果它们想在环绕通知中执行自定义行为。
     *  然而，子类应该调用此方法，该方法处理引入的接口和转发到目标。
     * /
     */
    @Override
    @Nullable
    public Object invoke(MethodInvocation mi) throws Throwable {
        if (isMethodOnIntroducedInterface(mi)) {
            Object delegate = getIntroductionDelegateFor(mi.getThis());
            // 使用以下方法而不是直接使用反射，
            // 我们得到了对InvocationTargetException的正确处理
            // 如果引入的方法抛出异常。
            Object retVal = AopUtils.invokeJoinpointUsingReflection(delegate, mi.getMethod(), mi.getArguments());
            // 如果可能，对按摩（Massage）返回值进行修改：如果代理（Delegate）返回了自身，
            // 我们确实希望返回代理。
            if (retVal == delegate && mi instanceof ProxyMethodInvocation pmi) {
                retVal = pmi.getProxy();
            }
            return retVal;
        }
        return doProceed(mi);
    }

    /**
     * 使用提供的 {@link org.aopalliance.intercept.MethodInterceptor} 进行处理。
     * 子类可以重写此方法以拦截目标对象上的方法调用，这在需要监视被引入的对象时非常有用。此方法 <strong>绝对不会</strong> 在对引入的接口的 {@link MethodInvocation 方法调用} 上被调用。
     */
    @Nullable
    protected Object doProceed(MethodInvocation mi) throws Throwable {
        // 如果我们到达这里，就简单地传递调用。
        return mi.proceed();
    }

    private Object getIntroductionDelegateFor(@Nullable Object targetObject) {
        synchronized (this.delegateMap) {
            if (this.delegateMap.containsKey(targetObject)) {
                return this.delegateMap.get(targetObject);
            } else {
                Object delegate = createNewDelegate();
                this.delegateMap.put(targetObject, delegate);
                return delegate;
            }
        }
    }

    private Object createNewDelegate() {
        try {
            return ReflectionUtils.accessibleConstructor(this.defaultImplType).newInstance();
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Cannot create default implementation for '" + this.interfaceType.getName() + "' mixin (" + this.defaultImplType.getName() + "): " + ex);
        }
    }
}
