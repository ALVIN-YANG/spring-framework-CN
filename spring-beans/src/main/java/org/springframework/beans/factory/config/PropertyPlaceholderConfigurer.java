// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的质量保证或条件，
* 明示的或暗示的。有关许可证具体规定权限和限制的内容，
* 请参阅许可证。*/
package org.springframework.beans.factory.config;

import java.util.Properties;
import org.springframework.beans.BeansException;
import org.springframework.core.Constants;
import org.springframework.core.SpringProperties;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.StringValueResolver;

/**
 * {@link PlaceholderConfigurerSupport} 的子类，用于解析 ${...} 占位符，针对本地设置的 {@link #setLocation} 和/或系统属性以及环境变量。
 *
 * <p>当以下情况适用时，仍然可以使用 {@link PropertyPlaceholderConfigurer}：
 * <ul>
 * <li>当不使用 {@code spring-context} 模块（即使用 Spring 的 {@code BeanFactory} API 而不是 {@code ApplicationContext}）。
 * <li>现有的配置使用了 {@link #setSystemPropertiesMode(int) "systemPropertiesMode"} 和/或 {@link #setSystemPropertiesModeName(String) "systemPropertiesModeName"} 属性。
 * 建议用户远离这些设置，而是通过容器的 {@code Environment} 来配置属性源搜索顺序；然而，通过继续使用 {@code PropertyPlaceholderConfigurer} 可以保持功能的一致性。
 * </ul>
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2003年10月2日
 * @see #setSystemPropertiesModeName
 * @see PlaceholderConfigurerSupport
 * @see PropertyOverrideConfigurer
 * @deprecated 自 5.2 版本以来已过时；请使用替代方案 {@code org.springframework.context.support.PropertySourcesPlaceholderConfigurer}，它通过利用 {@link org.springframework.core.env.Environment} 和 {@link org.springframework.core.env.PropertySource} 机制提供了更大的灵活性。
 */
@Deprecated
public class PropertyPlaceholderConfigurer extends PlaceholderConfigurerSupport {

    /**
     * 永远不要检查系统属性。
     */
    public static final int SYSTEM_PROPERTIES_MODE_NEVER = 0;

    /**
     * 检查系统属性，如果指定的属性中无法解析。
     * 这是默认行为。
     */
    public static final int SYSTEM_PROPERTIES_MODE_FALLBACK = 1;

    /**
     * 首先检查系统属性，然后再尝试指定的属性。
     * 这允许系统属性覆盖任何其他属性来源。
     */
    public static final int SYSTEM_PROPERTIES_MODE_OVERRIDE = 2;

    private static final Constants constants = new Constants(PropertyPlaceholderConfigurer.class);

    private int systemPropertiesMode = SYSTEM_PROPERTIES_MODE_FALLBACK;

    private boolean searchSystemEnvironment = !SpringProperties.getFlag(AbstractEnvironment.IGNORE_GETENV_PROPERTY_NAME);

    /**
     * 通过对应的常量名称设置系统属性 mode，
     * 例如："SYSTEM_PROPERTIES_MODE_OVERRIDE"。
     * @param constantName 常量名称
     * @see #setSystemPropertiesMode
     */
    public void setSystemPropertiesModeName(String constantName) throws IllegalArgumentException {
        this.systemPropertiesMode = constants.asNumber(constantName).intValue();
    }

    /**
     * 设置检查系统属性的方式：作为后备、作为覆盖或永不。
     * 例如，将解析 `${user.dir}` 到 "user.dir" 系统属性。
     * <p>默认为 "fallback"：如果无法使用指定的属性解析占位符，将尝试使用系统属性。
     * "override" 将首先检查系统属性，然后再尝试指定的属性。"never" 则完全不会检查系统属性。
     * @see #SYSTEM_PROPERTIES_MODE_NEVER
     * @see #SYSTEM_PROPERTIES_MODE_FALLBACK
     * @see #SYSTEM_PROPERTIES_MODE_OVERRIDE
     * @see #setSystemPropertiesModeName
     */
    public void setSystemPropertiesMode(int systemPropertiesMode) {
        this.systemPropertiesMode = systemPropertiesMode;
    }

