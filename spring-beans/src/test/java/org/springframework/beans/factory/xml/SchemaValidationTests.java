// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者们。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @作者 Rob Harrop
 */
public class SchemaValidationTests {

    @Test
    public void withAutodetection() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        assertThatExceptionOfType(BeansException.class).isThrownBy(() -> reader.loadBeanDefinitions(new ClassPathResource("invalidPerSchema.xml", getClass()))).withCauseInstanceOf(SAXParseException.class);
    }

    @Test
    public void withExplicitValidationMode() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
        assertThatExceptionOfType(BeansException.class).isThrownBy(() -> reader.loadBeanDefinitions(new ClassPathResource("invalidPerSchema.xml", getClass()))).withCauseInstanceOf(SAXParseException.class);
    }

    @Test
    public void loadDefinitions() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
        reader.loadBeanDefinitions(new ClassPathResource("schemaValidated.xml", getClass()));
        TestBean foo = (TestBean) bf.getBean("fooBean");
        assertThat(foo.getSpouse()).as("Spouse is null").isNotNull();
        assertThat(foo.getFriends().size()).as("Incorrect number of friends").isEqualTo(2);
    }
}
