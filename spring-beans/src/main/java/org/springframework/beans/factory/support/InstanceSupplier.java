// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体规定许可权和限制。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.function.ThrowingBiFunction;
import org.springframework.util.function.ThrowingSupplier;

/**
 * 专用的 {@link Supplier}，可以被设置在
 * {@link AbstractBeanDefinition#setInstanceSupplier(Supplier) BeanDefinition}
 * 上，当需要提供关于 {@link RegisteredBean 注册的bean} 的详细信息以供应实例时使用。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @param <T> 由该供应商提供的实例的类型
 * @see RegisteredBean
 * @see org.springframework.beans.factory.aot.BeanInstanceSupplier
 */
@FunctionalInterface
public interface InstanceSupplier<T> extends ThrowingSupplier<T> {

    @Override
    default T getWithException() {
        throw new IllegalStateException("No RegisteredBean parameter provided");
    }

    /**
     * 获取提供的实例。
     * @param registeredBean 请求实例的已注册的bean
     * @return 返回提供的实例
     * @throws Exception 发生错误时抛出异常
     */
    T get(RegisteredBean registeredBean) throws Exception;

    /**
     * 返回此供应商使用的用于创建实例的工厂方法，或者如果未知或此供应商使用其他方式，则返回 {@code null}。
     * @return 用于创建实例的工厂方法，或者返回 {@code null}
     */
    @Nullable
    default Method getFactoryMethod() {
        return null;
    }

    /**
     * 返回一个组合实例供应商，它首先从这个供应商获取实例，然后应用 `after` 函数以获得结果。
     * @param <V> `after` 函数的输出类型，以及组合函数的输出类型
     * @param after 获取实例后要应用的函数
     * @return 一个组合实例供应商
     */
    default <V> InstanceSupplier<V> andThen(ThrowingBiFunction<RegisteredBean, ? super T, ? extends V> after) {
        Assert.notNull(after, "'after' function must not be null");
        return new InstanceSupplier<>() {

            @Override
            public V get(RegisteredBean registeredBean) throws Exception {
                return after.applyWithException(registeredBean, InstanceSupplier.this.get(registeredBean));
            }

            @Override
            public Method getFactoryMethod() {
                return InstanceSupplier.this.getFactoryMethod();
            }
        };
    }

    /**
     * 工厂方法，用于从 {@link ThrowingSupplier} 创建一个 {@link InstanceSupplier}。
     * @param <T> 由此提供者提供的实例的类型
     * @param supplier 源提供者
     * @return 一个新的 {@link InstanceSupplier}
     */
    static <T> InstanceSupplier<T> using(ThrowingSupplier<T> supplier) {
        Assert.notNull(supplier, "Supplier must not be null");
        if (supplier instanceof InstanceSupplier<T> instanceSupplier) {
            return instanceSupplier;
        }
        return registeredBean -> supplier.getWithException();
    }

    /**
     * 工厂方法，用于从 {@link ThrowingSupplier} 创建一个 {@link InstanceSupplier}。
     * @param <T> 由此供应商提供的实例的类型
     * @param factoryMethod 正在使用的工厂方法
     * @param supplier 源供应商
     * @return 一个新的 {@link InstanceSupplier}
     */
    static <T> InstanceSupplier<T> using(@Nullable Method factoryMethod, ThrowingSupplier<T> supplier) {
        Assert.notNull(supplier, "Supplier must not be null");
        if (supplier instanceof InstanceSupplier<T> instanceSupplier && instanceSupplier.getFactoryMethod() == factoryMethod) {
            return instanceSupplier;
        }
        return new InstanceSupplier<>() {

            @Override
            public T get(RegisteredBean registeredBean) throws Exception {
                return supplier.getWithException();
            }

            @Override
            public Method getFactoryMethod() {
                return factoryMethod;
            }
        };
    }

    /**
     * 适用于 Lambda 表达式的友好方法，可用于在单个调用中创建一个
     * {@link InstanceSupplier} 并添加后处理器。例如：`InstanceSupplier.of(registeredBean -> ...).andThen(...)`。
     * @param <T> 由此供应商提供的实例类型
     * @param instanceSupplier 实例供应商
     * @return 一个新的 {@link InstanceSupplier}
     */
    static <T> InstanceSupplier<T> of(InstanceSupplier<T> instanceSupplier) {
        Assert.notNull(instanceSupplier, "InstanceSupplier must not be null");
        return instanceSupplier;
    }
}
