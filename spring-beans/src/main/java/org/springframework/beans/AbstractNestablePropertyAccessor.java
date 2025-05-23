// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 一个基本的 {@link ConfigurablePropertyAccessor}，为所有典型用例提供必要的
 * 基础设施。
 *
 * <p>此访问器将根据需要将集合和数组值转换为相应的目标集合或数组。处理集合或数组的自定义属性编辑器可以通过 PropertyEditor 的 {@code setValue} 方法编写，或者通过逗号分隔的字符串通过 {@code setAsText} 方法编写，因为如果数组本身不可赋值，字符串数组将以这种格式转换。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 4.2
 * @see #registerCustomEditor
 * @see #setPropertyValues
 * @see #setPropertyValue
 * @see #getPropertyValue
 * @see #getPropertyType
 * @see BeanWrapper
 * @see PropertyEditorRegistrySupport
 */
public abstract class AbstractNestablePropertyAccessor extends AbstractPropertyAccessor {

    /**
     * 我们会创建很多这样的对象，因此我们不希望每次都创建一个新的记录器。
     */
    private static final Log logger = LogFactory.getLog(AbstractNestablePropertyAccessor.class);

    private int autoGrowCollectionLimit = Integer.MAX_VALUE;

    @Nullable
    Object wrappedObject;

    private String nestedPath = "";

    @Nullable
    Object rootObject;

    /**
     * 包含缓存的嵌套访问器的映射：嵌套路径 -> 访问器实例。
     */
    @Nullable
    private Map<String, AbstractNestablePropertyAccessor> nestedPropertyAccessors;

    /**
     * 创建一个新的空访问器。之后需要设置包装实例。
     * 注册默认编辑器。
     * @see #setWrappedInstance
     */
    protected AbstractNestablePropertyAccessor() {
        this(true);
    }

    /**
     * 创建一个新的空访问器。稍后需要设置封装的实例。
     * @param registerDefaultEditors 是否注册默认编辑器
     * （如果访问器不需要任何类型转换，可以抑制注册）
     * @see #setWrappedInstance
     */
    protected AbstractNestablePropertyAccessor(boolean registerDefaultEditors) {
        if (registerDefaultEditors) {
            registerDefaultEditors();
        }
        this.typeConverterDelegate = new TypeConverterDelegate(this);
    }

    /**
     * 为给定对象创建一个新的访问器。
     * @param object 由此访问器包装的对象
     */
    protected AbstractNestablePropertyAccessor(Object object) {
        registerDefaultEditors();
        setWrappedInstance(object);
    }

    /**
     * 创建一个新的访问器，封装指定类的实例。
     * @param clazz 要实例化并封装的类
     */
    protected AbstractNestablePropertyAccessor(Class<?> clazz) {
        registerDefaultEditors();
        setWrappedInstance(BeanUtils.instantiateClass(clazz));
    }

    /**
     * 为给定对象创建一个新的访问器，
     * 注册对象所在的嵌套路径。
     * @param object 由此访问器包装的对象
     * @param nestedPath 对象的嵌套路径
     * @param rootObject 路径顶部的根对象
     */
    protected AbstractNestablePropertyAccessor(Object object, String nestedPath, Object rootObject) {
        registerDefaultEditors();
        setWrappedInstance(object, nestedPath, rootObject);
    }

    /**
     * 为给定对象创建一个新的访问器，
     * 注册对象所在的嵌套路径。
     * @param object 由此访问器包裹的对象
     * @param nestedPath 对象的嵌套路径
     * @param parent 包含的访问器（不得为 {@code null}）
     */
    protected AbstractNestablePropertyAccessor(Object object, String nestedPath, AbstractNestablePropertyAccessor parent) {
        setWrappedInstance(object, nestedPath, parent.getWrappedInstance());
        setExtractOldValueForEditor(parent.isExtractOldValueForEditor());
        setAutoGrowNestedPaths(parent.isAutoGrowNestedPaths());
        setAutoGrowCollectionLimit(parent.getAutoGrowCollectionLimit());
        setConversionService(parent.getConversionService());
    }

    /**
     * 为数组及集合的自动扩容指定一个限制。
     * <p>默认情况下，在普通访问器中为无限。
     */
    public void setAutoGrowCollectionLimit(int autoGrowCollectionLimit) {
        this.autoGrowCollectionLimit = autoGrowCollectionLimit;
    }

