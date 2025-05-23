// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非遵守许可证。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“现状”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为还是适销性。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.GenericTypeAwareAutowireCandidateResolver;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * `@AutowireCandidateResolver` 实现类，用于匹配要自动装配的字段或参数上的 `@Qualifier` 注解与 Bean 定义限定符。
 * 还支持通过 `@Value` 注解建议的表达式值。
 *
 * <p>如果可用，还支持 JSR-330 的 `@jakarta.inject.Qualifier` 注解。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.5
 * @see AutowireCandidateQualifier
 * @see Qualifier
 * @see Value
 */
public class QualifierAnnotationAutowireCandidateResolver extends GenericTypeAwareAutowireCandidateResolver {

    private final Set<Class<? extends Annotation>> qualifierTypes = new LinkedHashSet<>(2);

    private Class<? extends Annotation> valueAnnotationType = Value.class;

    /**
     * 创建一个新的 QualifierAnnotationAutowireCandidateResolver
     * 用于处理 Spring 的标准 {@link Qualifier} 注解。
     * <p>如果可用，也支持 JSR-330 的 {@link jakarta.inject.Qualifier} 注解。
     */
    @SuppressWarnings("unchecked")
    public QualifierAnnotationAutowireCandidateResolver() {
        this.qualifierTypes.add(Qualifier.class);
        try {
            this.qualifierTypes.add((Class<? extends Annotation>) ClassUtils.forName("jakarta.inject.Qualifier", QualifierAnnotationAutowireCandidateResolver.class.getClassLoader()));
        } catch (ClassNotFoundException ex) {
            // JSR-330 API 不可用 - 简单地跳过。
        }
    }

    /**
     * 为给定的限定注解类型创建一个新的 QualifierAnnotationAutowireCandidateResolver。
     * @param qualifierType 要查找的限定注解
     */
    public QualifierAnnotationAutowireCandidateResolver(Class<? extends Annotation> qualifierType) {
        Assert.notNull(qualifierType, "'qualifierType' must not be null");
        this.qualifierTypes.add(qualifierType);
    }

    /**
     * 为给定的限定符注解类型创建一个新的 QualifierAnnotationAutowireCandidateResolver。
     * @param qualifierTypes 要查找的限定符注解
     */
    public QualifierAnnotationAutowireCandidateResolver(Set<Class<? extends Annotation>> qualifierTypes) {
        Assert.notNull(qualifierTypes, "'qualifierTypes' must not be null");
        this.qualifierTypes.addAll(qualifierTypes);
    }

    /**
     * 将给定类型注册为自动装配时的限定符。
     * <p>这识别了直接使用（在字段、方法参数和构造函数参数上）的限定符注解以及反过来识别实际限定符注解的元注解。
     * <p>此实现仅支持注解作为限定符类型。
     * 默认为Spring的{@link Qualifier}注解，它既作为直接使用的限定符，也作为元注解。
     * @param qualifierType 要注册的注解类型
     */
    public void addQualifierType(Class<? extends Annotation> qualifierType) {
        this.qualifierTypes.add(qualifierType);
    }

    /**
     * 设置 'value' 注解类型，用于字段、方法参数和构造函数参数。
     * 默认的值注解类型是 Spring 提供的 {@link Value} 注解。
     * 此设置属性存在，以便开发人员可以提供他们自己的（非 Spring 特定的）注解类型，以指示特定参数的默认值表达式。
     */
    public void setValueAnnotationType(Class<? extends Annotation> valueAnnotationType) {
        this.valueAnnotationType = valueAnnotationType;
    }

