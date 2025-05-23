// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 您不得使用此文件除非符合许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明示或暗示。有关权限和限制的特定语言，
* 请参阅许可证。*/
package org.springframework.beans.factory.xml;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.w3c.dom.Element;
import org.springframework.beans.factory.config.FieldRetrievingFactoryBean;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPathFactoryBean;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;

/**
 * 用于 {@code util} 命名空间的 {@link NamespaceHandler}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class UtilNamespaceHandler extends NamespaceHandlerSupport {

    private static final String SCOPE_ATTRIBUTE = "scope";

    @Override
    public void init() {
        registerBeanDefinitionParser("constant", new ConstantBeanDefinitionParser());
        registerBeanDefinitionParser("property-path", new PropertyPathBeanDefinitionParser());
        registerBeanDefinitionParser("list", new ListBeanDefinitionParser());
        registerBeanDefinitionParser("set", new SetBeanDefinitionParser());
        registerBeanDefinitionParser("map", new MapBeanDefinitionParser());
        registerBeanDefinitionParser("properties", new PropertiesBeanDefinitionParser());
    }

    private static class ConstantBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return FieldRetrievingFactoryBean.class;
        }

        @Override
        protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
            String id = super.resolveId(element, definition, parserContext);
            if (!StringUtils.hasText(id)) {
                id = element.getAttribute("static-field");
            }
            return id;
        }
    }

    private static class PropertyPathBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return PropertyPathFactoryBean.class;
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            String path = element.getAttribute("path");
            if (!StringUtils.hasText(path)) {
                parserContext.getReaderContext().error("Attribute 'path' must not be empty", element);
                return;
            }
            int dotIndex = path.indexOf('.');
            if (dotIndex == -1) {
                parserContext.getReaderContext().error("Attribute 'path' must follow pattern 'beanName.propertyName'", element);
                return;
            }
            String beanName = path.substring(0, dotIndex);
            String propertyPath = path.substring(dotIndex + 1);
            builder.addPropertyValue("targetBeanName", beanName);
            builder.addPropertyValue("propertyPath", propertyPath);
        }

        @Override
        protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
            String id = super.resolveId(element, definition, parserContext);
            if (!StringUtils.hasText(id)) {
                id = element.getAttribute("path");
            }
            return id;
        }
    }

    private static class ListBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return ListFactoryBean.class;
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            List<Object> parsedList = parserContext.getDelegate().parseListElement(element, builder.getRawBeanDefinition());
            builder.addPropertyValue("sourceList", parsedList);
            String listClass = element.getAttribute("list-class");
            if (StringUtils.hasText(listClass)) {
                builder.addPropertyValue("targetListClass", listClass);
            }
            String scope = element.getAttribute(SCOPE_ATTRIBUTE);
            if (StringUtils.hasLength(scope)) {
                builder.setScope(scope);
            }
        }
    }

    private static class SetBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return SetFactoryBean.class;
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            Set<Object> parsedSet = parserContext.getDelegate().parseSetElement(element, builder.getRawBeanDefinition());
            builder.addPropertyValue("sourceSet", parsedSet);
            String setClass = element.getAttribute("set-class");
            if (StringUtils.hasText(setClass)) {
                builder.addPropertyValue("targetSetClass", setClass);
            }
            String scope = element.getAttribute(SCOPE_ATTRIBUTE);
            if (StringUtils.hasLength(scope)) {
                builder.setScope(scope);
            }
        }
    }

    private static class MapBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return MapFactoryBean.class;
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            Map<Object, Object> parsedMap = parserContext.getDelegate().parseMapElement(element, builder.getRawBeanDefinition());
            builder.addPropertyValue("sourceMap", parsedMap);
            String mapClass = element.getAttribute("map-class");
            if (StringUtils.hasText(mapClass)) {
                builder.addPropertyValue("targetMapClass", mapClass);
            }
            String scope = element.getAttribute(SCOPE_ATTRIBUTE);
            if (StringUtils.hasLength(scope)) {
                builder.setScope(scope);
            }
        }
    }

    private static class PropertiesBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return PropertiesFactoryBean.class;
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            Properties parsedProps = parserContext.getDelegate().parsePropsElement(element);
            builder.addPropertyValue("properties", parsedProps);
            String location = element.getAttribute("location");
            if (StringUtils.hasLength(location)) {
                location = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(location);
                String[] locations = StringUtils.commaDelimitedListToStringArray(location);
                builder.addPropertyValue("locations", locations);
            }
            builder.addPropertyValue("ignoreResourceNotFound", Boolean.valueOf(element.getAttribute("ignore-resource-not-found")));
            builder.addPropertyValue("localOverride", Boolean.valueOf(element.getAttribute("local-override")));
            String scope = element.getAttribute(SCOPE_ATTRIBUTE);
            if (StringUtils.hasLength(scope)) {
                builder.setScope(scope);
            }
        }
    }
}
