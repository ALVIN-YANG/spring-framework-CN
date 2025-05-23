// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或原作者。

根据Apache License, Version 2.0（“许可证”），除非按照许可证要求或书面同意，否则不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 基于Java包`java.util.regex`的正则表达式切入点。
 * 支持以下JavaBean属性：
 * <ul>
 * <li>pattern：匹配完全限定方法名的正则表达式
 * <li>patterns：另一个属性，接受包含正则表达式的字符串数组。结果将是这些模式的总和。
 * </ul>
 *
 * <p>注意：正则表达式必须匹配。例如，`.*get.*`将匹配`com.mycom.Foo.getBar()`。
 * `get.*`将不会匹配。
 *
 * @author Dmitriy Kopylenko
 * @author Rob Harrop
 * @since 1.1
 */
@SuppressWarnings("serial")
public class JdkRegexpMethodPointcut extends AbstractRegexpMethodPointcut {

    /**
     * 模式的编译形式。
     */
    private Pattern[] compiledPatterns = new Pattern[0];

    /**
     * 排除模式的编译形式。
     */
    private Pattern[] compiledExclusionPatterns = new Pattern[0];

    /**
     * 从提供的字符串数组中初始化 {@link Pattern}。
     */
    @Override
    protected void initPatternRepresentation(String[] patterns) throws PatternSyntaxException {
        this.compiledPatterns = compilePatterns(patterns);
    }

    /**
     * 从提供的字符串数组中初始化排除的 {@link Pattern 模式}。
     */
    @Override
    protected void initExcludedPatternRepresentation(String[] excludedPatterns) throws PatternSyntaxException {
        this.compiledExclusionPatterns = compilePatterns(excludedPatterns);
    }

    /**
     * 返回 {@code true} 如果索引为 {@code patternIndex} 的 {@link Pattern} 与提供的候选字符串 {@code String} 匹配。
     */
    @Override
    protected boolean matches(String pattern, int patternIndex) {
        Matcher matcher = this.compiledPatterns[patternIndex].matcher(pattern);
        return matcher.matches();
    }

    /**
     * 如果索引为 {@code patternIndex} 的排除模式 {@link Pattern} 匹配给定的候选字符串 {@code String}，则返回 {@code true}。
     */
    @Override
    protected boolean matchesExclusion(String candidate, int patternIndex) {
        Matcher matcher = this.compiledExclusionPatterns[patternIndex].matcher(candidate);
        return matcher.matches();
    }

    /**
     * 将提供的 {@code String[]} 编译成一组 {@link Pattern} 对象，并返回该数组。
     */
    private Pattern[] compilePatterns(String[] source) throws PatternSyntaxException {
        Pattern[] destination = new Pattern[source.length];
        for (int i = 0; i < source.length; i++) {
            destination[i] = Pattern.compile(source[i]);
        }
        return destination;
    }
}
