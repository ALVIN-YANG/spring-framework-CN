// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式，无论是明示的还是暗示的，
* 保证或条件。请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * {@link PropertyValues} 接口的默认实现。
 * 允许对属性进行简单操作，并提供构造函数以支持深度复制和从 Map 构建实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2001年5月13日
 */
@SuppressWarnings("serial")
public class MutablePropertyValues implements PropertyValues, Serializable {

    private final List<PropertyValue> propertyValueList;

    @Nullable
    private Set<String> processedProperties;

    private volatile boolean converted;

    /**
     * 创建一个新的空MutablePropertyValues对象。
     * <p>可以使用{@code add}方法添加属性值。
     * @see #add(String, Object)
     */
    public MutablePropertyValues() {
        this.propertyValueList = new ArrayList<>(0);
    }

    /**
     * 深度复制构造函数。保证 PropertyValue 引用是独立的，尽管目前它无法深度复制由个别 PropertyValue 对象引用的对象。
     * @param original 要复制的 PropertyValues
     * @see #addPropertyValues(PropertyValues)
     */
    public MutablePropertyValues(@Nullable PropertyValues original) {
        // 我们可以进行优化，因为这都是全新的。
        // 现有属性值不会被替换。
        if (original != null) {
            PropertyValue[] pvs = original.getPropertyValues();
            this.propertyValueList = new ArrayList<>(pvs.length);
            for (PropertyValue pv : pvs) {
                this.propertyValueList.add(new PropertyValue(pv));
            }
        } else {
            this.propertyValueList = new ArrayList<>(0);
        }
    }

    /**
     * 从一个 Map 中构建一个新的 MutablePropertyValues 对象。
     * @param original 一个以属性名称字符串为键的属性值 Map
     * @see #addPropertyValues(Map)
     */
    public MutablePropertyValues(@Nullable Map<?, ?> original) {
        // 我们可以优化这部分，因为它完全是新的：
        // 现有属性值无法被替换。
        if (original != null) {
            this.propertyValueList = new ArrayList<>(original.size());
            original.forEach((attrName, attrValue) -> this.propertyValueList.add(new PropertyValue(attrName.toString(), attrValue)));
        } else {
            this.propertyValueList = new ArrayList<>(0);
        }
    }

    /**
     * 使用给定的 PropertyValue 对象列表直接构造一个新的 MutablePropertyValues 对象。
     * <p>这是一个用于高级使用场景的构造函数。
     * 它不适用于典型的编程使用。
     * @param propertyValueList PropertyValue 对象列表
     */
    public MutablePropertyValues(@Nullable List<PropertyValue> propertyValueList) {
        this.propertyValueList = (propertyValueList != null ? propertyValueList : new ArrayList<>());
    }

    /**
     * 返回 PropertyValue 对象的底层 List 的原始形式。
     * 返回的 List 可以直接修改，尽管这不被推荐。
     * <p>这是一个用于优化访问所有 PropertyValue 对象的访问器。
     * 它不适用于典型的程序性使用。
     */
    public List<PropertyValue> getPropertyValueList() {
        return this.propertyValueList;
    }

    /**
     * 返回列表中 PropertyValue 条目的数量。
     */
    public int size() {
        return this.propertyValueList.size();
    }

    /**
     * 将所有给定的 PropertyValues 复制到这个对象中。保证 PropertyValue 引用是独立的，尽管目前无法深度复制由个别 PropertyValue 对象引用的对象。
     * @param other 要复制的 PropertyValues
     * @return 返回 this 以允许在链中添加多个属性值
     */
    public MutablePropertyValues addPropertyValues(@Nullable PropertyValues other) {
        if (other != null) {
            PropertyValue[] pvs = other.getPropertyValues();
            for (PropertyValue pv : pvs) {
                addPropertyValue(new PropertyValue(pv));
            }
        }
        return this;
    }

    /**
     * 将给定 Map 中的所有属性值添加进来。
     * @param other 一个 Map，其属性值通过属性名键入，该键必须是 String 类型
     * @return 返回 this，以便允许链式添加多个属性值
     */
    public MutablePropertyValues addPropertyValues(@Nullable Map<?, ?> other) {
        if (other != null) {
            other.forEach((attrName, attrValue) -> addPropertyValue(new PropertyValue(attrName.toString(), attrValue)));
        }
        return this;
    }

    /**
     * 添加一个 PropertyValue 对象，如果对应属性已存在，则替换它；如果适用，则与其合并。
     * @param pv 要添加的 PropertyValue 对象
     * @return 返回 this 以允许在链中添加多个属性值
     */
    public MutablePropertyValues addPropertyValue(PropertyValue pv) {
        for (int i = 0; i < this.propertyValueList.size(); i++) {
            PropertyValue currentPv = this.propertyValueList.get(i);
            if (currentPv.getName().equals(pv.getName())) {
                pv = mergeIfRequired(pv, currentPv);
                setPropertyValueAt(pv, i);
                return this;
            }
        }
        this.propertyValueList.add(pv);
        return this;
    }

    /**
     * 重载的 {@code addPropertyValue} 版本，它接受一个属性名称和一个属性值。
     * <p>注意：我们建议使用更简洁且具有链式调用能力的变体 {@link #add(String, Object)}。
     * @param propertyName 属性名称
     * @param propertyValue 属性值
     * @see #addPropertyValue(PropertyValue)
     */
    public void addPropertyValue(String propertyName, Object propertyValue) {
        addPropertyValue(new PropertyValue(propertyName, propertyValue));
    }

