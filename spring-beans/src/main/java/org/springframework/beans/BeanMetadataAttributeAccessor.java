// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import org.springframework.core.AttributeAccessorSupport;
import org.springframework.lang.Nullable;

/**
 * 扩展自 {@link org.springframework.core.AttributeAccessorSupport}，
 * 将属性以 {@link BeanMetadataAttribute} 对象的形式持有，以便跟踪定义源。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
@SuppressWarnings("serial")
public class BeanMetadataAttributeAccessor extends AttributeAccessorSupport implements BeanMetadataElement {

    @Nullable
    private Object source;

    /**
     * 为此元数据元素设置配置源 {@code Object}。
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
     * 将给定的BeanMetadataAttribute添加到这个访问器的属性集中。
     * @param attribute 要注册的BeanMetadataAttribute对象
     */
    public void addMetadataAttribute(BeanMetadataAttribute attribute) {
        super.setAttribute(attribute.getName(), attribute);
    }

    /**
     * 在此访问器的属性集中查找给定的 BeanMetadataAttribute。
     * @param name 属性的名称
     * @return 对应的 BeanMetadataAttribute 对象，
     * 或者在没有定义此类属性时返回 {@code null}
     */
    @Nullable
    public BeanMetadataAttribute getMetadataAttribute(String name) {
        return (BeanMetadataAttribute) super.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, @Nullable Object value) {
        super.setAttribute(name, new BeanMetadataAttribute(name, value));
    }

    @Override
    @Nullable
    public Object getAttribute(String name) {
        BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.getAttribute(name);
        return (attribute != null ? attribute.getValue() : null);
    }

    @Override
    @Nullable
    public Object removeAttribute(String name) {
        BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.removeAttribute(name);
        return (attribute != null ? attribute.getValue() : null);
    }
}
