// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“许可证”），除非法律规定或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解管理许可权和限制的具体语言。*/
package org.springframework.beans.testfixture.beans.factory.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class PrivateMethodInjectionSample {

    @SuppressWarnings("unused")
    private Environment environment;

    @Autowired
    private void setTestBean(Environment environment) {
        this.environment = environment;
    }
}
