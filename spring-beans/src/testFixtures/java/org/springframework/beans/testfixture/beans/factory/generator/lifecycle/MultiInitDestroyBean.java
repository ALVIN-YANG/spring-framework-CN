// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“协议”）许可；
* 您不得使用此文件除非符合本协议。您可以在以下链接获取协议副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在协议许可下分发的软件
* 是按“原样”分发的，不提供任何形式的质量保证或条件，无论是明示的还是暗示的。
* 请参阅协议以了解具体规定许可和限制。*/
package org.springframework.beans.testfixture.beans.factory.generator.lifecycle;

public class MultiInitDestroyBean extends InitDestroyBean {

    @Init
    void anotherInitMethod() {
    }

    @Destroy
    void anotherDestroyMethod() {
    }
}
