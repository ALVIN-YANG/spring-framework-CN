// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下网址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可范围和限制。*/
package org.springframework.beans.factory.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;
import org.springframework.lang.Nullable;

/**
 * 用于存储托管 List 元素的标签集合类，这些元素可能包括运行时 Bean 引用（将被解析为 Bean 对象）。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 27.05.2003
 * @param <E> 元素类型
 */
@SuppressWarnings("serial")
public class ManagedList<E> extends ArrayList<E> implements Mergeable, BeanMetadataElement {

    @Nullable
    private Object source;

    @Nullable
    private String elementTypeName;

    private boolean mergeEnabled;

    public ManagedList() {
    }

    public ManagedList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 创建一个包含任意数量元素的新实例。
     * @param elements 列表中要包含的元素
     * @param <E> 列表的元素类型
     * @return 包含指定元素的一个 {@code ManagedList}
     * @since 5.3.16
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <E> ManagedList<E> of(E... elements) {
        ManagedList<E> list = new ManagedList<>();
        Collections.addAll(list, elements);
        return list;
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
     * 设置用于此列表的默认元素类型名称（类名称）。
     */
    public void setElementTypeName(String elementTypeName) {
        this.elementTypeName = elementTypeName;
    }

    /**
     * 返回用于此列表的默认元素类型（类名）。
     */
    @Nullable
    public String getElementTypeName() {
        return this.elementTypeName;
    }

    /**
     * 设置是否应该为此集合启用合并功能，
     * 如果存在'父'集合值时。
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
    public List<E> merge(@Nullable Object parent) {
        if (!this.mergeEnabled) {
            throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
        }
        if (parent == null) {
            return this;
        }
        if (!(parent instanceof List)) {
            throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
        }
        List<E> merged = new ManagedList<>();
        merged.addAll((List<E>) parent);
        merged.addAll(this);
        return merged;
    }
}
