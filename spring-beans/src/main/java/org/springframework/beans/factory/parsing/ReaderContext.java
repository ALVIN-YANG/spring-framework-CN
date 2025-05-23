// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（"许可证"），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * 被传递给 Bean 定义读取过程的上下文，
 * 封装了所有相关的配置以及状态。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ReaderContext {

    private final Resource resource;

    private final ProblemReporter problemReporter;

    private final ReaderEventListener eventListener;

    private final SourceExtractor sourceExtractor;

    /**
     * 构建一个新的 {@code ReaderContext}。
     * @param resource XML bean 定义资源
     * @param problemReporter 正在使用的问题报告器
     * @param eventListener 正在使用的事件监听器
     * @param sourceExtractor 正在使用的源提取器
     */
    public ReaderContext(Resource resource, ProblemReporter problemReporter, ReaderEventListener eventListener, SourceExtractor sourceExtractor) {
        this.resource = resource;
        this.problemReporter = problemReporter;
        this.eventListener = eventListener;
        this.sourceExtractor = sourceExtractor;
    }

    public final Resource getResource() {
        return this.resource;
    }

    // 错误和警告
    /**
     * 抛出一个致命错误。
     */
    public void fatal(String message, @Nullable Object source) {
        fatal(message, source, null, null);
    }

    /**
     * 抛出致命错误。
     */
    public void fatal(String message, @Nullable Object source, @Nullable Throwable cause) {
        fatal(message, source, null, cause);
    }

    /**
     * 抛出一个致命错误。
     */
    public void fatal(String message, @Nullable Object source, @Nullable ParseState parseState) {
        fatal(message, source, parseState, null);
    }

    /**
     * 抛出致命错误。
     */
    public void fatal(String message, @Nullable Object source, @Nullable ParseState parseState, @Nullable Throwable cause) {
        Location location = new Location(getResource(), source);
        this.problemReporter.fatal(new Problem(message, location, parseState, cause));
    }

    /**
     * 抛出一个常规错误。
     */
    public void error(String message, @Nullable Object source) {
        error(message, source, null, null);
    }

    /**
     * 抛出一个常规错误。
     */
    public void error(String message, @Nullable Object source, @Nullable Throwable cause) {
        error(message, source, null, cause);
    }

    /**
     * 抛出一个常规错误。
     */
    public void error(String message, @Nullable Object source, @Nullable ParseState parseState) {
        error(message, source, parseState, null);
    }

    /**
     * 抛出一个常规错误。
     */
    public void error(String message, @Nullable Object source, @Nullable ParseState parseState, @Nullable Throwable cause) {
        Location location = new Location(getResource(), source);
        this.problemReporter.error(new Problem(message, location, parseState, cause));
    }

    /**
     * 抛出一个非关键警告。
     */
    public void warning(String message, @Nullable Object source) {
        warning(message, source, null, null);
    }

    /**
     * 抛出一个非关键警告。
     */
    public void warning(String message, @Nullable Object source, @Nullable Throwable cause) {
        warning(message, source, null, cause);
    }

    /**
     * 抛出一个非关键警告。
     */
    public void warning(String message, @Nullable Object source, @Nullable ParseState parseState) {
        warning(message, source, parseState, null);
    }

    /**
     * 抛出一个非关键警告。
     */
    public void warning(String message, @Nullable Object source, @Nullable ParseState parseState, @Nullable Throwable cause) {
        Location location = new Location(getResource(), source);
        this.problemReporter.warning(new Problem(message, location, parseState, cause));
    }

    // 显式解析事件
    /**
     * 触发一个已注册默认事件的操作。
     */
    public void fireDefaultsRegistered(DefaultsDefinition defaultsDefinition) {
        this.eventListener.defaultsRegistered(defaultsDefinition);
    }

    /**
     * 触发一个组件注册事件。
     */
    public void fireComponentRegistered(ComponentDefinition componentDefinition) {
        this.eventListener.componentRegistered(componentDefinition);
    }

    /**
     * 触发一个别名注册事件。
     */
    public void fireAliasRegistered(String beanName, String alias, @Nullable Object source) {
        this.eventListener.aliasRegistered(new AliasDefinition(beanName, alias, source));
    }

    /**
     * 触发一个导入处理事件。
     */
    public void fireImportProcessed(String importedResource, @Nullable Object source) {
        this.eventListener.importProcessed(new ImportDefinition(importedResource, source));
    }

    /**
     * 触发一个导入处理事件。
     */
    public void fireImportProcessed(String importedResource, Resource[] actualResources, @Nullable Object source) {
        this.eventListener.importProcessed(new ImportDefinition(importedResource, actualResources, source));
    }

    // 源提取
    /**
     * 返回正在使用的源提取器。
     */
    public SourceExtractor getSourceExtractor() {
        return this.sourceExtractor;
    }

    /**
     * 调用给定源对象的源提取器。
     * @param sourceCandidate 原始源对象
     * @return 要存储的源对象，或返回 {@code null} 表示没有。
     * @see #getSourceExtractor()
     * @see SourceExtractor#extractSource
     */
    @Nullable
    public Object extractSource(Object sourceCandidate) {
        return this.sourceExtractor.extractSource(sourceCandidate, this.resource);
    }
}
