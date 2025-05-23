// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.parsing.BeanEntry;
import org.springframework.beans.factory.parsing.ConstructorArgumentEntry;
import org.springframework.beans.factory.parsing.ParseState;
import org.springframework.beans.factory.parsing.PropertyEntry;
import org.springframework.beans.factory.parsing.QualifierEntry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.ManagedArray;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

/**
 * 用于解析 XML 实体定义的状态保持代理类。
 * 旨在由主解析器以及任何扩展
 * {@link BeanDefinitionParser BeanDefinitionParsers} 或
 * {@link BeanDefinitionDecorator BeanDefinitionDecorators} 使用。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Mark Fisher
 * @author Gary Russell
 * @since 2.0
 * @see ParserContext
 * @see DefaultBeanDefinitionDocumentReader
 */
public class BeanDefinitionParserDelegate {

    public static final String BEANS_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

    public static final String MULTI_VALUE_ATTRIBUTE_DELIMITERS = ",; ";

    /**
     * 表示真的 T/F 属性的值。
     * 其他任何内容表示假。
     */
    public static final String TRUE_VALUE = "true";

    public static final String FALSE_VALUE = "false";

    public static final String DEFAULT_VALUE = "default";

    public static final String DESCRIPTION_ELEMENT = "description";

    public static final String AUTOWIRE_NO_VALUE = "no";

    public static final String AUTOWIRE_BY_NAME_VALUE = "byName";

    public static final String AUTOWIRE_BY_TYPE_VALUE = "byType";

    public static final String AUTOWIRE_CONSTRUCTOR_VALUE = "constructor";

    public static final String AUTOWIRE_AUTODETECT_VALUE = "autodetect";

    public static final String NAME_ATTRIBUTE = "name";

    public static final String BEAN_ELEMENT = "bean";

    public static final String META_ELEMENT = "meta";

    public static final String ID_ATTRIBUTE = "id";

    public static final String PARENT_ATTRIBUTE = "parent";

    public static final String CLASS_ATTRIBUTE = "class";

    public static final String ABSTRACT_ATTRIBUTE = "abstract";

    public static final String SCOPE_ATTRIBUTE = "scope";

    private static final String SINGLETON_ATTRIBUTE = "singleton";

    public static final String LAZY_INIT_ATTRIBUTE = "lazy-init";

    public static final String AUTOWIRE_ATTRIBUTE = "autowire";

    public static final String AUTOWIRE_CANDIDATE_ATTRIBUTE = "autowire-candidate";

    public static final String PRIMARY_ATTRIBUTE = "primary";

    public static final String DEPENDS_ON_ATTRIBUTE = "depends-on";

    public static final String INIT_METHOD_ATTRIBUTE = "init-method";

    public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";

    public static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";

    public static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";

    public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";

    public static final String INDEX_ATTRIBUTE = "index";

    public static final String TYPE_ATTRIBUTE = "type";

    public static final String VALUE_TYPE_ATTRIBUTE = "value-type";

    public static final String KEY_TYPE_ATTRIBUTE = "key-type";

    public static final String PROPERTY_ELEMENT = "property";

    public static final String REF_ATTRIBUTE = "ref";

    public static final String VALUE_ATTRIBUTE = "value";

    public static final String LOOKUP_METHOD_ELEMENT = "lookup-method";

    public static final String REPLACED_METHOD_ELEMENT = "replaced-method";

    public static final String REPLACER_ATTRIBUTE = "replacer";

    public static final String ARG_TYPE_ELEMENT = "arg-type";

    public static final String ARG_TYPE_MATCH_ATTRIBUTE = "match";

    public static final String REF_ELEMENT = "ref";

    public static final String IDREF_ELEMENT = "idref";

    public static final String BEAN_REF_ATTRIBUTE = "bean";

    public static final String PARENT_REF_ATTRIBUTE = "parent";

    public static final String VALUE_ELEMENT = "value";

    public static final String NULL_ELEMENT = "null";

    public static final String ARRAY_ELEMENT = "array";

    public static final String LIST_ELEMENT = "list";

    public static final String SET_ELEMENT = "set";

    public static final String MAP_ELEMENT = "map";

    public static final String ENTRY_ELEMENT = "entry";

    public static final String KEY_ELEMENT = "key";

    public static final String KEY_ATTRIBUTE = "key";

    public static final String KEY_REF_ATTRIBUTE = "key-ref";

    public static final String VALUE_REF_ATTRIBUTE = "value-ref";

    public static final String PROPS_ELEMENT = "props";

    public static final String PROP_ELEMENT = "prop";

    public static final String MERGE_ATTRIBUTE = "merge";

    public static final String QUALIFIER_ELEMENT = "qualifier";

    public static final String QUALIFIER_ATTRIBUTE_ELEMENT = "attribute";

    public static final String DEFAULT_LAZY_INIT_ATTRIBUTE = "default-lazy-init";

    public static final String DEFAULT_MERGE_ATTRIBUTE = "default-merge";

    public static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";

    public static final String DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE = "default-autowire-candidates";

    public static final String DEFAULT_INIT_METHOD_ATTRIBUTE = "default-init-method";

    public static final String DEFAULT_DESTROY_METHOD_ATTRIBUTE = "default-destroy-method";

    protected final Log logger = LogFactory.getLog(getClass());

    private final XmlReaderContext readerContext;

    private final DocumentDefaultsDefinition defaults = new DocumentDefaultsDefinition();

    private final ParseState parseState = new ParseState();

    /**
     * 存储所有使用的 Bean 名称，以便我们可以在每个 beans 元素级别上强制执行唯一性。相同的 Bean ID/名称不允许在同一级别的 beans 元素嵌套中存在，但可以在不同级别上重复。
     */
    private final Set<String> usedNames = new HashSet<>();

    /**
     * 创建一个与提供的 {@link XmlReaderContext} 关联的新 BeanDefinitionParserDelegate。
     */
    public BeanDefinitionParserDelegate(XmlReaderContext readerContext) {
        Assert.notNull(readerContext, "XmlReaderContext must not be null");
        this.readerContext = readerContext;
    }

    /**
     * 获取与该辅助实例关联的 {@link XmlReaderContext}。
     */
    public final XmlReaderContext getReaderContext() {
        return this.readerContext;
    }

    /**
     * 调用{@link org.springframework.beans.factory.parsing.SourceExtractor}来从提供的{@link Element}中提取源元数据。
     */
    @Nullable
    protected Object extractSource(Element ele) {
        return this.readerContext.extractSource(ele);
    }

    /**
     * 为给定的源元素报告带有给定消息的错误。
     */
    protected void error(String message, Node source) {
        this.readerContext.error(message, source, this.parseState.snapshot());
    }

