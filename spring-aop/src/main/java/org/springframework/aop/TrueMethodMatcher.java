// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache许可证版本2.0（以下简称“许可证”）许可；除非符合许可证规定，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 匹配所有方法的规范方法匹配器实例。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
final class TrueMethodMatcher implements MethodMatcher, Serializable {

    public static final TrueMethodMatcher INSTANCE = new TrueMethodMatcher();

    /**
     * 确保单例模式。
     */
    private TrueMethodMatcher() {
    }

    @Override
    public boolean isRuntime() {
        return false;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return true;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, Object... args) {
        // 永远不应直接调用，因为isRuntime返回false。
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "MethodMatcher.TRUE";
    }

    /**
     * 用于支持序列化。在反序列化时替换为规范实例，保护单例模式。
     * 是重写 {@code equals()} 的替代方案。
     */
    private Object readResolve() {
        return INSTANCE;
    }
}
