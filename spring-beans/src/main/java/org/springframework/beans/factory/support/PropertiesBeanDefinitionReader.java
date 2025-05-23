// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途的适用性。
* 请参阅许可证以了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.factory.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.lang.Nullable;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.StringUtils;

/**
 * 简单属性格式的 Bean 定义读取器。
 *
 * <p>为 Map/Properties 和 ResourceBundle 提供了 Bean 定义注册方法。通常应用于 DefaultListableBeanFactory。
 *
 * <p><b>示例：</b>
 *
 * <pre class="code">
 * employee.(class)=MyClass       // Bean 的类为 MyClass
 * employee.(abstract)=true       // 此 Bean 不能直接实例化
 * employee.group=Insurance       // 实际属性
 * employee.usesDialUp=false      // 实际属性（可能被覆盖）
 *
 * salesrep.(parent)=employee     // 从 "employee" Bean 定义继承
 * salesrep.(lazy-init)=true      // 懒加载此单例 Bean
 * salesrep.manager(ref)=tony     // 对另一个 Bean 的引用
 * salesrep.department=Sales      // 实际属性
 *
 * techie.(parent)=employee       // 从 "employee" Bean 定义继承
 * techie.(scope)=prototype       // Bean 是原型（不是共享实例）
 * techie.manager(ref)=jeff       // 对另一个 Bean 的引用
 * techie.department=Engineering  // 实际属性
 * techie.usesDialUp=true         // 实际属性（覆盖父级值）
 *
 * ceo.$0(ref)=secretary          // 将 'secretary' Bean 注入为 0 个构造函数参数
 * ceo.$1=1000000                 // 在 1 个构造函数参数位置注入值 '1000000'
 * </pre>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 26.11.2003
 * @see DefaultListableBeanFactory
 * @deprecated as of 5.3, in favor of Spring's common bean definition formats
 * and/or custom reader implementations
 */
@Deprecated
public class PropertiesBeanDefinitionReader extends AbstractBeanDefinitionReader {

    /**
     * 表示“真”的 T/F 属性的值。
     * 任何其他值表示“假”。区分大小写。
     */
    public static final String TRUE_VALUE = "true";

    /**
     * 分隔 bean 名称和属性名称的符号。
     * 我们遵循正常的 Java 习惯用法。
     */
    public static final String SEPARATOR = ".";

    /**
     * 专门的关键字来区分 {@code owner.(类)=com.myapp.MyClass}。
     */
    public static final String CLASS_KEY = "(class)";

    /**
     * 特殊键以区分 {@code owner.(parent)=parentBeanName}。
     */
    public static final String PARENT_KEY = "(parent)";

    /**
     * 特殊键用于区分 {@code owner.(范围)=原型}。
     * 默认值为 "true"。
     */
    public static final String SCOPE_KEY = "(scope)";

    /**
     * 用于区分 {@code owner.(singleton)=false} 的特殊键。
     * 默认值为 "true"。
     */
    public static final String SINGLETON_KEY = "(singleton)";

    /**
     * 特殊键以区分 {@code owner.(抽象)=true}
     * 默认值为 "false"。
     */
    public static final String ABSTRACT_KEY = "(abstract)";

    /**
     * 特殊键用于区分 {@code owner.(lazy-init)=true}
     * 默认值为 "false"。
     */
    public static final String LAZY_INIT_KEY = "(lazy-init)";

    /**
     * 当前 BeanFactory 中对其他 Bean 的属性后缀：例如，`{@code owner.dog(ref)=fido}`。
     * 这是否是对单例（singleton）还是原型（prototype）的引用将取决于目标 Bean 的定义。
     */
    public static final String REF_SUFFIX = "(ref)";

    /**
     * 在引用其他 Bean 的值之前的前缀。
     */
    public static final String REF_PREFIX = "*";

    /**
     * 用于表示构造函数参数定义的前缀。
     */
    public static final String CONSTRUCTOR_ARG_PREFIX = "$";

    @Nullable
    private String defaultParentBean;

    private PropertiesPersister propertiesPersister = DefaultPropertiesPersister.INSTANCE;

