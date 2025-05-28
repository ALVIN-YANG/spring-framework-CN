// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 {@link BeanDefinitionValueResolver}。
 *
 * @author Stephane Nicoll
 */
class BeanDefinitionValueResolverTests {

    @Test
    void resolveInnerBean() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        RootBeanDefinition parentBd = new RootBeanDefinition();
        GenericBeanDefinition innerBd = new GenericBeanDefinition();
        innerBd.setAttribute("test", 42);
        BeanDefinitionValueResolver bdvr = new BeanDefinitionValueResolver(beanFactory, "test", parentBd);
        RootBeanDefinition resolvedInnerBd = bdvr.resolveInnerBean(null, innerBd, (name, mbd) -> {
            assertThat(name).isNotNull().startsWith("(inner bean");
            return mbd;
        });
        assertThat(resolvedInnerBd.getAttribute("test")).isEqualTo(42);
    }
}
