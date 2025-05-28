// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（"许可证"）进行许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.testfixture.beans.factory.generator.factory;

public abstract class SampleFactory {

    public static String create(String testBean) {
        return testBean;
    }

    public static String create(char character) {
        return String.valueOf(character);
    }

    public static String create(Number number, String test) {
        return number + test;
    }

    public static String create(Class<?> type) {
        return type.getName();
    }

    public static Integer integerBean() {
        return 42;
    }
}
