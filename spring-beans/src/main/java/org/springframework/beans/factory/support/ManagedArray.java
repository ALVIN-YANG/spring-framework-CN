// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于存储管理数组的标签集合类，可能包含运行时bean引用（将解析为bean对象）。
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
@SuppressWarnings("serial")
public class ManagedArray extends ManagedList<Object> {

    /**
     * 已解析目标数组运行时创建的元素类型。
     */
    @Nullable
    volatile Class<?> resolvedElementType;

    /**
     * 创建一个新的受管理数组占位符。
     * @param elementTypeName 目标元素类型作为类名
     * @param size 数组的大小
     */
    public ManagedArray(String elementTypeName, int size) {
        super(size);
        Assert.notNull(elementTypeName, "elementTypeName must not be null");
        setElementTypeName(elementTypeName);
    }
}
