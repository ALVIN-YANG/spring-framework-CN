// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache许可证版本2.0（“许可证”）授权；
除非符合许可证规定，否则您不得使用此文件。
您可以在以下链接处获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的的保证。
有关许可证对权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.util.PatternMatchUtils;

/**
 * 简单方法名匹配的切入点，作为正则表达式模式的替代方案。
 *
 * <p>不处理重载方法：具有给定名称的所有方法都将有资格。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 11.02.2004
 * @see #isMatch
 */
@SuppressWarnings("serial")
public class NameMatchMethodPointcut extends StaticMethodMatcherPointcut implements Serializable {

    private List<String> mappedNames = new ArrayList<>();

    /**
     * 当我们只有一个方法名需要匹配时，这是一个便利的方法。
     * 使用此方法或 {@code setMappedNames} 中的一个，不要同时使用两个。
     * @see #setMappedNames
     */
    public void setMappedName(String mappedName) {
        setMappedNames(mappedName);
    }

    /**
     * 设置定义匹配方法的名称。
     * 匹配将是所有这些名称的并集；如果其中任何名称匹配，
     * 则切入点匹配。
     */
    public void setMappedNames(String... mappedNames) {
        this.mappedNames = new ArrayList<>(Arrays.asList(mappedNames));
    }

    /**
     * 添加另一个符合条件的方法名称，除了已经命名的那些。
     * 与设置方法类似，此方法用于在配置代理时使用，
     * 在使用代理之前。
     * <p><b>注意：</b>此方法在代理开始使用后无效，
     * 因为建议链会被缓存。
     * @param name 将匹配此点切的额外方法的名称
     * @return 此切点以允许在一行中添加多个额外方法
     */
    public NameMatchMethodPointcut addMethodName(String name) {
        this.mappedNames.add(name);
        return this;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        for (String mappedName : this.mappedNames) {
            if (mappedName.equals(method.getName()) || isMatch(method.getName(), mappedName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回如果给定的方法名称与映射名称匹配。
     * <p>默认实现检查"xxx*"、"*xxx"和"*xxx*"的匹配，以及直接相等性。可在子类中重写。
     * @param methodName 类的方法名称
     * @param mappedName 描述符中的名称
     * @return 如果名称匹配
     * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)
     */
    protected boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof NameMatchMethodPointcut that && this.mappedNames.equals(that.mappedNames)));
    }

    @Override
    public int hashCode() {
        return this.mappedNames.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + this.mappedNames;
    }
}
