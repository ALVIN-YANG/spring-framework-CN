// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“许可协议”），除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可协议下分发的软件按“原样”提供，不提供任何形式的明示或暗示保证。
* 请参阅许可协议了解具体语言管辖的权限和限制。*/
package org.springframework.aop.aspectj.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;

/**
 * 用于创建基于AspectJ 5注释语法的AspectJ类所表示的Spring AOP通知的抽象基类。
 *
 * <p>此类处理注释解析和验证功能。它本身并不实际生成Spring AOP通知，这一操作留给了子类去实现。
 *
 * @author Rod Johnson
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 */
public abstract class AbstractAspectJAdvisorFactory implements AspectJAdvisorFactory {

    private static final Class<?>[] ASPECTJ_ANNOTATION_CLASSES = new Class<?>[] { Pointcut.class, Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class };

    /**
     * 可供子类使用的 Logger。
     */
    protected final Log logger = LogFactory.getLog(getClass());

    protected final ParameterNameDiscoverer parameterNameDiscoverer = new AspectJAnnotationParameterNameDiscoverer();

    @Override
    public boolean isAspect(Class<?> clazz) {
        return (AnnotationUtils.findAnnotation(clazz, Aspect.class) != null);
    }

    @Override
    public void validate(Class<?> aspectClass) throws AopConfigException {
        AjType<?> ajType = AjTypeSystem.getAjType(aspectClass);
        if (!ajType.isAspect()) {
            throw new NotAnAtAspectException(aspectClass);
        }
        if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOW) {
            throw new AopConfigException(aspectClass.getName() + " uses percflow instantiation model: " + "This is not supported in Spring AOP.");
        }
        if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOWBELOW) {
            throw new AopConfigException(aspectClass.getName() + " uses percflowbelow instantiation model: " + "This is not supported in Spring AOP.");
        }
    }

    /**
     * 查找并返回给定方法上的第一个 AspectJ 注解
     * （实际上应该只有一个注解...）。
     */
    @SuppressWarnings("unchecked")
    @Nullable
    protected static AspectJAnnotation findAspectJAnnotationOnMethod(Method method) {
        for (Class<?> annotationType : ASPECTJ_ANNOTATION_CLASSES) {
            AspectJAnnotation annotation = findAnnotation(method, (Class<Annotation>) annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    @Nullable
    private static AspectJAnnotation findAnnotation(Method method, Class<? extends Annotation> annotationType) {
        Annotation annotation = AnnotationUtils.findAnnotation(method, annotationType);
        if (annotation != null) {
            return new AspectJAnnotation(annotation);
        } else {
            return null;
        }
    }

    /**
     * 用于AspectJ注解类型的枚举。
     * @see AspectJAnnotation#getAnnotationType()
     */
    protected enum AspectJAnnotationType {

        AtPointcut,
        AtAround,
        AtBefore,
        AtAfter,
        AtAfterReturning,
        AtAfterThrowing
    }

    /**
     * 模拟 AspectJ 注解的类，公开其类型枚举和切入点字符串。
     */
    protected static class AspectJAnnotation {

        private static final String[] EXPRESSION_ATTRIBUTES = { "pointcut", "value" };

        private static final Map<Class<?>, AspectJAnnotationType> annotationTypeMap = Map.of(//
        Pointcut.class, //
        AspectJAnnotationType.AtPointcut, //
        Around.class, //
        AspectJAnnotationType.AtAround, //
        Before.class, //
        AspectJAnnotationType.AtBefore, //
        After.class, //
        AspectJAnnotationType.AtAfter, //
        AfterReturning.class, //
        AspectJAnnotationType.AtAfterReturning, //
        AfterThrowing.class, //
        AspectJAnnotationType.AtAfterThrowing);

        private final Annotation annotation;

        private final AspectJAnnotationType annotationType;

        private final String pointcutExpression;

        private final String argumentNames;

        public AspectJAnnotation(Annotation annotation) {
            this.annotation = annotation;
            this.annotationType = determineAnnotationType(annotation);
            try {
                this.pointcutExpression = resolvePointcutExpression(annotation);
                Object argNames = AnnotationUtils.getValue(annotation, "argNames");
                this.argumentNames = (argNames instanceof String names ? names : "");
            } catch (Exception ex) {
                throw new IllegalArgumentException(annotation + " is not a valid AspectJ annotation", ex);
            }
        }

        private AspectJAnnotationType determineAnnotationType(Annotation annotation) {
            AspectJAnnotationType type = annotationTypeMap.get(annotation.annotationType());
            if (type != null) {
                return type;
            }
            throw new IllegalStateException("Unknown annotation type: " + annotation);
        }

        private String resolvePointcutExpression(Annotation annotation) {
            for (String attributeName : EXPRESSION_ATTRIBUTES) {
                Object val = AnnotationUtils.getValue(annotation, attributeName);
                if (val instanceof String str && !str.isEmpty()) {
                    return str;
                }
            }
            throw new IllegalStateException("Failed to resolve pointcut expression in: " + annotation);
        }

        public AspectJAnnotationType getAnnotationType() {
            return this.annotationType;
        }

        public Annotation getAnnotation() {
            return this.annotation;
        }

        public String getPointcutExpression() {
            return this.pointcutExpression;
        }

        public String getArgumentNames() {
            return this.argumentNames;
        }

        @Override
        public String toString() {
            return this.annotation.toString();
        }
    }

    /**
     * 参数名称发现器的实现，该实现分析在 AspectJ 注解级别指定的参数名称。
     */
    private static class AspectJAnnotationParameterNameDiscoverer implements ParameterNameDiscoverer {

        private static final String[] EMPTY_ARRAY = new String[0];

        @Override
        @Nullable
        public String[] getParameterNames(Method method) {
            if (method.getParameterCount() == 0) {
                return EMPTY_ARRAY;
            }
            AspectJAnnotation annotation = findAspectJAnnotationOnMethod(method);
            if (annotation == null) {
                return null;
            }
            StringTokenizer nameTokens = new StringTokenizer(annotation.getArgumentNames(), ",");
            int numTokens = nameTokens.countTokens();
            if (numTokens > 0) {
                String[] names = new String[numTokens];
                for (int i = 0; i < names.length; i++) {
                    names[i] = nameTokens.nextToken();
                }
                return names;
            } else {
                return null;
            }
        }

        @Override
        @Nullable
        public String[] getParameterNames(Constructor<?> ctor) {
            throw new UnsupportedOperationException("Spring AOP cannot handle constructor advice");
        }
    }
}
