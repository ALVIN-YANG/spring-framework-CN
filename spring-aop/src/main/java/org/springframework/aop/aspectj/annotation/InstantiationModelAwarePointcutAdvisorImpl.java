// 翻译完成 glm-4-flash
/*版权所有 2002-2024 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）许可；
除非根据法律规定或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途的适用性。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJPrecedenceInformation;
import org.springframework.aop.aspectj.InstantiationModelAwarePointcutAdvisor;
import org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.aop.support.Pointcuts;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * AspectJPointcutAdvisor 的内部实现。
 *
 * <p>请注意，对于每个目标方法，都会有一个此顾问的实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 */
@SuppressWarnings("serial")
final class InstantiationModelAwarePointcutAdvisorImpl implements InstantiationModelAwarePointcutAdvisor, AspectJPrecedenceInformation, Serializable {

    private static final Advice EMPTY_ADVICE = new Advice() {
    };

    private final AspectJExpressionPointcut declaredPointcut;

    private final Class<?> declaringClass;

    private final String methodName;

    private final Class<?>[] parameterTypes;

    private transient Method aspectJAdviceMethod;

    private final AspectJAdvisorFactory aspectJAdvisorFactory;

    private final MetadataAwareAspectInstanceFactory aspectInstanceFactory;

    private final int declarationOrder;

    private final String aspectName;

    private final Pointcut pointcut;

    private final boolean lazy;

    @Nullable
    private Advice instantiatedAdvice;

    @Nullable
    private Boolean isBeforeAdvice;

    @Nullable
    private Boolean isAfterAdvice;

