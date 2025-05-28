// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非适用法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 Spring XML 中对新的嵌套 beans 元素的支持
 *
 * @author Chris Beams
 */
public class NestedBeansElementTests {

    private final Resource XML = new ClassPathResource("NestedBeansElementTests-context.xml", this.getClass());

    @Test
    public void getBean_withoutActiveProfile() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(XML);
        Object foo = bf.getBean("foo");
        assertThat(foo).isInstanceOf(String.class);
    }

    @Test
    public void getBean_withActiveProfile() {
        ConfigurableEnvironment env = new StandardEnvironment();
        env.setActiveProfiles("dev");
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.setEnvironment(env);
        reader.loadBeanDefinitions(XML);
        // 不应该抛出 NSBDE
        bf.getBean("devOnlyBean");
        Object foo = bf.getBean("foo");
        assertThat(foo).isInstanceOf(Integer.class);
        bf.getBean("devOnlyBean");
    }
}
