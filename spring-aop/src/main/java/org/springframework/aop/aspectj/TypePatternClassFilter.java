// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache许可证版本2.0（“许可证”），除非法律要求或书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下网址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的保证或条件，无论是明示的还是暗示的。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj;

import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.TypePatternMatcher;
import org.springframework.aop.ClassFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Spring AOP 使用 AspectJ 类型匹配的 {@link ClassFilter} 实现。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 */
public class TypePatternClassFilter implements ClassFilter {

    private String typePattern = "";

    @Nullable
    private TypePatternMatcher aspectJTypePatternMatcher;

    /**
     * 创建一个 {@link TypePatternClassFilter} 类的新实例。
     * <p>这是一个 JavaBean 构造函数；请务必设置 {@link #setTypePattern(String) typePattern} 属性，
     * 否则当首次调用 {@link #matches(Class)} 方法时，无疑会抛出一个致命的 {@link IllegalStateException} 异常。
     */
    public TypePatternClassFilter() {
    }

    /**
     * 使用给定的类型模式创建一个完全配置的 {@link TypePatternClassFilter}。
     * @param typePattern AspectJ 编译器应该解析的类型模式。
     */
    public TypePatternClassFilter(String typePattern) {
        setTypePattern(typePattern);
    }

    /**
     * 设置 AspectJ 类型模式以匹配。
     * <p>示例包括：
     * <code class="code">
     * org.springframework.beans.*
     * </code>
     * 这将匹配给定包中的任何类或接口。
     * <code class="code">
     * org.springframework.beans.ITestBean+
     * </code>
     * 这将匹配 {@code ITestBean} 接口及其任何实现类。
     * <p>这些约定由 AspectJ 建立，而非 Spring AOP。
     * @param typePattern AspectJ 编译器应解析的类型模式
     */
    public void setTypePattern(String typePattern) {
        Assert.notNull(typePattern, "Type pattern must not be null");
        this.typePattern = typePattern;
        this.aspectJTypePatternMatcher = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution().parseTypePattern(replaceBooleanOperators(typePattern));
    }

    /**
     * 返回匹配的 AspectJ 类型模式。
     */
    public String getTypePattern() {
        return this.typePattern;
    }

    /**
     * 该切入点是否应用于给定的接口或目标类？
     * @param clazz 候选目标类
     * @return 是否将通知应用于此候选目标类
     * @throws IllegalStateException 如果尚未设置 {@link #setTypePattern(String)}
     */
    @Override
    public boolean matches(Class<?> clazz) {
        Assert.state(this.aspectJTypePatternMatcher != null, "No type pattern has been set");
        return this.aspectJTypePatternMatcher.matches(clazz);
    }

    /**
     * 如果在XML中指定了类型模式，则用户不能将"and"写成"&&"（尽管"&&"将会生效）。
     * 我们还允许在两个子表达式之间使用"and"。
     * <p>此方法将转换回"&&"以供AspectJ切入点解析器使用。
     */
    private String replaceBooleanOperators(String pcExpr) {
        String result = StringUtils.replace(pcExpr, " and ", " && ");
        result = StringUtils.replace(result, " or ", " || ");
        return StringUtils.replace(result, " not ", " ! ");
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof TypePatternClassFilter that && ObjectUtils.nullSafeEquals(this.typePattern, that.typePattern)));
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.typePattern);
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + this.typePattern;
    }
}
