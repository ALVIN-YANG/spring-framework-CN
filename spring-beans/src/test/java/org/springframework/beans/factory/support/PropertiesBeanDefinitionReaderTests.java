// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Rob Harrop
 */
@SuppressWarnings("deprecation")
class PropertiesBeanDefinitionReaderTests {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    private final PropertiesBeanDefinitionReader reader = new PropertiesBeanDefinitionReader(this.beanFactory);

    @Test
    void withSimpleConstructorArg() {
        this.reader.loadBeanDefinitions(new ClassPathResource("simpleConstructorArg.properties", getClass()));
        TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
        assertThat(bean.getName()).isEqualTo("Rob Harrop");
    }

    @Test
    void withConstructorArgRef() {
        this.reader.loadBeanDefinitions(new ClassPathResource("refConstructorArg.properties", getClass()));
        TestBean rob = (TestBean) this.beanFactory.getBean("rob");
        TestBean sally = (TestBean) this.beanFactory.getBean("sally");
        assertThat(rob.getSpouse()).isEqualTo(sally);
    }

    @Test
    void withMultipleConstructorsArgs() {
        this.reader.loadBeanDefinitions(new ClassPathResource("multiConstructorArgs.properties", getClass()));
        TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
        assertThat(bean.getName()).isEqualTo("Rob Harrop");
        assertThat(bean.getAge()).isEqualTo(23);
    }
}
