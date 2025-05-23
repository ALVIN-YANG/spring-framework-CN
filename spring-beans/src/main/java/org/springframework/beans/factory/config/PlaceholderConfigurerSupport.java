// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式（明示或暗示）的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

/**
 * 属性资源配置器的抽象基类，用于解析在 Bean 定义属性值中的占位符。实现类<em>拉取</em>来自属性文件或其他 {@linkplain org.springframework.core.env.PropertySource 属性源} 的值到 Bean 定义中。
 *
 * <p>默认的占位符语法遵循 Ant / Log4J / JSP EL 风格：
 *
 * <pre class="code">${...}</pre>
 *
 * 示例 XML Bean 定义：
 *
 * <pre class="code">
 * &lt;bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource"&gt;
 *   &lt;property name="driverClassName" value="${driver}" /&gt;
 *   &lt;property name="url" value="jdbc:${dbname}" /&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * 示例属性文件：
 *
 * <pre class="code">
 * driver=com.mysql.jdbc.Driver
 * dbname=mysql:mydb</pre>
 *
 * 带注解的 Bean 定义可以利用 {@link org.springframework.beans.factory.annotation.Value @Value} 注解进行属性替换：
 *
 * <pre class="code">@Value("${person.age}")</pre>
 *
 * 实现类会检查简单的属性值、列表、映射、属性和 Bean 引用中的 Bean 名称。此外，占位符值还可以交叉引用其他占位符，例如：
 *
 * <pre class="code">
 * rootPath=myrootdir
 * subPath=${rootPath}/subdir</pre>
 *
 * 与 {@link PropertyOverrideConfigurer} 相比，此类子类允许在 Bean 定义中填充显式的占位符。
 *
 * <p>如果配置器无法解析占位符，将抛出 {@link BeanDefinitionStoreException} 异常。如果您想针对多个属性文件进行检查，请通过 {@link #setLocations 位置} 属性指定多个资源。您还可以定义多个配置器，每个配置器都有其 <em>自己的</em> 占位符语法。使用 {@link #ignoreUnresolvablePlaceholders} 有意地抑制在占位符无法解析时抛出异常。
 *
 * <p>可以通过 {@link #setProperties 属性} 属性为每个配置器实例定义默认属性值，或根据属性逐个定义，默认的分隔符为 {@code ":"}，可通过 {@link #setValueSeparator(String)} 进行自定义。
 *
 * <p>示例 XML 属性带有默认值：
 *
 * <pre class="code">
 *   &lt;property name="url" value="jdbc:${dbname:defaultdb}" /&gt;
 * </pre>
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see PropertyPlaceholderConfigurer
 * @see org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 */
public abstract class PlaceholderConfigurerSupport extends PropertyResourceConfigurer implements BeanNameAware, BeanFactoryAware {

    /**
     * 默认占位符前缀: {@value}。
     */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

    /**
     * 默认占位符后缀：{@value}。
     */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    /**
     * 默认值分隔符：{@value}。
     */
    public static final String DEFAULT_VALUE_SEPARATOR = ":";

    /**
     * 默认为 {@value #DEFAULT_PLACEHOLDER_PREFIX}。
     */
    protected String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

    /**
     * 默认为 {@value #DEFAULT_PLACEHOLDER_SUFFIX}。
     */
    protected String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

    /**
     * 默认为 {@value #DEFAULT_VALUE_SEPARATOR}。
     */
    @Nullable
    protected String valueSeparator = DEFAULT_VALUE_SEPARATOR;

    protected boolean trimValues = false;

    @Nullable
    protected String nullValue;

    protected boolean ignoreUnresolvablePlaceholders = false;

    @Nullable
    private String beanName;

    @Nullable
    private BeanFactory beanFactory;

    /**
     * 设置占位符字符串开始的前缀。
     * 默认值为 {@value #DEFAULT_PLACEHOLDER_PREFIX}。
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * 设置占位符字符串结尾的后缀。
     * 默认值为 {@value #DEFAULT_PLACEHOLDER_SUFFIX}。
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * 指定占位符变量与关联的默认值之间的分隔符，或者如果不需要处理此类特殊字符作为值分隔符，则为 {@code null}。默认为 {@value #DEFAULT_VALUE_SEPARATOR}。
     */
    public void setValueSeparator(@Nullable String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    /**
     * 指定在应用解析的值之前是否要对其进行修剪，
     * 从开头和结尾移除多余的空白字符。
     * <p>默认值为 {@code false}。
     * @since 4.3
     */
    public void setTrimValues(boolean trimValues) {
        this.trimValues = trimValues;
    }

    /**
     * 设置一个值，当解析为占位符时，应将其视为 {@code null}：例如 ""（空字符串）或 "null"。
     * <p>请注意，这仅适用于完整的属性值，不适用于拼接值的各个部分。
     * <p>默认情况下，没有定义这样的空值。这意味着除非您在此处显式映射相应的值，否则无法用属性值表示 {@code null}。
     */
    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    /**
     * 设置是否忽略无法解析的占位符。
     * <p>默认为 "false"：如果占位符无法解析，将抛出异常。将此标志切换为 "true" 以保留在这种情况下占位符字符串不变，并将其留给其他占位符配置器去解析。
     */
    public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    /**
     * 只需检查我们不是在解析自己的Bean定义，
     * 以避免在属性文件位置上出现无法解析的占位符时失败。
     * 后者情况可能出现在资源位置中的系统属性占位符中。
     * @see #setLocations
     * @see org.springframework.core.io.ResourceEditor
     */
    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * 只需检查我们不是在解析自己的Bean定义，
     * 以避免在属性文件位置上出现无法解析的占位符时失败。
     * 后者情况可能发生在资源位置中系统属性的占位符。
     * @see #setLocations
     * @see org.springframework.core.io.ResourceEditor
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess, StringValueResolver valueResolver) {
        BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);
        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        for (String curName : beanNames) {
            // 检查我们是否正在解析自己的bean定义，
            // 为了避免在属性文件位置上因无法解析的占位符而失败。
            if (!(curName.equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
                BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(curName);
                try {
                    visitor.visitBeanDefinition(bd);
                } catch (Exception ex) {
                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex.getMessage(), ex);
                }
            }
        }
        // 解析别名目标名称中的占位符以及别名本身。
        beanFactoryToProcess.resolveAliases(valueResolver);
        // 解析嵌入值中的占位符，例如注解属性。
        beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
    }
}
