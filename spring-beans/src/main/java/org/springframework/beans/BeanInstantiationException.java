// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.springframework.lang.Nullable;

/**
 * 当实例化 Bean 失败时抛出的异常。
 * 包含了导致问题的 Bean 类。
 *
 * @author Juergen Hoeller
 * @since 1.2.8
 */
@SuppressWarnings("serial")
public class BeanInstantiationException extends FatalBeanException {

    private final Class<?> beanClass;

    @Nullable
    private final Constructor<?> constructor;

    @Nullable
    private final Method constructingMethod;

    /**
     * 创建一个新的BeanInstantiationException。
     * @param beanClass 出错的bean类
     * @param msg 详细信息
     */
    public BeanInstantiationException(Class<?> beanClass, String msg) {
        this(beanClass, msg, null);
    }

    /**
     * 创建一个新的BeanInstantiationException。
     * @param beanClass 导致问题的Bean类
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public BeanInstantiationException(Class<?> beanClass, String msg, @Nullable Throwable cause) {
        super("Failed to instantiate [" + beanClass.getName() + "]: " + msg, cause);
        this.beanClass = beanClass;
        this.constructor = null;
        this.constructingMethod = null;
    }

    /**
     * 创建一个新的 BeanInstantiationException。
     * @param constructor 违法的构造函数
     * @param msg 详细信息
     * @param cause 根本原因
     * @since 4.3
     */
    public BeanInstantiationException(Constructor<?> constructor, String msg, @Nullable Throwable cause) {
        super("Failed to instantiate [" + constructor.getDeclaringClass().getName() + "]: " + msg, cause);
        this.beanClass = constructor.getDeclaringClass();
        this.constructor = constructor;
        this.constructingMethod = null;
    }

    /**
     * 创建一个新的 BeanInstantiationException 对象。
     * @param constructingMethod 用于构建 Bean 的代理方法（通常，但不一定是静态工厂方法）
     * @param msg 详细消息
     * @param cause 根本原因
     * @since 4.3
     */
    public BeanInstantiationException(Method constructingMethod, String msg, @Nullable Throwable cause) {
        super("Failed to instantiate [" + constructingMethod.getReturnType().getName() + "]: " + msg, cause);
        this.beanClass = constructingMethod.getReturnType();
        this.constructor = null;
        this.constructingMethod = constructingMethod;
    }

    /**
     * 返回有问题的 bean 类（绝不返回 {@code null}）。
     * @return 要实例化的类
     */
    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    /**
     * 返回已知的违规构造函数。
     * @return 正在使用的构造函数，或在工厂方法或默认实例化的情况下返回 {@code null}
     * @since 4.3
     */
    @Nullable
    public Constructor<?> getConstructor() {
        return this.constructor;
    }

    /**
     * 返回用于构建 Bean 的代理，如果已知。
     * @return 正在使用的方 法（通常是静态工厂方法），
     * 或在基于构造函数的实例化情况下返回 {@code null}
     * @since 4.3
     */
    @Nullable
    public Method getConstructingMethod() {
        return this.constructingMethod;
    }
}
