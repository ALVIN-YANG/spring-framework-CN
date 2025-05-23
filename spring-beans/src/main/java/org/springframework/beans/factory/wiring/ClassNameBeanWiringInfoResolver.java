// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或原作者。
*
* 根据 Apache License 2.0 版本（以下简称“许可证”）授权；
* 您不得使用此文件除非遵守许可证规定。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”提供的，没有任何形式的明示或暗示保证，
* 请参阅许可证以了解具体的管理权限和限制。*/
package org.springframework.beans.factory.wiring;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 简单的默认实现接口 {@link BeanWiringInfoResolver}，
 * 寻找与完全限定类名相同的bean。
 * 如果bean标签的"id"属性未使用，则与Spring XML文件中bean的默认名称匹配。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ClassNameBeanWiringInfoResolver implements BeanWiringInfoResolver {

    @Override
    public BeanWiringInfo resolveWiringInfo(Object beanInstance) {
        Assert.notNull(beanInstance, "Bean instance must not be null");
        return new BeanWiringInfo(ClassUtils.getUserClass(beanInstance).getName(), true);
    }
}
