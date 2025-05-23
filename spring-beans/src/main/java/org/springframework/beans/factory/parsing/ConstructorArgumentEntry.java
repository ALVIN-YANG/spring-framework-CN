// 翻译完成 glm-4-flash
/** 版权所有 2002-2012，原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.parsing;

import org.springframework.util.Assert;

/**
 * 表示一个（可能已索引的）构造函数参数的 {@link ParseState} 条目。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ConstructorArgumentEntry implements ParseState.Entry {

    private final int index;

    /**
     * 创建一个表示具有（目前）未知索引的构造函数参数的新实例的{@link ConstructorArgumentEntry}类
     */
    public ConstructorArgumentEntry() {
        this.index = -1;
    }

    /**
     * 创建一个表示在提供的索引处的构造函数参数的新实例的 {@link ConstructorArgumentEntry} 类。
     * @param index 构造函数参数的索引
     * @throws IllegalArgumentException 如果提供的索引 {@code index} 小于零
     */
    public ConstructorArgumentEntry(int index) {
        Assert.isTrue(index >= 0, "Constructor argument index must be greater than or equal to zero");
        this.index = index;
    }

    @Override
    public String toString() {
        return "Constructor-arg" + (this.index >= 0 ? " #" + this.index : "");
    }
}
