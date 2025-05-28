// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按 "原样" 分发，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权的保证。
* 请参阅许可证以了解具体管理权限和限制的语言。*/
package org.springframework.beans.factory.config;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.testfixture.io.ResourceTestUtils.qualifiedResource;

/**
 * 对 {@link PropertiesFactoryBean} 的单元测试。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 01.11.2003
 */
public class PropertiesFactoryBeanTests {

    private static final Class<?> CLASS = PropertiesFactoryBeanTests.class;

    private static final Resource TEST_PROPS = qualifiedResource(CLASS, "test.properties");

    private static final Resource TEST_PROPS_XML = qualifiedResource(CLASS, "test.properties.xml");

    @Test
    public void testWithPropertiesFile() throws Exception {
        PropertiesFactoryBean pfb = new PropertiesFactoryBean();
        pfb.setLocation(TEST_PROPS);
        pfb.afterPropertiesSet();
        Properties props = pfb.getObject();
        assertThat(props.getProperty("tb.array[0].age")).isEqualTo("99");
    }

    @Test
    public void testWithPropertiesXmlFile() throws Exception {
        PropertiesFactoryBean pfb = new PropertiesFactoryBean();
        pfb.setLocation(TEST_PROPS_XML);
        pfb.afterPropertiesSet();
        Properties props = pfb.getObject();
        assertThat(props.getProperty("tb.array[0].age")).isEqualTo("99");
    }

    @Test
    public void testWithLocalProperties() throws Exception {
        PropertiesFactoryBean pfb = new PropertiesFactoryBean();
        Properties localProps = new Properties();
        localProps.setProperty("key2", "value2");
        pfb.setProperties(localProps);
        pfb.afterPropertiesSet();
        Properties props = pfb.getObject();
        assertThat(props.getProperty("key2")).isEqualTo("value2");
    }

    @Test
    public void testWithPropertiesFileAndLocalProperties() throws Exception {
        PropertiesFactoryBean pfb = new PropertiesFactoryBean();
        pfb.setLocation(TEST_PROPS);
        Properties localProps = new Properties();
        localProps.setProperty("key2", "value2");
        localProps.setProperty("tb.array[0].age", "0");
        pfb.setProperties(localProps);
        pfb.afterPropertiesSet();
        Properties props = pfb.getObject();
        assertThat(props.getProperty("tb.array[0].age")).isEqualTo("99");
        assertThat(props.getProperty("key2")).isEqualTo("value2");
    }

    @Test
    public void testWithPropertiesFileAndMultipleLocalProperties() throws Exception {
        PropertiesFactoryBean pfb = new PropertiesFactoryBean();
        pfb.setLocation(TEST_PROPS);
        Properties props1 = new Properties();
        props1.setProperty("key2", "value2");
        props1.setProperty("tb.array[0].age", "0");
        Properties props2 = new Properties();
        props2.setProperty("spring", "framework");
        props2.setProperty("Don", "Mattingly");
        Properties props3 = new Properties();
        props3.setProperty("spider", "man");
        props3.setProperty("bat", "man");
        pfb.setPropertiesArray(new Properties[] { props1, props2, props3 });
        pfb.afterPropertiesSet();
        Properties props = pfb.getObject();
        assertThat(props.getProperty("tb.array[0].age")).isEqualTo("99");
        assertThat(props.getProperty("key2")).isEqualTo("value2");
        assertThat(props.getProperty("spring")).isEqualTo("framework");
        assertThat(props.getProperty("Don")).isEqualTo("Mattingly");
        assertThat(props.getProperty("spider")).isEqualTo("man");
        assertThat(props.getProperty("bat")).isEqualTo("man");
    }

    @Test
    public void testWithPropertiesFileAndLocalPropertiesAndLocalOverride() throws Exception {
        PropertiesFactoryBean pfb = new PropertiesFactoryBean();
        pfb.setLocation(TEST_PROPS);
        Properties localProps = new Properties();
        localProps.setProperty("key2", "value2");
        localProps.setProperty("tb.array[0].age", "0");
        pfb.setProperties(localProps);
        pfb.setLocalOverride(true);
        pfb.afterPropertiesSet();
        Properties props = pfb.getObject();
        assertThat(props.getProperty("tb.array[0].age")).isEqualTo("0");
        assertThat(props.getProperty("key2")).isEqualTo("value2");
    }

    @Test
    public void testWithPrototype() throws Exception {
        PropertiesFactoryBean pfb = new PropertiesFactoryBean();
        pfb.setSingleton(false);
        pfb.setLocation(TEST_PROPS);
        Properties localProps = new Properties();
        localProps.setProperty("key2", "value2");
        pfb.setProperties(localProps);
        pfb.afterPropertiesSet();
        Properties props = pfb.getObject();
        assertThat(props.getProperty("tb.array[0].age")).isEqualTo("99");
        assertThat(props.getProperty("key2")).isEqualTo("value2");
        Properties newProps = pfb.getObject();
        assertThat(props).isNotSameAs(newProps);
        assertThat(newProps.getProperty("tb.array[0].age")).isEqualTo("99");
        assertThat(newProps.getProperty("key2")).isEqualTo("value2");
    }
}
