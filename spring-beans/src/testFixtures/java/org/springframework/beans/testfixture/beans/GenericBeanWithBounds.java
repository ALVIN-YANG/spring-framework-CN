// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 您必须遵守许可证才能使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可权和限制。*/
package org.springframework.beans.testfixture.beans;

public class GenericBeanWithBounds<T extends Person> {

    @SafeVarargs
    public final void process(T... persons) {
    }
}
