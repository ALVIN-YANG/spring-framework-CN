// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.config;

import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerWithGeneratedName;

/**
 * 对 {@link PropertyPlaceholderConfigurer} 的单元测试。
 *
 * @author Chris Beams
 */
@SuppressWarnings("deprecation")
public class PropertyPlaceholderConfigurerTests {

    private static final String P1 = "p1";

    private static final String P1_LOCAL_PROPS_VAL = "p1LocalPropsVal";

    private static final String P1_SYSTEM_PROPS_VAL = "p1SystemPropsVal";

    private DefaultListableBeanFactory bf;

    private PropertyPlaceholderConfigurer ppc;

    private Properties ppcProperties;

    private AbstractBeanDefinition p1BeanDef;

    @BeforeEach
    public void setup() {
        p1BeanDef = rootBeanDefinition(TestBean.class).addPropertyValue("name", "${" + P1 + "}").getBeanDefinition();
        bf = new DefaultListableBeanFactory();
        ppcProperties = new Properties();
        ppcProperties.setProperty(P1, P1_LOCAL_PROPS_VAL);
        System.setProperty(P1, P1_SYSTEM_PROPS_VAL);
        ppc = new PropertyPlaceholderConfigurer();
        ppc.setProperties(ppcProperties);
    }

    @AfterEach
    public void cleanup() {
        System.clearProperty(P1);
    }

