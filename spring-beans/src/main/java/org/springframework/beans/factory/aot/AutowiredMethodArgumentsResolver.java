// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.function.ThrowingConsumer;

/**
 * 用于支持方法的自动装配的解析器。通常用于AOT（ ahead-of-time）处理的应用程序，作为
 * {@link org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 * AutowiredAnnotationBeanPostProcessor}的有针对性的替代方案。
 *
 * <p>在原生图像中解析参数时，使用的{@link Method}必须标记有{@link ExecutableMode#INTROSPECT
 * 检查}提示，以便可以读取字段注解。只有当使用此类中的
 * {@link #resolveAndInvoke(RegisteredBean, Object)}方法时，才需要完整的
 * {@link ExecutableMode#INVOKE 调用}提示（通常用于支持私有方法）。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
public final class AutowiredMethodArgumentsResolver extends AutowiredElementResolver {

    private final String methodName;

    private final Class<?>[] parameterTypes;

    private final boolean required;

    @Nullable
    private final String[] shortcuts;

    private AutowiredMethodArgumentsResolver(String methodName, Class<?>[] parameterTypes, boolean required, @Nullable String[] shortcuts) {
        Assert.hasText(methodName, "'methodName' must not be empty");
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.required = required;
        this.shortcuts = shortcuts;
    }

    /**
     * 为指定的方法创建一个新的 {@link AutowiredMethodArgumentsResolver}，其中注入是可选的。
     * @param methodName 方法名称
     * @param parameterTypes 工厂方法参数类型
     * @return 一个新的 {@link AutowiredFieldValueResolver} 实例
     */
    public static AutowiredMethodArgumentsResolver forMethod(String methodName, Class<?>... parameterTypes) {
        return new AutowiredMethodArgumentsResolver(methodName, parameterTypes, false, null);
    }

    /**
     * 为需要注入的指定方法创建一个新的 {@link AutowiredMethodArgumentsResolver}。
     * @param methodName 方法名称
     * @param parameterTypes 工厂方法参数类型
     * @return 一个新的 {@link AutowiredFieldValueResolver} 实例
     */
    public static AutowiredMethodArgumentsResolver forRequiredMethod(String methodName, Class<?>... parameterTypes) {
        return new AutowiredMethodArgumentsResolver(methodName, parameterTypes, true, null);
    }

    /**
     * 返回一个新的 {@link AutowiredMethodArgumentsResolver} 实例
     * 该实例使用特定的参数的直接bean名称注入快捷方式。
     * @param beanNames 要用作快捷方式的bean名称（与方法参数对齐）
     * @return 一个新的使用快捷方式的 {@link AutowiredMethodArgumentsResolver} 实例
     */
    public AutowiredMethodArgumentsResolver withShortcut(String... beanNames) {
        return new AutowiredMethodArgumentsResolver(this.methodName, this.parameterTypes, this.required, beanNames);
    }

    /**
     * 解析指定已注册 bean 的方法参数，并将其提供给要执行的动作。
     * @param registeredBean 已注册的 bean
     * @param action 要使用解析后的方法参数执行的动作
     */
    public void resolve(RegisteredBean registeredBean, ThrowingConsumer<AutowiredArguments> action) {
        Assert.notNull(registeredBean, "'registeredBean' must not be null");
        Assert.notNull(action, "'action' must not be null");
        AutowiredArguments resolved = resolve(registeredBean);
        if (resolved != null) {
            action.accept(resolved);
        }
    }

    /**
     * 解决指定已注册 Bean 的方法参数。
     * @param registeredBean 已注册的 Bean
     * @return 解析后的方法参数
     */
    @Nullable
    public AutowiredArguments resolve(RegisteredBean registeredBean) {
        Assert.notNull(registeredBean, "'registeredBean' must not be null");
        return resolveArguments(registeredBean, getMethod(registeredBean));
    }

    /**
     * 解决指定注册的 Bean 的方法参数，并使用反射调用该方法。
     * @param registeredBean 注册的 Bean
     * @param instance Bean 实例
     */
    public void resolveAndInvoke(RegisteredBean registeredBean, Object instance) {
        Assert.notNull(registeredBean, "'registeredBean' must not be null");
        Assert.notNull(instance, "'instance' must not be null");
        Method method = getMethod(registeredBean);
        AutowiredArguments resolved = resolveArguments(registeredBean, method);
        if (resolved != null) {
            ReflectionUtils.makeAccessible(method);
            ReflectionUtils.invokeMethod(method, instance, resolved.toArray());
        }
    }

    @Nullable
    private AutowiredArguments resolveArguments(RegisteredBean registeredBean, Method method) {
        String beanName = registeredBean.getBeanName();
        Class<?> beanClass = registeredBean.getBeanClass();
        ConfigurableBeanFactory beanFactory = registeredBean.getBeanFactory();
        Assert.isInstanceOf(AutowireCapableBeanFactory.class, beanFactory);
        AutowireCapableBeanFactory autowireCapableBeanFactory = (AutowireCapableBeanFactory) beanFactory;
        int argumentCount = method.getParameterCount();
        Object[] arguments = new Object[argumentCount];
        Set<String> autowiredBeanNames = new LinkedHashSet<>(argumentCount);
        TypeConverter typeConverter = beanFactory.getTypeConverter();
        for (int i = 0; i < argumentCount; i++) {
            MethodParameter parameter = new MethodParameter(method, i);
            DependencyDescriptor descriptor = new DependencyDescriptor(parameter, this.required);
            descriptor.setContainingClass(beanClass);
            String shortcut = (this.shortcuts != null ? this.shortcuts[i] : null);
            if (shortcut != null) {
                descriptor = new ShortcutDependencyDescriptor(descriptor, shortcut);
            }
            try {
                Object argument = autowireCapableBeanFactory.resolveDependency(descriptor, beanName, autowiredBeanNames, typeConverter);
                if (argument == null && !this.required) {
                    return null;
                }
                arguments[i] = argument;
            } catch (BeansException ex) {
                throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(parameter), ex);
            }
        }
        registerDependentBeans(beanFactory, beanName, autowiredBeanNames);
        return AutowiredArguments.of(arguments);
    }

    private Method getMethod(RegisteredBean registeredBean) {
        Method method = ReflectionUtils.findMethod(registeredBean.getBeanClass(), this.methodName, this.parameterTypes);
        Assert.notNull(method, () -> "Method '%s' with parameter types [%s] declared on %s could not be found.".formatted(this.methodName, toCommaSeparatedNames(this.parameterTypes), registeredBean.getBeanClass().getName()));
        return method;
    }

    private String toCommaSeparatedNames(Class<?>... parameterTypes) {
        return Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(", "));
    }
}
