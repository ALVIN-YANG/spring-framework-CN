// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或原作者。
 
根据Apache许可证第2版（“许可证”）授权；除非符合许可证规定，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律强制规定或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可的具体语言、权限和限制，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Conventions;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 用于自动代理感知组件的实用工具。
 * 主要用于框架内部使用。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see AbstractAutoProxyCreator
 */
public abstract class AutoProxyUtils {

    /**
     * 用于定义是否应该使用目标类来代理给定Bean的属性。该值可以是 {@code Boolean.TRUE} 或 {@code Boolean.FALSE}。
     * <p>代理工厂可以设置此属性，如果它们为特定Bean构建了目标类代理，并且想要强制该Bean始终可以被转换为它的目标类（即使通过自动代理应用了AOP通知）。
     * @see #shouldProxyTargetClass
     */
    public static final String PRESERVE_TARGET_CLASS_ATTRIBUTE = Conventions.getQualifiedAttributeName(AutoProxyUtils.class, "preserveTargetClass");

    /**
     * 表示自动代理的bean的原目标类定义属性，例如用于在基于接口的代理后面的目标类上检查注解。
     * @since 4.2.3
     * @see #determineTargetClass
     */
    public static final String ORIGINAL_TARGET_CLASS_ATTRIBUTE = Conventions.getQualifiedAttributeName(AutoProxyUtils.class, "originalTargetClass");

    /**
     * 判断给定的Bean是否应该使用其目标类而不是接口进行代理。检查相应的Bean定义中的
     * {@link #PRESERVE_TARGET_CLASS_ATTRIBUTE "preserveTargetClass" 属性}。
     * @param beanFactory 包含的 ConfigurableListableBeanFactory
     * @param beanName Bean的名称
     * @return 给定的Bean是否应该使用其目标类进行代理
     */
    public static boolean shouldProxyTargetClass(ConfigurableListableBeanFactory beanFactory, @Nullable String beanName) {
        if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            return Boolean.TRUE.equals(bd.getAttribute(PRESERVE_TARGET_CLASS_ATTRIBUTE));
        }
        return false;
    }

    /**
     * 确定指定bean的原始目标类，如果可能的话，否则回退到常规的{@code getType}查找。
     * @param beanFactory 包含ConfigurableListableBeanFactory的容器
     * @param beanName bean的名称
     * @return 如果有任何，则返回存储在bean定义中的原始目标类
     * @since 4.2.3
     * @see org.springframework.beans.factory.BeanFactory#getType(String)
     */
    @Nullable
    public static Class<?> determineTargetClass(ConfigurableListableBeanFactory beanFactory, @Nullable String beanName) {
        if (beanName == null) {
            return null;
        }
        if (beanFactory.containsBeanDefinition(beanName)) {
            BeanDefinition bd = beanFactory.getMergedBeanDefinition(beanName);
            Class<?> targetClass = (Class<?>) bd.getAttribute(ORIGINAL_TARGET_CLASS_ATTRIBUTE);
            if (targetClass != null) {
                return targetClass;
            }
        }
        return beanFactory.getType(beanName);
    }

    /**
     * 尝试将指定的目标类暴露给指定的bean，如果可能的话。
     * @param beanFactory 包含的 ConfigurableListableBeanFactory
     * @param beanName bean的名称
     * @param targetClass 相应的目标类
     * @since 4.2.3
     */
    static void exposeTargetClass(ConfigurableListableBeanFactory beanFactory, @Nullable String beanName, Class<?> targetClass) {
        if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.getMergedBeanDefinition(beanName).setAttribute(ORIGINAL_TARGET_CLASS_ATTRIBUTE, targetClass);
        }
    }

    /**
     * 判断给定的 Bean 名称是否表示一个“原始实例”，根据 {@link AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX} 进行判断，跳过对该实例的任何代理尝试。
     * @param beanName Bean 的名称
     * @param beanClass 对应的 Bean 类
     * @since 5.1
     * @see AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX
     */
    static boolean isOriginalInstance(String beanName, Class<?> beanClass) {
        if (!StringUtils.hasLength(beanName) || beanName.length() != beanClass.getName().length() + AutowireCapableBeanFactory.ORIGINAL_INSTANCE_SUFFIX.length()) {
            return false;
        }
        return (beanName.startsWith(beanClass.getName()) && beanName.endsWith(AutowireCapableBeanFactory.ORIGINAL_INSTANCE_SUFFIX));
    }
}
