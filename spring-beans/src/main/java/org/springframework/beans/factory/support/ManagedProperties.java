// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.support;

import java.util.Properties;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;
import org.springframework.lang.Nullable;

/**
 * 标签类，代表一个由 Spring 管理的 {@link Properties} 实例，支持合并父/子定义。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class ManagedProperties extends Properties implements Mergeable, BeanMetadataElement {

    @Nullable
    private Object source;

    private boolean mergeEnabled;

    /**
     * 为此元数据元素设置配置源对象。
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
     * 设置是否应为此集合启用合并功能，
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
    public Object merge(@Nullable Object parent) {
        if (!this.mergeEnabled) {
            throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
        }
        if (parent == null) {
            return this;
        }
        if (!(parent instanceof Properties properties)) {
            throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
        }
        Properties merged = new ManagedProperties();
        merged.putAll(properties);
        merged.putAll(this);
        return merged;
    }
}
