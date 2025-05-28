// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关许可协议对权限和限制的具体语言，请参阅许可协议。*/
package org.springframework.beans.testfixture.beans.factory.generator.factory;

import java.io.Serializable;

/**
 * 一个具有泛型类型的示例对象。
 *
 * @param <T> 数字类型
 * @author Stephane Nicoll
 */
@SuppressWarnings("serial")
public class NumberHolder<T extends Number> implements Serializable {

    @SuppressWarnings("unused")
    private final T number;

    public NumberHolder(T number) {
        this.number = number;
    }
}
