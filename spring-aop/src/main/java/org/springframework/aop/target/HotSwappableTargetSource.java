// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可；除非符合许可证规定，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“现状”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.target;

import java.io.Serializable;
import org.springframework.aop.TargetSource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.aop.TargetSource} 的实现，它缓存一个本地目标对象，但允许在应用程序运行期间更换目标对象。
 *
 * <p>如果在一个 Spring IoC 容器中配置此类的对象，请使用构造函数注入。
 *
 * <p>如果目标对象在序列化时存在，此 TargetSource 可序列化。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class HotSwappableTargetSource implements TargetSource, Serializable {

    /**
     * 使用 Spring 1.2 中的 serialVersionUID 以确保互操作性。
     */
    private static final long serialVersionUID = 7497929212653839187L;

    /**
     * 当前目标对象。
     */
    @SuppressWarnings("serial")
    private Object target;

    /**
     * 创建一个新的可热插拔的目标源（HotSwappableTargetSource），并使用给定的初始目标对象。
     * @param initialTarget 初始目标对象
     */
    public HotSwappableTargetSource(Object initialTarget) {
        Assert.notNull(initialTarget, "Target object must not be null");
        this.target = initialTarget;
    }

    /**
     * 返回当前目标对象的类型。
     * <p>返回的类型通常应该在所有目标对象中保持不变。
     */
    @Override
    public synchronized Class<?> getTargetClass() {
        return this.target.getClass();
    }

    @Override
    public final boolean isStatic() {
        return false;
    }

    @Override
    public synchronized Object getTarget() {
        return this.target;
    }

    @Override
    public void releaseTarget(Object target) {
        // 没有要执行的操作
    }

    /**
     * 交换目标对象，并返回旧的目标对象。
     * @param newTarget 新的目标对象
     * @return 返回旧的目标对象
     * @throws IllegalArgumentException 如果新目标对象无效，则抛出此异常
     */
    public synchronized Object swap(Object newTarget) throws IllegalArgumentException {
        Assert.notNull(newTarget, "Target object must not be null");
        Object old = this.target;
        this.target = newTarget;
        return old;
    }

    /**
     * 两个 HotSwappableTargetSources 相等，当且仅当它们的当前目标对象相等。
     */
    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof HotSwappableTargetSource that && this.target.equals(that.target)));
    }

    @Override
    public int hashCode() {
        return HotSwappableTargetSource.class.hashCode();
    }

    @Override
    public String toString() {
        return "HotSwappableTargetSource for target: " + this.target;
    }
}
