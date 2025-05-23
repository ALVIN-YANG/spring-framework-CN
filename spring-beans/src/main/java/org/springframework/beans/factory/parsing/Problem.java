// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 包括但不限于对适销性、适用性和非侵权性的保证。
* 请参阅许可证以了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory.parsing;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 表示一个与Bean定义配置相关的问题。
 * 主要作为传递给 {@link ProblemReporter} 的通用参数。
 *
 * <p>可能表示一个潜在致命的问题（错误）或只是一个警告。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see ProblemReporter
 */
public class Problem {

    private final String message;

    private final Location location;

    @Nullable
    private final ParseState parseState;

    @Nullable
    private final Throwable rootCause;

    /**
     * 创建一个新的 {@link Problem} 类实例。
     * @param message 包含问题的详细信息的消息
     * @param location 触发错误的 bean 配置源中的位置
     */
    public Problem(String message, Location location) {
        this(message, location, null, null);
    }

    /**
     * 创建一个新的 {@link Problem} 类实例。
     * @param message 包含问题的详细信息的消息
     * @param parseState 发生错误时的 {@link ParseState}
     * @param location 触发错误的配置源中 bean 的位置
     */
    public Problem(String message, Location location, ParseState parseState) {
        this(message, location, parseState, null);
    }

    /**
     * 创建一个新的 {@link Problem} 类实例。
     * @param message 包含问题的详细信息的消息
     * @param rootCause 导致错误的底层异常（可能为 {@code null}）
     * @param parseState 错误发生时的 {@link ParseState}
     * @param location 触发错误的 bean 配置源中的位置
     */
    public Problem(String message, Location location, @Nullable ParseState parseState, @Nullable Throwable rootCause) {
        Assert.notNull(message, "Message must not be null");
        Assert.notNull(location, "Location must not be null");
        this.message = message;
        this.location = location;
        this.parseState = parseState;
        this.rootCause = rootCause;
    }

    /**
     * 获取详细问题的消息。
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * 获取触发错误的bean配置源中的位置。
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * 获取触发错误的bean配置源描述，该描述包含在当前Problem的Location对象中。
     * @see #getLocation()
     */
    public String getResourceDescription() {
        return getLocation().getResource().getDescription();
    }

    /**
     * 获取错误发生时的 {@link ParseState}（可能为 {@code null}）。
     */
    @Nullable
    public ParseState getParseState() {
        return this.parseState;
    }

    /**
     * 获取导致错误的底层异常（可能为 {@code null}）。
     */
    @Nullable
    public Throwable getRootCause() {
        return this.rootCause;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration problem: ");
        sb.append(getMessage());
        sb.append("\nOffending resource: ").append(getResourceDescription());
        if (getParseState() != null) {
            sb.append('\n').append(getParseState());
        }
        return sb.toString();
    }
}
