// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、特定用途的适用性或不侵犯第三方权利。
* 请参阅许可证了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory.parsing;

import java.util.ArrayDeque;
import org.springframework.lang.Nullable;

/**
 * 基于简单 {@link ArrayDeque} 的结构，用于在解析过程中跟踪逻辑位置。在解析阶段的每个点上，以特定于读取器的方式将 {@link Entry entries} 添加到 ArrayDeque 中。
 *
 * <p>调用 {@link #toString()} 将渲染解析阶段当前逻辑位置的树形视图。这种表示方式旨在用于错误消息。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public final class ParseState {

    /**
     * 内部存储，使用 {@link ArrayDeque}。
     */
    private final ArrayDeque<Entry> state;

    /**
     * 创建一个新的 {@code ParseState}，并使用一个空的 {@link ArrayDeque}。
     */
    public ParseState() {
        this.state = new ArrayDeque<>();
    }

    /**
     * 创建一个新的 {@code ParseState}，其内部的 {@link ArrayDeque} 是传入的 {@code ParseState} 中状态的克隆
     */
    private ParseState(ParseState other) {
        this.state = other.state.clone();
    }

    /**
     * 向 {@link ArrayDeque} 中添加一个新的 {@link Entry}。
     */
    public void push(Entry entry) {
        this.state.push(entry);
    }

    /**
     * 从 {@link ArrayDeque} 中移除一个 {@link Entry}。
     */
    public void pop() {
        this.state.pop();
    }

    /**
     * 返回当前位于 {@link ArrayDeque} 顶部的 {@link Entry}，或者如果 {@link ArrayDeque} 为空，则返回 {@code null}。
     */
    @Nullable
    public Entry peek() {
        return this.state.peek();
    }

    /**
     * 创建一个新实例的 {@link ParseState}，它是此实例的一个独立快照。
     */
    public ParseState snapshot() {
        return new ParseState(this);
    }

    /**
     * 返回当前 {@code ParseState} 的树形表示。
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        int i = 0;
        for (ParseState.Entry entry : this.state) {
            if (i > 0) {
                sb.append('\n');
                for (int j = 0; j < i; j++) {
                    sb.append('\t');
                }
                sb.append("-> ");
            }
            sb.append(entry);
            i++;
        }
        return sb.toString();
    }

    /**
     * 用于进入 {@link ParseState} 的标记接口。
     */
    public interface Entry {
    }
}