    public InstantiationModelAwarePointcutAdvisorImpl(AspectJExpressionPointcut declaredPointcut, Method aspectJAdviceMethod, AspectJAdvisorFactory aspectJAdvisorFactory, MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {
        this.declaredPointcut = declaredPointcut;
        this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
        this.methodName = aspectJAdviceMethod.getName();
        this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
        this.aspectJAdviceMethod = aspectJAdviceMethod;
        this.aspectJAdvisorFactory = aspectJAdvisorFactory;
        this.aspectInstanceFactory = aspectInstanceFactory;
        this.declarationOrder = declarationOrder;
        this.aspectName = aspectName;
        if (aspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
            // 静态部分的切入点是一个懒加载类型。
            Pointcut preInstantiationPointcut = Pointcuts.union(aspectInstanceFactory.getAspectMetadata().getPerClausePointcut(), this.declaredPointcut);
            // 使其动态：必须在预实例化和后实例化状态之间进行修改。
            // 如果不是动态切点，它可能被优化掉
            // 在第一次评估之后，由 Spring AOP 基础设施执行。
            this.pointcut = new PerTargetInstantiationModelPointcut(this.declaredPointcut, preInstantiationPointcut, aspectInstanceFactory);
            this.lazy = true;
        } else {
            // 一个单例方面。
            this.pointcut = this.declaredPointcut;
            this.lazy = false;
            this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
        }
    }

    /**
     * 这是 Spring AOP 所使用的切入点。
     * 切入点的实际行为将根据通知的状态而变化。
     */
    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public boolean isLazy() {
        return this.lazy;
    }

    @Override
    public synchronized boolean isAdviceInstantiated() {
        return (this.instantiatedAdvice != null);
    }

    /**
     * 如有必要，则延迟实例化建议。
     */
    @Override
    public synchronized Advice getAdvice() {
        if (this.instantiatedAdvice == null) {
            this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
        }
        return this.instantiatedAdvice;
    }

    private Advice instantiateAdvice(AspectJExpressionPointcut pointcut) {
        Advice advice = this.aspectJAdvisorFactory.getAdvice(this.aspectJAdviceMethod, pointcut, this.aspectInstanceFactory, this.declarationOrder, this.aspectName);
        return (advice != null ? advice : EMPTY_ADVICE);
    }

    /**
     * 这仅在 Spring AOP 中具有意义：AspectJ 的实例化语义更为丰富。在 AspectJ 术语中，所有返回 {@code true} 的情况都意味着这里所说的方面不是单例的。
     */
    @Override
    public boolean isPerInstance() {
        return (getAspectMetadata().getAjType().getPerClause().getKind() != PerClauseKind.SINGLETON);
    }

    /**
     * 返回此顾问的 AspectJ AspectMetadata。
     */
    public AspectMetadata getAspectMetadata() {
        return this.aspectInstanceFactory.getAspectMetadata();
    }

    public MetadataAwareAspectInstanceFactory getAspectInstanceFactory() {
        return this.aspectInstanceFactory;
    }

    public AspectJExpressionPointcut getDeclaredPointcut() {
        return this.declaredPointcut;
    }

    @Override
    public int getOrder() {
        return this.aspectInstanceFactory.getOrder();
    }

    @Override
    public String getAspectName() {
        return this.aspectName;
    }

    @Override
    public int getDeclarationOrder() {
        return this.declarationOrder;
    }

    @Override
    public boolean isBeforeAdvice() {
        if (this.isBeforeAdvice == null) {
            determineAdviceType();
        }
        return this.isBeforeAdvice;
    }

    @Override
    public boolean isAfterAdvice() {
        if (this.isAfterAdvice == null) {
            determineAdviceType();
        }
        return this.isAfterAdvice;
    }

    /**
     * 从getAdvice复制了一些逻辑，但重要的是不强制创建建议。
     */
    private void determineAdviceType() {
        AspectJAnnotation aspectJAnnotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(this.aspectJAdviceMethod);
        if (aspectJAnnotation == null) {
            this.isBeforeAdvice = false;
            this.isAfterAdvice = false;
        } else {
            switch(aspectJAnnotation.getAnnotationType()) {
                case AtPointcut, AtAround ->
                    {
                        this.isBeforeAdvice = false;
                        this.isAfterAdvice = false;
                    }
                case AtBefore ->
                    {
                        this.isBeforeAdvice = true;
                        this.isAfterAdvice = false;
                    }
                case AtAfter, AtAfterReturning, AtAfterThrowing ->
                    {
                        this.isBeforeAdvice = false;
                        this.isAfterAdvice = true;
                    }
            }
        }
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        try {
            this.aspectJAdviceMethod = this.declaringClass.getMethod(this.methodName, this.parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Failed to find advice method on deserialization", ex);
        }
    }

    @Override
    public String toString() {
        return "InstantiationModelAwarePointcutAdvisor: expression [" + getDeclaredPointcut().getExpression() + "]; advice method [" + this.aspectJAdviceMethod + "]; perClauseKind=" + this.aspectInstanceFactory.getAspectMetadata().getAjType().getPerClause().getKind();
    }

    /**
     * Pointcut 实现，当建议（Advice）实例化时，其行为会发生变化。
     * 注意，这是一个 <i>动态</i> 的切入点；如果不是这样，如果它最初没有静态匹配，则可能被优化掉。
     */
    private static final class PerTargetInstantiationModelPointcut extends DynamicMethodMatcherPointcut {

        private final AspectJExpressionPointcut declaredPointcut;

        private final Pointcut preInstantiationPointcut;

        @Nullable
        private LazySingletonAspectInstanceFactoryDecorator aspectInstanceFactory;

        public PerTargetInstantiationModelPointcut(AspectJExpressionPointcut declaredPointcut, Pointcut preInstantiationPointcut, MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
            this.declaredPointcut = declaredPointcut;
            this.preInstantiationPointcut = preInstantiationPointcut;
            if (aspectInstanceFactory instanceof LazySingletonAspectInstanceFactoryDecorator lazyFactory) {
                this.aspectInstanceFactory = lazyFactory;
            }
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            // 我们要么是实例化的，要么是在声明的切点上进行匹配。
            // 或者对任一点切点进行未实例化的匹配...
            return (isAspectMaterialized() && this.declaredPointcut.matches(method, targetClass)) || this.preInstantiationPointcut.getMethodMatcher().matches(method, targetClass);
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass, Object... args) {
            // 这只能匹配已声明的切入点。
            return (isAspectMaterialized() && this.declaredPointcut.matches(method, targetClass, args));
        }

        private boolean isAspectMaterialized() {
            return (this.aspectInstanceFactory == null || this.aspectInstanceFactory.isMaterialized());
        }

        @Override
        public boolean equals(@Nullable Object other) {
            // 为了比较等价性，我们只需要比较 preInstantiationPointcut 字段，因为
            // 它们包括声明的pointcut字段。此外，我们不应该比较
            // 方面实例工厂的字段，因为 LazySingletonAspectInstanceFactoryDecorator 做了
            // 未实现equals()方法。
            return (this == other || (other instanceof PerTargetInstantiationModelPointcut that && ObjectUtils.nullSafeEquals(this.preInstantiationPointcut, that.preInstantiationPointcut)));
        }

        @Override
        public int hashCode() {
            return ObjectUtils.nullSafeHashCode(this.declaredPointcut.getExpression());
        }

        @Override
        public String toString() {
            return PerTargetInstantiationModelPointcut.class.getName() + ": " + this.declaredPointcut.getExpression();
        }
    }
}
