// 翻译完成 glm-4-flash
/*版权所有 2002-2020 原作者或作者。
 
根据Apache License 2.0（“许可证”）授权；除非符合许可证，否则不得使用此文件。
您可以在以下网址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发给他人，
不提供任何形式的保证或条件，无论是明示的还是暗示的。有关权限和限制的具体语言，
请参阅许可证。*/
package org.springframework.aop.config;

import org.springframework.beans.factory.parsing.ParseState;

/**
 * 代表切点的 {@link ParseState} 入口。
 *
 * @author Mark Fisher
 * @since 2.0
 */
public class PointcutEntry implements ParseState.Entry {

    private final String name;

    /**
     * 创建一个新的 {@code PointcutEntry} 实例。
     * @param name 点切的 bean 名称
     */
    public PointcutEntry(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Pointcut '" + this.name + "'";
    }
}
