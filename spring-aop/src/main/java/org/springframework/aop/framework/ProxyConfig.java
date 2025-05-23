// 翻译完成 glm-4-flash
/*版权所有 2002-2021 原作者或原作者。

根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接处获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式（明示或暗示）的保证或条件。有关许可权限和限制的具体语言，
请参阅许可证。*/
package org.springframework.aop.framework;

import java.io.Serializable;
import org.springframework.util.Assert;

/**
 * 用于创建代理时配置的便利超类，
 * 确保所有代理创建者具有一致的属性。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AdvisedSupport
 */
public class ProxyConfig implements Serializable {

    /**
     * 使用 Spring 1.2 中的 serialVersionUID 以确保互操作性。
     */
    private static final long serialVersionUID = -8409359707199703185L;

    private boolean proxyTargetClass = false;

    private boolean optimize = false;

    boolean opaque = false;

    boolean exposeProxy = false;

    private boolean frozen = false;

    /**
     * 设置是否直接代理目标类，而不是仅仅代理特定的接口。默认为 "false"。
     * <p>将此设置为 "true" 将强制对 TargetSource 暴露的目标类进行代理。如果该目标类是一个接口，将为该接口创建一个 JDK 代理。如果该目标类是任何其他类，将为该类创建一个 CGLIB 代理。
     * <p>注意：根据具体代理工厂的配置，如果在没有指定接口（并且未激活接口自动检测）的情况下，也会应用 proxy-target-class 行为。
     * @see org.springframework.aop.TargetSource#getTargetClass()
     */
    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    /**
     * 返回是否直接代理目标类以及任何接口。
     */
    public boolean isProxyTargetClass() {
        return this.proxyTargetClass;
    }

    /**
     * 设置代理是否应该执行激进的优化。
     * “激进的优化”的确切含义在不同的代理之间可能会有所不同，但通常会有一些权衡。
     * 默认值为“false”。
     * <p>在Spring当前的代理选项中，此标志实际上强制使用CGLIB代理（类似于{@link #setProxyTargetClass}），但无需进行任何类验证检查（对于最终方法等）。
     */
    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    /**
     * 返回是否应该对代理执行激进优化。
     */
    public boolean isOptimize() {
        return this.optimize;
    }

    /**
     * 设置由该配置创建的代理是否应防止被转换为 {@link Advised} 以查询代理状态。
     * <p>默认值为 "false"，表示任何 AOP 代理都可以被转换为 {@link Advised}。
     */
    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }

    /**
     * 返回由此配置创建的代理是否应该被阻止转换为 {@link Advised}。
     */
    public boolean isOpaque() {
        return this.opaque;
    }

    /**
     * 设置是否通过AOP框架将代理以ThreadLocal的形式暴露，以便通过AopContext类进行检索。这非常有用，
     * 如果一个被建议的对象需要调用其自身的另一个被建议的方法。（如果它使用`this`，则调用不会被建议）。
     * <p>默认值为"false"，以避免不必要的额外拦截。这意味着无法保证在建议对象的任何方法中AopContext的访问都能
     * 一致地工作。
     */
    public void setExposeProxy(boolean exposeProxy) {
        this.exposeProxy = exposeProxy;
    }

    /**
     * 返回 AOP 代理是否会在每次调用时暴露 AOP 代理。
     */
    public boolean isExposeProxy() {
        return this.exposeProxy;
    }

    /**
     * 设置此配置是否应该被冻结。
     * <p>当一个配置被冻结时，将无法进行任何建议更改。这在优化时非常有用，并且当不想允许调用者在转换为Advised后操作配置时也很有用。
     */
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    /**
     * 返回配置是否已冻结，并且无法进行任何建议更改。
     */
    public boolean isFrozen() {
        return this.frozen;
    }

    /**
     * 从其他配置对象复制配置。
     * @param other 要从中复制配置的对象
     */
    public void copyFrom(ProxyConfig other) {
        Assert.notNull(other, "Other ProxyConfig object must not be null");
        this.proxyTargetClass = other.proxyTargetClass;
        this.optimize = other.optimize;
        this.exposeProxy = other.exposeProxy;
        this.frozen = other.frozen;
        this.opaque = other.opaque;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("proxyTargetClass=").append(this.proxyTargetClass).append("; ");
        sb.append("optimize=").append(this.optimize).append("; ");
        sb.append("opaque=").append(this.opaque).append("; ");
        sb.append("exposeProxy=").append(this.exposeProxy).append("; ");
        sb.append("frozen=").append(this.frozen);
        return sb.toString();
    }
}
