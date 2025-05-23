// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）
* 您可能仅在使用许可证的情况下使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，
* 在许可证下分发的软件按“原样”分发，
* 不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.factory.aot;

import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.MethodReference;

/**
 * 可用于配置将生成以执行bean工厂初始化的代码的接口。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanFactoryInitializationAotContribution
 */
public interface BeanFactoryInitializationCode {

    /**
     * 用于引用Bean工厂的推荐变量名称。
     */
    String BEAN_FACTORY_VARIABLE = "beanFactory";

    /**
     * 获取初始化代码使用的 {@link GeneratedMethods}。
     * @return 返回生成的方法
     */
    GeneratedMethods getMethods();

    /**
     * 添加一个初始化方法调用。初始化方法可以使用灵活的签名，可以使用以下任一选项：
     * <ul>
     * <li>使用 {@code DefaultListableBeanFactory} 或 {@code ConfigurableListableBeanFactory} 来使用 Bean 工厂。</li>
     * <li>使用 {@code ConfigurableEnvironment} 或 {@code Environment} 来访问环境。</li>
     * <li>使用 {@code ResourceLoader} 来加载资源。</li>
     * </ul>
     * @param methodReference 调用的初始化方法的引用。
     */
    void addInitializer(MethodReference methodReference);
}
