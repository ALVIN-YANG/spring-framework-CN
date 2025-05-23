// 翻译完成 glm-4-flash
/*版权所有 2002-2019 原作者或作者。

根据Apache License, Version 2.0（以下简称“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.util.Assert;

/**
 * 用于匹配getter和setter以及用于操作和评估pointcut的有用静态方法常量。
 *
 * <p>这些方法特别适用于使用并集和交集方法组合pointcut。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class Pointcuts {

    /**
     * 切点匹配所有类中的Bean属性设置器。
     */
    public static final Pointcut SETTERS = SetterPointcut.INSTANCE;

    /**
     * 切点匹配所有类的bean属性获取器。
     */
    public static final Pointcut GETTERS = GetterPointcut.INSTANCE;

    /**
     * 匹配所有给定切入点中<b>任意一个</b>（或两个都）匹配的方法。
     * @param pc1 第一个切入点
     * @param pc2 第二个切入点
     * @return 一个独特的切入点，匹配所有给定切入点中任意一个匹配的方法
     */
    public static Pointcut union(Pointcut pc1, Pointcut pc2) {
        return new ComposablePointcut(pc1).union(pc2);
    }

    /**
     * 匹配所有既符合第一个切点又符合第二个切点的方法。
     * @param pc1 第一个切点
     * @param pc2 第二个切点
     * @return 一个独特的切点，它匹配所有既符合第一个切点又符合第二个切点的方法
     */
    public static Pointcut intersection(Pointcut pc1, Pointcut pc2) {
        return new ComposablePointcut(pc1).intersection(pc2);
    }

    /**
     * 执行对切入点匹配的最便宜检查。
     * @param pointcut 要匹配的切入点
     * @param method 候选方法
     * @param targetClass 目标类
     * @param args 方法的参数
     * @return 是否存在运行时匹配
     */
    public static boolean matches(Pointcut pointcut, Method method, Class<?> targetClass, Object... args) {
        Assert.notNull(pointcut, "Pointcut must not be null");
        if (pointcut == Pointcut.TRUE) {
            return true;
        }
        if (pointcut.getClassFilter().matches(targetClass)) {
            // 只需检查它是否通过了第一个障碍。
            MethodMatcher mm = pointcut.getMethodMatcher();
            if (mm.matches(method, targetClass)) {
                // 我们可能需要额外的运行时（参数）检查。
                return (!mm.isRuntime() || mm.matches(method, targetClass, args));
            }
        }
        return false;
    }

    /**
     * 用于匹配bean属性设置器的切点实现。
     */
    @SuppressWarnings("serial")
    private static class SetterPointcut extends StaticMethodMatcherPointcut implements Serializable {

        public static final SetterPointcut INSTANCE = new SetterPointcut();

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return (method.getName().startsWith("set") && method.getParameterCount() == 1 && method.getReturnType() == Void.TYPE);
        }

        private Object readResolve() {
            return INSTANCE;
        }

        @Override
        public String toString() {
            return "Pointcuts.SETTERS";
        }
    }

    /**
     * 用于匹配bean属性获取器的切点实现。
     */
    @SuppressWarnings("serial")
    private static class GetterPointcut extends StaticMethodMatcherPointcut implements Serializable {

        public static final GetterPointcut INSTANCE = new GetterPointcut();

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return (method.getName().startsWith("get") && method.getParameterCount() == 0);
        }

        private Object readResolve() {
            return INSTANCE;
        }

        @Override
        public String toString() {
            return "Pointcuts.GETTERS";
        }
    }
}
