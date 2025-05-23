// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非适用法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按照“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 构造器参数值持有者，通常作为Bean定义的一部分。
 *
 * <p>支持在构造器参数列表中特定索引的值以及通过类型进行泛型参数匹配的值。
 *
 * @author Juergen Hoeller
 * @since 09.11.2003
 * @see BeanDefinition#getConstructorArgumentValues
 */
public class ConstructorArgumentValues {

    private final Map<Integer, ValueHolder> indexedArgumentValues = new LinkedHashMap<>();

    private final List<ValueHolder> genericArgumentValues = new ArrayList<>();

    /**
     * 创建一个新的空 ConstructorArgumentValues 对象。
     */
    public ConstructorArgumentValues() {
    }

    /**
     * 深度复制构造函数。
     * @param original 要复制的 ConstructorArgumentValues
     */
    public ConstructorArgumentValues(ConstructorArgumentValues original) {
        addArgumentValues(original);
    }

    /**
     * 将所有给定的参数值复制到这个对象中，使用独立的持有者实例来保持值与原始对象独立。
     * <p>注意：相同的ValueHolder实例只会注册一次，以便允许参数值定义的合并和重新合并。当然，携带相同内容的不同ValueHolder实例是允许的。
     */
    public void addArgumentValues(@Nullable ConstructorArgumentValues other) {
        if (other != null) {
            other.indexedArgumentValues.forEach((index, argValue) -> addOrMergeIndexedArgumentValue(index, argValue.copy()));
            other.genericArgumentValues.stream().filter(valueHolder -> !this.genericArgumentValues.contains(valueHolder)).forEach(valueHolder -> addOrMergeGenericArgumentValue(valueHolder.copy()));
        }
    }

    /**
     * 在构造函数参数列表中为指定的索引添加一个参数值。
     * @param index 构造函数参数列表中的索引
     * @param value 参数值
     */
    public void addIndexedArgumentValue(int index, @Nullable Object value) {
        addIndexedArgumentValue(index, new ValueHolder(value));
    }

    /**
     * 在构造函数参数列表中为指定索引添加一个参数值。
     * @param index 构造函数参数列表中的索引
     * @param value 参数值
     * @param type 构造函数参数的类型
     */
    public void addIndexedArgumentValue(int index, @Nullable Object value, String type) {
        addIndexedArgumentValue(index, new ValueHolder(value, type));
    }

    /**
     * 在构造函数参数列表中给指定索引添加一个参数值。
     * @param index 构造函数参数列表中的索引
     * @param newValue 以 ValueHolder 形式表示的参数值
     */
    public void addIndexedArgumentValue(int index, ValueHolder newValue) {
        Assert.isTrue(index >= 0, "Index must not be negative");
        Assert.notNull(newValue, "ValueHolder must not be null");
        addOrMergeIndexedArgumentValue(index, newValue);
    }

    /**
     * 在构造函数参数列表中为给定索引添加一个参数值，
     * 如果需要，将新值（通常是集合）与当前值合并：请参阅{@link org.springframework.beans.Mergeable}。
     * @param key 构造函数参数列表中的索引
     * @param newValue 以ValueHolder形式表示的参数值
     */
    private void addOrMergeIndexedArgumentValue(Integer key, ValueHolder newValue) {
        ValueHolder currentValue = this.indexedArgumentValues.get(key);
        if (currentValue != null && newValue.getValue() instanceof Mergeable mergeable) {
            if (mergeable.isMergeEnabled()) {
                newValue.setValue(mergeable.merge(currentValue.getValue()));
            }
        }
        this.indexedArgumentValues.put(key, newValue);
    }

    /**
     * 检查是否已为给定索引注册了参数值。
     * @param index 构造函数参数列表中的索引
     */
    public boolean hasIndexedArgumentValue(int index) {
        return this.indexedArgumentValues.containsKey(index);
    }

    /**
     * 获取构造函数参数列表中给定索引的参数值。
     * @param index 构造函数参数列表中的索引
     * @param requiredType 要匹配的类型（可以为 {@code null} 以匹配未类型化的值）
     * @return 对应参数的 ValueHolder 对象，如果没有设置则返回 {@code null}
     */
    @Nullable
    public ValueHolder getIndexedArgumentValue(int index, @Nullable Class<?> requiredType) {
        return getIndexedArgumentValue(index, requiredType, null);
    }

