// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0 ("许可证") 许可使用，除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为还是其他方面。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * 为标准 {@link BeanInfo} 对象的装饰器，例如通过 {@link Introspector#getBeanInfo(Class)} 创建，旨在发现并注册静态和非 void 返回值的设置器方法。例如：
 *
 * <pre class="code">
 * public class Bean {
 *
 *     private Foo foo;
 *
 *     public Foo getFoo() {
 *         return this.foo;
 *     }
 *
 *     public Bean setFoo(Foo foo) {
 *         this.foo = foo;
 *         return this;
 *     }
 * }</pre>
 * 标准的 JavaBeans {@code Introspector} 将会发现读取方法 {@code getFoo}，但会跳过设置方法 {@code #setFoo(Foo)}，因为它的非 void 返回签名不符合 JavaBeans 规范。
 * 相反，{@code ExtendedBeanInfo} 将会识别并包含它。这是为了允许在 Spring 的 {@code <beans>} XML 中使用具有 "构建器" 或方法链式设置器签名的 API。{@link #getPropertyDescriptors()} 返回被包装的 {@code BeanInfo} 中所有现有的属性描述符，以及为非 void 返回的设置器添加的任何属性描述符。标准（"非索引"）和
 * <a href="https://docs.oracle.com/javase/tutorial/javabeans/writing/properties.html">
 * 索引属性</a> 都得到完全支持。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see #ExtendedBeanInfo(BeanInfo)
 * @see ExtendedBeanInfoFactory
 * @see CachedIntrospectionResults
 */
class ExtendedBeanInfo implements BeanInfo {

    private static final Log logger = LogFactory.getLog(ExtendedBeanInfo.class);

    private final BeanInfo delegate;

    private final Set<PropertyDescriptor> propertyDescriptors = new TreeSet<>(new PropertyDescriptorComparator());

    /**
     * 包装给定的 {@link BeanInfo} 实例；在本地复制其现有的属性描述符
     * 并将每个描述符封装在自定义的 {@link SimpleIndexedPropertyDescriptor 索引型}
     * 或 {@link SimplePropertyDescriptor 非索引型} 属性描述符变体中，从而绕过默认 JDK 弱/软引用管理；
     * 然后遍历其方法描述符，查找任何非 void 返回值的写方法，并为每个找到的方法更新或创建相应的
     * {@link PropertyDescriptor}。
     * @param delegate 被包装的 {@code BeanInfo}，该实例不会被修改
     * @see #getPropertyDescriptors()
     */
    public ExtendedBeanInfo(BeanInfo delegate) {
        this.delegate = delegate;
        for (PropertyDescriptor pd : delegate.getPropertyDescriptors()) {
            try {
                this.propertyDescriptors.add(pd instanceof IndexedPropertyDescriptor indexedPd ? new SimpleIndexedPropertyDescriptor(indexedPd) : new SimplePropertyDescriptor(pd));
            } catch (IntrospectionException ex) {
                // 可能仅仅是一个不应该遵循JavaBeans模式的方法...
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring invalid bean property '" + pd.getName() + "': " + ex.getMessage());
                }
            }
        }
        MethodDescriptor[] methodDescriptors = delegate.getMethodDescriptors();
        if (methodDescriptors != null) {
            for (Method method : findCandidateWriteMethods(methodDescriptors)) {
                try {
                    handleCandidateWriteMethod(method);
                } catch (IntrospectionException ex) {
                    // 我们只是在尝试寻找候选人，这里可以轻松忽略额外的那些...
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ignoring candidate write method [" + method + "]: " + ex.getMessage());
                    }
                }
            }
        }
    }

    private List<Method> findCandidateWriteMethods(MethodDescriptor[] methodDescriptors) {
        List<Method> matches = new ArrayList<>();
        for (MethodDescriptor methodDescriptor : methodDescriptors) {
            Method method = methodDescriptor.getMethod();
            if (isCandidateWriteMethod(method)) {
                matches.add(method);
            }
        }
        // 将非void返回值的写方法进行排序，以防范不良影响
        // 非确定性排序来自 Class#getMethods 返回的方法。
        // 由于历史原因，自然排序顺序被反转。
        // 请参阅 https://github.com/spring-projects/spring-framework/issues/14744。
        matches.sort(Comparator.comparing(Method::toString).reversed());
        return matches;
    }

    public static boolean isCandidateWriteMethod(Method method) {
        String methodName = method.getName();
        int nParams = method.getParameterCount();
        return (methodName.length() > 3 && methodName.startsWith("set") && Modifier.isPublic(method.getModifiers()) && (!void.class.isAssignableFrom(method.getReturnType()) || Modifier.isStatic(method.getModifiers())) && (nParams == 1 || (nParams == 2 && int.class == method.getParameterTypes()[0])));
    }

    private void handleCandidateWriteMethod(Method method) throws IntrospectionException {
        int nParams = method.getParameterCount();
        String propertyName = propertyNameFor(method);
        Class<?> propertyType = method.getParameterTypes()[nParams - 1];
        PropertyDescriptor existingPd = findExistingPropertyDescriptor(propertyName, propertyType);
        if (nParams == 1) {
            if (existingPd == null) {
                this.propertyDescriptors.add(new SimplePropertyDescriptor(propertyName, null, method));
            } else {
                existingPd.setWriteMethod(method);
            }
        } else if (nParams == 2) {
            if (existingPd == null) {
                this.propertyDescriptors.add(new SimpleIndexedPropertyDescriptor(propertyName, null, null, null, method));
            } else if (existingPd instanceof IndexedPropertyDescriptor indexedPd) {
                indexedPd.setIndexedWriteMethod(method);
            } else {
                this.propertyDescriptors.remove(existingPd);
                this.propertyDescriptors.add(new SimpleIndexedPropertyDescriptor(propertyName, existingPd.getReadMethod(), existingPd.getWriteMethod(), null, method));
            }
        } else {
            throw new IllegalArgumentException("Write method must have exactly 1 or 2 parameters: " + method);
        }
    }

    @Nullable
    private PropertyDescriptor findExistingPropertyDescriptor(String propertyName, Class<?> propertyType) {
        for (PropertyDescriptor pd : this.propertyDescriptors) {
            final Class<?> candidateType;
            final String candidateName = pd.getName();
            if (pd instanceof IndexedPropertyDescriptor indexedPd) {
                candidateType = indexedPd.getIndexedPropertyType();
                if (candidateName.equals(propertyName) && (candidateType.equals(propertyType) || candidateType.equals(propertyType.getComponentType()))) {
                    return pd;
                }
            } else {
                candidateType = pd.getPropertyType();
                if (candidateName.equals(propertyName) && (candidateType.equals(propertyType) || propertyType.equals(candidateType.getComponentType()))) {
                    return pd;
                }
            }
        }
        return null;
    }

    private String propertyNameFor(Method method) {
        return Introspector.decapitalize(method.getName().substring(3));
    }

    /**
     * 返回从包装的 {@link PropertyDescriptor PropertyDescriptors} 对象以及构造过程中找到的每个非 void 返回值的 setter 方法对应的 {@code PropertyDescriptors} 集合。
     * @see #ExtendedBeanInfo(BeanInfo)
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return this.propertyDescriptors.toArray(new PropertyDescriptor[0]);
    }

    @Override
    public BeanInfo[] getAdditionalBeanInfo() {
        return this.delegate.getAdditionalBeanInfo();
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return this.delegate.getBeanDescriptor();
    }

    @Override
    public int getDefaultEventIndex() {
        return this.delegate.getDefaultEventIndex();
    }

    @Override
    public int getDefaultPropertyIndex() {
        return this.delegate.getDefaultPropertyIndex();
    }

    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        return this.delegate.getEventSetDescriptors();
    }

    @Override
    public Image getIcon(int iconKind) {
        return this.delegate.getIcon(iconKind);
    }

    @Override
    public MethodDescriptor[] getMethodDescriptors() {
        return this.delegate.getMethodDescriptors();
    }

    /**
     * 一个简单的 {@link PropertyDescriptor}。
     */
    static class SimplePropertyDescriptor extends PropertyDescriptor {

        @Nullable
        private Method readMethod;

        @Nullable
        private Method writeMethod;

        @Nullable
        private Class<?> propertyType;

        @Nullable
        private Class<?> propertyEditorClass;

        public SimplePropertyDescriptor(PropertyDescriptor original) throws IntrospectionException {
            this(original.getName(), original.getReadMethod(), original.getWriteMethod());
            PropertyDescriptorUtils.copyNonMethodProperties(original, this);
        }

        public SimplePropertyDescriptor(String propertyName, @Nullable Method readMethod, Method writeMethod) throws IntrospectionException {
            super(propertyName, null, null);
            this.readMethod = readMethod;
            this.writeMethod = writeMethod;
            this.propertyType = PropertyDescriptorUtils.findPropertyType(readMethod, writeMethod);
        }

        @Override
        @Nullable
        public Method getReadMethod() {
            return this.readMethod;
        }

        @Override
        public void setReadMethod(@Nullable Method readMethod) {
            this.readMethod = readMethod;
        }

        @Override
        @Nullable
        public Method getWriteMethod() {
            return this.writeMethod;
        }

        @Override
        public void setWriteMethod(@Nullable Method writeMethod) {
            this.writeMethod = writeMethod;
        }

        @Override
        @Nullable
        public Class<?> getPropertyType() {
            if (this.propertyType == null) {
                try {
                    this.propertyType = PropertyDescriptorUtils.findPropertyType(this.readMethod, this.writeMethod);
                } catch (IntrospectionException ex) {
                    // 忽略，就像 PropertyDescriptor# getPropertyType 所做的那样
                }
            }
            return this.propertyType;
        }

        @Override
        @Nullable
        public Class<?> getPropertyEditorClass() {
            return this.propertyEditorClass;
        }

        @Override
        public void setPropertyEditorClass(@Nullable Class<?> propertyEditorClass) {
            this.propertyEditorClass = propertyEditorClass;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof PropertyDescriptor that && PropertyDescriptorUtils.equals(this, that)));
        }

        @Override
        public int hashCode() {
            return (ObjectUtils.nullSafeHashCode(getReadMethod()) * 29 + ObjectUtils.nullSafeHashCode(getWriteMethod()));
        }

        @Override
        public String toString() {
            return String.format("%s[name=%s, propertyType=%s, readMethod=%s, writeMethod=%s]", getClass().getSimpleName(), getName(), getPropertyType(), this.readMethod, this.writeMethod);
        }
    }

    /**
     * 一个简单的 {@link IndexedPropertyDescriptor}。
     */
    static class SimpleIndexedPropertyDescriptor extends IndexedPropertyDescriptor {

        @Nullable
        private Method readMethod;

        @Nullable
        private Method writeMethod;

        @Nullable
        private Class<?> propertyType;

        @Nullable
        private Method indexedReadMethod;

        @Nullable
        private Method indexedWriteMethod;

        @Nullable
        private Class<?> indexedPropertyType;

        @Nullable
        private Class<?> propertyEditorClass;

        public SimpleIndexedPropertyDescriptor(IndexedPropertyDescriptor original) throws IntrospectionException {
            this(original.getName(), original.getReadMethod(), original.getWriteMethod(), original.getIndexedReadMethod(), original.getIndexedWriteMethod());
            PropertyDescriptorUtils.copyNonMethodProperties(original, this);
        }

        public SimpleIndexedPropertyDescriptor(String propertyName, @Nullable Method readMethod, @Nullable Method writeMethod, @Nullable Method indexedReadMethod, Method indexedWriteMethod) throws IntrospectionException {
            super(propertyName, null, null, null, null);
            this.readMethod = readMethod;
            this.writeMethod = writeMethod;
            this.propertyType = PropertyDescriptorUtils.findPropertyType(readMethod, writeMethod);
            this.indexedReadMethod = indexedReadMethod;
            this.indexedWriteMethod = indexedWriteMethod;
            this.indexedPropertyType = PropertyDescriptorUtils.findIndexedPropertyType(propertyName, this.propertyType, indexedReadMethod, indexedWriteMethod);
        }

        @Override
        @Nullable
        public Method getReadMethod() {
            return this.readMethod;
        }

        @Override
        public void setReadMethod(@Nullable Method readMethod) {
            this.readMethod = readMethod;
        }

        @Override
        @Nullable
        public Method getWriteMethod() {
            return this.writeMethod;
        }

        @Override
        public void setWriteMethod(@Nullable Method writeMethod) {
            this.writeMethod = writeMethod;
        }

        @Override
        @Nullable
        public Class<?> getPropertyType() {
            if (this.propertyType == null) {
                try {
                    this.propertyType = PropertyDescriptorUtils.findPropertyType(this.readMethod, this.writeMethod);
                } catch (IntrospectionException ex) {
                    // 忽略，正如 IndexedPropertyDescriptor# getPropertyType 所做的那样
                }
            }
            return this.propertyType;
        }

        @Override
        @Nullable
        public Method getIndexedReadMethod() {
            return this.indexedReadMethod;
        }

        @Override
        public void setIndexedReadMethod(@Nullable Method indexedReadMethod) throws IntrospectionException {
            this.indexedReadMethod = indexedReadMethod;
        }

        @Override
        @Nullable
        public Method getIndexedWriteMethod() {
            return this.indexedWriteMethod;
        }

        @Override
        public void setIndexedWriteMethod(@Nullable Method indexedWriteMethod) throws IntrospectionException {
            this.indexedWriteMethod = indexedWriteMethod;
        }

        @Override
        @Nullable
        public Class<?> getIndexedPropertyType() {
            if (this.indexedPropertyType == null) {
                try {
                    this.indexedPropertyType = PropertyDescriptorUtils.findIndexedPropertyType(getName(), getPropertyType(), this.indexedReadMethod, this.indexedWriteMethod);
                } catch (IntrospectionException ex) {
                    // 忽略，就像 IndexedPropertyDescriptor#getColorPropertyType 方法一样
                }
            }
            return this.indexedPropertyType;
        }

        @Override
        @Nullable
        public Class<?> getPropertyEditorClass() {
            return this.propertyEditorClass;
        }

        @Override
        public void setPropertyEditorClass(@Nullable Class<?> propertyEditorClass) {
            this.propertyEditorClass = propertyEditorClass;
        }

        /** 请参阅 java.beans.IndexedPropertyDescriptor#equals 方法*/
        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof IndexedPropertyDescriptor that && ObjectUtils.nullSafeEquals(getIndexedReadMethod(), that.getIndexedReadMethod()) && ObjectUtils.nullSafeEquals(getIndexedWriteMethod(), that.getIndexedWriteMethod()) && ObjectUtils.nullSafeEquals(getIndexedPropertyType(), that.getIndexedPropertyType()) && PropertyDescriptorUtils.equals(this, that)));
        }

        @Override
        public int hashCode() {
            int hashCode = ObjectUtils.nullSafeHashCode(getReadMethod());
            hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getWriteMethod());
            hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getIndexedReadMethod());
            hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getIndexedWriteMethod());
            return hashCode;
        }

        @Override
        public String toString() {
            return String.format("%s[name=%s, propertyType=%s, indexedPropertyType=%s, " + "readMethod=%s, writeMethod=%s, indexedReadMethod=%s, indexedWriteMethod=%s]", getClass().getSimpleName(), getName(), getPropertyType(), getIndexedPropertyType(), this.readMethod, this.writeMethod, this.indexedReadMethod, this.indexedWriteMethod);
        }
    }

    /**
     * 按字母数字顺序对 PropertyDescriptor 实例进行排序，以模拟
     * {@link java.beans.BeanInfo#getPropertyDescriptors()} 的行为。
     * @see ExtendedBeanInfo#propertyDescriptors
     */
    static class PropertyDescriptorComparator implements Comparator<PropertyDescriptor> {

        @Override
        public int compare(PropertyDescriptor desc1, PropertyDescriptor desc2) {
            String left = desc1.getName();
            String right = desc2.getName();
            byte[] leftBytes = left.getBytes();
            byte[] rightBytes = right.getBytes();
            for (int i = 0; i < left.length(); i++) {
                if (right.length() == i) {
                    return 1;
                }
                int result = leftBytes[i] - rightBytes[i];
                if (result != 0) {
                    return result;
                }
            }
            return left.length() - right.length();
        }
    }
}
