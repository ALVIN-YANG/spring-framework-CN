// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件，除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、特定用途适用性和非侵权性。
* 请参阅许可证，了解具体规定许可权和限制。*/
package org.springframework.beans.factory.xml;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集合合并支持的单元和集成测试。
 *
 * @author Rob Harrop
 * @author Rick Evans
 */
@SuppressWarnings("rawtypes")
public class CollectionMergingTests {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    @BeforeEach
    public void setUp() throws Exception {
        BeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
        reader.loadBeanDefinitions(new ClassPathResource("collectionMerging.xml", getClass()));
    }

    @Test
    public void mergeList() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithList");
        List<?> list = bean.getSomeList();
        assertThat(list).as("Incorrect size").hasSize(3);
        assertThat(list.get(0)).isEqualTo("Rob Harrop");
        assertThat(list.get(1)).isEqualTo("Rod Johnson");
        assertThat(list.get(2)).isEqualTo("Juergen Hoeller");
    }

    @Test
    public void mergeListWithInnerBeanAsListElement() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithListOfRefs");
        List<?> list = bean.getSomeList();
        assertThat(list).isNotNull();
        assertThat(list).hasSize(3);
        assertThat(list.get(2) instanceof TestBean).isTrue();
    }

    @Test
    public void mergeSet() {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithSet");
        Set<?> set = bean.getSomeSet();
        assertThat(set).as("Incorrect size").hasSize(2);
        assertThat(set.contains("Rob Harrop")).isTrue();
        assertThat(set.contains("Sally Greenwood")).isTrue();
    }

    @Test
    public void mergeSetWithInnerBeanAsSetElement() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithSetOfRefs");
        Set<?> set = bean.getSomeSet();
        assertThat(set).isNotNull();
        assertThat(set).hasSize(2);
        Iterator it = set.iterator();
        it.next();
        Object o = it.next();
        assertThat(o instanceof TestBean).isTrue();
        assertThat(((TestBean) o).getName()).isEqualTo("Sally");
    }

    @Test
    public void mergeMap() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithMap");
        Map<?, ?> map = bean.getSomeMap();
        assertThat(map).as("Incorrect size").hasSize(3);
        assertThat(map.get("Rob")).isEqualTo("Sally");
        assertThat(map.get("Rod")).isEqualTo("Kerry");
        assertThat(map.get("Juergen")).isEqualTo("Eva");
    }

    @Test
    public void mergeMapWithInnerBeanAsMapEntryValue() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithMapOfRefs");
        Map<?, ?> map = bean.getSomeMap();
        assertThat(map).isNotNull();
        assertThat(map).hasSize(2);
        assertThat(map.get("Rob")).isNotNull();
        assertThat(map.get("Rob") instanceof TestBean).isTrue();
        assertThat(((TestBean) map.get("Rob")).getName()).isEqualTo("Sally");
    }

    @Test
    public void mergeProperties() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithProps");
        Properties props = bean.getSomeProperties();
        assertThat(props).as("Incorrect size").hasSize(3);
        assertThat(props.getProperty("Rob")).isEqualTo("Sally");
        assertThat(props.getProperty("Rod")).isEqualTo("Kerry");
        assertThat(props.getProperty("Juergen")).isEqualTo("Eva");
    }

    @Test
    public void mergeListInConstructor() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithListInConstructor");
        List<?> list = bean.getSomeList();
        assertThat(list).as("Incorrect size").hasSize(3);
        assertThat(list.get(0)).isEqualTo("Rob Harrop");
        assertThat(list.get(1)).isEqualTo("Rod Johnson");
        assertThat(list.get(2)).isEqualTo("Juergen Hoeller");
    }

    @Test
    public void mergeListWithInnerBeanAsListElementInConstructor() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithListOfRefsInConstructor");
        List<?> list = bean.getSomeList();
        assertThat(list).isNotNull();
        assertThat(list).hasSize(3);
        assertThat(list.get(2)).isNotNull();
        assertThat(list.get(2) instanceof TestBean).isTrue();
    }

    @Test
    public void mergeSetInConstructor() {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithSetInConstructor");
        Set<?> set = bean.getSomeSet();
        assertThat(set).as("Incorrect size").hasSize(2);
        assertThat(set.contains("Rob Harrop")).isTrue();
        assertThat(set.contains("Sally Greenwood")).isTrue();
    }

    @Test
    public void mergeSetWithInnerBeanAsSetElementInConstructor() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithSetOfRefsInConstructor");
        Set<?> set = bean.getSomeSet();
        assertThat(set).isNotNull();
        assertThat(set).hasSize(2);
        Iterator it = set.iterator();
        it.next();
        Object o = it.next();
        assertThat(o instanceof TestBean).isTrue();
        assertThat(((TestBean) o).getName()).isEqualTo("Sally");
    }

    @Test
    public void mergeMapInConstructor() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithMapInConstructor");
        Map<?, ?> map = bean.getSomeMap();
        assertThat(map).as("Incorrect size").hasSize(3);
        assertThat(map.get("Rob")).isEqualTo("Sally");
        assertThat(map.get("Rod")).isEqualTo("Kerry");
        assertThat(map.get("Juergen")).isEqualTo("Eva");
    }

    @Test
    public void mergeMapWithInnerBeanAsMapEntryValueInConstructor() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithMapOfRefsInConstructor");
        Map<?, ?> map = bean.getSomeMap();
        assertThat(map).isNotNull();
        assertThat(map).hasSize(2);
        assertThat(map.get("Rob") instanceof TestBean).isTrue();
        assertThat(((TestBean) map.get("Rob")).getName()).isEqualTo("Sally");
    }

    @Test
    public void mergePropertiesInConstructor() throws Exception {
        TestBean bean = (TestBean) this.beanFactory.getBean("childWithPropsInConstructor");
        Properties props = bean.getSomeProperties();
        assertThat(props).as("Incorrect size").hasSize(3);
        assertThat(props.getProperty("Rob")).isEqualTo("Sally");
        assertThat(props.getProperty("Rod")).isEqualTo("Kerry");
        assertThat(props.getProperty("Juergen")).isEqualTo("Eva");
    }
}
