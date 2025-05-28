// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.aot;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.testfixture.beans.AnnotatedBean;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 {@link BeanRegistrationsAotProcessor}。
 *
 * @author Phillip Webb
 * @author Sebastien Deleuze
 */
class BeanRegistrationsAotProcessorTests {

    @Test
    void beanRegistrationsAotProcessorIsRegistered() {
        assertThat(AotServices.factoriesAndBeans(new DefaultListableBeanFactory()).load(BeanFactoryInitializationAotProcessor.class)).anyMatch(BeanRegistrationsAotProcessor.class::isInstance);
    }

    @Test
    void processAheadOfTimeReturnsBeanRegistrationsAotContributionWithRegistrations() {
        BeanRegistrationsAotProcessor processor = new BeanRegistrationsAotProcessor();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("b1", new RootBeanDefinition(TestBean.class));
        beanFactory.registerBeanDefinition("b2", new RootBeanDefinition(AnnotatedBean.class));
        BeanRegistrationsAotContribution contribution = processor.processAheadOfTime(beanFactory);
        assertThat(contribution).extracting("registrations").asInstanceOf(InstanceOfAssertFactories.MAP).hasSize(2);
    }

    @Test
    void processAheadOfTimeReturnsBeanRegistrationsAotContributionWithAliases() {
        BeanRegistrationsAotProcessor processor = new BeanRegistrationsAotProcessor();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class));
        beanFactory.registerAlias("test", "testAlias");
        BeanRegistrationsAotContribution contribution = processor.processAheadOfTime(beanFactory);
        assertThat(contribution).extracting("registrations").asInstanceOf(InstanceOfAssertFactories.MAP).hasEntrySatisfying(new BeanRegistrationKey("test", TestBean.class), registration -> assertThat(registration).extracting("aliases").asInstanceOf(InstanceOfAssertFactories.ARRAY).singleElement().isEqualTo("testAlias"));
    }
}
