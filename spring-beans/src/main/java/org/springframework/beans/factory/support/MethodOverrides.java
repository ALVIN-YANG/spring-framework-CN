// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体规定许可权和限制。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.springframework.lang.Nullable;

/**
 * 一组方法重写，用于确定在运行时 Spring IoC 容器将覆盖托管对象上的哪些方法（如果有的话）。
 *
 * <p>目前支持的 {@link MethodOverride} 变体有 {@link LookupOverride} 和 {@link ReplaceOverride}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 * @see MethodOverride
 */
public class MethodOverrides {

    private final Set<MethodOverride> overrides = new CopyOnWriteArraySet<>();

    /**
     * 创建新的 MethodOverrides。
     */
    public MethodOverrides() {
    }

    /**
     * 深拷贝构造函数。
     */
    public MethodOverrides(MethodOverrides other) {
        addOverrides(other);
    }

    /**
     * 将所有给定的方法重写复制到这个对象中。
     */
    public void addOverrides(@Nullable MethodOverrides other) {
        if (other != null) {
            this.overrides.addAll(other.overrides);
        }
    }

    /**
     * 添加指定的方法重写。
     */
    public void addOverride(MethodOverride override) {
        this.overrides.add(override);
    }

    /**
     * 返回此对象包含的所有方法重写。
     * @return 返回一个包含 MethodOverride 对象的 Set
     * @see MethodOverride
     */
    public Set<MethodOverride> getOverrides() {
        return this.overrides;
    }

    /**
     * 返回方法重写集是否为空。
     */
    public boolean isEmpty() {
        return this.overrides.isEmpty();
    }

    /**
     * 返回给定方法的覆盖实现，如果有的话。
     * @param method 要检查覆盖实现的方法
     * @return 方法的覆盖实现，如果没有则返回 {@code null}
     */
    @Nullable
    public MethodOverride getOverride(Method method) {
        MethodOverride match = null;
        for (MethodOverride candidate : this.overrides) {
            if (candidate.matches(method)) {
                match = candidate;
            }
        }
        return match;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof MethodOverrides that && this.overrides.equals(that.overrides)));
    }

    @Override
    public int hashCode() {
        return this.overrides.hashCode();
    }
}
