// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache License, Version 2.0 ("许可")条款许可；除非遵守许可，否则您不得使用此文件。
您可以在以下地址获取许可副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可下分发的软件按"原样"分发，不提供任何形式的明示或暗示保证。
有关许可的具体语言规定权限和限制，请参阅许可。*/
package org.springframework.aop.support;

import java.lang.reflect.Method;
import org.springframework.aop.MethodMatcher;

/**
 * 静态方法匹配器的便捷抽象超类，这类匹配器在运行时不关心参数。
 *
 * @author Rod Johnson
 */
public abstract class StaticMethodMatcher implements MethodMatcher {

    @Override
    public final boolean isRuntime() {
        return false;
    }

    @Override
    public final boolean matches(Method method, Class<?> targetClass, Object... args) {
        // 该方法绝不应该被调用，因为isRuntime()返回false。
        throw new UnsupportedOperationException("Illegal MethodMatcher usage");
    }
}
