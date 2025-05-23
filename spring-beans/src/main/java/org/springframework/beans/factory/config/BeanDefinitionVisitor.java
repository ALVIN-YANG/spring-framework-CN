// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringValueResolver;

/**
 * 用于遍历 {@link BeanDefinition} 对象的访问者类，特别是其中包含的属性值和构造函数参数值，
 * 解析豆元数据值。
 *
 * <p>由 {@link PlaceholderConfigurerSupport} 使用，以解析 BeanDefinition 中包含的所有字符串值，
 * 解析找到的任何占位符。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.2
 * @see BeanDefinition
 * @see BeanDefinition#getPropertyValues
 * @see BeanDefinition#getConstructorArgumentValues
 * @see PlaceholderConfigurerSupport
 */
public class BeanDefinitionVisitor {

    @Nullable
    private StringValueResolver valueResolver;

    /**
     * 创建一个新的 BeanDefinitionVisitor，将指定的值解析器应用到所有 Bean 元数据值上。
     * @param valueResolver 要应用的字符串值解析器
     */
    public BeanDefinitionVisitor(StringValueResolver valueResolver) {
        Assert.notNull(valueResolver, "StringValueResolver must not be null");
        this.valueResolver = valueResolver;
    }

    /**
     * 创建一个新的 BeanDefinitionVisitor 用于继承。
     * 子类需要重写 {@link #resolveStringValue} 方法。
     */
    protected BeanDefinitionVisitor() {
    }

    /**
     * 遍历给定的 BeanDefinition 对象及其包含的 MutablePropertyValues 和 ConstructorArgumentValues。
     * @param beanDefinition 要遍历的 BeanDefinition 对象
     * @see #resolveStringValue(String)
     */
    public void visitBeanDefinition(BeanDefinition beanDefinition) {
        visitParentName(beanDefinition);
        visitBeanClassName(beanDefinition);
        visitFactoryBeanName(beanDefinition);
        visitFactoryMethodName(beanDefinition);
        visitScope(beanDefinition);
        if (beanDefinition.hasPropertyValues()) {
            visitPropertyValues(beanDefinition.getPropertyValues());
        }
        if (beanDefinition.hasConstructorArgumentValues()) {
            ConstructorArgumentValues cas = beanDefinition.getConstructorArgumentValues();
            visitIndexedArgumentValues(cas.getIndexedArgumentValues());
            visitGenericArgumentValues(cas.getGenericArgumentValues());
        }
    }

    protected void visitParentName(BeanDefinition beanDefinition) {
        String parentName = beanDefinition.getParentName();
        if (parentName != null) {
            String resolvedName = resolveStringValue(parentName);
            if (!parentName.equals(resolvedName)) {
                beanDefinition.setParentName(resolvedName);
            }
        }
    }

    protected void visitBeanClassName(BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        if (beanClassName != null) {
            String resolvedName = resolveStringValue(beanClassName);
            if (!beanClassName.equals(resolvedName)) {
                beanDefinition.setBeanClassName(resolvedName);
            }
        }
    }

    protected void visitFactoryBeanName(BeanDefinition beanDefinition) {
        String factoryBeanName = beanDefinition.getFactoryBeanName();
        if (factoryBeanName != null) {
            String resolvedName = resolveStringValue(factoryBeanName);
            if (!factoryBeanName.equals(resolvedName)) {
                beanDefinition.setFactoryBeanName(resolvedName);
            }
        }
    }

    protected void visitFactoryMethodName(BeanDefinition beanDefinition) {
        String factoryMethodName = beanDefinition.getFactoryMethodName();
        if (factoryMethodName != null) {
            String resolvedName = resolveStringValue(factoryMethodName);
            if (!factoryMethodName.equals(resolvedName)) {
                beanDefinition.setFactoryMethodName(resolvedName);
            }
        }
    }

    protected void visitScope(BeanDefinition beanDefinition) {
        String scope = beanDefinition.getScope();
        if (scope != null) {
            String resolvedScope = resolveStringValue(scope);
            if (!scope.equals(resolvedScope)) {
                beanDefinition.setScope(resolvedScope);
            }
        }
    }

    protected void visitPropertyValues(MutablePropertyValues pvs) {
        PropertyValue[] pvArray = pvs.getPropertyValues();
        for (PropertyValue pv : pvArray) {
            Object newVal = resolveValue(pv.getValue());
            if (!ObjectUtils.nullSafeEquals(newVal, pv.getValue())) {
                pvs.add(pv.getName(), newVal);
            }
        }
    }

