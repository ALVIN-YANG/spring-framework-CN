// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”），您可以使用此文件，但必须遵守许可证规定。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或适用于特定目的的保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.testfixture.beans.factory.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class PackagePrivateMethodInjectionSample {

    public Environment environment;

    @Autowired
    void setTestBean(Environment environment) {
        this.environment = environment;
    }
}
