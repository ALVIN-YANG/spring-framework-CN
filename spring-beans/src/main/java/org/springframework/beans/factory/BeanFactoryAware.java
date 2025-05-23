// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可协议") 进行许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“原样”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * 该接口供希望了解其所属的 {@link BeanFactory} 的 Bean 实现。
 *
 * <p>例如，Bean 可以通过工厂查找协作的 Bean（依赖查找）。请注意，大多数 Bean 都会选择通过相应的 Bean 属性或构造函数参数来接收协作 Bean 的引用（依赖注入）。
 *
 * <p>关于所有 Bean 生命周期方法的列表，请参阅 {@link BeanFactory BeanFactory} 的 javadoc。
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @since 2003年3月11日
 * @see BeanNameAware
 * @see BeanClassLoaderAware
 * @see InitializingBean
 * @see org.springframework.context.ApplicationContextAware
 */
public interface BeanFactoryAware extends Aware {

    /**
     * 供给bean实例所属工厂的回调。
     * <p>在正常bean属性填充之后，但在初始化回调（如
     * {@link InitializingBean#afterPropertiesSet()} 或自定义的init-method）之前被调用。
     * @param beanFactory 所拥有的BeanFactory（绝不会为{@code null}）。
     * bean可以立即调用工厂上的方法。
     * @throws BeansException 在初始化错误的情况下
     * @see BeanInitializationException
     */
    void setBeanFactory(BeanFactory beanFactory) throws BeansException;
}
