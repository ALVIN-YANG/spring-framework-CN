// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按 "原样" 分发，不提供任何形式的明示或暗示保证。
* 请参阅许可协议了解具体语言管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.sql.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.testfixture.io.ResourceTestUtils.qualifiedResource;

/**
 * 对{@link FieldRetrievingFactoryBean}的单元测试。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2004年7月31日
 */
public class FieldRetrievingFactoryBeanTests {

    @Test
    public void testStaticField() throws Exception {
        FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
        fr.setStaticField("java.sql.Connection.TRANSACTION_SERIALIZABLE");
        fr.afterPropertiesSet();
        assertThat(fr.getObject()).isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
    }

    @Test
    public void testStaticFieldWithWhitespace() throws Exception {
        FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
        fr.setStaticField("  java.sql.Connection.TRANSACTION_SERIALIZABLE  ");
        fr.afterPropertiesSet();
        assertThat(fr.getObject()).isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
    }

    @Test
    public void testStaticFieldViaClassAndFieldName() throws Exception {
        FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
        fr.setTargetClass(Connection.class);
        fr.setTargetField("TRANSACTION_SERIALIZABLE");
        fr.afterPropertiesSet();
        assertThat(fr.getObject()).isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
    }

    @Test
    public void testNonStaticField() throws Exception {
        FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
        PublicFieldHolder target = new PublicFieldHolder();
        fr.setTargetObject(target);
        fr.setTargetField("publicField");
        fr.afterPropertiesSet();
        assertThat(fr.getObject()).isEqualTo(target.publicField);
    }

    @Test
    public void testNothingButBeanName() throws Exception {
        FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
        fr.setBeanName("java.sql.Connection.TRANSACTION_SERIALIZABLE");
        fr.afterPropertiesSet();
        assertThat(fr.getObject()).isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
    }

    @Test
    public void testJustTargetField() throws Exception {
        FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
        fr.setTargetField("TRANSACTION_SERIALIZABLE");
        try {
            fr.afterPropertiesSet();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testJustTargetClass() throws Exception {
        FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
        fr.setTargetClass(Connection.class);
        try {
            fr.afterPropertiesSet();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testJustTargetObject() throws Exception {
        FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
        fr.setTargetObject(new PublicFieldHolder());
        try {
            fr.afterPropertiesSet();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testWithConstantOnClassWithPackageLevelVisibility() throws Exception {
        FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
        fr.setBeanName("org.springframework.beans.testfixture.beans.PackageLevelVisibleBean.CONSTANT");
        fr.afterPropertiesSet();
        assertThat(fr.getObject()).isEqualTo("Wuby");
    }

    @Test
    public void testBeanNameSyntaxWithBeanFactory() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(qualifiedResource(FieldRetrievingFactoryBeanTests.class, "context.xml"));
        TestBean testBean = (TestBean) bf.getBean("testBean");
        assertThat(testBean.getSomeIntegerArray()[0]).isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
        assertThat(testBean.getSomeIntegerArray()[1]).isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
    }

    private static class PublicFieldHolder {

        public String publicField = "test";
    }
}
