// 翻译完成 glm-4-flash
/** 版权所有 2002-2014 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.factory.aspectj;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable("configuredBean")
@SuppressWarnings("serial")
public class ShouldBeConfiguredBySpring implements Serializable {

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
