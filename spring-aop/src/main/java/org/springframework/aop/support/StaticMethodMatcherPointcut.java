// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache 许可证版本 2.0 ("许可证") 许可使用，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.aop.support;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

/**
 * 在我们希望强制子类实现 {@link MethodMatcher} 接口，但子类又希望作为切入点时，这是一个方便的超类。
 *
 * <p>可以通过设置 {@link #setClassFilter "classFilter"} 属性来自定义 {@link ClassFilter} 的行为。默认值为 {@link ClassFilter#TRUE}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class StaticMethodMatcherPointcut extends StaticMethodMatcher implements Pointcut {

    private ClassFilter classFilter = ClassFilter.TRUE;

    /**
     * 设置用于此切入点（pointcut）的类过滤器（ClassFilter）。
     * 默认值为 {@link ClassFilter#TRUE}。
     */
    public void setClassFilter(ClassFilter classFilter) {
        this.classFilter = classFilter;
    }

    @Override
    public ClassFilter getClassFilter() {
        return this.classFilter;
    }

    @Override
    public final MethodMatcher getMethodMatcher() {
        return this;
    }
}
