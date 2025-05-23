// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache许可证版本2.0（“许可证”）授权；
除非根据法律规定或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下网址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为或特定用途的适用性。
有关许可的具体语言、权限和限制，请参阅许可证。*/
package org.springframework.aop;

import java.io.Serializable;

/**
 * 始终匹配的规范切点实例。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
final class TruePointcut implements Pointcut, Serializable {

    public static final TruePointcut INSTANCE = new TruePointcut();

    /**
     * 强制实现单例模式。
     */
    private TruePointcut() {
    }

    @Override
    public ClassFilter getClassFilter() {
        return ClassFilter.TRUE;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return MethodMatcher.TRUE;
    }

    /**
     * 用于支持序列化。在反序列化时用规范实例替换，保护单例模式。
     * 作为重写 {@code equals()} 的替代方案。
     */
    private Object readResolve() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "Pointcut.TRUE";
    }
}
