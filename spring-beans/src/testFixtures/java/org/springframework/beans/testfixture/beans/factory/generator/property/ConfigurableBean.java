// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 您不得使用此文件除非符合许可证。您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的或不侵犯他人权利的保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.testfixture.beans.factory.generator.property;

public class ConfigurableBean {

    @SuppressWarnings("unused")
    private String name;

    @SuppressWarnings("unused")
    private Integer counter;

    public void setName(String name) {
        this.name = name;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }
}
