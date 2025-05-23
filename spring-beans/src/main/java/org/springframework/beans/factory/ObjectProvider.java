// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的质量保证或条件，
* 明示或暗示的。请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * 这是一个专门为注入点设计的 {@link ObjectFactory} 变体，
 * 允许程序性可选性和宽松的非唯一处理。
 *
 * <p>从 5.1 版本开始，此接口扩展了 {@link Iterable} 并提供了对 {@link Stream} 的支持。
 * 因此，它可以用于循环，提供对 #forEach 的迭代，并允许以集合样式访问 #stream。
 *
 * @author Juergen Hoeller
 * @since 4.3
 * @param <T> 对象类型
 * @see BeanFactory#getBeanProvider
 * @see org.springframework.beans.factory.annotation.Autowired
 */
public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T> {

    /**
     * 返回由该工厂管理的对象的一个实例（可能是共享的或独立的）。
     * 允许指定显式的构造参数，类似于 {@link BeanFactory#getBean(String, Object...)}。
     * @param args 创建相应实例时使用的参数
     * @return 该Bean的一个实例
     * @throws BeansException 在创建过程中发生错误时抛出
     * @see #getObject()
     */
    T getObject(Object... args) throws BeansException;

    /**
     * 返回由该工厂管理的对象的一个实例（可能是共享的或独立的）。
     * @return 返回一个bean的实例，或者如果不可用则返回{@code null}
     * @throws BeansException 如果在创建过程中出现错误
     * @see #getObject()
     */
    @Nullable
    T getIfAvailable() throws BeansException;

    /**
     * 返回由该工厂管理的对象的一个实例（可能是共享的或独立的）。
     * @param defaultSupplier 一个回调，用于在工厂中没有对象时提供默认对象
     * @return 一个bean实例，或者如果没有可用的bean，则返回提供的默认对象
     * @throws BeansException 在创建错误的情况下抛出
     * @since 5.0
     * @see #getIfAvailable()
     */
    default T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
        T dependency = getIfAvailable();
        return (dependency != null ? dependency : defaultSupplier.get());
    }

    /**
     * 消费由该工厂管理的对象的一个实例（可能是共享的或独立的），如果可用的话。
     * @param dependencyConsumer 一个回调，用于处理可用的目标对象（如果不可用则不会调用）
     * @throws BeansException 如果创建过程中发生错误
     * @since 5.0
     * @see #getIfAvailable()
     */
    default void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
        T dependency = getIfAvailable();
        if (dependency != null) {
            dependencyConsumer.accept(dependency);
        }
    }

    /**
     * 返回由该工厂管理的对象的一个实例（可能是共享的或独立的）。
     * @return 返回bean的一个实例，或者在不可用或非唯一（即找到多个候选者，但没有一个被标记为主要）的情况下返回{@code null}
     * @throws BeansException 如果创建过程中出现错误
     * @see #getObject()
     */
    @Nullable
    T getIfUnique() throws BeansException;

    /**
     * 返回由该工厂管理的对象的一个实例（可能是共享的或独立的）。
     * @param defaultSupplier 一个回调，用于提供默认对象，如果工厂中没有找到唯一的候选对象
     * @return 一个bean的实例，或者如果不可用或不是唯一的（即找到多个候选对象，但没有标记为主候选）时，则提供所提供的默认对象
     * @throws BeansException 在创建错误的情况下抛出
     * @since 5.0
     * @see #getIfUnique()
     */
    default T getIfUnique(Supplier<T> defaultSupplier) throws BeansException {
        T dependency = getIfUnique();
        return (dependency != null ? dependency : defaultSupplier.get());
    }

    /**
     * 消费由该工厂管理的对象实例（可能是共享的或独立的），如果该实例是唯一的。
     * @param dependencyConsumer 用于处理目标对象（如果唯一）的回调，否则不调用
     * @throws BeansException 如果创建过程中发生错误
     * @since 5.0
     * @see #getIfAvailable()
     */
    default void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
        T dependency = getIfUnique();
        if (dependency != null) {
            dependencyConsumer.accept(dependency);
        }
    }

    /**
     * 返回一个遍历所有匹配对象实例的 {@link Iterator}，
     * 不保证特定的排序（但通常是注册顺序）。
     * @since 5.1
     * @see #stream()
     */
    @Override
    default Iterator<T> iterator() {
        return stream().iterator();
    }

    /**
     * 返回所有匹配对象实例的顺序 {@link Stream}，
     * 不保证特定排序（但通常按注册顺序）。
     * @since 5.1
     * @see #iterator()
     * @see #orderedStream()
     */
    default Stream<T> stream() {
        throw new UnsupportedOperationException("Multi element access not supported");
    }

    /**
     * 返回一个按工厂的公共顺序比较器预排序的所有匹配对象实例的顺序流。
     * <p>在标准的Spring应用程序上下文中，这将根据{@link org.springframework.core.Ordered}约定进行排序，
     * 并且在基于注解的配置情况下，还会考虑{@link org.springframework.core.annotation.Order}注解，
     * 类似于列表/数组类型的多元素注入点。
     * @since 5.1
     * @see #stream()
     * @see org.springframework.core.OrderComparator
     */
    default Stream<T> orderedStream() {
        throw new UnsupportedOperationException("Ordered element access not supported");
    }
}
