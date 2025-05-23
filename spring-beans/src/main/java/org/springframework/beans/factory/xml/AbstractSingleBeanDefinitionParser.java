// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）授权；
* 除非遵守许可协议，否则不得使用此文件。
* 您可以在以下地址获取许可协议副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“现状”提供的，不提供任何形式的明示或暗示保证，
* 包括但不限于对适销性、适用性和非侵权性的保证。
* 请参阅许可协议了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.lang.Nullable;

/**
 * 为那些需要解析并定义单个 <i>单一</i> 的 {@code BeanDefinition} 的 {@link BeanDefinitionParser} 实现提供基类。
 *
 * <p>当您想要从一个任意复杂的 XML 元素中创建一个单例的 bean 定义时，请扩展此解析器类。如果您想从一个相对简单的自定义 XML 元素中创建单个 bean 定义，您可能会考虑扩展 {@link AbstractSimpleBeanDefinitionParser}。
 *
 * <p>生成的 {@code BeanDefinition} 将会自动注册到 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}。您的任务只是使用 {@link #doParse} 方法将自定义 XML 元素解析为单个的 {@code BeanDefinition}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 * @see #getBeanClass
 * @see #getBeanClassName
 * @see #doParse
 */
public abstract class AbstractSingleBeanDefinitionParser extends AbstractBeanDefinitionParser {

    /**
     * 创建一个用于 {@link #getBeanClass bean 类} 的 {@link BeanDefinitionBuilder} 实例，并将其传递给 {@link #doParse} 策略方法。
     * @param element 要解析成单个 BeanDefinition 的元素
     * @param parserContext 封装解析过程当前状态的对象
     * @return 解析提供的 {@link Element} 后得到的 BeanDefinition
     * @throws IllegalStateException 如果从 {@link #getBeanClass(org.w3c.dom.Element)} 返回的 bean {@link Class} 为 {@code null}
     * @see #doParse
     */
    @Override
    protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        String parentName = getParentName(element);
        if (parentName != null) {
            builder.getRawBeanDefinition().setParentName(parentName);
        }
        Class<?> beanClass = getBeanClass(element);
        if (beanClass != null) {
            builder.getRawBeanDefinition().setBeanClass(beanClass);
        } else {
            String beanClassName = getBeanClassName(element);
            if (beanClassName != null) {
                builder.getRawBeanDefinition().setBeanClassName(beanClassName);
            }
        }
        builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
        BeanDefinition containingBd = parserContext.getContainingBeanDefinition();
        if (containingBd != null) {
            // 内部 Bean 定义必须与包含 Bean 具有相同的作用域。
            builder.setScope(containingBd.getScope());
        }
        if (parserContext.isDefaultLazyInit()) {
            // 默认的延迟初始化也适用于自定义的Bean定义。
            builder.setLazyInit(true);
        }
        doParse(element, parserContext, builder);
        return builder.getBeanDefinition();
    }

    /**
     * 确定当前解析的bean的父bean的名称，
     * 如果当前bean被定义为子bean。
     * <p>默认实现返回{@code null}，
     * 表示这是一个根bean定义。
     * @param element 正在解析的{@code Element}
     * @return 当前解析的bean的父bean的名称，
     * 或者在没有父bean时返回{@code null}
     */
    @Nullable
    protected String getParentName(Element element) {
        return null;
    }

    /**
     * 确定与提供的 {@link Element} 对应的 Bean 类。
     * <p>注意，对于应用程序类，通常更倾向于覆盖 {@link #getBeanClassName}，以避免直接依赖于 Bean 实现类。这样，BeanDefinitionParser 及其 NamespaceHandler 可以在 IDE 插件中使用，即使应用程序类不在插件的类路径上。
     * @param element 正在被解析的 {@code Element}
     * @return 通过解析提供的 {@code Element} 定义的 Bean 的 {@link Class}，或如果不存在则返回 {@code null}
     * @see #getBeanClassName
     */
    @Nullable
    protected Class<?> getBeanClass(Element element) {
        return null;
    }

    /**
     * 确定与提供的 {@link Element} 对应的 bean 类名称。
     * @param element 正在被解析的 {@code Element}
     * @return 通过解析提供的 {@code Element} 定义的 bean 的类名称，如果没有则返回 {@code null}
     * @see #getBeanClass
     */
    @Nullable
    protected String getBeanClassName(Element element) {
        return null;
    }

    /**
     * 解析提供的 {@link Element} 并按需填充提供的
     * {@link BeanDefinitionBuilder}。
     * <p>默认实现委托给没有 ParserContext 参数的 {@code doParse}
     * 版本。
     * @param element 正在解析的 XML 元素
     * @param parserContext 封装解析过程当前状态的对象
     * @param builder 用于定义的 {@code BeanDefinition}
     * @see #doParse(Element, BeanDefinitionBuilder)
     */
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        doParse(element, builder);
    }

    /**
     * 解析提供的 {@link Element} 并根据需要填充提供的
     * {@link BeanDefinitionBuilder}。
     * <p>默认实现不执行任何操作。
     * @param element 正在被解析的 XML 元素
     * @param builder 用于定义的 {@code BeanDefinition}
     */
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
    }
}
