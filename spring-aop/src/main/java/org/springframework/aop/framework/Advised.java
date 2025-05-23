// 翻译完成 glm-4-flash
/*版权所有 2002-2020 原作者或作者。
 
根据Apache许可证版本2.0（以下简称“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下网址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的明示或暗示保证。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.TargetSource;

/**
 * 该接口由持有 AOP 代理工厂配置的类实现。该配置包括拦截器和其他建议、顾问以及被代理的接口。
 *
 * <p>从 Spring 获取的任何 AOP 代理都可以转换为该接口，以便操作其 AOP 建议。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see org.springframework.aop.framework.AdvisedSupport
 */
public interface Advised extends TargetClassAware {

    /**
     * 返回是否配置了Advised配置已冻结，
     * 在这种情况下，无法进行任何建议更改。
     */
    boolean isFrozen();

    /**
     * 我们是代理整个目标类而不是指定的接口吗？
     */
    boolean isProxyTargetClass();

    /**
     * 返回由 AOP 代理代理的接口。
     * <p>不会包括目标类，该类也可能被代理。
     */
    Class<?>[] getProxiedInterfaces();

    /**
     * 判断给定的接口是否被代理。
     * @param intf 需要检查的接口
     */
    boolean isInterfaceProxied(Class<?> intf);

    /**
     * 更改此 {@code Advised} 对象使用的 {@code TargetSource}。
     * <p>仅在配置未被 {@linkplain #isFrozen 冻结} 的情况下有效。
     * @param targetSource 要使用的新 TargetSource
     */
    void setTargetSource(TargetSource targetSource);

    /**
     * 返回此 {@code Advised} 对象所使用的 {@code TargetSource}。
     */
    TargetSource getTargetSource();

    /**
     * 设置是否由 AOP 框架将代理作为 {@link ThreadLocal} 暴露，以便通过 {@link AopContext} 类进行检索。
     * <p>如果被建议的对象需要调用自身的方法并应用建议，则可能需要暴露代理。否则，如果被建议的对象在调用方法时使用 {@code this}，则不会应用任何建议。
     * <p>默认为 {@code false}，以实现最佳性能。
     */
    void setExposeProxy(boolean exposeProxy);

    /**
     * 返回工厂是否应将代理暴露为 {@link ThreadLocal}。
     * <p>在某些情况下，可能需要暴露代理，如果被建议的对象需要在其自身上调用方法并应用建议。否则，如果被建议的对象在调用方法时使用了 `this`，则不会应用任何建议。
     * <p>获取代理类似于 EJB 调用 `getEJBObject()`。
     * @see AopContext
     */
    boolean isExposeProxy();

    /**
     * 设置此代理配置是否已预先过滤，以便它只包含适用的顾问（匹配此代理的目标类）。
     * <p>默认值为 "false"。如果顾问已经被预先过滤，即表示可以跳过 ClassFilter 检查，
     * 在构建代理调用实际顾问链时，请将此设置为 "true"。
     * @see org.springframework.aop.ClassFilter
     */
    void setPreFiltered(boolean preFiltered);

    /**
     * 返回此代理配置是否为预过滤，以便它只包含适用的顾问（匹配此代理的目标类）。
     */
    boolean isPreFiltered();

    /**
     * 返回申请此代理的顾问。
     * @return 申请此代理的顾问列表（永远不会为 {@code null}）
     */
    Advisor[] getAdvisors();

    /**
     * 返回申请此代理的顾问数量。
     * <p>默认实现委托给 {@code getAdvisors().length}。
     * @since 5.3.1
     */
    default int getAdvisorCount() {
        return getAdvisors().length;
    }

    /**
     * 在顾问链末尾添加一个顾问。
     * <p>顾问可能是一个 {@link org.springframework.aop.IntroductionAdvisor}，
     * 当从相关工厂获取下一个代理时，将可使用新接口。
     * @param advisor 要添加到链末尾的顾问
     * @throws AopConfigException 如果出现无效的建议时抛出异常
     */
    void addAdvisor(Advisor advisor) throws AopConfigException;

