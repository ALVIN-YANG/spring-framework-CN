// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”），您可能不得使用此文件除非符合许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或特定用途适用性。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.aop.aspectj.annotation;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJAfterAdvice;
import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAfterThrowingAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJMethodBeforeAdvice;
import org.springframework.aop.aspectj.DeclareParentsAdvisor;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConvertingComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.util.StringUtils;
import org.springframework.util.comparator.InstanceComparator;

/**
 * 可以根据遵循AspectJ注解语法的AspectJ类创建Spring AOP顾问的工厂，使用反射调用相应的方法。
 *
 * @author Rod Johnson
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 2.0
 */
@SuppressWarnings("serial")
public class ReflectiveAspectJAdvisorFactory extends AbstractAspectJAdvisorFactory implements Serializable {

    // 排除 @Pointcut 方法
    private static final MethodFilter adviceMethodFilter = ReflectionUtils.USER_DECLARED_METHODS.and(method -> (AnnotationUtils.getAnnotation(method, Pointcut.class) == null));

    private static final Comparator<Method> adviceMethodComparator;

    static {
        // 注意：尽管 @After 注解的顺序在 @AfterReturning 和 @AfterThrowing 注解之前，
        // 一个 `@After` 通知方法实际上会在 `@AfterReturning` 之后被调用。
        // 由于 AspectJAfterAdvice.invoke(MethodInvocation) 的原因，使用了 @AfterThrowing 方法。
        // 在一个 `try` 块中调用 `proceed()`，并且只调用 `@After` 通知方法
        // 在相应的 `finally` 块中。
        Comparator<Method> adviceKindComparator = new ConvertingComparator<>(new InstanceComparator<>(Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class), (Converter<Method, Annotation>) method -> {
            AspectJAnnotation ann = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(method);
            return (ann != null ? ann.getAnnotation() : null);
        });
        Comparator<Method> methodNameComparator = new ConvertingComparator<>(Method::getName);
        adviceMethodComparator = adviceKindComparator.thenComparing(methodNameComparator);
    }

    @Nullable
    private final BeanFactory beanFactory;

    /**
     * 创建一个新的 {@code ReflectiveAspectJAdvisorFactory}。
     */
    public ReflectiveAspectJAdvisorFactory() {
        this(null);
    }

