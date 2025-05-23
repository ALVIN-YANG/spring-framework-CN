// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.config;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 用于 {@code aop} 命名空间的 {@code NamespaceHandler}。
 *
 * <p>提供用于解析 {@code <aop:config>} 标签的 {@link org.springframework.beans.factory.xml.BeanDefinitionParser}。一个配置标签可以包含嵌套的 {@code pointcut}、{@code advisor} 和 {@code aspect} 标签。
 *
 * <p>使用 {@code pointcut} 标签可以创建名为的 {@link AspectJExpressionPointcut} 实例，并使用简单的语法：
 * <pre class="code">
 * &lt;aop:pointcut id=&quot;getNameCalls&quot; expression=&quot;execution(* *..ITestBean.getName(..))&quot;/&gt;
 * </pre>
 *
 * <p>通过使用 {@code advisor} 标签，您可以配置一个 {@link org.springframework.aop.Advisor} 并自动将其应用于您的 {@link org.springframework.beans.factory.BeanFactory} 中所有相关的 bean。该标签支持内联和引用的 {@link org.springframework.aop.Pointcut}：
 *
 * <pre class="code">
 * &lt;aop:advisor id=&quot;getAgeAdvisor&quot;
 *     pointcut=&quot;execution(* *..ITestBean.getAge(..))&quot;
 *     advice-ref=&quot;getAgeCounter&quot;/&gt;
 *
 * &lt;aop:advisor id=&quot;getNameAdvisor&quot;
 *     pointcut-ref=&quot;getNameCalls&quot;
 *     advice-ref=&quot;getNameCounter&quot;/&gt;</pre>
 *
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AopNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * 注册 '{@code config}'、'@{code spring-configured}'、'@{code aspectj-autoproxy}'
     * 和 '{@code scoped-proxy}' 标签的 {@link BeanDefinitionParser BeanDefinitionParsers}。
     */
    @Override
    public void init() {
        // 在 2.0 XSD 以及 2.5+ XSD 中
        registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
        registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser());
        registerBeanDefinitionDecorator("scoped-proxy", new ScopedProxyBeanDefinitionDecorator());
        // 仅在 2.0 XSD 中：在 2.5+ 中移动到上下文命名空间
        registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
    }
}
