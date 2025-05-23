// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0 版本（以下简称“许可证”）许可；
* 除非符合许可证要求，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.config;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 简单的共享 Set 实例工厂。允许通过 XML bean 定义中的 "set" 元素集中设置 Sets。
 *
 * @author Juergen Hoeller
 * @since 09.12.2003
 * @see ListFactoryBean
 * @see MapFactoryBean
 */
public class SetFactoryBean extends AbstractFactoryBean<Set<Object>> {

    @Nullable
    private Set<?> sourceSet;

    @SuppressWarnings("rawtypes")
    @Nullable
    private Class<? extends Set> targetSetClass;

    /**
     * 设置源 Set，通常通过 XML 中的 "set" 元素填充。
     */
    public void setSourceSet(Set<?> sourceSet) {
        this.sourceSet = sourceSet;
    }

    /**
     * 设置用于目标Set的类。当在Spring应用上下文中定义时，可以使用完全限定类名进行填充。
     * <p>默认为LinkedHashSet，保持注册顺序。
     * @see java.util.LinkedHashSet
     */
    @SuppressWarnings("rawtypes")
    public void setTargetSetClass(@Nullable Class<? extends Set> targetSetClass) {
        if (targetSetClass == null) {
            throw new IllegalArgumentException("'targetSetClass' must not be null");
        }
        if (!Set.class.isAssignableFrom(targetSetClass)) {
            throw new IllegalArgumentException("'targetSetClass' must implement [java.util.Set]");
        }
        this.targetSetClass = targetSetClass;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<Set> getObjectType() {
        return Set.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Set<Object> createInstance() {
        if (this.sourceSet == null) {
            throw new IllegalArgumentException("'sourceSet' is required");
        }
        Set<Object> result = null;
        if (this.targetSetClass != null) {
            result = BeanUtils.instantiateClass(this.targetSetClass);
        } else {
            result = new LinkedHashSet<>(this.sourceSet.size());
        }
        Class<?> valueType = null;
        if (this.targetSetClass != null) {
            valueType = ResolvableType.forClass(this.targetSetClass).asCollection().resolveGeneric();
        }
        if (valueType != null) {
            TypeConverter converter = getBeanTypeConverter();
            for (Object elem : this.sourceSet) {
                result.add(converter.convertIfNecessary(elem, valueType));
            }
        } else {
            result.addAll(this.sourceSet);
        }
        return result;
    }
}
