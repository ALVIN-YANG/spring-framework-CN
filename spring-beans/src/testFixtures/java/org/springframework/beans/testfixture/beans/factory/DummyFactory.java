// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。请参阅许可证了解具体管理许可
* 授予权限和限制的条款。*/
package org.springframework.beans.testfixture.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.testfixture.beans.TestBean;

/**
 * 简单工厂，用于测试 AbstractBeanFactory 中 FactoryBean 的支持。
 * 根据其 singleton 属性是否设置，它将返回一个单例实例或原型实例。
 *
 * <p>实现了 InitializingBean 接口，这样我们就可以检查工厂是否在需要时获得这个生命周期回调。
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @since 2003年3月10日
 */
public class DummyFactory implements FactoryBean<Object>, BeanNameAware, BeanFactoryAware, InitializingBean, DisposableBean {

    public static final String SINGLETON_NAME = "Factory singleton";

    private static boolean prototypeCreated;

    /**
     * 清除静态状态。
     */
    public static void reset() {
        prototypeCreated = false;
    }

    /**
     * 默认情况下，工厂应该返回一个单例实例。
     */
    private boolean singleton = true;

    private String beanName;

    private AutowireCapableBeanFactory beanFactory;

    private boolean postProcessed;

    private boolean initialized;

    private TestBean testBean;

    private TestBean otherTestBean;

    public DummyFactory() {
        this.testBean = new TestBean();
        this.testBean.setName(SINGLETON_NAME);
        this.testBean.setAge(25);
    }

    /**
     * 如果由该工厂管理的Bean是单例，则返回。
     * @see FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return this.singleton;
    }

    /**
     * 设置由该工厂管理的 Bean 是否为单例。
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
        this.beanFactory.applyBeanPostProcessorsBeforeInitialization(this.testBean, this.beanName);
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setPostProcessed(boolean postProcessed) {
        this.postProcessed = postProcessed;
    }

    public boolean isPostProcessed() {
        return postProcessed;
    }

    public void setOtherTestBean(TestBean otherTestBean) {
        this.otherTestBean = otherTestBean;
        this.testBean.setSpouse(otherTestBean);
    }

    public TestBean getOtherTestBean() {
        return otherTestBean;
    }

    @Override
    public void afterPropertiesSet() {
        if (initialized) {
            throw new RuntimeException("Cannot call afterPropertiesSet twice on the one bean");
        }
        this.initialized = true;
    }

    /**
     * 是否通过调用InitializingBean接口中的afterPropertiesSet()方法进行初始化？
     */
    public boolean wasInitialized() {
        return initialized;
    }

    public static boolean wasPrototypeCreated() {
        return prototypeCreated;
    }

    /**
     * 返回管理对象，支持单例模式和原型模式。
     * @see FactoryBean#getObject()
     */
    @Override
    public Object getObject() throws BeansException {
        if (isSingleton()) {
            return this.testBean;
        } else {
            TestBean prototype = new TestBean("prototype created at " + System.currentTimeMillis(), 11);
            if (this.beanFactory != null) {
                this.beanFactory.applyBeanPostProcessorsBeforeInitialization(prototype, this.beanName);
            }
            prototypeCreated = true;
            return prototype;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return TestBean.class;
    }

    @Override
    public void destroy() {
        if (this.testBean != null) {
            this.testBean.setName(null);
        }
    }
}
