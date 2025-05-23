// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”）；除非符合许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;

/**
 * {@link PropertyAccessor}接口的抽象实现。
 * 提供所有便捷方法的基类实现，实际的属性访问由子类实现。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.0
 * @see #getPropertyValue
 * @see #setPropertyValue
 */
public abstract class AbstractPropertyAccessor extends TypeConverterSupport implements ConfigurablePropertyAccessor {

    private boolean extractOldValueForEditor = false;

    private boolean autoGrowNestedPaths = false;

    boolean suppressNotWritablePropertyException = false;

    @Override
    public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
        this.extractOldValueForEditor = extractOldValueForEditor;
    }

    @Override
    public boolean isExtractOldValueForEditor() {
        return this.extractOldValueForEditor;
    }

    @Override
    public void setAutoGrowNestedPaths(boolean autoGrowNestedPaths) {
        this.autoGrowNestedPaths = autoGrowNestedPaths;
    }

    @Override
    public boolean isAutoGrowNestedPaths() {
        return this.autoGrowNestedPaths;
    }

    @Override
    public void setPropertyValue(PropertyValue pv) throws BeansException {
        setPropertyValue(pv.getName(), pv.getValue());
    }

    @Override
    public void setPropertyValues(Map<?, ?> map) throws BeansException {
        setPropertyValues(new MutablePropertyValues(map));
    }

    @Override
    public void setPropertyValues(PropertyValues pvs) throws BeansException {
        setPropertyValues(pvs, false, false);
    }

    @Override
    public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) throws BeansException {
        setPropertyValues(pvs, ignoreUnknown, false);
    }

    @Override
    public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid) throws BeansException {
        List<PropertyAccessException> propertyAccessExceptions = null;
        List<PropertyValue> propertyValues = (pvs instanceof MutablePropertyValues mpvs ? mpvs.getPropertyValueList() : Arrays.asList(pvs.getPropertyValues()));
        if (ignoreUnknown) {
            this.suppressNotWritablePropertyException = true;
        }
        try {
            for (PropertyValue pv : propertyValues) {
                // setPropertyValue 方法可能会抛出任何 BeansException 异常，这些异常不会被捕获
                // 这里，如果发生严重故障，例如没有匹配的字段。
                // 我们可以尝试仅处理较轻的异常。
                try {
                    setPropertyValue(pv);
                } catch (NotWritablePropertyException ex) {
                    if (!ignoreUnknown) {
                        throw ex;
                    }
                    // 否则，就忽略它并继续...
                } catch (NullValueInNestedPathException ex) {
                    if (!ignoreInvalid) {
                        throw ex;
                    }
                    // 否则，就忽略它并继续...
                } catch (PropertyAccessException ex) {
                    if (propertyAccessExceptions == null) {
                        propertyAccessExceptions = new ArrayList<>();
                    }
                    propertyAccessExceptions.add(ex);
                }
            }
        } finally {
            if (ignoreUnknown) {
                this.suppressNotWritablePropertyException = false;
            }
        }
        // 如果我们遇到了单个异常，则抛出组合异常。
        if (propertyAccessExceptions != null) {
            PropertyAccessException[] paeArray = propertyAccessExceptions.toArray(new PropertyAccessException[0]);
            throw new PropertyBatchUpdateException(paeArray);
        }
    }

    // 以公共可见性重新定义。
    @Override
    @Nullable
    public Class<?> getPropertyType(String propertyPath) {
        return null;
    }

    /**
     * 实际上获取属性的值。
     * @param propertyName 要获取值的属性名称
     * @return 属性的值
     * @throws InvalidPropertyException 如果不存在该属性或者
     * 如果属性不可读时抛出
     * @throws PropertyAccessException 如果属性有效但访问器方法失败时抛出
     */
    @Override
    @Nullable
    public abstract Object getPropertyValue(String propertyName) throws BeansException;

    /**
     * 实际设置属性值。
     * @param propertyName 要设置的属性的名称
     * @param value 新的值
     * @throws InvalidPropertyException 如果不存在该属性或
     * 如果属性不可写
     * @throws PropertyAccessException 如果属性有效但访问器方法失败或发生类型不匹配
     */
    @Override
    public abstract void setPropertyValue(String propertyName, @Nullable Object value) throws BeansException;
}
