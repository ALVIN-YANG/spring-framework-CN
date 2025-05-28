// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.context.annotation.aspectj;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.aspectj.ShouldBeConfiguredBySpring;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 @EnableSpringConfigured 是否能够正确注册
 * 一个 {@link org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect}，
 * 就像使用 {@code <context:spring-configured>} 一样。
 *
 * @author Chris Beams
 * @since 3.1
 */
public class AnnotationBeanConfigurerTests {

    @Test
    public void injection() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class)) {
            ShouldBeConfiguredBySpring myObject = new ShouldBeConfiguredBySpring();
            assertThat(myObject.getName()).isEqualTo("Rod");
        }
    }

    @Configuration
    @ImportResource("org/springframework/beans/factory/aspectj/beanConfigurerTests-beans.xml")
    @EnableSpringConfigured
    static class Config {
    }
}
