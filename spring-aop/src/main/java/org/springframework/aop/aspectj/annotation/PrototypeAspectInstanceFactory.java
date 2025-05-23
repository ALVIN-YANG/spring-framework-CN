// 翻译完成 glm-4-flash
/*版权所有 2002-2015 原作者或作者。
 
根据Apache License, Version 2.0 ("许可证")授权；
除非遵守许可证，否则不得使用此文件。
您可以在以下链接获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵权。
有关许可证的具体语言、许可权限和限制，请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import java.io.Serializable;
import org.springframework.beans.factory.BeanFactory;

/**
 * 基于 {@link org.springframework.aop.aspectj.AspectInstanceFactory}，由
 * 由 {@link BeanFactory} 提供的原型实现，强制执行原型语义。
 *
 * <p>请注意，这可能会多次实例化，这可能不会给出您预期的语义。请使用
 * {@link LazySingletonAspectInstanceFactoryDecorator} 来包装此对象，以确保
 * 只返回一个新方面。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory
 * @see LazySingletonAspectInstanceFactoryDecorator
 */
@SuppressWarnings("serial")
public class PrototypeAspectInstanceFactory extends BeanFactoryAspectInstanceFactory implements Serializable {

    /**
     * 创建一个PrototypeAspectInstanceFactory。AspectJ将被调用以进行反射，使用从BeanFactory中返回的给定bean名称的类型来创建AJType元数据。
     * @param beanFactory 从其中获取实例的BeanFactory
     * @param name bean的名称
     */
    public PrototypeAspectInstanceFactory(BeanFactory beanFactory, String name) {
        super(beanFactory, name);
        if (!beanFactory.isPrototype(name)) {
            throw new IllegalArgumentException("Cannot use PrototypeAspectInstanceFactory with bean named '" + name + "': not a prototype");
        }
    }
}
