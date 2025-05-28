// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可协议下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、 merchantability 或特定用途的适用性。
* 请参阅许可协议了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory.aspectj;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Chris Beams
 */
public class XmlBeanConfigurerTests {

    @Test
    public void injection() {
        try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("org/springframework/beans/factory/aspectj/beanConfigurerTests.xml")) {
            ShouldBeConfiguredBySpring myObject = new ShouldBeConfiguredBySpring();
            assertThat(myObject.getName()).isEqualTo("Rod");
        }
    }
}
