// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 您不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明示或暗示。有关许可权限和限制的具体语言，
* 请参阅许可证。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * 默认实现 {@link BeanNameGenerator} 接口，委托给
 * {@link BeanDefinitionReaderUtils#generateBeanName(BeanDefinition, BeanDefinitionRegistry)}。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 */
public class DefaultBeanNameGenerator implements BeanNameGenerator {

    /**
     * 一个方便的常量，用于默认的 {@code DefaultBeanNameGenerator} 实例，
     * 如用于 {@link AbstractBeanDefinitionReader} 的设置。
     * @since 5.2
     */
    public static final DefaultBeanNameGenerator INSTANCE = new DefaultBeanNameGenerator();

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        return BeanDefinitionReaderUtils.generateBeanName(definition, registry);
    }
}
