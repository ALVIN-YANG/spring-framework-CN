// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache 许可证版本 2.0（"许可证"），许可使用此文件；除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下网址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或经书面同意，否则根据许可证分发的软件按"现状"提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的语言。*/
package org.springframework.beans;

import org.springframework.lang.Nullable;

/**
 * 表示一个对象，其值集可以与父对象的值集合并的接口。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see org.springframework.beans.factory.support.ManagedSet
 * @see org.springframework.beans.factory.support.ManagedList
 * @see org.springframework.beans.factory.support.ManagedMap
 * @see org.springframework.beans.factory.support.ManagedProperties
 */
public interface Mergeable {

    /**
     * 是否为这个特定实例启用了合并功能？
     */
    boolean isMergeEnabled();

    /**
     * 将当前值集与所提供对象中的值集合并。
     * <p>所提供的对象被视为父对象，调用者的值集中的值必须覆盖所提供对象中的值。
     * @param parent 要合并的对象
     * @return 合并操作的结果
     * @throws IllegalArgumentException 如果提供的父对象为 {@code null}
     * @throws IllegalStateException 如果此实例不支持合并（即，{@code mergeEnabled} 等于 {@code false}）。
     */
    Object merge(@Nullable Object parent);
}
