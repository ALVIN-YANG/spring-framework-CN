// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 进行许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接处获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可协议，了解具体规定许可权限和限制。*/
package org.springframework.beans.testfixture.beans.factory.generator.lifecycle;

public class InitDestroyBean {

    @Init
    public void initMethod() {
    }

    public void customInitMethod() {
    }

    @Destroy
    public void destroyMethod() {
    }

    public void customDestroyMethod() {
    }
}
