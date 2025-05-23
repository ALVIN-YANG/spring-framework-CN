// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.util.ObjectUtils;

/**
 * 允许从属性资源（例如属性文件）配置单个bean属性值，
 * 即，以“beanName.property=value”样式进行覆盖（<i>将值从属性文件推送到bean定义</i>）。
 * 对于针对系统管理员且覆盖应用程序上下文中配置的bean属性的定制配置文件非常有用。
 *
 * <p>在分发中提供了两种具体的实现：
 * <ul>
 * <li>{@link PropertyOverrideConfigurer} 用于“beanName.property=value”样式的覆盖（<i>将值从属性文件推送到bean定义</i>）
 * <li>{@link PropertyPlaceholderConfigurer} 用于替换 "${...}" 占位符（<i>从属性文件拉取值到bean定义</i>）
 * </ul>
 *
 * <p>可以通过覆盖 {@link #convertPropertyValue} 方法在读取属性值之后进行转换。例如，可以检测并相应地解密加密值，然后再进行处理。
 *
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 * @see PropertyPlaceholderConfigurer
 */
public abstract class PropertyResourceConfigurer extends PropertiesLoaderSupport implements BeanFactoryPostProcessor, PriorityOrdered {

    // 默认情况下：与非有序相同
    private int order = Ordered.LOWEST_PRECEDENCE;

    /**
     * 设置此对象的排序值，用于排序目的。
     * @see PriorityOrdered
     */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * 将属性与给定的bean工厂进行合并（Merge）、转换（Convert）和处理（Process）。
     * 如果有任何属性无法加载，则抛出BeanInitializationException异常。
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            Properties mergedProps = mergeProperties();
            // 如有必要，转换合并后的属性。
            convertProperties(mergedProps);
            // 让子类处理属性。
            processProperties(beanFactory, mergedProps);
        } catch (IOException ex) {
            throw new BeanInitializationException("Could not load properties: " + ex.getMessage(), ex);
        }
    }

    /**
     * 将给定的合并属性进行转换，如果需要的话。转换后的结果将被处理。
     * <p>默认实现将调用 {@link #convertPropertyValue} 对每个属性值进行转换，用转换后的值替换原始值。
     * @param props 要转换的 Properties 对象
     * @see #processProperties
     */
    protected void convertProperties(Properties props) {
        Enumeration<?> propertyNames = props.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            String propertyValue = props.getProperty(propertyName);
            String convertedValue = convertProperty(propertyName, propertyValue);
            if (!ObjectUtils.nullSafeEquals(propertyValue, convertedValue)) {
                props.setProperty(propertyName, convertedValue);
            }
        }
    }

    /**
     * 将给定的属性从属性源转换为应应用的价值
     * 默认实现调用 {@link #convertPropertyValue(String)}。
     * @param propertyName 属性名称，值为该属性定义
     * @param propertyValue 来自属性源的原始值
     * @return 转换后的值，用于处理
     * @see #convertPropertyValue(String)
     */
    protected String convertProperty(String propertyName, String propertyValue) {
        return convertPropertyValue(propertyValue);
    }

    /**
     * 将给定的属性值从属性源转换为应应用的值。
     * <p>默认实现简单地返回原始值。
     * 可以在子类中重写，例如用于检测加密值并相应地解密。
     * @param originalValue 从属性源（属性文件或本地“属性”）中获取的原始值
     * @return 转换后的值，用于处理
     * @see #setProperties
     * @see #setLocations
     * @see #setLocation
     * @see #convertProperty(String, String)
     */
    protected String convertPropertyValue(String originalValue) {
        return originalValue;
    }

    /**
     * 将给定的属性应用于给定的BeanFactory。
     * @param beanFactory 应用程序上下文所使用的BeanFactory
     * @param props 要应用属性
     * @throws org.springframework.beans.BeansException 如果出现错误
     */
    protected abstract void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException;
}
