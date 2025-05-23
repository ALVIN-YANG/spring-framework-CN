// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非符合许可证规定。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵权。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.function.ThrowingConsumer;

/**
 * 用于支持字段自动装配的解析器。通常用于AOT处理的应用中，作为
 * {@link org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 * AutowiredAnnotationBeanPostProcessor}的替代方案。
 *
 * <p>在原生镜像中解析参数时，所使用的{@link Field}必须标记为带有
 * {@link ExecutableMode#INTROSPECT introspection}提示，以便可以读取字段注解。
 * 只有当此类的{@link #resolveAndSet(RegisteredBean, Object)}方法被使用时（通常用于支持私有字段）
 * 才需要完整的{@link ExecutableMode#INVOKE invocation}提示。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
public final class AutowiredFieldValueResolver extends AutowiredElementResolver {

    private final String fieldName;

    private final boolean required;

    @Nullable
    private final String shortcut;

    private AutowiredFieldValueResolver(String fieldName, boolean required, @Nullable String shortcut) {
        Assert.hasText(fieldName, "'fieldName' must not be empty");
        this.fieldName = fieldName;
        this.required = required;
        this.shortcut = shortcut;
    }

    /**
     * 为指定的字段创建一个新的 {@link AutowiredFieldValueResolver}，其中注入是可选的。
     * @param fieldName 字段名称
     * @return 一个新的 {@link AutowiredFieldValueResolver} 实例
     */
    public static AutowiredFieldValueResolver forField(String fieldName) {
        return new AutowiredFieldValueResolver(fieldName, false, null);
    }

    /**
     * 为指定的需要注入的字段创建一个新的 {@link AutowiredFieldValueResolver}
     * @param fieldName 字段名称
     * @return 一个新的 {@link AutowiredFieldValueResolver} 实例
     */
    public static AutowiredFieldValueResolver forRequiredField(String fieldName) {
        return new AutowiredFieldValueResolver(fieldName, true, null);
    }

    /**
     * 返回一个新的 {@link AutowiredFieldValueResolver} 实例，该实例使用直接通过 Bean 名称注入的快捷方式。
     * @param beanName 要使用的 Bean 名称作为快捷方式
     * @return 一个新的、使用快捷方式的 {@link AutowiredFieldValueResolver} 实例
     */
    public AutowiredFieldValueResolver withShortcut(String beanName) {
        return new AutowiredFieldValueResolver(this.fieldName, this.required, beanName);
    }

    /**
     * 解析指定注册的 bean 的字段，并将其提供给给定的操作。
     * @param registeredBean 注册的 bean
     * @param action 使用解析后的字段值执行的操作
     */
    public <T> void resolve(RegisteredBean registeredBean, ThrowingConsumer<T> action) {
        Assert.notNull(registeredBean, "'registeredBean' must not be null");
        Assert.notNull(action, "'action' must not be null");
        T resolved = resolve(registeredBean);
        if (resolved != null) {
            action.accept(resolved);
        }
    }

    /**
     * 解析指定已注册 Bean 的字段值。
     * @param registeredBean 已注册的 Bean
     * @param requiredType 所需的类型
     * @return 解析得到的字段值
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T resolve(RegisteredBean registeredBean, Class<T> requiredType) {
        Object value = resolveObject(registeredBean);
        Assert.isInstanceOf(requiredType, value);
        return (T) value;
    }

    /**
     * 解决指定已注册 Bean 的字段值。
     * @param registeredBean 已注册的 Bean
     * @return 解析得到的字段值
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T resolve(RegisteredBean registeredBean) {
        return (T) resolveObject(registeredBean);
    }

    /**
     * 解析指定已注册 Bean 的字段值。
     * @param registeredBean 已注册的 Bean
     * @return 解析得到的字段值
     */
    @Nullable
    public Object resolveObject(RegisteredBean registeredBean) {
        Assert.notNull(registeredBean, "'registeredBean' must not be null");
        return resolveValue(registeredBean, getField(registeredBean));
    }

    /**
     * 解析指定已注册的 Bean 的字段值，并使用反射将其设置。
     * @param registeredBean 已注册的 Bean
     * @param instance Bean 实例
     */
    public void resolveAndSet(RegisteredBean registeredBean, Object instance) {
        Assert.notNull(registeredBean, "'registeredBean' must not be null");
        Assert.notNull(instance, "'instance' must not be null");
        Field field = getField(registeredBean);
        Object resolved = resolveValue(registeredBean, field);
        if (resolved != null) {
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, instance, resolved);
        }
    }

    @Nullable
    private Object resolveValue(RegisteredBean registeredBean, Field field) {
        String beanName = registeredBean.getBeanName();
        Class<?> beanClass = registeredBean.getBeanClass();
        ConfigurableBeanFactory beanFactory = registeredBean.getBeanFactory();
        DependencyDescriptor descriptor = new DependencyDescriptor(field, this.required);
        descriptor.setContainingClass(beanClass);
        if (this.shortcut != null) {
            descriptor = new ShortcutDependencyDescriptor(descriptor, this.shortcut);
        }
        Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
        TypeConverter typeConverter = beanFactory.getTypeConverter();
        try {
            Assert.isInstanceOf(AutowireCapableBeanFactory.class, beanFactory);
            Object value = ((AutowireCapableBeanFactory) beanFactory).resolveDependency(descriptor, beanName, autowiredBeanNames, typeConverter);
            registerDependentBeans(beanFactory, beanName, autowiredBeanNames);
            return value;
        } catch (BeansException ex) {
            throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(field), ex);
        }
    }

    private Field getField(RegisteredBean registeredBean) {
        Field field = ReflectionUtils.findField(registeredBean.getBeanClass(), this.fieldName);
        Assert.notNull(field, () -> "No field '" + this.fieldName + "' found on " + registeredBean.getBeanClass().getName());
        return field;
    }
}
