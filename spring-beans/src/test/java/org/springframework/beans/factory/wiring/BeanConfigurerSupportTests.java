// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.wiring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Rick Evans
 * @author Juergen Hoeller
 * @author Sam Brannen
 *
 * 作者：Rick Evans
 * 作者：Juergen Hoeller
 * 作者：Sam Brannen
 */
public class BeanConfigurerSupportTests {

    @Test
    public void supplyIncompatibleBeanFactoryImplementation() {
        assertThatIllegalArgumentException().isThrownBy(() -> new StubBeanConfigurerSupport().setBeanFactory(mock()));
    }

    @Test
    public void configureBeanDoesNothingIfBeanWiringInfoResolverResolvesToNull() throws Exception {
        TestBean beanInstance = new TestBean();
        BeanWiringInfoResolver resolver = mock();
        BeanConfigurerSupport configurer = new StubBeanConfigurerSupport();
        configurer.setBeanWiringInfoResolver(resolver);
        configurer.setBeanFactory(new DefaultListableBeanFactory());
        configurer.configureBean(beanInstance);
        verify(resolver).resolveWiringInfo(beanInstance);
        assertThat(beanInstance.getName()).isNull();
    }

    @Test
    public void configureBeanDoesNothingIfNoBeanFactoryHasBeenSet() throws Exception {
        TestBean beanInstance = new TestBean();
        BeanConfigurerSupport configurer = new StubBeanConfigurerSupport();
        configurer.configureBean(beanInstance);
        assertThat(beanInstance.getName()).isNull();
    }

    @Test
    public void configureBeanReallyDoesDefaultToUsingTheFullyQualifiedClassNameOfTheSuppliedBeanInstance() throws Exception {
        TestBean beanInstance = new TestBean();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TestBean.class);
        builder.addPropertyValue("name", "Harriet Wheeler");
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition(beanInstance.getClass().getName(), builder.getBeanDefinition());
        BeanConfigurerSupport configurer = new StubBeanConfigurerSupport();
        configurer.setBeanFactory(factory);
        configurer.afterPropertiesSet();
        configurer.configureBean(beanInstance);
        assertThat(beanInstance.getName()).as("Bean is evidently not being configured (for some reason)").isEqualTo("Harriet Wheeler");
    }

    @Test
    public void configureBeanPerformsAutowiringByNameIfAppropriateBeanWiringInfoResolverIsPluggedIn() throws Exception {
        TestBean beanInstance = new TestBean();
        // 配偶，用于按名称自动装配...
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TestBean.class);
        builder.addConstructorArgValue("David Gavurin");
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("spouse", builder.getBeanDefinition());
        BeanWiringInfoResolver resolver = mock();
        given(resolver.resolveWiringInfo(beanInstance)).willReturn(new BeanWiringInfo(BeanWiringInfo.AUTOWIRE_BY_NAME, false));
        BeanConfigurerSupport configurer = new StubBeanConfigurerSupport();
        configurer.setBeanFactory(factory);
        configurer.setBeanWiringInfoResolver(resolver);
        configurer.configureBean(beanInstance);
        assertThat(beanInstance.getSpouse().getName()).as("Bean is evidently not being configured (for some reason)").isEqualTo("David Gavurin");
    }

    @Test
    public void configureBeanPerformsAutowiringByTypeIfAppropriateBeanWiringInfoResolverIsPluggedIn() throws Exception {
        TestBean beanInstance = new TestBean();
        // 配偶，用于按类型自动装配...
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TestBean.class);
        builder.addConstructorArgValue("David Gavurin");
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("Mmm, I fancy a salad!", builder.getBeanDefinition());
        BeanWiringInfoResolver resolver = mock();
        given(resolver.resolveWiringInfo(beanInstance)).willReturn(new BeanWiringInfo(BeanWiringInfo.AUTOWIRE_BY_TYPE, false));
        BeanConfigurerSupport configurer = new StubBeanConfigurerSupport();
        configurer.setBeanFactory(factory);
        configurer.setBeanWiringInfoResolver(resolver);
        configurer.configureBean(beanInstance);
        assertThat(beanInstance.getSpouse().getName()).as("Bean is evidently not being configured (for some reason)").isEqualTo("David Gavurin");
    }

    private static class StubBeanConfigurerSupport extends BeanConfigurerSupport {
    }
}
