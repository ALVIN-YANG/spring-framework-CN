// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照“现状”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵权。
* 请参阅许可证以了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.testfixture.beans.ITestBean;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @作者 Rob Harrop
 * @作者 Juergen Hoeller
 * @作者 Arjen Poutsma
 */
public class SimplePropertyNamespaceHandlerTests {

    @Test
    public void simpleBeanConfigured() throws Exception {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(beanFactory).loadBeanDefinitions(new ClassPathResource("simplePropertyNamespaceHandlerTests.xml", getClass()));
        ITestBean rob = (TestBean) beanFactory.getBean("rob");
        ITestBean sally = (TestBean) beanFactory.getBean("sally");
        assertThat(rob.getName()).isEqualTo("Rob Harrop");
        assertThat(rob.getAge()).isEqualTo(24);
        assertThat(sally).isEqualTo(rob.getSpouse());
    }

    @Test
    public void innerBeanConfigured() throws Exception {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(beanFactory).loadBeanDefinitions(new ClassPathResource("simplePropertyNamespaceHandlerTests.xml", getClass()));
        TestBean sally = (TestBean) beanFactory.getBean("sally2");
        ITestBean rob = sally.getSpouse();
        assertThat(rob.getName()).isEqualTo("Rob Harrop");
        assertThat(rob.getAge()).isEqualTo(24);
        assertThat(sally).isEqualTo(rob.getSpouse());
    }

    @Test
    public void withPropertyDefinedTwice() throws Exception {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        assertThatExceptionOfType(BeanDefinitionStoreException.class).isThrownBy(() -> new XmlBeanDefinitionReader(beanFactory).loadBeanDefinitions(new ClassPathResource("simplePropertyNamespaceHandlerTestsWithErrors.xml", getClass())));
    }

    @Test
    public void propertyWithNameEndingInRef() throws Exception {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(beanFactory).loadBeanDefinitions(new ClassPathResource("simplePropertyNamespaceHandlerTests.xml", getClass()));
        ITestBean sally = (TestBean) beanFactory.getBean("derivedSally");
        assertThat(sally.getSpouse().getName()).isEqualTo("r");
    }
}
