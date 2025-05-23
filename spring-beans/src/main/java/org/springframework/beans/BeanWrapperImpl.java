// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * 默认的 {@link BeanWrapper} 实现，对于所有典型用例都应足够。为了效率，它会缓存反射结果。
 *
 * <p>注意：自动注册来自 {@code org.springframework.beans.propertyeditors} 包的默认属性编辑器，这些编辑器除了 JDK 的标准属性编辑器外还会应用。应用程序可以通过调用 {@link #registerCustomEditor(Class, java.beans.PropertyEditor)} 方法来注册特定实例的编辑器（即它们不是在整个应用程序中共享的）。有关详细信息，请参阅基类 {@link PropertyEditorRegistrySupport}。
 *
 * <p><b>注意：自 Spring 2.5 以来，对于几乎所有目的而言，这是一个内部类。</b> 它之所以公开，是为了允许其他框架包访问。对于标准应用程序访问目的，请使用 {@link PropertyAccessorFactory#forBeanPropertyAccess} 工厂方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Stephane Nicoll
 * @since 15 April 2001
 * @see #registerCustomEditor
 * @see #setPropertyValues
 * @see #setPropertyValue
 * @see #getPropertyValue
 * @see #getPropertyType
 * @see BeanWrapper
 * @see PropertyEditorRegistrySupport
 */
public class BeanWrapperImpl extends AbstractNestablePropertyAccessor implements BeanWrapper {

    /**
     * 缓存此对象的内省结果，以避免每次都遇到JavaBeans内省的成本。
     */
    @Nullable
    private CachedIntrospectionResults cachedIntrospectionResults;

    /**
     * 创建一个新的空的 BeanWrapperImpl 对象。稍后需要设置包装实例。
     * 注册默认的编辑器。
     * @see #setWrappedInstance
     */
    public BeanWrapperImpl() {
        this(true);
    }

    /**
     * 创建一个新的空的 BeanWrapperImpl 对象。之后需要设置包装实例。
     * @param registerDefaultEditors 是否注册默认的编辑器
     * （如果 BeanWrapper 不需要任何类型转换，可以抑制注册）
     * @see #setWrappedInstance
     */
    public BeanWrapperImpl(boolean registerDefaultEditors) {
        super(registerDefaultEditors);
    }

    /**
     * 为给定的对象创建一个新的 BeanWrapperImpl。
     * @param object 由这个 BeanWrapper 封装的对象
     */
    public BeanWrapperImpl(Object object) {
        super(object);
    }

    /**
     * 创建一个新的BeanWrapperImpl，该实例封装了指定类的新的实例。
     * @param clazz 要实例化和封装的类
     */
    public BeanWrapperImpl(Class<?> clazz) {
        super(clazz);
    }

    /**
     * 为给定的对象创建一个新的 BeanWrapperImpl，
     * 注册对象所在的嵌套路径。
     * @param object 由这个 BeanWrapper 包装的对象
     * @param nestedPath 对象的嵌套路径
     * @param rootObject 路径顶部的根对象
     */
    public BeanWrapperImpl(Object object, String nestedPath, Object rootObject) {
        super(object, nestedPath, rootObject);
    }

    /**
     * 为给定的对象创建一个新的 BeanWrapperImpl，
     * 注册对象所在的嵌套路径。
     * @param object 由这个 BeanWrapper 包装的对象
     * @param nestedPath 对象的嵌套路径
     * @param parent 包含的 BeanWrapper（不得为 {@code null}）
     */
    private BeanWrapperImpl(Object object, String nestedPath, BeanWrapperImpl parent) {
        super(object, nestedPath, parent);
    }

    /**
     * 设置一个 Bean 实例以保存，无需对 {@link java.util.Optional} 进行任何解包。
     * @param object 实际的目标对象
     * @since 4.3
     * @see #setWrappedInstance(Object)
     */
    public void setBeanInstance(Object object) {
        this.wrappedObject = object;
        this.rootObject = object;
        this.typeConverterDelegate = new TypeConverterDelegate(this, this.wrappedObject);
        setIntrospectionClass(object.getClass());
    }

    @Override
    public void setWrappedInstance(Object object, @Nullable String nestedPath, @Nullable Object rootObject) {
        super.setWrappedInstance(object, nestedPath, rootObject);
        setIntrospectionClass(getWrappedClass());
    }

    /**
     * 设置要反射的类。
     * 需要在目标对象改变时调用。
     * @param clazz 要反射的类
     */
    protected void setIntrospectionClass(Class<?> clazz) {
        if (this.cachedIntrospectionResults != null && this.cachedIntrospectionResults.getBeanClass() != clazz) {
            this.cachedIntrospectionResults = null;
        }
    }

    /**
     * 获取包装对象的延迟初始化的 CachedIntrospectionResults 实例。
     */
    private CachedIntrospectionResults getCachedIntrospectionResults() {
        if (this.cachedIntrospectionResults == null) {
            this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(getWrappedClass());
        }
        return this.cachedIntrospectionResults;
    }

    /**
     * 将指定属性的给定值转换为该属性的类型。
     * <p>此方法仅用于BeanFactory中的优化。
     * 使用{@code convertIfNecessary}方法进行程序化转换。
     * @param value 要转换的值
     * @param propertyName 目标属性
     * (注意，此处不支持嵌套或索引属性)
     * @return 新值，可能是类型转换的结果
     * @throws TypeMismatchException 如果类型转换失败
     */
    @Nullable
    public Object convertForProperty(@Nullable Object value, String propertyName) throws TypeMismatchException {
        CachedIntrospectionResults cachedIntrospectionResults = getCachedIntrospectionResults();
        PropertyDescriptor pd = cachedIntrospectionResults.getPropertyDescriptor(propertyName);
        if (pd == null) {
            throw new InvalidPropertyException(getRootClass(), getNestedPath() + propertyName, "No property '" + propertyName + "' found");
        }
        TypeDescriptor td = cachedIntrospectionResults.getTypeDescriptor(pd);
        if (td == null) {
            td = cachedIntrospectionResults.addTypeDescriptor(pd, new TypeDescriptor(property(pd)));
        }
        return convertForProperty(propertyName, null, value, td);
    }

    private Property property(PropertyDescriptor pd) {
        GenericTypeAwarePropertyDescriptor gpd = (GenericTypeAwarePropertyDescriptor) pd;
        return new Property(gpd.getBeanClass(), gpd.getReadMethod(), gpd.getWriteMethod(), gpd.getName());
    }

    @Override
    @Nullable
    protected BeanPropertyHandler getLocalPropertyHandler(String propertyName) {
        PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(propertyName);
        return (pd != null ? new BeanPropertyHandler(pd) : null);
    }

    @Override
    protected BeanWrapperImpl newNestedPropertyAccessor(Object object, String nestedPath) {
        return new BeanWrapperImpl(object, nestedPath, this);
    }

    @Override
    protected NotWritablePropertyException createNotWritablePropertyException(String propertyName) {
        PropertyMatches matches = PropertyMatches.forProperty(propertyName, getRootClass());
        throw new NotWritablePropertyException(getRootClass(), getNestedPath() + propertyName, matches.buildErrorMessage(), matches.getPossibleMatches());
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return getCachedIntrospectionResults().getPropertyDescriptors();
    }

    @Override
    public PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException {
        BeanWrapperImpl nestedBw = (BeanWrapperImpl) getPropertyAccessorForPropertyPath(propertyName);
        String finalPath = getFinalPath(nestedBw, propertyName);
        PropertyDescriptor pd = nestedBw.getCachedIntrospectionResults().getPropertyDescriptor(finalPath);
        if (pd == null) {
            throw new InvalidPropertyException(getRootClass(), getNestedPath() + propertyName, "No property '" + propertyName + "' found");
        }
        return pd;
    }

    private class BeanPropertyHandler extends PropertyHandler {

        private final PropertyDescriptor pd;

        public BeanPropertyHandler(PropertyDescriptor pd) {
            super(pd.getPropertyType(), pd.getReadMethod() != null, pd.getWriteMethod() != null);
            this.pd = pd;
        }

        @Override
        public ResolvableType getResolvableType() {
            return ResolvableType.forMethodReturnType(this.pd.getReadMethod());
        }

        @Override
        public TypeDescriptor toTypeDescriptor() {
            return new TypeDescriptor(property(this.pd));
        }

        @Override
        @Nullable
        public TypeDescriptor nested(int level) {
            return TypeDescriptor.nested(property(this.pd), level);
        }

        @Override
        @Nullable
        public Object getValue() throws Exception {
            Method readMethod = this.pd.getReadMethod();
            ReflectionUtils.makeAccessible(readMethod);
            return readMethod.invoke(getWrappedInstance(), (Object[]) null);
        }

        @Override
        public void setValue(@Nullable Object value) throws Exception {
            Method writeMethod = (this.pd instanceof GenericTypeAwarePropertyDescriptor typeAwarePd ? typeAwarePd.getWriteMethodForActualAccess() : this.pd.getWriteMethod());
            ReflectionUtils.makeAccessible(writeMethod);
            writeMethod.invoke(getWrappedInstance(), value);
        }
    }
}
