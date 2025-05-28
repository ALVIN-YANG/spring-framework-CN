// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，除非法律要求或经书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律要求或书面同意，否则在许可协议下分发的软件按照“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议，了解具体的权限和限制。*/
package org.springframework.beans.testfixture.beans;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 简单测试 BeanFactory 初始化和生命周期回调。
 *
 * @author Rod Johnson
 * @author Colin Sampaleanu
 * @author Chris Beams
 * @since 2003年3月12日
 */
public class LifecycleBean implements BeanNameAware, BeanFactoryAware, InitializingBean, DisposableBean {

    protected boolean initMethodDeclared = false;

    protected String beanName;

    protected BeanFactory owningFactory;

    protected boolean postProcessedBeforeInit;

    protected boolean inited;

    protected boolean initedViaDeclaredInitMethod;

    protected boolean postProcessedAfterInit;

    protected boolean destroyed;

    public void setInitMethodDeclared(boolean initMethodDeclared) {
        this.initMethodDeclared = initMethodDeclared;
    }

    public boolean isInitMethodDeclared() {
        return initMethodDeclared;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.owningFactory = beanFactory;
    }

    public void postProcessBeforeInit() {
        if (this.inited || this.initedViaDeclaredInitMethod) {
            throw new RuntimeException("Factory called postProcessBeforeInit after afterPropertiesSet");
        }
        if (this.postProcessedBeforeInit) {
            throw new RuntimeException("Factory called postProcessBeforeInit twice");
        }
        this.postProcessedBeforeInit = true;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.owningFactory == null) {
            throw new RuntimeException("Factory didn't call setBeanFactory before afterPropertiesSet on lifecycle bean");
        }
        if (!this.postProcessedBeforeInit) {
            throw new RuntimeException("Factory didn't call postProcessBeforeInit before afterPropertiesSet on lifecycle bean");
        }
        if (this.initedViaDeclaredInitMethod) {
            throw new RuntimeException("Factory initialized via declared init method before initializing via afterPropertiesSet");
        }
        if (this.inited) {
            throw new RuntimeException("Factory called afterPropertiesSet twice");
        }
        this.inited = true;
    }

    public void declaredInitMethod() {
        if (!this.inited) {
            throw new RuntimeException("Factory didn't call afterPropertiesSet before declared init method");
        }
        if (this.initedViaDeclaredInitMethod) {
            throw new RuntimeException("Factory called declared init method twice");
        }
        this.initedViaDeclaredInitMethod = true;
    }

    public void postProcessAfterInit() {
        if (!this.inited) {
            throw new RuntimeException("Factory called postProcessAfterInit before afterPropertiesSet");
        }
        if (this.initMethodDeclared && !this.initedViaDeclaredInitMethod) {
            throw new RuntimeException("Factory called postProcessAfterInit before calling declared init method");
        }
        if (this.postProcessedAfterInit) {
            throw new RuntimeException("Factory called postProcessAfterInit twice");
        }
        this.postProcessedAfterInit = true;
    }

    /**
     * 一个模拟的业务方法，除非工厂正确管理了bean的生命周期，否则将失败
     */
    public void businessMethod() {
        if (!this.inited || (this.initMethodDeclared && !this.initedViaDeclaredInitMethod) || !this.postProcessedAfterInit) {
            throw new RuntimeException("Factory didn't initialize lifecycle object correctly");
        }
    }

    @Override
    public void destroy() {
        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed");
        }
        this.destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public static class PostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
            if (bean instanceof LifecycleBean) {
                ((LifecycleBean) bean).postProcessBeforeInit();
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
            if (bean instanceof LifecycleBean) {
                ((LifecycleBean) bean).postProcessAfterInit();
            }
            return bean;
        }
    }
}