    /**
     * 添加一个 PropertyValue 对象，如果对应属性已存在，则替换它；如果适用，则与它合并。
     * @param propertyName 属性名称
     * @param propertyValue 属性值
     * @return 返回 this 以允许在链中添加多个属性值
     */
    public MutablePropertyValues add(String propertyName, @Nullable Object propertyValue) {
        addPropertyValue(new PropertyValue(propertyName, propertyValue));
        return this;
    }

    /**
     * 修改此对象中持有的 PropertyValue 对象。
     * 从 0 开始索引。
     */
    public void setPropertyValueAt(PropertyValue pv, int i) {
        this.propertyValueList.set(i, pv);
    }

    /**
     * 如果支持并启用了合并，则将提供的 'new' {@link PropertyValue} 的值与当前 {@link PropertyValue} 的值合并。
     * @see Mergeable
     */
    private PropertyValue mergeIfRequired(PropertyValue newPv, PropertyValue currentPv) {
        Object value = newPv.getValue();
        if (value instanceof Mergeable mergeable) {
            if (mergeable.isMergeEnabled()) {
                Object merged = mergeable.merge(currentPv.getValue());
                return new PropertyValue(newPv.getName(), merged);
            }
        }
        return newPv;
    }

    /**
     * 如果存在，则移除给定的PropertyValue。
     * @param pv 要移除的PropertyValue
     */
    public void removePropertyValue(PropertyValue pv) {
        this.propertyValueList.remove(pv);
    }

    /**
     * 重载的 {@code removePropertyValue} 版本，接受一个属性名。
     * @param propertyName 属性名
     * @see #removePropertyValue(PropertyValue)
     */
    public void removePropertyValue(String propertyName) {
        this.propertyValueList.remove(getPropertyValue(propertyName));
    }

    @Override
    public Iterator<PropertyValue> iterator() {
        return Collections.unmodifiableList(this.propertyValueList).iterator();
    }

    @Override
    public Spliterator<PropertyValue> spliterator() {
        return Spliterators.spliterator(this.propertyValueList, 0);
    }

    @Override
    public Stream<PropertyValue> stream() {
        return this.propertyValueList.stream();
    }

    @Override
    public PropertyValue[] getPropertyValues() {
        return this.propertyValueList.toArray(new PropertyValue[0]);
    }

    @Override
    @Nullable
    public PropertyValue getPropertyValue(String propertyName) {
        for (PropertyValue pv : this.propertyValueList) {
            if (pv.getName().equals(propertyName)) {
                return pv;
            }
        }
        return null;
    }

    /**
     * 获取原始属性值（如果存在）。
     * @param propertyName 要搜索的属性名称
     * @return 返回原始属性值，如果没有找到则返回 {@code null}
     * @since 4.0
     * @see #getPropertyValue(String)
     * @see PropertyValue#getValue()
     */
    @Nullable
    public Object get(String propertyName) {
        PropertyValue pv = getPropertyValue(propertyName);
        return (pv != null ? pv.getValue() : null);
    }

    @Override
    public PropertyValues changesSince(PropertyValues old) {
        MutablePropertyValues changes = new MutablePropertyValues();
        if (old == this) {
            return changes;
        }
        // 对于新集合中的每个属性值
        for (PropertyValue newPv : this.propertyValueList) {
            // 如果没有旧的（对象或值），则添加它
            PropertyValue pvOld = old.getPropertyValue(newPv.getName());
            if (pvOld == null || !pvOld.equals(newPv)) {
                changes.addPropertyValue(newPv);
            }
        }
        return changes;
    }

    @Override
    public boolean contains(String propertyName) {
        return (getPropertyValue(propertyName) != null || (this.processedProperties != null && this.processedProperties.contains(propertyName)));
    }

    @Override
    public boolean isEmpty() {
        return this.propertyValueList.isEmpty();
    }

    /**
     * 将指定的属性注册为“已处理”，即在某种处理器在PropertyValue(s)机制之外调用相应的setter方法的情况下。
     * <p>这将导致在指定属性的{@code #contains}调用中返回值为{@code true}。
     * @param propertyName 属性的名称。
     */
    public void registerProcessedProperty(String propertyName) {
        if (this.processedProperties == null) {
            this.processedProperties = new HashSet<>(4);
        }
        this.processedProperties.add(propertyName);
    }

    /**
     * 清除给定属性的“已处理”注册，如果有的话。
     * @since 3.2.13
     */
    public void clearProcessedProperty(String propertyName) {
        if (this.processedProperties != null) {
            this.processedProperties.remove(propertyName);
        }
    }

    /**
     * 将此持有者标记为仅包含转换后的值
     * （即不再需要运行时解析）。
     */
    public void setConverted() {
        this.converted = true;
    }

    /**
     * 返回此持有者是否仅包含转换后的值（返回值为{@code true}），
     * 或者值仍然需要被转换（返回值为{@code false}）。
     */
    public boolean isConverted() {
        return this.converted;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof MutablePropertyValues that && this.propertyValueList.equals(that.propertyValueList)));
    }

    @Override
    public int hashCode() {
        return this.propertyValueList.hashCode();
    }

    @Override
    public String toString() {
        PropertyValue[] pvs = getPropertyValues();
        if (pvs.length > 0) {
            return "PropertyValues: length=" + pvs.length + "; " + StringUtils.arrayToDelimitedString(pvs, "; ");
        }
        return "PropertyValues: length=0";
    }
}
