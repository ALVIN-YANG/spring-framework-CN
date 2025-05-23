// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.aot;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 解决了要自动装配的参数。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanInstanceSupplier
 * @see AutowiredMethodArgumentsResolver
 */
@FunctionalInterface
public interface AutowiredArguments {

    /**
     * 返回指定索引处的解析后的参数。
     * @param <T> 参数的类型
     * @param index 参数索引
     * @param requiredType 所需的参数类型
     * @return 参数
     */
    @SuppressWarnings("unchecked")
    @Nullable
    default <T> T get(int index, Class<T> requiredType) {
        Object value = getObject(index);
        if (!ClassUtils.isAssignableValue(requiredType, value)) {
            throw new IllegalArgumentException("Argument type mismatch: expected '" + ClassUtils.getQualifiedName(requiredType) + "' for value [" + value + "]");
        }
        return (T) value;
    }

    /**
     * 返回指定索引处的解析后的参数。
     * @param <T> 参数的类型
     * @param index 参数索引
     * @return 参数
     */
    @SuppressWarnings("unchecked")
    @Nullable
    default <T> T get(int index) {
        return (T) getObject(index);
    }

    /**
     * 返回指定索引处的已解析参数。
     * @param index 参数索引
     * @return 参数
     */
    @Nullable
    default Object getObject(int index) {
        return toArray()[index];
    }

    /**
     * 返回参数作为一个对象数组。
     * @return 返回参数作为一个对象数组
     */
    Object[] toArray();

    /**
     * 工厂方法，用于从给定的对象数组中创建一个新的 {@link AutowiredArguments} 实例。
     * @param arguments 参数数组
     * @return 一个新的 {@link AutowiredArguments} 实例
     */
    static AutowiredArguments of(Object[] arguments) {
        Assert.notNull(arguments, "'arguments' must not be null");
        return () -> arguments;
    }
}
