// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.Collection;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

/**
 * {@link BeanInfoFactory} 的实现，绕过标准的 {@link java.beans.Introspector} 以实现更快的内省，缩减为基本的属性确定（如 Spring 中通常需要的）。
 *
 * <p>在 6.0 中默认使用，通过直接从 {@link CachedIntrospectionResults} 调用来使用。
 * 可以通过以下内容的 {@code META-INF/spring.factories} 文件进行配置，以覆盖其他自定义的 {@code org.springframework.beans.BeanInfoFactory} 声明：
 * {@code org.springframework.beans.BeanInfoFactory=org.springframework.beans.SimpleBeanInfoFactory}
 *
 * <p>在 {@code Ordered.LOWEST_PRECEDENCE - 1} 的顺序中，如果需要可以覆盖 {@link ExtendedBeanInfoFactory}（默认在 5.3 中注册），同时仍然允许其他用户定义的
 * {@link BeanInfoFactory} 类型具有优先权。
 *
 * @author Juergen Hoeller
 * @since 5.3.24
 * @see ExtendedBeanInfoFactory
 * @see CachedIntrospectionResults
 */
class SimpleBeanInfoFactory implements BeanInfoFactory, Ordered {

    @Override
    @NonNull
    public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
        Collection<? extends PropertyDescriptor> pds = PropertyDescriptorUtils.determineBasicProperties(beanClass);
        return new SimpleBeanInfo() {

            @Override
            public BeanDescriptor getBeanDescriptor() {
                return new BeanDescriptor(beanClass);
            }

            @Override
            public PropertyDescriptor[] getPropertyDescriptors() {
                return pds.toArray(PropertyDescriptorUtils.EMPTY_PROPERTY_DESCRIPTOR_ARRAY);
            }
        };
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
