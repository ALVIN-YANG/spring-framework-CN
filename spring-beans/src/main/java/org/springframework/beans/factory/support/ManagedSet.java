// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.support;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;
import org.springframework.lang.Nullable;

/**
 * 用于持有托管集合值的标签集合类，这些值可能包括运行时豆引用（将被解析为豆对象）。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 21.01.2004
 * @param <E> 元素类型
 */
@SuppressWarnings("serial")
public class ManagedSet<E> extends LinkedHashSet<E> implements Mergeable, BeanMetadataElement {

    @Nullable
    private Object source;

    @Nullable
    private String elementTypeName;

    private boolean mergeEnabled;

    public ManagedSet() {
    }

    public ManagedSet(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 创建一个包含任意数量元素的新实例。
     * @param elements 要包含在集合中的元素
     * @param <E> 集合的元素类型
     * @return 包含指定元素的一个 {@code ManagedSet}
     * @since 5.3.16
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <E> ManagedSet<E> of(E... elements) {
        ManagedSet<E> set = new ManagedSet<>();
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * 为此元数据元素设置配置源 {@code Object}。
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
     * 设置用于此集合的默认元素类型名称（类名）。
     */
    public void setElementTypeName(@Nullable String elementTypeName) {
        this.elementTypeName = elementTypeName;
    }

    /**
     * 返回用于此集合的默认元素类型名称（类名）。
     */
    @Nullable
    public String getElementTypeName() {
        return this.elementTypeName;
    }

    /**
     * 设置是否为该集合启用合并功能，
     * 在存在 '父' 集合值的情况下。
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
    public Set<E> merge(@Nullable Object parent) {
        if (!this.mergeEnabled) {
            throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
        }
        if (parent == null) {
            return this;
        }
        if (!(parent instanceof Set)) {
            throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
        }
        Set<E> merged = new ManagedSet<>();
        merged.addAll((Set<E>) parent);
        merged.addAll(this);
        return merged;
    }
}
