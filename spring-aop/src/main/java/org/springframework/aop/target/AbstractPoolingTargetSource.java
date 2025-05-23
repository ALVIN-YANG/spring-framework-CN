// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非法律要求或经书面同意，否则您不得使用此文件。
* 您可以在以下网址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用的法律或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.aop.target;

import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;

/**
 * 用于管理目标实例池的抽象基类，每个方法调用时从池中获取和释放一个目标对象。
 * 此抽象基类独立于具体的池化技术；请参阅子类 {@link CommonsPool2TargetSource} 以获取一个具体示例。
 *
 * <p>子类必须根据所选的对象池实现 {@link #getTarget} 和
 * {@link #releaseTarget} 方法。从 {@link AbstractPrototypeBasedTargetSource} 继承的
 * {@link #newPrototypeInstance()} 方法可用于创建对象，以便将它们放入池中。
 *
 * <p>子类还必须实现来自 {@link PoolingConfig} 接口的一些监控方法。通过
 * {@link #getPoolingConfigMixin()} 方法，这些统计信息可以通过 IntroductionAdvisor
 * 在代理对象上提供。
 *
 * <p>此类实现了 {@link org.springframework.beans.factory.DisposableBean} 接口，以强制子类实现
 * 一个 {@link #destroy()} 方法，关闭其对象池。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getTarget
 * @see #releaseTarget
 * @see #destroy
 */
@SuppressWarnings("serial")
public abstract class AbstractPoolingTargetSource extends AbstractPrototypeBasedTargetSource implements PoolingConfig, DisposableBean {

    /**
     * 池的最大大小。
     */
    private int maxSize = -1;

    /**
     * 设置线程池的最大大小。
     * 默认值为-1，表示没有大小限制。
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 返回池的最大大小。
     */
    @Override
    public int getMaxSize() {
        return this.maxSize;
    }

    @Override
    public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        super.setBeanFactory(beanFactory);
        try {
            createPool();
        } catch (Throwable ex) {
            throw new BeanInitializationException("Could not create instance pool for TargetSource", ex);
        }
    }

    /**
     * 创建池。
     * @throws Exception 抛出异常以避免对池化API施加约束
     */
    protected abstract void createPool() throws Exception;

    /**
     * 从池中获取一个对象。
     * @return 从池中返回一个对象
     * @throws Exception 我们可能需要处理来自池APIs的受检异常，因此我们对异常签名采取了宽容的态度
     */
    @Override
    @Nullable
    public abstract Object getTarget() throws Exception;

    /**
     * 将给定对象返回到池中。
     * @param target 必须是通过调用 {@code getTarget()} 从池中获取的对象
     * @throws Exception 允许池化API抛出异常
     * @see #getTarget
     */
    @Override
    public abstract void releaseTarget(Object target) throws Exception;

    /**
     * 返回一个 IntroductionAdvisor，它提供了一个混合模式，用于暴露由该对象维护的池的统计信息。
     */
    public DefaultIntroductionAdvisor getPoolingConfigMixin() {
        DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
        return new DefaultIntroductionAdvisor(dii, PoolingConfig.class);
    }
}
