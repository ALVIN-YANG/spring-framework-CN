// 翻译完成 glm-4-flash
/** 版权所有 2002-2010 原作者或作者。
*
* 根据 Apache License，版本 2.0（以下简称“许可证”），除非法律法规要求或经书面同意，否则您不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律法规要求或经书面同意，否则在许可证下分发的软件按照“原样”分发，不提供任何形式的明示或暗示保证。
* 请参阅许可证以了解具体规定许可权和限制。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

/**
 * 对标准 {@link BeanFactoryPostProcessor} SPI 的扩展，允许在常规的 BeanFactoryPostProcessor 检测启动之前注册更多的 Bean 定义。特别是，BeanDefinitionRegistryPostProcessor 可以注册更多的 Bean 定义，这些定义反过来定义了 BeanFactoryPostProcessor 实例。
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

    /**
     * 在应用程序上下文的标准初始化之后修改其内部bean定义注册表。所有常规bean定义都将被加载，但尚未实例化任何bean。这允许在下一个后处理阶段开始之前添加更多的bean定义。
     * @param registry 应用程序上下文使用的bean定义注册表
     * @throws org.springframework.beans.BeansException 如果发生错误
     */
    void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;
}
