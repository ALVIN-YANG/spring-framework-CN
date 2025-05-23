// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.aop.target.dynamic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.lang.Nullable;

/**
 * 抽象实现类 {@link org.springframework.aop.TargetSource}，它包装了一个可刷新的目标对象。子类可以确定是否需要刷新，并且需要提供新的目标对象。
 *
 * 实现了 {@link Refreshable} 接口，以便能够显式控制刷新状态。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see #requiresRefresh()
 * @see #freshTarget()
 */
public abstract class AbstractRefreshableTargetSource implements TargetSource, Refreshable {

    /**
     * 可被子类使用的日志记录器。
     */
    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    protected Object targetObject;

    private long refreshCheckDelay = -1;

    private long lastRefreshCheck = -1;

    private long lastRefreshTime = -1;

    private long refreshCount = 0;

    /**
     * 设置刷新检查之间的延迟，单位为毫秒。
     * 默认值为-1，表示不进行任何刷新检查。
     * <p>注意，实际刷新只有在
     * {@link #requiresRefresh()} 返回 {@code true} 时才会发生。
     */
    public void setRefreshCheckDelay(long refreshCheckDelay) {
        this.refreshCheckDelay = refreshCheckDelay;
    }

    @Override
    public synchronized Class<?> getTargetClass() {
        if (this.targetObject == null) {
            refresh();
        }
        return this.targetObject.getClass();
    }

    /**
     * 非静态。
     */
    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    @Nullable
    public final synchronized Object getTarget() {
        if ((refreshCheckDelayElapsed() && requiresRefresh()) || this.targetObject == null) {
            refresh();
        }
        return this.targetObject;
    }

    /**
     * 无需释放目标。
     */
    @Override
    public void releaseTarget(Object object) {
    }

    @Override
    public final synchronized void refresh() {
        logger.debug("Attempting to refresh target");
        this.targetObject = freshTarget();
        this.refreshCount++;
        this.lastRefreshTime = System.currentTimeMillis();
        logger.debug("Target refreshed successfully");
    }

    @Override
    public synchronized long getRefreshCount() {
        return this.refreshCount;
    }

    @Override
    public synchronized long getLastRefreshTime() {
        return this.lastRefreshTime;
    }

    private boolean refreshCheckDelayElapsed() {
        if (this.refreshCheckDelay < 0) {
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (this.lastRefreshCheck < 0 || currentTimeMillis - this.lastRefreshCheck > this.refreshCheckDelay) {
            // 即将执行刷新检查 - 更新时间戳。
            this.lastRefreshCheck = currentTimeMillis;
            logger.debug("Refresh check delay elapsed - checking whether refresh is required");
            return true;
        }
        return false;
    }

    /**
     * 判断是否需要刷新。
     * 在刷新检查延迟时间经过后，每次调用刷新检查时都会被调用。
     * <p>默认实现始终返回 {@code true}，每次延迟时间经过时都会触发刷新。由子类覆盖以执行对底层目标资源的适当检查。
     * @return 是否需要刷新
     */
    protected boolean requiresRefresh() {
        return true;
    }

    /**
     * 获取一个全新的目标对象。
     * <p>仅在刷新检查发现需要刷新时调用（即，`@link #requiresRefresh()` 返回了 `true`）。
     * @return 新鲜的目标对象
     */
    protected abstract Object freshTarget();
}
