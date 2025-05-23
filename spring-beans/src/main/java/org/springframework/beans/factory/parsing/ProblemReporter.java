// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不遵守许可证使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.parsing;

/**
 * SPI（服务提供接口）允许工具和其他外部进程处理在bean定义解析过程中报告的错误和警告。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see Problem
 */
public interface ProblemReporter {

    /**
     * 在解析过程中遇到致命错误时调用。
     * <p>实现类必须将给定的问题视为致命的，
     * 即它们最终必须抛出一个异常。
     * @param problem 错误的来源（从不为 {@code null}）
     */
    void fatal(Problem problem);

    /**
     * 在解析过程中遇到错误时被调用。
     * <p>实现者可以选择将错误视为致命的。
     * @param problem 错误的来源（永远不会为 {@code null}）
     */
    void error(Problem problem);

    /**
     * 在解析过程中触发警告时调用。
     * <p>警告<strong>永远</strong>不被视为致命。
     * @param problem 警告的来源（从不为{@code null}）
     */
    void warning(Problem problem);
}
