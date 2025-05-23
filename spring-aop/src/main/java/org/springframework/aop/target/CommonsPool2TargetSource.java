// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.target;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 实现 `org.springframework.aop.TargetSource` 的类，该类使用可配置的 Apache Commons2 Pool 来保存对象。
 *
 * <p>默认情况下，将创建一个 `GenericObjectPool` 实例。子类可以通过覆盖 `createObjectPool()` 方法来更改使用的 `ObjectPool` 类型。
 *
 * <p>提供了许多配置属性，这些属性与 Commons Pool 的 `GenericObjectPool` 类的属性相对应；这些属性在构造时传递给 `GenericObjectPool`。如果创建该类的子类以更改 `ObjectPool` 实现类型，请传入与所选实现相关的配置属性值。
 *
 * <p>`testOnBorrow`、`testOnReturn` 和 `testWhileIdle` 属性明确不进行镜像，因为此类使用的 `PoolableObjectFactory` 实现不执行有意义的验证。所有公开的 Commons Pool 属性都使用相应的 Commons Pool 默认值。
 *
 * <p>与 Apache Commons Pool 2.4 兼容，自 Spring 4.2 版本起。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Kazuki Shimizu
 * @since 4.2
 * @see GenericObjectPool
 * @see #createObjectPool()
 * @see #setMaxSize
 * @see #setMaxIdle
 * @see #setMinIdle
 * @see #setMaxWait
 * @see #setTimeBetweenEvictionRunsMillis
 * @see #setMinEvictableIdleTimeMillis
 */
@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class CommonsPool2TargetSource extends AbstractPoolingTargetSource implements PooledObjectFactory<Object> {

    private int maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;

    private int minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

    private long maxWait = GenericObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;

    private long timeBetweenEvictionRunsMillis = GenericObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;

    private long minEvictableIdleTimeMillis = GenericObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

    private boolean blockWhenExhausted = GenericObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED;

    /**
     * Apache Commons 的 {@code ObjectPool} 用于对象池管理目标对象。
     */
    @Nullable
    private ObjectPool pool;

    /**
     * 创建一个具有默认设置的 CommonsPoolTargetSource。
     * 默认的最大池大小为 8。
     * @see #setMaxSize
     * @see GenericObjectPoolConfig#setMaxTotal
     */
    public CommonsPool2TargetSource() {
        setMaxSize(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL);
    }

    /**
     * 设置池中最大空闲对象的数量。
     * 默认值为8。
     * @see GenericObjectPool#setMaxIdle
     */
    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    /**
     * 返回池中空闲对象的最大数量。
     */
    public int getMaxIdle() {
        return this.maxIdle;
    }

    /**
     * 设置池中空闲对象的最小数量。
     * 默认值为0。
     * @see GenericObjectPool#setMinIdle
     */
    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    /**
     * 返回池中空闲对象的最小数量。
     */
    public int getMinIdle() {
        return this.minIdle;
    }

    /**
     * 设置从池中获取对象的最大等待时间。
     * 默认值为 -1，表示无限期等待。
     * @see GenericObjectPool#setMaxWaitMillis
     */
    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    /**
     * 返回从池中获取对象的最大等待时间。
     */
    public long getMaxWait() {
        return this.maxWait;
    }

    /**
     * 设置空闲对象检查的驱逐运行之间的时间，检查这些对象是否空闲时间过长或已失效。
     * 默认值为 -1，不执行任何驱逐操作。
     * @see GenericObjectPool#setTimeBetweenEvictionRunsMillis
     */
    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    /**
     * 返回在检查空闲对象的驱逐运行之间的时间。
     */
    public long getTimeBetweenEvictionRunsMillis() {
        return this.timeBetweenEvictionRunsMillis;
    }

    /**
     * 设置一个空闲对象在池中驻留的最短时间，在此时间之后它将受到驱逐。默认值为1800000（30分钟）。
     * <p>注意，需要执行驱逐运行来使此设置生效。
     * @see #setTimeBetweenEvictionRunsMillis
     * @see GenericObjectPool#setMinEvictableIdleTimeMillis
     */
    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    /**
     * 返回空闲对象可以在池中驻留的最短时间。
     */
    public long getMinEvictableIdleTimeMillis() {
        return this.minEvictableIdleTimeMillis;
    }

    /**
     * 设置当连接池耗尽时，调用是否应该阻塞。
     */
    public void setBlockWhenExhausted(boolean blockWhenExhausted) {
        this.blockWhenExhausted = blockWhenExhausted;
    }

    /**
     * 指定当连接池耗尽时，调用是否应该阻塞。
     */
    public boolean isBlockWhenExhausted() {
        return this.blockWhenExhausted;
    }

    /**
     * 创建并持有 ObjectPool 实例。
     * @see #createObjectPool()
     */
    @Override
    protected final void createPool() {
        logger.debug("Creating Commons object pool");
        this.pool = createObjectPool();
    }

    /**
     * 子类可以覆盖此方法以返回特定的 Commons 池。
     * 它们应该在这里将任何配置属性应用到池上。
     * <p>默认情况下是一个具有给定池大小的 GenericObjectPool 实例。
     * @return 一个空的 Commons {@code ObjectPool}。
     * @see GenericObjectPool
     * @see #setMaxSize
     */
    protected ObjectPool createObjectPool() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(getMaxSize());
        config.setMaxIdle(getMaxIdle());
        config.setMinIdle(getMinIdle());
        config.setMaxWaitMillis(getMaxWait());
        config.setTimeBetweenEvictionRunsMillis(getTimeBetweenEvictionRunsMillis());
        config.setMinEvictableIdleTimeMillis(getMinEvictableIdleTimeMillis());
        config.setBlockWhenExhausted(isBlockWhenExhausted());
        return new GenericObjectPool(this, config);
    }

    /**
     * 从 {@code ObjectPool} 中借用一个对象。
     */
    @Override
    public Object getTarget() throws Exception {
        Assert.state(this.pool != null, "No Commons ObjectPool available");
        return this.pool.borrowObject();
    }

    /**
     * 将指定的对象返回到底层的 {@code ObjectPool}。
     */
    @Override
    public void releaseTarget(Object target) throws Exception {
        if (this.pool != null) {
            this.pool.returnObject(target);
        }
    }

    @Override
    public int getActiveCount() throws UnsupportedOperationException {
        return (this.pool != null ? this.pool.getNumActive() : 0);
    }

    @Override
    public int getIdleCount() throws UnsupportedOperationException {
        return (this.pool != null ? this.pool.getNumIdle() : 0);
    }

    /**
     * 当销毁此对象时关闭底层的 {@code ObjectPool}。
     */
    @Override
    public void destroy() throws Exception {
        if (this.pool != null) {
            logger.debug("Closing Commons ObjectPool");
            this.pool.close();
        }
    }

    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您翻译成中文。
    // 实现 org.apache.commons.pool2.PooledObjectFactory 接口
    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供您希望翻译的 Java 代码注释，我将为您翻译成中文。
    @Override
    public PooledObject<Object> makeObject() throws Exception {
        return new DefaultPooledObject<>(newPrototypeInstance());
    }

    @Override
    public void destroyObject(PooledObject<Object> p) throws Exception {
        destroyPrototypeInstance(p.getObject());
    }

    @Override
    public boolean validateObject(PooledObject<Object> p) {
        return true;
    }

    @Override
    public void activateObject(PooledObject<Object> p) throws Exception {
    }

    @Override
    public void passivateObject(PooledObject<Object> p) throws Exception {
    }
}
