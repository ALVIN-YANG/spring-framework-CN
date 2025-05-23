// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证以了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Maps属性编辑器，将任何源Map转换为指定的目标Map类型。
 *
 * @author Juergen Hoeller
 * @since 2.0.1
 * @see java.util.Map
 * @see java.util.SortedMap
 */
public class CustomMapEditor extends PropertyEditorSupport {

    @SuppressWarnings("rawtypes")
    private final Class<? extends Map> mapType;

    private final boolean nullAsEmptyMap;

    /**
     * 为给定的目标类型创建一个新的 CustomMapEditor，
     * 保持传入的 {@code null} 保持原样。
     * @param mapType 目标类型，需要是 Map 的子接口或具体的 Map 类
     * @see java.util.Map
     * @see java.util.HashMap
     * @see java.util.TreeMap
     * @see java.util.LinkedHashMap
     */
    @SuppressWarnings("rawtypes")
    public CustomMapEditor(Class<? extends Map> mapType) {
        this(mapType, false);
    }

    /**
     * 为给定的目标类型创建一个新的 CustomMapEditor。
     * <p>如果传入的值是给定类型的，它将被直接使用。
     * 如果它是一个不同的 Map 类型或数组，它将被转换为给定 Map 类型的默认实现。
     * 如果值是其他任何内容，将创建一个包含该单个值的目标 Map。
     * <p>默认的 Map 实现是：SortedMap 使用 TreeMap，Map 使用 LinkedHashMap。
     * @param mapType 需要的目标类型，它必须是 Map 的子接口或具体的 Map 类
     * @param nullAsEmptyMap 是否将传入的 null 值转换为空 Map（适当类型的）
     * @see java.util.Map
     * @see java.util.TreeMap
     * @see java.util.LinkedHashMap
     */
    @SuppressWarnings("rawtypes")
    public CustomMapEditor(Class<? extends Map> mapType, boolean nullAsEmptyMap) {
        Assert.notNull(mapType, "Map type is required");
        if (!Map.class.isAssignableFrom(mapType)) {
            throw new IllegalArgumentException("Map type [" + mapType.getName() + "] does not implement [java.util.Map]");
        }
        this.mapType = mapType;
        this.nullAsEmptyMap = nullAsEmptyMap;
    }

    /**
     * 将给定的文本值转换为包含单个元素的 Map。
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(text);
    }

    /**
     * 将给定的值转换为目标类型的 Map。
     */
    @Override
    public void setValue(@Nullable Object value) {
        if (value == null && this.nullAsEmptyMap) {
            super.setValue(createMap(this.mapType, 0));
        } else if (value == null || (this.mapType.isInstance(value) && !alwaysCreateNewMap())) {
            // 直接使用源值，因为它与目标类型匹配。
            super.setValue(value);
        } else if (value instanceof Map<?, ?> source) {
            // 转换 Map 元素。
            Map<Object, Object> target = createMap(this.mapType, source.size());
            source.forEach((key, val) -> target.put(convertKey(key), convertValue(val)));
            super.setValue(target);
        } else {
            throw new IllegalArgumentException("Value cannot be converted to Map: " + value);
        }
    }

    /**
     * 创建一个给定类型的 Map，具有给定的初始容量（如果该 Map 类型支持）。
     * @param mapType Map 的子接口
     * @param initialCapacity 初始容量
     * @return 新的 Map 实例
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Map<Object, Object> createMap(Class<? extends Map> mapType, int initialCapacity) {
        if (!mapType.isInterface()) {
            try {
                return ReflectionUtils.accessibleConstructor(mapType).newInstance();
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Could not instantiate map class: " + mapType.getName(), ex);
            }
        } else if (SortedMap.class == mapType) {
            return new TreeMap<>();
        } else {
            return new LinkedHashMap<>(initialCapacity);
        }
    }

    /**
     * 返回是否始终创建一个新的 Map，
     * 即使传入的 Map 类型已经匹配。
     * <p>默认值为 "false"；可以重写以强制创建一个新的 Map，
     * 例如，用于将元素转换为任何大小写。
     * @see #convertKey
     * @see #convertValue
     */
    protected boolean alwaysCreateNewMap() {
        return false;
    }

    /**
     * 钩子，用于转换每个遇到的 Map 键。
     * 默认实现只是简单地返回传入的键原样。
     * <p>可以被重写以执行某些键的转换，例如从 String 转换为 Integer。
     * <p>仅在实际上创建一个新的 Map 时才会调用！
     * 默认情况下，如果传入的 Map 类型已经匹配，则不会这样做。
     * 要强制在每种情况下都创建一个新的 Map，重写 {@link #alwaysCreateNewMap()}。
     * @param key 源键
     * @return 目标 Map 中要使用的键
     * @see #alwaysCreateNewMap
     */
    protected Object convertKey(Object key) {
        return key;
    }

    /**
     * 钩子用于转换遇到的每个 Map 值。
     * 默认实现简单地返回传入的值原样。
     * <p>可以覆盖以执行某些值的转换，例如从 String 转换为 Integer。
     * <p>仅在实际上创建新的 Map 时才会调用！
     * 默认情况下，如果传入的 Map 类型已经匹配，则不会这样做。
     * 覆盖 {@link #alwaysCreateNewMap()} 以在每种情况下强制创建新的 Map。
     * @param value 源值
     * @return 目标 Map 中要使用的值
     * @see #alwaysCreateNewMap
     */
    protected Object convertValue(Object value) {
        return value;
    }

    /**
     * 此实现返回 {@code null} 以指示没有合适的文本表示。
     */
    @Override
    @Nullable
    public String getAsText() {
        return null;
    }
}
