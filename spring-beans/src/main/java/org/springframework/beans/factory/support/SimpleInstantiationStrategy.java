// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据Apache许可证版本2.0（以下简称“许可证”）授权；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”提供的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 简单的对象实例化策略，用于BeanFactory。
 *
 * <p>不支持方法注入，尽管它提供了钩子供子类覆盖以添加方法注入支持，例如通过重写方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public class SimpleInstantiationStrategy implements InstantiationStrategy {

    private static final ThreadLocal<Method> currentlyInvokedFactoryMethod = new ThreadLocal<>();

    /**
     * 返回当前正在调用的工厂方法或 {@code null}（如果没有）。
     * <p>允许工厂方法实现确定当前调用者是否是容器本身，而不是用户代码。
     */
    @Nullable
    public static Method getCurrentlyInvokedFactoryMethod() {
        return currentlyInvokedFactoryMethod.get();
    }

    /**
     * 设置当前正在调用的工厂方法或 {@code null} 以移除当前的值（如果有的话）。
     * @param method 当前正在调用的工厂方法或 {@code null}
     * @since 6.0
     */
    public static void setCurrentlyInvokedFactoryMethod(@Nullable Method method) {
        if (method != null) {
            currentlyInvokedFactoryMethod.set(method);
        } else {
            currentlyInvokedFactoryMethod.remove();
        }
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
        // 如果没有重写方法，请不要使用CGLIB来覆盖这个类。
        if (!bd.hasMethodOverrides()) {
            Constructor<?> constructorToUse;
            synchronized (bd.constructorArgumentLock) {
                constructorToUse = (Constructor<?>) bd.resolvedConstructorOrFactoryMethod;
                if (constructorToUse == null) {
                    Class<?> clazz = bd.getBeanClass();
                    if (clazz.isInterface()) {
                        throw new BeanInstantiationException(clazz, "Specified class is an interface");
                    }
                    try {
                        constructorToUse = clazz.getDeclaredConstructor();
                        bd.resolvedConstructorOrFactoryMethod = constructorToUse;
                    } catch (Throwable ex) {
                        throw new BeanInstantiationException(clazz, "No default constructor found", ex);
                    }
                }
            }
            return BeanUtils.instantiateClass(constructorToUse);
        } else {
            // 必须生成CGLIB子类。
            return instantiateWithMethodInjection(bd, beanName, owner);
        }
    }

    /**
     * 子类可以重写此方法，如果它们可以使用给定 RootBeanDefinition 中指定的方法注入实例化一个对象，则该方法实现为抛出 UnsupportedOperationException。实例化应使用无参构造函数。
     */
    protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
        throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner, Constructor<?> ctor, Object... args) {
        if (!bd.hasMethodOverrides()) {
            return BeanUtils.instantiateClass(ctor, args);
        } else {
            return instantiateWithMethodInjection(bd, beanName, owner, ctor, args);
        }
    }

    /**
     * 子类可以覆盖此方法，该方法默认实现为抛出`UnsupportedOperationException`，如果它们可以使用在给定的`RootBeanDefinition`中指定的方法注入实例化一个对象。实例化应使用给定的构造函数和参数。
     */
    protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner, @Nullable Constructor<?> ctor, Object... args) {
        throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner, @Nullable Object factoryBean, Method factoryMethod, Object... args) {
        try {
            ReflectionUtils.makeAccessible(factoryMethod);
            Method priorInvokedFactoryMethod = getCurrentlyInvokedFactoryMethod();
            try {
                setCurrentlyInvokedFactoryMethod(factoryMethod);
                Object result = factoryMethod.invoke(factoryBean, args);
                if (result == null) {
                    result = new NullBean();
                }
                return result;
            } finally {
                setCurrentlyInvokedFactoryMethod(priorInvokedFactoryMethod);
            }
        } catch (IllegalArgumentException ex) {
            throw new BeanInstantiationException(factoryMethod, "Illegal arguments to factory method '" + factoryMethod.getName() + "'; " + "args: " + StringUtils.arrayToCommaDelimitedString(args), ex);
        } catch (IllegalAccessException ex) {
            throw new BeanInstantiationException(factoryMethod, "Cannot access factory method '" + factoryMethod.getName() + "'; is it public?", ex);
        } catch (InvocationTargetException ex) {
            String msg = "Factory method '" + factoryMethod.getName() + "' threw exception with message: " + ex.getTargetException().getMessage();
            if (bd.getFactoryBeanName() != null && owner instanceof ConfigurableBeanFactory cbf && cbf.isCurrentlyInCreation(bd.getFactoryBeanName())) {
                msg = "Circular reference involving containing bean '" + bd.getFactoryBeanName() + "' - consider " + "declaring the factory method as static for independence from its containing instance. " + msg;
            }
            throw new BeanInstantiationException(factoryMethod, msg, ex.getTargetException());
        }
    }
}
