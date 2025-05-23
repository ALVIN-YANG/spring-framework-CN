// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。

根据 Apache License, Version 2.0 ("许可协议") 许可，您可能不得使用此文件除非符合许可协议。
您可以在以下链接获得许可协议的副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则根据许可协议分发的软件是按照“原样”分发的，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
有关权限和限制的具体语言，请参阅许可协议。*/
package org.springframework.aop.config;

import java.util.List;
import org.w3c.dom.Node;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 为希望向结果bean添加 {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator BeanDefinitionDecorators} 的基本实现
 * 提供了添加一个 {@link org.aopalliance.intercept.MethodInterceptor 拦截器} 的功能。
 *
 * <p>此基本类控制了创建 {@link ProxyFactoryBean} bean定义的过程，并将原始定义作为内部bean定义包装到 {@link ProxyFactoryBean} 的 `target` 属性中。
 *
 * <p>链式调用得到了正确处理，确保只创建一个 {@link ProxyFactoryBean} 定义。如果之前的 {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator}
 * 已经创建了 {@link org.springframework.aop.framework.ProxyFactoryBean}，则拦截器将简单地添加到现有定义中。
 *
 * <p>子类只需创建它们希望添加的拦截器的 `BeanDefinition` 即可。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.aopalliance.intercept.MethodInterceptor
 */
public abstract class AbstractInterceptorDrivenBeanDefinitionDecorator implements BeanDefinitionDecorator {

    @Override
    public final BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definitionHolder, ParserContext parserContext) {
        BeanDefinitionRegistry registry = parserContext.getRegistry();
        // 获取根bean的名称 - 将是生成的代理工厂bean的名称
        String existingBeanName = definitionHolder.getBeanName();
        BeanDefinition targetDefinition = definitionHolder.getBeanDefinition();
        BeanDefinitionHolder targetHolder = new BeanDefinitionHolder(targetDefinition, existingBeanName + ".TARGET");
        // 委托子类以定义拦截器
        BeanDefinition interceptorDefinition = createInterceptorDefinition(node);
        // 生成名称并注册拦截器
        String interceptorName = existingBeanName + '.' + getInterceptorNameSuffix(interceptorDefinition);
        BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(interceptorDefinition, interceptorName), registry);
        BeanDefinitionHolder result = definitionHolder;
        if (!isProxyFactoryBeanDefinition(targetDefinition)) {
            // 创建代理定义
            RootBeanDefinition proxyDefinition = new RootBeanDefinition();
            // 创建代理工厂bean定义
            proxyDefinition.setBeanClass(ProxyFactoryBean.class);
            proxyDefinition.setScope(targetDefinition.getScope());
            proxyDefinition.setLazyInit(targetDefinition.isLazyInit());
            // 设置目标
            proxyDefinition.setDecoratedDefinition(targetHolder);
            proxyDefinition.getPropertyValues().add("target", targetHolder);
            // 创建拦截器名称列表
            proxyDefinition.getPropertyValues().add("interceptorNames", new ManagedList<String>());
            // 从原始的Bean定义复制自动装配设置。
            proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
            proxyDefinition.setPrimary(targetDefinition.isPrimary());
            if (targetDefinition instanceof AbstractBeanDefinition abd) {
                proxyDefinition.copyQualifiersFrom(abd);
            }
            // 将其包装在具有Bean名称的BeanDefinitionHolder中
            result = new BeanDefinitionHolder(proxyDefinition, existingBeanName);
        }
        addInterceptorNameToList(interceptorName, result.getBeanDefinition());
        return result;
    }

    @SuppressWarnings("unchecked")
    private void addInterceptorNameToList(String interceptorName, BeanDefinition beanDefinition) {
        List<String> list = (List<String>) beanDefinition.getPropertyValues().get("interceptorNames");
        Assert.state(list != null, "Missing 'interceptorNames' property");
        list.add(interceptorName);
    }

    private boolean isProxyFactoryBeanDefinition(BeanDefinition existingDefinition) {
        return ProxyFactoryBean.class.getName().equals(existingDefinition.getBeanClassName());
    }

    protected String getInterceptorNameSuffix(BeanDefinition interceptorDefinition) {
        String beanClassName = interceptorDefinition.getBeanClassName();
        return (StringUtils.hasLength(beanClassName) ? StringUtils.uncapitalize(ClassUtils.getShortName(beanClassName)) : "");
    }

    /**
     * 子类应实现此方法以返回要应用于被装饰bean的拦截器的 {@code BeanDefinition}。
     */
    protected abstract BeanDefinition createInterceptorDefinition(Node node);
}
