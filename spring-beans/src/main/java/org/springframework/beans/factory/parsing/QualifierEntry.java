// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可；
* 除非遵守许可协议，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.parsing;

import org.springframework.util.StringUtils;

/**
 * 表示自动装配候选者限定符的 {@link ParseState} 入口。
 *
 * @author Mark Fisher
 * @since 2.5
 */
public class QualifierEntry implements ParseState.Entry {

    private final String typeName;

    /**
     * 创建一个新的 {@code QualifierEntry} 实例。
     * @param typeName 规范化类型的名称
     */
    public QualifierEntry(String typeName) {
        if (!StringUtils.hasText(typeName)) {
            throw new IllegalArgumentException("Invalid qualifier type '" + typeName + "'");
        }
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "Qualifier '" + this.typeName + "'";
    }
}
