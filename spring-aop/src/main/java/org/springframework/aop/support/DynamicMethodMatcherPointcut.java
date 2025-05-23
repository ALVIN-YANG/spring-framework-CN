// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或原作者。

根据Apache License，版本2.0（以下简称“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下网址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权的保证。
有关许可的具体语言、权限和限制，请参阅许可证。*/
package org.springframework.aop.support;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

/**
 * 在我们希望强制子类实现 MethodMatcher 接口，但子类又想作为切入点时，这是一个便捷的超类。可以通过重写 getClassFilter() 方法来自定义 ClassFilter 的行为。
 *
 * @author Rod Johnson
 */
public abstract class DynamicMethodMatcherPointcut extends DynamicMethodMatcher implements Pointcut {

    @Override
    public ClassFilter getClassFilter() {
        return ClassFilter.TRUE;
    }

    @Override
    public final MethodMatcher getMethodMatcher() {
        return this;
    }
}
