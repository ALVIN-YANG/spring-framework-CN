// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.testfixture.beans.factory.generator.injection;

import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("unused")
public class InjectionComponent {

    private final String bean;

    private Integer counter;

    public InjectionComponent(String bean) {
        this.bean = bean;
    }

    @Autowired(required = false)
    public void setCounter(Integer counter) {
        this.counter = counter;
    }
}
