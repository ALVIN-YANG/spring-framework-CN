// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.parsing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;

/**
 * 简单的 {@link ProblemReporter} 实现，当遇到错误时表现出快速失败行为。
 *
 * <p>遇到的第一个错误会导致抛出 {@link BeanDefinitionParsingException}。
 *
 * <p>警告会被写入到
 * {@link #setLogger(org.apache.commons.logging.Log) 本类的日志} 中。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 */
public class FailFastProblemReporter implements ProblemReporter {

    private Log logger = LogFactory.getLog(getClass());

    /**
     * 设置用于报告警告的 {@link Log 日志记录器}。
     * <p>如果设置为 {@code null}，则将使用默认的、名称设置为实例类名称的 {@link Log 日志记录器}。
     * @param logger 用于报告警告的 {@link Log 日志记录器}
     */
    public void setLogger(@Nullable Log logger) {
        this.logger = (logger != null ? logger : LogFactory.getLog(getClass()));
    }

    /**
     * 抛出包含已发生错误的详细信息的 {@link BeanDefinitionParsingException}。
     * @param problem 错误的来源
     */
    @Override
    public void fatal(Problem problem) {
        throw new BeanDefinitionParsingException(problem);
    }

    /**
     * 抛出一个详细说明发生错误的 {@link BeanDefinitionParsingException}。
     * @param problem 错误的来源
     */
    @Override
    public void error(Problem problem) {
        throw new BeanDefinitionParsingException(problem);
    }

    /**
     * 将提供的 {@link Problem} 写入到 {@link Log} 中，日志级别为 {@code WARN}。
     * @param problem 警告的来源
     */
    @Override
    public void warning(Problem problem) {
        logger.warn(problem, problem.getRootCause());
    }
}
