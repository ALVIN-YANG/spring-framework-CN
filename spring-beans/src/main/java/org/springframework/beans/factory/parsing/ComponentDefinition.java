// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，您不得使用此文件除非符合许可协议。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权性的保证。
* 请参阅许可协议了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.parsing;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;

/**
 * 接口描述了一组Bean定义（BeanDefinitions）和Bean引用（BeanReferences）的逻辑视图，这些视图在某些配置上下文中呈现。
 *
 * <p>随着可插拔自定义XML标签（如org.springframework.beans.factory.xml.NamespaceHandler）的引入，单个逻辑配置实体（在本例中为一个XML标签）现在可以创建多个Bean定义（BeanDefinitions）和运行时Bean引用（RuntimeBeanReferences），从而提供更简洁的配置和方便最终用户。因此，不再假设每个配置实体（例如XML标签）映射到一个Bean定义。对于希望提供Spring应用程序可视化或配置支持的工具供应商和其他用户来说，确保存在某种机制将BeanFactory中的Bean定义与配置数据关联起来，这对最终用户具有具体意义非常重要。因此，NamespaceHandler实现能够为每个正在配置的逻辑实体发布形式为ComponentDefinition的事件。第三方可以订阅这些事件，从而允许以用户为中心的Bean元数据视图。
 *
 * <p>每个ComponentDefinition都有一个source对象，该对象是配置特定的。在基于XML的配置中，这通常是包含用户提供的配置信息的org.w3c.dom.Node。此外，ComponentDefinition中包含的每个BeanDefinition都有自己的source对象，它可能指向不同的、更具体的配置数据集。除此之外，Bean元数据的各个部分（如org.springframework.beans.PropertyValue PropertyValues）也可能有自己的source对象，从而提供更详细的级别。source对象的提取通过SourceExtractor处理，可以根据需要进行自定义。
 *
 * <p>虽然通过getBeanReferences提供了对重要的Bean引用（BeanReferences）的直接访问，但工具可能希望检查所有Bean定义（BeanDefinitions）以收集完整的Bean引用集合。实现必须提供所有必需的Bean引用，这些引用用于验证整体逻辑实体的配置以及提供完整的用户配置可视化。预计某些Bean引用对验证或用户配置视图不重要，因此可以省略。工具可能希望显示通过提供的Bean定义源生的任何其他Bean引用，但这不是一个典型情况。
 *
 * <p>工具可以通过检查BeanDefinition.getRole角色标识符来确定包含的Bean定义（BeanDefinitions）的重要性。角色基本上是工具对配置提供者认为一个Bean定义对最终用户重要性的提示。预计工具将不会显示给定ComponentDefinition的所有Bean定义，而是根据角色进行过滤。工具可以选择使此过滤用户可配置。特别要注意的是BeanDefinition.ROLE_INFRASTRUCTURE角色标识符。具有此角色的Bean定义对最终用户完全不重要，仅用于内部实现原因。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see AbstractComponentDefinition
 * @see CompositeComponentDefinition
 * @see BeanComponentDefinition
 * @see ReaderEventListener#componentRegistered(ComponentDefinition)
 */
public interface ComponentDefinition extends BeanMetadataElement {

    /**
     * 获取此 {@code ComponentDefinition} 的用户可见名称。
     * <p>这应直接链接到给定上下文中此组件对应的配置数据。
     */
    String getName();

    /**
     * 返回对所描述组件的友好描述。
     * <p>鼓励实现返回与 {@code toString()} 相同的值。
     */
    String getDescription();

    /**
     * 返回构成此 {@code ComponentDefinition} 的已注册的 {@link BeanDefinition BeanDefinitions}。
     * 应注意，一个 {@code ComponentDefinition} 可能与其他的 {@link BeanDefinition BeanDefinitions} 通过 {@link BeanReference references} 相关联，
     * 但是这些 <strong>不包括</strong> 在内，因为它们可能不是立即可用的。重要 的 {@link BeanReference BeanReferences} 可从 {@link #getBeanReferences()} 获取。
     * @return BeanDefinitions 数组，如果没有则返回空数组
     */
    BeanDefinition[] getBeanDefinitions();

    /**
     * 返回表示此组件中所有相关内部Bean的{@link BeanDefinition BeanDefinitions}。
     * 在关联的{@link BeanDefinition BeanDefinitions}中可能存在其他内部Bean，
     * 但是这些Bean在验证或用户可视化方面不被认为是必需的。
     * @return BeanDefinitions数组，如果没有则返回空数组
     */
    BeanDefinition[] getInnerBeanDefinitions();

    /**
     * 返回被认为是与此 {@code ComponentDefinition} 重要的 {@link BeanReference BeanReferences} 集合。
     * 在关联的 {@link BeanDefinition BeanDefinitions} 中可能存在其他 {@link BeanReference BeanReferences}，
     * 然而，这些并不被认为是用于验证或用户可视化的必需项。
     * @return BeanReferences 的数组，如果没有则返回空数组
     */
    BeanReference[] getBeanReferences();
}
