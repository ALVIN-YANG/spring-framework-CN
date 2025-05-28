// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.xml;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 测试了不同配置声明的组合与各种配置激活和配置默认场景的匹配。
 *
 * @author Chris Beams
 * @author Sam Brannen
 * @since 3.1
 */
public class ProfileXmlBeanDefinitionTests {

    private static final String PROD_ELIGIBLE_XML = "ProfileXmlBeanDefinitionTests-prodProfile.xml";

    private static final String DEV_ELIGIBLE_XML = "ProfileXmlBeanDefinitionTests-devProfile.xml";

    private static final String NOT_DEV_ELIGIBLE_XML = "ProfileXmlBeanDefinitionTests-notDevProfile.xml";

    private static final String ALL_ELIGIBLE_XML = "ProfileXmlBeanDefinitionTests-noProfile.xml";

    private static final String MULTI_ELIGIBLE_XML = "ProfileXmlBeanDefinitionTests-multiProfile.xml";

    private static final String MULTI_NEGATED_XML = "ProfileXmlBeanDefinitionTests-multiProfileNegated.xml";

    private static final String MULTI_NOT_DEV_ELIGIBLE_XML = "ProfileXmlBeanDefinitionTests-multiProfileNotDev.xml";

    private static final String MULTI_ELIGIBLE_SPACE_DELIMITED_XML = "ProfileXmlBeanDefinitionTests-spaceDelimitedProfile.xml";

    private static final String UNKNOWN_ELIGIBLE_XML = "ProfileXmlBeanDefinitionTests-unknownProfile.xml";

    private static final String DEFAULT_ELIGIBLE_XML = "ProfileXmlBeanDefinitionTests-defaultProfile.xml";

    private static final String CUSTOM_DEFAULT_ELIGIBLE_XML = "ProfileXmlBeanDefinitionTests-customDefaultProfile.xml";

    private static final String DEFAULT_AND_DEV_ELIGIBLE_XML = "ProfileXmlBeanDefinitionTests-defaultAndDevProfile.xml";

    private static final String PROD_ACTIVE = "prod";

    private static final String DEV_ACTIVE = "dev";

    private static final String NULL_ACTIVE = null;

    private static final String UNKNOWN_ACTIVE = "unknown";

    private static final String[] NONE_ACTIVE = new String[0];

    private static final String[] MULTI_ACTIVE = new String[] { PROD_ACTIVE, DEV_ACTIVE };

    private static final String TARGET_BEAN = "foo";

    @Test
    public void testProfileValidation() {
        assertThatIllegalArgumentException().isThrownBy(() -> beanFactoryFor(PROD_ELIGIBLE_XML, NULL_ACTIVE));
    }

