// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据Apache License，版本2.0（以下简称“许可”）进行许可；
* 除非符合许可，否则您不得使用此文件。
* 您可以在以下地址获取许可副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可下分发的软件按“原样”分发，
* 不提供任何形式（明示或暗示）的保证或条件。
* 请参阅许可，了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Rob Harrop
 * @作者 Juergen Hoeller
 */
public class AutowireWithExclusionTests {

    @Test
    public void byTypeAutowireWithAutoSelfExclusion() throws Exception {
        CountingFactory.reset();
        DefaultListableBeanFactory beanFactory = getBeanFactory("autowire-with-exclusion.xml");
        beanFactory.preInstantiateSingletons();
        TestBean rob = (TestBean) beanFactory.getBean("rob");
        TestBean sally = (TestBean) beanFactory.getBean("sally");
        assertThat(rob.getSpouse()).isEqualTo(sally);
        assertThat(CountingFactory.getFactoryBeanInstanceCount()).isEqualTo(1);
    }

    @Test
    public void byTypeAutowireWithExclusion() throws Exception {
        CountingFactory.reset();
        DefaultListableBeanFactory beanFactory = getBeanFactory("autowire-with-exclusion.xml");
        beanFactory.preInstantiateSingletons();
        TestBean rob = (TestBean) beanFactory.getBean("rob");
        assertThat(rob.getSomeProperties().getProperty("name")).isEqualTo("props1");
        assertThat(CountingFactory.getFactoryBeanInstanceCount()).isEqualTo(1);
    }

    @Test
    public void byTypeAutowireWithExclusionInParentFactory() throws Exception {
        CountingFactory.reset();
        DefaultListableBeanFactory parent = getBeanFactory("autowire-with-exclusion.xml");
        parent.preInstantiateSingletons();
        DefaultListableBeanFactory child = new DefaultListableBeanFactory(parent);
        RootBeanDefinition robDef = new RootBeanDefinition(TestBean.class);
        robDef.setAutowireMode(RootBeanDefinition.AUTOWIRE_BY_TYPE);
        robDef.getPropertyValues().add("spouse", new RuntimeBeanReference("sally"));
        child.registerBeanDefinition("rob2", robDef);
        TestBean rob = (TestBean) child.getBean("rob2");
        assertThat(rob.getSomeProperties().getProperty("name")).isEqualTo("props1");
        assertThat(CountingFactory.getFactoryBeanInstanceCount()).isEqualTo(1);
    }

    @Test
    public void byTypeAutowireWithPrimaryInParentFactory() throws Exception {
        CountingFactory.reset();
        DefaultListableBeanFactory parent = getBeanFactory("autowire-with-exclusion.xml");
        parent.getBeanDefinition("props1").setPrimary(true);
        parent.preInstantiateSingletons();
        DefaultListableBeanFactory child = new DefaultListableBeanFactory(parent);
        RootBeanDefinition robDef = new RootBeanDefinition(TestBean.class);
        robDef.setAutowireMode(RootBeanDefinition.AUTOWIRE_BY_TYPE);
        robDef.getPropertyValues().add("spouse", new RuntimeBeanReference("sally"));
        child.registerBeanDefinition("rob2", robDef);
        RootBeanDefinition propsDef = new RootBeanDefinition(PropertiesFactoryBean.class);
        propsDef.getPropertyValues().add("properties", "name=props3");
        child.registerBeanDefinition("props3", propsDef);
        TestBean rob = (TestBean) child.getBean("rob2");
        assertThat(rob.getSomeProperties().getProperty("name")).isEqualTo("props1");
        assertThat(CountingFactory.getFactoryBeanInstanceCount()).isEqualTo(1);
    }

