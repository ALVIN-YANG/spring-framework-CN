// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import java.beans.PropertyEditor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.NumberUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 用于将属性值转换为目标类型的内部辅助类。
 *
 * <p>作用于给定的 {@link PropertyEditorRegistrySupport} 实例。
 * 由 {@link BeanWrapperImpl} 和 {@link SimpleTypeConverter} 作为代理使用。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @since 2.0
 * @see BeanWrapperImpl
 * @see SimpleTypeConverter
 */
class TypeConverterDelegate {

    private static final Log logger = LogFactory.getLog(TypeConverterDelegate.class);

    private final PropertyEditorRegistrySupport propertyEditorRegistry;

    @Nullable
    private final Object targetObject;

    /**
     * 为给定的编辑器注册表创建一个新的 TypeConverterDelegate。
     * @param propertyEditorRegistry 要使用的编辑器注册表
     */
    public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry) {
        this(propertyEditorRegistry, null);
    }

    /**
     * 为给定的编辑器注册表和 Bean 实例创建一个新的 TypeConverterDelegate。
     * @param propertyEditorRegistry 要使用的编辑器注册表
     * @param targetObject 要操作的目标对象（作为可以传递给编辑器的上下文）
     */
    public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry, @Nullable Object targetObject) {
        this.propertyEditorRegistry = propertyEditorRegistry;
        this.targetObject = targetObject;
    }

    /**
     * 将值转换为指定属性的所需类型。
     * @param propertyName 属性名称
     * @param oldValue 可用时的旧值（可能为 {@code null}）
     * @param newValue 建议的新值
     * @param requiredType 我们必须转换到的类型
     * （或为 {@code null}，如果未知，例如在集合元素的情况下）
     * @return 新值，可能是类型转换的结果
     * @throws IllegalArgumentException 如果类型转换失败
     */
    @Nullable
    public <T> T convertIfNecessary(@Nullable String propertyName, @Nullable Object oldValue, Object newValue, @Nullable Class<T> requiredType) throws IllegalArgumentException {
        return convertIfNecessary(propertyName, oldValue, newValue, requiredType, TypeDescriptor.valueOf(requiredType));
    }

    /**
     * 将值转换为所需的类型（如果需要的话，从字符串转换而来），用于指定的属性。
     * @param propertyName 属性名称
     * @param oldValue 可用时的旧值（可能为 {@code null}）
     * @param newValue 提议的新值
     * @param requiredType 我们必须转换到的类型（或如果不知道，则为 {@code null}，例如在集合元素的情况下）
     * @param typeDescriptor 目标属性或字段的描述符
     * @return 新值，可能是类型转换的结果
     * @throws IllegalArgumentException 如果类型转换失败
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T convertIfNecessary(@Nullable String propertyName, @Nullable Object oldValue, @Nullable Object newValue, @Nullable Class<T> requiredType, @Nullable TypeDescriptor typeDescriptor) throws IllegalArgumentException {
        // 是否为该类型自定义了编辑器？
        PropertyEditor editor = this.propertyEditorRegistry.findCustomEditor(requiredType, propertyName);
        ConversionFailedException conversionAttemptEx = null;
        // 没有指定自定义编辑器，但指定了自定义的 ConversionService？
        ConversionService conversionService = this.propertyEditorRegistry.getConversionService();
        if (editor == null && conversionService != null && newValue != null && typeDescriptor != null) {
            TypeDescriptor sourceTypeDesc = TypeDescriptor.forObject(newValue);
            if (conversionService.canConvert(sourceTypeDesc, typeDescriptor)) {
                try {
                    return (T) conversionService.convert(newValue, sourceTypeDesc, typeDescriptor);
                } catch (ConversionFailedException ex) {
                    // 回退到以下默认转换逻辑
                    conversionAttemptEx = ex;
                }
            }
        }
        Object convertedValue = newValue;
        // 值不是所需类型？
        if (editor != null || (requiredType != null && !ClassUtils.isAssignableValue(requiredType, convertedValue))) {
            if (typeDescriptor != null && requiredType != null && Collection.class.isAssignableFrom(requiredType) && convertedValue instanceof String text) {
                TypeDescriptor elementTypeDesc = typeDescriptor.getElementTypeDescriptor();
                if (elementTypeDesc != null) {
                    Class<?> elementType = elementTypeDesc.getType();
                    if (Class.class == elementType || Enum.class.isAssignableFrom(elementType)) {
                        convertedValue = StringUtils.commaDelimitedListToStringArray(text);
                    }
                }
            }
            if (editor == null) {
                editor = findDefaultEditor(requiredType);
            }
            convertedValue = doConvertValue(oldValue, convertedValue, requiredType, editor);
        }
        boolean standardConversion = false;
        if (requiredType != null) {
            // 尝试根据适当的情况应用一些标准的类型转换规则。
            if (convertedValue != null) {
                if (Object.class == requiredType) {
                    return (T) convertedValue;
                } else if (requiredType.isArray()) {
                    // 数组必须 -> 应用适当的元素转换。
                    if (convertedValue instanceof String text && Enum.class.isAssignableFrom(requiredType.getComponentType())) {
                        convertedValue = StringUtils.commaDelimitedListToStringArray(text);
                    }
                    return (T) convertToTypedArray(convertedValue, propertyName, requiredType.getComponentType());
                } else if (convertedValue instanceof Collection<?> coll) {
                    // 将元素转换为目标类型，如果确定的话。
                    convertedValue = convertToTypedCollection(coll, propertyName, requiredType, typeDescriptor);
                    standardConversion = true;
                } else if (convertedValue instanceof Map<?, ?> map) {
                    // 将键和值转换为相应的目标类型，如果确定的话。
                    convertedValue = convertToTypedMap(map, propertyName, requiredType, typeDescriptor);
                    standardConversion = true;
                }
                if (convertedValue.getClass().isArray() && Array.getLength(convertedValue) == 1) {
                    convertedValue = Array.get(convertedValue, 0);
                    standardConversion = true;
                }
                if (String.class == requiredType && ClassUtils.isPrimitiveOrWrapper(convertedValue.getClass())) {
                    // 我们可以将任何原始值转换为字符串表示形式。
                    return (T) convertedValue.toString();
                } else if (convertedValue instanceof String text && !requiredType.isInstance(convertedValue)) {
                    if (conversionAttemptEx == null && !requiredType.isInterface() && !requiredType.isEnum()) {
                        try {
                            Constructor<T> strCtor = requiredType.getConstructor(String.class);
                            return BeanUtils.instantiateClass(strCtor, convertedValue);
                        } catch (NoSuchMethodException ex) {
                            // 进行字段查找
                            if (logger.isTraceEnabled()) {
                                logger.trace("No String constructor found on type [" + requiredType.getName() + "]", ex);
                            }
                        } catch (Exception ex) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Construction via String failed for type [" + requiredType.getName() + "]", ex);
                            }
                        }
                    }
                    String trimmedValue = text.trim();
                    if (requiredType.isEnum() && trimmedValue.isEmpty()) {
                        // 这是一个空的枚举标识符：将枚举值重置为null。
                        return null;
                    }
                    convertedValue = attemptToConvertStringToEnum(requiredType, trimmedValue, convertedValue);
                    standardConversion = true;
                } else if (convertedValue instanceof Number num && Number.class.isAssignableFrom(requiredType)) {
                    convertedValue = NumberUtils.convertNumberToTargetClass(num, (Class<Number>) requiredType);
                    standardConversion = true;
                }
            } else {
                // 转换后的值等于null
                if (requiredType == Optional.class) {
                    convertedValue = Optional.empty();
                }
            }
            if (!ClassUtils.isAssignableValue(requiredType, convertedValue)) {
                if (conversionAttemptEx != null) {
                    // 原始异常来自上面的 former ConversionService 调用...
                    throw conversionAttemptEx;
                } else if (conversionService != null && typeDescriptor != null) {
                    // 转换服务之前未尝试过，可能找到了自定义编辑器
                    // 但是编辑器无法生成所需的类型...
                    TypeDescriptor sourceTypeDesc = TypeDescriptor.forObject(newValue);
                    if (conversionService.canConvert(sourceTypeDesc, typeDescriptor)) {
                        return (T) conversionService.convert(newValue, sourceTypeDesc, typeDescriptor);
                    }
                }
                // 当然不匹配：抛出 IllegalArgumentException/IllegalStateException 异常
                StringBuilder msg = new StringBuilder();
                msg.append("Cannot convert value of type '").append(ClassUtils.getDescriptiveType(newValue));
                msg.append("' to required type '").append(ClassUtils.getQualifiedName(requiredType)).append('\'');
                if (propertyName != null) {
                    msg.append(" for property '").append(propertyName).append('\'');
                }
                if (editor != null) {
                    msg.append(": PropertyEditor [").append(editor.getClass().getName()).append("] returned inappropriate value of type '").append(ClassUtils.getDescriptiveType(convertedValue)).append('\'');
                    throw new IllegalArgumentException(msg.toString());
                } else {
                    msg.append(": no matching editors or conversion strategy found");
                    throw new IllegalStateException(msg.toString());
                }
            }
        }
        if (conversionAttemptEx != null) {
            if (editor == null && !standardConversion && requiredType != null && Object.class != requiredType) {
                throw conversionAttemptEx;
            }
            logger.debug("Original ConversionService attempt failed - ignored since " + "PropertyEditor based conversion eventually succeeded", conversionAttemptEx);
        }
        return (T) convertedValue;
    }

    private Object attemptToConvertStringToEnum(Class<?> requiredType, String trimmedValue, Object currentConvertedValue) {
        Object convertedValue = currentConvertedValue;
        if (Enum.class == requiredType && this.targetObject != null) {
            // 目标类型被声明为原始枚举，将裁剪后的值视为 <枚举的全限定名>.FIELD_NAME
            int index = trimmedValue.lastIndexOf('.');
            if (index > -1) {
                String enumType = trimmedValue.substring(0, index);
                String fieldName = trimmedValue.substring(index + 1);
                ClassLoader cl = this.targetObject.getClass().getClassLoader();
                try {
                    Class<?> enumValueType = ClassUtils.forName(enumType, cl);
                    Field enumField = enumValueType.getField(fieldName);
                    convertedValue = enumField.get(null);
                } catch (ClassNotFoundException ex) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Enum class [" + enumType + "] cannot be loaded", ex);
                    }
                } catch (Throwable ex) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Field [" + fieldName + "] isn't an enum value for type [" + enumType + "]", ex);
                    }
                }
            }
        }
        if (convertedValue == currentConvertedValue) {
            // 尝试使用字段查找作为后备：用于 JDK 1.5 枚举或自定义枚举
            // 值定义为静态字段。结果值仍然需要
            // 需要检查，因此我们不会立即返回它。
            try {
                Field enumField = requiredType.getField(trimmedValue);
                ReflectionUtils.makeAccessible(enumField);
                convertedValue = enumField.get(null);
            } catch (Throwable ex) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Field [" + convertedValue + "] isn't an enum value", ex);
                }
            }
        }
        return convertedValue;
    }

    /**
     * 为给定的类型查找默认编辑器。
     * @param requiredType 要查找编辑器的类型
     * @return 相应的编辑器，如果没有则返回 {@code null}
     */
    @Nullable
    private PropertyEditor findDefaultEditor(@Nullable Class<?> requiredType) {
        PropertyEditor editor = null;
        if (requiredType != null) {
            // 没有自定义编辑器 -> 检查 BeanWrapperImpl 的默认编辑器。
            editor = this.propertyEditorRegistry.getDefaultEditor(requiredType);
            if (editor == null && String.class != requiredType) {
                // 没有默认的BeanWrapper编辑器 -> 检查标准的JavaBean编辑器。
                editor = BeanUtils.findEditorByConvention(requiredType);
            }
        }
        return editor;
    }

    /**
     * 将值转换为所需的类型（如果需要从字符串转换），使用给定的属性编辑器。
     * @param oldValue 可用的先前值（可能为{@code null}）
     * @param newValue 建议的新值
     * @param requiredType 我们必须转换到的类型
     * （或为{@code null}如果未知，例如在集合元素的情况下）
     * @param editor 要使用的属性编辑器
     * @return 新值，可能是类型转换的结果
     * @throws IllegalArgumentException 如果类型转换失败
     */
    @Nullable
    private Object doConvertValue(@Nullable Object oldValue, @Nullable Object newValue, @Nullable Class<?> requiredType, @Nullable PropertyEditor editor) {
        Object convertedValue = newValue;
        if (editor != null && !(convertedValue instanceof String)) {
            // 不是字符串 -> 使用 PropertyEditor 的 setValue 方法。
            // 使用标准属性编辑器，这将返回完全相同的对象；
            // 我们只想允许特殊的属性编辑器覆盖setValue方法
            // 用于非字符串值到所需类型的类型转换。
            try {
                editor.setValue(convertedValue);
                Object newConvertedValue = editor.getValue();
                if (newConvertedValue != convertedValue) {
                    convertedValue = newConvertedValue;
                    // 重置属性编辑器：它已经进行了适当的转换。
                    // 不要再次用于setAsText调用。
                    editor = null;
                }
            } catch (Exception ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("PropertyEditor [" + editor.getClass().getName() + "] does not support setValue call", ex);
                }
                // 吞咽并继续。
            }
        }
        Object returnValue = convertedValue;
        if (requiredType != null && !requiredType.isArray() && convertedValue instanceof String[] array) {
            // 将字符串数组转换为以逗号分隔的字符串。
            // 仅当之前没有 PropertyEditor 将字符串数组转换过来时适用。
            // 如果有的话，CSV 字符串将被传递到 PropertyEditor 的 setAsText 方法中。
            if (logger.isTraceEnabled()) {
                logger.trace("Converting String array to comma-delimited String [" + convertedValue + "]");
            }
            convertedValue = StringUtils.arrayToCommaDelimitedString(array);
        }
        if (convertedValue instanceof String newTextValue) {
            if (editor != null) {
                // 在字符串值的情况下，使用 PropertyEditor 的 setAsText 方法。
                if (logger.isTraceEnabled()) {
                    logger.trace("Converting String to [" + requiredType + "] using property editor [" + editor + "]");
                }
                return doConvertTextValue(oldValue, newTextValue, editor);
            } else if (String.class == requiredType) {
                returnValue = convertedValue;
            }
        }
        return returnValue;
    }

    /**
     * 使用给定的属性编辑器转换给定的文本值。
     * @param oldValue 可用的前一个值（可能为 {@code null}）
     * @param newTextValue 提议的文本值
     * @param editor 要使用的属性编辑器
     * @return 转换后的值
     */
    private Object doConvertTextValue(@Nullable Object oldValue, String newTextValue, PropertyEditor editor) {
        try {
            editor.setValue(oldValue);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("PropertyEditor [" + editor.getClass().getName() + "] does not support setValue call", ex);
            }
            // 吞咽并继续。
        }
        editor.setAsText(newTextValue);
        return editor.getValue();
    }

    private Object convertToTypedArray(Object input, @Nullable String propertyName, Class<?> componentType) {
        if (input instanceof Collection<?> coll) {
            // 将集合元素转换为数组元素。
            Object result = Array.newInstance(componentType, coll.size());
            int i = 0;
            for (Iterator<?> it = coll.iterator(); it.hasNext(); i++) {
                Object value = convertIfNecessary(buildIndexedPropertyName(propertyName, i), null, it.next(), componentType);
                Array.set(result, i, value);
            }
            return result;
        } else if (input.getClass().isArray()) {
            // 如果需要，转换数组元素。
            if (componentType.equals(input.getClass().getComponentType()) && !this.propertyEditorRegistry.hasCustomEditorForElement(componentType, propertyName)) {
                return input;
            }
            int arrayLength = Array.getLength(input);
            Object result = Array.newInstance(componentType, arrayLength);
            for (int i = 0; i < arrayLength; i++) {
                Object value = convertIfNecessary(buildIndexedPropertyName(propertyName, i), null, Array.get(input, i), componentType);
                Array.set(result, i, value);
            }
            return result;
        } else {
            // 一个普通值：将其转换为只有一个元素的数组。
            Object result = Array.newInstance(componentType, 1);
            Object value = convertIfNecessary(buildIndexedPropertyName(propertyName, 0), null, input, componentType);
            Array.set(result, 0, value);
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<?> convertToTypedCollection(Collection<?> original, @Nullable String propertyName, Class<?> requiredType, @Nullable TypeDescriptor typeDescriptor) {
        if (!Collection.class.isAssignableFrom(requiredType)) {
            return original;
        }
        boolean approximable = CollectionFactory.isApproximableCollectionType(requiredType);
        if (!approximable && !canCreateCopy(requiredType)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Custom Collection type [" + original.getClass().getName() + "] does not allow for creating a copy - injecting original Collection as-is");
            }
            return original;
        }
        boolean originalAllowed = requiredType.isInstance(original);
        TypeDescriptor elementType = (typeDescriptor != null ? typeDescriptor.getElementTypeDescriptor() : null);
        if (elementType == null && originalAllowed && !this.propertyEditorRegistry.hasCustomEditorForElement(null, propertyName)) {
            return original;
        }
        Iterator<?> it;
        try {
            it = original.iterator();
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot access Collection of type [" + original.getClass().getName() + "] - injecting original Collection as-is: " + ex);
            }
            return original;
        }
        Collection<Object> convertedCopy;
        try {
            if (approximable) {
                convertedCopy = CollectionFactory.createApproximateCollection(original, original.size());
            } else {
                convertedCopy = (Collection<Object>) ReflectionUtils.accessibleConstructor(requiredType).newInstance();
            }
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot create copy of Collection type [" + original.getClass().getName() + "] - injecting original Collection as-is: " + ex);
            }
            return original;
        }
        for (int i = 0; it.hasNext(); i++) {
            Object element = it.next();
            String indexedPropertyName = buildIndexedPropertyName(propertyName, i);
            Object convertedElement = convertIfNecessary(indexedPropertyName, null, element, (elementType != null ? elementType.getType() : null), elementType);
            try {
                convertedCopy.add(convertedElement);
            } catch (Throwable ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Collection type [" + original.getClass().getName() + "] seems to be read-only - injecting original Collection as-is: " + ex);
                }
                return original;
            }
            originalAllowed = originalAllowed && (element == convertedElement);
        }
        return (originalAllowed ? original : convertedCopy);
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> convertToTypedMap(Map<?, ?> original, @Nullable String propertyName, Class<?> requiredType, @Nullable TypeDescriptor typeDescriptor) {
        if (!Map.class.isAssignableFrom(requiredType)) {
            return original;
        }
        boolean approximable = CollectionFactory.isApproximableMapType(requiredType);
        if (!approximable && !canCreateCopy(requiredType)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Custom Map type [" + original.getClass().getName() + "] does not allow for creating a copy - injecting original Map as-is");
            }
            return original;
        }
        boolean originalAllowed = requiredType.isInstance(original);
        TypeDescriptor keyType = (typeDescriptor != null ? typeDescriptor.getMapKeyTypeDescriptor() : null);
        TypeDescriptor valueType = (typeDescriptor != null ? typeDescriptor.getMapValueTypeDescriptor() : null);
        if (keyType == null && valueType == null && originalAllowed && !this.propertyEditorRegistry.hasCustomEditorForElement(null, propertyName)) {
            return original;
        }
        Iterator<?> it;
        try {
            it = original.entrySet().iterator();
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot access Map of type [" + original.getClass().getName() + "] - injecting original Map as-is: " + ex);
            }
            return original;
        }
        Map<Object, Object> convertedCopy;
        try {
            if (approximable) {
                convertedCopy = CollectionFactory.createApproximateMap(original, original.size());
            } else {
                convertedCopy = (Map<Object, Object>) ReflectionUtils.accessibleConstructor(requiredType).newInstance();
            }
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot create copy of Map type [" + original.getClass().getName() + "] - injecting original Map as-is: " + ex);
            }
            return original;
        }
        while (it.hasNext()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            String keyedPropertyName = buildKeyedPropertyName(propertyName, key);
            Object convertedKey = convertIfNecessary(keyedPropertyName, null, key, (keyType != null ? keyType.getType() : null), keyType);
            Object convertedValue = convertIfNecessary(keyedPropertyName, null, value, (valueType != null ? valueType.getType() : null), valueType);
            try {
                convertedCopy.put(convertedKey, convertedValue);
            } catch (Throwable ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Map type [" + original.getClass().getName() + "] seems to be read-only - injecting original Map as-is: " + ex);
                }
                return original;
            }
            originalAllowed = originalAllowed && (key == convertedKey) && (value == convertedValue);
        }
        return (originalAllowed ? original : convertedCopy);
    }

    @Nullable
    private String buildIndexedPropertyName(@Nullable String propertyName, int index) {
        return (propertyName != null ? propertyName + PropertyAccessor.PROPERTY_KEY_PREFIX + index + PropertyAccessor.PROPERTY_KEY_SUFFIX : null);
    }

    @Nullable
    private String buildKeyedPropertyName(@Nullable String propertyName, Object key) {
        return (propertyName != null ? propertyName + PropertyAccessor.PROPERTY_KEY_PREFIX + key + PropertyAccessor.PROPERTY_KEY_SUFFIX : null);
    }

    private boolean canCreateCopy(Class<?> requiredType) {
        return (!requiredType.isInterface() && !Modifier.isAbstract(requiredType.getModifiers()) && Modifier.isPublic(requiredType.getModifiers()) && ClassUtils.hasConstructor(requiredType));
    }
}
