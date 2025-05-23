// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.aot;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;

/**
 *  一个AOT（Ahead-of-Time）处理器，通过处理{@link ConfigurableListableBeanFactory}实例来提供bean工厂初始化的贡献。
 *
 * <p>{@code BeanFactoryInitializationAotProcessor}实现可以注册在{@value AotServices#FACTORIES_RESOURCE_LOCATION}资源中，或者作为一个bean。
 *
 * <p>在注册的bean上使用此接口将导致bean及其所有依赖项在AOT处理期间被初始化。我们通常建议仅将此接口与具有有限依赖项并且在bean工厂生命周期早期已经初始化的基础设施bean（如{@link BeanFactoryPostProcessor}）一起使用。如果使用工厂方法注册此类bean，请确保将其声明为{@code static}，以便其封装类无需初始化。
 *
 * <p>实现此接口的组件不会提供贡献。
 *
 * @作者 Phillip Webb
 * @作者 Stephane Nicoll
 * @since 6.0
 * @see BeanFactoryInitializationAotContribution
 */
@FunctionalInterface
public interface BeanFactoryInitializationAotProcessor {

    /**
     * 预处理给定的 {@link ConfigurableListableBeanFactory} 实例，并返回一个贡献或 {@code null}。
     * <p>处理器可以自由使用任何它们喜欢的技术来分析给定的 bean 工厂。最常见的是使用反射来找到用于贡献的字段或方法。贡献通常生成源代码或资源文件，这些文件可以在 AOT 优化的应用程序运行时使用。
     * <p>如果给定的 bean 工厂不包含与处理器相关的任何内容，则此方法应返回一个 {@code null} 贡献。
     * @param beanFactory 要处理的 bean 工厂
     * @return 一个 {@link BeanFactoryInitializationAotContribution} 或 {@code null}
     */
    @Nullable
    BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory);
}
