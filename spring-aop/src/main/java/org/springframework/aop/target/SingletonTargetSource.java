// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License 2.0（以下简称“许可”）许可，除非法律要求或经书面同意，否则不得使用此文件。
您可以在以下地址获取许可副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非根据适用的法律或书面同意，否则在许可下分发的软件按“现状”分发，不提供任何明示或暗示的保证或条件。
有关许可下授予的权限和限制的具体语言，请参阅许可。*/
package org.springframework.aop.target;

import java.io.Serializable;
import org.springframework.aop.TargetSource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 实现{@link org.springframework.aop.TargetSource}接口，该接口持有给定对象。这是TargetSource接口的默认实现，由Spring AOP框架使用。在应用程序代码中通常没有必要创建此类对象的实例。
 *
 * <p>此类是可序列化的。然而，SingletonTargetSource的可序列化性将取决于目标对象是否可序列化。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.framework.AdvisedSupport#setTarget(Object)
 */
public class SingletonTargetSource implements TargetSource, Serializable {

    /**
     * 使用 Spring 1.2 中的 serialVersionUID 以实现互操作性。
     */
    private static final long serialVersionUID = 9031246629662423738L;

    /**
     * 使用反射缓存并调用目标。
     */
    @SuppressWarnings("serial")
    private final Object target;

    /**
     * 为给定目标创建一个新的 SingletonTargetSource。
     * @param target 目标对象
     */
    public SingletonTargetSource(Object target) {
        Assert.notNull(target, "Target object must not be null");
        this.target = target;
    }

    @Override
    public Class<?> getTargetClass() {
        return this.target.getClass();
    }

    @Override
    public Object getTarget() {
        return this.target;
    }

    @Override
    public void releaseTarget(Object target) {
        // 没有要做的事
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    /**
     * 两个调用拦截器相等，如果它们具有相同的目标，或者如果目标或目标对象相等。
     */
    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof SingletonTargetSource that && this.target.equals(that.target)));
    }

    /**
     * SingletonTargetSource 使用目标对象的哈希码。
     */
    @Override
    public int hashCode() {
        return this.target.hashCode();
    }

    @Override
    public String toString() {
        return "SingletonTargetSource for target object [" + ObjectUtils.identityToString(this.target) + "]";
    }
}
