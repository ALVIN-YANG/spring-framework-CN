// 翻译完成 glm-4-flash
/*版权所有 2002-2023，原作者或作者。

根据Apache License，版本2.0（“许可证”）；除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵权。
请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.aop.target;

import java.io.Serializable;
import org.springframework.aop.TargetSource;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * 当没有目标对象（或者只知道目标类）时，这是规范化的 {@code TargetSource}，
 * 行为仅由接口和顾问提供。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public final class EmptyTargetSource implements TargetSource, Serializable {

    /**
     * 使用 Spring 1.2 中的 serialVersionUID 以确保互操作性。
     */
    private static final long serialVersionUID = 3680494563553489691L;

    // 很抱歉，您只提供了代码的分割线（---------------------------------------------------------------------），没有提供实际的代码注释内容。请提供您希望翻译的Java代码注释，我将为您翻译。
    // 静态工厂方法
    // 很抱歉，您只提供了一个代码注释的起始符号“---------------------------------------------------------------------”，但没有提供实际的代码注释内容。请提供完整的代码注释内容，以便我能够进行翻译。
    /**
     * 该类的规范（单例）实例，对应于本类的 {@link EmptyTargetSource}。
     */
    public static final EmptyTargetSource INSTANCE = new EmptyTargetSource(null, true);

    /**
     * 返回给定目标类的 EmptyTargetSource。
     * @param targetClass 目标类（可能为 {@code null}）
     * @see #getTargetClass()
     */
    public static EmptyTargetSource forClass(@Nullable Class<?> targetClass) {
        return forClass(targetClass, true);
    }

    /**
     * 返回给定目标类的 EmptyTargetSource。
     * @param targetClass 目标类（可能为 null）
     * @param isStatic 是否将 TargetSource 标记为静态
     * @see #getTargetClass()
     */
    public static EmptyTargetSource forClass(@Nullable Class<?> targetClass, boolean isStatic) {
        return (targetClass == null && isStatic ? INSTANCE : new EmptyTargetSource(targetClass, isStatic));
    }

    // 由于您提供的代码注释内容为空，我无法进行翻译。请提供具体的Java代码注释内容，我将为您翻译成中文。
    // 实例实现
    // 很抱歉，您提供的内容 "---------------------------------------------------------------------" 并不是一个有效的 Java 代码注释。它看起来像是一条水平线或者一个分隔符，而不是代码的一部分。如果您能提供实际的 Java 代码注释内容，我会很乐意为您翻译。
    @Nullable
    private final Class<?> targetClass;

    private final boolean isStatic;

    /**
     * 创建一个新实例的{@link EmptyTargetSource}类。
     * <p>此构造函数是私有的，以强制执行单例模式/工厂方法模式。
     * @param targetClass 要暴露的目标类（可能为null）
     * @param isStatic TargetSource是否被标记为静态
     */
    private EmptyTargetSource(@Nullable Class<?> targetClass, boolean isStatic) {
        this.targetClass = targetClass;
        this.isStatic = isStatic;
    }

    /**
     * 总是返回指定的目标类，如果没有则返回 {@code null}。
     */
    @Override
    @Nullable
    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    /**
     * 总是返回 {@code true}。
     */
    @Override
    public boolean isStatic() {
        return this.isStatic;
    }

    /**
     * 总是返回 null。
     */
    @Override
    @Nullable
    public Object getTarget() {
        return null;
    }

    /**
     * 无需释放。
     */
    @Override
    public void releaseTarget(Object target) {
    }

    /**
     * 在反序列化过程中，如果没有目标类，则返回规范实例，从而保护单例模式。
     */
    private Object readResolve() {
        return (this.targetClass == null && this.isStatic ? INSTANCE : this);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof EmptyTargetSource that && ObjectUtils.nullSafeEquals(this.targetClass, that.targetClass) && this.isStatic == that.isStatic));
    }

    @Override
    public int hashCode() {
        return EmptyTargetSource.class.hashCode() * 13 + ObjectUtils.nullSafeHashCode(this.targetClass);
    }

    @Override
    public String toString() {
        return "EmptyTargetSource: " + (this.targetClass != null ? "target class [" + this.targetClass.getName() + "]" : "no target class") + ", " + (this.isStatic ? "static" : "dynamic");
    }
}
