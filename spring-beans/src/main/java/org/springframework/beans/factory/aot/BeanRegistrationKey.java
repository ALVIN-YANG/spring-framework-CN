// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.aot;

/**
 * 记录类，用于保存在Bean工厂中注册的bean的关键信息。
 *
 * @param beanName 已注册bean的名称
 * @param beanClass 已注册bean的类型
 * @author Brian Clozel
 * @since 6.0.8
 */
record BeanRegistrationKey(String beanName, Class<?> beanClass) {
}
