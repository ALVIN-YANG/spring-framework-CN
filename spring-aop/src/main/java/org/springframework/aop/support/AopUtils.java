// 翻译完成 glm-4-flash
/*版权所有 2002-2024 原作者或作者们。

根据Apache许可证版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件
是按“原样”分发的，不提供任何形式的明示或暗示保证，
包括但不限于适销性、特定用途适用性和非侵权性。
有关许可的具体语言，请参阅许可证中规定的权限和限制。*/
package org.springframework.aop.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.aop.Advisor;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * AOP支持代码的实用方法。
 *
 * <p>主要供Spring的AOP支持内部使用。
 *
 * <p>有关依赖Spring AOP框架实现内部机制的框架特定AOP实用方法的集合，请参阅{@link org.springframework.aop.framework.AopProxyUtils}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see org.springframework.aop.framework.AopProxyUtils
 */
public abstract class AopUtils {

    /**
     * 检查给定的对象是否是 JDK 动态代理或 CGLIB 代理。
     * <p>此方法还额外检查给定的对象是否为 {@link SpringProxy} 的实例。
     * @param object 要检查的对象
     * @see #isJdkDynamicProxy
     * @see #isCglibProxy
     */
    public static boolean isAopProxy(@Nullable Object object) {
        return (object instanceof SpringProxy && (Proxy.isProxyClass(object.getClass()) || object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)));
    }

    /**
     * 检查给定的对象是否是 JDK 动态代理。
     * <p>此方法超越了 {@link Proxy#isProxyClass(Class)} 的实现，额外检查给定的对象是否是 {@link SpringProxy} 的实例。
     * @param object 要检查的对象
     * @see java.lang.reflect.Proxy#isProxyClass
     */
    public static boolean isJdkDynamicProxy(@Nullable Object object) {
        return (object instanceof SpringProxy && Proxy.isProxyClass(object.getClass()));
    }

    /**
     * 检查给定的对象是否是CGLIB代理。
     * <p>此方法不仅超越了{@link ClassUtils#isCglibProxy(Object)}的实现，还额外检查给定的对象是否为{@link SpringProxy}的实例。
     * @param object 要检查的对象
     * @see ClassUtils#isCglibProxy(Object)
     */
    public static boolean isCglibProxy(@Nullable Object object) {
        return (object instanceof SpringProxy && object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR));
    }

    /**
     * 确定给定bean实例的目标类，该实例可能是一个AOP代理。
     * <p>返回AOP代理的目标类或否则返回普通类。
     * @param candidate 要检查的实例（可能是一个AOP代理）
     * @return 目标类（或给定对象的普通类作为后备；永远不会为null）
     * @see org.springframework.aop.TargetClassAware#getTargetClass()
     * @see org.springframework.aop.framework.AopProxyUtils#ultimateTargetClass(Object)
     */
    public static Class<?> getTargetClass(Object candidate) {
        Assert.notNull(candidate, "Candidate object must not be null");
        Class<?> result = null;
        if (candidate instanceof TargetClassAware targetClassAware) {
            result = targetClassAware.getTargetClass();
        }
        if (result == null) {
            result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
        }
        return result;
    }

    /**
     * 选择目标类型上的可调用方法：要么是给定的方法本身，如果实际上在目标类型上暴露了，否则是在目标类型的某个接口或目标类型本身上的相应方法。
     * @param method 要检查的方法
     * @param targetType 要在其上搜索方法的的目标类型（通常是一个AOP代理）
     * @return 目标类型上的相应可调用方法
     * @throws IllegalStateException 如果给定的方法在给定的目标类型上不可调用（通常由于代理不匹配）
     * @since 4.3
     * @see MethodIntrospector#selectInvocableMethod(Method, Class)
     */
    public static Method selectInvocableMethod(Method method, @Nullable Class<?> targetType) {
        if (targetType == null) {
            return method;
        }
        Method methodToUse = MethodIntrospector.selectInvocableMethod(method, targetType);
        if (Modifier.isPrivate(methodToUse.getModifiers()) && !Modifier.isStatic(methodToUse.getModifiers()) && SpringProxy.class.isAssignableFrom(targetType)) {
            throw new IllegalStateException(String.format("Need to invoke method '%s' found on proxy for target class '%s' but cannot " + "be delegated to target bean. Switch its visibility to package or protected.", method.getName(), method.getDeclaringClass().getSimpleName()));
        }
        return methodToUse;
    }

    /**
     * 判断给定的方法是否是“equals”方法。
     * @see java.lang.Object#equals
     */
    public static boolean isEqualsMethod(@Nullable Method method) {
        return ReflectionUtils.isEqualsMethod(method);
    }

    /**
     * 判断给定方法是否是“hashCode”方法。
     * @see java.lang.Object#hashCode
     */
    public static boolean isHashCodeMethod(@Nullable Method method) {
        return ReflectionUtils.isHashCodeMethod(method);
    }

    /**
     * 判断给定方法是否为 "toString" 方法。
     * @see java.lang.Object#toString()
     */
    public static boolean isToStringMethod(@Nullable Method method) {
        return ReflectionUtils.isToStringMethod(method);
    }

    /**
     * 判断给定方法是否为“finalize”方法。
     * @see java.lang.Object#finalize()
     */
    public static boolean isFinalizeMethod(@Nullable Method method) {
        return (method != null && method.getName().equals("finalize") && method.getParameterCount() == 0);
    }

    /**
     * 给定一个方法，该方法可能来自一个接口，以及当前AOP调用中使用的目标类，如果存在，找到相应的目标方法。例如，该方法可能是`IFoo.bar()`，而目标类可能是`DefaultFoo`。在这种情况下，该方法可能是`DefaultFoo.bar()`。这允许找到该方法的属性。
     * <p><b>注意：</b>与`org.springframework.util.ClassUtils#getMostSpecificMethod`相比，此方法解析桥接方法，以便从<i>原始</i>方法定义中检索属性。
     * @param method 要调用的方法，可能来自一个接口
     * @param targetClass 当前调用的目标类（可以为`null`或甚至可能不实现该方法）
     * @return 特定的目标方法，或者如果`targetClass`不实现它，则为原始方法
     * @see org.springframework.util.ClassUtils#getMostSpecificMethod
     */
    public static Method getMostSpecificMethod(Method method, @Nullable Class<?> targetClass) {
        Class<?> specificTargetClass = (targetClass != null ? ClassUtils.getUserClass(targetClass) : null);
        Method resolvedMethod = ClassUtils.getMostSpecificMethod(method, specificTargetClass);
        // 如果我们正在处理带有泛型参数的方法，找到原始方法。
        return BridgeMethodResolver.findBridgedMethod(resolvedMethod);
    }

    /**
     * 给定的切点能否应用于指定的类？
     * <p>这是一个重要的测试，因为它可以用来优化针对一个类的切点。
     * @param pc 要检查的静态或动态切点
     * @param targetClass 要测试的类
     * @return 切点是否可以应用于任何方法
     */
    public static boolean canApply(Pointcut pc, Class<?> targetClass) {
        return canApply(pc, targetClass, false);
    }

    /**
     * 给定的切入点是否可以在给定的类上完全应用？
     * <p>这是一个重要的测试，因为它可以用来优化
     * 对于类的切入点。
     * @param pc 要检查的静态或动态切入点
     * @param targetClass 要测试的类
     * @param hasIntroductions 是否该bean的顾问链
     * 包含任何引入
     * @return 切入点是否可以应用于任何方法
     */
    public static boolean canApply(Pointcut pc, Class<?> targetClass, boolean hasIntroductions) {
        Assert.notNull(pc, "Pointcut must not be null");
        if (!pc.getClassFilter().matches(targetClass)) {
            return false;
        }
        MethodMatcher methodMatcher = pc.getMethodMatcher();
        if (methodMatcher == MethodMatcher.TRUE) {
            // 如果无论如何都要匹配方法，就没有必要迭代它们了...
            return true;
        }
        IntroductionAwareMethodMatcher introductionAwareMethodMatcher = null;
        if (methodMatcher instanceof IntroductionAwareMethodMatcher iamm) {
            introductionAwareMethodMatcher = iamm;
        }
        Set<Class<?>> classes = new LinkedHashSet<>();
        if (!Proxy.isProxyClass(targetClass)) {
            classes.add(ClassUtils.getUserClass(targetClass));
        }
        classes.addAll(ClassUtils.getAllInterfacesForClassAsSet(targetClass));
        for (Class<?> clazz : classes) {
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
            for (Method method : methods) {
                if (introductionAwareMethodMatcher != null ? introductionAwareMethodMatcher.matches(method, targetClass, hasIntroductions) : methodMatcher.matches(method, targetClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 该顾问是否可以应用于给定的类？
     * 这是一个重要的测试，因为它可以用来优化
     * 某个类上的顾问。
     * @param advisor 要检查的顾问
     * @param targetClass 我们正在测试的类
     * @return 是否可以在任何方法上应用切入点
     */
    public static boolean canApply(Advisor advisor, Class<?> targetClass) {
        return canApply(advisor, targetClass, false);
    }

    /**
     * 给定的顾问是否可以在给定的类上应用？
     * <p>这是一个重要的测试，因为它可以用来优化掉对某个类的顾问。
     * 此版本还考虑了引入（对于IntroductionAwareMethodMatchers）。
     * @param advisor 要检查的顾问
     * @param targetClass 我们要测试的类
     * @param hasIntroductions 是否此bean的顾问链包括任何引入
     * @return 是否切入点可以在任何方法上应用
     */
    public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
        if (advisor instanceof IntroductionAdvisor ia) {
            return ia.getClassFilter().matches(targetClass);
        } else if (advisor instanceof PointcutAdvisor pca) {
            return canApply(pca.getPointcut(), targetClass, hasIntroductions);
        } else {
            // 它没有点切（pointcut），所以我们假设它适用。
            return true;
        }
    }

    /**
     * 确定适用于给定类的 {@code candidateAdvisors} 列表子列表。
     * @param candidateAdvisors 要评估的顾问列表
     * @param clazz 目标类
     * @return 可应用于给定类对象的顾问子列表（可能是传入的列表本身）
     */
    public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz) {
        if (candidateAdvisors.isEmpty()) {
            return candidateAdvisors;
        }
        List<Advisor> eligibleAdvisors = new ArrayList<>();
        for (Advisor candidate : candidateAdvisors) {
            if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
                eligibleAdvisors.add(candidate);
            }
        }
        boolean hasIntroductions = !eligibleAdvisors.isEmpty();
        for (Advisor candidate : candidateAdvisors) {
            if (candidate instanceof IntroductionAdvisor) {
                // 已处理
                continue;
            }
            if (canApply(candidate, clazz, hasIntroductions)) {
                eligibleAdvisors.add(candidate);
            }
        }
        return eligibleAdvisors;
    }

    /**
     * 通过反射调用给定的目标对象，作为面向切面编程（AOP）方法调用的一部分。
     * @param target 目标对象
     * @param method 要调用的方法
     * @param args 方法的参数
     * @return 如果有，则返回调用结果
     * @throws Throwable 如果由目标方法抛出
     * @throws org.springframework.aop.AopInvocationException 在反射错误的情况下抛出
     */
    @Nullable
    public static Object invokeJoinpointUsingReflection(@Nullable Object target, Method method, Object[] args) throws Throwable {
        // 使用反射来调用方法。
        try {
            ReflectionUtils.makeAccessible(method);
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            // 调用的方法抛出了一个检查型异常。
            // 我们必须重新抛出它。客户端将看不到拦截器。
            throw ex.getTargetException();
        } catch (IllegalArgumentException ex) {
            throw new AopInvocationException("AOP configuration seems to be invalid: tried calling method [" + method + "] on target [" + target + "]", ex);
        } catch (IllegalAccessException ex) {
            throw new AopInvocationException("Could not access method [" + method + "]", ex);
        }
    }
}
