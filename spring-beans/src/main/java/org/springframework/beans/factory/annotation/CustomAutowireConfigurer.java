// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用，除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“现状”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性或非侵权性。
* 请参阅许可证了解管理许可权和限制的具体语言。*/
package org.springframework.beans.factory.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * 一个实现 {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} 的类，允许方便地注册自定义自动装配限定符类型。
 *
 * <pre class="code">
 * &lt;bean id="customAutowireConfigurer" class="org.springframework.beans.factory.annotation.CustomAutowireConfigurer"&gt;
 *   &lt;property name="customQualifierTypes"&gt;
 *     &lt;set&gt;
 *       &lt;value&gt;mypackage.MyQualifier&lt;/value&gt;
 *     &lt;/set&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.beans.factory.annotation.Qualifier
 */
public class CustomAutowireConfigurer implements BeanFactoryPostProcessor, BeanClassLoaderAware, Ordered {

    // 默认：与非有序相同
    private int order = Ordered.LOWEST_PRECEDENCE;

    @Nullable
    private Set<?> customQualifierTypes;

    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }

    /**
     * 注册自定义限定符注解类型，以便在自动装配 bean 时考虑。提供的集合中的每个元素可以是 Class 实例，也可以是自定义注解的完全限定类名的字符串表示。
     * <p>注意，任何自身被 Spring 的 {@link org.springframework.beans.factory.annotation.Qualifier} 注解的注解不需要显式注册。
     * @param customQualifierTypes 要注册的自定义类型
     */
    public void setCustomQualifierTypes(Set<?> customQualifierTypes) {
        this.customQualifierTypes = customQualifierTypes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (this.customQualifierTypes != null) {
            if (!(beanFactory instanceof DefaultListableBeanFactory dlbf)) {
                throw new IllegalStateException("CustomAutowireConfigurer needs to operate on a DefaultListableBeanFactory");
            }
            if (!(dlbf.getAutowireCandidateResolver() instanceof QualifierAnnotationAutowireCandidateResolver)) {
                dlbf.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
            }
            QualifierAnnotationAutowireCandidateResolver resolver = (QualifierAnnotationAutowireCandidateResolver) dlbf.getAutowireCandidateResolver();
            for (Object value : this.customQualifierTypes) {
                Class<? extends Annotation> customType = null;
                if (value instanceof Class) {
                    customType = (Class<? extends Annotation>) value;
                } else if (value instanceof String className) {
                    customType = (Class<? extends Annotation>) ClassUtils.resolveClassName(className, this.beanClassLoader);
                } else {
                    throw new IllegalArgumentException("Invalid value [" + value + "] for custom qualifier type: needs to be Class or String.");
                }
                if (!Annotation.class.isAssignableFrom(customType)) {
                    throw new IllegalArgumentException("Qualifier type [" + customType.getName() + "] needs to be annotation type");
                }
                resolver.addQualifierType(customType);
            }
        }
    }
}
