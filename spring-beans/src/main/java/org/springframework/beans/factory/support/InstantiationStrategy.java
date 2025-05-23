// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为或特定用途的适用性。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;

/**
 * 负责创建与根Bean定义对应的实例的接口。
 *
 * <p>这个接口被提取为一个策略，因为存在多种可能的实现方法，
 * 包括使用CGLIB动态创建子类以支持方法注入。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public interface InstantiationStrategy {

    /**
     * 返回本工厂中给定名称的bean实例。
     * @param bd bean定义
     * @param beanName 在本上下文中创建bean时的名称。
     * 如果我们正在自动装配不属于工厂的bean，名称可以为null。
     * @param owner 拥有BeanFactory
     * @return 对应此bean定义的bean实例
     * @throws BeansException 如果实例化尝试失败
     */
    Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) throws BeansException;

    /**
     * 返回此工厂中给定名称的bean实例，通过给定的构造函数创建。
     * @param bd bean定义
     * @param beanName 在此上下文中创建的bean的名称。如果我们要自动装配不属于工厂的bean，则名称可以为null。
     * @param owner 拥有BeanFactory
     * @param ctor 要使用的构造函数
     * @param args 要应用的构造函数参数
     * @return 该bean定义的bean实例
     * @throws BeansException 如果实例化尝试失败
     */
    Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner, Constructor<?> ctor, Object... args) throws BeansException;

    /**
     * 返回此工厂中给定名称的bean实例，通过给定的工厂方法创建。
     * @param bd bean定义
     * @param beanName 在此上下文中创建bean时的名称。名称可以是null，如果我们正在自动装配不属于工厂的bean。
     * @param owner 拥有BeanFactory
     * @param factoryBean 调用工厂方法的工厂bean实例，或为null（在这种情况下，使用静态工厂方法）
     * @param factoryMethod 要使用的工厂方法
     * @param args 应用到工厂方法的参数
     * @return 此bean定义的bean实例
     * @throws BeansException 如果实例化尝试失败
     */
    Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner, @Nullable Object factoryBean, Method factoryMethod, Object... args) throws BeansException;

    /**
     * 确定给定 Bean 定义在运行时实际所对应的类。
     * @since 6.0
     */
    default Class<?> getActualBeanClass(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
        return bd.getBeanClass();
    }
}
