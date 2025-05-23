// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.aop.target;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * 动态实现 {@link org.springframework.aop.TargetSource} 的基类，用于创建新的原型 bean 实例以支持池化或每次调用创建新实例的策略。
 *
 * <p>此类 TargetSource 必须在 {@link BeanFactory} 中运行，因为它需要调用 `getBean` 方法来创建新的原型实例。因此，这个基类扩展了 {@link AbstractBeanFactoryBasedTargetSource}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see PrototypeTargetSource
 * @see ThreadLocalTargetSource
 * @see CommonsPool2TargetSource
 */
@SuppressWarnings("serial")
public abstract class AbstractPrototypeBasedTargetSource extends AbstractBeanFactoryBasedTargetSource {

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        super.setBeanFactory(beanFactory);
        // 检查目标Bean是否定义为原型（prototype）。
        if (!beanFactory.isPrototype(getTargetBeanName())) {
            throw new BeanDefinitionStoreException("Cannot use prototype-based TargetSource against non-prototype bean with name '" + getTargetBeanName() + "': instances would not be independent");
        }
    }

    /**
     * 子类应调用此方法以创建一个新的原型实例。
     * 如果bean创建失败，则抛出BeansException异常。
     */
    protected Object newPrototypeInstance() throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating new instance of bean '" + getTargetBeanName() + "'");
        }
        return getBeanFactory().getBean(getTargetBeanName());
    }

    /**
     * 子类应调用此方法来销毁一个过时的原型实例。
     * @param target 要销毁的Bean实例
     */
    protected void destroyPrototypeInstance(Object target) {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying instance of bean '" + getTargetBeanName() + "'");
        }
        if (getBeanFactory() instanceof ConfigurableBeanFactory cbf) {
            cbf.destroyBean(getTargetBeanName(), target);
        } else if (target instanceof DisposableBean disposableBean) {
            try {
                disposableBean.destroy();
            } catch (Throwable ex) {
                logger.warn("Destroy method on bean with name '" + getTargetBeanName() + "' threw an exception", ex);
            }
        }
    }

    // 很抱歉，您提供的代码注释内容是空的，没有包含任何注释文本。请提供具体的 Java 代码注释内容，我才能帮您进行翻译。
    // 序列化支持
    // 很抱歉，您没有提供具体的Java代码注释内容。请提供需要翻译的代码注释，我将为您进行翻译。
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        throw new NotSerializableException("A prototype-based TargetSource itself is not deserializable - " + "just a disconnected SingletonTargetSource or EmptyTargetSource is");
    }

    /**
     * 在序列化过程中，将此对象替换为SingletonTargetSource。
     * 被声明为受保护的，否则子类将无法调用此方法。
     * （`writeReplace()`方法必须对正在序列化的类可见。）
     * <p>使用此方法的实现，无需将此类或子类中的非序列化字段标记为transient。
     */
    protected Object writeReplace() throws ObjectStreamException {
        if (logger.isDebugEnabled()) {
            logger.debug("Disconnecting TargetSource [" + this + "]");
        }
        try {
            // 创建不连接的 SingletonTargetSource/EmptyTargetSource。
            Object target = getTarget();
            return (target != null ? new SingletonTargetSource(target) : EmptyTargetSource.forClass(getTargetClass()));
        } catch (Exception ex) {
            String msg = "Cannot get target for disconnecting TargetSource [" + this + "]";
            logger.error(msg, ex);
            throw new NotSerializableException(msg + ": " + ex);
        }
    }
}
