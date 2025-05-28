// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.beans.testfixture.beans.factory.aot;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;

/**
 * 一个具有泛型类型的公共 {@link FactoryBean}。
 *
 * @author Stephane Nicoll
 */
public class GenericFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> beanType;

    public GenericFactoryBean(Class<T> beanType) {
        this.beanType = beanType;
    }

    @Nullable
    @Override
    public T getObject() throws Exception {
        return BeanUtils.instantiateClass(this.beanType);
    }

    @Nullable
    @Override
    public Class<?> getObjectType() {
        return this.beanType;
    }
}
