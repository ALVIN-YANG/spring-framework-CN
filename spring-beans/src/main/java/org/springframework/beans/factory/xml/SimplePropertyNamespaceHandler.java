// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.Conventions;
import org.springframework.lang.Nullable;

/**
 * 简单的 {@code NamespaceHandler} 实现，将自定义属性直接映射到 Bean 属性。需要注意的是，这个 {@code NamespaceHandler} 没有对应的 schema，因为没有方法预先知道所有可能的属性名称。
 *
 * <p>以下展示了如何使用这个 {@code NamespaceHandler} 的示例：
 *
 * <pre class="code">
 * &lt;bean id=&quot;rob&quot; class=&quot;..TestBean&quot; p:name=&quot;Rob Harrop&quot; p:spouse-ref=&quot;sally&quot;/&gt;</pre>
 *
 * 在这里，'{@code p:name}' 直接对应于类 '{@code TestBean}' 上的 '{@code name}' 属性。'{@code p:spouse-ref}' 属性对应于 '{@code spouse}' 属性，而不是具体的值，它包含将被注入该属性的 Bean 的名称。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class SimplePropertyNamespaceHandler implements NamespaceHandler {

    private static final String REF_SUFFIX = "-ref";

    @Override
    public void init() {
    }

    @Override
    @Nullable
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        parserContext.getReaderContext().error("Class [" + getClass().getName() + "] does not support custom elements.", element);
        return null;
    }

    @Override
    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
        if (node instanceof Attr attr) {
            String propertyName = parserContext.getDelegate().getLocalName(attr);
            String propertyValue = attr.getValue();
            MutablePropertyValues pvs = definition.getBeanDefinition().getPropertyValues();
            if (pvs.contains(propertyName)) {
                parserContext.getReaderContext().error("Property '" + propertyName + "' is already defined using " + "both <property> and inline syntax. Only one approach may be used per property.", attr);
            }
            if (propertyName.endsWith(REF_SUFFIX)) {
                propertyName = propertyName.substring(0, propertyName.length() - REF_SUFFIX.length());
                pvs.add(Conventions.attributeNameToPropertyName(propertyName), new RuntimeBeanReference(propertyValue));
            } else {
                pvs.add(Conventions.attributeNameToPropertyName(propertyName), propertyValue);
            }
        }
        return definition;
    }
}
