// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。

根据Apache License, Version 2.0（“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可的具体语言管理权限和限制，请参阅许可证。*/
package org.springframework.aop;

import java.lang.reflect.Method;

/**
 *  一种特殊的{@link MethodMatcher}类型，在匹配方法时会考虑引入。如果目标类没有引入（introductions），方法匹配器可能能够更有效地进行匹配优化，例如。
 *
 * @author Adrian Colyer
 * @since 2.0
 */
public interface IntroductionAwareMethodMatcher extends MethodMatcher {

    /**
     * 执行静态检查以确定给定的方法是否匹配。如果调用者支持扩展的IntroductionAwareMethodMatcher接口，
     * 可以使用此方法代替带有两个参数的{@link #matches(java.lang.reflect.Method, Class)}方法。
     * @param method 候选方法
     * @param targetClass 目标类
     * @param hasIntroductions 如果我们询问的对象是一个或多个引入的主题，则为{@code true}；否则为{@code false}
     * @return 是否静态匹配此方法
     */
    boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions);
}
