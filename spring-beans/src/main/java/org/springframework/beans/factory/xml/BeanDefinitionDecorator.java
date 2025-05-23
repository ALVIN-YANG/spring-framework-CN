// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0 版本（以下简称“许可证”）授权；
* 您不得使用此文件除非符合许可证规定。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import org.w3c.dom.Node;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

/**
 *  由 {@link DefaultBeanDefinitionDocumentReader} 使用的接口
 *  用于处理自定义的嵌套（直接位于一个 {@code <bean>} 标签下）标签。
 *
 * <p>装饰也可以基于应用于 {@code <bean>} 标签的自定义属性发生。实现可以自由地将自定义标签中的元数据转换为所需的任意多个
 *  {@link org.springframework.beans.factory.config.BeanDefinition BeanDefinitions}，并转换封装的
 *  {@link org.springframework.beans.factory.config.BeanDefinition} 的标签，甚至可能返回一个完全不同的
 *  {@link org.springframework.beans.factory.config.BeanDefinition} 来替换原始的。
 *
 * <p>{@link BeanDefinitionDecorator BeanDefinitionDecorators} 应该意识到它们可能是一个链的一部分。特别是，
 *  一个 {@link BeanDefinitionDecorator} 应该意识到前一个 {@link BeanDefinitionDecorator} 可能已经用
 *  {@link org.springframework.aop.framework.ProxyFactoryBean} 定义替换了原始的
 *  {@link org.springframework.beans.factory.config.BeanDefinition}，允许添加自定义的
 *  {@link org.aopalliance.intercept.MethodInterceptor} 拦截器。
 *
 * <p>希望向封装的 bean 添加拦截器的
 *  {@link BeanDefinitionDecorator BeanDefinitionDecorators} 应该扩展
 *  {@link org.springframework.aop.config.AbstractInterceptorDrivenBeanDefinitionDecorator}，
 *  该类处理链的链接，确保只创建一个代理，并且它包含链中的所有拦截器。
 *
 * <p>解析器从自定义标签所在的命名空间的
 *  {@link NamespaceHandler} 中定位到一个
 *  {@link BeanDefinitionDecorator}。
 *
 *  作者：Rob Harrop
 *  自 2.0 版本以来
 *  @see NamespaceHandler
 *  @see BeanDefinitionParser
 */
public interface BeanDefinitionDecorator {

    /**
     * 解析指定的 {@link Node}（元素或属性）并装饰提供的 {@link org.springframework.beans.factory.config.BeanDefinition}，
     * 返回装饰后的定义。
     * <p>实现者可以选择返回一个全新的定义，这将替换在最终生成的
     * {@link org.springframework.beans.factory.BeanFactory} 中的原始定义。
     * <p>提供的 {@link ParserContext} 可以用于注册任何支持主定义所需的支持性额外的 bean。
     */
    BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext);
}
