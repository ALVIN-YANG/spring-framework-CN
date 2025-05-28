// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可；
* 除非遵守许可协议，否则不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 包括但不限于适销性、特定用途的适用性或不侵犯权利。
* 请参阅许可协议了解具体语言规范、权限和限制。*/
package org.springframework.beans.testfixture.beans.factory.aot;

/**
 * 带有内部类的配置。
 *
 * @author Stephane Nicoll
 */
public class InnerBeanConfiguration {

    public static class Simple {

        public SimpleBean simpleBean() {
            return new SimpleBean();
        }

        public static class Another {

            public SimpleBean anotherBean() {
                return new SimpleBean();
            }
        }
    }
}
