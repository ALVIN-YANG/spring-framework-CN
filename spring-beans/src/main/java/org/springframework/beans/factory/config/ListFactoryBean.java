// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，无论是关于其适用性还是其特定用途。
* 请参阅许可证以了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 简单工厂，用于创建共享的 List 实例。允许通过 XML bean 定义中的 "list" 元素集中设置 List。
 *
 * @author Juergen Hoeller
 * @since 09.12.2003
 * @see SetFactoryBean
 * @see MapFactoryBean
 */
public class ListFactoryBean extends AbstractFactoryBean<List<Object>> {

    @Nullable
    private List<?> sourceList;

    @SuppressWarnings("rawtypes")
    @Nullable
    private Class<? extends List> targetListClass;

    /**
     * 设置源列表，通常通过XML中的"list"元素填充。
     */
    public void setSourceList(List<?> sourceList) {
        this.sourceList = sourceList;
    }

    /**
     * 设置用于目标List的类。当在Spring应用上下文中定义时，可以用完全限定的类名填充。
     * 默认是 {@code java.util.ArrayList}。
     * @see java.util.ArrayList
     */
    @SuppressWarnings("rawtypes")
    public void setTargetListClass(@Nullable Class<? extends List> targetListClass) {
        if (targetListClass == null) {
            throw new IllegalArgumentException("'targetListClass' must not be null");
        }
        if (!List.class.isAssignableFrom(targetListClass)) {
            throw new IllegalArgumentException("'targetListClass' must implement [java.util.List]");
        }
        this.targetListClass = targetListClass;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<List> getObjectType() {
        return List.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Object> createInstance() {
        if (this.sourceList == null) {
            throw new IllegalArgumentException("'sourceList' is required");
        }
        List<Object> result = null;
        if (this.targetListClass != null) {
            result = BeanUtils.instantiateClass(this.targetListClass);
        } else {
            result = new ArrayList<>(this.sourceList.size());
        }
        Class<?> valueType = null;
        if (this.targetListClass != null) {
            valueType = ResolvableType.forClass(this.targetListClass).asCollection().resolveGeneric();
        }
        if (valueType != null) {
            TypeConverter converter = getBeanTypeConverter();
            for (Object elem : this.sourceList) {
                result.add(converter.convertIfNecessary(elem, valueType));
            }
        } else {
            result.addAll(this.sourceList);
        }
        return result;
    }
}
