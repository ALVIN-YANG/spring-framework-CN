// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），您可能不得使用此文件除非遵守许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“现状”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的特定语言，
* 请参阅许可证。*/
package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 抽象的 {@link BeanDefinitionParser} 实现，提供了一组便利方法和一个
 * {@link AbstractBeanDefinitionParser#parseInternal 模板方法}，
 * 子类必须重写此模板方法以提供实际的解析逻辑。
 *
 * <p>当您需要将任意复杂的 XML 解析成一个或多个
 * {@link BeanDefinition BeanDefinitions} 时，请使用此
 * {@link BeanDefinitionParser} 实现。如果您只想将 XML 解析成一个
 * 单一的 {@code BeanDefinition}，您可能希望考虑此类的简单便利扩展，
 * 即 {@link AbstractSingleBeanDefinitionParser} 和
 * {@link AbstractSimpleBeanDefinitionParser}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @author Dave Syer
 * @since 2.0
 */
public abstract class AbstractBeanDefinitionParser implements BeanDefinitionParser {

    /**
     * 常量，用于 "id" 属性。
     */
    public static final String ID_ATTRIBUTE = "id";

    /**
     * 常量用于“name”属性。
     */
    public static final String NAME_ATTRIBUTE = "name";

    @Override
    @Nullable
    public final BeanDefinition parse(Element element, ParserContext parserContext) {
        AbstractBeanDefinition definition = parseInternal(element, parserContext);
        if (definition != null && !parserContext.isNested()) {
            try {
                String id = resolveId(element, definition, parserContext);
                if (!StringUtils.hasText(id)) {
                    parserContext.getReaderContext().error("Id is required for element '" + parserContext.getDelegate().getLocalName(element) + "' when used as a top-level tag", element);
                }
                String[] aliases = null;
                if (shouldParseNameAsAliases()) {
                    String name = element.getAttribute(NAME_ATTRIBUTE);
                    if (StringUtils.hasLength(name)) {
                        aliases = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(name));
                    }
                }
                BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, id, aliases);
                registerBeanDefinition(holder, parserContext.getRegistry());
                if (shouldFireEvents()) {
                    BeanComponentDefinition componentDefinition = new BeanComponentDefinition(holder);
                    postProcessComponentDefinition(componentDefinition);
                    parserContext.registerComponent(componentDefinition);
                }
            } catch (BeanDefinitionStoreException ex) {
                String msg = ex.getMessage();
                parserContext.getReaderContext().error((msg != null ? msg : ex.toString()), element);
                return null;
            }
        }
        return definition;
    }

    /**
     * 解决提供的 {@link BeanDefinition} 的 ID。
     * <p>当使用 {@link #shouldGenerateId 生成} 功能时，会自动生成一个名称。
     * 否则，ID 从 "id" 属性中提取，可能还会使用一个
     * {@link #shouldGenerateIdAsFallback() 回退} 到生成的 ID。
     * @param element 从中构建了 bean 定义的对象
     * @param definition 要注册的 bean 定义
     * @param parserContext 封装了当前解析过程状态的对象；
     * 提供了对一个 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
     * 的访问
     * @return 解析出的 ID
     * @throws BeanDefinitionStoreException 如果无法为给定的 bean 定义生成唯一的名称
     */
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        if (shouldGenerateId()) {
            return parserContext.getReaderContext().generateBeanName(definition);
        } else {
            String id = element.getAttribute(ID_ATTRIBUTE);
            if (!StringUtils.hasText(id) && shouldGenerateIdAsFallback()) {
                id = parserContext.getReaderContext().generateBeanName(definition);
            }
            return id;
        }
    }

    /**
     * 将提供的 {@link BeanDefinitionHolder bean} 注册到提供的
     * {@link BeanDefinitionRegistry registry}。
     * <p>子类可以覆盖此方法以控制提供的 {@link BeanDefinitionHolder bean} 是否真正注册，或者注册更多的 bean。
     * <p>默认实现仅在参数 `isNested` 为 `false` 时将提供的 {@link BeanDefinitionHolder bean} 注册到提供的 {@link BeanDefinitionRegistry registry}，因为通常不希望内部 bean 被注册为顶级 bean。
     * @param definition 要注册的 bean 定义
     * @param registry 要将 bean 注册到的注册表
     * @see BeanDefinitionReaderUtils#registerBeanDefinition(BeanDefinitionHolder, BeanDefinitionRegistry)
     */
    protected void registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
        BeanDefinitionReaderUtils.registerBeanDefinition(definition, registry);
    }

    /**
     * 实际解析提供的 {@link Element} 到一个或多个 {@link BeanDefinition BeanDefinitions} 的中心模板方法。
     * @param element 要解析成一个或多个 {@link BeanDefinition BeanDefinitions} 的元素
     * @param parserContext 封装解析过程当前状态的对象；提供对 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry} 的访问
     * @return 解析提供的 {@link Element} 后得到的初始 {@link BeanDefinition}
     * @see #parse(org.w3c.dom.Element, ParserContext)
     * @see #postProcessComponentDefinition(org.springframework.beans.factory.parsing.BeanComponentDefinition)
     */
    @Nullable
    protected abstract AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext);

    /**
     * 是否应该生成 ID 而不是从传入的 {@link Element} 中读取？
     * <p>默认情况下是禁用的；子类可以覆盖此方法以启用 ID 生成。
     * 注意，这个标志是关于 <i>始终</i> 生成 ID 的；在这种情况下，解析器甚至不会检查 "id" 属性。
     * @return 解析器是否应该始终生成 ID
     */
    protected boolean shouldGenerateId() {
        return false;
    }

    /**
     * 如果传入的 {@link Element} 没有明确指定 "id" 属性，是否应该生成一个 ID？
     * 默认情况下禁用；子类可以重写此方法以启用 ID 生成作为后备：在这种情况下，解析器将首先检查 "id" 属性，如果未指定值，则回退到生成一个 ID。
     * @return 如果未指定 id，解析器是否应该生成一个 ID
     */
    protected boolean shouldGenerateIdAsFallback() {
        return false;
    }

    /**
     * 判断元素的 "name" 属性是否应该被解析为
     * 实例定义别名，即备选的实例定义名称。
     * <p>默认实现返回 {@code true}。
     * @return 解析器是否应该将 "name" 属性评估为别名
     * @since 4.1.5
     */
    protected boolean shouldParseNameAsAliases() {
        return true;
    }

    /**
     * 判断这个解析器是否应在解析完bean定义后触发一个
     * {@link org.springframework.beans.factory.parsing.BeanComponentDefinition}
     * 事件。
     * <p>此实现默认返回 {@code true}；也就是说，当bean定义被完全解析后，将会触发一个事件。
     * 覆盖此方法以返回 {@code false} 以取消触发事件。
     * @return 返回 {@code true} 以在解析完bean定义后触发一个组件注册事件；返回 {@code false} 以取消事件
     * @see #postProcessComponentDefinition
     * @see org.springframework.beans.factory.parsing.ReaderContext#fireComponentRegistered
     */
    protected boolean shouldFireEvents() {
        return true;
    }

    /**
     * 在对 `BeanComponentDefinition` 进行主要解析之后、但在将其注册到 `org.springframework.beans.factory.support.BeanDefinitionRegistry` 之前调用的钩子方法。
     * 派生类可以覆盖此方法以提供任何在所有解析完成后要执行的定制逻辑。
     * 默认实现是一个空操作（no-op）。
     * @param componentDefinition 要处理的 `BeanComponentDefinition`
     */
    protected void postProcessComponentDefinition(BeanComponentDefinition componentDefinition) {
    }
}
