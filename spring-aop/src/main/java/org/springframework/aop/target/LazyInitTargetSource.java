// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.target;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 *  懒加载访问来自 `org.springframework.beans.factory.BeanFactory` 的单例 Bean 的 `org.springframework.aop.TargetSource`。
 *
 * <p>当需要在初始化时需要一个代理引用，但实际的目标对象应该在首次使用时才初始化时，这很有用。当目标 Bean 定义在 `org.springframework.context.ApplicationContext`（或一个会预先实例化单例 Bean 的 `BeanFactory`）中时，它也必须标记为 "lazy-init"，否则它将在启动时被该 `ApplicationContext`（或 `BeanFactory`）实例化。
 * <p>例如：
 * <pre class="code">
 * &lt;bean id="serviceTarget" class="example.MyService" lazy-init="true"&gt;
 *   ...
 * &lt;/bean&gt;
 *
 * &lt;bean id="service" class="org.springframework.aop.framework.ProxyFactoryBean"&gt;
 *   &lt;property name="targetSource"&gt;
 *     &lt;bean class="org.springframework.aop.target.LazyInitTargetSource"&gt;
 *       &lt;property name="targetBeanName"&gt;&lt;idref local="serviceTarget"/&gt;&lt;/property&gt;
 *     &lt;/bean&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 * “serviceTarget” Bean 不会在调用 "service" 代理上的任何方法之前初始化。
 *
 * <p>子类可以扩展此类并覆盖 `#postProcessTargetObject(Object)` 方法，以便在目标对象首次加载时执行一些额外的处理。
 *
 * @作者 Juergen Hoeller
 * @作者 Rob Harrop
 * @since 1.1.4
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see #postProcessTargetObject
 */
@SuppressWarnings("serial")
public class LazyInitTargetSource extends AbstractBeanFactoryBasedTargetSource {

    @Nullable
    private Object target;

    @Override
    public synchronized Object getTarget() throws BeansException {
        if (this.target == null) {
            this.target = getBeanFactory().getBean(getTargetBeanName());
            postProcessTargetObject(this.target);
        }
        return this.target;
    }

    /**
     * 子类可以重写此方法，在目标对象首次加载时执行额外的处理。
     * @param targetObject 刚被实例化（并配置）的目标对象
     */
    protected void postProcessTargetObject(Object targetObject) {
    }
}
