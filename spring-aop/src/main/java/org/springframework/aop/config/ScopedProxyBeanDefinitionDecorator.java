// 翻译完成 glm-4-flash
/** 版权所有 2002-2021，原作者或原作者们。
*
* 根据 Apache 许可证 2.0 版（以下简称“许可证”）进行许可；
* 您除非遵守许可证，否则不得使用此文件。
* 您可以在以下网址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”基础进行分发，
* 不提供任何形式的质量保证或条件，无论是明示的或暗示的。
* 请参阅许可证，了解具体的管理权限和限制。*/
package org.springframework.aop.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;

/**
 * /**
 *  {@link BeanDefinitionDecorator} 负责解析
 *  {@code <aop:scoped-proxy/>} 标签。
 *
 *  @author Rob Harrop
 *  @author Juergen Hoeller
 *  @author Mark Fisher
 *  @since 2.0
 * /
 */
class ScopedProxyBeanDefinitionDecorator implements BeanDefinitionDecorator {

    private static final String PROXY_TARGET_CLASS = "proxy-target-class";

    @Override
    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
        boolean proxyTargetClass = true;
        if (node instanceof Element ele) {
            if (ele.hasAttribute(PROXY_TARGET_CLASS)) {
                proxyTargetClass = Boolean.parseBoolean(ele.getAttribute(PROXY_TARGET_CLASS));
            }
        }
        // 将原始的 Bean 定义注册，因为作用域代理将会引用它
        // 并且与工具（验证、导航）相关。
        BeanDefinitionHolder holder = ScopedProxyUtils.createScopedProxy(definition, parserContext.getRegistry(), proxyTargetClass);
        String targetBeanName = ScopedProxyUtils.getTargetBeanName(definition.getBeanName());
        parserContext.getReaderContext().fireComponentRegistered(new BeanComponentDefinition(definition.getBeanDefinition(), targetBeanName));
        return holder;
    }
}
