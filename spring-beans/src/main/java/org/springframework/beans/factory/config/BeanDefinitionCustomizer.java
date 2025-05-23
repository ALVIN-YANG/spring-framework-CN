// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 您除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明示或暗示。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.config;

/**
 * 用于自定义给定bean定义的回调。
 * 专为与lambda表达式或方法引用一起使用而设计。
 *
 * @author Juergen Hoeller
 * @since 5.0
 * @see org.springframework.beans.factory.support.BeanDefinitionBuilder#applyCustomizers
 */
@FunctionalInterface
public interface BeanDefinitionCustomizer {

    /**
     * 自定义给定的Bean定义。
     */
    void customize(BeanDefinition bd);
}
