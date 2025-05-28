// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache 许可证 2.0 版（"许可证"），除非法律法规要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律法规或经书面同意，否则在许可证下分发的软件按照"原样"提供，
* 不提供任何明示或暗示的保证或条件，无论是关于适销性还是特定用途的适用性。
* 请参阅许可证以获取关于许可权限和限制的特定语言规定。*/
package org.springframework.beans.testfixture.beans.factory.generator;

import java.io.IOException;

public class SimpleConfiguration {

    public SimpleConfiguration() {
    }

    public String stringBean() {
        return "Hello";
    }

    @SuppressWarnings("unused")
    private static String privateStaticStringBean() {
        return "Hello";
    }

    static String packageStaticStringBean() {
        return "Hello";
    }

    public static Integer integerBean() {
        return 42;
    }

    public Integer throwingIntegerBean() throws IOException {
        return 42;
    }
}
