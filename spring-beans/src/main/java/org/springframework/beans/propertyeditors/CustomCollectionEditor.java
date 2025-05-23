// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，
* 无论是明示的还是暗示的。有关权限和限制的具体语言，
* 请参阅许可证。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 集合属性编辑器，将任何源集合转换为指定的目标集合类型。
 *
 * <p>默认情况下，已注册为Set、SortedSet和List，如果类型不匹配目标属性，将自动将任何给定的集合转换为这些目标类型之一。
 *
 * @author Juergen Hoeller
 * @since 1.1.3
 * @see java.util.Collection
 * @see java.util.Set
 * @see java.util.SortedSet
 * @see java.util.List
 */
public class CustomCollectionEditor extends PropertyEditorSupport {

    @SuppressWarnings("rawtypes")
    private final Class<? extends Collection> collectionType;

    private final boolean nullAsEmptyCollection;

    /**
     * 为给定的目标类型创建一个新的 CustomCollectionEditor，
     * 保持传入的 {@code null} 不变。
     * @param collectionType 目标类型，该类型需要是 Collection 的子接口或具体的 Collection 类
     * @see java.util.Collection
     * @see java.util.ArrayList
     * @see java.util.TreeSet
     * @see java.util.LinkedHashSet
     */
    @SuppressWarnings("rawtypes")
    public CustomCollectionEditor(Class<? extends Collection> collectionType) {
        this(collectionType, false);
    }

    /**
     * 为给定目标类型创建一个新的 CustomCollectionEditor。
     * <p>如果传入的值是给定类型，则直接使用。
     * 如果它是一个不同的 Collection 类型或数组，则将其转换为给定 Collection 类型的默认实现。
     * 如果值是其他任何内容，则将创建一个包含该单个值的目标 Collection。
     * <p>默认 Collection 实现如下：ArrayList 用于 List，TreeSet 用于 SortedSet，LinkedHashSet 用于 Set。
     * @param collectionType 需要是 Collection 或其子接口的子接口或具体 Collection 类型的目标类型
     * @param nullAsEmptyCollection 是否将传入的 {@code null} 值转换为适当类型的空 Collection
     * @see java.util.Collection
     * @see java.util.ArrayList
     * @see java.util.TreeSet
     * @see java.util.LinkedHashSet
     */
    @SuppressWarnings("rawtypes")
    public CustomCollectionEditor(Class<? extends Collection> collectionType, boolean nullAsEmptyCollection) {
        Assert.notNull(collectionType, "Collection type is required");
        if (!Collection.class.isAssignableFrom(collectionType)) {
            throw new IllegalArgumentException("Collection type [" + collectionType.getName() + "] does not implement [java.util.Collection]");
        }
        this.collectionType = collectionType;
        this.nullAsEmptyCollection = nullAsEmptyCollection;
    }

    /**
     * 将给定的文本值转换为包含单个元素的集合。
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(text);
    }

    /**
     * 将给定的值转换为目标类型的集合。
     */
    @Override
    public void setValue(@Nullable Object value) {
        if (value == null && this.nullAsEmptyCollection) {
            super.setValue(createCollection(this.collectionType, 0));
        } else if (value == null || (this.collectionType.isInstance(value) && !alwaysCreateNewCollection())) {
            // 直接使用源值，因为它与目标类型匹配。
            super.setValue(value);
        } else if (value instanceof Collection<?> source) {
            // 转换集合元素。
            Collection<Object> target = createCollection(this.collectionType, source.size());
            for (Object elem : source) {
                target.add(convertElement(elem));
            }
            super.setValue(target);
        } else if (value.getClass().isArray()) {
            // 将数组元素转换为集合元素。
            int length = Array.getLength(value);
            Collection<Object> target = createCollection(this.collectionType, length);
            for (int i = 0; i < length; i++) {
                target.add(convertElement(Array.get(value, i)));
            }
            super.setValue(target);
        } else {
            // 一个普通值：将其转换为包含单个元素的集合。
            Collection<Object> target = createCollection(this.collectionType, 1);
            target.add(convertElement(value));
            super.setValue(target);
        }
    }

    /**
     * 创建给定类型的集合，如果集合类型支持，则具有给定的初始容量。
     * @param collectionType 集合的子接口
     * @param initialCapacity 初始容量
     * @return 新的集合实例
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Collection<Object> createCollection(Class<? extends Collection> collectionType, int initialCapacity) {
        if (!collectionType.isInterface()) {
            try {
                return ReflectionUtils.accessibleConstructor(collectionType).newInstance();
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Could not instantiate collection class: " + collectionType.getName(), ex);
            }
        } else if (List.class == collectionType) {
            return new ArrayList<>(initialCapacity);
        } else if (SortedSet.class == collectionType) {
            return new TreeSet<>();
        } else {
            return new LinkedHashSet<>(initialCapacity);
        }
    }

    /**
     * 返回是否总是创建一个新的集合，
     * 即使传入的集合类型已经匹配。
     * <p>默认值为 "false"；可以被重写以强制创建一个新的集合，
     * 例如用于将元素转换为任何情况。
     * @see #convertElement
     */
    protected boolean alwaysCreateNewCollection() {
        return false;
    }

    /**
     * 用于转换每个遇到的集合/数组元素的钩子。
     * 默认实现简单地返回传入的元素而不做任何改变。
     * <p>可以被重写以执行特定元素的转换，例如如果传入的是一个字符串数组，并且应该被转换为整数对象的集合。
     * <p>仅在实际上创建新的集合时才会被调用！默认情况下，如果传入的集合的类型已经匹配，则不会创建新的集合。通过重写`#alwaysCreateNewCollection()`方法来强制在每种情况下都创建新的集合。
     * @param element 源元素
     * @return 在目标集合中使用的元素
     * @see #alwaysCreateNewCollection()
     */
    protected Object convertElement(Object element) {
        return element;
    }

    /**
     * 此实现返回 {@code null} 以表示没有适当的文本表示。
     */
    @Override
    @Nullable
    public String getAsText() {
        return null;
    }
}
