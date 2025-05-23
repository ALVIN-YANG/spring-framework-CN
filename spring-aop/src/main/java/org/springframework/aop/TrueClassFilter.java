// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop;

import java.io.Serializable;

/**
 * 匹配所有类的规范 ClassFilter 实例。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
final class TrueClassFilter implements ClassFilter, Serializable {

    public static final TrueClassFilter INSTANCE = new TrueClassFilter();

    /**
     * 实现单例模式。
     */
    private TrueClassFilter() {
    }

    @Override
    public boolean matches(Class<?> clazz) {
        return true;
    }

    /**
     * 用于支持序列化。在反序列化时，替换为规范实例，以保护单例模式。
     * 是重写 {@code equals()} 的替代方案。
     */
    private Object readResolve() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "ClassFilter.TRUE";
    }
}
