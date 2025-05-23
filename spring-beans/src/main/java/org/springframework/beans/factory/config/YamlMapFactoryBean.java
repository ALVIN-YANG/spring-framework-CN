// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;

/**
 * 用于从 YAML 源读取的 {@code Map} 工厂，保留 YAML 声明的值类型及其结构。
 *
 * <p>YAML 是一种便于人类阅读的配置格式，它具有一些有用的层次属性。它大致上是 JSON 的超集，因此它具有许多类似的功能。
 *
 * <p>如果提供了多个资源，则后续的资源将按层次覆盖早期资源中的条目；也就是说，任何深度的具有相同嵌套键（类型为 {@code Map}）的所有条目都将合并。例如：
 *
 * <pre class="code">
 * foo:
 *   bar:
 *    one: two
 * three: four
 * </pre>
 *
 * 加上（在列表中的后续部分）
 *
 * <pre class="code">
 * foo:
 *   bar:
 *    one: 2
 * five: six
 * </pre>
 *
 * 结果是有效的输入
 *
 * <pre class="code">
 * foo:
 *   bar:
 *    one: 2
 * three: four
 * five: six
 * </pre>
 *
 * 注意，在第一个文档中 "foo" 的值并不是简单地被第二个文档中的值替换，而是其嵌套值被合并。
 *
 * <p>需要 SnakeYAML 1.18 或更高版本，自 Spring Framework 5.0.6 以来。
 *
 * @author Dave Syer
 * @author Juergen Hoeller
 * @since 4.1
 */
public class YamlMapFactoryBean extends YamlProcessor implements FactoryBean<Map<String, Object>>, InitializingBean {

    private boolean singleton = true;

    @Nullable
    private Map<String, Object> map;

    /**
     * 设置是否创建单例，还是在每次请求时创建新对象。默认为 {@code true}（单例）。
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
            this.map = createMap();
        }
    }

    @Override
    @Nullable
    public Map<String, Object> getObject() {
        return (this.map != null ? this.map : createMap());
    }

    @Override
    public Class<?> getObjectType() {
        return Map.class;
    }

    /**
     * 模板方法，子类可以重写此方法来构建由该工厂返回的对象。
     * <p>在首次调用{@link #getObject()}时，对于共享的单例，该方法会延迟调用；否则，在每次调用{@link #getObject()}时调用。
     * <p>默认实现返回合并的{@code Map}实例。
     * @return 由该工厂返回的对象
     * @see #process(MatchCallback)
     */
    protected Map<String, Object> createMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        process((properties, map) -> merge(result, map));
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void merge(Map<String, Object> output, Map<String, Object> map) {
        map.forEach((key, value) -> {
            Object existing = output.get(key);
            if (value instanceof Map valueMap && existing instanceof Map existingMap) {
                Map<String, Object> result = new LinkedHashMap<>(existingMap);
                merge(result, valueMap);
                output.put(key, result);
            } else {
                output.put(key, value);
            }
        });
    }
}
