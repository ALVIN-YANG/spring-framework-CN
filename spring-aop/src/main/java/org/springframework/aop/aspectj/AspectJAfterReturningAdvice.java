// 翻译完成 glm-4-flash
/*版权所有 2002-2016 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的保证或条件，无论是明示的还是隐含的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.springframework.aop.AfterAdvice;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.TypeUtils;

/**
 * Spring AOP 提供的包装 AspectJ 后返回（after-returning）建议方法的建议。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice implements AfterReturningAdvice, AfterAdvice, Serializable {

    public AspectJAfterReturningAdvice(Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {
        super(aspectJBeforeAdviceMethod, pointcut, aif);
    }

    @Override
    public boolean isBeforeAdvice() {
        return false;
    }

    @Override
    public boolean isAfterAdvice() {
        return true;
    }

    @Override
    public void setReturningName(String name) {
        setReturningNameNoCheck(name);
    }

    @Override
    public void afterReturning(@Nullable Object returnValue, Method method, Object[] args, @Nullable Object target) throws Throwable {
        if (shouldInvokeOnReturnValueOf(method, returnValue)) {
            invokeAdviceMethod(getJoinPointMatch(), returnValue, null);
        }
    }

    /**
     * 依据 AspectJ 的语义，如果指定了返回语句，则只有当返回值是给定返回类型及其任何泛型类型参数的实例，并且泛型类型参数匹配赋值规则时，才会调用通知。如果返回类型是 Object，则通知始终会被调用。
     * @param returnValue 目标方法的返回值
     * @return 对于给定的返回值是否调用通知方法
     */
    private boolean shouldInvokeOnReturnValueOf(Method method, @Nullable Object returnValue) {
        Class<?> type = getDiscoveredReturningType();
        Type genericType = getDiscoveredReturningGenericType();
        // 如果我们处理的不是原始类型，检查泛型参数是否可赋值。
        return (matchesReturnValue(type, method, returnValue) && (genericType == null || genericType == type || TypeUtils.isAssignable(genericType, method.getGenericReturnType())));
    }

    /**
     * 根据AspectJ的语义，如果返回值是null（或者返回类型是void），
     * 则应该使用目标方法的返回类型来决定是否调用通知。此外，即使返回类型是void，
     * 如果通知方法中声明的参数类型是Object，则通知仍然必须被调用。
     * @param type 通知方法中声明的参数类型
     * @param method 通知方法
     * @param returnValue 目标方法的返回值
     * @return 对于给定的返回值和类型，是否调用通知方法
     */
    private boolean matchesReturnValue(Class<?> type, Method method, @Nullable Object returnValue) {
        if (returnValue != null) {
            return ClassUtils.isAssignableValue(type, returnValue);
        } else if (Object.class == type && void.class == method.getReturnType()) {
            return true;
        } else {
            return ClassUtils.isAssignable(type, method.getReturnType());
        }
    }
}
