// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律强制要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的质量保证或适用性保证；
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.util.Date;
import jakarta.inject.Provider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.testfixture.io.SerializationTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.core.testfixture.io.ResourceTestUtils.qualifiedResource;

/**
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @author Rick Evans
 * @author Chris Beams
 *
 * 作者：Colin Sampaleanu
 * 作者：Juergen Hoeller
 * 作者：Rick Evans
 * 作者：Chris Beams
 */
public class ObjectFactoryCreatingFactoryBeanTests {

    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        this.beanFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(this.beanFactory).loadBeanDefinitions(qualifiedResource(ObjectFactoryCreatingFactoryBeanTests.class, "context.xml"));
        this.beanFactory.setSerializationId("test");
    }

    @AfterEach
    public void close() {
        this.beanFactory.setSerializationId(null);
    }

    @Test
    public void testFactoryOperation() {
        FactoryTestBean testBean = beanFactory.getBean("factoryTestBean", FactoryTestBean.class);
        ObjectFactory<?> objectFactory = testBean.getObjectFactory();
        Date date1 = (Date) objectFactory.getObject();
        Date date2 = (Date) objectFactory.getObject();
        assertThat(date1).isNotSameAs(date2);
    }

    @Test
    public void testFactorySerialization() throws Exception {
        FactoryTestBean testBean = beanFactory.getBean("factoryTestBean", FactoryTestBean.class);
        ObjectFactory<?> objectFactory = testBean.getObjectFactory();
        objectFactory = SerializationTestUtils.serializeAndDeserialize(objectFactory);
        Date date1 = (Date) objectFactory.getObject();
        Date date2 = (Date) objectFactory.getObject();
        assertThat(date1).isNotSameAs(date2);
    }

    @Test
    public void testProviderOperation() {
        ProviderTestBean testBean = beanFactory.getBean("providerTestBean", ProviderTestBean.class);
        Provider<?> provider = testBean.getProvider();
        Date date1 = (Date) provider.get();
        Date date2 = (Date) provider.get();
        assertThat(date1).isNotSameAs(date2);
    }

    @Test
    public void testProviderSerialization() throws Exception {
        ProviderTestBean testBean = beanFactory.getBean("providerTestBean", ProviderTestBean.class);
        Provider<?> provider = testBean.getProvider();
        provider = SerializationTestUtils.serializeAndDeserialize(provider);
        Date date1 = (Date) provider.get();
        Date date2 = (Date) provider.get();
        assertThat(date1).isNotSameAs(date2);
    }

    @Test
    public void testDoesNotComplainWhenTargetBeanNameRefersToSingleton() throws Exception {
        final String targetBeanName = "singleton";
        final String expectedSingleton = "Alicia Keys";
        BeanFactory beanFactory = mock();
        given(beanFactory.getBean(targetBeanName)).willReturn(expectedSingleton);
        ObjectFactoryCreatingFactoryBean factory = new ObjectFactoryCreatingFactoryBean();
        factory.setTargetBeanName(targetBeanName);
        factory.setBeanFactory(beanFactory);
        factory.afterPropertiesSet();
        ObjectFactory<?> objectFactory = factory.getObject();
        Object actualSingleton = objectFactory.getObject();
        assertThat(actualSingleton).isSameAs(expectedSingleton);
    }

    @Test
    public void testWhenTargetBeanNameIsNull() throws Exception {
        assertThatIllegalArgumentException().as("'targetBeanName' property not set").isThrownBy(new ObjectFactoryCreatingFactoryBean()::afterPropertiesSet);
    }

    @Test
    public void testWhenTargetBeanNameIsEmptyString() throws Exception {
        ObjectFactoryCreatingFactoryBean factory = new ObjectFactoryCreatingFactoryBean();
        factory.setTargetBeanName("");
        assertThatIllegalArgumentException().as("'targetBeanName' property set to (invalid) empty string").isThrownBy(factory::afterPropertiesSet);
    }

    @Test
    public void testWhenTargetBeanNameIsWhitespacedString() throws Exception {
        ObjectFactoryCreatingFactoryBean factory = new ObjectFactoryCreatingFactoryBean();
        factory.setTargetBeanName("  \t");
        assertThatIllegalArgumentException().as("'targetBeanName' property set to (invalid) only-whitespace string").isThrownBy(factory::afterPropertiesSet);
    }

    @Test
    public void testEnsureOFBFBReportsThatItActuallyCreatesObjectFactoryInstances() {
        assertThat(new ObjectFactoryCreatingFactoryBean().getObjectType()).as("Must be reporting that it creates ObjectFactory instances (as per class contract).").isEqualTo(ObjectFactory.class);
    }

    public static class FactoryTestBean {

        private ObjectFactory<?> objectFactory;

        public ObjectFactory<?> getObjectFactory() {
            return objectFactory;
        }

        public void setObjectFactory(ObjectFactory<?> objectFactory) {
            this.objectFactory = objectFactory;
        }
    }

    public static class ProviderTestBean {

        private Provider<?> provider;

        public Provider<?> getProvider() {
            return provider;
        }

        public void setProvider(Provider<?> provider) {
            this.provider = provider;
        }
    }
}