    /**
     * 返回数组及集合自动增长的限制。
     */
    public int getAutoGrowCollectionLimit() {
        return this.autoGrowCollectionLimit;
    }

    /**
     * 切换目标对象，仅在新的对象与被替换对象的类不同时，替换缓存的反射结果。
     * @param object 新的目标对象
     */
    public void setWrappedInstance(Object object) {
        setWrappedInstance(object, "", null);
    }

    /**
     * 切换目标对象，仅当新对象的类与被替换对象的类不同时，才替换缓存的反射结果。
     * @param object 新的目标对象
     * @param nestedPath 对象的嵌套路径
     * @param rootObject 路径顶部的根对象
     */
    public void setWrappedInstance(Object object, @Nullable String nestedPath, @Nullable Object rootObject) {
        this.wrappedObject = ObjectUtils.unwrapOptional(object);
        Assert.notNull(this.wrappedObject, "Target object must not be null");
        this.nestedPath = (nestedPath != null ? nestedPath : "");
        this.rootObject = (!this.nestedPath.isEmpty() ? rootObject : this.wrappedObject);
        this.nestedPropertyAccessors = null;
        this.typeConverterDelegate = new TypeConverterDelegate(this, this.wrappedObject);
    }

    public final Object getWrappedInstance() {
        Assert.state(this.wrappedObject != null, "No wrapped object");
        return this.wrappedObject;
    }

    public final Class<?> getWrappedClass() {
        return getWrappedInstance().getClass();
    }

    /**
     * 返回由该访问器包装的对象的嵌套路径。
     */
    public final String getNestedPath() {
        return this.nestedPath;
    }

    /**
     * 返回此访问器路径顶端的根对象。
     * @see #getNestedPath
     */
    public final Object getRootInstance() {
        Assert.state(this.rootObject != null, "No root object");
        return this.rootObject;
    }

    /**
     * 返回此访问器路径顶端的根对象的类。
     * @see #getNestedPath
     */
    public final Class<?> getRootClass() {
        return getRootInstance().getClass();
    }

