// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）授权；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或已书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory;

import org.springframework.lang.Nullable;

/**
 * 需要由在 {@link BeanFactory} 中使用的对象实现的接口，这些对象本身是单个对象的工厂。如果一个 Bean 实现了这个接口，它将被用作要暴露的对象的工厂，而不是直接作为本身要暴露的 Bean 实例。
 *
 * <p><b>注意：实现此接口的 Bean 不能用作普通 Bean。</b> FactoryBean 以 Bean 的形式定义，但暴露给 Bean 引用的对象（通过 {@link #getObject()}）始终是它创建的对象。
 *
 * <p>FactoryBeans 可以支持单例和原型，并且可以在需要时或启动时创建对象。通过实现 {@link SmartFactoryBean} 接口，可以暴露更细粒度的行为元数据。
 *
 * <p>此接口在框架内部被大量使用，例如用于 AOP 的 {@link org.springframework.aop.framework.ProxyFactoryBean} 或 {@link org.springframework.jndi.JndiObjectFactoryBean}。它也可以用于自定义组件；然而，这通常只适用于基础设施代码。
 *
 * <p><b>FactoryBean 是一个程序性契约。实现不应依赖于注解驱动的注入或其他反射功能。</b> 对象的调用（如 {@link #getObjectType()} 和 {@link #getObject()}）可能会在启动过程中很早到来，甚至早于任何后处理器设置。如果您需要访问其他 Bean，请实现 {@link BeanFactoryAware} 并以程序方式获取它们。
 *
 * <p><b>容器只负责管理 FactoryBean 实例的生命周期，而不是由 FactoryBean 创建的对象的生命周期。</b> 因此，暴露的 Bean 对象上的销毁方法（如 {@link java.io.Closeable#close()}）将 <i>不会</i> 自动调用。相反，FactoryBean 应该实现 {@link DisposableBean} 并将此类关闭调用委托给底层对象。
 *
 * <p>最后，FactoryBean 对象参与了包含 BeanFactory 的 Bean 创建同步。通常，除了在 FactoryBean 本身（或类似情况）中进行懒加载初始化之外，不需要进行内部同步。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 08.03.2003
 * @param <T> Bean 类型
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public interface FactoryBean<T> {

    /**
     * 可在 `org.springframework.beans.factory.config.BeanDefinition` 上设置属性的名称，
     *  这样工厂bean可以在其对象类型无法从工厂bean类中推断出来时发出信号。
     *  @since 5.2
     */
    String OBJECT_TYPE_ATTRIBUTE = "factoryBeanObjectType";

    /**
     * 返回由该工厂管理的对象实例（可能是共享的或独立的）。
     * <p>与{@link BeanFactory}类似，这允许支持单例模式和原型设计模式。
     * <p>如果在调用时此FactoryBean尚未完全初始化（例如，因为它涉及循环引用），则抛出相应的{@link FactoryBeanNotInitializedException}。
     * <p>自Spring 2.0起，FactoryBeans允许返回{@code null}对象。工厂将将其视为正常值使用；在这种情况下，它不再会抛出FactoryBeanNotInitializedException。鼓励FactoryBean实现根据适当的情况自己抛出FactoryBeanNotInitializedException。
     * @return bean的实例（可以是{@code null}）
     * @throws Exception 如果在创建过程中出现错误
     * @see FactoryBeanNotInitializedException
     */
    @Nullable
    T getObject() throws Exception;

    /**
     *  返回此FactoryBean创建的对象类型，
     *   * 或在事先未知的情况下返回{@code null}。
     *   * <p>这允许在不需要实例化对象的情况下检查特定类型的bean，
     *   * 例如在自动装配时。
     *   * <p>对于创建单例对象的实现，此方法应尽可能避免单例的创建；
     *   * 它应更倾向于提前估计类型。
     *   * 对于原型，在此处返回有意义的类型也是建议的。
     *   * <p>此方法可以在FactoryBean完全初始化之前被调用。
     *   * 它不得依赖于初始化期间创建的状态；当然，如果可用，它仍然可以使用这样的状态。
     *   * <p><b>注意：</b>自动装配将简单地忽略在此处返回{@code null}的FactoryBean。
     *   * 因此，强烈建议正确实现此方法，使用FactoryBean的当前状态。
     *   * @return 此FactoryBean创建的对象类型，
     *   * 或在调用时未知的情况下返回{@code null}
     *   * @see ListableBeanFactory#getBeansOfType
     */
    @Nullable
    Class<?> getObjectType();

    /**
     * 此工厂管理的对象是否为单例？也就是说，`#getObject()` 是否总是返回相同的对象
     * （一个可以被缓存的引用）？
     * <p><b>注意：</b>如果FactoryBean表示持有单例对象，则`#getObject()`返回的对象可能会被拥有它的BeanFactory缓存。因此，除非FactoryBean始终暴露相同的引用，否则不要返回`true`。
     * <p>FactoryBean自身的单例状态通常由拥有它的BeanFactory提供；通常，它必须在那里定义为单例。
     * <p><b>注意：</b>此方法返回`false`并不一定意味着返回的对象是独立的实例。实现了扩展的`#SmartFactoryBean`接口的实现可能通过其`#SmartFactoryBean#isPrototype()`方法显式地表示独立的实例。不实现此扩展接口的简单`#FactoryBean`实现假设如果`#isSingleton()`实现返回`false`，则始终返回独立的实例。
     * <p>默认实现返回`true`，因为`FactoryBean`通常管理单例实例。
     * @return 是否暴露的对象是单例
     * @see #getObject()
     * @see #SmartFactoryBean#isPrototype()
     */
    default boolean isSingleton() {
        return true;
    }
}
