// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）授权；
除非根据法律规定或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下链接获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 用于简单 <b>cflow</b> 风格切入点中的切入点和方法匹配器。
 * 注意，评估此类切入点的速度比评估正常切入点慢10-15倍，但它们在某些情况下很有用。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@SuppressWarnings("serial")
public class ControlFlowPointcut implements Pointcut, ClassFilter, MethodMatcher, Serializable {

    private final Class<?> clazz;

    @Nullable
    private final String methodName;

    private final AtomicInteger evaluations = new AtomicInteger();

    /**
     * 构造一个新的切入点，该切入点匹配该类以下的所有控制流。
     * @param clazz 类参数
     */
    public ControlFlowPointcut(Class<?> clazz) {
        this(clazz, null);
    }

    /**
     * 构建一个新的切入点，该切入点匹配给定类中给定方法下所有的调用。如果没有指定方法名，则匹配给定类下所有的控制流。
     * @param clazz 给定的类
     * @param methodName 方法的名称（可能为 {@code null}）
     */
    public ControlFlowPointcut(Class<?> clazz, @Nullable String methodName) {
        Assert.notNull(clazz, "Class must not be null");
        this.clazz = clazz;
        this.methodName = methodName;
    }

    /**
     * 子类可以重写此方法以实现更高级的过滤（以及性能提升）。
     */
    @Override
    public boolean matches(Class<?> clazz) {
        return true;
    }

    /**
     * 子类可以在可能的情况下覆盖此方法，以过滤掉一些候选类。
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return true;
    }

    @Override
    public boolean isRuntime() {
        return true;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, Object... args) {
        this.evaluations.incrementAndGet();
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            if (element.getClassName().equals(this.clazz.getName()) && (this.methodName == null || element.getMethodName().equals(this.methodName))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 了解我们射击了多少次，这对于优化是有用的。
     */
    public int getEvaluations() {
        return this.evaluations.get();
    }

    @Override
    public ClassFilter getClassFilter() {
        return this;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof ControlFlowPointcut that && this.clazz.equals(that.clazz)) && ObjectUtils.nullSafeEquals(this.methodName, that.methodName));
    }

    @Override
    public int hashCode() {
        int code = this.clazz.hashCode();
        if (this.methodName != null) {
            code = 37 * code + this.methodName.hashCode();
        }
        return code;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": class = " + this.clazz.getName() + "; methodName = " + this.methodName;
    }
}
