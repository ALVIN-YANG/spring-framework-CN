// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * {@link ConfigurablePropertyAccessor} 的实现，直接访问实例字段。允许直接将字段绑定，而不是通过 JavaBean 的设置器。
 *
 * <p>自 Spring 4.2 以来，大多数的 {@link BeanWrapper} 功能已经合并到 {@link AbstractPropertyAccessor} 中，这意味着属性遍历以及集合和映射的访问现在也在此处支持。
 *
 * <p>DirectFieldAccessor 的 "extractOldValueForEditor" 设置默认值为 "true"，因为字段总是可以读取而不会产生副作用。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.0
 * @see #setExtractOldValueForEditor
 * @see BeanWrapper
 * @see org.springframework.validation.DirectFieldBindingResult
 * @see org.springframework.validation.DataBinder#initDirectFieldAccess()
 */
public class DirectFieldAccessor extends AbstractNestablePropertyAccessor {

    private final Map<String, FieldPropertyHandler> fieldMap = new HashMap<>();

    /**
     * 为给定对象创建一个新的DirectFieldAccessor。
     * @param object 由这个DirectFieldAccessor包装的对象
     */
    public DirectFieldAccessor(Object object) {
        super(object);
    }

    /**
     * 为给定对象创建一个新的 DirectFieldAccessor，
     * 注册对象所在的嵌套路径。
     * @param object 由此 DirectFieldAccessor 包装的对象
     * @param nestedPath 对象的嵌套路径
     * @param parent 包含 DirectFieldAccessor 的容器（不得为 {@code null}）
     */
    protected DirectFieldAccessor(Object object, String nestedPath, DirectFieldAccessor parent) {
        super(object, nestedPath, parent);
    }

    @Override
    @Nullable
    protected FieldPropertyHandler getLocalPropertyHandler(String propertyName) {
        FieldPropertyHandler propertyHandler = this.fieldMap.get(propertyName);
        if (propertyHandler == null) {
            Field field = ReflectionUtils.findField(getWrappedClass(), propertyName);
            if (field != null) {
                propertyHandler = new FieldPropertyHandler(field);
                this.fieldMap.put(propertyName, propertyHandler);
            }
        }
        return propertyHandler;
    }

    @Override
    protected DirectFieldAccessor newNestedPropertyAccessor(Object object, String nestedPath) {
        return new DirectFieldAccessor(object, nestedPath, this);
    }

    @Override
    protected NotWritablePropertyException createNotWritablePropertyException(String propertyName) {
        PropertyMatches matches = PropertyMatches.forField(propertyName, getRootClass());
        throw new NotWritablePropertyException(getRootClass(), getNestedPath() + propertyName, matches.buildErrorMessage(), matches.getPossibleMatches());
    }

    private class FieldPropertyHandler extends PropertyHandler {

        private final Field field;

        public FieldPropertyHandler(Field field) {
            super(field.getType(), true, true);
            this.field = field;
        }

        @Override
        public TypeDescriptor toTypeDescriptor() {
            return new TypeDescriptor(this.field);
        }

        @Override
        public ResolvableType getResolvableType() {
            return ResolvableType.forField(this.field);
        }

        @Override
        @Nullable
        public TypeDescriptor nested(int level) {
            return TypeDescriptor.nested(this.field, level);
        }

        @Override
        @Nullable
        public Object getValue() throws Exception {
            try {
                ReflectionUtils.makeAccessible(this.field);
                return this.field.get(getWrappedInstance());
            } catch (IllegalAccessException ex) {
                throw new InvalidPropertyException(getWrappedClass(), this.field.getName(), "Field is not accessible", ex);
            }
        }

        @Override
        public void setValue(@Nullable Object value) throws Exception {
            try {
                ReflectionUtils.makeAccessible(this.field);
                this.field.set(getWrappedInstance(), value);
            } catch (IllegalAccessException ex) {
                throw new InvalidPropertyException(getWrappedClass(), this.field.getName(), "Field is not accessible", ex);
            }
        }
    }
}
