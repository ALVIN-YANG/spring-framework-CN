// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.testfixture.beans.factory.generator;

import org.springframework.core.env.Environment;

public class InnerComponentConfiguration {

    public class NoDependencyComponent {

        public NoDependencyComponent() {
        }
    }

    public class EnvironmentAwareComponent {

        public EnvironmentAwareComponent(Environment environment) {
        }
    }
}
