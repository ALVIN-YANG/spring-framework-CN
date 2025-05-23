// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.parsing.EmptyReaderEventListener;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.NullSourceExtractor;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.Constants;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.util.xml.XmlValidationModeDetector;

/**
 * XML Bean 定义读取器。
 * 将实际的 XML 文档读取委托给实现 {@link BeanDefinitionDocumentReader} 接口的对象。
 *
 * <p>通常应用于
 * {@link org.springframework.beans.factory.support.DefaultListableBeanFactory}
 * 或 {@link org.springframework.context.support.GenericApplicationContext}。
 *
 * <p>此类加载一个 DOM 文档，并将 BeanDefinitionDocumentReader 应用到该文档上。
 * 文档读取器会将每个 Bean 定义注册到给定的 Bean 工厂中，与后者的
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry} 接口进行交互。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Chris Beams
 * @since 26.11.2003
 * @see #setDocumentReaderClass
 * @see BeanDefinitionDocumentReader
 * @see DefaultBeanDefinitionDocumentReader
 * @see BeanDefinitionRegistry
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

    /**
     * 表示应该禁用验证。
     */
    public static final int VALIDATION_NONE = XmlValidationModeDetector.VALIDATION_NONE;

    /**
     * 表示应自动检测验证模式。
     */
    public static final int VALIDATION_AUTO = XmlValidationModeDetector.VALIDATION_AUTO;

    /**
     * 表示应使用 DTD 验证。
     */
    public static final int VALIDATION_DTD = XmlValidationModeDetector.VALIDATION_DTD;

    /**
     * 表示应使用 XSD 验证。
     */
    public static final int VALIDATION_XSD = XmlValidationModeDetector.VALIDATION_XSD;

    /**
     * 本类的常量实例。
     */
    private static final Constants constants = new Constants(XmlBeanDefinitionReader.class);

    private int validationMode = VALIDATION_AUTO;

    private boolean namespaceAware = false;

    private Class<? extends BeanDefinitionDocumentReader> documentReaderClass = DefaultBeanDefinitionDocumentReader.class;

    private ProblemReporter problemReporter = new FailFastProblemReporter();

    private ReaderEventListener eventListener = new EmptyReaderEventListener();

    private SourceExtractor sourceExtractor = new NullSourceExtractor();

    @Nullable
    private NamespaceHandlerResolver namespaceHandlerResolver;

    private DocumentLoader documentLoader = new DefaultDocumentLoader();

    @Nullable
    private EntityResolver entityResolver;

    private ErrorHandler errorHandler = new SimpleSaxErrorHandler(logger);

    private final XmlValidationModeDetector validationModeDetector = new XmlValidationModeDetector();

    private final ThreadLocal<Set<EncodedResource>> resourcesCurrentlyBeingLoaded = new NamedThreadLocal<>("XML bean definition resources currently being loaded") {

        @Override
        protected Set<EncodedResource> initialValue() {
            return new HashSet<>(4);
        }
    };

    /**
     * 为给定的bean工厂创建新的XmlBeanDefinitionReader。
     * @param registry 要将bean定义加载到其中的BeanFactory，形式为一个BeanDefinitionRegistry
     */
    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
    }

    /**
     * 设置是否使用 XML 验证。默认值为 {@code true}。
     * <p>当验证关闭时，此方法将启用命名空间感知，以便在此场景下仍能正确处理模式命名空间。
     * @see #setValidationMode
     * @see #setNamespaceAware
     */
    public void setValidating(boolean validating) {
        this.validationMode = (validating ? VALIDATION_AUTO : VALIDATION_NONE);
        this.namespaceAware = !validating;
    }

    /**
     * 设置通过名称使用的验证模式。默认为 {@link #VALIDATION_AUTO}。
     * @see #setValidationMode
     */
    public void setValidationModeName(String validationModeName) {
        setValidationMode(constants.asNumber(validationModeName).intValue());
    }

    /**
     * 设置要使用的验证模式。默认为 {@link #VALIDATION_AUTO}。
     * <p>注意，这仅激活或停用验证本身。
     * 如果你需要关闭模式文件上的验证，可能需要显式激活模式命名空间支持：请参阅 {@link #setNamespaceAware}。
     */
    public void setValidationMode(int validationMode) {
        this.validationMode = validationMode;
    }

    /**
     * 返回要使用的验证模式。
     */
    public int getValidationMode() {
        return this.validationMode;
    }

    /**
     * 设置 XML 解析器是否应支持 XML 命名空间。
     * 默认值为 "false"。
     * <p>当启用模式验证时，通常不需要这项功能。
     * 然而，在没有验证的情况下，为了正确处理模式命名空间，必须将其切换为 "true"。
     */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /**
     * 返回 XML 解析器是否应该支持 XML 命名空间。
     */
    public boolean isNamespaceAware() {
        return this.namespaceAware;
    }

    /**
     * 指定要使用的 {@link org.springframework.beans.factory.parsing.ProblemReporter}。
     * <p>默认实现是 {@link org.springframework.beans.factory.parsing.FailFastProblemReporter}，它表现出快速失败的行为。外部工具可以提供替代实现，该实现可以收集错误和警告以在工具用户界面中显示。
     */
    public void setProblemReporter(@Nullable ProblemReporter problemReporter) {
        this.problemReporter = (problemReporter != null ? problemReporter : new FailFastProblemReporter());
    }

    /**
     * 指定要使用哪个 {@link ReaderEventListener}。
     * <p>默认实现为 EmptyReaderEventListener，它丢弃每个事件通知。
     * 外部工具可以提供替代实现来监控在 BeanFactory 中注册的组件。
     */
    public void setEventListener(@Nullable ReaderEventListener eventListener) {
        this.eventListener = (eventListener != null ? eventListener : new EmptyReaderEventListener());
    }

    /**
     * 指定要使用的 {@link SourceExtractor}。
     * <p>默认实现是 {@link NullSourceExtractor}，它简单地返回空值作为源对象。这意味着在正常运行时执行期间 -
     * 不会将额外的源元数据附加到 Bean 配置元数据上。
     */
    public void setSourceExtractor(@Nullable SourceExtractor sourceExtractor) {
        this.sourceExtractor = (sourceExtractor != null ? sourceExtractor : new NullSourceExtractor());
    }

    /**
     * 指定要使用的 {@link NamespaceHandlerResolver}。
     * <p>如果没有指定，将通过 {@link #createDefaultNamespaceHandlerResolver()} 创建默认实例。
     */
    public void setNamespaceHandlerResolver(@Nullable NamespaceHandlerResolver namespaceHandlerResolver) {
        this.namespaceHandlerResolver = namespaceHandlerResolver;
    }

    /**
     * 指定要使用的 {@link DocumentLoader}。
     * <p>默认实现是 {@link DefaultDocumentLoader}，它使用 JAXP 加载 {@link Document} 实例。
     */
    public void setDocumentLoader(@Nullable DocumentLoader documentLoader) {
        this.documentLoader = (documentLoader != null ? documentLoader : new DefaultDocumentLoader());
    }

    /**
     * 设置用于解析的 SAX 实体解析器。
     * <p>默认情况下，将使用 {@link ResourceEntityResolver}。可以对其进行覆盖以实现自定义实体解析，例如相对于某个特定的基础路径。
     */
    public void setEntityResolver(@Nullable EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    /**
     * 返回要使用的实体解析器，如果没有指定，则构建一个默认解析器。
     */
    protected EntityResolver getEntityResolver() {
        if (this.entityResolver == null) {
            // 确定要使用的默认实体解析器。
            ResourceLoader resourceLoader = getResourceLoader();
            if (resourceLoader != null) {
                this.entityResolver = new ResourceEntityResolver(resourceLoader);
            } else {
                this.entityResolver = new DelegatingEntityResolver(getBeanClassLoader());
            }
        }
        return this.entityResolver;
    }

    /**
     * 为XML解析错误和警告设置一个实现接口的实例：{@code org.xml.sax.ErrorHandler}。
     * <p>如果没有设置，将使用默认的SimpleSaxErrorHandler，它只是使用视图类的日志实例记录警告，
     * 并重新抛出错误以中断XML转换。
     * @see SimpleSaxErrorHandler
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * 指定要使用的 {@link BeanDefinitionDocumentReader} 实现类，该类负责实际读取 XML 领域定义文档。
     * <p>默认值为 {@link DefaultBeanDefinitionDocumentReader}。
     * @param documentReaderClass 要使用的 BeanDefinitionDocumentReader 实现类的完全限定名
     */
    public void setDocumentReaderClass(Class<? extends BeanDefinitionDocumentReader> documentReaderClass) {
        this.documentReaderClass = documentReaderClass;
    }

    /**
     * 从指定的 XML 文件中加载 Bean 定义。
     * @param resource XML 文件的资源描述符
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下抛出
     */
    @Override
    public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
        return loadBeanDefinitions(new EncodedResource(resource));
    }

    /**
     * 从指定的 XML 文件加载 Bean 定义。
     * @param encodedResource XML 文件的资源描述符，允许指定用于解析文件的编码
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下抛出
     */
    public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
        Assert.notNull(encodedResource, "EncodedResource must not be null");
        if (logger.isTraceEnabled()) {
            logger.trace("Loading XML bean definitions from " + encodedResource);
        }
        Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
        if (!currentResources.add(encodedResource)) {
            throw new BeanDefinitionStoreException("Detected cyclic loading of " + encodedResource + " - check your import definitions!");
        }
        try (InputStream inputStream = encodedResource.getResource().getInputStream()) {
            InputSource inputSource = new InputSource(inputStream);
            if (encodedResource.getEncoding() != null) {
                inputSource.setEncoding(encodedResource.getEncoding());
            }
            return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("IOException parsing XML document from " + encodedResource.getResource(), ex);
        } finally {
            currentResources.remove(encodedResource);
            if (currentResources.isEmpty()) {
                this.resourcesCurrentlyBeingLoaded.remove();
            }
        }
    }

    /**
     * 从指定的 XML 文件中加载 Bean 定义。
     * @param inputSource 从中读取的 SAX InputSource
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下抛出
     */
    public int loadBeanDefinitions(InputSource inputSource) throws BeanDefinitionStoreException {
        return loadBeanDefinitions(inputSource, "resource loaded through SAX InputSource");
    }

    /**
     * 从指定的 XML 文件加载 Bean 定义。
     * @param inputSource 要从中读取的 SAX InputSource
     * @param resourceDescription 资源描述（可以为 null 或空）
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析出错的情况下抛出异常
     */
    public int loadBeanDefinitions(InputSource inputSource, @Nullable String resourceDescription) throws BeanDefinitionStoreException {
        return doLoadBeanDefinitions(inputSource, new DescriptiveResource(resourceDescription));
    }

    /**
     * 实际从指定的 XML 文件中加载 Bean 定义。
     * @param inputSource 要从中读取的 SAX InputSource
     * @param resource XML 文件的资源描述符
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下抛出
     * @see #doLoadDocument
     * @see #registerBeanDefinitions
     */
    protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource) throws BeanDefinitionStoreException {
        try {
            Document doc = doLoadDocument(inputSource, resource);
            int count = registerBeanDefinitions(doc, resource);
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded " + count + " bean definitions from " + resource);
            }
            return count;
        } catch (BeanDefinitionStoreException ex) {
            throw ex;
        } catch (SAXParseException ex) {
            throw new XmlBeanDefinitionStoreException(resource.getDescription(), "Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
        } catch (SAXException ex) {
            throw new XmlBeanDefinitionStoreException(resource.getDescription(), "XML document from " + resource + " is invalid", ex);
        } catch (ParserConfigurationException ex) {
            throw new BeanDefinitionStoreException(resource.getDescription(), "Parser configuration exception parsing XML from " + resource, ex);
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException(resource.getDescription(), "IOException parsing XML document from " + resource, ex);
        } catch (Throwable ex) {
            throw new BeanDefinitionStoreException(resource.getDescription(), "Unexpected exception parsing XML document from " + resource, ex);
        }
    }

    /**
     * 实际上使用配置的 DocumentLoader 加载指定的文档。
     * @param inputSource 从中读取的 SAX InputSource
     * @param resource XML文件的资源描述符
     * @return DOM 文档
     * @throws Exception 当 DocumentLoader 抛出异常时
     * @see #setDocumentLoader
     * @see DocumentLoader#loadDocument
     */
    protected Document doLoadDocument(InputSource inputSource, Resource resource) throws Exception {
        return this.documentLoader.loadDocument(inputSource, getEntityResolver(), this.errorHandler, getValidationModeForResource(resource), isNamespaceAware());
    }

    /**
     * 确定指定资源的验证模式。
     * 如果没有显式配置验证模式，则从给定资源中检测到验证模式。
     * <p>如果您想完全控制验证模式，即使在设置了除 {@link #VALIDATION_AUTO} 之外的模式时也是如此，请重写此方法。
     * @see #detectValidationMode
     */
    protected int getValidationModeForResource(Resource resource) {
        int validationModeToUse = getValidationMode();
        if (validationModeToUse != VALIDATION_AUTO) {
            return validationModeToUse;
        }
        int detectedMode = detectValidationMode(resource);
        if (detectedMode != VALIDATION_AUTO) {
            return detectedMode;
        }
        // 嗯，我们没有得到明确的指示...让我们假设是XSD。
        // 由于显然在此之前的某个地方尚未找到 DTD 声明
        // 检测已停止（在找到文档的根标签之前）。
        return VALIDATION_XSD;
    }

    /**
     *  检测对由提供的 {@link Resource} 标识的 XML 文件执行哪种验证。如果文件有 {@code DOCTYPE} 定义，则使用 DTD 验证，否则假设使用 XSD 验证。
     * 	<p>如果您想自定义解析 {@link #VALIDATION_AUTO} 模式，请覆盖此方法。
     */
    protected int detectValidationMode(Resource resource) {
        if (resource.isOpen()) {
            throw new BeanDefinitionStoreException("Passed-in Resource [" + resource + "] contains an open stream: " + "cannot determine validation mode automatically. Either pass in a Resource " + "that is able to create fresh streams, or explicitly specify the validationMode " + "on your XmlBeanDefinitionReader instance.");
        }
        InputStream inputStream;
        try {
            inputStream = resource.getInputStream();
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("Unable to determine validation mode for [" + resource + "]: cannot open InputStream. " + "Did you attempt to load directly from a SAX InputSource without specifying the " + "validationMode on your XmlBeanDefinitionReader instance?", ex);
        }
        try {
            return this.validationModeDetector.detectValidationMode(inputStream);
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("Unable to determine validation mode for [" + resource + "]: an error occurred whilst reading from the InputStream.", ex);
        }
    }

    /**
     * 注册包含在给定 DOM 文档中的 Bean 定义。
     * 由 {@code loadBeanDefinitions} 调用。
     * <p>创建解析器类的新实例，并对其调用 {@code registerBeanDefinitions}。
     * @param doc DOM 文档
     * @param resource 资源描述符（用于上下文信息）
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在解析错误的情况下
     * @see #loadBeanDefinitions
     * @see #setDocumentReaderClass
     * @see BeanDefinitionDocumentReader#registerBeanDefinitions
     */
    public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
        BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
        int countBefore = getRegistry().getBeanDefinitionCount();
        documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
        return getRegistry().getBeanDefinitionCount() - countBefore;
    }

    /**
     * 创建用于从 XML 文档实际读取 bean 定义使用的 {@link BeanDefinitionDocumentReader}。
     * <p>默认实现会实例化指定的 "documentReaderClass"。
     * @see #setDocumentReaderClass
     */
    protected BeanDefinitionDocumentReader createBeanDefinitionDocumentReader() {
        return BeanUtils.instantiateClass(this.documentReaderClass);
    }

    /**
     * 创建一个要传递给文档读取器的 {@link XmlReaderContext}。
     */
    public XmlReaderContext createReaderContext(Resource resource) {
        return new XmlReaderContext(resource, this.problemReporter, this.eventListener, this.sourceExtractor, this, getNamespaceHandlerResolver());
    }

    /**
     * 如果之前未设置，则懒加载一个默认的 NamespaceHandlerResolver。
     * @see #createDefaultNamespaceHandlerResolver()
     */
    public NamespaceHandlerResolver getNamespaceHandlerResolver() {
        if (this.namespaceHandlerResolver == null) {
            this.namespaceHandlerResolver = createDefaultNamespaceHandlerResolver();
        }
        return this.namespaceHandlerResolver;
    }

    /**
     * 创建默认的 {@link NamespaceHandlerResolver} 实现，如果未指定则使用。
     * <p>默认实现返回一个 {@link DefaultNamespaceHandlerResolver} 的实例。
     * @see DefaultNamespaceHandlerResolver#DefaultNamespaceHandlerResolver(ClassLoader)
     */
    protected NamespaceHandlerResolver createDefaultNamespaceHandlerResolver() {
        ResourceLoader resourceLoader = getResourceLoader();
        ClassLoader cl = (resourceLoader != null ? resourceLoader.getClassLoader() : getBeanClassLoader());
        return new DefaultNamespaceHandlerResolver(cl);
    }
}
