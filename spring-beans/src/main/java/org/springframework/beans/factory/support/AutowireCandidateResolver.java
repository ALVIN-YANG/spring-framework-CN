// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，
* 在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.lang.Nullable;

/**
 * 确定特定Bean定义是否作为特定依赖的自动装配候选者的策略接口。
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 */
public interface AutowireCandidateResolver {

    /**
     * 判断给定的 Bean 定义是否满足作为给定依赖的自动装配候选者的条件。
     * 默认实现检查 {@link org.springframework.beans.factory.config.BeanDefinition#isAutowireCandidate()}。
     * @param bdHolder 包含 Bean 名称和别名的 Bean 定义
     * @param descriptor 目标方法参数或字段的描述符
     * @return Bean 定义是否满足自动装配候选者的条件
     * @see org.springframework.beans.factory.config.BeanDefinition#isAutowireCandidate()
     */
    default boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return bdHolder.getBeanDefinition().isAutowireCandidate();
    }

    /**
     * 判断给定的描述符是否实际上是必需的。
     * <p>默认实现检查{@link DependencyDescriptor#isRequired()}。
     * @param descriptor 目标方法参数或字段的描述符
     * @return 描述符是否被标记为必需，或者可能以其他方式（例如通过参数注解）指示非必需状态
     * @since 5.0
     * @see DependencyDescriptor#isRequired()
     */
    default boolean isRequired(DependencyDescriptor descriptor) {
        return descriptor.isRequired();
    }

    /**
     * 判断给定的描述符是否声明了超出类型的限定符（通常是——但不一定是特定类型的注解）。
     * <p>默认实现返回 {@code false}。
     * @param descriptor 目标方法参数或字段的描述符
     * @return 是否描述符声明了一个限定符，缩小了候选状态，超出类型匹配
     * @since 5.1
     * @see org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver#hasQualifier
     */
    default boolean hasQualifier(DependencyDescriptor descriptor) {
        return false;
    }

    /**
     * 判断是否建议为给定的依赖项设置默认值。
     * <p>默认实现简单地返回 {@code null}。
     * @param descriptor 目标方法参数或字段的描述符
     * @return 建议的值（通常是表达式字符串），
     * 或者在未找到时返回 {@code null}
     * @since 3.0
     */
    @Nullable
    default Object getSuggestedValue(DependencyDescriptor descriptor) {
        return null;
    }

    /**
     * 构建一个代理，用于对实际依赖目标的延迟解析，
     * 如果注入点需要的话。
     * <p>默认实现简单返回 {@code null}。
     * @param descriptor 目标方法参数或字段的描述符
     * @param beanName 包含注入点的bean的名称
     * @return 实际依赖目标的延迟解析代理，
     * 或者在需要直接解析时返回 {@code null}
     * @since 4.0
     */
    @Nullable
    default Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, @Nullable String beanName) {
        return null;
    }

    /**
     * 确定代理类以实现依赖目标的延迟解析，如果注入点需要的话。
     * <p>默认实现简单地返回 {@code null}。
     * @param descriptor 目标方法参数或字段的描述符
     * @param beanName 包含注入点的bean的名称
     * @return 如果有，则返回依赖目标的延迟解析代理类
     * @since 6.0
     */
    @Nullable
    default Class<?> getLazyResolutionProxyClass(DependencyDescriptor descriptor, @Nullable String beanName) {
        return null;
    }

    /**
     * 如果需要，返回此解析器实例的克隆，保留其本地配置，并允许克隆实例与新的bean工厂关联，如果没有此类状态，则与原始实例关联。
     * <p>默认实现通过默认类构造函数创建一个单独的实例，假设没有要复制的特定配置状态。
     * 子类可以通过自定义配置状态处理或使用标准的{@link Cloneable}支持（如Spring自己配置的{@code AutowireCandidateResolver}变体实现）来覆盖此方法，或者简单地返回{@code this}（如{@link SimpleAutowireCandidateResolver}）。
     * @since 5.2.7
     * @see GenericTypeAwareAutowireCandidateResolver#cloneIfNecessary()
     * @see DefaultListableBeanFactory#copyConfigurationFrom
     */
    default AutowireCandidateResolver cloneIfNecessary() {
        return BeanUtils.instantiateClass(getClass());
    }
}