    /**
     * 为给定的bean工厂创建一个新的PropertiesBeanDefinitionReader。
     * @param registry 要将bean定义加载到其中的BeanFactory，形式为一个BeanDefinitionRegistry
     */
    public PropertiesBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
    }

    /**
     * 为此 Bean 工厂设置默认的父 Bean。
     * 如果由该工厂处理的子 Bean 定义既没有提供父 Bean 也没有提供类属性，则使用此默认值。
     * <p>例如，可用于视图定义文件，为所有视图定义一个默认视图类和公共属性。
     * 定义了自己的父 Bean 或携带自己类的视图定义仍然可以覆盖此设置。
     * <p>严格来说，默认父设置不适用于携带类属性的 Bean 定义是为了向后兼容性原因。它仍然符合典型的使用场景。
     */
    public void setDefaultParentBean(@Nullable String defaultParentBean) {
        this.defaultParentBean = defaultParentBean;
    }

    /**
     * 返回此bean工厂的默认父bean。
     */
    @Nullable
    public String getDefaultParentBean() {
        return this.defaultParentBean;
    }

    /**
     * 设置用于解析属性文件的 PropertiesPersister。
     * 默认为 {@code DefaultPropertiesPersister}。
     * @see DefaultPropertiesPersister#INSTANCE
     */
    public void setPropertiesPersister(@Nullable PropertiesPersister propertiesPersister) {
        this.propertiesPersister = (propertiesPersister != null ? propertiesPersister : DefaultPropertiesPersister.INSTANCE);
    }

    /**
     * 返回用于解析属性文件的 PropertiesPersister。
     */
    public PropertiesPersister getPropertiesPersister() {
        return this.propertiesPersister;
    }

    /**
     * 从指定的属性文件中加载Bean定义，
     * 使用所有属性键（即不按前缀过滤）。
     * @param resource 属性文件的资源描述符
     * @return 找到的Bean定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下抛出
     * @see #loadBeanDefinitions(org.springframework.core.io.Resource, String)
     */
    @Override
    public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
        return loadBeanDefinitions(new EncodedResource(resource), null);
    }

    /**
     * 从指定的属性文件中加载 Bean 定义。
     * @param resource 属性文件的资源描述符
     * @param prefix 在映射的键中的过滤器：例如 'beans.'
     * （可以为空或 {@code null}）
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下
     */
    public int loadBeanDefinitions(Resource resource, @Nullable String prefix) throws BeanDefinitionStoreException {
        return loadBeanDefinitions(new EncodedResource(resource), prefix);
    }

    /**
     * 从指定的属性文件中加载 Bean 定义。
     * @param encodedResource 属性文件的资源描述符，允许指定用于解析文件的编码
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下抛出异常
     */
    public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
        return loadBeanDefinitions(encodedResource, null);
    }

    /**
     * 从指定的属性文件中加载 Bean 定义。
     * @param encodedResource 属性文件的资源描述符，允许指定用于解析文件的编码
     * @param prefix 地图键中的过滤器：例如 'beans.'
     * （可以为空或 {@code null}）
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下
     */
    public int loadBeanDefinitions(EncodedResource encodedResource, @Nullable String prefix) throws BeanDefinitionStoreException {
        if (logger.isTraceEnabled()) {
            logger.trace("Loading properties bean definitions from " + encodedResource);
        }
        Properties props = new Properties();
        try {
            try (InputStream is = encodedResource.getResource().getInputStream()) {
                if (encodedResource.getEncoding() != null) {
                    getPropertiesPersister().load(props, new InputStreamReader(is, encodedResource.getEncoding()));
                } else {
                    getPropertiesPersister().load(props, is);
                }
            }
            int count = registerBeanDefinitions(props, prefix, encodedResource.getResource().getDescription());
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded " + count + " bean definitions from " + encodedResource);
            }
            return count;
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("Could not parse properties from " + encodedResource.getResource(), ex);
        }
    }

    /**
     * 注册包含在资源包中的bean定义，
     * 使用所有属性键（即不按前缀过滤）。
     * @param rb 要从中加载的ResourceBundle
     * @return 找到的bean定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下
     * @see #registerBeanDefinitions(java.util.ResourceBundle, String)
     */
    public int registerBeanDefinitions(ResourceBundle rb) throws BeanDefinitionStoreException {
        return registerBeanDefinitions(rb, null);
    }

    /**
     * 注册包含在 ResourceBundle 中的 bean 定义。
     * <p>语法与 Map 类似。此方法很有用，可以启用标准的 Java 国际化支持。
     * @param rb 要从中加载的 ResourceBundle
     * @param prefix 用于过滤 map 中的键的一个前缀：例如 'beans.'
     * （可以为空或为 null）
     * @return 找到的 bean 定义的数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下
     */
    public int registerBeanDefinitions(ResourceBundle rb, @Nullable String prefix) throws BeanDefinitionStoreException {
        // 只需创建一个映射并调用重载的方法。
        Map<String, Object> map = new HashMap<>();
        Enumeration<String> keys = rb.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key, rb.getObject(key));
        }
        return registerBeanDefinitions(map, prefix);
    }

    /**
     * 注册包含在Map中的Bean定义，使用所有属性键（即不按前缀过滤）。
     * @param map 一个以{@code name}到{@code property}（String或Object）的Map。如果来自属性文件等，属性值将是字符串。属性名称（键）<b>必须</b>是String。类键必须是String。
     * @return 找到的Bean定义数量
     * @throws BeansException 如果在加载或解析过程中发生错误
     * @see #registerBeanDefinitions(java.util.Map, String, String)
     */
    public int registerBeanDefinitions(Map<?, ?> map) throws BeansException {
        return registerBeanDefinitions(map, null);
    }

    /**
     * 注册包含在 Map 中的 Bean 定义。
     * 忽略不合格的属性。
     * @param map 一个包含 {@code name} 到 {@code property}（String 或 Object）的 Map。如果来自 Properties 文件等，属性值将是字符串。属性名称（键）<b>必须</b>是字符串。类键必须是字符串。
     * @param prefix 一个过滤 Map 中的键：例如 'beans.'（可以是空的或 {@code null}）
     * @return 找到的 Bean 定义数量
     * @throws BeansException 在加载或解析错误的情况下
     */
    public int registerBeanDefinitions(Map<?, ?> map, @Nullable String prefix) throws BeansException {
        return registerBeanDefinitions(map, prefix, "Map " + map);
    }

    /**
     * 注册包含在Map中的bean定义。
     * 忽略不合格的属性。
     * @param map 一个从 {@code name} 到 {@code property}（字符串或对象）的映射。如果来自属性文件等，属性值将是字符串。属性名（键）<b>必须</b>是字符串。类键必须是字符串。
     * @param prefix 映射中键的过滤器：例如 'beans.'（可以为空或为 {@code null}）
     * @param resourceDescription 描述Map来自的资源（用于日志记录）
     * @return 找到的bean定义数量
     * @throws BeansException 在加载或解析错误的情况下
     * @see #registerBeanDefinitions(Map, String)
     */
    public int registerBeanDefinitions(Map<?, ?> map, @Nullable String prefix, String resourceDescription) throws BeansException {
        if (prefix == null) {
            prefix = "";
        }
        int beanCount = 0;
        for (Object key : map.keySet()) {
            if (!(key instanceof String keyString)) {
                throw new IllegalArgumentException("Illegal key [" + key + "]: only Strings allowed");
            }
            if (keyString.startsWith(prefix)) {
                // 键的形式为：前缀<名称>.属性
                String nameAndProperty = keyString.substring(prefix.length());
                // 在属性名前找到点，忽略属性键中的点。
                int sepIdx;
                int propKeyIdx = nameAndProperty.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX);
                if (propKeyIdx != -1) {
                    sepIdx = nameAndProperty.lastIndexOf(SEPARATOR, propKeyIdx);
                } else {
                    sepIdx = nameAndProperty.lastIndexOf(SEPARATOR);
                }
                if (sepIdx != -1) {
                    String beanName = nameAndProperty.substring(0, sepIdx);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Found bean name '" + beanName + "'");
                    }
                    if (!getRegistry().containsBeanDefinition(beanName)) {
                        // 如果我们还没有注册它...
                        registerBeanDefinition(beanName, map, prefix + beanName, resourceDescription);
                        ++beanCount;
                    }
                } else {
                    // 忽略它：这不是一个有效的Bean名称和属性。
                    // 尽管它确实以所需的前缀开始。
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invalid bean name and property [" + nameAndProperty + "]");
                    }
                }
            }
        }
        return beanCount;
    }

    /**
     * 获取所有带有指定前缀（将被去除）的属性值，并将定义的bean添加到工厂中，使用给定的名称。
     * @param beanName 要定义的bean的名称
     * @param map 包含字符串对的Map
     * @param prefix 每个条目的前缀，将被去除
     * @param resourceDescription Map来源的资源描述（用于日志记录）
     * @throws BeansException 如果无法解析或注册bean定义
     */
    protected void registerBeanDefinition(String beanName, Map<?, ?> map, String prefix, String resourceDescription) throws BeansException {
        String className = null;
        String parent = null;
        String scope = BeanDefinition.SCOPE_SINGLETON;
        boolean isAbstract = false;
        boolean lazyInit = false;
        ConstructorArgumentValues cas = new ConstructorArgumentValues();
        MutablePropertyValues pvs = new MutablePropertyValues();
        String prefixWithSep = prefix + SEPARATOR;
        int beginIndex = prefixWithSep.length();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = ((String) entry.getKey()).strip();
            if (key.startsWith(prefixWithSep)) {
                String property = key.substring(beginIndex);
                if (CLASS_KEY.equals(property)) {
                    className = ((String) entry.getValue()).strip();
                } else if (PARENT_KEY.equals(property)) {
                    parent = ((String) entry.getValue()).strip();
                } else if (ABSTRACT_KEY.equals(property)) {
                    String val = ((String) entry.getValue()).strip();
                    isAbstract = TRUE_VALUE.equals(val);
                } else if (SCOPE_KEY.equals(property)) {
                    // Spring 2.0 风格
                    scope = ((String) entry.getValue()).strip();
                } else if (SINGLETON_KEY.equals(property)) {
                    // Spring 1.2 风格
                    String val = ((String) entry.getValue()).strip();
                    scope = (!StringUtils.hasLength(val) || TRUE_VALUE.equals(val) ? BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
                } else if (LAZY_INIT_KEY.equals(property)) {
                    String val = ((String) entry.getValue()).strip();
                    lazyInit = TRUE_VALUE.equals(val);
                } else if (property.startsWith(CONSTRUCTOR_ARG_PREFIX)) {
                    if (property.endsWith(REF_SUFFIX)) {
                        int index = Integer.parseInt(property, 1, property.length() - REF_SUFFIX.length(), 10);
                        cas.addIndexedArgumentValue(index, new RuntimeBeanReference(entry.getValue().toString()));
                    } else {
                        int index = Integer.parseInt(property, 1, property.length(), 10);
                        cas.addIndexedArgumentValue(index, readValue(entry));
                    }
                } else if (property.endsWith(REF_SUFFIX)) {
                    // 这不是一个真正的属性，而是一个指向另一个原型的引用
                    // 提取属性名称：属性形式为dog(ref)
                    property = property.substring(0, property.length() - REF_SUFFIX.length());
                    String ref = ((String) entry.getValue()).strip();
                    // 无论所引用的 Bean 是否已被注册，这都没有关系：
                    // 这将确保在运行时解析引用。
                    Object val = new RuntimeBeanReference(ref);
                    pvs.add(property, val);
                } else {
                    // 这是一个普通的 Bean 属性。
                    pvs.add(property, readValue(entry));
                }
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Registering bean definition for bean name '" + beanName + "' with " + pvs);
        }
        // 如果处理的不是父对象本身，就使用默认的父对象。
        // 如果没有指定类名。后者必须发生，因为
        // 向后兼容性原因。
        if (parent == null && className == null && !beanName.equals(this.defaultParentBean)) {
            parent = this.defaultParentBean;
        }
        try {
            AbstractBeanDefinition bd = BeanDefinitionReaderUtils.createBeanDefinition(parent, className, getBeanClassLoader());
            bd.setScope(scope);
            bd.setAbstract(isAbstract);
            bd.setLazyInit(lazyInit);
            bd.setConstructorArgumentValues(cas);
            bd.setPropertyValues(pvs);
            getRegistry().registerBeanDefinition(beanName, bd);
        } catch (ClassNotFoundException ex) {
            throw new CannotLoadBeanClassException(resourceDescription, beanName, className, ex);
        } catch (LinkageError err) {
            throw new CannotLoadBeanClassException(resourceDescription, beanName, className, err);
        }
    }

    /**
     * 读取条目的值。正确解析以星号开头的前缀值为bean引用。
     */
    private Object readValue(Map.Entry<?, ?> entry) {
        Object val = entry.getValue();
        if (val instanceof String strVal) {
            // 如果它以引用前缀开头...
            if (strVal.startsWith(REF_PREFIX)) {
                // 扩展引用。
                String targetName = strVal.substring(1);
                if (targetName.startsWith(REF_PREFIX)) {
                    // 转义前缀 -> 使用普通值。
                    val = targetName;
                } else {
                    val = new RuntimeBeanReference(targetName);
                }
            }
        }
        return val;
    }
}
