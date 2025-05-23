// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.aop.target;

import java.util.HashSet;
import java.util.Set;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.NamedThreadLocal;

/**
 * 对象池的替代方案。这个 {@link org.springframework.aop.TargetSource}
 * 使用一个线程模型，其中每个线程都有自己的目标副本。
 * 目标之间没有竞争。在运行的服务器上，目标对象的创建被保持在最低限度。
 *
 * <p>应用程序代码按正常池的方式编写；调用者不能假设在不同线程的调用中会处理相同的实例。
 * 然而，可以在单个线程的操作期间依赖状态：例如，如果一个调用者对AOP代理进行了重复调用。
 *
 * <p>线程绑定对象的清理在BeanFactory销毁时执行，如果可用，将调用它们的{@code DisposableBean.destroy()}方法。
 * 请注意，许多线程绑定对象可以存在，直到应用程序实际关闭。
 */
@SuppressWarnings("serial")
public class ThreadLocalTargetSource extends AbstractPrototypeBasedTargetSource implements ThreadLocalTargetSourceStats, DisposableBean {

    /**
     * ThreadLocal 存储与当前线程关联的目标对象。与大多数静态的 ThreadLocal 不同，这个变量旨在每个线程每个 ThreadLocalTargetSource 类的实例中独立存在。
     */
    private final ThreadLocal<Object> targetInThread = new NamedThreadLocal<>("Thread-local instance of bean") {

        @Override
        public String toString() {
            return super.toString() + " '" + getTargetBeanName() + "'";
        }
    };

    /**
     * 管理目标的集合，使我们能够跟踪我们所创建的目标。
     */
    private final Set<Object> targetSet = new HashSet<>();

    private int invocationCount;

    private int hitCount;

    /**
     * 抽象getTarget()方法的实现。
     * 我们寻找一个存储在ThreadLocal中的目标。如果我们找不到一个目标，
     * 我们将创建一个并绑定到当前线程。不需要同步。
     */
    @Override
    public Object getTarget() throws BeansException {
        ++this.invocationCount;
        Object target = this.targetInThread.get();
        if (target == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No target for prototype '" + getTargetBeanName() + "' bound to thread: " + "creating one and binding it to thread '" + Thread.currentThread().getName() + "'");
            }
            // 将目标与ThreadLocal关联。
            target = newPrototypeInstance();
            this.targetInThread.set(target);
            synchronized (this.targetSet) {
                this.targetSet.add(target);
            }
        } else {
            ++this.hitCount;
        }
        return target;
    }

    /**
     * 如有必要，销毁目标；清除ThreadLocal。
     * @see #destroyPrototypeInstance
     */
    @Override
    public void destroy() {
        logger.debug("Destroying ThreadLocalTargetSource bindings");
        synchronized (this.targetSet) {
            for (Object target : this.targetSet) {
                destroyPrototypeInstance(target);
            }
            this.targetSet.clear();
        }
        // 清除ThreadLocal，以防万一。
        this.targetInThread.remove();
    }

    @Override
    public int getInvocationCount() {
        return this.invocationCount;
    }

    @Override
    public int getHitCount() {
        return this.hitCount;
    }

    @Override
    public int getObjectCount() {
        synchronized (this.targetSet) {
            return this.targetSet.size();
        }
    }

    /**
     * 返回一个介绍型顾问混合类，该类允许 AOP 代理被转换为 ThreadLocalInvokerStats。
     */
    public IntroductionAdvisor getStatsMixin() {
        DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
        return new DefaultIntroductionAdvisor(dii, ThreadLocalTargetSourceStats.class);
    }
}
