// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”），您可以使用此文件，但必须遵守许可证。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.parsing;

import org.springframework.util.StringUtils;

/**
 * 代表 JavaBean 属性的 {@link ParseState} 条目。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class PropertyEntry implements ParseState.Entry {

    private final String name;

    /**
     * 创建一个新的 {@code PropertyEntry} 实例。
     * @param name 由此实例表示的 JavaBean 属性的名称
     */
    public PropertyEntry(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Invalid property name '" + name + "'");
        }
        this.name = name;
    }

    @Override
    public String toString() {
        return "Property '" + this.name + "'";
    }
}