    /**
     * 在构造函数参数列表中获取给定索引的参数值。
     * @param index 构造函数参数列表中的索引
     * @param requiredType 需要匹配的类型（可以为 {@code null} 以仅匹配无类型值）
     * @param requiredName 需要匹配的名称（可以为 {@code null} 以仅匹配无名称值，或空字符串以匹配任何名称）
     * @return 参数的 ValueHolder 对象，如果没有设置则返回 {@code null}
     */
    @Nullable
    public ValueHolder getIndexedArgumentValue(int index, @Nullable Class<?> requiredType, @Nullable String requiredName) {
        Assert.isTrue(index >= 0, "Index must not be negative");
        ValueHolder valueHolder = this.indexedArgumentValues.get(index);
        if (valueHolder != null && (valueHolder.getType() == null || (requiredType != null && ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) && (valueHolder.getName() == null || (requiredName != null && (requiredName.isEmpty() || requiredName.equals(valueHolder.getName()))))) {
            return valueHolder;
        }
        return null;
    }

    /**
     * 返回索引参数值的映射。
     * @return 以 Integer 索引为键，ValueHolder 为值的不可修改的 Map
     * @see ValueHolder
     */
    public Map<Integer, ValueHolder> getIndexedArgumentValues() {
        return Collections.unmodifiableMap(this.indexedArgumentValues);
    }

    /**
     * 添加一个要按类型匹配的泛型参数值。
     * <p>注意：单个泛型参数值将只会使用一次，
     * 而不是多次匹配。
     * @param value 参数值
     */
    public void addGenericArgumentValue(@Nullable Object value) {
        this.genericArgumentValues.add(new ValueHolder(value));
    }

    /**
     * 添加一个用于类型匹配的泛型参数值。
     * <p>注意：单个泛型参数值将仅使用一次，
     * 而不是多次匹配。
     * @param value 参数值
     * @param type 构造函数参数的类型
     */
    public void addGenericArgumentValue(Object value, String type) {
        this.genericArgumentValues.add(new ValueHolder(value, type));
    }

    /**
     * 添加一个通用的参数值，用于通过类型或名称（如果可用）进行匹配。
     * <p>注意：单个通用参数值将只会使用一次，而不是多次匹配。
     * @param newValue 以 ValueHolder 形式存在的参数值
     * <p>注意：相同的 ValueHolder 实例只会注册一次，以便允许参数值定义的合并和重新合并。当然，携带相同内容的不同的 ValueHolder 实例是允许的。
     */
    public void addGenericArgumentValue(ValueHolder newValue) {
        Assert.notNull(newValue, "ValueHolder must not be null");
        if (!this.genericArgumentValues.contains(newValue)) {
            addOrMergeGenericArgumentValue(newValue);
        }
    }

    /**
     * 添加一个泛型参数值，如果需要，则将新值（通常是集合）与当前值合并：请参阅{@link org.springframework.beans.Mergeable}。
     * @param newValue 以ValueHolder形式的新参数值
     */
    private void addOrMergeGenericArgumentValue(ValueHolder newValue) {
        if (newValue.getName() != null) {
            for (Iterator<ValueHolder> it = this.genericArgumentValues.iterator(); it.hasNext(); ) {
                ValueHolder currentValue = it.next();
                if (newValue.getName().equals(currentValue.getName())) {
                    if (newValue.getValue() instanceof Mergeable mergeable) {
                        if (mergeable.isMergeEnabled()) {
                            newValue.setValue(mergeable.merge(currentValue.getValue()));
                        }
                    }
                    it.remove();
                }
            }
        }
        this.genericArgumentValues.add(newValue);
    }

    /**
     * 查找与给定类型匹配的泛型参数值。
     * @param requiredType 需要匹配的类型
     * @return 返回参数的 ValueHolder 对象，如果没有设置则返回 {@code null}
     */
    @Nullable
    public ValueHolder getGenericArgumentValue(Class<?> requiredType) {
        return getGenericArgumentValue(requiredType, null, null);
    }

    /**
     * 查找与给定类型匹配的泛型参数值。
     * @param requiredType 要匹配的类型
     * @param requiredName 要匹配的名称
     * @return 参数的 ValueHolder，如果没有设置则返回 {@code null}
     */
    @Nullable
    public ValueHolder getGenericArgumentValue(Class<?> requiredType, String requiredName) {
        return getGenericArgumentValue(requiredType, requiredName, null);
    }

    /**
     * 寻找与给定类型匹配的下一个泛型参数值，
     * 忽略在当前解析过程中已使用的参数值。
     * @param requiredType 要匹配的类型（可以为 {@code null} 以查找任意下一个泛型参数值）
     * @param requiredName 要匹配的名称（可以为 {@code null} 以不通过名称匹配参数值，或空字符串以匹配任何名称）
     * @param usedValueHolders 已在当前解析过程中使用并因此不应再次返回的 ValueHolder 对象的集合
     * @return 对应参数的 ValueHolder，或未找到时返回 {@code null}
     */
    @Nullable
    public ValueHolder getGenericArgumentValue(@Nullable Class<?> requiredType, @Nullable String requiredName, @Nullable Set<ValueHolder> usedValueHolders) {
        for (ValueHolder valueHolder : this.genericArgumentValues) {
            if (usedValueHolders != null && usedValueHolders.contains(valueHolder)) {
                continue;
            }
            if (valueHolder.getName() != null && (requiredName == null || (!requiredName.isEmpty() && !requiredName.equals(valueHolder.getName())))) {
                continue;
            }
            if (valueHolder.getType() != null && (requiredType == null || !ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) {
                continue;
            }
            if (requiredType != null && valueHolder.getType() == null && valueHolder.getName() == null && !ClassUtils.isAssignableValue(requiredType, valueHolder.getValue())) {
                continue;
            }
            return valueHolder;
        }
        return null;
    }

    /**
     * 返回泛型参数值的列表。
     * @return 不可修改的 ValueHolders 列表
     * @see ValueHolder
     */
    public List<ValueHolder> getGenericArgumentValues() {
        return Collections.unmodifiableList(this.genericArgumentValues);
    }

    /**
     * 查找一个与构造函数参数列表中给定索引对应的参数值，或者通过类型进行泛型匹配。
     * @param index 构造函数参数列表中的索引
     * @param requiredType 要匹配的参数类型
     * @return 对应参数的 ValueHolder 对象，或如果未设置则返回 {@code null}
     */
    @Nullable
    public ValueHolder getArgumentValue(int index, Class<?> requiredType) {
        return getArgumentValue(index, requiredType, null, null);
    }

    /**
     * 查找一个与构造函数参数列表中给定索引对应的参数值，或者通过类型进行通用匹配。
     * @param index 构造函数参数列表中的索引
     * @param requiredType 需要匹配的参数类型
     * @param requiredName 需要匹配的参数名称
     * @return 对应参数的 ValueHolder，或如果未设置则返回 {@code null}
     */
    @Nullable
    public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName) {
        return getArgumentValue(index, requiredType, requiredName, null);
    }

    /**
     * 查找与构造函数参数列表中给定索引对应的参数值，或者通过类型进行通配匹配。
     * @param index 构造函数参数列表中的索引
     * @param requiredType 要匹配的参数类型（可以为 {@code null} 以查找无类型的参数值）
     * @param requiredName 要匹配的参数名称（可以为 {@code null} 以查找未命名的参数值，或空字符串以匹配任何名称）
     * @param usedValueHolders 已在当前解析过程中使用过的 ValueHolder 对象的集合，因此不应再次返回（允许在存在相同类型的多个泛型参数值时返回下一个泛型参数匹配）
     * @return 对应参数的 ValueHolder，或如果未设置则返回 {@code null}
     */
    @Nullable
    public ValueHolder getArgumentValue(int index, @Nullable Class<?> requiredType, @Nullable String requiredName, @Nullable Set<ValueHolder> usedValueHolders) {
        Assert.isTrue(index >= 0, "Index must not be negative");
        ValueHolder valueHolder = getIndexedArgumentValue(index, requiredType, requiredName);
        if (valueHolder == null) {
            valueHolder = getGenericArgumentValue(requiredType, requiredName, usedValueHolders);
        }
        return valueHolder;
    }

    /**
     * 判断是否至少有一个参数值引用了一个名称。
     * @since 6.0.3
     * @see ValueHolder#getName()
     */
    public boolean containsNamedArgument() {
        for (ValueHolder valueHolder : this.indexedArgumentValues.values()) {
            if (valueHolder.getName() != null) {
                return true;
            }
        }
        for (ValueHolder valueHolder : this.genericArgumentValues) {
            if (valueHolder.getName() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回此实例中持有的参数值的数量，
     * 包括索引参数值和泛型参数值。
     */
    public int getArgumentCount() {
        return (this.indexedArgumentValues.size() + this.genericArgumentValues.size());
    }

    /**
     * 如果此持有者不包含任何参数值，
     * 既不是索引参数也不是泛型参数，则返回。
     */
    public boolean isEmpty() {
        return (this.indexedArgumentValues.isEmpty() && this.genericArgumentValues.isEmpty());
    }

    /**
     * 清除此持有者，移除所有参数值。
     */
    public void clear() {
        this.indexedArgumentValues.clear();
        this.genericArgumentValues.clear();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ConstructorArgumentValues that)) {
            return false;
        }
        if (this.genericArgumentValues.size() != that.genericArgumentValues.size() || this.indexedArgumentValues.size() != that.indexedArgumentValues.size()) {
            return false;
        }
        Iterator<ValueHolder> it1 = this.genericArgumentValues.iterator();
        Iterator<ValueHolder> it2 = that.genericArgumentValues.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            ValueHolder vh1 = it1.next();
            ValueHolder vh2 = it2.next();
            if (!vh1.contentEquals(vh2)) {
                return false;
            }
        }
        for (Map.Entry<Integer, ValueHolder> entry : this.indexedArgumentValues.entrySet()) {
            ValueHolder vh1 = entry.getValue();
            ValueHolder vh2 = that.indexedArgumentValues.get(entry.getKey());
            if (vh2 == null || !vh1.contentEquals(vh2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 7;
        for (ValueHolder valueHolder : this.genericArgumentValues) {
            hashCode = 31 * hashCode + valueHolder.contentHashCode();
        }
        hashCode = 29 * hashCode;
        for (Map.Entry<Integer, ValueHolder> entry : this.indexedArgumentValues.entrySet()) {
            hashCode = 31 * hashCode + (entry.getValue().contentHashCode() ^ entry.getKey().hashCode());
        }
        return hashCode;
    }

    /**
     * 用于存储构造函数参数值的持有者，包含一个可选的类型属性，指示实际构造函数参数的目标类型。
     */
    public static class ValueHolder implements BeanMetadataElement {

        @Nullable
        private Object value;

        @Nullable
        private String type;

        @Nullable
        private String name;

        @Nullable
        private Object source;

        private boolean converted = false;

        @Nullable
        private Object convertedValue;

        /**
         * 为给定的值创建一个新的 ValueHolder。
         * @param value 给定的参数值
         */
        public ValueHolder(@Nullable Object value) {
            this.value = value;
        }

        /**
         * 为给定值和类型创建一个新的 ValueHolder 实例。
         * @param value 要传入的参数值
         * @param type 构造函数参数的类型
         */
        public ValueHolder(@Nullable Object value, @Nullable String type) {
            this.value = value;
            this.type = type;
        }

        /**
         * 为给定的值、类型和名称创建一个新的 ValueHolder。
         * @param value 输入的值
         * @param type 构造函数参数的类型
         * @param name 构造函数参数的名称
         */
        public ValueHolder(@Nullable Object value, @Nullable String type, @Nullable String name) {
            this.value = value;
            this.type = type;
            this.name = name;
        }

        /**
         * 设置构造函数参数的值。
         */
        public void setValue(@Nullable Object value) {
            this.value = value;
        }

        /**
         * 返回构造函数参数的值。
         */
        @Nullable
        public Object getValue() {
            return this.value;
        }

        /**
         * 设置构造函数参数的类型。
         */
        public void setType(@Nullable String type) {
            this.type = type;
        }

        /**
         * 返回构造函数参数的类型。
         */
        @Nullable
        public String getType() {
            return this.type;
        }

        /**
         * 设置构造函数参数的名称。
         */
        public void setName(@Nullable String name) {
            this.name = name;
        }

        /**
         * 返回构造函数参数的名称。
         */
        @Nullable
        public String getName() {
            return this.name;
        }

        /**
         * 设置此元数据元素的配置源对象。
         * <p>对象的准确类型将取决于所使用的配置机制。
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
         * 返回此持有者是否已经包含一个转换后的值（{@code true}），
         * 或者值是否还需要被转换（{@code false}）。
         */
        public synchronized boolean isConverted() {
            return this.converted;
        }

        /**
         * 设置构造函数参数的转换后值，
         * 在处理类型转换之后。
         */
        public synchronized void setConvertedValue(@Nullable Object value) {
            this.converted = (value != null);
            this.convertedValue = value;
        }

        /**
         * 返回构造函数参数的转换值，
         * 在处理类型转换之后。
         */
        @Nullable
        public synchronized Object getConvertedValue() {
            return this.convertedValue;
        }

        /**
         * 判断这个ValueHolder的内容是否与给定其他ValueHolder的内容相等
         * <p>注意，ValueHolder没有直接实现{@code equals}方法，
         * 以允许多个具有相同内容的ValueHolder实例存在于同一个Set中。
         */
        private boolean contentEquals(ValueHolder other) {
            return (this == other || (ObjectUtils.nullSafeEquals(this.value, other.value) && ObjectUtils.nullSafeEquals(this.type, other.type)));
        }

        /**
         * 确定此 ValueHolder 内容的哈希码。
         * <p>注意，ValueHolder 没有直接实现 {@code hashCode}，
         * 以允许具有相同内容的多个 ValueHolder 实例存在于同一个 Set 中。
         */
        private int contentHashCode() {
            return ObjectUtils.nullSafeHashCode(this.value) * 29 + ObjectUtils.nullSafeHashCode(this.type);
        }

        /**
         * 创建此ValueHolder的副本：即具有相同内容的独立ValueHolder实例。
         */
        public ValueHolder copy() {
            ValueHolder copy = new ValueHolder(this.value, this.type, this.name);
            copy.setSource(this.source);
            return copy;
        }
    }
}
