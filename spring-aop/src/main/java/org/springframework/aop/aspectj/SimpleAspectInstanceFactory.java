// 翻译完成 glm-4-flash
/*版权所有 2002-2018，原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵权。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj;

import java.lang.reflect.InvocationTargetException;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 实现 {@link AspectInstanceFactory}，为每次调用 {@link #getAspectInstance()} 创建指定切面类的全新实例。
 *
 * @author Juergen Hoeller
 * @since 2.0.4
 */
public class SimpleAspectInstanceFactory implements AspectInstanceFactory {

    private final Class<?> aspectClass;

    /**
     * 为给定的方面类创建一个新的 SimpleAspectInstanceFactory。
     * @param aspectClass 方面类
     */
    public SimpleAspectInstanceFactory(Class<?> aspectClass) {
        Assert.notNull(aspectClass, "Aspect class must not be null");
        this.aspectClass = aspectClass;
    }

    /**
     * 返回指定的方面类（永不返回null）。
     */
    public final Class<?> getAspectClass() {
        return this.aspectClass;
    }

    @Override
    public final Object getAspectInstance() {
        try {
            return ReflectionUtils.accessibleConstructor(this.aspectClass).newInstance();
        } catch (NoSuchMethodException ex) {
            throw new AopConfigException("No default constructor on aspect class: " + this.aspectClass.getName(), ex);
        } catch (InstantiationException ex) {
            throw new AopConfigException("Unable to instantiate aspect class: " + this.aspectClass.getName(), ex);
        } catch (IllegalAccessException ex) {
            throw new AopConfigException("Could not access aspect constructor: " + this.aspectClass.getName(), ex);
        } catch (InvocationTargetException ex) {
            throw new AopConfigException("Failed to invoke aspect constructor: " + this.aspectClass.getName(), ex.getTargetException());
        }
    }

    @Override
    @Nullable
    public ClassLoader getAspectClassLoader() {
        return this.aspectClass.getClassLoader();
    }

    /**
     * 确定此工厂的方面实例的顺序，
     * 可以是通过实现 {@link org.springframework.core.Ordered} 接口来表达的实例特定顺序，
     * 或者是一个回退顺序。
     * @see org.springframework.core.Ordered
     * @see #getOrderForAspectClass
     */
    @Override
    public int getOrder() {
        return getOrderForAspectClass(this.aspectClass);
    }

    /**
     * 确定一个回退顺序，用于当方面实例没有通过实现 {@link org.springframework.core.Ordered} 接口来表示特定实例的顺序时。
     * <p>默认实现简单地返回 {@code Ordered.LOWEST_PRECEDENCE}。
     * @param aspectClass 方面类
     */
    protected int getOrderForAspectClass(Class<?> aspectClass) {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
