// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.config;

import java.util.Properties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.CollectionFactory;
import org.springframework.lang.Nullable;

/**
 *  用于创建 `java.util.Properties` 的工厂，从 YAML 源读取数据，提供扁平结构的字符串属性值。
 *
 * <p>YAML 是一种便于阅读的配置格式，它具有一些有用的层次属性。它大致是 JSON 的超集，因此具有许多类似的功能。
 *
 * <p><b>注意：</b>所有公开的值均为类型 `String`，可以通过通用的 `Properties#getProperty` 方法访问（例如，通过 `PropertyResourceConfigurer#setProperties(Properties)` 在配置属性解析中）。
 * 如果这不是期望的行为，请使用 `YamlMapFactoryBean`。
 *
 * <p>此工厂创建的 Properties 具有嵌套路径，用于层次化对象，例如以下 YAML：
 *
 * <pre class="code">
 * environments:
 *   dev:
 *     url: https://dev.bar.com
 *     name: Developer Setup
 *   prod:
 *     url: https://foo.bar.com
 *     name: My Cool App
 * </pre>
 *
 * 转换为以下属性：
 *
 * <pre class="code">
 * environments.dev.url=https://dev.bar.com
 * environments.dev.name=Developer Setup
 * environments.prod.url=https://foo.bar.com
 * environments.prod.name=My Cool App
 * </pre>
 *
 * 列表作为带有 `<code>[]</code>` 解引用符的属性键分割，例如以下 YAML：
 *
 * <pre class="code">
 * servers:
 *   - dev.bar.com
 *   - foo.bar.com
 * </pre>
 *
 * 转换为以下属性：
 *
 * <pre class="code">
 * servers[0]=dev.bar.com
 * servers[1]=foo.bar.com
 * </pre>
 *
 * <p>从 Spring Framework 5.0.6 开始，需要 SnakeYAML 1.18 或更高版本。
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 4.1
 */
public class YamlPropertiesFactoryBean extends YamlProcessor implements FactoryBean<Properties>, InitializingBean {

    private boolean singleton = true;

    @Nullable
    private Properties properties;

    /**
     * 设置是否应该创建一个单例，还是每次请求时创建一个新的对象。默认为 {@code true}（单例）。
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public boolean isSingleton() {
        return this.singleton;
    }

    @Override
    public void afterPropertiesSet() {
        if (isSingleton()) {
            this.properties = createProperties();
        }
    }

    @Override
    @Nullable
    public Properties getObject() {
        return (this.properties != null ? this.properties : createProperties());
    }

    @Override
    public Class<?> getObjectType() {
        return Properties.class;
    }

    /**
     * 模板方法，子类可以覆盖以构建由该工厂返回的对象。默认实现返回包含所有资源内容的属性。
     * <p>在共享单例的情况下，在第一次调用 {@link #getObject()} 时被惰性调用；否则，在每次调用 {@link #getObject()} 时调用。
     * @return 由该工厂返回的对象
     * @see #process(MatchCallback)
     */
    protected Properties createProperties() {
        Properties result = CollectionFactory.createStringAdaptingProperties();
        process((properties, map) -> result.putAll(properties));
        return result;
    }
}
