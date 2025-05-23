// 翻译完成 glm-4-flash
/*版权所有 2002-2022 原作者或作者们。

根据Apache许可证2.0版（以下简称“许可证”）许可；除非符合许可证规定，否则不得使用此文件。
您可以在以下网址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可的特定语言、权限和限制，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy.target;

import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;

/**
 *  用于强制为每个定义为“延迟初始化”的bean创建一个`LazyInitTargetSource`的`TargetSourceCreator`。这将导致为这些bean中的每一个创建一个代理，允许在不实际初始化目标bean实例的情况下获取该bean的引用。
 *
 * <p>作为自动代理创建器的自定义`TargetSourceCreator`进行注册，结合特定bean的自定义拦截器或仅用于创建延迟初始化代理。例如，作为一个在XML应用程序上下文定义中自动检测的基础设施bean：
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator"&gt;
 *   &lt;property name="beanNames" value="*" /&gt; &lt;!-- 应用到所有bean --&gt;
 *   &lt;property name="customTargetSourceCreators"&gt;
 *     &lt;list&gt;
 *       &lt;bean class="org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator" /&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="myLazyInitBean" class="mypackage.MyBeanClass" lazy-init="true"&gt;
 *   &lt;!-- ... --&gt;
 * &lt;/bean&gt;</pre>
 *
 * @since 1.2
 * @see org.springframework.beans.factory.config.BeanDefinition#isLazyInit
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators
 * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator
 */
public class LazyInitTargetSourceCreator extends AbstractBeanFactoryBasedTargetSourceCreator {

    @Override
    protected boolean isPrototypeBased() {
        return false;
    }

    @Override
    @Nullable
    protected AbstractBeanFactoryBasedTargetSource createBeanFactoryBasedTargetSource(Class<?> beanClass, String beanName) {
        if (getBeanFactory() instanceof ConfigurableListableBeanFactory clbf) {
            BeanDefinition definition = clbf.getBeanDefinition(beanName);
            if (definition.isLazyInit()) {
                return new LazyInitTargetSource();
            }
        }
        return null;
    }
}
