// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可的具体语言、权限和限制，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 抽象基类正则表达式切入点Bean。JavaBean属性包括：
 * <ul>
 * <li>pattern：匹配完全限定方法名的正则表达式。确切的正则表达式语法将取决于子类（例如Perl5正则表达式）
 * <li>patterns：可选属性，接受一个包含正则表达式的字符串数组。结果将是这些表达式的并集。
 * </ul>
 *
 * <p>注意：正则表达式必须匹配。例如，
 * {@code .*get.*} 将匹配 com.mycom.Foo.getBar()。
 * {@code get.*} 不会匹配。
 *
 * <p>此基类是可序列化的。子类应声明所有字段为transient；反序列化时将再次调用 {@link #initPatternRepresentation} 方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1
 * @see JdkRegexpMethodPointcut
 */
@SuppressWarnings("serial")
public abstract class AbstractRegexpMethodPointcut extends StaticMethodMatcherPointcut implements Serializable {

    /**
     * 匹配的正则表达式。
     */
    private String[] patterns = new String[0];

    /**
     * 不匹配的正则表达式 <strong>不</strong>要匹配。
     */
    private String[] excludedPatterns = new String[0];

    /**
     * 便捷方法，当我们只有一个模式时使用。
     * 使用此方法或 {@link #setPatterns} 中的一个，不要同时使用两个。
     * @see #setPatterns
     */
    public void setPattern(String pattern) {
        setPatterns(pattern);
    }

    /**
     * 设置定义方法的正则表达式。
     * 匹配将是所有这些表达式的并集；如果其中任何表达式匹配，切点就匹配。
     * @see #setPattern
     */
    public void setPatterns(String... patterns) {
        Assert.notEmpty(patterns, "'patterns' must not be empty");
        this.patterns = new String[patterns.length];
        for (int i = 0; i < patterns.length; i++) {
            this.patterns[i] = patterns[i].strip();
        }
        initPatternRepresentation(this.patterns);
    }

    /**
     * 返回用于方法匹配的正则表达式。
     */
    public String[] getPatterns() {
        return this.patterns;
    }

    /**
     * 当我们只有一个排除模式时使用的便捷方法。
     * 使用此方法或 {@link #setExcludedPatterns} 中的一个，不要同时使用两个。
     * @see #setExcludedPatterns
     */
    public void setExcludedPattern(String excludedPattern) {
        setExcludedPatterns(excludedPattern);
    }

    /**
     * 设置定义方法匹配排除的正则表达式。
     * 匹配将是所有这些匹配的并集；如果其中任何一项匹配，则切入点匹配。
     * @see #setExcludedPattern
     */
    public void setExcludedPatterns(String... excludedPatterns) {
        Assert.notEmpty(excludedPatterns, "'excludedPatterns' must not be empty");
        this.excludedPatterns = new String[excludedPatterns.length];
        for (int i = 0; i < excludedPatterns.length; i++) {
            this.excludedPatterns[i] = excludedPatterns[i].strip();
        }
        initExcludedPatternRepresentation(this.excludedPatterns);
    }

    /**
     * 返回用于排除匹配的正则表达式。
     */
    public String[] getExcludedPatterns() {
        return this.excludedPatterns;
    }

    /**
     * 尝试将正则表达式与目标类的完全限定名称以及方法的声明类以及方法名称进行匹配。
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return (matchesPattern(ClassUtils.getQualifiedMethodName(method, targetClass)) || (targetClass != method.getDeclaringClass() && matchesPattern(ClassUtils.getQualifiedMethodName(method, method.getDeclaringClass()))));
    }

    /**
     * 将指定的候选者与配置的模式进行匹配。
     * @param signatureString 采用 "java.lang.Object.hashCode" 风格的签名
     * @return 候选者是否至少匹配指定的模式之一
     */
    protected boolean matchesPattern(String signatureString) {
        for (int i = 0; i < this.patterns.length; i++) {
            boolean matched = matches(signatureString, i);
            if (matched) {
                for (int j = 0; j < this.excludedPatterns.length; j++) {
                    boolean excluded = matchesExclusion(signatureString, j);
                    if (excluded) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 子类必须实现此方法以初始化正则表达式切入点。
     * 可以被多次调用。
     * <p>此方法将在 {@link #setPatterns} 方法中调用，并在反序列化时调用。
     * @param patterns 要初始化的模式
     * @throws IllegalArgumentException 如果模式无效时抛出异常
     */
    protected abstract void initPatternRepresentation(String[] patterns) throws IllegalArgumentException;

    /**
     * 子类必须实现此方法以初始化正则表达式点断。
     * 可被多次调用。
     * <p>此方法将由 {@link #setExcludedPatterns} 方法调用，并且在反序列化时也会被调用。
     * @param patterns 要初始化的模式
     * @throws IllegalArgumentException 如果模式无效，则抛出此异常
     */
    protected abstract void initExcludedPatternRepresentation(String[] patterns) throws IllegalArgumentException;

    /**
     * 给定索引的模式是否与给定的字符串匹配？
     * @param pattern 要匹配的字符串模式
     * @param patternIndex 模式的索引（从0开始）
     * @return 如果有匹配，则返回 {@code true}，否则返回 {@code false}
     */
    protected abstract boolean matches(String pattern, int patternIndex);

    /**
     * 给定的索引处的排除模式是否与给定的字符串匹配？
     * @param pattern 要匹配的字符串模式
     * @param patternIndex 模式的索引（从0开始）
     * @return 如果有匹配，则返回 {@code true}，否则返回 {@code false}
     */
    protected abstract boolean matchesExclusion(String pattern, int patternIndex);

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof AbstractRegexpMethodPointcut otherPointcut && Arrays.equals(this.patterns, otherPointcut.patterns) && Arrays.equals(this.excludedPatterns, otherPointcut.excludedPatterns)));
    }

    @Override
    public int hashCode() {
        int result = 27;
        for (String pattern : this.patterns) {
            result = 13 * result + pattern.hashCode();
        }
        for (String excludedPattern : this.excludedPatterns) {
            result = 13 * result + excludedPattern.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": patterns " + ObjectUtils.nullSafeToString(this.patterns) + ", excluded patterns " + ObjectUtils.nullSafeToString(this.excludedPatterns);
    }
}
