// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或适用于特定目的的保证。
* 请参阅许可证了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory.aot;

/**
 * 测试枚举，包含类体。
 *
 * @author Phillip Webb
 */
public enum EnumWithClassBody {

    /**
     * 没有类体。
     */
    ONE,
    /**
     * 带有类体。
     */
    TWO {

        @Override
        public String toString() {
            return "2";
        }
    }

}
