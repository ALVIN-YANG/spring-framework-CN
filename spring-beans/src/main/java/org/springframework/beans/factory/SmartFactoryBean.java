// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 您不得使用此文件除非符合许可证。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”提供的，不提供任何形式的明示或暗示保证，
* 包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证，了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory;

/**
 *  扩展了 {@link FactoryBean} 接口。实现类可以表明它们是否总是返回独立的实例，对于它们在 {@link #isSingleton()} 的实现返回 {@code false} 时，并不能明确指示独立实例的情况。
 *
 * <p>简单的 {@link FactoryBean} 实现如果没有实现这个扩展接口，将简单地假定它们在返回 {@code false} 时总是返回独立的实例；暴露的对象仅在需要时才被访问。
 *
 * <p><b>注意：</b>此接口是一个特殊用途的接口，主要用于框架内部及其协作框架内部。通常，应用提供的 FactoryBeans 应该简单地实现普通的 {@link FactoryBean} 接口。即使在点版本发布中，也可能向此扩展接口添加新方法。
 *
 * @作者 Juergen Hoeller
 * @since 2.0.3
 * @param <T> 实例的bean类型
 * @see #isPrototype()
 * @see #isSingleton()
 */
public interface SmartFactoryBean<T> extends FactoryBean<T> {

    /**
     * 这个工厂管理的对象是否为原型？也就是说，`getObject()` 方法是否总是返回一个独立的实例？
     * <p>FactoryBean自身的原型状态通常由拥有它的`BeanFactory`提供；通常，在那里它必须被定义为单例。
     * <p>此方法应严格检查独立实例；它不应对范围对象或其他非单例、非独立对象返回`true`。因此，这不仅仅是`isSingleton()`方法取反的形式。
     * <p>默认实现返回`false`。
     * @return 返回暴露的对象是否为原型
     * @see #getObject()
     * @see #isSingleton()
     */
    default boolean isPrototype() {
        return false;
    }

    /**
     * 这个FactoryBean是否期望进行主动初始化，即，
     * 不仅主动初始化自身，还期望（如果有的话）其单例对象（如果有的话）也进行主动初始化？
     * <p>标准的FactoryBean不期望进行主动初始化：
     * 它的{@link #getObject()}方法仅在实际访问时才会被调用，即使在单例对象的情况下。从这个方法返回{@code true}表示应该主动调用{@link #getObject()}方法，
     * 同时也主动应用后处理器。在单例对象的情况下，这可能是合理的，尤其是如果后处理器期望在启动时应用。
     * <p>默认实现返回{@code false}。
     * @return 是否适用主动初始化
     * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
     */
    default boolean isEagerInit() {
        return false;
    }
}
