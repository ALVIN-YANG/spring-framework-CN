// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可；
除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵犯知识产权。
有关许可证的具体语言、权限和限制，请参阅许可证。*/
package org.springframework.aop.framework;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.Assert;

/**
 * 代理工厂的基类。
 * 提供了对可配置的AopProxyFactory的便捷访问。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see #createAopProxy()
 */
@SuppressWarnings("serial")
public class ProxyCreatorSupport extends AdvisedSupport {

    private AopProxyFactory aopProxyFactory;

    private final List<AdvisedSupportListener> listeners = new ArrayList<>();

    /**
     * 当第一个 AOP 代理被创建时设置为 true。
     */
    private boolean active = false;

    /**
     * 创建一个新的 ProxyCreatorSupport 实例。
     */
    public ProxyCreatorSupport() {
        this.aopProxyFactory = DefaultAopProxyFactory.INSTANCE;
    }

    /**
     * 创建一个新的 ProxyCreatorSupport 实例。
     * @param aopProxyFactory 要使用的 AopProxyFactory 对象
     */
    public ProxyCreatorSupport(AopProxyFactory aopProxyFactory) {
        Assert.notNull(aopProxyFactory, "AopProxyFactory must not be null");
        this.aopProxyFactory = aopProxyFactory;
    }

    /**
     * 自定义 AopProxyFactory，允许在不更改核心框架的情况下，插入不同的策略。
     * <p>默认为 {@link DefaultAopProxyFactory}，根据需求使用动态 JDK 代理或 CGLIB 代理。
     */
    public void setAopProxyFactory(AopProxyFactory aopProxyFactory) {
        Assert.notNull(aopProxyFactory, "AopProxyFactory must not be null");
        this.aopProxyFactory = aopProxyFactory;
    }

    /**
     * 返回此ProxyConfig使用的AopProxyFactory。
     */
    public AopProxyFactory getAopProxyFactory() {
        return this.aopProxyFactory;
    }

    /**
     * 将给定的AdvisedSupportListener添加到这个代理配置中。
     * @param listener 要注册的监听器
     */
    public void addListener(AdvisedSupportListener listener) {
        Assert.notNull(listener, "AdvisedSupportListener must not be null");
        this.listeners.add(listener);
    }

    /**
     * 从当前代理配置中移除指定的AdvisedSupportListener。
     * @param listener 要移除的监听器
     */
    public void removeListener(AdvisedSupportListener listener) {
        Assert.notNull(listener, "AdvisedSupportListener must not be null");
        this.listeners.remove(listener);
    }

    /**
     * 子类应调用此方法来获取一个新的 AOP 代理。它们<b>不应</b>使用{@code this}作为参数来创建 AOP 代理。
     */
    protected final synchronized AopProxy createAopProxy() {
        if (!this.active) {
            activate();
        }
        return getAopProxyFactory().createAopProxy(this);
    }

    /**
     * 激活此代理配置。
     * @see AdvisedSupportListener#activated
     */
    private void activate() {
        this.active = true;
        for (AdvisedSupportListener listener : this.listeners) {
            listener.activated(this);
        }
    }

    /**
     * 将建议更改事件传播给所有AdvisedSupportListeners。
     * @see AdvisedSupportListener#adviceChanged
     */
    @Override
    protected void adviceChanged() {
        super.adviceChanged();
        synchronized (this) {
            if (this.active) {
                for (AdvisedSupportListener listener : this.listeners) {
                    listener.adviceChanged(this);
                }
            }
        }
    }

    /**
     * 子类可以调用此方法以检查是否已经创建了任何 AOP 代理。
     */
    protected final synchronized boolean isActive() {
        return this.active;
    }
}
