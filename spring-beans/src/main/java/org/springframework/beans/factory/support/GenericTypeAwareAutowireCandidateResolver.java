// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0（“许可”）授权；
* 除非遵守许可，否则不得使用此文件。
* 您可以在以下链接获取许可副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，
* 在许可下分发的软件按照“原样”分发，
* 不提供任何形式的质量保证或适用条件，
* 无论明确还是暗示。请参阅许可了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.Properties;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * 基础的 {@link AutowireCandidateResolver}，当依赖被声明为泛型类型时（例如，`@Repository<Customer>`），将执行与候选者类型的完全泛型类型匹配。
 *
 * <p>这是 {@link org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver} 的基类，提供在此级别上所有非注解式解析步骤的实现。
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
public class GenericTypeAwareAutowireCandidateResolver extends SimpleAutowireCandidateResolver implements BeanFactoryAware, Cloneable {

    @Nullable
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Nullable
    protected final BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        if (!super.isAutowireCandidate(bdHolder, descriptor)) {
            // 如果明确为false，则不执行任何其他检查...
            return false;
        }
        return checkGenericTypeMatch(bdHolder, descriptor);
    }

    /**
     * 将给定的依赖类型与其泛型类型信息与给定的候选bean定义进行匹配。
     */
    protected boolean checkGenericTypeMatch(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        ResolvableType dependencyType = descriptor.getResolvableType();
        if (dependencyType.getType() instanceof Class) {
            // 没有泛型类型 -> 我们知道它是Class类型匹配，因此无需再次检查。
            return true;
        }
        ResolvableType targetType = null;
        boolean cacheType = false;
        RootBeanDefinition rbd = null;
        if (bdHolder.getBeanDefinition() instanceof RootBeanDefinition rootBeanDef) {
            rbd = rootBeanDef;
        }
        if (rbd != null) {
            targetType = rbd.targetType;
            if (targetType == null) {
                cacheType = true;
                // 首先，检查工厂方法返回类型，如果适用
                targetType = getReturnTypeForFactoryMethod(rbd, descriptor);
                if (targetType == null) {
                    RootBeanDefinition dbd = getResolvedDecoratedDefinition(rbd);
                    if (dbd != null) {
                        targetType = dbd.targetType;
                        if (targetType == null) {
                            targetType = getReturnTypeForFactoryMethod(dbd, descriptor);
                        }
                    }
                }
            } else {
                // 现有目标类型：在存在泛型FactoryBean类型的情况下，
                // 在匹配非FactoryBean类型时，取消嵌套泛型类型的包装。
                Class<?> resolvedClass = targetType.resolve();
                if (resolvedClass != null && FactoryBean.class.isAssignableFrom(resolvedClass)) {
                    Class<?> typeToBeMatched = dependencyType.resolve();
                    if (typeToBeMatched != null && !FactoryBean.class.isAssignableFrom(typeToBeMatched) && !typeToBeMatched.isAssignableFrom(resolvedClass)) {
                        targetType = targetType.getGeneric();
                        if (descriptor.fallbackMatchAllowed()) {
                            // 匹配基于类的类型确定与FactoryBean
                            // 在以下 lazy-determination getType 代码路径中的对象。
                            targetType = ResolvableType.forClass(targetType.resolve());
                        }
                    }
                }
            }
        }
        if (targetType == null) {
            // 常规情况：直接使用 Bean 实例，BeanFactory 可用。
            if (this.beanFactory != null) {
                Class<?> beanType = this.beanFactory.getType(bdHolder.getBeanName());
                if (beanType != null) {
                    targetType = ResolvableType.forClass(ClassUtils.getUserClass(beanType));
                }
            }
            // 回退：未设置BeanFactory，或无法通过它解析类型
            // -> 如果适用，则对目标类进行尽力匹配。
            if (targetType == null && rbd != null && rbd.hasBeanClass() && rbd.getFactoryMethodName() == null) {
                Class<?> beanClass = rbd.getBeanClass();
                if (!FactoryBean.class.isAssignableFrom(beanClass)) {
                    targetType = ResolvableType.forClass(ClassUtils.getUserClass(beanClass));
                }
            }
        }
        if (targetType == null) {
            return true;
        }
        if (cacheType) {
            rbd.targetType = targetType;
        }
        if (descriptor.fallbackMatchAllowed() && (targetType.hasUnresolvableGenerics() || targetType.resolve() == Properties.class)) {
            // 回退匹配允许无法解析的泛型，例如将普通 HashMap 转换为 Map<String, String>。
            // 并且从实用主义的角度来看，也将 java.util.Properties 转换为任何 Map（尽管形式上它是一个
            // Map<Object, Object> 和 java.util.Properties 通常被视为 (Map<String, String>)。
            return true;
        }
        // 全面检查复杂泛型类型匹配...
        return dependencyType.isAssignableFrom(targetType);
    }

    @Nullable
    protected RootBeanDefinition getResolvedDecoratedDefinition(RootBeanDefinition rbd) {
        BeanDefinitionHolder decDef = rbd.getDecoratedDefinition();
        if (decDef != null && this.beanFactory instanceof ConfigurableListableBeanFactory clbf) {
            if (clbf.containsBeanDefinition(decDef.getBeanName())) {
                BeanDefinition dbd = clbf.getMergedBeanDefinition(decDef.getBeanName());
                if (dbd instanceof RootBeanDefinition rootBeanDef) {
                    return rootBeanDef;
                }
            }
        }
        return null;
    }

    @Nullable
    protected ResolvableType getReturnTypeForFactoryMethod(RootBeanDefinition rbd, DependencyDescriptor descriptor) {
        // 通常应适用于任何类型的工厂方法，因为BeanFactory
        // 在它们达到 AutowireCandidateResolver 之前预先解析它们...
        ResolvableType returnType = rbd.factoryMethodReturnType;
        if (returnType == null) {
            Method factoryMethod = rbd.getResolvedFactoryMethod();
            if (factoryMethod != null) {
                returnType = ResolvableType.forMethodReturnType(factoryMethod);
            }
        }
        if (returnType != null) {
            Class<?> resolvedClass = returnType.resolve();
            if (resolvedClass != null && descriptor.getDependencyType().isAssignableFrom(resolvedClass)) {
                // 仅当返回类型确实足够表达时才使用工厂方法元数据
                // 对于我们的依赖项。否则，返回的实例类型可能已经匹配
                // 如果容器中已经注册了一个单例实例。
                return returnType;
            }
        }
        return null;
    }

    /**
     * 此实现通过标准的 {@link Cloneable} 支持克隆所有实例字段，允许通过新的 {@link #setBeanFactory} 调用来重新配置克隆的实例。
     * @see #clone()
     */
    @Override
    public AutowireCandidateResolver cloneIfNecessary() {
        try {
            return (AutowireCandidateResolver) clone();
        } catch (CloneNotSupportedException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
