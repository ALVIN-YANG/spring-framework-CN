// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory;

/**
 * 抛出异常时，一个bean不是一个工厂，但用户尝试获取给定bean名称对应的工厂。一个bean是否是工厂由它是否实现了FactoryBean接口来决定。
 *
 * @author Rod Johnson
 * @since 10.03.2003
 * @see org.springframework.beans.factory.FactoryBean
 */
@SuppressWarnings("serial")
public class BeanIsNotAFactoryException extends BeanNotOfRequiredTypeException {

    /**
     * 创建一个新的 BeanIsNotAFactoryException 实例。
     * @param name 请求的 Bean 名称
     * @param actualType 实际返回的类型，该类型与预期类型不匹配
     */
    public BeanIsNotAFactoryException(String name, Class<?> actualType) {
        super(name, FactoryBean.class, actualType);
    }
}
