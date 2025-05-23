// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或原作者。

根据Apache License, Version 2.0（“许可证”），除非根据法律规定或书面同意，否则您不得使用此文件，除非遵守许可证。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可协议中规定的权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.config;

import org.w3c.dom.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;

/**
 * {@link BeanDefinitionParser} 负责解析
 * {@code <aop:spring-configured/>} 标签。
 *
 * <p><b>注意：</b>这实际上与 Spring 2.5 的
 * {@link org.springframework.context.config.SpringConfiguredBeanDefinitionParser}
 * 相同，用于解析 {@code <context:spring-configured/>} 标签，在此处进行镜像以保持与
 * Spring 2.0 的 {@code <aop:spring-configured/>} 标签的兼容性（避免直接依赖 context 包）。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
class SpringConfiguredBeanDefinitionParser implements BeanDefinitionParser {

    /**
     * 内部管理的Bean配置器切面的Bean名称。
     */
    public static final String BEAN_CONFIGURER_ASPECT_BEAN_NAME = "org.springframework.context.config.internalBeanConfigurerAspect";

    private static final String BEAN_CONFIGURER_ASPECT_CLASS_NAME = "org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        if (!parserContext.getRegistry().containsBeanDefinition(BEAN_CONFIGURER_ASPECT_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition();
            def.setBeanClassName(BEAN_CONFIGURER_ASPECT_CLASS_NAME);
            def.setFactoryMethodName("aspectOf");
            def.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            def.setSource(parserContext.extractSource(element));
            parserContext.registerBeanComponent(new BeanComponentDefinition(def, BEAN_CONFIGURER_ASPECT_BEAN_NAME));
        }
        return null;
    }
}