    @Test
    public void byTypeAutowireWithPrimaryOverridingParentFactory() throws Exception {
        CountingFactory.reset();
        DefaultListableBeanFactory parent = getBeanFactory("autowire-with-exclusion.xml");
        parent.preInstantiateSingletons();
        DefaultListableBeanFactory child = new DefaultListableBeanFactory(parent);
        RootBeanDefinition robDef = new RootBeanDefinition(TestBean.class);
        robDef.setAutowireMode(RootBeanDefinition.AUTOWIRE_BY_TYPE);
        robDef.getPropertyValues().add("spouse", new RuntimeBeanReference("sally"));
        child.registerBeanDefinition("rob2", robDef);
        RootBeanDefinition propsDef = new RootBeanDefinition(PropertiesFactoryBean.class);
        propsDef.getPropertyValues().add("properties", "name=props3");
        propsDef.setPrimary(true);
        child.registerBeanDefinition("props3", propsDef);
        TestBean rob = (TestBean) child.getBean("rob2");
        assertThat(rob.getSomeProperties().getProperty("name")).isEqualTo("props3");
        assertThat(CountingFactory.getFactoryBeanInstanceCount()).isEqualTo(1);
    }

    @Test
    public void byTypeAutowireWithPrimaryInParentAndChild() throws Exception {
        CountingFactory.reset();
        DefaultListableBeanFactory parent = getBeanFactory("autowire-with-exclusion.xml");
        parent.getBeanDefinition("props1").setPrimary(true);
        parent.preInstantiateSingletons();
        DefaultListableBeanFactory child = new DefaultListableBeanFactory(parent);
        RootBeanDefinition robDef = new RootBeanDefinition(TestBean.class);
        robDef.setAutowireMode(RootBeanDefinition.AUTOWIRE_BY_TYPE);
        robDef.getPropertyValues().add("spouse", new RuntimeBeanReference("sally"));
        child.registerBeanDefinition("rob2", robDef);
        RootBeanDefinition propsDef = new RootBeanDefinition(PropertiesFactoryBean.class);
        propsDef.getPropertyValues().add("properties", "name=props3");
        propsDef.setPrimary(true);
        child.registerBeanDefinition("props3", propsDef);
        TestBean rob = (TestBean) child.getBean("rob2");
        assertThat(rob.getSomeProperties().getProperty("name")).isEqualTo("props3");
        assertThat(CountingFactory.getFactoryBeanInstanceCount()).isEqualTo(1);
    }

    @Test
    public void byTypeAutowireWithInclusion() throws Exception {
        CountingFactory.reset();
        DefaultListableBeanFactory beanFactory = getBeanFactory("autowire-with-inclusion.xml");
        beanFactory.preInstantiateSingletons();
        TestBean rob = (TestBean) beanFactory.getBean("rob");
        assertThat(rob.getSomeProperties().getProperty("name")).isEqualTo("props1");
        assertThat(CountingFactory.getFactoryBeanInstanceCount()).isEqualTo(1);
    }

    @Test
    public void byTypeAutowireWithSelectiveInclusion() throws Exception {
        CountingFactory.reset();
        DefaultListableBeanFactory beanFactory = getBeanFactory("autowire-with-selective-inclusion.xml");
        beanFactory.preInstantiateSingletons();
        TestBean rob = (TestBean) beanFactory.getBean("rob");
        assertThat(rob.getSomeProperties().getProperty("name")).isEqualTo("props1");
        assertThat(CountingFactory.getFactoryBeanInstanceCount()).isEqualTo(1);
    }

    @Test
    public void constructorAutowireWithAutoSelfExclusion() throws Exception {
        DefaultListableBeanFactory beanFactory = getBeanFactory("autowire-constructor-with-exclusion.xml");
        TestBean rob = (TestBean) beanFactory.getBean("rob");
        TestBean sally = (TestBean) beanFactory.getBean("sally");
        assertThat(rob.getSpouse()).isEqualTo(sally);
        TestBean rob2 = (TestBean) beanFactory.getBean("rob");
        assertThat(rob2).isEqualTo(rob);
        assertThat(rob2).isNotSameAs(rob);
        assertThat(rob2.getSpouse()).isEqualTo(rob.getSpouse());
        assertThat(rob2.getSpouse()).isNotSameAs(rob.getSpouse());
    }

    @Test
    public void constructorAutowireWithExclusion() throws Exception {
        DefaultListableBeanFactory beanFactory = getBeanFactory("autowire-constructor-with-exclusion.xml");
        TestBean rob = (TestBean) beanFactory.getBean("rob");
        assertThat(rob.getSomeProperties().getProperty("name")).isEqualTo("props1");
    }

    private DefaultListableBeanFactory getBeanFactory(String configPath) {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new ClassPathResource(configPath, getClass()));
        return bf;
    }
}
