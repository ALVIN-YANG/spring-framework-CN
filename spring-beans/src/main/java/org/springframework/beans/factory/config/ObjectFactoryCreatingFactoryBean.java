// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），您可以使用此文件，但必须遵守许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证以了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.factory.config;

import java.io.Serializable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 *  一个实现 {@link org.springframework.beans.factory.FactoryBean} 的类，它返回一个值，这个值是一个 {@link org.springframework.beans.factory.ObjectFactory}，该工厂随后从 {@link org.springframework.beans.factory.BeanFactory} 中返回一个 Bean。
 *
 * <p>因此，这可以用来避免客户端对象直接调用 {@link org.springframework.beans.factory.BeanFactory#getBean(String)} 来从
 * {@link org.springframework.beans.factory.BeanFactory} 获取一个（通常是原型）Bean，这将违反控制反转原则。相反，使用这个类，客户端对象可以接收一个
 * {@link org.springframework.beans.factory.ObjectFactory} 实例作为属性，该实例直接返回一个目标 Bean（再次强调，通常是原型 Bean）。
 *
 * <p>在一个基于 XML 的
 * {@link org.springframework.beans.factory.BeanFactory} 中的示例配置可能如下所示：
 *
 * <pre class="code">&lt;beans&gt;
 *
 *    &lt;!-- 原型 Bean，因为我们有状态 --&gt;
 *    &lt;bean id="myService" class="a.b.c.MyService" scope="prototype"/&gt;
 *
 *    &lt;bean id="myServiceFactory"
 *        class="org.springframework.beans.factory.config.ObjectFactoryCreatingFactoryBean"&gt;
 *      &lt;property name="targetBeanName"&gt;&lt;idref local="myService"/&gt;&lt;/property&gt;
 *    &lt;/bean&gt;
 *
 *    &lt;bean id="clientBean" class="a.b.c.MyClientBean"&gt;
 *      &lt;property name="myServiceFactory" ref="myServiceFactory"/&gt;
 *    &lt;/bean&gt;
 *
 * &lt;/beans&gt;</pre>
 *
 * <p>相应的 {@code MyClientBean} 类实现可能如下所示：
 *
 * <pre class="code">package a.b.c;
 *
 *  import org.springframework.beans.factory.ObjectFactory;
 *
 *  public class MyClientBean {
 *
 *    private ObjectFactory&lt;MyService&gt; myServiceFactory;
 *
 *    public void setMyServiceFactory(ObjectFactory&lt;MyService&gt; myServiceFactory) {
 *      this.myServiceFactory = myServiceFactory;
 *    }
 *
 *    public void someBusinessMethod() {
 *      // 获取一个全新的 MyService 实例
 *      MyService service = this.myServiceFactory.getObject();
 *      // 使用服务对象来执行业务逻辑...
 *    }
 *  }</pre>
 *
 * <p>另一种应用对象创建模式的方法是使用 {@link ServiceLocatorFactoryBean} 来获取（原型）Bean。使用
 * {@link ServiceLocatorFactoryBean} 的优点是不需要依赖于任何 Spring 特定的接口，如
 * {@link org.springframework.beans.factory.ObjectFactory}，但缺点是需要运行时类生成。请参阅
 * {@link ServiceLocatorFactoryBean ServiceLocatorFactoryBean JavaDoc} 以获得对此问题的更全面讨论。
 *
 *  @author Colin Sampaleanu
 *  @author Juergen Hoeller
 *  @since 1.0.2
 *  @see org.springframework.beans.factory.ObjectFactory
 *  @see ServiceLocatorFactoryBean
 */
public class ObjectFactoryCreatingFactoryBean extends AbstractFactoryBean<ObjectFactory<Object>> {

    @Nullable
    private String targetBeanName;

    /**
     * 设置目标 Bean 的名称。
     * <p>目标 Bean 不一定<i>必须</i>是非单例的，但现实中通常都会是（因为如果目标 Bean 是单例的，那么该单例 Bean 可以直接注入到依赖对象中，从而消除了通过这种工厂方法提供的多一层间接引用的需要）。
     */
    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.targetBeanName, "Property 'targetBeanName' is required");
        super.afterPropertiesSet();
    }

    @Override
    public Class<?> getObjectType() {
        return ObjectFactory.class;
    }

    @Override
    protected ObjectFactory<Object> createInstance() {
        BeanFactory beanFactory = getBeanFactory();
        Assert.state(beanFactory != null, "No BeanFactory available");
        Assert.state(this.targetBeanName != null, "No target bean name specified");
        return new TargetBeanObjectFactory(beanFactory, this.targetBeanName);
    }

    /**
     * 独立内部类 - 用于序列化目的。
     */
    @SuppressWarnings("serial")
    private static class TargetBeanObjectFactory implements ObjectFactory<Object>, Serializable {

        private final BeanFactory beanFactory;

        private final String targetBeanName;

        public TargetBeanObjectFactory(BeanFactory beanFactory, String targetBeanName) {
            this.beanFactory = beanFactory;
            this.targetBeanName = targetBeanName;
        }

        @Override
        public Object getObject() throws BeansException {
            return this.beanFactory.getBean(this.targetBeanName);
        }
    }
}