    /**
     * 创建一个新的 {@code ReflectiveAspectJAdvisorFactory}，将给定的
     * {@link BeanFactory} 传播到创建的 {@link AspectJExpressionPointcut} 实例中，
     * 用于处理 bean 切点以及一致的 {@link ClassLoader} 解析。
     * @param beanFactory 要传播的 BeanFactory（可能为 {@code null}）
     * @since 4.3.6
     * @see AspectJExpressionPointcut#setBeanFactory
     * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getBeanClassLoader()
     */
    public ReflectiveAspectJAdvisorFactory(@Nullable BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
        Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
        String aspectName = aspectInstanceFactory.getAspectMetadata().getAspectName();
        validate(aspectClass);
        // 我们需要将 MetadataAwareAspectInstanceFactory 包装在一个装饰器中。
        // 以确保它只实例化一次。
        MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory = new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);
        List<Advisor> advisors = new ArrayList<>();
        for (Method method : getAdvisorMethods(aspectClass)) {
            if (method.equals(ClassUtils.getMostSpecificMethod(method, aspectClass))) {
                // 在 Spring Framework 5.2.7 之前，`advisors.size()` 被用作 `declarationOrderInAspect` 的声明顺序。
                // 获取advisor(...)来表示声明的方法列表中的“当前位置”。
                // 然而，自从 Java 7 以来，“当前位置”不再有效，因为 JDK 已经不再
                // 返回声明的方法的顺序，与它们在源代码中声明的顺序相同。
                // 因此，我们现在将所有advice方法的declarationOrderInAspect硬编码为0
                // 通过反射发现，以支持跨 JVM 启动时的可靠建议排序。
                // 具体来说，0 的值与在以下情况下使用的默认值相对应：
                // AspectJPrecedenceComparator.getAspectDeclarationOrder(Advisor). 翻译为中文为： AspectJ优先级比较器.getAspectDeclarationOrder(顾问).
                Advisor advisor = getAdvisor(method, lazySingletonAspectInstanceFactory, 0, aspectName);
                if (advisor != null) {
                    advisors.add(advisor);
                }
            }
        }
        // 如果是一个针对特定目标的方面，则发出一个模拟实例化的方面。
        if (!advisors.isEmpty() && lazySingletonAspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
            Advisor instantiationAdvisor = new SyntheticInstantiationAdvisor(lazySingletonAspectInstanceFactory);
            advisors.add(0, instantiationAdvisor);
        }
        // 查找介绍字段。
        for (Field field : aspectClass.getDeclaredFields()) {
            Advisor advisor = getDeclareParentsAdvisor(field);
            if (advisor != null) {
                advisors.add(advisor);
            }
        }
        return advisors;
    }

    private List<Method> getAdvisorMethods(Class<?> aspectClass) {
        List<Method> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(aspectClass, methods::add, adviceMethodFilter);
        if (methods.size() > 1) {
            methods.sort(adviceMethodComparator);
        }
        return methods;
    }

    /**
     * 为给定的引入字段构建一个 {@link org.springframework.aop.aspectj.DeclareParentsAdvisor}。
     * <p>生成的顾问需要对目标进行评估。
     * @param introductionField 要反射的字段
     * @return 顾问实例，如果没有顾问则返回 {@code null}
     */
    @Nullable
    private Advisor getDeclareParentsAdvisor(Field introductionField) {
        DeclareParents declareParents = introductionField.getAnnotation(DeclareParents.class);
        if (declareParents == null) {
            // 非介绍字段
            return null;
        }
        if (DeclareParents.class == declareParents.defaultImpl()) {
            throw new IllegalStateException("'defaultImpl' attribute must be set on DeclareParents");
        }
        return new DeclareParentsAdvisor(introductionField.getType(), declareParents.value(), declareParents.defaultImpl());
    }

    @Override
    @Nullable
    public Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrderInAspect, String aspectName) {
        validate(aspectInstanceFactory.getAspectMetadata().getAspectClass());
        AspectJExpressionPointcut expressionPointcut = getPointcut(candidateAdviceMethod, aspectInstanceFactory.getAspectMetadata().getAspectClass());
        if (expressionPointcut == null) {
            return null;
        }
        try {
            return new InstantiationModelAwarePointcutAdvisorImpl(expressionPointcut, candidateAdviceMethod, this, aspectInstanceFactory, declarationOrderInAspect, aspectName);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignoring incompatible advice method: " + candidateAdviceMethod, ex);
            }
            return null;
        }
    }

    @Nullable
    private AspectJExpressionPointcut getPointcut(Method candidateAdviceMethod, Class<?> candidateAspectClass) {
        AspectJAnnotation aspectJAnnotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
        if (aspectJAnnotation == null) {
            return null;
        }
        AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(candidateAspectClass, new String[0], new Class<?>[0]);
        ajexp.setExpression(aspectJAnnotation.getPointcutExpression());
        if (this.beanFactory != null) {
            ajexp.setBeanFactory(this.beanFactory);
        }
        return ajexp;
    }

    @Override
    @Nullable
    public Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut expressionPointcut, MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {
        Class<?> candidateAspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
        validate(candidateAspectClass);
        AspectJAnnotation aspectJAnnotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
        if (aspectJAnnotation == null) {
            return null;
        }
        // 如果我们到达这里，我们知道我们有一个 AspectJ 方法。
        // 检查该类是否是AspectJ注解类
        if (!isAspect(candidateAspectClass)) {
            throw new AopConfigException("Advice must be declared inside an aspect type: " + "Offending method '" + candidateAdviceMethod + "' in class [" + candidateAspectClass.getName() + "]");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Found AspectJ method: " + candidateAdviceMethod);
        }
        AbstractAspectJAdvice springAdvice;
        switch(aspectJAnnotation.getAnnotationType()) {
            case AtPointcut ->
                {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Processing pointcut '" + candidateAdviceMethod.getName() + "'");
                    }
                    return null;
                }
            case AtAround ->
                springAdvice = new AspectJAroundAdvice(candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
            case AtBefore ->
                springAdvice = new AspectJMethodBeforeAdvice(candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
            case AtAfter ->
                springAdvice = new AspectJAfterAdvice(candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
            case AtAfterReturning ->
                {
                    springAdvice = new AspectJAfterReturningAdvice(candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                    AfterReturning afterReturningAnnotation = (AfterReturning) aspectJAnnotation.getAnnotation();
                    if (StringUtils.hasText(afterReturningAnnotation.returning())) {
                        springAdvice.setReturningName(afterReturningAnnotation.returning());
                    }
                }
            case AtAfterThrowing ->
                {
                    springAdvice = new AspectJAfterThrowingAdvice(candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                    AfterThrowing afterThrowingAnnotation = (AfterThrowing) aspectJAnnotation.getAnnotation();
                    if (StringUtils.hasText(afterThrowingAnnotation.throwing())) {
                        springAdvice.setThrowingName(afterThrowingAnnotation.throwing());
                    }
                }
            default ->
                throw new UnsupportedOperationException("Unsupported advice type on method: " + candidateAdviceMethod);
        }
        // 现在来配置通知...
        springAdvice.setAspectName(aspectName);
        springAdvice.setDeclarationOrder(declarationOrder);
        String[] argNames = this.parameterNameDiscoverer.getParameterNames(candidateAdviceMethod);
        if (argNames != null) {
            springAdvice.setArgumentNamesFromStringArray(argNames);
        }
        springAdvice.calculateArgumentBindings();
        return springAdvice;
    }

    /**
     * 生成器顾问，用于实例化方面。
     * 由非单例方面的每句切点触发。
     * 通知没有效果。
     */
    @SuppressWarnings("serial")
    protected static class SyntheticInstantiationAdvisor extends DefaultPointcutAdvisor {

        public SyntheticInstantiationAdvisor(final MetadataAwareAspectInstanceFactory aif) {
            super(aif.getAspectMetadata().getPerClausePointcut(), (MethodBeforeAdvice) (method, args, target) -> aif.getAspectInstance());
        }
    }
}