    /**
     * 判断提供的Bean定义是否是自动装配的候选者。
     * 要被视为候选者，Bean的<em>autowire-candidate</em>属性不得设置为'false'。此外，如果要将自动装配的字段或参数上的注解被此Bean工厂识别为<em>限定符</em>，则Bean必须与注解以及其可能包含的任何属性相匹配。Bean定义必须包含相同的限定符或通过元属性进行匹配。如果限定符或属性不匹配，则"值"属性将回退到与Bean名称或别名进行匹配。
     * @see Qualifier
     */
    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        boolean match = super.isAutowireCandidate(bdHolder, descriptor);
        if (match) {
            match = checkQualifiers(bdHolder, descriptor.getAnnotations());
            if (match) {
                MethodParameter methodParam = descriptor.getMethodParameter();
                if (methodParam != null) {
                    Method method = methodParam.getMethod();
                    if (method == null || void.class == method.getReturnType()) {
                        match = checkQualifiers(bdHolder, methodParam.getMethodAnnotations());
                    }
                }
            }
        }
        return match;
    }

    /**
     * 将给定的限定符注解与候选 Bean 定义进行匹配。
     */
    protected boolean checkQualifiers(BeanDefinitionHolder bdHolder, Annotation[] annotationsToSearch) {
        if (ObjectUtils.isEmpty(annotationsToSearch)) {
            return true;
        }
        SimpleTypeConverter typeConverter = new SimpleTypeConverter();
        for (Annotation annotation : annotationsToSearch) {
            Class<? extends Annotation> type = annotation.annotationType();
            boolean checkMeta = true;
            boolean fallbackToMeta = false;
            if (isQualifier(type)) {
                if (!checkQualifier(bdHolder, annotation, typeConverter)) {
                    fallbackToMeta = true;
                } else {
                    checkMeta = false;
                }
            }
            if (checkMeta) {
                boolean foundMeta = false;
                for (Annotation metaAnn : type.getAnnotations()) {
                    Class<? extends Annotation> metaType = metaAnn.annotationType();
                    if (isQualifier(metaType)) {
                        foundMeta = true;
                        // 仅当存在具有值的 @Qualifier 注解时才接受回退匹配...
                        // 否则，它只是一个用于自定义限定符注解的标记。
                        if ((fallbackToMeta && ObjectUtils.isEmpty(AnnotationUtils.getValue(metaAnn))) || !checkQualifier(bdHolder, metaAnn, typeConverter)) {
                            return false;
                        }
                    }
                }
                if (fallbackToMeta && !foundMeta) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查给定的注解类型是否为已识别的限定符类型。
     */
    protected boolean isQualifier(Class<? extends Annotation> annotationType) {
        for (Class<? extends Annotation> qualifierType : this.qualifierTypes) {
            if (annotationType.equals(qualifierType) || annotationType.isAnnotationPresent(qualifierType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将给定的限定符注解与候选bean定义进行匹配。
     */
    protected boolean checkQualifier(BeanDefinitionHolder bdHolder, Annotation annotation, TypeConverter typeConverter) {
        Class<? extends Annotation> type = annotation.annotationType();
        RootBeanDefinition bd = (RootBeanDefinition) bdHolder.getBeanDefinition();
        AutowireCandidateQualifier qualifier = bd.getQualifier(type.getName());
        if (qualifier == null) {
            qualifier = bd.getQualifier(ClassUtils.getShortName(type));
        }
        if (qualifier == null) {
            // 首先，检查有资格的元素上的注解，如果有任何注解
            Annotation targetAnnotation = getQualifiedElementAnnotation(bd, type);
            // 然后，检查工厂方法的注解，如果适用的话
            if (targetAnnotation == null) {
                targetAnnotation = getFactoryMethodAnnotation(bd, type);
            }
            if (targetAnnotation == null) {
                RootBeanDefinition dbd = getResolvedDecoratedDefinition(bd);
                if (dbd != null) {
                    targetAnnotation = getFactoryMethodAnnotation(dbd, type);
                }
            }
            if (targetAnnotation == null) {
                BeanFactory beanFactory = getBeanFactory();
                // 查找目标类上的匹配注解
                if (beanFactory != null) {
                    try {
                        Class<?> beanType = beanFactory.getType(bdHolder.getBeanName());
                        if (beanType != null) {
                            targetAnnotation = AnnotationUtils.getAnnotation(ClassUtils.getUserClass(beanType), type);
                        }
                    } catch (NoSuchBeanDefinitionException ex) {
                        // 这不是通常的情况 - 直接忽略类型检查...
                    }
                }
                if (targetAnnotation == null && bd.hasBeanClass()) {
                    targetAnnotation = AnnotationUtils.getAnnotation(ClassUtils.getUserClass(bd.getBeanClass()), type);
                }
            }
            if (targetAnnotation != null && targetAnnotation.equals(annotation)) {
                return true;
            }
        }
        Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
        if (attributes.isEmpty() && qualifier == null) {
            // 如果没有属性，则必须存在限定符
            return false;
        }
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            Object expectedValue = entry.getValue();
            Object actualValue = null;
            // 首先检查限定符
            if (qualifier != null) {
                actualValue = qualifier.getAttribute(attributeName);
            }
            if (actualValue == null) {
                // 回退到 Bean 定义属性
                actualValue = bd.getAttribute(attributeName);
            }
            if (actualValue == null && attributeName.equals(AutowireCandidateQualifier.VALUE_KEY) && expectedValue instanceof String name && bdHolder.matchesName(name)) {
                // 回退到使用 Bean 名称（或别名）匹配
                continue;
            }
            if (actualValue == null && qualifier != null) {
                // 回退到默认值，但仅当存在限定符时才回退
                actualValue = AnnotationUtils.getDefaultValue(annotation, attributeName);
            }
            if (actualValue != null) {
                actualValue = typeConverter.convertIfNecessary(actualValue, expectedValue.getClass());
            }
            if (!ObjectUtils.nullSafeEquals(expectedValue, actualValue)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    protected Annotation getQualifiedElementAnnotation(RootBeanDefinition bd, Class<? extends Annotation> type) {
        AnnotatedElement qualifiedElement = bd.getQualifiedElement();
        return (qualifiedElement != null ? AnnotationUtils.getAnnotation(qualifiedElement, type) : null);
    }

    @Nullable
    protected Annotation getFactoryMethodAnnotation(RootBeanDefinition bd, Class<? extends Annotation> type) {
        Method resolvedFactoryMethod = bd.getResolvedFactoryMethod();
        return (resolvedFactoryMethod != null ? AnnotationUtils.getAnnotation(resolvedFactoryMethod, type) : null);
    }

    /**
     * 判断给定的依赖是否声明了自动装配注解，通过检查其必需标志。
     * @see Autowired#required()
     */
    @Override
    public boolean isRequired(DependencyDescriptor descriptor) {
        if (!super.isRequired(descriptor)) {
            return false;
        }
        Autowired autowired = descriptor.getAnnotation(Autowired.class);
        return (autowired == null || autowired.required());
    }

    /**
     * 判断给定的依赖是否声明了限定符注解。
     * @see #isQualifier(Class)
     * @see Qualifier
     */
    @Override
    public boolean hasQualifier(DependencyDescriptor descriptor) {
        for (Annotation ann : descriptor.getAnnotations()) {
            if (isQualifier(ann.annotationType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断给定的依赖是否声明了值注解。
     * @see Value
     */
    @Override
    @Nullable
    public Object getSuggestedValue(DependencyDescriptor descriptor) {
        Object value = findValue(descriptor.getAnnotations());
        if (value == null) {
            MethodParameter methodParam = descriptor.getMethodParameter();
            if (methodParam != null) {
                value = findValue(methodParam.getMethodAnnotations());
            }
        }
        return value;
    }

    /**
     * 从给定的候选注解中确定一个建议值。
     */
    @Nullable
    protected Object findValue(Annotation[] annotationsToSearch) {
        if (annotationsToSearch.length > 0) {
            // 限定注解必须局部
            AnnotationAttributes attr = AnnotatedElementUtils.getMergedAnnotationAttributes(AnnotatedElementUtils.forAnnotations(annotationsToSearch), this.valueAnnotationType);
            if (attr != null) {
                return extractValue(attr);
            }
        }
        return null;
    }

    /**
     * 从给定的注解中提取 value 属性。
     * @since 4.3
     */
    protected Object extractValue(AnnotationAttributes attr) {
        Object value = attr.get(AnnotationUtils.VALUE);
        if (value == null) {
            throw new IllegalStateException("Value annotation must have a value attribute");
        }
        return value;
    }
}
