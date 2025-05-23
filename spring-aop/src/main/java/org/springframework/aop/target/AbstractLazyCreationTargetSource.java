// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache 许可证 2.0 版（"许可证"），除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体语言管辖的权限和限制。*/
package org.springframework.aop.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.lang.Nullable;

/**
 * {@link org.springframework.aop.TargetSource} 的实现，该实现将延迟创建用户管理的对象。
 *
 * <p>懒加载目标对象的创建由用户通过实现 {@link #createObject()} 方法来控制。此 {@code TargetSource} 将在第一次访问代理时调用此方法。
 *
 * <p>当您需要将某个依赖项的引用传递给对象，但实际上您不希望该依赖项在首次使用之前就被创建时，这非常有用。这种情况的一个典型场景是远程资源的连接。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2.4
 * @see #isInitialized()
 * @see #createObject()
 */
public abstract class AbstractLazyCreationTargetSource implements TargetSource {

    /**
     * 日志记录器可供子类使用。
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * 延迟初始化的目标对象。
     */
    @Nullable
    private Object lazyTarget;

    /**
     * 返回此TargetSource的懒加载目标对象是否已经被获取。
     */
    public synchronized boolean isInitialized() {
        return (this.lazyTarget != null);
    }

    /**
     * 这个默认实现如果目标为null（尚未初始化），则返回 {@code null}，或者如果目标已经初始化，则返回目标类。
     * <p>子类可能希望覆盖此方法，以便在目标仍然为 {@code null} 时提供有意义的值。
     * @see #isInitialized()
     */
    @Override
    @Nullable
    public synchronized Class<?> getTargetClass() {
        return (this.lazyTarget != null ? this.lazyTarget.getClass() : null);
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    /**
     * 返回懒加载的初始化目标对象，
     * 如果它不存在，则在运行时创建它。
     * @see #createObject()
     */
    @Override
    public synchronized Object getTarget() throws Exception {
        if (this.lazyTarget == null) {
            logger.debug("Initializing lazy target object");
            this.lazyTarget = createObject();
        }
        return this.lazyTarget;
    }

    @Override
    public void releaseTarget(Object target) throws Exception {
        // 没有要执行的操作
    }

    /**
     * 子类应实现此方法以返回延迟初始化的对象。
     * 在第一次调用代理时被调用。
     * @return 创建的对象
     * @throws Exception 如果创建失败时抛出异常
     */
    protected abstract Object createObject() throws Exception;
}
