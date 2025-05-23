// 翻译完成 glm-4-flash
/** 版权所有 2002-2011 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议，了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.lang.Nullable;

/**
 * 由 {@link DefaultBeanDefinitionDocumentReader} 使用的接口，用于处理自定义、顶级（直接位于 {@code <beans/>} 标签下）的标签。
 *
 * <p>实现类可以自由地将自定义标签中的元数据转换为所需的任意数量的 {@link BeanDefinition BeanDefinitions}。
 *
 * <p>解析器从与自定义标签所在命名空间关联的 {@link NamespaceHandler} 中定位一个 {@link BeanDefinitionParser}。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see AbstractBeanDefinitionParser
 */
public interface BeanDefinitionParser {

    /**
     * 解析指定的 {@link Element}，并将解析结果 {@link BeanDefinition BeanDefinition(s)} 注册到由提供的 {@link ParserContext} 包含的
     * {@link org.springframework.beans.factory.xml.ParserContext#getRegistry() BeanDefinitionRegistry}。
     * <p>实现类必须返回解析产生的首要 {@link BeanDefinition}，如果它们将用于嵌套方式（例如作为 <code><property/></code> 标签内的内部标签）。如果实现类将
     * <strong>不</strong> 用于嵌套方式，则可以返回 {@code null}。
     * @param element 要解析成一个或多个 {@link BeanDefinition BeanDefinitions} 的元素
     * @param parserContext 封装当前解析过程状态的对象；提供对 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry} 的访问
     * @return 首要的 {@link BeanDefinition}
     */
    @Nullable
    BeanDefinition parse(Element element, ParserContext parserContext);
}
