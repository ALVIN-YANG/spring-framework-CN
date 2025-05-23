// 翻译完成 glm-4-flash
/*版权所有 2002-2015 原作者或作者。

根据Apache License，版本2.0（“许可证”）许可；
除非法律要求或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“现状”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权或特定用途的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import java.lang.reflect.Method;
import java.util.List;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.lang.Nullable;

/**
 * 用于从使用AspectJ注解语法的类中创建Spring AOP顾问的工厂接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see AspectMetadata
 * @see org.aspectj.lang.reflect.AjTypeSystem
 */
public interface AspectJAdvisorFactory {

    /**
     * 判断给定的类是否为切面，由 AspectJ 的 {@link org.aspectj.lang.reflect.AjTypeSystem} 报告。
     * <p>如果所谓的切面无效（例如是具体切面类的扩展），将直接返回 {@code false}。
     * 对于一些 Spring AOP 无法处理的切面，如不支持实例化模型的切面，将返回 true。
     * 如果需要，可以使用 {@link #validate} 方法来处理这些情况。
     * @param clazz 假定的注解风格的 AspectJ 类
     * @return 是否被 AspectJ 识别为切面类
     */
    boolean isAspect(Class<?> clazz);

    /**
     * 判断给定的类是否是有效的AspectJ方面类？
     * @param aspectClass 需要验证的假设AspectJ注解风格的类
     * @throws AopConfigException 如果该类是一个无效的方面（这种情况绝对不合法）
     * @throws NotAnAtAspectException 如果该类根本不是方面（这种情况可能合法，也可能不合法，取决于上下文）
     */
    void validate(Class<?> aspectClass) throws AopConfigException;

    /**
     * 为指定方面实例上的所有注解At-AspectJ方法构建Spring AOP顾问
     * @param aspectInstanceFactory 方面实例工厂（不是方面实例本身，以避免提前实例化）
     * @return 该类的一个顾问列表
     */
    List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory);

    /**
     * 为给定的AspectJ建议方法构建一个Spring AOP顾问。
     * @param candidateAdviceMethod 候选建议方法
     * @param aspectInstanceFactory Aspect实例工厂
     * @param declarationOrder Aspect内部声明顺序
     * @param aspectName Aspect的名称
     * @return 如果该方法不是AspectJ建议方法，或者它是一个将被其他建议使用但不会单独创建Spring建议的切点，则返回null
     */
    @Nullable
    Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);

    /**
     * 为给定的AspectJ建议方法构建一个Spring AOP建议。
     * @param candidateAdviceMethod 候选建议方法
     * @param expressionPointcut AspectJ表达式切点
     * @param aspectInstanceFactory Aspect实例工厂
     * @param declarationOrder Aspect内部声明顺序
     * @param aspectName Aspect名称
     * @return 如果该方法不是AspectJ建议方法，或者它是一个将被其他建议使用但不会在其自身创建Spring建议的切点，则返回null
     * @see org.springframework.aop.aspectj.AspectJAroundAdvice
     * @see org.springframework.aop.aspectj.AspectJMethodBeforeAdvice
     * @see org.springframework.aop.aspectj.AspectJAfterAdvice
     * @see org.springframework.aop.aspectj.AspectJAfterReturningAdvice
     * @see org.springframework.aop.aspectj.AspectJAfterThrowingAdvice
     */
    @Nullable
    Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut expressionPointcut, MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);
}
