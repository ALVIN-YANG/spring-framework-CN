// 翻译完成 glm-4-flash
/*版权所有 2002-2021 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;

/**
 * 用于 `aspectj-autoproxy` 标签的 `BeanDefinitionParser`，
 * 允许自动应用在 `org.springframework.beans.factory.BeanFactory` 中找到的
 * @AspectJ风格的方面。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
class AspectJAutoProxyBeanDefinitionParser implements BeanDefinitionParser {

    @Override
    @Nullable
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);
        extendBeanDefinition(element, parserContext);
        return null;
    }

    private void extendBeanDefinition(Element element, ParserContext parserContext) {
        BeanDefinition beanDef = parserContext.getRegistry().getBeanDefinition(AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
        if (element.hasChildNodes()) {
            addIncludePatterns(element, parserContext, beanDef);
        }
    }

    private void addIncludePatterns(Element element, ParserContext parserContext, BeanDefinition beanDef) {
        ManagedList<TypedStringValue> includePatterns = new ManagedList<>();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element includeElement) {
                TypedStringValue valueHolder = new TypedStringValue(includeElement.getAttribute("name"));
                valueHolder.setSource(parserContext.extractSource(includeElement));
                includePatterns.add(valueHolder);
            }
        }
        if (!includePatterns.isEmpty()) {
            includePatterns.setSource(parserContext.extractSource(element));
            beanDef.getPropertyValues().add("includePatterns", includePatterns);
        }
    }
}
