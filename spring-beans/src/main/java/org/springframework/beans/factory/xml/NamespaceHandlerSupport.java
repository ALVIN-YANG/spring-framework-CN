// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.xml;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.lang.Nullable;

/**
 * 实现自定义 {@link NamespaceHandler NamespaceHandlers} 的支持类。
 * 单个 {@link Node 节点} 的解析和装饰分别通过 {@link BeanDefinitionParser} 和 {@link BeanDefinitionDecorator} 策略接口进行。
 *
 * <p>提供了注册处理特定元素的 {@link BeanDefinitionParser} 或 {@link BeanDefinitionDecorator} 的方法：`#registerBeanDefinitionParser` 和 `#registerBeanDefinitionDecorator`。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see #registerBeanDefinitionParser(String, BeanDefinitionParser)
 * @see #registerBeanDefinitionDecorator(String, BeanDefinitionDecorator)
 */
public abstract class NamespaceHandlerSupport implements NamespaceHandler {

    /**
     * 存储按它们处理的 {@link Element Elements} 的本地名称键控的 {@link BeanDefinitionParser} 实现类。
     */
    private final Map<String, BeanDefinitionParser> parsers = new HashMap<>();

    /**
     * 存储根据它们处理的 {@link Element 元素} 的本地名称来键控的 {@link BeanDefinitionDecorator} 实现。
     */
    private final Map<String, BeanDefinitionDecorator> decorators = new HashMap<>();

    /**
     * 存储以它们处理的 {@link BeanDefinitionDecorator} 实现的键，键为它们处理的局部名称的 {@link Attr Attrs}。
     */
    private final Map<String, BeanDefinitionDecorator> attributeDecorators = new HashMap<>();

    /**
     * 解析提供的 {@link Element}，通过委托给为该 {@link Element} 注册的 {@link BeanDefinitionParser} 来实现。
     */
    @Override
    @Nullable
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionParser parser = findParserForElement(element, parserContext);
        return (parser != null ? parser.parse(element, parserContext) : null);
    }

    /**
     * 从注册实现中通过提供的{@link Element}的本地名称查找相应的{@link BeanDefinitionParser}。
     */
    @Nullable
    private BeanDefinitionParser findParserForElement(Element element, ParserContext parserContext) {
        String localName = parserContext.getDelegate().getLocalName(element);
        BeanDefinitionParser parser = this.parsers.get(localName);
        if (parser == null) {
            parserContext.getReaderContext().fatal("Cannot locate BeanDefinitionParser for element [" + localName + "]", element);
        }
        return parser;
    }

    /**
     * 通过委派到已注册用于处理该 {@link Node} 的 {@link BeanDefinitionDecorator} 来装饰提供的 {@link Node}。
     */
    @Override
    @Nullable
    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
        BeanDefinitionDecorator decorator = findDecoratorForNode(node, parserContext);
        return (decorator != null ? decorator.decorate(node, definition, parserContext) : null);
    }

    /**
     * 从注册实现中根据提供的{@link Node}的本地名称定位到{@link BeanDefinitionParser}。支持{@link Element 元素}和{@link Attr 属性}。
     */
    @Nullable
    private BeanDefinitionDecorator findDecoratorForNode(Node node, ParserContext parserContext) {
        BeanDefinitionDecorator decorator = null;
        String localName = parserContext.getDelegate().getLocalName(node);
        if (node instanceof Element) {
            decorator = this.decorators.get(localName);
        } else if (node instanceof Attr) {
            decorator = this.attributeDecorators.get(localName);
        } else {
            parserContext.getReaderContext().fatal("Cannot decorate based on Nodes of type [" + node.getClass().getName() + "]", node);
        }
        if (decorator == null) {
            parserContext.getReaderContext().fatal("Cannot locate BeanDefinitionDecorator for " + (node instanceof Element ? "element" : "attribute") + " [" + localName + "]", node);
        }
        return decorator;
    }

    /**
     * 子类可以调用此方法来注册提供的{@link BeanDefinitionParser}以处理指定的元素。元素名称是本地（非命名空间限定）名称。
     */
    protected final void registerBeanDefinitionParser(String elementName, BeanDefinitionParser parser) {
        this.parsers.put(elementName, parser);
    }

    /**
     * 子类可以调用此方法来注册提供的 {@link BeanDefinitionDecorator} 以处理指定的元素。元素名称是本地（非命名空间限定）名称。
     */
    protected final void registerBeanDefinitionDecorator(String elementName, BeanDefinitionDecorator dec) {
        this.decorators.put(elementName, dec);
    }

    /**
     * 子类可以调用此方法来注册一个提供的 {@link BeanDefinitionDecorator}，用于处理指定的属性。属性名是本地（非命名空间限定）的名称。
     */
    protected final void registerBeanDefinitionDecoratorForAttribute(String attrName, BeanDefinitionDecorator dec) {
        this.attributeDecorators.put(attrName, dec);
    }
}
