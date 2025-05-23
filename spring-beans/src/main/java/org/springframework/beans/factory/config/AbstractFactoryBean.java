// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可协议”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可协议副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可协议。*/
package org.springframework.beans.factory.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 简单的模板超类，用于实现依赖于标志位创建单例或原型对象的 {@link FactoryBean}。
 *
 * <p>如果“singleton”标志位为 {@code true}（默认值），
 * 此类将在初始化时创建对象一次，并在后续所有调用 {@link #getObject()} 方法时返回该单例实例。
 *
 * <p>否则，每次调用 {@link #getObject()} 方法时，此类都将创建一个新实例。子类负责实现抽象的 {@link #createInstance()} 模板方法来实际创建要公开的对象。
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @since 1.0.2
 * @param <T> bean 类型
 * @see #setSingleton
 * @see #createInstance()
 */
public abstract class AbstractFactoryBean<T> implements FactoryBean<T>, BeanClassLoaderAware, BeanFactoryAware, InitializingBean, DisposableBean {

    /**
     * 日志记录器可供子类使用。
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private boolean singleton = true;

    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    @Nullable
    private BeanFactory beanFactory;

    private boolean initialized = false;

    @Nullable
    private T singletonInstance;

    @Nullable
    private T earlySingletonInstance;

    /**
     * 设置是否应该创建单例，还是每次请求时创建一个新对象。默认为 {@code true}（单例）。
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public boolean isSingleton() {
        return this.singleton;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public void setBeanFactory(@Nullable BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 返回此 Bean 运行的 BeanFactory。
     */
    @Nullable
    protected BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    /**
     * 从运行此Bean的BeanFactory中获取一个Bean类型转换器。这通常是对每次调用都是一个新的实例，
     * 因为类型转换器通常<i>不是</i>线程安全的。
     * <p>当不在BeanFactory中运行时，回退到SimpleTypeConverter。
     * @see ConfigurableBeanFactory#getTypeConverter()
     * @see org.springframework.beans.SimpleTypeConverter
     */
    protected TypeConverter getBeanTypeConverter() {
        BeanFactory beanFactory = getBeanFactory();
        if (beanFactory instanceof ConfigurableBeanFactory cbf) {
            return cbf.getTypeConverter();
        } else {
            return new SimpleTypeConverter();
        }
    }

    /**
     * 如果需要，则积极创建单例实例。
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (isSingleton()) {
            this.initialized = true;
            this.singletonInstance = createInstance();
            this.earlySingletonInstance = null;
        }
    }

    /**
     * 暴露单例实例或创建一个新的原型实例。
     * @see #createInstance()
     * @see #getEarlySingletonInterfaces()
     */
    @Override
    public final T getObject() throws Exception {
        if (isSingleton()) {
            return (this.initialized ? this.singletonInstance : getEarlySingletonInstance());
        } else {
            return createInstance();
        }
    }

    /**
     * 确定一个“早期单例”实例，在出现循环引用的情况下暴露出来。在非循环引用场景下不调用。
     */
    @SuppressWarnings("unchecked")
    private T getEarlySingletonInstance() throws Exception {
        Class<?>[] ifcs = getEarlySingletonInterfaces();
        if (ifcs == null) {
            throw new FactoryBeanNotInitializedException(getClass().getName() + " does not support circular references");
        }
        if (this.earlySingletonInstance == null) {
            this.earlySingletonInstance = (T) Proxy.newProxyInstance(this.beanClassLoader, ifcs, new EarlySingletonInvocationHandler());
        }
        return this.earlySingletonInstance;
    }

    /**
     * 暴露单例实例（通过“早期单例”代理进行访问）。
     * @return 由该FactoryBean持有的单例实例
     * @throws IllegalStateException 如果单例实例未初始化
     */
    @Nullable
    private T getSingletonInstance() throws IllegalStateException {
        Assert.state(this.initialized, "Singleton instance not initialized yet");
        return this.singletonInstance;
    }

    /**
     * 销毁单例实例（如果存在）。
     * @see #destroyInstance(Object)
     */
    @Override
    public void destroy() throws Exception {
        if (isSingleton()) {
            destroyInstance(this.singletonInstance);
        }
    }

    /**
     * 这个抽象方法声明与FactoryBean接口中的方法相对应，旨在提供一致的抽象模板方法。
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    @Nullable
    public abstract Class<?> getObjectType();

    /**
     * 模板方法，子类必须重写以构建
     * 此工厂返回的对象。
     * <p>在单例情况下，在初始化此FactoryBean时调用；否则，在每次调用{@link #getObject()}时调用。
     * @return 此工厂返回的对象
     * @throws Exception 如果在对象创建过程中发生异常
     * @see #getObject()
     */
    protected abstract T createInstance() throws Exception;

    /**
     * 返回一个接口数组，该数组由该FactoryBean暴露的单例对象应该实现，用于与可能因循环引用而暴露的'早期单例代理'一起使用。
     * <p>默认实现返回此FactoryBean的对象类型，前提是它是一个接口，否则返回null。后一种情况表示此FactoryBean不支持早期单例访问。
     * 这将导致抛出FactoryBeanNotInitializedException异常。
     * @return 用于'早期单例'的接口数组，或返回null以指示抛出FactoryBeanNotInitializedException异常
     * @see org.springframework.beans.factory.FactoryBeanNotInitializedException
     */
    @Nullable
    protected Class<?>[] getEarlySingletonInterfaces() {
        Class<?> type = getObjectType();
        return (type != null && type.isInterface() ? new Class<?>[] { type } : null);
    }

    /**
     * 销毁单例实例的回调。子类可以重写此方法以销毁先前创建的实例。
     * <p>默认实现为空。
     * @param instance 由 {@link #createInstance()} 返回的单例实例
     * @throws Exception 在关闭时发生错误的情况下抛出
     * @see #createInstance()
     */
    protected void destroyInstance(@Nullable T instance) throws Exception {
    }

    /**
     * 用于对实际单例对象进行懒加载访问的反射调用处理器。
     */
    private class EarlySingletonInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (ReflectionUtils.isEqualsMethod(method)) {
                // 仅当代理完全相同才考虑相等。
                return (proxy == args[0]);
            } else if (ReflectionUtils.isHashCodeMethod(method)) {
                // 使用引用代理的hashCode。
                return System.identityHashCode(proxy);
            } else if (!initialized && ReflectionUtils.isToStringMethod(method)) {
                return "Early singleton proxy for interfaces " + ObjectUtils.nullSafeToString(getEarlySingletonInterfaces());
            }
            try {
                return method.invoke(getSingletonInstance(), args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
}
