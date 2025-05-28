// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 您不得使用此文件除非遵守许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 包括但不限于适销性或特定用途的适用性。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.testfixture.io.ResourceTestUtils.qualifiedResource;

/**
 * 对 {@link CustomAutowireConfigurer} 的单元测试。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Chris Beams
 */
public class CustomAutowireConfigurerTests {

    @Test
    public void testCustomResolver() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(qualifiedResource(CustomAutowireConfigurerTests.class, "context.xml"));
        CustomAutowireConfigurer cac = new CustomAutowireConfigurer();
        CustomResolver customResolver = new CustomResolver();
        bf.setAutowireCandidateResolver(customResolver);
        cac.postProcessBeanFactory(bf);
        TestBean testBean = (TestBean) bf.getBean("testBean");
        assertThat(testBean.getName()).isEqualTo("#1!");
    }

    public static class TestBean {

        private String name;

        public TestBean(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    public static class CustomResolver implements AutowireCandidateResolver {

        @Override
        public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
            if (!bdHolder.getBeanDefinition().isAutowireCandidate()) {
                return false;
            }
            if (!bdHolder.getBeanName().matches("[a-z-]+")) {
                return false;
            }
            if (bdHolder.getBeanDefinition().getAttribute("priority").equals("1")) {
                return true;
            }
            return false;
        }

        @Override
        public Object getSuggestedValue(DependencyDescriptor descriptor) {
            return null;
        }

        @Override
        public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
            return null;
        }
    }
}
