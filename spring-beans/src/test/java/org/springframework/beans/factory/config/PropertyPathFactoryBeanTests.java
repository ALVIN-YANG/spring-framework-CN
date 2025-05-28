// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.testfixture.beans.ITestBean;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.Resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.testfixture.io.ResourceTestUtils.qualifiedResource;

/**
 * 对 {@link PropertyPathFactoryBean} 的单元测试。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2004年10月4日
 */
public class PropertyPathFactoryBeanTests {

    private static final Resource CONTEXT = qualifiedResource(PropertyPathFactoryBeanTests.class, "context.xml");

    @Test
    public void testPropertyPathFactoryBeanWithSingletonResult() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(xbf).loadBeanDefinitions(CONTEXT);
        assertThat(xbf.getBean("propertyPath1")).isEqualTo(12);
        assertThat(xbf.getBean("propertyPath2")).isEqualTo(11);
        assertThat(xbf.getBean("tb.age")).isEqualTo(10);
        assertThat(xbf.getType("otb.spouse")).isEqualTo(ITestBean.class);
        Object result1 = xbf.getBean("otb.spouse");
        Object result2 = xbf.getBean("otb.spouse");
        assertThat(result1 instanceof TestBean).isTrue();
        assertThat(result1).isSameAs(result2);
        assertThat(((TestBean) result1).getAge()).isEqualTo(99);
    }

    @Test
    public void testPropertyPathFactoryBeanWithPrototypeResult() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(xbf).loadBeanDefinitions(CONTEXT);
        assertThat(xbf.getType("tb.spouse")).isNull();
        assertThat(xbf.getType("propertyPath3")).isEqualTo(TestBean.class);
        Object result1 = xbf.getBean("tb.spouse");
        Object result2 = xbf.getBean("propertyPath3");
        Object result3 = xbf.getBean("propertyPath3");
        assertThat(result1 instanceof TestBean).isTrue();
        assertThat(result2 instanceof TestBean).isTrue();
        assertThat(result3 instanceof TestBean).isTrue();
        assertThat(((TestBean) result1).getAge()).isEqualTo(11);
        assertThat(((TestBean) result2).getAge()).isEqualTo(11);
        assertThat(((TestBean) result3).getAge()).isEqualTo(11);
        assertThat(result1).isNotSameAs(result2);
        assertThat(result1).isNotSameAs(result3);
        assertThat(result2).isNotSameAs(result3);
    }

    @Test
    public void testPropertyPathFactoryBeanWithNullResult() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(xbf).loadBeanDefinitions(CONTEXT);
        assertThat(xbf.getType("tb.spouse.spouse")).isNull();
        assertThat(xbf.getBean("tb.spouse.spouse").toString()).isEqualTo("null");
    }

    @Test
    public void testPropertyPathFactoryBeanAsInnerBean() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(xbf).loadBeanDefinitions(CONTEXT);
        TestBean spouse = (TestBean) xbf.getBean("otb.spouse");
        TestBean tbWithInner = (TestBean) xbf.getBean("tbWithInner");
        assertThat(tbWithInner.getSpouse()).isSameAs(spouse);
        assertThat(!tbWithInner.getFriends().isEmpty()).isTrue();
        assertThat(tbWithInner.getFriends().iterator().next()).isSameAs(spouse);
    }

    @Test
    public void testPropertyPathFactoryBeanAsNullReference() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(xbf).loadBeanDefinitions(CONTEXT);
        assertThat(xbf.getBean("tbWithNullReference", TestBean.class).getSpouse()).isNull();
    }

    @Test
    public void testPropertyPathFactoryBeanAsInnerNull() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(xbf).loadBeanDefinitions(CONTEXT);
        assertThat(xbf.getBean("tbWithInnerNull", TestBean.class).getSpouse()).isNull();
    }
}