    protected void visitIndexedArgumentValues(Map<Integer, ConstructorArgumentValues.ValueHolder> ias) {
        for (ConstructorArgumentValues.ValueHolder valueHolder : ias.values()) {
            Object newVal = resolveValue(valueHolder.getValue());
            if (!ObjectUtils.nullSafeEquals(newVal, valueHolder.getValue())) {
                valueHolder.setValue(newVal);
            }
        }
    }

    protected void visitGenericArgumentValues(List<ConstructorArgumentValues.ValueHolder> gas) {
        for (ConstructorArgumentValues.ValueHolder valueHolder : gas) {
            Object newVal = resolveValue(valueHolder.getValue());
            if (!ObjectUtils.nullSafeEquals(newVal, valueHolder.getValue())) {
                valueHolder.setValue(newVal);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    protected Object resolveValue(@Nullable Object value) {
        if (value instanceof BeanDefinition beanDef) {
            visitBeanDefinition(beanDef);
        } else if (value instanceof BeanDefinitionHolder beanDefHolder) {
            visitBeanDefinition(beanDefHolder.getBeanDefinition());
        } else if (value instanceof RuntimeBeanReference ref) {
            String newBeanName = resolveStringValue(ref.getBeanName());
            if (newBeanName == null) {
                return null;
            }
            if (!newBeanName.equals(ref.getBeanName())) {
                return new RuntimeBeanReference(newBeanName);
            }
        } else if (value instanceof RuntimeBeanNameReference ref) {
            String newBeanName = resolveStringValue(ref.getBeanName());
            if (newBeanName == null) {
                return null;
            }
            if (!newBeanName.equals(ref.getBeanName())) {
                return new RuntimeBeanNameReference(newBeanName);
            }
        } else if (value instanceof Object[] array) {
            visitArray(array);
        } else if (value instanceof List list) {
            visitList(list);
        } else if (value instanceof Set set) {
            visitSet(set);
        } else if (value instanceof Map map) {
            visitMap(map);
        } else if (value instanceof TypedStringValue typedStringValue) {
            String stringValue = typedStringValue.getValue();
            if (stringValue != null) {
                String visitedString = resolveStringValue(stringValue);
                typedStringValue.setValue(visitedString);
            }
        } else if (value instanceof String strValue) {
            return resolveStringValue(strValue);
        }
        return value;
    }

    protected void visitArray(Object[] arrayVal) {
        for (int i = 0; i < arrayVal.length; i++) {
            Object elem = arrayVal[i];
            Object newVal = resolveValue(elem);
            if (!ObjectUtils.nullSafeEquals(newVal, elem)) {
                arrayVal[i] = newVal;
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void visitList(List listVal) {
        for (int i = 0; i < listVal.size(); i++) {
            Object elem = listVal.get(i);
            Object newVal = resolveValue(elem);
            if (!ObjectUtils.nullSafeEquals(newVal, elem)) {
                listVal.set(i, newVal);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void visitSet(Set setVal) {
        Set newContent = new LinkedHashSet();
        boolean entriesModified = false;
        for (Object elem : setVal) {
            int elemHash = (elem != null ? elem.hashCode() : 0);
            Object newVal = resolveValue(elem);
            int newValHash = (newVal != null ? newVal.hashCode() : 0);
            newContent.add(newVal);
            entriesModified = entriesModified || (newVal != elem || newValHash != elemHash);
        }
        if (entriesModified) {
            setVal.clear();
            setVal.addAll(newContent);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void visitMap(Map<?, ?> mapVal) {
        Map newContent = new LinkedHashMap();
        boolean entriesModified = false;
        for (Map.Entry entry : mapVal.entrySet()) {
            Object key = entry.getKey();
            int keyHash = (key != null ? key.hashCode() : 0);
            Object newKey = resolveValue(key);
            int newKeyHash = (newKey != null ? newKey.hashCode() : 0);
            Object val = entry.getValue();
            Object newVal = resolveValue(val);
            newContent.put(newKey, newVal);
            entriesModified = entriesModified || (newVal != val || newKey != key || newKeyHash != keyHash);
        }
        if (entriesModified) {
            mapVal.clear();
            mapVal.putAll(newContent);
        }
    }

    /**
     * 解析给定的字符串值，例如解析占位符。
     * @param strVal 原始字符串值
     * @return 解析后的字符串值
     */
    @Nullable
    protected String resolveStringValue(String strVal) {
        if (this.valueResolver == null) {
            throw new IllegalStateException("No StringValueResolver specified - pass a resolver " + "object into the constructor or override the 'resolveStringValue' method");
        }
        String resolvedValue = this.valueResolver.resolveStringValue(strVal);
        // 如果字符串未被修改，则返回原始字符串。
        return (strVal.equals(resolvedValue) ? strVal : resolvedValue);
    }
}
