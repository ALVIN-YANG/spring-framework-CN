// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获得许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体的管理权限和限制。*/
package org.springframework.beans.testfixture.beans.factory.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class PackagePrivateFieldInjectionSample {

    @Autowired
    Environment environment;
}
