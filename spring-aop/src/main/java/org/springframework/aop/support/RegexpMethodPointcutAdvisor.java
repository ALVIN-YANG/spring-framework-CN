// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）许可；
除非符合许可证规定，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的的适用性。
有关许可的具体语言管理权限和限制，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * 用于持有 Advice 的正则表达式方法切入点便捷类，使其成为 {@link org.springframework.aop.Advisor}。
 *
 * <p>使用 "pattern" 和 "patterns" 透传属性配置此类。这些属性与 {@link AbstractRegexpMethodPointcut} 的 pattern 和 patterns 属性类似。
 *
 * <p>可以委派给任何 {@link AbstractRegexpMethodPointcut} 子类。默认情况下，将使用 {@link JdkRegexpMethodPointcut}。要选择特定的实现，覆盖 {@link #createPointcut} 方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setPattern
 * @see #setPatterns
 * @see JdkRegexpMethodPointcut
 */
@SuppressWarnings("serial")
public class RegexpMethodPointcutAdvisor extends AbstractGenericPointcutAdvisor {

    @Nullable
    private String[] patterns;

    @Nullable
    private AbstractRegexpMethodPointcut pointcut;

    private final Object pointcutMonitor = new SerializableMonitor();

    /**
     * 创建一个空的RegexpMethodPointcutAdvisor。
     * @see #setPattern
     * @see #setPatterns
     * @see #setAdvice
     */
    public RegexpMethodPointcutAdvisor() {
    }

    /**
     * 为给定的建议创建一个RegexpMethodPointcutAdvisor。
     * 仍然需要在之后指定模式。
     * @param advice 要使用的建议
     * @see #setPattern
     * @see #setPatterns
     */
    public RegexpMethodPointcutAdvisor(Advice advice) {
        setAdvice(advice);
    }

    /**
     * 为给定的建议创建一个RegexpMethodPointcutAdvisor。
     * @param pattern 要使用的模式
     * @param advice 要使用的建议
     */
    public RegexpMethodPointcutAdvisor(String pattern, Advice advice) {
        setPattern(pattern);
        setAdvice(advice);
    }

    /**
     * 为给定的建议创建一个RegexpMethodPointcutAdvisor。
     * @param patterns 要使用的模式
     * @param advice 要使用的建议
     */
    public RegexpMethodPointcutAdvisor(String[] patterns, Advice advice) {
        setPatterns(patterns);
        setAdvice(advice);
    }

    /**
     * 设置定义匹配方法的正则表达式。
     * <p>使用此方法或 {@link #setPatterns} 中的一个，不要同时使用两个。
     * @see #setPatterns
     */
    public void setPattern(String pattern) {
        setPatterns(pattern);
    }

    /**
     * 设置定义方法匹配的正则表达式。
     * 将传递给切点实现。
     * <p>匹配将是所有这些的正则表达式的并集；如果任何模式匹配，切点就匹配。
     * @see AbstractRegexpMethodPointcut#setPatterns
     */
    public void setPatterns(String... patterns) {
        this.patterns = patterns;
    }

    /**
     * 初始化此Advisor中持有的单例Pointcut。
     */
    @Override
    public Pointcut getPointcut() {
        synchronized (this.pointcutMonitor) {
            if (this.pointcut == null) {
                this.pointcut = createPointcut();
                if (this.patterns != null) {
                    this.pointcut.setPatterns(this.patterns);
                }
            }
            return this.pointcut;
        }
    }

    /**
     * 创建实际切入点：默认情况下，将使用 {@link JdkRegexpMethodPointcut}。
     * @return 返回 Pointcut 实例（绝不会为 {@code null}）
     */
    protected AbstractRegexpMethodPointcut createPointcut() {
        return new JdkRegexpMethodPointcut();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": advice [" + getAdvice() + "], pointcut patterns " + ObjectUtils.nullSafeToString(this.patterns);
    }

    /**
     * 空类，用于序列化的监视器对象。
     */
    private static class SerializableMonitor implements Serializable {
    }
}
