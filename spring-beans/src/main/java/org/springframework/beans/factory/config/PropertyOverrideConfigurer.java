// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.config;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanInitializationException;

/**
 * 属性资源配置器，用于覆盖应用程序上下文定义中的bean属性值。它将属性文件中的值<i>推入</i>bean定义中。
 *
 * <p>期望配置行具有以下形式：
 *
 * <pre class="code">beanName.property=value</pre>
 *
 * 示例属性文件：
 *
 * <pre class="code">dataSource.driverClassName=com.mysql.jdbc.Driver
 * dataSource.url=jdbc:mysql:mydb</pre>
 *
 * 与PropertyPlaceholderConfigurer不同，原始定义可以为这样的bean属性提供默认值或根本不提供值。如果覆盖属性文件没有为某个bean属性提供条目，则使用默认上下文定义。
 *
 * <p>请注意，上下文定义<i>不</i>知道已被覆盖；因此，在查看XML定义文件时，这并不立即明显。此外，请注意，指定的覆盖值始终是<i>字面</i>值；它们不会被转换为bean引用。这也适用于当XML bean定义中的原始值指定了一个bean引用时。
 *
 * <p>如果有多个PropertyOverrideConfigurers定义了同一bean属性的不同的值，则<i>最后一个</i>定义的值将生效（由于覆盖机制）。
 *
 * <p>在读取属性值之后，可以通过覆盖`convertPropertyValue`方法将属性值进行转换。例如，可以检测加密的值并相应地解密，然后再进行处理。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 12.03.2003
 * @see #convertPropertyValue
 * @see PropertyPlaceholderConfigurer
 */
public class PropertyOverrideConfigurer extends PropertyResourceConfigurer {

    /**
     * 默认的 Bean 名称分隔符。
     */
    public static final String DEFAULT_BEAN_NAME_SEPARATOR = ".";

    private String beanNameSeparator = DEFAULT_BEAN_NAME_SEPARATOR;

    private boolean ignoreInvalidKeys = false;

    /**
     * 包含具有重写方法的 Bean 名称。
     */
    private final Set<String> beanNames = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /**
     * 设置分隔符，用于分隔 Bean 名称和属性路径。
     * 默认为点号 (".")。
     */
    public void setBeanNameSeparator(String beanNameSeparator) {
        this.beanNameSeparator = beanNameSeparator;
    }

    /**
     * 设置是否忽略无效键。默认值为 "false"。
     * <p>如果忽略无效键，则不符合 'beanName.property' 格式（或引用无效的bean名称或属性）的键将只以调试级别进行记录。
     * 这允许在属性文件中存在任意其他键。
     */
    public void setIgnoreInvalidKeys(boolean ignoreInvalidKeys) {
        this.ignoreInvalidKeys = ignoreInvalidKeys;
    }

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
        for (Enumeration<?> names = props.propertyNames(); names.hasMoreElements(); ) {
            String key = (String) names.nextElement();
            try {
                processKey(beanFactory, key, props.getProperty(key));
            } catch (BeansException ex) {
                String msg = "Could not process key '" + key + "' in PropertyOverrideConfigurer";
                if (!this.ignoreInvalidKeys) {
                    throw new BeanInitializationException(msg, ex);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(msg, ex);
                }
            }
        }
    }

    /**
     * 处理给定的键作为 'beanName.property' 条目。
     */
    protected void processKey(ConfigurableListableBeanFactory factory, String key, String value) throws BeansException {
        int separatorIndex = key.indexOf(this.beanNameSeparator);
        if (separatorIndex == -1) {
            throw new BeanInitializationException("Invalid key '" + key + "': expected 'beanName" + this.beanNameSeparator + "property'");
        }
        String beanName = key.substring(0, separatorIndex);
        String beanProperty = key.substring(separatorIndex + 1);
        this.beanNames.add(beanName);
        applyPropertyValue(factory, beanName, beanProperty, value);
        if (logger.isDebugEnabled()) {
            logger.debug("Property '" + key + "' set to value [" + value + "]");
        }
    }

    /**
     * 将给定的属性值应用到相应的 Bean 上。
     */
    protected void applyPropertyValue(ConfigurableListableBeanFactory factory, String beanName, String property, String value) {
        BeanDefinition bd = factory.getBeanDefinition(beanName);
        BeanDefinition bdToUse = bd;
        while (bd != null) {
            bdToUse = bd;
            bd = bd.getOriginatingBeanDefinition();
        }
        PropertyValue pv = new PropertyValue(property, value);
        pv.setOptional(this.ignoreInvalidKeys);
        bdToUse.getPropertyValues().addPropertyValue(pv);
    }

    /**
     * 这个Bean有覆盖吗？
     * 仅在至少执行过一次处理之后有效。
     * @param beanName 要查询状态的Bean名称
     * @return 是否存在对指定Bean的属性覆盖
     */
    public boolean hasPropertyOverridesFor(String beanName) {
        return this.beanNames.contains(beanName);
    }
}
