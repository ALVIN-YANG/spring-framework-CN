// 翻译完成 glm-4-flash
/*版权所有 2002-2014 原作者或作者。
 
根据Apache许可证版本2.0（以下简称“许可证”）许可；除非符合许可证规定，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“现状”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;

/**
 * 用于名称匹配方法切入点并持有Advice的便捷类，使其成为Advisor。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see NameMatchMethodPointcut
 */
@SuppressWarnings("serial")
public class NameMatchMethodPointcutAdvisor extends AbstractGenericPointcutAdvisor {

    private final NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();

    public NameMatchMethodPointcutAdvisor() {
    }

    public NameMatchMethodPointcutAdvisor(Advice advice) {
        setAdvice(advice);
    }

    /**
     * 设置用于此切入点（pointcut）的 {@link ClassFilter}。
     * 默认为 {@link ClassFilter#TRUE}。
     * @see NameMatchMethodPointcut#setClassFilter
     */
    public void setClassFilter(ClassFilter classFilter) {
        this.pointcut.setClassFilter(classFilter);
    }

    /**
     * 便利方法，当我们只需要匹配一个方法名称时使用。
     * 请使用此方法或 {@code setMappedNames} 中的一个，不要同时使用两个。
     * @see #setMappedNames
     * @see NameMatchMethodPointcut#setMappedName
     */
    public void setMappedName(String mappedName) {
        this.pointcut.setMappedName(mappedName);
    }

    /**
     * 设置定义方法的名称，以便匹配。
     * 匹配将是所有这些名称的并集；如果有任何匹配，
     * 则切入点匹配。
     * @see NameMatchMethodPointcut#setMappedNames
     */
    public void setMappedNames(String... mappedNames) {
        this.pointcut.setMappedNames(mappedNames);
    }

    /**
     * 添加另一个符合条件的方法名称，除了已命名的那些。
     * 类似于设置方法，此方法用于配置代理时使用，
     * 在使用代理之前。
     * @param name 要添加的额外方法的名称，该名称将匹配
     * @return 此切入点对象，以便在一行中添加多个方法
     * @see NameMatchMethodPointcut#addMethodName
     */
    public NameMatchMethodPointcut addMethodName(String name) {
        return this.pointcut.addMethodName(name);
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }
}
