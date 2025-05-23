// 翻译完成 glm-4-flash
/** 版权所有 2002-2007 原作者或作者。
*
* 根据Apache许可证版本2.0（以下简称“许可证”）授权；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * 用于生成bean定义的bean名称的策略接口。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 */
public interface BeanNameGenerator {

    /**
     * 为给定的bean定义生成一个bean名称。
     * @param definition 需要生成名称的bean定义
     * @param registry 应该将给定定义注册到的bean定义注册器
     * @return 生成的bean名称
     */
    String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);
}
