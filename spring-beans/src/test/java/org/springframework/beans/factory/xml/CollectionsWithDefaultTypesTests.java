// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.xml;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class CollectionsWithDefaultTypesTests {

    private final DefaultListableBeanFactory beanFactory;

    public CollectionsWithDefaultTypesTests() {
        this.beanFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(this.beanFactory).loadBeanDefinitions(new ClassPathResource("collectionsWithDefaultTypes.xml", getClass()));
    }

    @Test
    public void testListHasDefaultType() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
        for (Object o : bean.getSomeList()) {
            assertThat(o.getClass()).as("Value type is incorrect").isEqualTo(Integer.class);
        }
    }

    @Test
    public void testSetHasDefaultType() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
        for (Object o : bean.getSomeSet()) {
            assertThat(o.getClass()).as("Value type is incorrect").isEqualTo(Integer.class);
        }
    }

    @Test
    public void testMapHasDefaultKeyAndValueType() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
        assertMap(bean.getSomeMap());
    }

    @Test
    public void testMapWithNestedElementsHasDefaultKeyAndValueType() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("testBean2");
        assertMap(bean.getSomeMap());
    }

    @SuppressWarnings("rawtypes")
    private void assertMap(Map<?, ?> map) {
        for (Map.Entry entry : map.entrySet()) {
            assertThat(entry.getKey().getClass()).as("Key type is incorrect").isEqualTo(Integer.class);
            assertThat(entry.getValue().getClass()).as("Value type is incorrect").isEqualTo(Boolean.class);
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testBuildCollectionFromMixtureOfReferencesAndValues() throws Exception {
        MixedCollectionBean jumble = (MixedCollectionBean) this.beanFactory.getBean("jumble");
        assertThat(jumble.getJumble()).as("Expected 3 elements, not " + jumble.getJumble().size()).hasSize(3);
        List l = (List) jumble.getJumble();
        assertThat(l.get(0).equals("literal")).isTrue();
        Integer[] array1 = (Integer[]) l.get(1);
        assertThat(array1[0].equals(2)).isTrue();
        assertThat(array1[1].equals(4)).isTrue();
        int[] array2 = (int[]) l.get(2);
        assertThat(array2[0]).isEqualTo(3);
        assertThat(array2[1]).isEqualTo(5);
    }
}