    /**
     * 在链中的指定位置添加一个顾问。
     * @param advisor 要添加到链中指定位置的顾问
     * @param pos 链中的位置（0表示头部）。必须是有效的。
     * @throws AopConfigException 如果出现无效的建议时抛出异常
     */
    void addAdvisor(int pos, Advisor advisor) throws AopConfigException;

    /**
     * 移除给定的顾问。
     * @param advisor 要移除的顾问
     * @return 如果顾问被移除则返回 {@code true}；如果顾问未找到因此无法被移除则返回 {@code false}
     */
    boolean removeAdvisor(Advisor advisor);

    /**
     * 移除给定索引处的顾问。
     * @param index 要移除的顾问的索引
     * @throws AopConfigException 如果索引无效
     */
    void removeAdvisor(int index) throws AopConfigException;

    /**
     * 返回给定顾问的索引（从0开始），如果没有此类顾问适用于此代理，则返回-1。
     * <p>此方法返回的值可用于对顾问数组进行索引。
     * @param advisor 要搜索的顾问
     * @return 此顾问的索引（从0开始），如果没有此类顾问则返回-1
     */
    int indexOf(Advisor advisor);

    /**
     * 替换给定的顾问。
     * <p><b>注意：</b>如果顾问是 {@link org.springframework.aop.IntroductionAdvisor}
     * 而替换项不是或实现不同的接口，则需要重新获取代理，否则旧接口将不会得到支持，而新接口将不会实现。
     * @param a 要替换的顾问
     * @param b 替换它的顾问
     * @return 是否已替换。如果顾问在顾问列表中未找到，则此方法返回 {@code false} 且不执行任何操作。
     * @throws AopConfigException 在存在无效建议的情况下抛出异常
     */
    boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException;

    /**
     * 将给定的 AOP Alliance 通知添加到通知（拦截器）链的末尾。
     * <p>这将被包装在一个具有始终适用的切点的 DefaultPointcutAdvisor 中，并以这种包装形式从该包装的 {@code getAdvisors()} 方法返回。
     * <p>请注意，给定的通知将应用于代理上的所有调用，甚至包括 {@code toString()} 方法！请使用适当的通知实现或指定适用于更窄方法集的切点。
     * @param advice 要添加到链末尾的通知
     * @throws AopConfigException 如果通知无效
     * @see #addAdvice(int, Advice)
     * @see org.springframework.aop.support.DefaultPointcutAdvisor
     */
    void addAdvice(Advice advice) throws AopConfigException;

    /**
     * 在指定的位置将给定的AOP Alliance Advice添加到advice链中。
     * <p>这将被包装在具有始终生效的pointcut的{@link org.springframework.aop.support.DefaultPointcutAdvisor}中，
     * 并且以这种包装形式从该方法的{@link #getAdvisors()}返回。
     * <p>注意：给定的advice将应用于代理的所有调用，即使是对{@code toString()}方法的调用！
     * 使用适当的advice实现或指定适用于更窄方法集的适当pointcut。
     * @param pos 从0（头部）开始的索引
     * @param advice 要添加到advice链中指定位置的advice
     * @throws AopConfigException 如果advice无效则抛出异常
     */
    void addAdvice(int pos, Advice advice) throws AopConfigException;

    /**
     * 移除包含给定建议的顾问。
     * @param advice 要移除的建议
     * @return 如果找到并移除了该建议，则返回 {@code true}；
     * 如果没有找到此类建议，则返回 {@code false}
     */
    boolean removeAdvice(Advice advice);

    /**
     * 返回给定AOP Alliance建议的索引（从0开始），如果没有这样的建议属于此代理，则返回-1。
     * <p>此方法返回的值可以用于索引到顾问数组。
     * @param advice 要搜索的AOP Alliance建议
     * @return 此建议的索引（从0开始），如果没有这样的建议则返回-1
     */
    int indexOf(Advice advice);

    /**
     * 由于通常会将 {@code toString()} 方法委托给目标对象，
     * 因此此方法返回 AOP 代理的等效字符串描述。
     * @return 返回代理配置的字符串描述
     */
    String toProxyConfigString();
}
