// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.config;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;
import org.springframework.core.CollectionFactory;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * YAML 工厂的基础类。
 *
 * <p>自 Spring Framework 5.0.6 开始，需要 SnakeYAML 1.18 或更高版本。
 *
 * @author Dave Syer
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Brian Clozel
 * @since 4.1
 */
public abstract class YamlProcessor {

    private final Log logger = LogFactory.getLog(getClass());

    private ResolutionMethod resolutionMethod = ResolutionMethod.OVERRIDE;

    private Resource[] resources = new Resource[0];

    private List<DocumentMatcher> documentMatchers = Collections.emptyList();

    private boolean matchDefault = true;

    private Set<String> supportedTypes = Collections.emptySet();

    /**
     * 一个文档匹配器的映射，允许调用者有选择性地仅使用 YAML 资源中的某些文档。在 YAML 中，文档通过 {@code ---} 分隔，每个文档在匹配之前都转换为属性。例如：
     *  <pre class="code">
     *  environment: dev
     *  url: https://dev.bar.com
     *  name: Developer Setup
     *  ---
     *  environment: prod
     *  url:https://foo.bar.com
     *  name: My Cool App
     *  </pre>
     *  当与以下代码映射时：
     *  <pre class="code">
     *  setDocumentMatchers(properties ->
     *      ("prod".equals(properties.getProperty("environment")) ? MatchStatus.FOUND : MatchStatus.NOT_FOUND));
     *  </pre>
     *  最终结果将是：
     *  <pre class="code">
     *  environment=prod
     *  url=https://foo.bar.com
     *  name=My Cool App
     *  </pre>
     */
    public void setDocumentMatchers(DocumentMatcher... matchers) {
        this.documentMatchers = List.of(matchers);
    }

    /**
     * 标记表示对于所有未参与匹配的
     * `#setDocumentMatchers(DocumentMatcher...)` 文档匹配器，该文档仍将匹配。默认值为 `true`。
     */
    public void setMatchDefault(boolean matchDefault) {
        this.matchDefault = matchDefault;
    }

    /**
     * 使用此方法来解析资源。每个资源都将转换为 Map，
     * 因此这个属性用于决定从该工厂的最终输出中保留哪些映射条目。
     * 默认值为 {@link ResolutionMethod#OVERRIDE}。
     */
    public void setResolutionMethod(ResolutionMethod resolutionMethod) {
        Assert.notNull(resolutionMethod, "ResolutionMethod must not be null");
        this.resolutionMethod = resolutionMethod;
    }

    /**
     * 设置要加载的 YAML {@link Resource 资源} 的位置。
     * @see ResolutionMethod
     */
    public void setResources(Resource... resources) {
        this.resources = resources;
    }

    /**
     * 设置可以从 YAML 文档中加载的支持的类型。
     * <p>如果没有配置支持的类型，则只有 Java 标准类（如定义在 {@link org.yaml.snakeyaml.constructor.SafeConstructor}）
     * 中遇到 YAML 文档中的类将被支持。如果遇到不支持的类型，则在处理相应的 YAML 节点时将抛出 {@link IllegalStateException}。
     * @param supportedTypes 支持的类型，或一个空数组以清除支持的类型
     * @since 5.1.16
     * @see #createYaml()
     */
    public void setSupportedTypes(Class<?>... supportedTypes) {
        if (ObjectUtils.isEmpty(supportedTypes)) {
            this.supportedTypes = Collections.emptySet();
        } else {
            Assert.noNullElements(supportedTypes, "'supportedTypes' must not contain null elements");
            this.supportedTypes = Arrays.stream(supportedTypes).map(Class::getName).collect(Collectors.toUnmodifiableSet());
        }
    }

