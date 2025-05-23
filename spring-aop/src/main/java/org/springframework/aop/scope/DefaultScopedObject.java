// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者。
 
根据Apache许可证版本2.0（以下简称“许可证”）授权；
除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.scope;

import java.io.Serializable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.util.Assert;

/**
 * 默认实现 {@link ScopedObject} 接口。
 *
 * <p>简单地将调用委托给底层的
 * {@link ConfigurableBeanFactory bean 工厂}
 * ({@link ConfigurableBeanFactory#getBean(String)}/
 * {@link ConfigurableBeanFactory#destroyScopedBean(String)}).
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroyScopedBean
 */
@SuppressWarnings("serial")
public class DefaultScopedObject implements ScopedObject, Serializable {

    private final ConfigurableBeanFactory beanFactory;

    private final String targetBeanName;

    /**
     * 创建一个新的 {@link DefaultScopedObject} 类实例。
     * @param beanFactory 包含作用域目标对象的 {@link ConfigurableBeanFactory}
     * @param targetBeanName 目标Bean的名称
     */
    public DefaultScopedObject(ConfigurableBeanFactory beanFactory, String targetBeanName) {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        Assert.hasText(targetBeanName, "'targetBeanName' must not be empty");
        this.beanFactory = beanFactory;
        this.targetBeanName = targetBeanName;
    }

    @Override
    public Object getTargetObject() {
        return this.beanFactory.getBean(this.targetBeanName);
    }

    @Override
    public void removeFromScope() {
        this.beanFactory.destroyScopedBean(this.targetBeanName);
    }
}
