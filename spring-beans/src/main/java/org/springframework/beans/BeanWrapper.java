// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体规定许可权和限制。*/
package org.springframework.beans;

import java.beans.PropertyDescriptor;

/**
 * Spring 低级 JavaBeans 基础设施的核心接口。
 *
 * <p>通常不直接使用，而是通过一个
 * {@link org.springframework.beans.factory.BeanFactory} 或一个
 * {@link org.springframework.validation.DataBinder} 间接使用。
 *
 * <p>提供操作来分析和操作标准 JavaBeans：获取和设置属性值（单个或批量）、获取属性描述符以及查询属性的可读性和可写性。
 *
 * <p>此接口支持 <b>嵌套属性</b>，允许对子属性的属性设置进行无限深度的设置。
 *
 * <p>BeanWrapper 的 "extractOldValueForEditor" 设置默认为 "false"，以避免因调用getter方法而引起的副作用。将其设置为 "true" 以将当前属性值暴露给自定义编辑器。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @see PropertyAccessor
 * @see PropertyEditorRegistry
 * @see PropertyAccessorFactory#forBeanPropertyAccess
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.validation.BeanPropertyBindingResult
 * @see org.springframework.validation.DataBinder#initBeanPropertyAccess()
 */
public interface BeanWrapper extends ConfigurablePropertyAccessor {

    /**
     * 指定数组和集合自动增长的限制。
     * <p>默认情况下，在普通的BeanWrapper上是无限制的。
     * @since 4.1
     */
    void setAutoGrowCollectionLimit(int autoGrowCollectionLimit);

    /**
     * 返回数组和集合自动增长的限制。
     * @since 4.1
     */
    int getAutoGrowCollectionLimit();

    /**
     * 返回由该对象包装的 bean 实例。
     */
    Object getWrappedInstance();

    /**
     * 返回包装的bean实例的类型。
     */
    Class<?> getWrappedClass();

    /**
     * 获取包装对象的属性描述符（通过标准的JavaBeans反射机制确定）。
     * @return 包装对象的属性描述符
     */
    PropertyDescriptor[] getPropertyDescriptors();

    /**
     * 获取包装对象的特定属性的属性描述符
     * @param propertyName 要获取描述符的属性名称
     * (可能是一个嵌套路径，但不能是一个索引/映射属性)
     * @return 指定属性的属性描述符
     * @throws InvalidPropertyException 如果不存在该属性
     */
    PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException;
}