    /**
     * 设置是否在未找到匹配的系统属性时搜索匹配的系统环境变量
     * 仅当 "systemPropertyMode" 激活时（即 "fallback" 或 "override"）适用，在检查 JVM 系统属性之后立即应用。
     * <p>默认值为 "true"。关闭此设置将永远不将占位符解析为系统环境变量。请注意，通常建议将外部值作为 JVM 系统属性传递：这可以通过启动脚本轻松实现，即使对于现有的环境变量也是如此。
     * @see #setSystemPropertiesMode
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    public void setSearchSystemEnvironment(boolean searchSystemEnvironment) {
        this.searchSystemEnvironment = searchSystemEnvironment;
    }

    /**
     * 使用给定的属性解析指定的占位符，根据给定的模式执行系统属性检查。
     * 默认实现会在系统属性检查前后委托给 {@code resolvePlaceholder(placeholder, props)}。
     * 子类可以覆盖此方法以实现自定义解析策略，包括自定义系统属性检查的点。
     * @param placeholder 要解析的占位符
     * @param props 此配置器的合并属性
     * @param systemPropertiesMode 系统属性模式，根据此类中的常量
     * @return 解析后的值，如果没有则返回 null
     * @see #setSystemPropertiesMode
     * @see System#getProperty
     * @see #resolvePlaceholder(String, java.util.Properties)
     */
    @Nullable
    protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
        String propVal = null;
        if (systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE) {
            propVal = resolveSystemProperty(placeholder);
        }
        if (propVal == null) {
            propVal = resolvePlaceholder(placeholder, props);
        }
        if (propVal == null && systemPropertiesMode == SYSTEM_PROPERTIES_MODE_FALLBACK) {
            propVal = resolveSystemProperty(placeholder);
        }
        return propVal;
    }

    /**
     * 使用给定的属性解析给定的占位符。
     * 默认实现只是简单地检查对应的属性键。
     * <p>子类可以覆盖此方法以实现定制的占位符到键的映射或自定义解析策略，可能只是将给定的属性作为后备。
     * <p>注意，在调用此方法之前或之后，仍将检查系统属性，具体取决于系统属性模式。
     * @param placeholder 要解析的占位符
     * @param props 此配置器的合并后的属性
     * @return 解析后的值，如果没有则返回 {@code null}
     * @see #setSystemPropertiesMode
     */
    @Nullable
    protected String resolvePlaceholder(String placeholder, Properties props) {
        return props.getProperty(placeholder);
    }

    /**
     * 将给定的键解析为 JVM 系统属性，如果未找到匹配的系统属性，则可选地也解析为系统环境变量。
     * @param key 要解析的系统属性键占位符
     * @return 系统属性值，如果未找到则返回 {@code null}
     * @see #setSearchSystemEnvironment
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    @Nullable
    protected String resolveSystemProperty(String key) {
        try {
            String value = System.getProperty(key);
            if (value == null && this.searchSystemEnvironment) {
                value = System.getenv(key);
            }
            return value;
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not access system property '" + key + "': " + ex);
            }
            return null;
        }
    }

    /**
     * 遍历给定bean工厂中的每个bean定义，并尝试用给定的属性值替换${...}属性占位符。
     */
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(props);
        doProcessProperties(beanFactoryToProcess, valueResolver);
    }

    private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

        private final PropertyPlaceholderHelper helper;

        private final PlaceholderResolver resolver;

        public PlaceholderResolvingStringValueResolver(Properties props) {
            this.helper = new PropertyPlaceholderHelper(placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
            this.resolver = new PropertyPlaceholderConfigurerResolver(props);
        }

        @Override
        @Nullable
        public String resolveStringValue(String strVal) throws BeansException {
            String resolved = this.helper.replacePlaceholders(strVal, this.resolver);
            if (trimValues) {
                resolved = resolved.trim();
            }
            return (resolved.equals(nullValue) ? null : resolved);
        }
    }

    private final class PropertyPlaceholderConfigurerResolver implements PlaceholderResolver {

        private final Properties props;

        private PropertyPlaceholderConfigurerResolver(Properties props) {
            this.props = props;
        }

        @Override
        @Nullable
        public String resolvePlaceholder(String placeholderName) {
            return PropertyPlaceholderConfigurer.this.resolvePlaceholder(placeholderName, this.props, systemPropertiesMode);
        }
    }
}