    /**
     * 为给定的源元素报告带有给定信息的错误。
     */
    protected void error(String message, Element source) {
        this.readerContext.error(message, source, this.parseState.snapshot());
    }

    /**
     * 为给定的源元素报告包含指定信息的错误。
     */
    protected void error(String message, Element source, Throwable cause) {
        this.readerContext.error(message, source, this.parseState.snapshot(), cause);
    }

    /**
     * 初始化默认设置，假设父委托为 {@code null}。
     */
    public void initDefaults(Element root) {
        initDefaults(root, null);
    }

    /**
     * 初始化默认的懒加载、自动装配、依赖检查设置、初始化方法、销毁方法和合并设置。通过回退到给定的父元素，支持嵌套的 'beans' 元素用例，以防在本地未显式设置默认值。
     * @see #populateDefaults(DocumentDefaultsDefinition, DocumentDefaultsDefinition, org.w3c.dom.Element)
     * @see #getDefaults()
     */
    public void initDefaults(Element root, @Nullable BeanDefinitionParserDelegate parent) {
        populateDefaults(this.defaults, (parent != null ? parent.defaults : null), root);
        this.readerContext.fireDefaultsRegistered(this.defaults);
    }

    /**
     * 将默认的懒加载（lazy-init）、自动装配（autowire）、依赖检查（dependency check）、初始化方法（init-method）、销毁方法（destroy-method）和合并设置填充给指定的 DocumentDefaultsDefinition 实例。
     * 通过回退到 {@code parentDefaults} 支持嵌套 'beans' 元素的使用场景，如果默认设置没有在本地明确设置。
     * @param defaults 要填充的默认值
     * @param parentDefaults 可选的父 BeanDefinitionParserDelegate 默认值，用于回退
     * @param root 当前 bean 定义文档的根元素（或嵌套的 beans 元素）
     */
    protected void populateDefaults(DocumentDefaultsDefinition defaults, @Nullable DocumentDefaultsDefinition parentDefaults, Element root) {
        String lazyInit = root.getAttribute(DEFAULT_LAZY_INIT_ATTRIBUTE);
        if (isDefaultValue(lazyInit)) {
            // 可能从外部的 `<beans>` 部分继承，否则回退到 false。
            lazyInit = (parentDefaults != null ? parentDefaults.getLazyInit() : FALSE_VALUE);
        }
        defaults.setLazyInit(lazyInit);
        String merge = root.getAttribute(DEFAULT_MERGE_ATTRIBUTE);
        if (isDefaultValue(merge)) {
            // 可能从外部的 `<beans>` 部分继承，否则回退为 false。
            merge = (parentDefaults != null ? parentDefaults.getMerge() : FALSE_VALUE);
        }
        defaults.setMerge(merge);
        String autowire = root.getAttribute(DEFAULT_AUTOWIRE_ATTRIBUTE);
        if (isDefaultValue(autowire)) {
            // 可能从外部的 <beans> 部分继承，否则回退到 'no'。
            autowire = (parentDefaults != null ? parentDefaults.getAutowire() : AUTOWIRE_NO_VALUE);
        }
        defaults.setAutowire(autowire);
        if (root.hasAttribute(DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE)) {
            defaults.setAutowireCandidates(root.getAttribute(DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE));
        } else if (parentDefaults != null) {
            defaults.setAutowireCandidates(parentDefaults.getAutowireCandidates());
        }
        if (root.hasAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE)) {
            defaults.setInitMethod(root.getAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE));
        } else if (parentDefaults != null) {
            defaults.setInitMethod(parentDefaults.getInitMethod());
        }
        if (root.hasAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE)) {
            defaults.setDestroyMethod(root.getAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE));
        } else if (parentDefaults != null) {
            defaults.setDestroyMethod(parentDefaults.getDestroyMethod());
        }
        defaults.setSource(this.readerContext.extractSource(root));
    }

    /**
     * 返回默认定义对象。
     */
    public DocumentDefaultsDefinition getDefaults() {
        return this.defaults;
    }

    /**
     * 返回顶级 `<beans/>` 元素属性中指示的 Bean 定义默认设置。
     */
    public BeanDefinitionDefaults getBeanDefinitionDefaults() {
        BeanDefinitionDefaults bdd = new BeanDefinitionDefaults();
        bdd.setLazyInit(TRUE_VALUE.equalsIgnoreCase(this.defaults.getLazyInit()));
        bdd.setAutowireMode(getAutowireMode(DEFAULT_VALUE));
        bdd.setInitMethodName(this.defaults.getInitMethod());
        bdd.setDestroyMethodName(this.defaults.getDestroyMethod());
        return bdd;
    }

    /**
     * 返回顶级 `<beans/>` 元素的 `'default-autowire-candidates'` 属性中提供的任何模式。
     */
    @Nullable
    public String[] getAutowireCandidatePatterns() {
        String candidatePattern = this.defaults.getAutowireCandidates();
        return (candidatePattern != null ? StringUtils.commaDelimitedListToStringArray(candidatePattern) : null);
    }

    /**
     * 解析提供的 `<bean>` 元素。如果在解析过程中出现错误，可能返回 `null`。
     * 错误将通过 `org.springframework.beans.factory.parsing.ProblemReporter` 报告。
     */
    @Nullable
    public BeanDefinitionHolder parseBeanDefinitionElement(Element ele) {
        return parseBeanDefinitionElement(ele, null);
    }

    /**
     * 解析提供的 {@code <bean>} 元素。如果解析过程中出现错误，可能返回 {@code null}。
     * 错误将通过 {@link org.springframework.beans.factory.parsing.ProblemReporter} 报告。
     */
    @Nullable
    public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, @Nullable BeanDefinition containingBean) {
        String id = ele.getAttribute(ID_ATTRIBUTE);
        String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
        List<String> aliases = new ArrayList<>();
        if (StringUtils.hasLength(nameAttr)) {
            String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
            aliases.addAll(Arrays.asList(nameArr));
        }
        String beanName = id;
        if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
            beanName = aliases.remove(0);
            if (logger.isTraceEnabled()) {
                logger.trace("No XML 'id' specified - using '" + beanName + "' as bean name and " + aliases + " as aliases");
            }
        }
        if (containingBean == null) {
            checkNameUniqueness(beanName, aliases, ele);
        }
        AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
        if (beanDefinition != null) {
            if (!StringUtils.hasText(beanName)) {
                try {
                    if (containingBean != null) {
                        beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, this.readerContext.getRegistry(), true);
                    } else {
                        beanName = this.readerContext.generateBeanName(beanDefinition);
                        // 如果仍然可能，为原始的 Bean 类名注册一个别名。
                        // 如果生成器返回了类名加上一个后缀。
                        // 这是为了与 Spring 1.2/2.0 的向后兼容性而预期的。
                        String beanClassName = beanDefinition.getBeanClassName();
                        if (beanClassName != null && beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() && !this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
                            aliases.add(beanClassName);
                        }
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Neither XML 'id' nor 'name' specified - " + "using generated bean name [" + beanName + "]");
                    }
                } catch (Exception ex) {
                    error(ex.getMessage(), ele);
                    return null;
                }
            }
            String[] aliasesArray = StringUtils.toStringArray(aliases);
            return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
        }
        return null;
    }

    /**
     * 验证指定的bean名称和别名是否已在当前beans元素嵌套级别中未被使用。
     */
    protected void checkNameUniqueness(String beanName, List<String> aliases, Element beanElement) {
        String foundName = null;
        if (StringUtils.hasText(beanName) && this.usedNames.contains(beanName)) {
            foundName = beanName;
        }
        if (foundName == null) {
            foundName = CollectionUtils.findFirstMatch(this.usedNames, aliases);
        }
        if (foundName != null) {
            error("Bean name '" + foundName + "' is already used in this <beans> element", beanElement);
        }
        this.usedNames.add(beanName);
        this.usedNames.addAll(aliases);
    }

    /**
     * 解析bean定义本身，不考虑名称或别名。如果在解析bean定义过程中发生问题，可能会返回{@code null}。
     */
    @Nullable
    public AbstractBeanDefinition parseBeanDefinitionElement(Element ele, String beanName, @Nullable BeanDefinition containingBean) {
        this.parseState.push(new BeanEntry(beanName));
        String className = null;
        if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
            className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
        }
        String parent = null;
        if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
            parent = ele.getAttribute(PARENT_ATTRIBUTE);
        }
        try {
            AbstractBeanDefinition bd = createBeanDefinition(className, parent);
            parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
            bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));
            parseMetaElements(ele, bd);
            parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
            parseReplacedMethodSubElements(ele, bd.getMethodOverrides());
            parseConstructorArgElements(ele, bd);
            parsePropertyElements(ele, bd);
            parseQualifierElements(ele, bd);
            bd.setResource(this.readerContext.getResource());
            bd.setSource(extractSource(ele));
            return bd;
        } catch (ClassNotFoundException ex) {
            error("Bean class [" + className + "] not found", ele, ex);
        } catch (NoClassDefFoundError err) {
            error("Class that bean class [" + className + "] depends on not found", ele, err);
        } catch (Throwable ex) {
            error("Unexpected failure during bean definition parsing", ele, ex);
        } finally {
            this.parseState.pop();
        }
        return null;
    }

    /**
     * 将给定 bean 元素的属性应用到给定的 bean 定义上。
     * @param ele bean 声明元素
     * @param beanName bean 名称
     * @param containingBean 包含的 bean 定义
     * @return 根据 bean 元素属性初始化的 bean 定义
     */
    public AbstractBeanDefinition parseBeanDefinitionAttributes(Element ele, String beanName, @Nullable BeanDefinition containingBean, AbstractBeanDefinition bd) {
        if (ele.hasAttribute(SINGLETON_ATTRIBUTE)) {
            error("Old 1.x 'singleton' attribute in use - upgrade to 'scope' declaration", ele);
        } else if (ele.hasAttribute(SCOPE_ATTRIBUTE)) {
            bd.setScope(ele.getAttribute(SCOPE_ATTRIBUTE));
        } else if (containingBean != null) {
            // 在内部bean定义的情况下，从包含的bean中取默认值。
            bd.setScope(containingBean.getScope());
        }
        if (ele.hasAttribute(ABSTRACT_ATTRIBUTE)) {
            bd.setAbstract(TRUE_VALUE.equals(ele.getAttribute(ABSTRACT_ATTRIBUTE)));
        }
        String lazyInit = ele.getAttribute(LAZY_INIT_ATTRIBUTE);
        if (isDefaultValue(lazyInit)) {
            lazyInit = this.defaults.getLazyInit();
        }
        bd.setLazyInit(TRUE_VALUE.equals(lazyInit));
        String autowire = ele.getAttribute(AUTOWIRE_ATTRIBUTE);
        bd.setAutowireMode(getAutowireMode(autowire));
        if (ele.hasAttribute(DEPENDS_ON_ATTRIBUTE)) {
            String dependsOn = ele.getAttribute(DEPENDS_ON_ATTRIBUTE);
            bd.setDependsOn(StringUtils.tokenizeToStringArray(dependsOn, MULTI_VALUE_ATTRIBUTE_DELIMITERS));
        }
        String autowireCandidate = ele.getAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE);
        if (isDefaultValue(autowireCandidate)) {
            String candidatePattern = this.defaults.getAutowireCandidates();
            if (candidatePattern != null) {
                String[] patterns = StringUtils.commaDelimitedListToStringArray(candidatePattern);
                bd.setAutowireCandidate(PatternMatchUtils.simpleMatch(patterns, beanName));
            }
        } else {
            bd.setAutowireCandidate(TRUE_VALUE.equals(autowireCandidate));
        }
        if (ele.hasAttribute(PRIMARY_ATTRIBUTE)) {
            bd.setPrimary(TRUE_VALUE.equals(ele.getAttribute(PRIMARY_ATTRIBUTE)));
        }
        if (ele.hasAttribute(INIT_METHOD_ATTRIBUTE)) {
            String initMethodName = ele.getAttribute(INIT_METHOD_ATTRIBUTE);
            bd.setInitMethodName(initMethodName);
        } else if (this.defaults.getInitMethod() != null) {
            bd.setInitMethodName(this.defaults.getInitMethod());
            bd.setEnforceInitMethod(false);
        }
        if (ele.hasAttribute(DESTROY_METHOD_ATTRIBUTE)) {
            String destroyMethodName = ele.getAttribute(DESTROY_METHOD_ATTRIBUTE);
            bd.setDestroyMethodName(destroyMethodName);
        } else if (this.defaults.getDestroyMethod() != null) {
            bd.setDestroyMethodName(this.defaults.getDestroyMethod());
            bd.setEnforceDestroyMethod(false);
        }
        if (ele.hasAttribute(FACTORY_METHOD_ATTRIBUTE)) {
            bd.setFactoryMethodName(ele.getAttribute(FACTORY_METHOD_ATTRIBUTE));
        }
        if (ele.hasAttribute(FACTORY_BEAN_ATTRIBUTE)) {
            bd.setFactoryBeanName(ele.getAttribute(FACTORY_BEAN_ATTRIBUTE));
        }
        return bd;
    }

    /**
     * 为给定的类名和父类名创建一个Bean定义。
     * @param className Bean类的名称
     * @param parentName Bean父Bean的名称
     * @return 新创建的Bean定义
     * @throws ClassNotFoundException 如果尝试解析Bean类但失败，将抛出此异常
     */
    protected AbstractBeanDefinition createBeanDefinition(@Nullable String className, @Nullable String parentName) throws ClassNotFoundException {
        return BeanDefinitionReaderUtils.createBeanDefinition(parentName, className, this.readerContext.getBeanClassLoader());
    }

    /**
     * 解析给定元素下方的元元素，如果有的话。
     */
    public void parseMetaElements(Element ele, BeanMetadataAttributeAccessor attributeAccessor) {
        NodeList nl = ele.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (isCandidateElement(node) && nodeNameEquals(node, META_ELEMENT)) {
                Element metaElement = (Element) node;
                String key = metaElement.getAttribute(KEY_ATTRIBUTE);
                String value = metaElement.getAttribute(VALUE_ATTRIBUTE);
                BeanMetadataAttribute attribute = new BeanMetadataAttribute(key, value);
                attribute.setSource(extractSource(metaElement));
                attributeAccessor.addMetadataAttribute(attribute);
            }
        }
    }

    /**
     * 将给定的自动装配属性值解析为
     * {@link AbstractBeanDefinition} 自动装配常量。
     */
    @SuppressWarnings("deprecation")
    public int getAutowireMode(String attrValue) {
        String attr = attrValue;
        if (isDefaultValue(attr)) {
            attr = this.defaults.getAutowire();
        }
        int autowire = AbstractBeanDefinition.AUTOWIRE_NO;
        if (AUTOWIRE_BY_NAME_VALUE.equals(attr)) {
            autowire = AbstractBeanDefinition.AUTOWIRE_BY_NAME;
        } else if (AUTOWIRE_BY_TYPE_VALUE.equals(attr)) {
            autowire = AbstractBeanDefinition.AUTOWIRE_BY_TYPE;
        } else if (AUTOWIRE_CONSTRUCTOR_VALUE.equals(attr)) {
            autowire = AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR;
        } else if (AUTOWIRE_AUTODETECT_VALUE.equals(attr)) {
            autowire = AbstractBeanDefinition.AUTOWIRE_AUTODETECT;
        }
        // 否则保留默认值。
        return autowire;
    }

    /**
     * 解析给定 bean 元素的构造函数参数子元素。
     */
    public void parseConstructorArgElements(Element beanEle, BeanDefinition bd) {
        NodeList nl = beanEle.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (isCandidateElement(node) && nodeNameEquals(node, CONSTRUCTOR_ARG_ELEMENT)) {
                parseConstructorArgElement((Element) node, bd);
            }
        }
    }

    /**
     * 解析给定 bean 元素的属性子元素。
     */
    public void parsePropertyElements(Element beanEle, BeanDefinition bd) {
        NodeList nl = beanEle.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (isCandidateElement(node) && nodeNameEquals(node, PROPERTY_ELEMENT)) {
                parsePropertyElement((Element) node, bd);
            }
        }
    }

    /**
     * 解析给定bean元素的限定符子元素。
     */
    public void parseQualifierElements(Element beanEle, AbstractBeanDefinition bd) {
        NodeList nl = beanEle.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (isCandidateElement(node) && nodeNameEquals(node, QUALIFIER_ELEMENT)) {
                parseQualifierElement((Element) node, bd);
            }
        }
    }

    /**
     * 解析给定bean元素的lookup-override子元素。
     */
    public void parseLookupOverrideSubElements(Element beanEle, MethodOverrides overrides) {
        NodeList nl = beanEle.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (isCandidateElement(node) && nodeNameEquals(node, LOOKUP_METHOD_ELEMENT)) {
                Element ele = (Element) node;
                String methodName = ele.getAttribute(NAME_ATTRIBUTE);
                String beanRef = ele.getAttribute(BEAN_ELEMENT);
                LookupOverride override = new LookupOverride(methodName, beanRef);
                override.setSource(extractSource(ele));
                overrides.addOverride(override);
            }
        }
    }

    /**
     * 解析给定bean元素中的替换方法子元素。
     */
    public void parseReplacedMethodSubElements(Element beanEle, MethodOverrides overrides) {
        NodeList nl = beanEle.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (isCandidateElement(node) && nodeNameEquals(node, REPLACED_METHOD_ELEMENT)) {
                Element replacedMethodEle = (Element) node;
                String name = replacedMethodEle.getAttribute(NAME_ATTRIBUTE);
                String callback = replacedMethodEle.getAttribute(REPLACER_ATTRIBUTE);
                ReplaceOverride replaceOverride = new ReplaceOverride(name, callback);
                // 查找参数类型匹配元素。
                List<Element> argTypeEles = DomUtils.getChildElementsByTagName(replacedMethodEle, ARG_TYPE_ELEMENT);
                for (Element argTypeEle : argTypeEles) {
                    String match = argTypeEle.getAttribute(ARG_TYPE_MATCH_ATTRIBUTE);
                    match = (StringUtils.hasText(match) ? match : DomUtils.getTextValue(argTypeEle));
                    if (StringUtils.hasText(match)) {
                        replaceOverride.addTypeIdentifier(match);
                    }
                }
                replaceOverride.setSource(extractSource(replacedMethodEle));
                overrides.addOverride(replaceOverride);
            }
        }
    }

    /**
     * 解析构造函数参数元素。
     */
    public void parseConstructorArgElement(Element ele, BeanDefinition bd) {
        String indexAttr = ele.getAttribute(INDEX_ATTRIBUTE);
        String typeAttr = ele.getAttribute(TYPE_ATTRIBUTE);
        String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
        if (StringUtils.hasLength(indexAttr)) {
            try {
                int index = Integer.parseInt(indexAttr);
                if (index < 0) {
                    error("'index' cannot be lower than 0", ele);
                } else {
                    try {
                        this.parseState.push(new ConstructorArgumentEntry(index));
                        Object value = parsePropertyValue(ele, bd, null);
                        ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
                        if (StringUtils.hasLength(typeAttr)) {
                            valueHolder.setType(typeAttr);
                        }
                        if (StringUtils.hasLength(nameAttr)) {
                            valueHolder.setName(nameAttr);
                        }
                        valueHolder.setSource(extractSource(ele));
                        if (bd.getConstructorArgumentValues().hasIndexedArgumentValue(index)) {
                            error("Ambiguous constructor-arg entries for index " + index, ele);
                        } else {
                            bd.getConstructorArgumentValues().addIndexedArgumentValue(index, valueHolder);
                        }
                    } finally {
                        this.parseState.pop();
                    }
                }
            } catch (NumberFormatException ex) {
                error("Attribute 'index' of tag 'constructor-arg' must be an integer", ele);
            }
        } else {
            try {
                this.parseState.push(new ConstructorArgumentEntry());
                Object value = parsePropertyValue(ele, bd, null);
                ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
                if (StringUtils.hasLength(typeAttr)) {
                    valueHolder.setType(typeAttr);
                }
                if (StringUtils.hasLength(nameAttr)) {
                    valueHolder.setName(nameAttr);
                }
                valueHolder.setSource(extractSource(ele));
                bd.getConstructorArgumentValues().addGenericArgumentValue(valueHolder);
            } finally {
                this.parseState.pop();
            }
        }
    }

    /**
     * 解析属性元素。
     */
    public void parsePropertyElement(Element ele, BeanDefinition bd) {
        String propertyName = ele.getAttribute(NAME_ATTRIBUTE);
        if (!StringUtils.hasLength(propertyName)) {
            error("Tag 'property' must have a 'name' attribute", ele);
            return;
        }
        this.parseState.push(new PropertyEntry(propertyName));
        try {
            if (bd.getPropertyValues().contains(propertyName)) {
                error("Multiple 'property' definitions for property '" + propertyName + "'", ele);
                return;
            }
            Object val = parsePropertyValue(ele, bd, propertyName);
            PropertyValue pv = new PropertyValue(propertyName, val);
            parseMetaElements(ele, pv);
            pv.setSource(extractSource(ele));
            bd.getPropertyValues().addPropertyValue(pv);
        } finally {
            this.parseState.pop();
        }
    }

    /**
     * 解析一个限定符元素。
     */
    public void parseQualifierElement(Element ele, AbstractBeanDefinition bd) {
        String typeName = ele.getAttribute(TYPE_ATTRIBUTE);
        if (!StringUtils.hasLength(typeName)) {
            error("Tag 'qualifier' must have a 'type' attribute", ele);
            return;
        }
        this.parseState.push(new QualifierEntry(typeName));
        try {
            AutowireCandidateQualifier qualifier = new AutowireCandidateQualifier(typeName);
            qualifier.setSource(extractSource(ele));
            String value = ele.getAttribute(VALUE_ATTRIBUTE);
            if (StringUtils.hasLength(value)) {
                qualifier.setAttribute(AutowireCandidateQualifier.VALUE_KEY, value);
            }
            NodeList nl = ele.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (isCandidateElement(node) && nodeNameEquals(node, QUALIFIER_ATTRIBUTE_ELEMENT)) {
                    Element attributeEle = (Element) node;
                    String attributeName = attributeEle.getAttribute(KEY_ATTRIBUTE);
                    String attributeValue = attributeEle.getAttribute(VALUE_ATTRIBUTE);
                    if (StringUtils.hasLength(attributeName) && StringUtils.hasLength(attributeValue)) {
                        BeanMetadataAttribute attribute = new BeanMetadataAttribute(attributeName, attributeValue);
                        attribute.setSource(extractSource(attributeEle));
                        qualifier.addMetadataAttribute(attribute);
                    } else {
                        error("Qualifier 'attribute' tag must have a 'name' and 'value'", attributeEle);
                        return;
                    }
                }
            }
            bd.addQualifier(qualifier);
        } finally {
            this.parseState.pop();
        }
    }

    /**
     * 获取属性元素的值。可能是一个列表等。
     * 也可用于构造函数的参数，在这种情况下"propertyName"为null。
     */
    @Nullable
    public Object parsePropertyValue(Element ele, BeanDefinition bd, @Nullable String propertyName) {
        String elementName = (propertyName != null ? "<property> element for property '" + propertyName + "'" : "<constructor-arg> element");
        // 应该只有一个子元素：ref、value、list等。
        NodeList nl = ele.getChildNodes();
        Element subElement = null;
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element currentElement && !nodeNameEquals(node, DESCRIPTION_ELEMENT) && !nodeNameEquals(node, META_ELEMENT)) {
                // 子元素就是我们正在寻找的。
                if (subElement != null) {
                    error(elementName + " must not contain more than one sub-element", ele);
                } else {
                    subElement = currentElement;
                }
            }
        }
        boolean hasRefAttribute = ele.hasAttribute(REF_ATTRIBUTE);
        boolean hasValueAttribute = ele.hasAttribute(VALUE_ATTRIBUTE);
        if ((hasRefAttribute && hasValueAttribute) || ((hasRefAttribute || hasValueAttribute) && subElement != null)) {
            error(elementName + " is only allowed to contain either 'ref' attribute OR 'value' attribute OR sub-element", ele);
        }
        if (hasRefAttribute) {
            String refName = ele.getAttribute(REF_ATTRIBUTE);
            if (!StringUtils.hasText(refName)) {
                error(elementName + " contains empty 'ref' attribute", ele);
            }
            RuntimeBeanReference ref = new RuntimeBeanReference(refName);
            ref.setSource(extractSource(ele));
            return ref;
        } else if (hasValueAttribute) {
            TypedStringValue valueHolder = new TypedStringValue(ele.getAttribute(VALUE_ATTRIBUTE));
            valueHolder.setSource(extractSource(ele));
            return valueHolder;
        } else if (subElement != null) {
            return parsePropertySubElement(subElement, bd);
        } else {
            // 既未找到子元素，也未找到 "ref" 或 "value" 属性。
            error(elementName + " must specify a ref or value", ele);
            return null;
        }
    }

    /**
     * 解析属性或构造函数参数元素的值、引用或集合子元素。
     * @param ele 属性元素的子元素；目前尚不清楚是哪一种
     * @param bd 当前（如果有）的 Bean 定义
     */
    @Nullable
    public Object parsePropertySubElement(Element ele, @Nullable BeanDefinition bd) {
        return parsePropertySubElement(ele, bd, null);
    }

    /**
     * 解析属性或构造函数参数元素的一个值、引用或集合子元素。
     * @param ele 属性元素的子元素；我们目前不知道具体是哪种
     * @param bd 当前（如果有的话）的bean定义
     * @param defaultValueType 为可能创建的任何`<value>`标签指定的默认类型（类名）
     */
    @Nullable
    public Object parsePropertySubElement(Element ele, @Nullable BeanDefinition bd, @Nullable String defaultValueType) {
        if (!isDefaultNamespace(ele)) {
            return parseNestedCustomElement(ele, bd);
        } else if (nodeNameEquals(ele, BEAN_ELEMENT)) {
            BeanDefinitionHolder nestedBd = parseBeanDefinitionElement(ele, bd);
            if (nestedBd != null) {
                nestedBd = decorateBeanDefinitionIfRequired(ele, nestedBd, bd);
            }
            return nestedBd;
        } else if (nodeNameEquals(ele, REF_ELEMENT)) {
            // 任何bean名称的泛型引用。
            String refName = ele.getAttribute(BEAN_REF_ATTRIBUTE);
            boolean toParent = false;
            if (!StringUtils.hasLength(refName)) {
                // 指向父上下文中另一个 bean 的 id 的引用。
                refName = ele.getAttribute(PARENT_REF_ATTRIBUTE);
                toParent = true;
                if (!StringUtils.hasLength(refName)) {
                    error("'bean' or 'parent' is required for <ref> element", ele);
                    return null;
                }
            }
            if (!StringUtils.hasText(refName)) {
                error("<ref> element contains empty target attribute", ele);
                return null;
            }
            RuntimeBeanReference ref = new RuntimeBeanReference(refName, toParent);
            ref.setSource(extractSource(ele));
            return ref;
        } else if (nodeNameEquals(ele, IDREF_ELEMENT)) {
            return parseIdRefElement(ele);
        } else if (nodeNameEquals(ele, VALUE_ELEMENT)) {
            return parseValueElement(ele, defaultValueType);
        } else if (nodeNameEquals(ele, NULL_ELEMENT)) {
            // 它是一个特殊的空值。让我们将其封装在一个TypedStringValue中
            // 对象，以便保留源位置。
            TypedStringValue nullHolder = new TypedStringValue(null);
            nullHolder.setSource(extractSource(ele));
            return nullHolder;
        } else if (nodeNameEquals(ele, ARRAY_ELEMENT)) {
            return parseArrayElement(ele, bd);
        } else if (nodeNameEquals(ele, LIST_ELEMENT)) {
            return parseListElement(ele, bd);
        } else if (nodeNameEquals(ele, SET_ELEMENT)) {
            return parseSetElement(ele, bd);
        } else if (nodeNameEquals(ele, MAP_ELEMENT)) {
            return parseMapElement(ele, bd);
        } else if (nodeNameEquals(ele, PROPS_ELEMENT)) {
            return parsePropsElement(ele);
        } else {
            error("Unknown property sub-element: [" + ele.getNodeName() + "]", ele);
            return null;
        }
    }

    /**
     * 为给定的 'idref' 元素返回一个类型化的 String 值对象。
     */
    @Nullable
    public Object parseIdRefElement(Element ele) {
        // 对任何名称的任何bean的泛型引用。
        String refName = ele.getAttribute(BEAN_REF_ATTRIBUTE);
        if (!StringUtils.hasLength(refName)) {
            error("'bean' is required for <idref> element", ele);
            return null;
        }
        if (!StringUtils.hasText(refName)) {
            error("<idref> element contains empty target attribute", ele);
            return null;
        }
        RuntimeBeanNameReference ref = new RuntimeBeanNameReference(refName);
        ref.setSource(extractSource(ele));
        return ref;
    }

    /**
     * 为给定的值元素返回一个类型化的 String 值对象。
     */
    public Object parseValueElement(Element ele, @Nullable String defaultTypeName) {
        // 它是一个字面值。
        String value = DomUtils.getTextValue(ele);
        String specifiedTypeName = ele.getAttribute(TYPE_ATTRIBUTE);
        String typeName = specifiedTypeName;
        if (!StringUtils.hasText(typeName)) {
            typeName = defaultTypeName;
        }
        try {
            TypedStringValue typedValue = buildTypedStringValue(value, typeName);
            typedValue.setSource(extractSource(ele));
            typedValue.setSpecifiedTypeName(specifiedTypeName);
            return typedValue;
        } catch (ClassNotFoundException ex) {
            error("Type class [" + typeName + "] not found for <value> element", ele, ex);
            return value;
        }
    }

    /**
     * 为给定的原始值构建一个类型化的 String 值对象。
     * @see org.springframework.beans.factory.config.TypedStringValue
     */
    protected TypedStringValue buildTypedStringValue(String value, @Nullable String targetTypeName) throws ClassNotFoundException {
        ClassLoader classLoader = this.readerContext.getBeanClassLoader();
        TypedStringValue typedValue;
        if (!StringUtils.hasText(targetTypeName)) {
            typedValue = new TypedStringValue(value);
        } else if (classLoader != null) {
            Class<?> targetType = ClassUtils.forName(targetTypeName, classLoader);
            typedValue = new TypedStringValue(value, targetType);
        } else {
            typedValue = new TypedStringValue(value, targetTypeName);
        }
        return typedValue;
    }

    /**
     * 解析数组元素。
     */
    public Object parseArrayElement(Element arrayEle, @Nullable BeanDefinition bd) {
        String elementType = arrayEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
        NodeList nl = arrayEle.getChildNodes();
        ManagedArray target = new ManagedArray(elementType, nl.getLength());
        target.setSource(extractSource(arrayEle));
        target.setElementTypeName(elementType);
        target.setMergeEnabled(parseMergeAttribute(arrayEle));
        parseCollectionElements(nl, target, bd, elementType);
        return target;
    }

    /**
     * 解析列表元素。
     */
    public List<Object> parseListElement(Element collectionEle, @Nullable BeanDefinition bd) {
        String defaultElementType = collectionEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
        NodeList nl = collectionEle.getChildNodes();
        ManagedList<Object> target = new ManagedList<>(nl.getLength());
        target.setSource(extractSource(collectionEle));
        target.setElementTypeName(defaultElementType);
        target.setMergeEnabled(parseMergeAttribute(collectionEle));
        parseCollectionElements(nl, target, bd, defaultElementType);
        return target;
    }

    /**
     * 解析集合元素。
     */
    public Set<Object> parseSetElement(Element collectionEle, @Nullable BeanDefinition bd) {
        String defaultElementType = collectionEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
        NodeList nl = collectionEle.getChildNodes();
        ManagedSet<Object> target = new ManagedSet<>(nl.getLength());
        target.setSource(extractSource(collectionEle));
        target.setElementTypeName(defaultElementType);
        target.setMergeEnabled(parseMergeAttribute(collectionEle));
        parseCollectionElements(nl, target, bd, defaultElementType);
        return target;
    }

    protected void parseCollectionElements(NodeList elementNodes, Collection<Object> target, @Nullable BeanDefinition bd, String defaultElementType) {
        for (int i = 0; i < elementNodes.getLength(); i++) {
            Node node = elementNodes.item(i);
            if (node instanceof Element currentElement && !nodeNameEquals(node, DESCRIPTION_ELEMENT)) {
                target.add(parsePropertySubElement(currentElement, bd, defaultElementType));
            }
        }
    }

    /**
     * 解析一个地图元素。
     */
    public Map<Object, Object> parseMapElement(Element mapEle, @Nullable BeanDefinition bd) {
        String defaultKeyType = mapEle.getAttribute(KEY_TYPE_ATTRIBUTE);
        String defaultValueType = mapEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
        List<Element> entryEles = DomUtils.getChildElementsByTagName(mapEle, ENTRY_ELEMENT);
        ManagedMap<Object, Object> map = new ManagedMap<>(entryEles.size());
        map.setSource(extractSource(mapEle));
        map.setKeyTypeName(defaultKeyType);
        map.setValueTypeName(defaultValueType);
        map.setMergeEnabled(parseMergeAttribute(mapEle));
        for (Element entryEle : entryEles) {
            // 应仅有一个值子元素：ref、value、list等。
            // 可选地，可能存在一个键子元素。
            NodeList entrySubNodes = entryEle.getChildNodes();
            Element keyEle = null;
            Element valueEle = null;
            for (int j = 0; j < entrySubNodes.getLength(); j++) {
                Node node = entrySubNodes.item(j);
                if (node instanceof Element candidateEle) {
                    if (nodeNameEquals(candidateEle, KEY_ELEMENT)) {
                        if (keyEle != null) {
                            error("<entry> element is only allowed to contain one <key> sub-element", entryEle);
                        } else {
                            keyEle = candidateEle;
                        }
                    } else {
                        // 子元素是我们正在寻找的。
                        if (nodeNameEquals(candidateEle, DESCRIPTION_ELEMENT)) {
                            // 该元素是一个 <description> -> 忽略它
                        } else if (valueEle != null) {
                            error("<entry> element must not contain more than one value sub-element", entryEle);
                        } else {
                            valueEle = candidateEle;
                        }
                    }
                }
            }
            // 从属性或子元素中提取键。
            Object key = null;
            boolean hasKeyAttribute = entryEle.hasAttribute(KEY_ATTRIBUTE);
            boolean hasKeyRefAttribute = entryEle.hasAttribute(KEY_REF_ATTRIBUTE);
            if ((hasKeyAttribute && hasKeyRefAttribute) || (hasKeyAttribute || hasKeyRefAttribute) && keyEle != null) {
                error("<entry> element is only allowed to contain either " + "a 'key' attribute OR a 'key-ref' attribute OR a <key> sub-element", entryEle);
            }
            if (hasKeyAttribute) {
                key = buildTypedStringValueForMap(entryEle.getAttribute(KEY_ATTRIBUTE), defaultKeyType, entryEle);
            } else if (hasKeyRefAttribute) {
                String refName = entryEle.getAttribute(KEY_REF_ATTRIBUTE);
                if (!StringUtils.hasText(refName)) {
                    error("<entry> element contains empty 'key-ref' attribute", entryEle);
                }
                RuntimeBeanReference ref = new RuntimeBeanReference(refName);
                ref.setSource(extractSource(entryEle));
                key = ref;
            } else if (keyEle != null) {
                key = parseKeyElement(keyEle, bd, defaultKeyType);
            } else {
                error("<entry> element must specify a key", entryEle);
            }
            // 从属性或子元素中提取值。
            Object value = null;
            boolean hasValueAttribute = entryEle.hasAttribute(VALUE_ATTRIBUTE);
            boolean hasValueRefAttribute = entryEle.hasAttribute(VALUE_REF_ATTRIBUTE);
            boolean hasValueTypeAttribute = entryEle.hasAttribute(VALUE_TYPE_ATTRIBUTE);
            if ((hasValueAttribute && hasValueRefAttribute) || (hasValueAttribute || hasValueRefAttribute) && valueEle != null) {
                error("<entry> element is only allowed to contain either " + "'value' attribute OR 'value-ref' attribute OR <value> sub-element", entryEle);
            }
            if ((hasValueTypeAttribute && hasValueRefAttribute) || (hasValueTypeAttribute && !hasValueAttribute) || (hasValueTypeAttribute && valueEle != null)) {
                error("<entry> element is only allowed to contain a 'value-type' " + "attribute when it has a 'value' attribute", entryEle);
            }
            if (hasValueAttribute) {
                String valueType = entryEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
                if (!StringUtils.hasText(valueType)) {
                    valueType = defaultValueType;
                }
                value = buildTypedStringValueForMap(entryEle.getAttribute(VALUE_ATTRIBUTE), valueType, entryEle);
            } else if (hasValueRefAttribute) {
                String refName = entryEle.getAttribute(VALUE_REF_ATTRIBUTE);
                if (!StringUtils.hasText(refName)) {
                    error("<entry> element contains empty 'value-ref' attribute", entryEle);
                }
                RuntimeBeanReference ref = new RuntimeBeanReference(refName);
                ref.setSource(extractSource(entryEle));
                value = ref;
            } else if (valueEle != null) {
                value = parsePropertySubElement(valueEle, bd, defaultValueType);
            } else {
                error("<entry> element must specify a value", entryEle);
            }
            // 将最终的键和值添加到 Map 中。
            map.put(key, value);
        }
        return map;
    }

    /**
     * 为给定的原始值构建一个类型化的 String 值对象。
     * @see org.springframework.beans.factory.config.TypedStringValue
     */
    protected final Object buildTypedStringValueForMap(String value, String defaultTypeName, Element entryEle) {
        try {
            TypedStringValue typedValue = buildTypedStringValue(value, defaultTypeName);
            typedValue.setSource(extractSource(entryEle));
            return typedValue;
        } catch (ClassNotFoundException ex) {
            error("Type class [" + defaultTypeName + "] not found for Map key/value type", entryEle, ex);
            return value;
        }
    }

    /**
     * 解析地图元素的一个键子元素。
     */
    @Nullable
    protected Object parseKeyElement(Element keyEle, @Nullable BeanDefinition bd, String defaultKeyTypeName) {
        NodeList nl = keyEle.getChildNodes();
        Element subElement = null;
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element currentElement) {
                // 子元素就是我们正在寻找的。
                if (subElement != null) {
                    error("<key> element must not contain more than one value sub-element", keyEle);
                } else {
                    subElement = currentElement;
                }
            }
        }
        if (subElement == null) {
            return null;
        }
        return parsePropertySubElement(subElement, bd, defaultKeyTypeName);
    }

    /**
     * 解析 props 元素。
     */
    public Properties parsePropsElement(Element propsEle) {
        ManagedProperties props = new ManagedProperties();
        props.setSource(extractSource(propsEle));
        props.setMergeEnabled(parseMergeAttribute(propsEle));
        List<Element> propEles = DomUtils.getChildElementsByTagName(propsEle, PROP_ELEMENT);
        for (Element propEle : propEles) {
            String key = propEle.getAttribute(KEY_ATTRIBUTE);
            // 修剪文本值以避免不必要的空格
            // 由典型的 XML 格式问题引起。
            String value = DomUtils.getTextValue(propEle).trim();
            TypedStringValue keyHolder = new TypedStringValue(key);
            keyHolder.setSource(extractSource(propEle));
            TypedStringValue valueHolder = new TypedStringValue(value);
            valueHolder.setSource(extractSource(propEle));
            props.put(keyHolder, valueHolder);
        }
        return props;
    }

    /**
     * 解析集合元素的合并属性（如果存在）。
     */
    public boolean parseMergeAttribute(Element collectionElement) {
        String value = collectionElement.getAttribute(MERGE_ATTRIBUTE);
        if (isDefaultValue(value)) {
            value = this.defaults.getMerge();
        }
        return TRUE_VALUE.equals(value);
    }

    /**
     * 解析一个自定义元素（位于默认命名空间之外）。
     * @param ele 要解析的元素
     * @return 解析后的 Bean 定义
     */
    @Nullable
    public BeanDefinition parseCustomElement(Element ele) {
        return parseCustomElement(ele, null);
    }

    /**
     * 解析一个自定义元素（位于默认命名空间之外）。
     * @param ele 要解析的元素
     * @param containingBd 包含的 Bean 定义（如果有）
     * @return 结果 Bean 定义
     */
    @Nullable
    public BeanDefinition parseCustomElement(Element ele, @Nullable BeanDefinition containingBd) {
        String namespaceUri = getNamespaceURI(ele);
        if (namespaceUri == null) {
            return null;
        }
        NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
        if (handler == null) {
            error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", ele);
            return null;
        }
        return handler.parse(ele, new ParserContext(this.readerContext, this, containingBd));
    }

    /**
     * 如果适用，通过命名空间处理器装饰给定的 bean 定义。
     * @param ele 当前元素
     * @param originalDef 当前 bean 定义
     * @return 装饰后的 bean 定义
     */
    public BeanDefinitionHolder decorateBeanDefinitionIfRequired(Element ele, BeanDefinitionHolder originalDef) {
        return decorateBeanDefinitionIfRequired(ele, originalDef, null);
    }

    /**
     * 如果适用，通过命名空间处理程序装饰给定的bean定义。
     * @param ele 当前元素
     * @param originalDef 当前bean定义
     * @param containingBd 包含的bean定义（如果有的话）
     * @return 装饰后的bean定义
     */
    public BeanDefinitionHolder decorateBeanDefinitionIfRequired(Element ele, BeanDefinitionHolder originalDef, @Nullable BeanDefinition containingBd) {
        BeanDefinitionHolder finalDefinition = originalDef;
        // 首先根据自定义属性进行装饰。
        NamedNodeMap attributes = ele.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            finalDefinition = decorateIfRequired(node, finalDefinition, containingBd);
        }
        // 基于自定义嵌套元素进行装饰。
        NodeList children = ele.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                finalDefinition = decorateIfRequired(node, finalDefinition, containingBd);
            }
        }
        return finalDefinition;
    }

    /**
     * 通过命名空间处理器装饰给定的Bean定义（如果适用）。
     * @param node 当前子节点
     * @param originalDef 当前Bean定义
     * @param containingBd 包含的Bean定义（如果有的话）
     * @return 装饰后的Bean定义
     */
    public BeanDefinitionHolder decorateIfRequired(Node node, BeanDefinitionHolder originalDef, @Nullable BeanDefinition containingBd) {
        String namespaceUri = getNamespaceURI(node);
        if (namespaceUri != null && !isDefaultNamespace(namespaceUri)) {
            NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
            if (handler != null) {
                BeanDefinitionHolder decorated = handler.decorate(node, originalDef, new ParserContext(this.readerContext, this, containingBd));
                if (decorated != null) {
                    return decorated;
                }
            } else if (namespaceUri.startsWith("http://www.springframework.org/schema/")) {
                error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", node);
            } else {
                // 自定义命名空间，不应由 Spring 处理 - 可能是 "xml:..."。
                if (logger.isDebugEnabled()) {
                    logger.debug("No Spring NamespaceHandler found for XML schema namespace [" + namespaceUri + "]");
                }
            }
        }
        return originalDef;
    }

    @Nullable
    private BeanDefinitionHolder parseNestedCustomElement(Element ele, @Nullable BeanDefinition containingBd) {
        BeanDefinition innerDefinition = parseCustomElement(ele, containingBd);
        if (innerDefinition == null) {
            error("Incorrect usage of element '" + ele.getNodeName() + "' in a nested manner. " + "This tag cannot be used nested inside <property>.", ele);
            return null;
        }
        String id = ele.getNodeName() + BeanDefinitionReaderUtils.GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(innerDefinition);
        if (logger.isTraceEnabled()) {
            logger.trace("Using generated bean name [" + id + "] for nested custom element '" + ele.getNodeName() + "'");
        }
        return new BeanDefinitionHolder(innerDefinition, id);
    }

    /**
     * 获取指定节点的命名空间URI。
     * <p>默认实现使用 {@link Node#getNamespaceURI}。
     * 子类可以覆盖默认实现，以提供不同的命名空间识别机制。
     * @param node 节点
     */
    @Nullable
    public String getNamespaceURI(Node node) {
        return node.getNamespaceURI();
    }

    /**
     * 获取给定 {@link Node} 的本地名称。
     * <p>默认实现调用 {@link Node#getLocalName}。
     * 子类可以覆盖默认实现，以提供获取本地名称的不同机制。
     * @param node 给定的 {@code Node}
     */
    public String getLocalName(Node node) {
        return node.getLocalName();
    }

    /**
     * 判断提供的节点名称是否与提供的名称相等。
     * <p>默认实现将提供的期望名称与{@link Node#getNodeName()}和{@link Node#getLocalName()}进行比较。
     * <p>子类可以覆盖默认实现，以提供不同的机制来比较节点名称。
     * @param node 要比较的节点
     * @param desiredName 要检查的名称
     */
    public boolean nodeNameEquals(Node node, String desiredName) {
        return desiredName.equals(node.getNodeName()) || desiredName.equals(getLocalName(node));
    }

    /**
     * 判断给定的URI是否指示默认命名空间。
     */
    public boolean isDefaultNamespace(@Nullable String namespaceUri) {
        return !StringUtils.hasLength(namespaceUri) || BEANS_NAMESPACE_URI.equals(namespaceUri);
    }

    /**
     * 判断给定的节点是否指示默认命名空间。
     */
    public boolean isDefaultNamespace(Node node) {
        return isDefaultNamespace(getNamespaceURI(node));
    }

    private boolean isDefaultValue(String value) {
        return !StringUtils.hasLength(value) || DEFAULT_VALUE.equals(value);
    }

    private boolean isCandidateElement(Node node) {
        return (node instanceof Element && (isDefaultNamespace(node) || !isDefaultNamespace(node.getParentNode())));
    }
}
