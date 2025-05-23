// 翻译完成 glm-4-flash
/** 版权所有 2002-2020，原始作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory.config;

import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

/**
 * 简单工厂，用于创建共享的 Map 实例。允许通过 XML bean 定义中的 "map" 元素集中设置 Maps。
 *
 * @author Juergen Hoeller
 * @since 09.12.2003
 * @see SetFactoryBean
 * @see ListFactoryBean
 */
public class MapFactoryBean extends AbstractFactoryBean<Map<Object, Object>> {

    @Nullable
    private Map<?, ?> sourceMap;

    @SuppressWarnings("rawtypes")
    @Nullable
    private Class<? extends Map> targetMapClass;

    /**
     * 设置源 Map，通常通过 XML 中的 "map" 元素填充。
     */
    public void setSourceMap(Map<?, ?> sourceMap) {
        this.sourceMap = sourceMap;
    }

    /**
     * 设置用于目标 Map 的类。当在 Spring 应用程序上下文中定义时，可以使用完全限定的类名进行填充。
     * <p>默认为LinkedHashMap，保持注册顺序。
     * @see java.util.LinkedHashMap
     */
    @SuppressWarnings("rawtypes")
    public void setTargetMapClass(@Nullable Class<? extends Map> targetMapClass) {
        if (targetMapClass == null) {
            throw new IllegalArgumentException("'targetMapClass' must not be null");
        }
        if (!Map.class.isAssignableFrom(targetMapClass)) {
            throw new IllegalArgumentException("'targetMapClass' must implement [java.util.Map]");
        }
        this.targetMapClass = targetMapClass;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<Map> getObjectType() {
        return Map.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<Object, Object> createInstance() {
        if (this.sourceMap == null) {
            throw new IllegalArgumentException("'sourceMap' is required");
        }
        Map<Object, Object> result = null;
        if (this.targetMapClass != null) {
            result = BeanUtils.instantiateClass(this.targetMapClass);
        } else {
            result = CollectionUtils.newLinkedHashMap(this.sourceMap.size());
        }
        Class<?> keyType = null;
        Class<?> valueType = null;
        if (this.targetMapClass != null) {
            ResolvableType mapType = ResolvableType.forClass(this.targetMapClass).asMap();
            keyType = mapType.resolveGeneric(0);
            valueType = mapType.resolveGeneric(1);
        }
        if (keyType != null || valueType != null) {
            TypeConverter converter = getBeanTypeConverter();
            for (Map.Entry<?, ?> entry : this.sourceMap.entrySet()) {
                Object convertedKey = converter.convertIfNecessary(entry.getKey(), keyType);
                Object convertedValue = converter.convertIfNecessary(entry.getValue(), valueType);
                result.put(convertedKey, convertedValue);
            }
        } else {
            result.putAll(this.sourceMap);
        }
        return result;
    }
}
