// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据Apache许可证版本2.0（"许可证"）进行许可；
* 您必须遵守许可证才能使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式，明示或暗示的保证。
* 请参阅许可证，了解具体的权限和限制条款。*/
package org.springframework.beans.testfixture.beans;

@SuppressWarnings("unused")
public class TestBeanWithPackagePrivateMethod {

    private int age;

    void setAge(int age) {
        this.age = age;
    }
}
