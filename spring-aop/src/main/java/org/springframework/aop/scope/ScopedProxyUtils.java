// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”），除非法律要求或书面同意，否则您不得使用此文件，除非符合许可证。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可证的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.aop.scope;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于创建作用域代理的实用工具类。
 *
 * <p>由ScopedProxyBeanDefinitionDecorator和ClassPathBeanDefinitionScanner使用。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 2.5
 */
public abstract class ScopedProxyUtils {

    private static final String TARGET_NAME_PREFIX = "scopedTarget.";

    private static final int TARGET_NAME_PREFIX_LENGTH = TARGET_NAME_PREFIX.length();

    /**
     * 为提供的目标bean生成一个作用域代理，将目标bean注册为一个内部名称，并在作用域代理上设置'targetBeanName'。
     * @param definition 原始bean定义
     * @param registry bean定义注册表
     * @param proxyTargetClass 是否创建目标类代理
     * @return 作用域代理定义
     * @see #getTargetBeanName(String)
     * @see #getOriginalBeanName(String)
     */
    public static BeanDefinitionHolder createScopedProxy(BeanDefinitionHolder definition, BeanDefinitionRegistry registry, boolean proxyTargetClass) {
        String originalBeanName = definition.getBeanName();
        BeanDefinition targetDefinition = definition.getBeanDefinition();
        String targetBeanName = getTargetBeanName(originalBeanName);
        // 为原始bean名称创建一个作用域代理定义。
        // 将目标 Bean 隐藏在内部目标定义中。
        RootBeanDefinition proxyDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);
        proxyDefinition.setDecoratedDefinition(new BeanDefinitionHolder(targetDefinition, targetBeanName));
        proxyDefinition.setOriginatingBeanDefinition(targetDefinition);
        proxyDefinition.setSource(definition.getSource());
        proxyDefinition.setRole(targetDefinition.getRole());
        proxyDefinition.getPropertyValues().add("targetBeanName", targetBeanName);
        if (proxyTargetClass) {
            targetDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
            // ScopedProxyFactoryBean的"proxyTargetClass"默认值为TRUE，因此我们在这里不需要显式设置它。
        } else {
            proxyDefinition.getPropertyValues().add("proxyTargetClass", Boolean.FALSE);
        }
        // 复制原始bean定义的自动装配设置。
        proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
        proxyDefinition.setPrimary(targetDefinition.isPrimary());
        if (targetDefinition instanceof AbstractBeanDefinition abd) {
            proxyDefinition.copyQualifiersFrom(abd);
        }
        // 目标Bean应该被忽略，以便使用作用域代理。
        targetDefinition.setAutowireCandidate(false);
        targetDefinition.setPrimary(false);
        // 将目标bean注册为工厂中独立的bean。
        registry.registerBeanDefinition(targetBeanName, targetDefinition);
        // 返回作用域代理定义作为主要bean定义
        // （可能是一个内部Bean）。
        return new BeanDefinitionHolder(proxyDefinition, originalBeanName, definition.getAliases());
    }

    /**
     * 生成在作用域代理中用于引用目标bean的bean名称。
     * @param originalBeanName bean的原名称
     * @return 用于引用目标bean的生成的bean
     * @see #getOriginalBeanName(String)
     */
    public static String getTargetBeanName(String originalBeanName) {
        return TARGET_NAME_PREFIX + originalBeanName;
    }

    /**
     * 获取提供的目标Bean名称（由# getTargetBeanName方法提供）的原生Bean名称。
     * @param targetBeanName 作用域代理的目标Bean名称
     * @return 原生Bean名称
     * @throws IllegalArgumentException 如果提供的Bean名称不指向作用域代理的目标
     * @since 5.1.10
     * @see #getTargetBeanName(String)
     * @see #isScopedTarget(String)
     */
    public static String getOriginalBeanName(@Nullable String targetBeanName) {
        Assert.isTrue(isScopedTarget(targetBeanName), () -> "bean name '" + targetBeanName + "' does not refer to the target of a scoped proxy");
        return targetBeanName.substring(TARGET_NAME_PREFIX_LENGTH);
    }

    /**
     * 判断 {@code beanName} 是否是引用了目标 bean 的作用域代理中的 bean 名称。
     * @since 4.1.4
     */
    public static boolean isScopedTarget(@Nullable String beanName) {
        return (beanName != null && beanName.startsWith(TARGET_NAME_PREFIX));
    }
}
