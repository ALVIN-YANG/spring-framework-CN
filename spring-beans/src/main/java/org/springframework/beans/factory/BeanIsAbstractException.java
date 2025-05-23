// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory;

/**
 * 当请求一个被标记为抽象的bean定义的bean实例时抛出的异常。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#setAbstract
 */
@SuppressWarnings("serial")
public class BeanIsAbstractException extends BeanCreationException {

    /**
     * 创建一个新的 BeanIsAbstractException 对象。
     * @param beanName 请求的 bean 名称
     */
    public BeanIsAbstractException(String beanName) {
        super(beanName, "Bean definition is abstract");
    }
}