    @Test
    public void testProfilePermutations() {
        assertThat(beanFactoryFor(PROD_ELIGIBLE_XML, NONE_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(PROD_ELIGIBLE_XML, DEV_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(PROD_ELIGIBLE_XML, PROD_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(PROD_ELIGIBLE_XML, MULTI_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(DEV_ELIGIBLE_XML, NONE_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(DEV_ELIGIBLE_XML, DEV_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(DEV_ELIGIBLE_XML, PROD_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(DEV_ELIGIBLE_XML, MULTI_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(NOT_DEV_ELIGIBLE_XML, NONE_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(NOT_DEV_ELIGIBLE_XML, DEV_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(NOT_DEV_ELIGIBLE_XML, PROD_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(NOT_DEV_ELIGIBLE_XML, MULTI_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(ALL_ELIGIBLE_XML, NONE_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(ALL_ELIGIBLE_XML, DEV_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(ALL_ELIGIBLE_XML, PROD_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(ALL_ELIGIBLE_XML, MULTI_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_ELIGIBLE_XML, NONE_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(MULTI_ELIGIBLE_XML, UNKNOWN_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(MULTI_ELIGIBLE_XML, DEV_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_ELIGIBLE_XML, PROD_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_ELIGIBLE_XML, MULTI_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_NEGATED_XML, NONE_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_NEGATED_XML, UNKNOWN_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_NEGATED_XML, DEV_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_NEGATED_XML, PROD_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_NEGATED_XML, MULTI_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(MULTI_NOT_DEV_ELIGIBLE_XML, NONE_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_NOT_DEV_ELIGIBLE_XML, UNKNOWN_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_NOT_DEV_ELIGIBLE_XML, DEV_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(MULTI_NOT_DEV_ELIGIBLE_XML, PROD_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_NOT_DEV_ELIGIBLE_XML, MULTI_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_ELIGIBLE_SPACE_DELIMITED_XML, NONE_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(MULTI_ELIGIBLE_SPACE_DELIMITED_XML, UNKNOWN_ACTIVE)).isNot(containingTarget());
        assertThat(beanFactoryFor(MULTI_ELIGIBLE_SPACE_DELIMITED_XML, DEV_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_ELIGIBLE_SPACE_DELIMITED_XML, PROD_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(MULTI_ELIGIBLE_SPACE_DELIMITED_XML, MULTI_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(UNKNOWN_ELIGIBLE_XML, MULTI_ACTIVE)).isNot(containingTarget());
    }

    @Test
    public void testDefaultProfile() {
        {
            DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
            ConfigurableEnvironment env = new StandardEnvironment();
            env.setDefaultProfiles("custom-default");
            reader.setEnvironment(env);
            reader.loadBeanDefinitions(new ClassPathResource(DEFAULT_ELIGIBLE_XML, getClass()));
            assertThat(beanFactory).isNot(containingTarget());
        }
        {
            DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
            ConfigurableEnvironment env = new StandardEnvironment();
            env.setDefaultProfiles("custom-default");
            reader.setEnvironment(env);
            reader.loadBeanDefinitions(new ClassPathResource(CUSTOM_DEFAULT_ELIGIBLE_XML, getClass()));
            assertThat(beanFactory).is(containingTarget());
        }
    }

    @Test
    public void testDefaultAndNonDefaultProfile() {
        assertThat(beanFactoryFor(DEFAULT_ELIGIBLE_XML, NONE_ACTIVE)).is(containingTarget());
        assertThat(beanFactoryFor(DEFAULT_ELIGIBLE_XML, "other")).isNot(containingTarget());
        {
            DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
            ConfigurableEnvironment env = new StandardEnvironment();
            env.setActiveProfiles(DEV_ACTIVE);
            env.setDefaultProfiles("default");
            reader.setEnvironment(env);
            reader.loadBeanDefinitions(new ClassPathResource(DEFAULT_AND_DEV_ELIGIBLE_XML, getClass()));
            assertThat(beanFactory).is(containingTarget());
        }
        {
            DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
            ConfigurableEnvironment env = new StandardEnvironment();
            // env.setActiveProfiles(DEV_ACTIVE); 的注释内容翻译成中文可能是：设置活动配置文件为开发配置（DEV_ACTIVE）。
            env.setDefaultProfiles("default");
            reader.setEnvironment(env);
            reader.loadBeanDefinitions(new ClassPathResource(DEFAULT_AND_DEV_ELIGIBLE_XML, getClass()));
            assertThat(beanFactory).is(containingTarget());
        }
        {
            DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
            ConfigurableEnvironment env = new StandardEnvironment();
            // env.setActiveProfiles(DEV_ACTIVE);这段代码的注释翻译成中文为：设置当前环境为开发环境配置。
            // 设置默认配置文件为 "default"。
            reader.setEnvironment(env);
            reader.loadBeanDefinitions(new ClassPathResource(DEFAULT_AND_DEV_ELIGIBLE_XML, getClass()));
            assertThat(beanFactory).is(containingTarget());
        }
    }

    private BeanDefinitionRegistry beanFactoryFor(String xmlName, String... activeProfiles) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        StandardEnvironment env = new StandardEnvironment();
        env.setActiveProfiles(activeProfiles);
        reader.setEnvironment(env);
        reader.loadBeanDefinitions(new ClassPathResource(xmlName, getClass()));
        return beanFactory;
    }

    private Condition<BeanDefinitionRegistry> containingTarget() {
        return new Condition<>(registry -> registry.containsBeanDefinition(TARGET_BEAN), "contains target");
    }
}
