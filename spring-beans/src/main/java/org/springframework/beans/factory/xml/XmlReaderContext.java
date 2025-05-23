// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接处获得许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可协议，了解特定语言对许可和限制的规范。*/
package org.springframework.beans.factory.xml;

import java.io.StringReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

/**
 * 扩展了 {@link org.springframework.beans.factory.parsing.ReaderContext}，
 * 专门用于与 {@link XmlBeanDefinitionReader} 一起使用。提供了对配置在
 * {@link XmlBeanDefinitionReader} 中的 {@link NamespaceHandlerResolver} 的访问。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class XmlReaderContext extends ReaderContext {

    private final XmlBeanDefinitionReader reader;

    private final NamespaceHandlerResolver namespaceHandlerResolver;

    /**
     * 构建一个新的 {@code XmlReaderContext}。
     * @param resource XML Bean 定义资源
     * @param problemReporter 正在使用的错误报告器
     * @param eventListener 正在使用的事件监听器
     * @param sourceExtractor 正在使用的源提取器
     * @param reader 正在使用的 XML Bean 定义读取器
     * @param namespaceHandlerResolver XML 命名空间解析器
     */
    public XmlReaderContext(Resource resource, ProblemReporter problemReporter, ReaderEventListener eventListener, SourceExtractor sourceExtractor, XmlBeanDefinitionReader reader, NamespaceHandlerResolver namespaceHandlerResolver) {
        super(resource, problemReporter, eventListener, sourceExtractor);
        this.reader = reader;
        this.namespaceHandlerResolver = namespaceHandlerResolver;
    }

    /**
     * 返回正在使用的 XML Bean 定义读取器。
     */
    public final XmlBeanDefinitionReader getReader() {
        return this.reader;
    }

    /**
     * 返回要使用的Bean定义注册表。
     * @see XmlBeanDefinitionReader#XmlBeanDefinitionReader(BeanDefinitionRegistry)
     */
    public final BeanDefinitionRegistry getRegistry() {
        return this.reader.getRegistry();
    }

    /**
     * 返回要使用的资源加载器，如果有的话。
     * <p>在常规情况下，此对象将非空，
     * 同时允许访问资源类加载器。
     * @see XmlBeanDefinitionReader#setResourceLoader
     * @see ResourceLoader#getClassLoader()
     */
    @Nullable
    public final ResourceLoader getResourceLoader() {
        return this.reader.getResourceLoader();
    }

    /**
     * 返回要使用的Bean类加载器，如果有。
     * <p>注意，在常规情况下这将返回null，
     * 作为延迟解析Bean类的指示。
     * @see XmlBeanDefinitionReader#setBeanClassLoader
     */
    @Nullable
    public final ClassLoader getBeanClassLoader() {
        return this.reader.getBeanClassLoader();
    }

    /**
     * 返回要使用的环境。
     * @see XmlBeanDefinitionReader#setEnvironment
     */
    public final Environment getEnvironment() {
        return this.reader.getEnvironment();
    }

    /**
     * 返回命名空间解析器。
     * @see XmlBeanDefinitionReader#setNamespaceHandlerResolver
     */
    public final NamespaceHandlerResolver getNamespaceHandlerResolver() {
        return this.namespaceHandlerResolver;
    }

    // 方便方法用于委托
    /**
     * 调用给定Bean定义的Bean名称生成器。
     * @see XmlBeanDefinitionReader#getBeanNameGenerator()
     * @see org.springframework.beans.factory.support.BeanNameGenerator#generateBeanName
     */
    public String generateBeanName(BeanDefinition beanDefinition) {
        return this.reader.getBeanNameGenerator().generateBeanName(beanDefinition, getRegistry());
    }

    /**
     * 调用给定的Bean定义的Bean名称生成器
     * 并将Bean定义在生成的名称下注册。
     * @see XmlBeanDefinitionReader#getBeanNameGenerator()
     * @see org.springframework.beans.factory.support.BeanNameGenerator#generateBeanName
     * @see BeanDefinitionRegistry#registerBeanDefinition
     */
    public String registerWithGeneratedName(BeanDefinition beanDefinition) {
        String generatedName = generateBeanName(beanDefinition);
        getRegistry().registerBeanDefinition(generatedName, beanDefinition);
        return generatedName;
    }

    /**
     * 从给定的字符串中读取一个XML文档。
     * @see #getReader()
     */
    public Document readDocumentFromString(String documentContent) {
        InputSource is = new InputSource(new StringReader(documentContent));
        try {
            return this.reader.doLoadDocument(is, getResource());
        } catch (Exception ex) {
            throw new BeanDefinitionStoreException("Failed to read XML document", ex);
        }
    }
}
