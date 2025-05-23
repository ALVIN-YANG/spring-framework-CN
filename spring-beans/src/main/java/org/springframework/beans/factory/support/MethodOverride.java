// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权和限制的语言。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 代表 IoC 容器中管理的对象方法重写的对象。
 *
 * <p>注意，重写机制 <em>并非</em> 作为插入横切代码的通用方式：为此请使用 AOP。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.1
 */
public abstract class MethodOverride implements BeanMetadataElement {

    private final String methodName;

    private boolean overloaded = true;

    @Nullable
    private Object source;

    /**
     * 构建给定方法的新重写。
     * @param methodName 要重写的方法名称
     */
    protected MethodOverride(String methodName) {
        Assert.notNull(methodName, "Method name must not be null");
        this.methodName = methodName;
    }

    /**
     * 返回要重写的方法名称。
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     *  设置是否重写的的方法是<em>重载</em>的（即，是否需要发生参数类型匹配来区分同名方法）。
     * 	<p>默认值为{@code true}；可以切换到{@code false}以优化运行时性能。
     */
    protected void setOverloaded(boolean overloaded) {
        this.overloaded = overloaded;
    }

    /**
     * 返回重写的方法是否是<em>重载</em>的（即，是否需要通过参数类型匹配来区分同名的多个方法）。
     */
    protected boolean isOverloaded() {
        return this.overloaded;
    }

    /**
     * 设置此元数据元素的配置源 {@code Object}。
     * <p>对象的精确类型将取决于所使用的配置机制。
     */
    public void setSource(@Nullable Object source) {
        this.source = source;
    }

    @Override
    @Nullable
    public Object getSource() {
        return this.source;
    }

    /**
     * 子类必须重写此方法以指示它们是否与给定的方法<em>匹配</em>。这允许进行参数列表检查以及方法名检查。
     * @param method 要检查的方法
     * @return 是否此重写方法与给定方法匹配
     */
    public abstract boolean matches(Method method);

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof MethodOverride that && ObjectUtils.nullSafeEquals(this.methodName, that.methodName) && ObjectUtils.nullSafeEquals(this.source, that.source)));
    }

    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(this.methodName);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.source);
        return hashCode;
    }
}
