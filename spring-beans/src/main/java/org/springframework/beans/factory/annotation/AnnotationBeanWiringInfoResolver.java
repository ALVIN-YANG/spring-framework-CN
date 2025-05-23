// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.wiring.BeanWiringInfo;
import org.springframework.beans.factory.wiring.BeanWiringInfoResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 使用了 `@link org.springframework.beans.factory.wiring.BeanWiringInfoResolver` 的类，该类通过 `@Configurable` 注解来标识哪些类需要自动装配。
 * 要查找的 Bean 名称将从 `@Configurable` 注解中获取，如果指定了的话；否则将使用正在配置的类的完全限定名称作为默认值。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see Configurable
 * @see org.springframework.beans.factory.wiring.ClassNameBeanWiringInfoResolver
 */
public class AnnotationBeanWiringInfoResolver implements BeanWiringInfoResolver {

    @Override
    @Nullable
    public BeanWiringInfo resolveWiringInfo(Object beanInstance) {
        Assert.notNull(beanInstance, "Bean instance must not be null");
        Configurable annotation = beanInstance.getClass().getAnnotation(Configurable.class);
        return (annotation != null ? buildWiringInfo(beanInstance, annotation) : null);
    }

    /**
     * 为给定的 {@link Configurable} 注解构建 {@link BeanWiringInfo}。
     * @param beanInstance 需要构建 BeanWiringInfo 的 Bean 实例
     * @param annotation 在 Bean 类上找到的 Configurable 注解
     * @return 解析后的 BeanWiringInfo
     */
    protected BeanWiringInfo buildWiringInfo(Object beanInstance, Configurable annotation) {
        if (!Autowire.NO.equals(annotation.autowire())) {
            // 按名称或按类型自动装配
            return new BeanWiringInfo(annotation.autowire().value(), annotation.dependencyCheck());
        } else if (!annotation.value().isEmpty()) {
            // 显式指定了用于从属性值中获取的 Bean 名称的 Bean 定义
            return new BeanWiringInfo(annotation.value(), false);
        } else {
            // 默认的Bean名称，用于从Bean定义中获取属性值
            return new BeanWiringInfo(getDefaultBeanName(beanInstance), true);
        }
    }

    /**
     * 确定指定bean实例的默认bean名称。
     * <p>默认实现为CGLIB代理返回其超类名称，否则返回普通bean类的名称。
     * @param beanInstance 要为其构建默认名称的bean实例
     * @return 要使用的默认bean名称
     * @see org.springframework.util.ClassUtils#getUserClass(Class)
     */
    protected String getDefaultBeanName(Object beanInstance) {
        return ClassUtils.getUserClass(beanInstance).getName();
    }
}
