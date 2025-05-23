// 翻译完成 glm-4-flash
/*版权所有 2002-2014 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）许可；
除非适用法律要求或书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.target;

import org.springframework.beans.BeansException;

/**
 * 实现{@link org.springframework.aop.TargetSource}的类，
 * 为每个请求创建目标bean的新实例，
 * 在释放时（每个请求之后）销毁每个实例。
 *
 * <p>从其包含的
 * {@link org.springframework.beans.factory.BeanFactory}获取bean实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setBeanFactory
 * @see #setTargetBeanName
 */
@SuppressWarnings("serial")
public class PrototypeTargetSource extends AbstractPrototypeBasedTargetSource {

    /**
     * 为每次调用获取一个新的原型实例。
     * @see #newPrototypeInstance()
     */
    @Override
    public Object getTarget() throws BeansException {
        return newPrototypeInstance();
    }

    /**
     * 销毁给定的独立实例。
     * @see #destroyPrototypeInstance
     */
    @Override
    public void releaseTarget(Object target) {
        destroyPrototypeInstance(target);
    }

    @Override
    public String toString() {
        return "PrototypeTargetSource for target bean with name '" + getTargetBeanName() + "'";
    }
}
