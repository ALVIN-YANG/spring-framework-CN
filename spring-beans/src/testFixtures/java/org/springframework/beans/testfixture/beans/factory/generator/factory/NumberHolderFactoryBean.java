// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.testfixture.beans.factory.generator.factory;

import org.springframework.beans.factory.FactoryBean;

/**
 * 一个具有泛型类型的示例工厂Bean。
 *
 * @param <T> 由该工厂Bean生成的数字的类型
 * @author Stephane Nicoll
 */
public class NumberHolderFactoryBean<T extends Number> implements FactoryBean<NumberHolder<T>> {

    private T number;

    public void setNumber(T number) {
        this.number = number;
    }

    @Override
    public NumberHolder<T> getObject() {
        return new NumberHolder<>(this.number);
    }

    @Override
    public Class<?> getObjectType() {
        return NumberHolder.class;
    }
}
