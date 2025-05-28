// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用，除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“现状”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵犯知识产权的保证。
* 请参阅许可证了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Arjen Poutsma
 */
public class DeprecatedBeanWarnerTests {

    private String beanName;

    private BeanDefinition beanDefinition;

    @Test
    @SuppressWarnings("deprecation")
    public void postProcess() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        BeanDefinition def = new RootBeanDefinition(MyDeprecatedBean.class);
        String beanName = "deprecated";
        beanFactory.registerBeanDefinition(beanName, def);
        DeprecatedBeanWarner warner = new MyDeprecatedBeanWarner();
        warner.postProcessBeanFactory(beanFactory);
        assertThat(this.beanName).isEqualTo(beanName);
        assertThat(this.beanDefinition).isEqualTo(def);
    }

    private class MyDeprecatedBeanWarner extends DeprecatedBeanWarner {

        @Override
        protected void logDeprecatedBean(String beanName, Class<?> beanType, BeanDefinition beanDefinition) {
            DeprecatedBeanWarnerTests.this.beanName = beanName;
            DeprecatedBeanWarnerTests.this.beanDefinition = beanDefinition;
        }
    }
}
