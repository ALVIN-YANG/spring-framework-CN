// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Spring内部{@link PropertyDescriptor}实现的常用代理方法。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 */
abstract class PropertyDescriptorUtils {

    public static final PropertyDescriptor[] EMPTY_PROPERTY_DESCRIPTOR_ARRAY = {};

    /**
     * 简单的反射算法，用于基本的 set/get/is 访问器方法，为它们构建相应的 JavaBeans 属性描述符。
     * <p>这仅支持基本的 JavaBeans 规范，不包含索引属性或任何自定义器，也不包含其他 BeanInfo 元数据。
     * 对于标准的 JavaBeans 反射，请使用 JavaBeans Introspector。
     * @param beanClass 要进行反射的目标类
     * @return 属性描述符的集合
     * @throws IntrospectionException 从反射给定的 Bean 类中抛出
     * @since 5.3.24
     * @see SimpleBeanInfoFactory
     * @see java.beans.Introspector#getBeanInfo(Class)
     */
    public static Collection<? extends PropertyDescriptor> determineBasicProperties(Class<?> beanClass) throws IntrospectionException {
        Map<String, BasicPropertyDescriptor> pdMap = new TreeMap<>();
        for (Method method : beanClass.getMethods()) {
            String methodName = method.getName();
            boolean setter;
            int nameIndex;
            if (methodName.startsWith("set") && method.getParameterCount() == 1) {
                setter = true;
                nameIndex = 3;
            } else if (methodName.startsWith("get") && method.getParameterCount() == 0 && method.getReturnType() != Void.TYPE) {
                setter = false;
                nameIndex = 3;
            } else if (methodName.startsWith("is") && method.getParameterCount() == 0 && method.getReturnType() == boolean.class) {
                setter = false;
                nameIndex = 2;
            } else {
                continue;
            }
            String propertyName = StringUtils.uncapitalizeAsProperty(methodName.substring(nameIndex));
            if (propertyName.isEmpty()) {
                continue;
            }
            BasicPropertyDescriptor pd = pdMap.get(propertyName);
            if (pd != null) {
                if (setter) {
                    Method writeMethod = pd.getWriteMethod();
                    if (writeMethod == null || writeMethod.getParameterTypes()[0].isAssignableFrom(method.getParameterTypes()[0])) {
                        pd.setWriteMethod(method);
                    } else {
                        pd.addWriteMethod(method);
                    }
                } else {
                    Method readMethod = pd.getReadMethod();
                    if (readMethod == null || (readMethod.getReturnType() == method.getReturnType() && method.getName().startsWith("is"))) {
                        pd.setReadMethod(method);
                    }
                }
            } else {
                pd = new BasicPropertyDescriptor(propertyName, (!setter ? method : null), (setter ? method : null));
                pdMap.put(propertyName, pd);
            }
        }
        return pdMap.values();
    }

