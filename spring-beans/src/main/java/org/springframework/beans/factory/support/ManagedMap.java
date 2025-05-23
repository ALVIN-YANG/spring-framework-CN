// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;
import org.springframework.lang.Nullable;

/**
 * 用于存储管理 Map 值的标签集合类，可能包括运行时 Bean 引用（将被解析为 Bean 对象）。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 27.05.2003
 * @param <K> 键类型
 * @param <V> 值类型
 */
@SuppressWarnings("serial")
public class ManagedMap<K, V> extends LinkedHashMap<K, V> implements Mergeable, BeanMetadataElement {

    @Nullable
    private Object source;

    @Nullable
    private String keyTypeName;

    @Nullable
    private String valueTypeName;

    private boolean mergeEnabled;

    public ManagedMap() {
    }

    public ManagedMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 返回一个新的实例，其中包含从给定条目中提取的键和值。这些条目本身不存储在映射中。
     * @param entries 包含要填充映射的键和值的 {@code Map.Entry}
     * @param <K> 映射的键类型
     * @param <V> 映射的值类型
     * @return 包含指定映射的 {@code Map}
     * @since 5.3.16
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <K, V> ManagedMap<K, V> ofEntries(Entry<? extends K, ? extends V>... entries) {
        ManagedMap<K, V> map = new ManagedMap<>();
        for (Entry<? extends K, ? extends V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * 设置此元数据元素的配置源对象。
     * <p>对象的精确类型将取决于所使用的配置机制。
     */
    public void setSource(@Nullable Object source) {
        this.source = source;
    }

    @Override
    @Nullable
    public Object getSource() {
        return this.source;
    }

    /**
     * 设置用于此映射的默认键类型名称（类名）。
     */
    public void setKeyTypeName(@Nullable String keyTypeName) {
        this.keyTypeName = keyTypeName;
    }

    /**
     * 返回用于此映射的默认键类型名称（类名）。
     */
    @Nullable
    public String getKeyTypeName() {
        return this.keyTypeName;
    }

    /**
     * 设置用于此映射的默认值类型名称（类名）。
     */
    public void setValueTypeName(@Nullable String valueTypeName) {
        this.valueTypeName = valueTypeName;
    }

    /**
     * 返回用于此映射的默认值类型名称（类名）。
     */
    @Nullable
    public String getValueTypeName() {
        return this.valueTypeName;
    }

    /**
     * 设置是否启用此集合的合并功能，
     * 如果存在一个'父'集合值时。
     */
    public void setMergeEnabled(boolean mergeEnabled) {
        this.mergeEnabled = mergeEnabled;
    }

    @Override
    public boolean isMergeEnabled() {
        return this.mergeEnabled;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object merge(@Nullable Object parent) {
        if (!this.mergeEnabled) {
            throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
        }
        if (parent == null) {
            return this;
        }
        if (!(parent instanceof Map)) {
            throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
        }
        Map<K, V> merged = new ManagedMap<>();
        merged.putAll((Map<K, V>) parent);
        merged.putAll(this);
        return merged;
    }
}