    /**
     * 为子类提供一个处理从提供的资源中解析出的 Yaml 的机会。每个资源依次被解析，其中的文档将与 {@link #setDocumentMatchers(DocumentMatcher...) 匹配器} 进行比较。如果文档匹配，它将被传递给回调，同时附上其作为 Properties 的表示形式。根据 {@link #setResolutionMethod(ResolutionMethod)} 的设置，并非所有文档都将被解析。
     * @param callback 当找到匹配的文档时委托的回调
     * @see #createYaml()
     */
    protected void process(MatchCallback callback) {
        Yaml yaml = createYaml();
        for (Resource resource : this.resources) {
            boolean found = process(callback, yaml, resource);
            if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND && found) {
                return;
            }
        }
    }

    /**
     * 创建用于的 {@link Yaml} 实例。
     * <p>默认实现将 "allowDuplicateKeys" 标志设置为 {@code false}，
     * 在 SnakeYAML 1.18+ 中启用内置的重复键处理。
     * <p>自 Spring Framework 5.1.16 版本起，如果已配置自定义的 {@linkplain #setSupportedTypes
     * 支持的类型}，默认实现将创建一个在 YAML 文档中过滤掉不支持的类型的 {@code Yaml} 实例。如果遇到不支持的类型，
     * 当处理节点时将抛出 {@link IllegalStateException} 异常。
     * @see LoaderOptions#setAllowDuplicateKeys(boolean)
     */
    protected Yaml createYaml() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        DumperOptions dumperOptions = new DumperOptions();
        return new Yaml(new FilteringConstructor(loaderOptions), new Representer(dumperOptions), dumperOptions, loaderOptions);
    }

    private boolean process(MatchCallback callback, Yaml yaml, Resource resource) {
        int count = 0;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading from YAML: " + resource);
            }
            try (Reader reader = new UnicodeReader(resource.getInputStream())) {
                for (Object object : yaml.loadAll(reader)) {
                    if (object != null && process(asMap(object), callback)) {
                        count++;
                        if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND) {
                            break;
                        }
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Loaded " + count + " document" + (count > 1 ? "s" : "") + " from YAML resource: " + resource);
                }
            }
        } catch (IOException ex) {
            handleProcessError(resource, ex);
        }
        return (count > 0);
    }

    private void handleProcessError(Resource resource, IOException ex) {
        if (this.resolutionMethod != ResolutionMethod.FIRST_FOUND && this.resolutionMethod != ResolutionMethod.OVERRIDE_AND_IGNORE) {
            throw new IllegalStateException(ex);
        }
        if (logger.isWarnEnabled()) {
            logger.warn("Could not load map from " + resource + ": " + ex.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, Object> asMap(Object object) {
        // YAML可以数字作为键
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map map)) {
            // 文档可以是一个文本字面量
            result.put("document", object);
            return result;
        }
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                value = asMap(value);
            }
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                // 在这种情况下，它必须是一个映射键
                result.put("[" + key.toString() + "]", value);
            }
        });
        return result;
    }

    private boolean process(Map<String, Object> map, MatchCallback callback) {
        Properties properties = CollectionFactory.createStringAdaptingProperties();
        properties.putAll(getFlattenedMap(map));
        if (this.documentMatchers.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Merging document (no matchers set): " + map);
            }
            callback.process(properties, map);
            return true;
        }
        MatchStatus result = MatchStatus.ABSTAIN;
        for (DocumentMatcher matcher : this.documentMatchers) {
            MatchStatus match = matcher.matches(properties);
            result = MatchStatus.getMostSpecific(match, result);
            if (match == MatchStatus.FOUND) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Matched document with document matcher: " + properties);
                }
                callback.process(properties, map);
                return true;
            }
        }
        if (result == MatchStatus.ABSTAIN && this.matchDefault) {
            if (logger.isDebugEnabled()) {
                logger.debug("Matched document with default matcher: " + map);
            }
            callback.process(properties, map);
            return true;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Unmatched document: " + map);
        }
        return false;
    }

    /**
     * 返回给定映射的扁平化版本，递归地跟随任何嵌套的 Map 或 Collection 值。结果映射中的条目保留与源相同的顺序。当使用来自 {@link MatchCallback} 的 Map 调用时，结果将包含与 {@link MatchCallback} 属性相同的值。
     * @param source 源映射
     * @return 一个扁平化的映射
     * @since 4.1.3
     */
    protected final Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, @Nullable String path) {
        source.forEach((key, value) -> {
            if (StringUtils.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map map) {
                // 需要一个复合键
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection collection) {
                // 需要一个复合键
                if (collection.isEmpty()) {
                    result.put(key, "");
                } else {
                    int count = 0;
                    for (Object object : collection) {
                        buildFlattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), key);
                    }
                }
            } else {
                result.put(key, (value != null ? value : ""));
            }
        });
    }

    /**
     * 用于处理 YAML 解析结果的回调接口。
     */
    @FunctionalInterface
    public interface MatchCallback {

        /**
         * 处理给定的解析结果表示。
         * @param properties 要处理的属性（以扁平化的表示形式，如果是一个集合或映射，则使用索引键）
         * @param map 结果映射（保留YAML文档中的原始值结构）
         */
        void process(Properties properties, Map<String, Object> map);
    }

    /**
     * 用于测试属性是否匹配的策略接口。
     */
    @FunctionalInterface
    public interface DocumentMatcher {

        /**
         * 测试给定的属性是否匹配。
         * @param properties 要测试的属性
         * @return 匹配的状态
         */
        MatchStatus matches(Properties properties);
    }

    /**
     * 从 {@link DocumentMatcher#matches(java.util.Properties)} 返回的状态
     */
    public enum MatchStatus {

        /**
         * 找到匹配项。
         */
        FOUND,
        /**
         * 未找到匹配项。
         */
        NOT_FOUND,
        /**
         * 该匹配器不应被考虑。
         */
        ABSTAIN;

        /**
         * 比较两个 {@link MatchStatus} 项目，返回最具体的状态。
         */
        public static MatchStatus getMostSpecific(MatchStatus a, MatchStatus b) {
            return (a.ordinal() < b.ordinal() ? a : b);
        }
    }

    /**
     * 用于解析资源的方
     */
    public enum ResolutionMethod {

        /**
         * 替换列表中较早的值。
         */
        OVERRIDE,
        /**
         * 替换列表中较早的值，忽略任何失败。
         */
        OVERRIDE_AND_IGNORE,
        /**
         * 选择列表中存在的第一个资源并仅使用该资源。
         */
        FIRST_FOUND
    }

    /**
     * 支持过滤不受支持类型的构造函数。
     * <p>如果在 YAML 文档中遇到不受支持的类型，将从 #getClassForName 抛出 {@link IllegalStateException} 异常。
     */
    private class FilteringConstructor extends Constructor {

        FilteringConstructor(LoaderOptions loaderOptions) {
            super(loaderOptions);
        }

        @Override
        protected Class<?> getClassForName(String name) throws ClassNotFoundException {
            Assert.state(YamlProcessor.this.supportedTypes.contains(name), () -> "Unsupported type encountered in YAML document: " + name);
            return super.getClassForName(name);
        }
    }
}
