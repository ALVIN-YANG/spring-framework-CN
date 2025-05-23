// 翻译完成 glm-4-flash
/** 版权所有 2002-2014 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按 "原样" 分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 这是一个便捷的基类，用于存在一个元素上的属性名称与正在配置的 {@link Class} 上的属性名称之间一对一映射的情况。
 *
 * <p>当你想从一个相对简单的自定义 XML 元素创建一个单独的 bean 定义时，可以扩展这个解析器类。生成的 {@code BeanDefinition} 将自动注册到相关的
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}。
 *
 * <p>以下例子将有助于理解这个特定解析器的使用。考虑以下类定义：
 *
 * <pre class="code">public class SimpleCache implements Cache {
 *
 *     public void setName(String name) {...}
 *     public void setTimeout(int timeout) {...}
 *     public void setEvictionPolicy(EvictionPolicy policy) {...}
 *
 *     // 为了清晰起见，省略了剩余的类定义...
 * }</pre>
 *
 * <p>然后假设以下 XML 标签已经定义，以便于配置上述类的实例；
 *
 * <pre class="code">&lt;caching:cache name="..." timeout="..." eviction-policy="..."/&gt;</pre>
 *
 * <p>负责解析上述 XML 标签到实际的 {@code SimpleCache} bean 定义的开发者需要做的是以下内容：
 *
 * <pre class="code">public class SimpleCacheBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {
 *
 *     protected Class getBeanClass(Element element) {
 *         return SimpleCache.class;
 *     }
 * }</pre>
 *
 * <p>请注意，`AbstractSimpleBeanDefinitionParser` 限制于填充创建的 bean 定义中的属性值。如果你想要从提供的 XML 元素中解析构造函数参数和嵌套元素，那么你必须实现
 * `#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.w3c.dom.Element)` 方法自行进行解析，或者（更可能）直接子类化 `AbstractSingleBeanDefinitionParser` 或 `AbstractBeanDefinitionParser` 类。
 *
 * <p>将 `SimpleCacheBeanDefinitionParser` 注册到 Spring XML 解析基础设施的过程在 Spring 框架参考文档中有描述（在附录之一）。
 *
 * <p>为了看到这个解析器在实际中的应用，可以查看 `@link org.springframework.beans.factory.xml.UtilNamespaceHandler.PropertiesBeanDefinitionParser` 的源代码；细致的读者会立即注意到实现中几乎没有代码。`PropertiesBeanDefinitionParser` 从类似下面的 XML 元素中填充了一个
 * `@link org.springframework.beans.factory.config.PropertiesFactoryBean`：
 *
 * <pre class="code">&lt;util:properties location="jdbc.properties"/&gt;</pre>
 *
 * <p>细致的读者会注意到，`<util:properties/>` 元素上的唯一属性与 `PropertiesFactoryBean` 的 `@link org.springframework.beans.factory.config.PropertiesFactoryBean#setLocation(org.springframework.core.io.Resource)` 方法名称匹配（下面展示的一般用法适用于任意数量的属性）。`PropertiesBeanDefinitionParser` 实际上只需要提供一个 `@link #getBeanClass(org.w3c.dom.Element)` 方法的实现来返回 `PropertiesFactoryBean` 类型。
 *
 * @author Rob Harrop
 * @author Rick Evans
 * @author Juergen Hoeller
 * @since 2.0
 * @see Conventions#attributeNameToPropertyName(String)
 */
public abstract class AbstractSimpleBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /**
     * 解析提供的 {@link Element} 并根据需要填充提供的
     * {@link BeanDefinitionBuilder}。
     * <p>此实现将提供的元素上存在的任何属性映射到
     * {@link org.springframework.beans.PropertyValue} 实例，并通过
     * {@link BeanDefinitionBuilder#addPropertyValue(String, Object)} 将它们
     * 添加到
     * {@link org.springframework.beans.factory.config.BeanDefinition} 构建器中。
     * <p>使用 {@link #extractPropertyName(String)} 方法来协调属性名称与 JavaBean 属性名称的关系。
     * @param element 正在解析的 XML 元素
     * @param builder 用于定义的 {@code BeanDefinition}
     * @see #extractPropertyName(String)
     */
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        NamedNodeMap attributes = element.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            if (isEligibleAttribute(attribute, parserContext)) {
                String propertyName = extractPropertyName(attribute.getLocalName());
                Assert.state(StringUtils.hasText(propertyName), "Illegal property name returned from 'extractPropertyName(String)': cannot be null or empty.");
                builder.addPropertyValue(propertyName, attribute.getValue());
            }
        }
        postProcess(builder, element);
    }

    /**
     * 判断给定的属性是否适合转换为相应的bean属性值。
     * 默认实现认为任何属性都适合，除了“id”属性和命名空间声明属性。
     * @param attribute 要检查的XML属性
     * @param parserContext 解析上下文对象
     * @see #isEligibleAttribute(String)
     */
    protected boolean isEligibleAttribute(Attr attribute, ParserContext parserContext) {
        String fullName = attribute.getName();
        return (!fullName.equals("xmlns") && !fullName.startsWith("xmlns:") && isEligibleAttribute(parserContext.getDelegate().getLocalName(attribute)));
    }

    /**
     * 判断给定的属性是否有资格被转换为相应的 bean 属性值。
     * 默认实现认为任何属性都是有资格的，除了 "id" 属性。
     * @param attributeName 从正在解析的 XML 元素中直接获取的属性名称（永远不会为 {@code null}）
     */
    protected boolean isEligibleAttribute(String attributeName) {
        return !ID_ATTRIBUTE.equals(attributeName);
    }

    /**
     * 从提供的属性名称中提取 JavaBean 属性名称。
     * <p>默认实现使用
     * {@link Conventions#attributeNameToPropertyName(String)}
     * 方法来完成提取。
     * <p>返回的名称必须遵守标准的 JavaBean 属性名称约定。例如，对于一个具有setter方法
     * '{@code setBingoHallFavourite(String)}' 的类，返回的名称最好是 '{@code bingoHallFavourite}'（具有该确切的字母大小写）。
     * @param attributeName 从正在解析的 XML 元素中直接获取的属性名称（永远不会为 {@code null}）
     * @return 提取的 JavaBean 属性名称（永远不会为 {@code null}）
     */
    protected String extractPropertyName(String attributeName) {
        return Conventions.attributeNameToPropertyName(attributeName);
    }

    /**
     * 钩子方法，派生类可以实现它来在解析完成后检查/修改bean定义。
     * <p>默认实现不执行任何操作。
     * @param beanDefinition 正在构建的解析完成（可能完全定义）的bean定义
     * @param element 产生bean定义元数据的XML元素
     */
    protected void postProcess(BeanDefinitionBuilder beanDefinition, Element element) {
    }
}
