// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可；除非符合许可证，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可的具体语言和限制，请参阅许可证。*/
package org.springframework.aop.target;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 基于 Spring 的 {@link org.springframework.beans.factory.BeanFactory} 的实现类的基础类，这些实现类将委托给 Spring 管理的 bean 实例。
 *
 * <p>子类可以创建原型实例或懒加载一个单例目标，例如。请参阅 {@link LazyInitTargetSource} 和 {@link AbstractPrototypeBasedTargetSource} 的子类以获取具体策略。
 *
 * <p>基于 BeanFactory 的 TargetSource 是可序列化的。这涉及到断开当前目标并将其转换为 {@link SingletonTargetSource}。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 1.1.4
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see LazyInitTargetSource
 * @see PrototypeTargetSource
 * @see ThreadLocalTargetSource
 * @see CommonsPool2TargetSource
 */
public abstract class AbstractBeanFactoryBasedTargetSource implements TargetSource, BeanFactoryAware, Serializable {

    /**
     * 使用 Spring 1.2.7 中的 serialVersionUID 以确保互操作性。
     */
    private static final long serialVersionUID = -4721607536018568393L;

    /**
     * Logger 可供子类使用。
     */
    protected final transient Log logger = LogFactory.getLog(getClass());

    /**
     * 每次调用时创建的目标 Bean 的名称。
     */
    @Nullable
    private String targetBeanName;

    /**
     * 目标类的类。
     */
    @Nullable
    private volatile Class<?> targetClass;

    /**
     * 拥有此TargetSource的BeanFactory。我们需要保留对这个引用，以便在需要时创建新的原型实例。
     */
    @Nullable
    private BeanFactory beanFactory;

    /**
     * 在工厂中设置目标bean的名称。
     * <p>目标bean不应是单例，否则每次从工厂获取的都是同一个实例，从而导致与{@link SingletonTargetSource}提供的行为相同。
     * @param targetBeanName 该拦截器所属的BeanFactory中目标bean的名称
     * @see SingletonTargetSource
     */
    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

    /**
     * 返回工厂中目标bean的名称。
     */
    public String getTargetBeanName() {
        Assert.state(this.targetBeanName != null, "Target bean name not set");
        return this.targetBeanName;
    }

    /**
     * 明确指定目标类，以避免对目标bean的任何访问（例如，避免初始化FactoryBean实例）。
     * <p>默认情况下，将通过在BeanFactory上调用{@code getType}方法自动检测类型（甚至可以作为后备使用完整的{@code getBean}调用）。
     */
    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * 设置所属的BeanFactory。我们需要保存一个引用，以便我们可以在每次调用时使用
     * {@code getBean} 方法。
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (this.targetBeanName == null) {
            throw new IllegalStateException("Property 'targetBeanName' is required");
        }
        this.beanFactory = beanFactory;
    }

    /**
     * 返回所属的BeanFactory。
     */
    public BeanFactory getBeanFactory() {
        Assert.state(this.beanFactory != null, "BeanFactory not set");
        return this.beanFactory;
    }

    @Override
    @Nullable
    public Class<?> getTargetClass() {
        Class<?> targetClass = this.targetClass;
        if (targetClass != null) {
            return targetClass;
        }
        synchronized (this) {
            // 在同步块内进行完整检查，仅进入一次BeanFactory交互算法...
            targetClass = this.targetClass;
            if (targetClass == null && this.beanFactory != null && this.targetBeanName != null) {
                // 确定目标 Bean 的类型。
                targetClass = this.beanFactory.getType(this.targetBeanName);
                if (targetClass == null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Getting bean with name '" + this.targetBeanName + "' for type determination");
                    }
                    Object beanInstance = this.beanFactory.getBean(this.targetBeanName);
                    targetClass = beanInstance.getClass();
                }
                this.targetClass = targetClass;
            }
            return targetClass;
        }
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public void releaseTarget(Object target) throws Exception {
        // 此处无事可做。
    }

    /**
     * /**
     *  从另一个 AbstractBeanFactoryBasedTargetSource 对象复制配置。
     *  子类如果希望暴露此功能，则应该重写此方法。
     *  @param other 从该对象复制配置
     * /
     */
    protected void copyFrom(AbstractBeanFactoryBasedTargetSource other) {
        this.targetBeanName = other.targetBeanName;
        this.targetClass = other.targetClass;
        this.beanFactory = other.beanFactory;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AbstractBeanFactoryBasedTargetSource otherTargetSource = (AbstractBeanFactoryBasedTargetSource) other;
        return (ObjectUtils.nullSafeEquals(this.beanFactory, otherTargetSource.beanFactory) && ObjectUtils.nullSafeEquals(this.targetBeanName, otherTargetSource.targetBeanName));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() * 13 + ObjectUtils.nullSafeHashCode(this.targetBeanName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(" for target bean '").append(this.targetBeanName).append('\'');
        Class<?> targetClass = this.targetClass;
        if (targetClass != null) {
            sb.append(" of type [").append(targetClass.getName()).append(']');
        }
        return sb.toString();
    }
}
