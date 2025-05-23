// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 您不得使用此文件，除非遵守许可证。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，
* 在许可证下分发的软件按照“原样”分发，
* 不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.lang.Nullable;

/**
 * 由 {@link DefaultBeanDefinitionDocumentReader} 使用的基接口，用于处理 Spring XML 配置文件中的自定义命名空间。
 *
 * <p>实现类应返回自定义顶级标签的 {@link BeanDefinitionParser} 接口的实现，以及自定义嵌套标签的 {@link BeanDefinitionDecorator} 接口的实现。
 *
 * <p>当解析器遇到位于 `<beans>` 标签下的自定义标签时，将调用 {@link #parse} 方法；当遇到位于 `<bean>` 标签下的自定义标签时，将调用 {@link #decorate} 方法。
 *
 * <p>编写自定义元素扩展的开发者通常不会直接实现此接口，而是使用提供的 {@link NamespaceHandlerSupport} 类。
 *
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 2.0
 * @see DefaultBeanDefinitionDocumentReader
 * @see NamespaceHandlerResolver
 */
public interface NamespaceHandler {

    /**
     * 由 {@link DefaultBeanDefinitionDocumentReader} 在构造后但在解析任何自定义元素之前调用。
     * @see NamespaceHandlerSupport#registerBeanDefinitionParser(String, BeanDefinitionParser)
     */
    void init();

    /**
     * 解析指定的 {@link Element}，并将产生的任何 {@link BeanDefinition BeanDefinitions} 注册到嵌入在提供的 {@link ParserContext} 中的 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}。
     * 实现类如果希望在（例如）一个 `<property>` 标签内部使用，应该返回解析阶段产生的首选 {@code BeanDefinition}。
     * 如果实现类在嵌套场景中 <strong>不会</strong> 使用，它们可以返回 {@code null}。
     * @param element 要解析为一个或多个 {@code BeanDefinitions} 的元素
     * @param parserContext 封装解析过程当前状态的对象
     * @return 首选的 {@code BeanDefinition}（如上所述，可以为 {@code null}）
     */
    @Nullable
    BeanDefinition parse(Element element, ParserContext parserContext);

    /**
     * 解析指定的 {@link Node} 并装饰提供的
     * {@link BeanDefinitionHolder}，返回装饰后的定义。
     * <p>根据是否解析自定义属性或元素，`Node` 可以为 `org.w3c.dom.Attr` 或 `Element`。
     * <p>实现者可以选择返回一个全新的定义，这将替换结果中的原始定义，在
     * `org.springframework.beans.factory.BeanFactory` 中使用。
     * <p>提供的 `ParserContext` 可以用来注册支持主要定义所需的任何额外bean。
     * @param source 要解析的源元素或属性
     * @param definition 当前bean定义
     * @param parserContext 封装解析过程当前状态的对象
     * @return 装饰后的定义（将被注册到BeanFactory中），或者如果不需要装饰，则简单地返回原始bean定义。
     * 从严格意义上讲，`null` 值是无效的，但将被宽容地处理，就像返回原始bean定义的情况一样。
     */
    @Nullable
    BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder definition, ParserContext parserContext);
}
