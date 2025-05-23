// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为还是适销性。
有关许可的具体语言、权限和限制，请参阅许可证。*/
package org.springframework.aop.target.dynamic;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

/**
 * 可刷新的目标源，从BeanFactory中获取新的目标Bean。
 *
 * <p>可以继承该类以覆盖`requiresRefresh()`方法，以抑制不必要的刷新。默认情况下，每次“refreshCheckDelay”时间到达时都会执行刷新。
 *
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory
 * @see #requiresRefresh()
 * @see #setRefreshCheckDelay
 */
public class BeanFactoryRefreshableTargetSource extends AbstractRefreshableTargetSource {

    private final BeanFactory beanFactory;

    private final String beanName;

    /**
     * 为给定的 BeanFactory 和 bean 名称创建一个新的 BeanFactoryRefreshableTargetSource。
     * <p>注意，传入的 BeanFactory 应该为给定的 bean 名称设置合适的 bean 定义。
     * @param beanFactory 要从中获取 beans 的 BeanFactory
     * @param beanName 目标 bean 的名称
     */
    public BeanFactoryRefreshableTargetSource(BeanFactory beanFactory, String beanName) {
        Assert.notNull(beanFactory, "BeanFactory is required");
        Assert.notNull(beanName, "Bean name is required");
        this.beanFactory = beanFactory;
        this.beanName = beanName;
    }

    /**
     * 获取一个新鲜的目标对象。
     */
    @Override
    protected final Object freshTarget() {
        return obtainFreshBean(this.beanFactory, this.beanName);
    }

    /**
     * 这是一个模板方法，子类可以重写它以提供给定的bean工厂和bean名称的新目标对象。
     * <p>默认实现从bean工厂中获取一个新的目标bean实例。
     * @see org.springframework.beans.factory.BeanFactory#getBean
     */
    protected Object obtainFreshBean(BeanFactory beanFactory, String beanName) {
        return beanFactory.getBean(beanName);
    }
}
