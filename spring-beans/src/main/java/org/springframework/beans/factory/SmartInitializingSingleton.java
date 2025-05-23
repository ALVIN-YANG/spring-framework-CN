// 翻译完成 glm-4-flash
/** 版权所有 2002-2014 原作者或作者。
*
* 根据 Apache License 2.0 版本（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.factory;

/**
 * 被触发于单例预实例化阶段末尾的回调接口，在 {@link BeanFactory} 启动过程中。此接口可以被单例 beans 实现，以便在常规单例实例化算法之后执行一些初始化操作，避免因意外早期初始化（例如来自 {@link ListableBeanFactory#getBeansOfType} 调用）而产生的副作用。从这一点来看，它是对 {@link InitializingBean} 的替代，后者在 bean 的本地构建阶段结束时触发。
 *
 * <p>这种回调变体在某种程度上类似于 {@link org.springframework.context.event.ContextRefreshedEvent}，但不需要实现 {@link org.springframework.context.ApplicationListener}，无需在上下文层次结构中过滤上下文引用等。它还意味着对仅涉及 {@code beans} 包的更最小化依赖，并由独立实现的 {@link ListableBeanFactory} 所尊重，而不仅限于在 {@link org.springframework.context.ApplicationContext} 环境中。
 *
 * <p><b>注意：</b>如果您打算启动/管理异步任务，最好实现 {@link org.springframework.context.Lifecycle}，它提供了更丰富的运行时管理模型，并允许分阶段启动/关闭。
 *
 * @author Juergen Hoeller
 * @since 4.1
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
 */
public interface SmartInitializingSingleton {

    /**
     * 在单例预实例化阶段的最后调用，
     * 确保所有常规单例bean已经创建完成。
     * 在此方法内部调用 {@link ListableBeanFactory#getBeansOfType} 不会在启动过程中引发意外的副作用。
     * <p><b>注意：</b>此回调不会在启动后按需懒加载的单例bean上触发，
     * 也不会触发其他任何bean作用域。请仔细使用，仅用于具有预期启动语义的bean。
     */
    void afterSingletonsInstantiated();
}