    @Test
    public void localPropertiesViaResource() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("testBean", genericBeanDefinition(TestBean.class).addPropertyValue("name", "${my.name}").getBeanDefinition());
        PropertyPlaceholderConfigurer pc = new PropertyPlaceholderConfigurer();
        Resource resource = new ClassPathResource("PropertyPlaceholderConfigurerTests.properties", this.getClass());
        pc.setLocation(resource);
        pc.postProcessBeanFactory(bf);
    }

    @Test
    public void resolveFromSystemProperties() {
        System.setProperty("otherKey", "systemValue");
        p1BeanDef = rootBeanDefinition(TestBean.class).addPropertyValue("name", "${" + P1 + "}").addPropertyValue("sex", "${otherKey}").getBeanDefinition();
        registerWithGeneratedName(p1BeanDef, bf);
        ppc.postProcessBeanFactory(bf);
        TestBean bean = bf.getBean(TestBean.class);
        assertThat(bean.getName()).isEqualTo(P1_LOCAL_PROPS_VAL);
        assertThat(bean.getSex()).isEqualTo("systemValue");
        System.clearProperty("otherKey");
    }

    @Test
    public void resolveFromLocalProperties() {
        System.clearProperty(P1);
        registerWithGeneratedName(p1BeanDef, bf);
        ppc.postProcessBeanFactory(bf);
        TestBean bean = bf.getBean(TestBean.class);
        assertThat(bean.getName()).isEqualTo(P1_LOCAL_PROPS_VAL);
    }

    @Test
    public void setSystemPropertiesMode_defaultIsFallback() {
        registerWithGeneratedName(p1BeanDef, bf);
        ppc.postProcessBeanFactory(bf);
        TestBean bean = bf.getBean(TestBean.class);
        assertThat(bean.getName()).isEqualTo(P1_LOCAL_PROPS_VAL);
    }

    @Test
    public void setSystemSystemPropertiesMode_toOverride_andResolveFromSystemProperties() {
        registerWithGeneratedName(p1BeanDef, bf);
        ppc.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        ppc.postProcessBeanFactory(bf);
        TestBean bean = bf.getBean(TestBean.class);
        assertThat(bean.getName()).isEqualTo(P1_SYSTEM_PROPS_VAL);
    }

    @Test
    public void setSystemSystemPropertiesMode_toOverride_andSetSearchSystemEnvironment_toFalse() {
        registerWithGeneratedName(p1BeanDef, bf);
        // 现在将完全回退到系统环境
        System.clearProperty(P1);
        ppc.setSearchSystemEnvironment(false);
        ppc.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        ppc.postProcessBeanFactory(bf);
        TestBean bean = bf.getBean(TestBean.class);
        // 必须求助于本地属性
        assertThat(bean.getName()).isEqualTo(P1_LOCAL_PROPS_VAL);
    }

    /**
     * 创建一个场景，其中配置了两个 PPC（Property PlaceholdersConfigurer），每个 PPC 在从环境解析属性方面有不同的设置。
     */
    @Test
    public void twoPlaceholderConfigurers_withConflictingSettings() {
        String P2 = "p2";
        String P2_LOCAL_PROPS_VAL = "p2LocalPropsVal";
        String P2_SYSTEM_PROPS_VAL = "p2SystemPropsVal";
        AbstractBeanDefinition p2BeanDef = rootBeanDefinition(TestBean.class).addPropertyValue("name", "${" + P1 + "}").addPropertyValue("country", "${" + P2 + "}").getBeanDefinition();
        bf.registerBeanDefinition("p1Bean", p1BeanDef);
        bf.registerBeanDefinition("p2Bean", p2BeanDef);
        ppc.setIgnoreUnresolvablePlaceholders(true);
        ppc.postProcessBeanFactory(bf);
        System.setProperty(P2, P2_SYSTEM_PROPS_VAL);
        Properties ppc2Properties = new Properties();
        ppc2Properties.put(P2, P2_LOCAL_PROPS_VAL);
        PropertyPlaceholderConfigurer ppc2 = new PropertyPlaceholderConfigurer();
        ppc2.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        ppc2.setProperties(ppc2Properties);
        ppc2Properties = new Properties();
        ppc2Properties.setProperty(P2, P2_LOCAL_PROPS_VAL);
        ppc2.postProcessBeanFactory(bf);
        TestBean p1Bean = bf.getBean("p1Bean", TestBean.class);
        assertThat(p1Bean.getName()).isEqualTo(P1_LOCAL_PROPS_VAL);
        TestBean p2Bean = bf.getBean("p2Bean", TestBean.class);
        assertThat(p2Bean.getName()).isEqualTo(P1_LOCAL_PROPS_VAL);
        assertThat(p2Bean.getCountry()).isEqualTo(P2_SYSTEM_PROPS_VAL);
        System.clearProperty(P2);
    }

    @Test
    public void customPlaceholderPrefixAndSuffix() {
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        ppc.setPlaceholderPrefix("@<");
        ppc.setPlaceholderSuffix(">");
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("testBean", rootBeanDefinition(TestBean.class).addPropertyValue("name", "@<key1>").addPropertyValue("sex", "${key2}").getBeanDefinition());
        System.setProperty("key1", "systemKey1Value");
        System.setProperty("key2", "systemKey2Value");
        ppc.postProcessBeanFactory(bf);
        System.clearProperty("key1");
        System.clearProperty("key2");
        assertThat(bf.getBean(TestBean.class).getName()).isEqualTo("systemKey1Value");
        assertThat(bf.getBean(TestBean.class).getSex()).isEqualTo("${key2}");
    }

    @Test
    public void nullValueIsPreserved() {
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        ppc.setNullValue("customNull");
        System.setProperty("my.name", "customNull");
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("testBean", rootBeanDefinition(TestBean.class).addPropertyValue("name", "${my.name}").getBeanDefinition());
        ppc.postProcessBeanFactory(bf);
        assertThat(bf.getBean(TestBean.class).getName()).isNull();
        System.clearProperty("my.name");
    }

    @Test
    public void trimValuesIsOffByDefault() {
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        System.setProperty("my.name", " myValue  ");
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("testBean", rootBeanDefinition(TestBean.class).addPropertyValue("name", "${my.name}").getBeanDefinition());
        ppc.postProcessBeanFactory(bf);
        assertThat(bf.getBean(TestBean.class).getName()).isEqualTo(" myValue  ");
        System.clearProperty("my.name");
    }

    @Test
    public void trimValuesIsApplied() {
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        ppc.setTrimValues(true);
        System.setProperty("my.name", " myValue  ");
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("testBean", rootBeanDefinition(TestBean.class).addPropertyValue("name", "${my.name}").getBeanDefinition());
        ppc.postProcessBeanFactory(bf);
        assertThat(bf.getBean(TestBean.class).getName()).isEqualTo("myValue");
        System.clearProperty("my.name");
    }
}
