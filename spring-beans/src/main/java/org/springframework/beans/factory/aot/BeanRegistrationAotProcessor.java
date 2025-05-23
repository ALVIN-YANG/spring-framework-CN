// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非适用法律要求或经书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、 merchantability 或特定用途的适用性。
* 请参阅许可证以了解具体规定许可权限和限制的条款。*/
package org.springframework.beans.factory.aot;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.lang.Nullable;

/**
 * AOT 处理器，通过处理
 * {@link RegisteredBean} 实例来贡献 bean 注册。
 *
 * <p>实现 {@code BeanRegistrationAotProcessor} 的类可以通过
 * {@value AotServices#FACTORIES_RESOURCE_LOCATION} 资源或作为一个 bean 进行注册。
 *
 * <p>在已注册的 bean 上使用此接口将导致在 AOT 处理期间
 * 初始化该 bean 及其所有依赖项。我们通常建议仅将此接口与
 * 例如 {@link BeanPostProcessor} 这样的基础设施 bean 一起使用，
 * 这些 bean 依赖性有限且已在 bean 工厂生命周期早期初始化。
 * 如果使用工厂方法注册此类 bean，请确保将其定义为
 * {@code static}，这样其封装类就不必被初始化。
 *
 * <p>AOT 处理器通过优化安排（通常是在生成的代码中）替换其通常的运行时行为。
 * 因此，实现此接口的组件默认不进行贡献。如果实现此接口的组件
 * 在运行时仍需要被调用，可以通过覆盖
 * {@link #isBeanExcludedFromAotProcessing} 来实现。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanRegistrationAotContribution
 */
@FunctionalInterface
public interface BeanRegistrationAotProcessor {

    /**
     *  预处理给定的 {@link RegisteredBean} 实例，并返回一个贡献或 {@code null}。
     * 	<p>
     * 	处理器可以自由地使用任何它们喜欢的技术来分析给定的实例。通常使用反射来找到用于贡献的字段或方法。贡献通常生成源代码或资源文件，这些文件可以在 AOT 优化的应用程序运行时使用。
     * 	<p>
     * 	如果给定的实例对处理器来说不相关，它应该返回一个 {@code null} 贡献。
     * 	@param registeredBean 要处理的注册 Bean
     * 	@return 一个 {@link BeanRegistrationAotContribution} 或 {@code null}
     */
    @Nullable
    BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean);

    /**
     * 返回与该处理器关联的 bean 实例是否应该从 AOT 处理中排除。默认情况下，此方法返回 {@code true} 以自动排除 bean，如果应该写入定义，则此方法可以重写以返回 {@code true}。
     * @return 是否应该排除 bean 进行 AOT 处理
     * @see BeanRegistrationExcludeFilter
     */
    default boolean isBeanExcludedFromAotProcessing() {
        return true;
    }
}
