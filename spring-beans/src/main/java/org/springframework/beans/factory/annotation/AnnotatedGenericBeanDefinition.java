// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按"原样"提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵权。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 扩展了 {@link org.springframework.beans.factory.support.GenericBeanDefinition} 类，
 * 通过添加对通过 {@link AnnotatedBeanDefinition} 接口暴露的注解元数据的支持。
 *
 * <p>这种 GenericBeanDefinition 变体主要用于测试代码，该代码期望在 AnnotatedBeanDefinition
 * 上进行操作，例如 Spring 组件扫描支持中的策略实现（其中默认的定义类是
 * {@link org.springframework.context.annotation.ScannedGenericBeanDefinition}，
 * 它也实现了 AnnotatedBeanDefinition 接口）。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see AnnotatedBeanDefinition#getMetadata()
 * @see org.springframework.core.type.StandardAnnotationMetadata
 */
@SuppressWarnings("serial")
public class AnnotatedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

    private final AnnotationMetadata metadata;

    @Nullable
    private MethodMetadata factoryMethodMetadata;

    /**
     * 为给定的bean类创建一个新的AnnotatedGenericBeanDefinition。
     * @param beanClass 被加载的bean类
     */
    public AnnotatedGenericBeanDefinition(Class<?> beanClass) {
        setBeanClass(beanClass);
        this.metadata = AnnotationMetadata.introspect(beanClass);
    }

    /**
     * 为给定的注解元数据创建一个新的 AnnotatedGenericBeanDefinition，
     * 允许基于 ASM 的处理并避免提前加载 Bean 类。
     * 注意，此构造函数在功能上等同于
     * {@link org.springframework.context.annotation.ScannedGenericBeanDefinition
     * ScannedGenericBeanDefinition}，然而后者的语义表明一个 Bean 是通过组件扫描特别发现的，
     * 而不是通过其他方式。
     * @param metadata 指定 Bean 类的注解元数据
     * @since 3.1.1
     */
    public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata) {
        Assert.notNull(metadata, "AnnotationMetadata must not be null");
        if (metadata instanceof StandardAnnotationMetadata sam) {
            setBeanClass(sam.getIntrospectedClass());
        } else {
            setBeanClassName(metadata.getClassName());
        }
        this.metadata = metadata;
    }

    /**
     * 根据给定的注解元数据、注解类及其上的工厂方法，创建一个新的 AnnotatedGenericBeanDefinition。
     * @param metadata 待处理 bean 类的注解元数据
     * @param factoryMethodMetadata 已选工厂方法的元数据
     * @since 4.1.1
     */
    public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata, MethodMetadata factoryMethodMetadata) {
        this(metadata);
        Assert.notNull(factoryMethodMetadata, "MethodMetadata must not be null");
        setFactoryMethodName(factoryMethodMetadata.getMethodName());
        this.factoryMethodMetadata = factoryMethodMetadata;
    }

    @Override
    public final AnnotationMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    @Nullable
    public final MethodMetadata getFactoryMethodMetadata() {
        return this.factoryMethodMetadata;
    }
}
