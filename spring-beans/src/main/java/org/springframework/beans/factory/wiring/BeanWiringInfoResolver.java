// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据Apache许可证版本2.0（以下简称“许可证”）授权；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.wiring;

import org.springframework.lang.Nullable;

/**
 * 策略接口，由可以解析给定新实例化的bean对象的bean名称信息的对象实现。对接口中`resolveWiringInfo`方法的调用将由相关具体方面的AspectJ切入点驱动。
 *
 * <p>元数据解析策略可以是可插拔的。一个好的默认实现是`ClassNameBeanWiringInfoResolver`，它使用完全限定的类名作为bean名称。
 *
 * @author Rod Johnson
 * @since 2.0
 * @see BeanWiringInfo
 * @see ClassNameBeanWiringInfoResolver
 * @see org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver
 */
public interface BeanWiringInfoResolver {

    /**
     * 解析给定 Bean 实例的 BeanWiringInfo。
     * @param beanInstance 要解析信息的 Bean 实例
     * @return 返回 BeanWiringInfo，如果没有找到则返回 {@code null}
     */
    @Nullable
    BeanWiringInfo resolveWiringInfo(Object beanInstance);
}