    @Override
    public void setPropertyValue(String propertyName, @Nullable Object value) throws BeansException {
        AbstractNestablePropertyAccessor nestedPa;
        try {
            nestedPa = getPropertyAccessorForPropertyPath(propertyName);
        } catch (NotReadablePropertyException ex) {
            throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName, "Nested property in path '" + propertyName + "' does not exist", ex);
        }
        PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedPa, propertyName));
        nestedPa.setPropertyValue(tokens, new PropertyValue(propertyName, value));
    }

    @Override
    public void setPropertyValue(PropertyValue pv) throws BeansException {
        PropertyTokenHolder tokens = (PropertyTokenHolder) pv.resolvedTokens;
        if (tokens == null) {
            String propertyName = pv.getName();
            AbstractNestablePropertyAccessor nestedPa;
            try {
                nestedPa = getPropertyAccessorForPropertyPath(propertyName);
            } catch (NotReadablePropertyException ex) {
                throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName, "Nested property in path '" + propertyName + "' does not exist", ex);
            }
            tokens = getPropertyNameTokens(getFinalPath(nestedPa, propertyName));
            if (nestedPa == this) {
                pv.getOriginalPropertyValue().resolvedTokens = tokens;
            }
            nestedPa.setPropertyValue(tokens, pv);
        } else {
            setPropertyValue(tokens, pv);
        }
    }

    protected void setPropertyValue(PropertyTokenHolder tokens, PropertyValue pv) throws BeansException {
        if (tokens.keys != null) {
            processKeyedProperty(tokens, pv);
        } else {
            processLocalProperty(tokens, pv);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void processKeyedProperty(PropertyTokenHolder tokens, PropertyValue pv) {
        Object propValue = getPropertyHoldingValue(tokens);
        PropertyHandler ph = getLocalPropertyHandler(tokens.actualName);
        if (ph == null) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + tokens.actualName, "No property handler found");
        }
        Assert.state(tokens.keys != null, "No token keys");
        String lastKey = tokens.keys[tokens.keys.length - 1];
        if (propValue.getClass().isArray()) {
            Class<?> requiredType = propValue.getClass().getComponentType();
            int arrayIndex = Integer.parseInt(lastKey);
            Object oldValue = null;
            try {
                if (isExtractOldValueForEditor() && arrayIndex < Array.getLength(propValue)) {
                    oldValue = Array.get(propValue, arrayIndex);
                }
                Object convertedValue = convertIfNecessary(tokens.canonicalName, oldValue, pv.getValue(), requiredType, ph.nested(tokens.keys.length));
                int length = Array.getLength(propValue);
                if (arrayIndex >= length && arrayIndex < this.autoGrowCollectionLimit) {
                    Class<?> componentType = propValue.getClass().getComponentType();
                    Object newArray = Array.newInstance(componentType, arrayIndex + 1);
                    System.arraycopy(propValue, 0, newArray, 0, length);
                    int lastKeyIndex = tokens.canonicalName.lastIndexOf('[');
                    String propName = tokens.canonicalName.substring(0, lastKeyIndex);
                    setPropertyValue(propName, newArray);
                    propValue = getPropertyValue(propName);
                }
                Array.set(propValue, arrayIndex, convertedValue);
            } catch (IndexOutOfBoundsException ex) {
                throw new InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName, "Invalid array index in property path '" + tokens.canonicalName + "'", ex);
            }
        } else if (propValue instanceof List list) {
            Class<?> requiredType = ph.getCollectionType(tokens.keys.length);
            int index = Integer.parseInt(lastKey);
            Object oldValue = null;
            if (isExtractOldValueForEditor() && index < list.size()) {
                oldValue = list.get(index);
            }
            Object convertedValue = convertIfNecessary(tokens.canonicalName, oldValue, pv.getValue(), requiredType, ph.nested(tokens.keys.length));
            int size = list.size();
            if (index >= size && index < this.autoGrowCollectionLimit) {
                for (int i = size; i < index; i++) {
                    try {
                        list.add(null);
                    } catch (NullPointerException ex) {
                        throw new InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName, "Cannot set element with index " + index + " in List of size " + size + ", accessed using property path '" + tokens.canonicalName + "': List does not support filling up gaps with null elements");
                    }
                }
                list.add(convertedValue);
            } else {
                try {
                    list.set(index, convertedValue);
                } catch (IndexOutOfBoundsException ex) {
                    throw new InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName, "Invalid list index in property path '" + tokens.canonicalName + "'", ex);
                }
            }
        } else if (propValue instanceof Map map) {
            Class<?> mapKeyType = ph.getMapKeyType(tokens.keys.length);
            Class<?> mapValueType = ph.getMapValueType(tokens.keys.length);
            // 重要：在此处不要传递完整的属性名 - 属性编辑器
            // 必须不对映射键生效，而仅对映射值生效。
            TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(mapKeyType);
            Object convertedMapKey = convertIfNecessary(null, null, lastKey, mapKeyType, typeDescriptor);
            Object oldValue = null;
            if (isExtractOldValueForEditor()) {
                oldValue = map.get(convertedMapKey);
            }
            // 在这里传入完整的属性名和旧值，因为我们希望获取完整的
            // 转换映射值的转换能力。
            Object convertedMapValue = convertIfNecessary(tokens.canonicalName, oldValue, pv.getValue(), mapValueType, ph.nested(tokens.keys.length));
            map.put(convertedMapKey, convertedMapValue);
        } else {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName, "Property referenced in indexed property path '" + tokens.canonicalName + "' is neither an array nor a List nor a Map; returned value was [" + propValue + "]");
        }
    }

    private Object getPropertyHoldingValue(PropertyTokenHolder tokens) {
        // 应用索引和映射键：获取除最后一个键之外的所有键的值。
        Assert.state(tokens.keys != null, "No token keys");
        PropertyTokenHolder getterTokens = new PropertyTokenHolder(tokens.actualName);
        getterTokens.canonicalName = tokens.canonicalName;
        getterTokens.keys = new String[tokens.keys.length - 1];
        System.arraycopy(tokens.keys, 0, getterTokens.keys, 0, tokens.keys.length - 1);
        Object propValue;
        try {
            propValue = getPropertyValue(getterTokens);
        } catch (NotReadablePropertyException ex) {
            throw new NotWritablePropertyException(getRootClass(), this.nestedPath + tokens.canonicalName, "Cannot access indexed value in property referenced " + "in indexed property path '" + tokens.canonicalName + "'", ex);
        }
        if (propValue == null) {
            // null 映射值情况
            if (isAutoGrowNestedPaths()) {
                int lastKeyIndex = tokens.canonicalName.lastIndexOf('[');
                getterTokens.canonicalName = tokens.canonicalName.substring(0, lastKeyIndex);
                propValue = setDefaultValue(getterTokens);
            } else {
                throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + tokens.canonicalName, "Cannot access indexed value in property referenced " + "in indexed property path '" + tokens.canonicalName + "': returned null");
            }
        }
        return propValue;
    }

    private void processLocalProperty(PropertyTokenHolder tokens, PropertyValue pv) {
        PropertyHandler ph = getLocalPropertyHandler(tokens.actualName);
        if (ph == null || !ph.isWritable()) {
            if (pv.isOptional()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring optional value for property '" + tokens.actualName + "' - property not found on bean class [" + getRootClass().getName() + "]");
                }
                return;
            }
            if (this.suppressNotWritablePropertyException) {
                // 针对常见的 ignoreUnknown=true 场景进行的优化，因为该
                // 异常将被上层捕获并吞没...
                return;
            }
            throw createNotWritablePropertyException(tokens.canonicalName);
        }
        Object oldValue = null;
        try {
            Object originalValue = pv.getValue();
            Object valueToApply = originalValue;
            if (!Boolean.FALSE.equals(pv.conversionNecessary)) {
                if (pv.isConverted()) {
                    valueToApply = pv.getConvertedValue();
                } else {
                    if (isExtractOldValueForEditor() && ph.isReadable()) {
                        try {
                            oldValue = ph.getValue();
                        } catch (Exception ex) {
                            if (ex instanceof PrivilegedActionException pae) {
                                ex = pae.getException();
                            }
                            if (logger.isDebugEnabled()) {
                                logger.debug("Could not read previous value of property '" + this.nestedPath + tokens.canonicalName + "'", ex);
                            }
                        }
                    }
                    valueToApply = convertForProperty(tokens.canonicalName, oldValue, originalValue, ph.toTypeDescriptor());
                }
                pv.getOriginalPropertyValue().conversionNecessary = (valueToApply != originalValue);
            }
            ph.setValue(valueToApply);
        } catch (TypeMismatchException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(getRootInstance(), this.nestedPath + tokens.canonicalName, oldValue, pv.getValue());
            if (ex.getTargetException() instanceof ClassCastException) {
                throw new TypeMismatchException(propertyChangeEvent, ph.getPropertyType(), ex.getTargetException());
            } else {
                Throwable cause = ex.getTargetException();
                if (cause instanceof UndeclaredThrowableException) {
                    // 可能会发生，例如与 Groovy 生成的类中的方法相关
                    cause = cause.getCause();
                }
                throw new MethodInvocationException(propertyChangeEvent, cause);
            }
        } catch (Exception ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(getRootInstance(), this.nestedPath + tokens.canonicalName, oldValue, pv.getValue());
            throw new MethodInvocationException(pce, ex);
        }
    }

    @Override
    @Nullable
    public Class<?> getPropertyType(String propertyName) throws BeansException {
        try {
            PropertyHandler ph = getPropertyHandler(propertyName);
            if (ph != null) {
                return ph.getPropertyType();
            } else {
                // 可能是一个索引/映射属性...
                Object value = getPropertyValue(propertyName);
                if (value != null) {
                    return value.getClass();
                }
                // 检查是否存在自定义编辑器，
                // 这可能提供了对期望的目标类型的指示。
                Class<?> editorType = guessPropertyTypeFromEditors(propertyName);
                if (editorType != null) {
                    return editorType;
                }
            }
        } catch (InvalidPropertyException ex) {
            // 视为不可确定。
        }
        return null;
    }

    @Override
    @Nullable
    public TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException {
        try {
            AbstractNestablePropertyAccessor nestedPa = getPropertyAccessorForPropertyPath(propertyName);
            String finalPath = getFinalPath(nestedPa, propertyName);
            PropertyTokenHolder tokens = getPropertyNameTokens(finalPath);
            PropertyHandler ph = nestedPa.getLocalPropertyHandler(tokens.actualName);
            if (ph != null) {
                if (tokens.keys != null) {
                    if (ph.isReadable() || ph.isWritable()) {
                        return ph.nested(tokens.keys.length);
                    }
                } else {
                    if (ph.isReadable() || ph.isWritable()) {
                        return ph.toTypeDescriptor();
                    }
                }
            }
        } catch (InvalidPropertyException ex) {
            // 视为不可确定。
        }
        return null;
    }

    @Override
    public boolean isReadableProperty(String propertyName) {
        try {
            PropertyHandler ph = getPropertyHandler(propertyName);
            if (ph != null) {
                return ph.isReadable();
            } else {
                // 可能是一个索引/映射属性...
                getPropertyValue(propertyName);
                return true;
            }
        } catch (InvalidPropertyException ex) {
            // 无法评估，因此无法阅读。
        }
        return false;
    }

    @Override
    public boolean isWritableProperty(String propertyName) {
        try {
            PropertyHandler ph = getPropertyHandler(propertyName);
            if (ph != null) {
                return ph.isWritable();
            } else {
                // 可能是一个索引/映射属性...
                getPropertyValue(propertyName);
                return true;
            }
        } catch (InvalidPropertyException ex) {
            // 无法评估，因此无法写入。
        }
        return false;
    }

    @Nullable
    private Object convertIfNecessary(@Nullable String propertyName, @Nullable Object oldValue, @Nullable Object newValue, @Nullable Class<?> requiredType, @Nullable TypeDescriptor td) throws TypeMismatchException {
        Assert.state(this.typeConverterDelegate != null, "No TypeConverterDelegate");
        try {
            return this.typeConverterDelegate.convertIfNecessary(propertyName, oldValue, newValue, requiredType, td);
        } catch (ConverterNotFoundException | IllegalStateException ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(getRootInstance(), this.nestedPath + propertyName, oldValue, newValue);
            throw new ConversionNotSupportedException(pce, requiredType, ex);
        } catch (ConversionException | IllegalArgumentException ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(getRootInstance(), this.nestedPath + propertyName, oldValue, newValue);
            throw new TypeMismatchException(pce, requiredType, ex);
        }
    }

    @Nullable
    protected Object convertForProperty(String propertyName, @Nullable Object oldValue, @Nullable Object newValue, TypeDescriptor td) throws TypeMismatchException {
        return convertIfNecessary(propertyName, oldValue, newValue, td.getType(), td);
    }

    @Override
    @Nullable
    public Object getPropertyValue(String propertyName) throws BeansException {
        AbstractNestablePropertyAccessor nestedPa = getPropertyAccessorForPropertyPath(propertyName);
        PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedPa, propertyName));
        return nestedPa.getPropertyValue(tokens);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nullable
    protected Object getPropertyValue(PropertyTokenHolder tokens) throws BeansException {
        String propertyName = tokens.canonicalName;
        String actualName = tokens.actualName;
        PropertyHandler ph = getLocalPropertyHandler(actualName);
        if (ph == null || !ph.isReadable()) {
            throw new NotReadablePropertyException(getRootClass(), this.nestedPath + propertyName);
        }
        try {
            Object value = ph.getValue();
            if (tokens.keys != null) {
                if (value == null) {
                    if (isAutoGrowNestedPaths()) {
                        value = setDefaultValue(new PropertyTokenHolder(tokens.actualName));
                    } else {
                        throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName, "Cannot access indexed value of property referenced in indexed " + "property path '" + propertyName + "': returned null");
                    }
                }
                StringBuilder indexedPropertyName = new StringBuilder(tokens.actualName);
                // 应用索引和映射键
                for (int i = 0; i < tokens.keys.length; i++) {
                    String key = tokens.keys[i];
                    if (value == null) {
                        throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName, "Cannot access indexed value of property referenced in indexed " + "property path '" + propertyName + "': returned null");
                    } else if (value.getClass().isArray()) {
                        int index = Integer.parseInt(key);
                        value = growArrayIfNecessary(value, index, indexedPropertyName.toString());
                        value = Array.get(value, index);
                    } else if (value instanceof List list) {
                        int index = Integer.parseInt(key);
                        growCollectionIfNecessary(list, index, indexedPropertyName.toString(), ph, i + 1);
                        value = list.get(index);
                    } else if (value instanceof Set set) {
                        // 将索引应用于集合中的迭代器。
                        int index = Integer.parseInt(key);
                        if (index < 0 || index >= set.size()) {
                            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Cannot get element with index " + index + " from Set of size " + set.size() + ", accessed using property path '" + propertyName + "'");
                        }
                        Iterator<Object> it = set.iterator();
                        for (int j = 0; it.hasNext(); j++) {
                            Object elem = it.next();
                            if (j == index) {
                                value = elem;
                                break;
                            }
                        }
                    } else if (value instanceof Map map) {
                        Class<?> mapKeyType = ph.getResolvableType().getNested(i + 1).asMap().resolveGeneric(0);
                        // 重要：在此处不要传递完整的属性名称 - 属性编辑器
                        // 必须不应用于映射键，而仅应用于映射值。
                        TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(mapKeyType);
                        Object convertedMapKey = convertIfNecessary(null, null, key, mapKeyType, typeDescriptor);
                        value = map.get(convertedMapKey);
                    } else {
                        throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Property referenced in indexed property path '" + propertyName + "' is neither an array nor a List nor a Set nor a Map; returned value was [" + value + "]");
                    }
                    indexedPropertyName.append(PROPERTY_KEY_PREFIX).append(key).append(PROPERTY_KEY_SUFFIX);
                }
            }
            return value;
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Index of out of bounds in property path '" + propertyName + "'", ex);
        } catch (NumberFormatException | TypeMismatchException ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Invalid index in property path '" + propertyName + "'", ex);
        } catch (InvocationTargetException ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Getter for property '" + actualName + "' threw exception", ex);
        } catch (Exception ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Illegal attempt to get property '" + actualName + "' threw exception", ex);
        }
    }

    /**
     * 返回指定 {@code propertyName} 的 {@link PropertyHandler}，如有必要进行导航。如果没有找到则返回 {@code null} 而不是抛出异常。
     * @param propertyName 要获取描述符的属性
     * @return 指定属性的属性描述符，
     * 或者在找不到时返回 {@code null}
     * @throws BeansException 如果出现反射失败
     */
    @Nullable
    protected PropertyHandler getPropertyHandler(String propertyName) throws BeansException {
        Assert.notNull(propertyName, "Property name must not be null");
        AbstractNestablePropertyAccessor nestedPa = getPropertyAccessorForPropertyPath(propertyName);
        return nestedPa.getLocalPropertyHandler(getFinalPath(nestedPa, propertyName));
    }

    /**
     * 返回一个用于指定本地属性名称的 {@link PropertyHandler}。
     * 仅用于访问当前上下文中可用的属性。
     * @param propertyName 本地属性的名称
     * @return 那个属性的处理器，如果没有找到则返回 {@code null}
     */
    @Nullable
    protected abstract PropertyHandler getLocalPropertyHandler(String propertyName);

    /**
     * 创建一个新的嵌套属性访问器实例。
     * 可以在子类中重写以创建 PropertyAccessor 子类。
     * @param object 由这个 PropertyAccessor 包装的对象
     * @param nestedPath 对象的嵌套路径
     * @return 嵌套的 PropertyAccessor 实例
     */
    protected abstract AbstractNestablePropertyAccessor newNestedPropertyAccessor(Object object, String nestedPath);

    /**
     * 为指定的属性创建一个 {@link NotWritablePropertyException}。
     */
    protected abstract NotWritablePropertyException createNotWritablePropertyException(String propertyName);

    private Object growArrayIfNecessary(Object array, int index, String name) {
        if (!isAutoGrowNestedPaths()) {
            return array;
        }
        int length = Array.getLength(array);
        if (index >= length && index < this.autoGrowCollectionLimit) {
            Class<?> componentType = array.getClass().getComponentType();
            Object newArray = Array.newInstance(componentType, index + 1);
            System.arraycopy(array, 0, newArray, 0, length);
            for (int i = length; i < Array.getLength(newArray); i++) {
                Array.set(newArray, i, newValue(componentType, null, name));
            }
            setPropertyValue(name, newArray);
            Object defaultValue = getPropertyValue(name);
            Assert.state(defaultValue != null, "Default value must not be null");
            return defaultValue;
        } else {
            return array;
        }
    }

    private void growCollectionIfNecessary(Collection<Object> collection, int index, String name, PropertyHandler ph, int nestingLevel) {
        if (!isAutoGrowNestedPaths()) {
            return;
        }
        int size = collection.size();
        if (index >= size && index < this.autoGrowCollectionLimit) {
            Class<?> elementType = ph.getResolvableType().getNested(nestingLevel).asCollection().resolveGeneric();
            if (elementType != null) {
                for (int i = collection.size(); i < index + 1; i++) {
                    collection.add(newValue(elementType, null, name));
                }
            }
        }
    }

    /**
     * 获取路径的最后组成部分。即使路径未嵌套也有效。
     * @param pa 要操作的属性访问器
     * @param nestedPath 已知的嵌套属性路径
     * @return 路径的最后组成部分（目标bean上的属性）
     */
    protected String getFinalPath(AbstractNestablePropertyAccessor pa, String nestedPath) {
        if (pa == this) {
            return nestedPath;
        }
        return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath) + 1);
    }

    /**
     * 递归导航以返回针对嵌套属性路径的属性访问器。
     * @param propertyPath 属性路径，可能为嵌套路径
     * @return 目标Bean的属性访问器
     */
    protected AbstractNestablePropertyAccessor getPropertyAccessorForPropertyPath(String propertyPath) {
        int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);
        // 递归处理嵌套属性。
        if (pos > -1) {
            String nestedProperty = propertyPath.substring(0, pos);
            String nestedPath = propertyPath.substring(pos + 1);
            AbstractNestablePropertyAccessor nestedPa = getNestedPropertyAccessor(nestedProperty);
            return nestedPa.getPropertyAccessorForPropertyPath(nestedPath);
        } else {
            return this;
        }
    }

    /**
     * 获取给定嵌套属性的属性访问器。
     * 如果在缓存中未找到，则创建一个新的。
     * <p>注意：现在缓存嵌套PropertyAccessors是必要的，
     * 以保持已注册的嵌套属性的定制编辑器。
     * @param nestedProperty 要为其创建PropertyAccessor的属性
     * @return 属性访问器实例，可能是缓存的或新创建的
     */
    private AbstractNestablePropertyAccessor getNestedPropertyAccessor(String nestedProperty) {
        if (this.nestedPropertyAccessors == null) {
            this.nestedPropertyAccessors = new HashMap<>();
        }
        // 获取 Bean 属性的值。
        PropertyTokenHolder tokens = getPropertyNameTokens(nestedProperty);
        String canonicalName = tokens.canonicalName;
        Object value = getPropertyValue(tokens);
        if (value == null || (value instanceof Optional<?> optional && optional.isEmpty())) {
            if (isAutoGrowNestedPaths()) {
                value = setDefaultValue(tokens);
            } else {
                throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + canonicalName);
            }
        }
        // 查找缓存的子属性访问器，如果未找到则创建一个新的。
        AbstractNestablePropertyAccessor nestedPa = this.nestedPropertyAccessors.get(canonicalName);
        if (nestedPa == null || nestedPa.getWrappedInstance() != ObjectUtils.unwrapOptional(value)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Creating new nested " + getClass().getSimpleName() + " for property '" + canonicalName + "'");
            }
            nestedPa = newNestedPropertyAccessor(value, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR);
            // 继承所有特定类型的属性编辑器。
            copyDefaultEditorsTo(nestedPa);
            copyCustomEditorsTo(nestedPa, canonicalName);
            this.nestedPropertyAccessors.put(canonicalName, nestedPa);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Using cached nested property accessor for property '" + canonicalName + "'");
            }
        }
        return nestedPa;
    }

    private Object setDefaultValue(PropertyTokenHolder tokens) {
        PropertyValue pv = createDefaultPropertyValue(tokens);
        setPropertyValue(tokens, pv);
        Object defaultValue = getPropertyValue(tokens);
        Assert.state(defaultValue != null, "Default value must not be null");
        return defaultValue;
    }

    private PropertyValue createDefaultPropertyValue(PropertyTokenHolder tokens) {
        TypeDescriptor desc = getPropertyTypeDescriptor(tokens.canonicalName);
        if (desc == null) {
            throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + tokens.canonicalName, "Could not determine property type for auto-growing a default value");
        }
        Object defaultValue = newValue(desc.getType(), desc, tokens.canonicalName);
        return new PropertyValue(tokens.canonicalName, defaultValue);
    }

    private Object newValue(Class<?> type, @Nullable TypeDescriptor desc, String name) {
        try {
            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                // 待办事项 - 只处理二维数组
                if (componentType.isArray()) {
                    Object array = Array.newInstance(componentType, 1);
                    Array.set(array, 0, Array.newInstance(componentType.getComponentType(), 0));
                    return array;
                } else {
                    return Array.newInstance(componentType, 0);
                }
            } else if (Collection.class.isAssignableFrom(type)) {
                TypeDescriptor elementDesc = (desc != null ? desc.getElementTypeDescriptor() : null);
                return CollectionFactory.createCollection(type, (elementDesc != null ? elementDesc.getType() : null), 16);
            } else if (Map.class.isAssignableFrom(type)) {
                TypeDescriptor keyDesc = (desc != null ? desc.getMapKeyTypeDescriptor() : null);
                return CollectionFactory.createMap(type, (keyDesc != null ? keyDesc.getType() : null), 16);
            } else {
                Constructor<?> ctor = type.getDeclaredConstructor();
                if (Modifier.isPrivate(ctor.getModifiers())) {
                    throw new IllegalAccessException("Auto-growing not allowed with private constructor: " + ctor);
                }
                return BeanUtils.instantiateClass(ctor);
            }
        } catch (Throwable ex) {
            throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + name, "Could not instantiate property type [" + type.getName() + "] to auto-grow nested property path", ex);
        }
    }

    /**
     * 解析给定的属性名称，转换为对应的属性名称标记。
     * @param propertyName 要解析的属性名称
     * @return 解析后的属性标记表示
     */
    private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
        String actualName = null;
        List<String> keys = new ArrayList<>(2);
        int searchIndex = 0;
        while (searchIndex != -1) {
            int keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex);
            searchIndex = -1;
            if (keyStart != -1) {
                int keyEnd = getPropertyNameKeyEnd(propertyName, keyStart + PROPERTY_KEY_PREFIX.length());
                if (keyEnd != -1) {
                    if (actualName == null) {
                        actualName = propertyName.substring(0, keyStart);
                    }
                    String key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length(), keyEnd);
                    if (key.length() > 1 && (key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) {
                        key = key.substring(1, key.length() - 1);
                    }
                    keys.add(key);
                    searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length();
                }
            }
        }
        PropertyTokenHolder tokens = new PropertyTokenHolder(actualName != null ? actualName : propertyName);
        if (!keys.isEmpty()) {
            tokens.canonicalName += PROPERTY_KEY_PREFIX + StringUtils.collectionToDelimitedString(keys, PROPERTY_KEY_SUFFIX + PROPERTY_KEY_PREFIX) + PROPERTY_KEY_SUFFIX;
            tokens.keys = StringUtils.toStringArray(keys);
        }
        return tokens;
    }

    private int getPropertyNameKeyEnd(String propertyName, int startIndex) {
        int unclosedPrefixes = 0;
        int length = propertyName.length();
        for (int i = startIndex; i < length; i++) {
            switch(propertyName.charAt(i)) {
                case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR:
                    // 属性名称包含开头的修饰符...
                    unclosedPrefixes++;
                    break;
                case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR:
                    if (unclosedPrefixes == 0) {
                        // 属性名（左侧）中没有未关闭的前缀（们）->
                        // 这是我们正在寻找的后缀。
                        return i;
                    } else {
                        // 这个后缀并没有关闭初始前缀，而是
                        // 仅指发生在属性名称中的一个。
                        unclosedPrefixes--;
                    }
                    break;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        String className = getClass().getName();
        if (this.wrappedObject == null) {
            return className + ": no wrapped object set";
        }
        return className + ": wrapping object [" + ObjectUtils.identityToString(this.wrappedObject) + ']';
    }

    /**
     * 特定属性的处理器。
     */
    protected abstract static class PropertyHandler {

        @Nullable
        private final Class<?> propertyType;

        private final boolean readable;

        private final boolean writable;

        public PropertyHandler(@Nullable Class<?> propertyType, boolean readable, boolean writable) {
            this.propertyType = propertyType;
            this.readable = readable;
            this.writable = writable;
        }

        @Nullable
        public Class<?> getPropertyType() {
            return this.propertyType;
        }

        public boolean isReadable() {
            return this.readable;
        }

        public boolean isWritable() {
            return this.writable;
        }

        public abstract TypeDescriptor toTypeDescriptor();

        public abstract ResolvableType getResolvableType();

        @Nullable
        public Class<?> getMapKeyType(int nestingLevel) {
            return getResolvableType().getNested(nestingLevel).asMap().resolveGeneric(0);
        }

        @Nullable
        public Class<?> getMapValueType(int nestingLevel) {
            return getResolvableType().getNested(nestingLevel).asMap().resolveGeneric(1);
        }

        @Nullable
        public Class<?> getCollectionType(int nestingLevel) {
            return getResolvableType().getNested(nestingLevel).asCollection().resolveGeneric();
        }

        @Nullable
        public abstract TypeDescriptor nested(int level);

        @Nullable
        public abstract Object getValue() throws Exception;

        public abstract void setValue(@Nullable Object value) throws Exception;
    }

    /**
     * 用来存储属性令牌的 Holder 类。
     */
    protected static class PropertyTokenHolder {

        public PropertyTokenHolder(String name) {
            this.actualName = name;
            this.canonicalName = name;
        }

        public String actualName;

        public String canonicalName;

        @Nullable
        public String[] keys;
    }
}
