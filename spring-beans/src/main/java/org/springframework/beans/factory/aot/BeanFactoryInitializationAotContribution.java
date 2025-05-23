// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非适用法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途适用性和非侵权的保证。
* 请参阅许可证了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.factory.aot;

import org.springframework.aot.generate.GenerationContext;

/**
 * 来自于一个用于初始化bean工厂的 {@link BeanFactoryInitializationAotProcessor} 的AOT（Ahead-of-Time）贡献。
 *
 * <p>注意：实现此接口的bean在AOT处理期间不会生成注册方法，除非它们还实现了 {@link org.springframework.beans.factory.aot.BeanRegistrationExcludeFilter}。
 *
 * @author Phillip Webb
 * @since 6.0
 * @see BeanFactoryInitializationAotProcessor
 */
@FunctionalInterface
public interface BeanFactoryInitializationAotContribution {

    /**
     * 将此贡献应用于给定的{@link BeanFactoryInitializationCode}。
     * @param generationContext 活跃的生成上下文
     * @param beanFactoryInitializationCode Bean工厂初始化代码
     */
    void applyTo(GenerationContext generationContext, BeanFactoryInitializationCode beanFactoryInitializationCode);
}
