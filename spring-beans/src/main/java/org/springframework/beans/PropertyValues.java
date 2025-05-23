// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的质量保证或适用性保证；
* 请参阅许可证，了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.springframework.lang.Nullable;

/**
 * 包含一个或多个 {@link PropertyValue} 对象的持有者，
 * 通常包含一个针对特定目标对象的更新。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2001年5月13日
 * @see PropertyValue
 */
public interface PropertyValues extends Iterable<PropertyValue> {

    /**
     * 返回一个遍历属性值的 {@link Iterator}。
     * @since 5.1
     */
    @Override
    default Iterator<PropertyValue> iterator() {
        return Arrays.asList(getPropertyValues()).iterator();
    }

    /**
     * 返回一个对属性值的 {@link Spliterator}。
     * @since 5.1
     */
    @Override
    default Spliterator<PropertyValue> spliterator() {
        return Spliterators.spliterator(getPropertyValues(), 0);
    }

    /**
     * 返回一个包含属性值的顺序 {@link Stream}。
     * @since 5.1
     */
    default Stream<PropertyValue> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * 返回此对象中持有的 PropertyValue 对象数组。
     */
    PropertyValue[] getPropertyValues();

    /**
     * 返回具有给定名称的属性值，如果存在的话。
     * @param propertyName 要搜索的名称
     * @return 属性值，如果不存在则返回 {@code null}
     */
    @Nullable
    PropertyValue getPropertyValue(String propertyName);

    /**
     * 返回自上次 PropertyValues 以来发生的变化。
     * 子类还应该重写 {@code equals} 方法。
     * @param old 上次属性值
     * @return 更新后的或新的属性。
     * 如果没有变化，则返回空的 PropertyValues。
     * @see Object#equals
     */
    PropertyValues changesSince(PropertyValues old);

    /**
     * 是否存在该属性的属性值（或其他处理条目）？
     * @param propertyName 我们感兴趣的属性的名称
     * @return 是否存在该属性的属性值
     */
    boolean contains(String propertyName);

    /**
     * 这个持有者完全不包含任何 PropertyValue 对象吗？
     */
    boolean isEmpty();
}
