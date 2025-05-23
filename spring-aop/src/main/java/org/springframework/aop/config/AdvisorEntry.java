// 翻译完成 glm-4-flash
/*版权所有 2002-2020，原作者或作者。

根据Apache License，版本2.0（“许可证”），除非适用法律要求或经书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可协议下权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.config;

import org.springframework.beans.factory.parsing.ParseState;

/**
 * 代表顾问的 {@link ParseState} 条目。
 *
 * @author Mark Fisher
 * @since 2.0
 */
public class AdvisorEntry implements ParseState.Entry {

    private final String name;

    /**
     * 创建一个新的 {@code AdvisorEntry} 实例。
     * @param name 辅助者的 bean 名称
     */
    public AdvisorEntry(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Advisor '" + this.name + "'";
    }
}
