// 翻译完成 glm-4-flash
/*版权所有 2002-2020 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律规定或书面同意，否则在许可证下分发的软件
按“原样”提供，不提供任何明示或暗示的保证或条件。
有关许可的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.aop.config;

import org.springframework.beans.factory.parsing.ParseState;

/**
 * 代表建议元素的 {@link ParseState} 条目。
 *
 * @author Mark Fisher
 * @since 2.0
 */
public class AdviceEntry implements ParseState.Entry {

    private final String kind;

    /**
     * 创建一个新的 {@code AdviceEntry} 实例。
     * @param kind 由此条目表示的建议类型（before, after, around）
     */
    public AdviceEntry(String kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "Advice (" + this.kind + ")";
    }
}