    /**
     * 请参阅 {@link java.beans.FeatureDescriptor}。
     */
    public static void copyNonMethodProperties(PropertyDescriptor source, PropertyDescriptor target) {
        target.setExpert(source.isExpert());
        target.setHidden(source.isHidden());
        target.setPreferred(source.isPreferred());
        target.setName(source.getName());
        target.setShortDescription(source.getShortDescription());
        target.setDisplayName(source.getDisplayName());
        // 复制所有属性（模拟 private FeatureDescriptor#addTable 的行为）
        Enumeration<String> keys = source.attributeNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            target.setValue(key, source.getValue(key));
        }
        // 请提供需要翻译的 Java 代码注释内容，我才能为您进行准确的翻译。
        target.setPropertyEditorClass(source.getPropertyEditorClass());
        target.setBound(source.isBound());
        target.setConstrained(source.isConstrained());
    }

    /**
     * 请参阅 {@link java.beans.PropertyDescriptor#findPropertyType}。
     */
    @Nullable
    public static Class<?> findPropertyType(@Nullable Method readMethod, @Nullable Method writeMethod) throws IntrospectionException {
        Class<?> propertyType = null;
        if (readMethod != null) {
            if (readMethod.getParameterCount() != 0) {
                throw new IntrospectionException("Bad read method arg count: " + readMethod);
            }
            propertyType = readMethod.getReturnType();
            if (propertyType == Void.TYPE) {
                throw new IntrospectionException("Read method returns void: " + readMethod);
            }
        }
        if (writeMethod != null) {
            Class<?>[] params = writeMethod.getParameterTypes();
            if (params.length != 1) {
                throw new IntrospectionException("Bad write method arg count: " + writeMethod);
            }
            if (propertyType != null) {
                if (propertyType.isAssignableFrom(params[0])) {
                    // 编写方法属性类型可能更具体
                    propertyType = params[0];
                } else if (params[0].isAssignableFrom(propertyType)) {
                    // 继续读取方法属性的类型
                } else {
                    throw new IntrospectionException("Type mismatch between read and write methods: " + readMethod + " - " + writeMethod);
                }
            } else {
                propertyType = params[0];
            }
        }
        return propertyType;
    }

    /**
     * 请参阅 {@link java.beans.IndexedPropertyDescriptor#findIndexedPropertyType}。
     */
    @Nullable
    public static Class<?> findIndexedPropertyType(String name, @Nullable Class<?> propertyType, @Nullable Method indexedReadMethod, @Nullable Method indexedWriteMethod) throws IntrospectionException {
        Class<?> indexedPropertyType = null;
        if (indexedReadMethod != null) {
            Class<?>[] params = indexedReadMethod.getParameterTypes();
            if (params.length != 1) {
                throw new IntrospectionException("Bad indexed read method arg count: " + indexedReadMethod);
            }
            if (params[0] != Integer.TYPE) {
                throw new IntrospectionException("Non int index to indexed read method: " + indexedReadMethod);
            }
            indexedPropertyType = indexedReadMethod.getReturnType();
            if (indexedPropertyType == Void.TYPE) {
                throw new IntrospectionException("Indexed read method returns void: " + indexedReadMethod);
            }
        }
        if (indexedWriteMethod != null) {
            Class<?>[] params = indexedWriteMethod.getParameterTypes();
            if (params.length != 2) {
                throw new IntrospectionException("Bad indexed write method arg count: " + indexedWriteMethod);
            }
            if (params[0] != Integer.TYPE) {
                throw new IntrospectionException("Non int index to indexed write method: " + indexedWriteMethod);
            }
            if (indexedPropertyType != null) {
                if (indexedPropertyType.isAssignableFrom(params[1])) {
                    // 编写方法属性类型可能更加具体
                    indexedPropertyType = params[1];
                } else if (params[1].isAssignableFrom(indexedPropertyType)) {
                    // 继续读取方法属性的类型
                } else {
                    throw new IntrospectionException("Type mismatch between indexed read and write methods: " + indexedReadMethod + " - " + indexedWriteMethod);
                }
            } else {
                indexedPropertyType = params[1];
            }
        }
        if (propertyType != null && (!propertyType.isArray() || propertyType.getComponentType() != indexedPropertyType)) {
            throw new IntrospectionException("Type mismatch between indexed and non-indexed methods: " + indexedReadMethod + " - " + indexedWriteMethod);
        }
        return indexedPropertyType;
    }

    /**
     * 比较给定的 {@code PropertyDescriptors}，如果它们等价，则返回 {@code true}，即它们的读取方法、写入方法、属性类型、属性编辑器和标志都相等。
     * @see java.beans.PropertyDescriptor#equals(Object)
     */
    public static boolean equals(PropertyDescriptor pd, PropertyDescriptor otherPd) {
        return (ObjectUtils.nullSafeEquals(pd.getReadMethod(), otherPd.getReadMethod()) && ObjectUtils.nullSafeEquals(pd.getWriteMethod(), otherPd.getWriteMethod()) && ObjectUtils.nullSafeEquals(pd.getPropertyType(), otherPd.getPropertyType()) && ObjectUtils.nullSafeEquals(pd.getPropertyEditorClass(), otherPd.getPropertyEditorClass()) && pd.isBound() == otherPd.isBound() && pd.isConstrained() == otherPd.isConstrained());
    }

    /**
     * 用于 {@link #determineBasicProperties(Class)} 的属性描述符，
     * 在设置 {@link #setReadMethod}/{@link #setWriteMethod} 方法时不进行任何早期类型确定。
     * 自 5.3.24 版本以来。
     */
    private static class BasicPropertyDescriptor extends PropertyDescriptor {

        @Nullable
        private Method readMethod;

        @Nullable
        private Method writeMethod;

        private final List<Method> alternativeWriteMethods = new ArrayList<>();

        public BasicPropertyDescriptor(String propertyName, @Nullable Method readMethod, @Nullable Method writeMethod) throws IntrospectionException {
            super(propertyName, readMethod, writeMethod);
        }

        @Override
        public void setReadMethod(@Nullable Method readMethod) {
            this.readMethod = readMethod;
        }

        @Override
        @Nullable
        public Method getReadMethod() {
            return this.readMethod;
        }

        @Override
        public void setWriteMethod(@Nullable Method writeMethod) {
            this.writeMethod = writeMethod;
        }

        public void addWriteMethod(Method writeMethod) {
            if (this.writeMethod != null) {
                this.alternativeWriteMethods.add(this.writeMethod);
                this.writeMethod = null;
            }
            this.alternativeWriteMethods.add(writeMethod);
        }

        @Override
        @Nullable
        public Method getWriteMethod() {
            if (this.writeMethod == null && !this.alternativeWriteMethods.isEmpty()) {
                if (this.readMethod == null) {
                    return this.alternativeWriteMethods.get(0);
                } else {
                    for (Method method : this.alternativeWriteMethods) {
                        if (this.readMethod.getReturnType().isAssignableFrom(method.getParameterTypes()[0])) {
                            this.writeMethod = method;
                            break;
                        }
                    }
                }
            }
            return this.writeMethod;
        }
    }
}
