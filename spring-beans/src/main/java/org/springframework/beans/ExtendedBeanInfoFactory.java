// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“协议”）进行许可；
* 您不得使用此文件除非遵守该协议。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在协议下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

/**
 *  对 {@link StandardBeanInfoFactory} 的扩展，该扩展通过 Spring 的（包可见的）{@code ExtendedBeanInfo} 实现通过反射支持“非标准”JavaBeans 的设置方法。
 *
 * <p>通过以下内容的 {@code META-INF/spring.factories} 文件进行配置：
 *  {@code org.springframework.beans.BeanInfoFactory=org.springframework.beans.ExtendedBeanInfoFactory}
 *
 * <p>按 {@link Ordered#LOWEST_PRECEDENCE} 排序，以允许其他用户定义的 {@link BeanInfoFactory} 类型具有优先权。
 *
 *  @author Chris Beams
 *  @author Juergen Hoeller
 *  @since 3.2
 *  @see StandardBeanInfoFactory
 *  @see CachedIntrospectionResults
 */
public class ExtendedBeanInfoFactory extends StandardBeanInfoFactory {

    @Override
    @NonNull
    public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
        BeanInfo beanInfo = super.getBeanInfo(beanClass);
        return (supports(beanClass) ? new ExtendedBeanInfo(beanInfo) : beanInfo);
    }

    /**
     * 返回给定的 Bean 类是否声明或继承任何非 void 返回的 Bean 属性或索引属性设置方法。
     */
    private boolean supports(Class<?> beanClass) {
        for (Method method : beanClass.getMethods()) {
            if (ExtendedBeanInfo.isCandidateWriteMethod(method)) {
                return true;
            }
        }
        return false;
    }
}
