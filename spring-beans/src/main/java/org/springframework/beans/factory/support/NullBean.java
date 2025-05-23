// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 包括但不限于对适销性、适用性和非侵权性的保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;

/**
 * 空Bean实例的内部表示，例如从 {@link FactoryBean#getObject()} 或工厂方法返回的 {@code null} 值。
 *
 * <p>每个这样的空Bean都由一个专门的 {@code NullBean} 实例表示，这些实例彼此不相等，独特地区分了从所有变种的 {@link org.springframework.beans.factory.BeanFactory#getBean} 返回的每个Bean。然而，每个这样的实例对 {@code #equals(null)} 都将返回 {@code true}，并且从 {@code #toString()} 返回 "null"，这就是它们如何在外部进行测试的方式（因为此类本身不是公开的）。
 *
 * @author Juergen Hoeller
 * @since 5.0
 */
final class NullBean {

    NullBean() {
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || other == null);
    }

    @Override
    public int hashCode() {
        return NullBean.class.hashCode();
    }

    @Override
    public String toString() {
        return "null";
    }
}
