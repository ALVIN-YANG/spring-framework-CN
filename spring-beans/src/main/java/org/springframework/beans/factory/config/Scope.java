// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.lang.Nullable;

/**
 * 用于由 {@link ConfigurableBeanFactory} 调用的策略接口，
 * 表示用于持有 bean 实例的目标范围。
 * 这允许通过注册特定键来扩展 BeanFactory 的标准范围
 * {@link ConfigurableBeanFactory#SCOPE_SINGLETON "singleton"} 和
 * {@link ConfigurableBeanFactory#SCOPE_PROTOTYPE "prototype"}
 * 以自定义的进一步范围。
 *
 * <p>如 {@link org.springframework.context.ApplicationContext} 实现，
 * 例如一个 {@link org.springframework.web.context.WebApplicationContext}，
 * 可以基于此范围 SPI 注册针对其环境特定的额外标准范围，
 * 例如，基于此范围 SPI 的
 * {@link org.springframework.web.context.WebApplicationContext#SCOPE_REQUEST "request"}
 * 和 {@link org.springframework.web.context.WebApplicationContext#SCOPE_SESSION "session"}。
 *
 * <p>尽管其主要用途是在 Web 环境中的扩展范围，
 * 但此 SPI 完全通用：它提供了从任何底层存储机制（如 HTTP 会话或自定义会话机制）
 * 获取和放置对象的能力。传递给此类中的 `get` 和 `remove` 方法的名称将标识当前范围中的目标对象。
 *
 * <p>期望 `Scope` 实现是线程安全的。如果需要，一个 `Scope` 实例可以同时与多个 bean 工厂一起使用，
 * 并且任何数量的线程可以从任何数量的工厂并发访问 `Scope`。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see ConfigurableBeanFactory#registerScope
 * @see CustomScopeConfigurer
 * @see org.springframework.aop.scope.ScopedProxyFactoryBean
 * @see org.springframework.web.context.request.RequestScope
 * @see org.springframework.web.context.request.SessionScope
 */
public interface Scope {

    /**
     * 从底层作用域中返回具有给定名称的对象，
     * 如果在底层存储机制中未找到，则通过调用
     * {@link org.springframework.beans.factory.ObjectFactory#getObject()} 创建它。
     * <p>这是作用域的核心操作，也是唯一绝对必需的操作。
     * @param name 要检索的对象的名称
     * @param objectFactory 如果在底层存储机制中不存在，将使用此
     * {@link ObjectFactory} 来创建作用域对象
     * @return 所需的对象（绝不会为 {@code null}）
     * @throws IllegalStateException 如果底层作用域当前不处于活动状态
     */
    Object get(String name, ObjectFactory<?> objectFactory);

    /**
     * 从底层作用域中移除具有给定 {@code name} 的对象。
     * <p>如果没有找到对象，则返回 {@code null}；否则返回被移除的 {@code Object}。
     * <p>注意，实现类还应移除指定对象的已注册的销毁回调，如果有的话。然而，在这种情况下，它 <i>不需要</i> 执行已注册的销毁回调，因为对象将由调用者（如果适用）销毁。
     * <p><b>注意：这是一个可选操作。</b> 如果实现类不支持显式移除对象，则可能会抛出 {@link UnsupportedOperationException}。
     * @param name 要移除的对象的名称
     * @return 被移除的对象，或者如果不存在对象则返回 {@code null}
     * @throws IllegalStateException 如果底层作用域当前不是活动的
     * @see #registerDestructionCallback
     */
    @Nullable
    Object remove(String name);

    /**
     * 在指定作用域中的对象销毁时执行回调（或者在作用域整体销毁时执行，如果作用域不销毁单个对象，而是整体终止）。
     * <p><b>注意：这是一个可选操作。</b> 此方法仅对具有实际销毁配置的作用域bean（DisposableBean、destroy-method、DestructionAwareBeanPostProcessor）进行调用。
     * 实现类应尽力在适当的时间执行给定的回调。如果底层运行环境根本不支持此类回调，则必须忽略该回调并记录相应的警告。
     * <p>请注意，“销毁”指的是对象作为作用域自身生命周期的一部分的自动销毁，而不是单个作用域对象被应用程序显式移除。
     * 如果通过此外观的`#remove(String)`方法移除作用域对象，则应同时移除已注册的销毁回调，假设被移除的对象将被重用或手动销毁。
     * @param name 要执行销毁回调的对象的名称
     * @param callback 要执行的销毁回调。
     * 注意，传入的Runnable不会抛出异常，因此可以安全地在没有包围的try-catch块中执行。
     * 此外，只要其目标对象也是可序列化的，Runnable通常也是可序列化的。
     * @throws IllegalStateException 如果底层作用域当前不活跃
     * @see org.springframework.beans.factory.DisposableBean
     * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getDestroyMethodName()
     * @see DestructionAwareBeanPostProcessor
     */
    void registerDestructionCallback(String name, Runnable callback);

    /**
     * 根据给定的键解析上下文对象（如果存在）。
     * 例如，对于键 "request" 的 HttpServletRequest 对象。
     * @param key 上下文键
     * @return 对应的对象，如果没有找到则返回 {@code null}
     * @throws IllegalStateException 如果底层作用域当前不活跃
     */
    @Nullable
    Object resolveContextualObject(String key);

    /**
     * 返回当前底层作用域的<em>会话ID</em>（如果有）。
     * <p>会话ID的确切含义取决于底层存储机制。在会话作用域对象的场景下，会话ID通常等于（或由）`@link jakarta.servlet.http.HttpSession#getId() 会话ID`派生；在整体会话内的自定义会话场景下，当前会话的特定ID是合适的。
     * <p><b>注意：这是一个可选操作。</b>如果底层存储机制没有明显的ID候选者，此方法的实现返回`null`是完全有效的。
     * @return 会话ID，或当前作用域没有会话ID时返回`null`
     * @throws IllegalStateException 如果底层作用域当前不处于活动状态
     */
    @Nullable
    String getConversationId();
}
