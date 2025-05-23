// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.aop.aspectj;

import java.io.Serializable;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 实现了`AspectInstanceFactory`接口，该实现依赖于一个指定的单例对象，对于每个`#getAspectInstance()`调用都返回相同的实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleAspectInstanceFactory
 */
@SuppressWarnings("serial")
public class SingletonAspectInstanceFactory implements AspectInstanceFactory, Serializable {

    private final Object aspectInstance;

    /**
     * 为给定的方面实例创建一个新的 SingletonAspectInstanceFactory。
     * @param aspectInstance 单例方面实例
     */
    public SingletonAspectInstanceFactory(Object aspectInstance) {
        Assert.notNull(aspectInstance, "Aspect instance must not be null");
        this.aspectInstance = aspectInstance;
    }

    @Override
    public final Object getAspectInstance() {
        return this.aspectInstance;
    }

    @Override
    @Nullable
    public ClassLoader getAspectClassLoader() {
        return this.aspectInstance.getClass().getClassLoader();
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
        if (this.aspectInstance instanceof Ordered ordered) {
            return ordered.getOrder();
        }
        return getOrderForAspectClass(this.aspectInstance.getClass());
    }

    /**
     * 确定一个回退顺序，以防方面实例没有通过实现 {@link org.springframework.core.Ordered} 接口来表示实例特定的顺序。
     * <p>默认实现简单地返回 {@code Ordered.LOWEST_PRECEDENCE}。
     * @param aspectClass 方面类
     */
    protected int getOrderForAspectClass(Class<?> aspectClass) {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
