// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），除非法律要求或经书面同意，否则您不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * 该类是 {@link BeanDefinitionDocumentReader} 接口的默认实现，用于根据 "spring-beans" DTD 和 XSD 格式读取 bean 定义
 * （Spring 默认的 XML bean 定义格式）。
 *
 * <p>所需 XML 文档的结构、元素和属性名称在此类中硬编码。（当然，如果需要，可以运行一个转换来生成此格式）。XML 文档的根元素不需要是 {@code <beans>}：此类将解析 XML 文件中的所有 bean 定义元素，无论实际根元素是什么。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 2003年12月18日
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

    public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

    public static final String NESTED_BEANS_ELEMENT = "beans";

    public static final String ALIAS_ELEMENT = "alias";

    public static final String NAME_ATTRIBUTE = "name";

    public static final String ALIAS_ATTRIBUTE = "alias";

    public static final String IMPORT_ELEMENT = "import";

    public static final String RESOURCE_ATTRIBUTE = "resource";

    public static final String PROFILE_ATTRIBUTE = "profile";

    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private XmlReaderContext readerContext;

    @Nullable
    private BeanDefinitionParserDelegate delegate;

    /**
     * 此实现根据 "spring-beans" XSD（或历史上使用的 DTD）解析 Bean 定义。
     * <p>首先打开 DOM 文档；然后初始化在 `<beans/>` 级别指定的默认设置；接着解析包含的 Bean 定义。
     */
    @Override
    public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
        this.readerContext = readerContext;
        doRegisterBeanDefinitions(doc.getDocumentElement());
    }

    /**
     * 返回此解析器所操作的 XML 资源描述符。
     */
    protected final XmlReaderContext getReaderContext() {
        Assert.state(this.readerContext != null, "No XmlReaderContext available");
        return this.readerContext;
    }

    /**
     * 调用 {@link org.springframework.beans.factory.parsing.SourceExtractor} 以从提供的 {@link Element} 中提取源元数据。
     */
    @Nullable
    protected Object extractSource(Element ele) {
        return getReaderContext().extractSource(ele);
    }

    /**
     * 在给定的根元素 {@code <beans/>} 中注册每个 Bean 定义。
     */
    // 对于 Environment.acceptsProfiles(String...)
    @SuppressWarnings("deprecation")
    protected void doRegisterBeanDefinitions(Element root) {
        // 任何嵌套的 `<beans>` 元素将导致此方法中的递归。在
        // 为了正确传播和保留 `<beans>` 默认-* 属性。
        // 跟踪当前（父）委托，它可能为null。创建
        // 新的（子）代理，其中包含对父对象的引用，用于回退目的。
        // 然后最终将 this.delegate 重置为其原始（父级）引用。
        // 这种行为模拟了一个不需要实际存在的代理栈。
        BeanDefinitionParserDelegate parent = this.delegate;
        this.delegate = createDelegate(getReaderContext(), root, parent);
        if (this.delegate.isDefaultNamespace(root)) {
            String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
            if (StringUtils.hasText(profileSpec)) {
                String[] specifiedProfiles = StringUtils.tokenizeToStringArray(profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
                // 我们不能使用 Profiles.of(...)，因为不支持配置文件表达式。
                // 在 XML 配置中。请参阅 SPR-12458 以获取详细信息。
                if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec + "] not matching: " + getReaderContext().getResource());
                    }
                    return;
                }
            }
        }
        preProcessXml(root);
        parseBeanDefinitions(root, this.delegate);
        postProcessXml(root);
        this.delegate = parent;
    }

    protected BeanDefinitionParserDelegate createDelegate(XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {
        BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
        delegate.initDefaults(root, parentDelegate);
        return delegate;
    }

    /**
     * 解析文档根级别下的元素：
     * "import", "alias", "bean".
     * @param root 文档的 DOM 根元素
     */
    protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
        if (delegate.isDefaultNamespace(root)) {
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node instanceof Element ele) {
                    if (delegate.isDefaultNamespace(ele)) {
                        parseDefaultElement(ele, delegate);
                    } else {
                        delegate.parseCustomElement(ele);
                    }
                }
            }
        } else {
            delegate.parseCustomElement(root);
        }
    }

    private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
        if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
            importBeanDefinitionResource(ele);
        } else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
            processAliasRegistration(ele);
        } else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
            processBeanDefinition(ele, delegate);
        } else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
            // 递归
            doRegisterBeanDefinitions(ele);
        }
    }

    /**
     * 解析一个 "import" 元素并将给定资源中的 bean 定义加载到 bean 工厂中。
     */
    protected void importBeanDefinitionResource(Element ele) {
        String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
        if (!StringUtils.hasText(location)) {
            getReaderContext().error("Resource location must not be empty", ele);
            return;
        }
        // 解析系统属性：例如 "${user.dir}"
        location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);
        Set<Resource> actualResources = new LinkedHashSet<>(4);
        // 检测位置是绝对URI还是相对URI
        boolean absoluteLocation = false;
        try {
            absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
        } catch (URISyntaxException ex) {
            // 无法转换为 URI，考虑到位置是相对的
            // 除非它是众所周知的 Spring 前缀 "classpath*:"
        }
        // 绝对还是相对？
        if (absoluteLocation) {
            try {
                int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
                if (logger.isTraceEnabled()) {
                    logger.trace("Imported " + importCount + " bean definitions from URL location [" + location + "]");
                }
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error("Failed to import bean definitions from URL location [" + location + "]", ele, ex);
            }
        } else {
            // 没有URL -> 将资源位置视为相对于当前文件。
            try {
                int importCount;
                Resource relativeResource = getReaderContext().getResource().createRelative(location);
                if (relativeResource.exists()) {
                    importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
                    actualResources.add(relativeResource);
                } else {
                    String baseLocation = getReaderContext().getResource().getURL().toString();
                    importCount = getReaderContext().getReader().loadBeanDefinitions(StringUtils.applyRelativePath(baseLocation, location), actualResources);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Imported " + importCount + " bean definitions from relative location [" + location + "]");
                }
            } catch (IOException ex) {
                getReaderContext().error("Failed to resolve current resource location", ele, ex);
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error("Failed to import bean definitions from relative location [" + location + "]", ele, ex);
            }
        }
        Resource[] actResArray = actualResources.toArray(new Resource[0]);
        getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
    }

    /**
     * 处理给定的别名元素，将别名注册到注册表中。
     */
    protected void processAliasRegistration(Element ele) {
        String name = ele.getAttribute(NAME_ATTRIBUTE);
        String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
        boolean valid = true;
        if (!StringUtils.hasText(name)) {
            getReaderContext().error("Name must not be empty", ele);
            valid = false;
        }
        if (!StringUtils.hasText(alias)) {
            getReaderContext().error("Alias must not be empty", ele);
            valid = false;
        }
        if (valid) {
            try {
                getReaderContext().getRegistry().registerAlias(name, alias);
            } catch (Exception ex) {
                getReaderContext().error("Failed to register alias '" + alias + "' for bean with name '" + name + "'", ele, ex);
            }
            getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
        }
    }

    /**
     * 处理给定的bean元素，解析bean定义并将其注册到注册表中。
     */
    protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
        BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
        if (bdHolder != null) {
            bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
            try {
                // 注册最终的装饰实例。
                BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error("Failed to register bean definition with name '" + bdHolder.getBeanName() + "'", ele, ex);
            }
            // 发送注册事件。
            getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
        }
    }

    /**
     * 允许XML通过首先处理任何自定义元素类型来扩展，然后再开始处理bean定义。此方法是任何其他自定义XML预处理的自然扩展点。
     * <p>默认实现为空。子类可以重写此方法以将自定义元素转换为标准Spring bean定义，例如。实现者可以通过相应的访问器访问解析器的bean定义读取器和底层的XML资源。
     * @see #getReaderContext()
     */
    protected void preProcessXml(Element root) {
    }

    /**
     * 允许 XML 具有可扩展性，通过最后处理任何自定义元素类型，在我们完成处理 Bean 定义之后。此方法是对任何其他自定义 XML 后处理的自然扩展点。
     * <p>默认实现为空。子类可以覆盖此方法以将自定义元素转换为标准 Spring Bean 定义，例如。实现者可以通过相应的访问器访问解析器的 Bean 定义读取器和底层的 XML 资源。
     * @see #getReaderContext()
     */
    protected void postProcessXml(Element root) {
    }
}
