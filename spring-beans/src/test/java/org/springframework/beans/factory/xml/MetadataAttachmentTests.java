// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可，除非法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下链接处获得许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可协议下分发的软件按 "原样" 提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可协议了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Rob Harrop
 */
public class MetadataAttachmentTests {

    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    public void setUp() throws Exception {
        this.beanFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(this.beanFactory).loadBeanDefinitions(new ClassPathResource("withMeta.xml", getClass()));
    }

    @Test
    public void metadataAttachment() throws Exception {
        BeanDefinition beanDefinition1 = this.beanFactory.getMergedBeanDefinition("testBean1");
        assertThat(beanDefinition1.getAttribute("foo")).isEqualTo("bar");
    }

    @Test
    public void metadataIsInherited() throws Exception {
        BeanDefinition beanDefinition = this.beanFactory.getMergedBeanDefinition("testBean2");
        assertThat(beanDefinition.getAttribute("foo")).as("Metadata not inherited").isEqualTo("bar");
        assertThat(beanDefinition.getAttribute("abc")).as("Child metdata not attached").isEqualTo("123");
    }

    @Test
    public void propertyMetadata() throws Exception {
        BeanDefinition beanDefinition = this.beanFactory.getMergedBeanDefinition("testBean3");
        PropertyValue pv = beanDefinition.getPropertyValues().getPropertyValue("name");
        assertThat(pv.getAttribute("surname")).isEqualTo("Harrop");
    }
}
