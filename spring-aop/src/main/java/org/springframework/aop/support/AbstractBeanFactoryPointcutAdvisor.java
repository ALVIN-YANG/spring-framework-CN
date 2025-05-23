// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者。
 
根据Apache License 2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下网址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是隐含的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.IOException;
import java.io.ObjectInputStream;
import org.aopalliance.aop.Advice;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 基于BeanFactory的抽象PointcutAdvisor，允许任何Advice被配置为对Advice bean的引用，在BeanFactory中。
 *
 * <p>指定advice bean的名称而不是advice对象本身（如果运行在BeanFactory中）
 * 可以在初始化时提高松散耦合，以便在pointcut实际匹配之前不初始化advice对象。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see #setAdviceBeanName
 * @see DefaultBeanFactoryPointcutAdvisor
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanFactoryPointcutAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    @Nullable
    private String adviceBeanName;

    @Nullable
    private BeanFactory beanFactory;

    @Nullable
    private transient volatile Advice advice;

    private transient volatile Object adviceMonitor = new Object();

    /**
     * 指定此顾问应该引用的咨询Bean的名称。
     * <p>在首次访问此顾问的咨询时，将获取指定Bean的一个实例。此顾问将只获取最多一个咨询Bean的单个实例，并将实例缓存以供顾问的生命周期使用。
     * @see #getAdvice()
     */
    public void setAdviceBeanName(@Nullable String adviceBeanName) {
        this.adviceBeanName = adviceBeanName;
    }

    /**
     * 返回此顾问引用的咨询bean的名称，如果有的话。
     */
    @Nullable
    public String getAdviceBeanName() {
        return this.adviceBeanName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        resetAdviceMonitor();
    }

    private void resetAdviceMonitor() {
        if (this.beanFactory instanceof ConfigurableBeanFactory cbf) {
            this.adviceMonitor = cbf.getSingletonMutex();
        } else {
            this.adviceMonitor = new Object();
        }
    }

    /**
     * 直接指定目标通知（Advice）的特定实例，
     * 避免在`#getAdvice()`中使用延迟解析。
     * @since 3.1
     */
    public void setAdvice(Advice advice) {
        synchronized (this.adviceMonitor) {
            this.advice = advice;
        }
    }

    @Override
    public Advice getAdvice() {
        Advice advice = this.advice;
        if (advice != null) {
            return advice;
        }
        Assert.state(this.adviceBeanName != null, "'adviceBeanName' must be specified");
        Assert.state(this.beanFactory != null, "BeanFactory must be set to resolve 'adviceBeanName'");
        if (this.beanFactory.isSingleton(this.adviceBeanName)) {
            // 依赖工厂提供的单例语义。
            advice = this.beanFactory.getBean(this.adviceBeanName, Advice.class);
            this.advice = advice;
            return advice;
        } else {
            // 工厂不保证单例 -> 我们本地进行锁定，但是
            // 复用工厂的单一实例锁，以防出现延迟依赖
            // 我们的建议 Bean 不幸隐式地触发了单例锁...
            synchronized (this.adviceMonitor) {
                advice = this.advice;
                if (advice == null) {
                    advice = this.beanFactory.getBean(this.adviceBeanName, Advice.class);
                    this.advice = advice;
                }
                return advice;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append(": advice ");
        if (this.adviceBeanName != null) {
            sb.append("bean '").append(this.adviceBeanName).append('\'');
        } else {
            sb.append(this.advice);
        }
        return sb.toString();
    }

    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将很乐意为您提供准确的中文翻译。
    // 序列化支持
    // 很抱歉，您提供的代码注释内容是空的，没有实际的注释内容可以翻译。请提供具体的 Java 代码注释，以便我能够为您进行翻译。
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // 依赖于默认序列化，只需在反序列化后初始化状态即可。
        ois.defaultReadObject();
        // 初始化 transient 字段。
        resetAdviceMonitor();
    }
}
