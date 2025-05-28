// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用，除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.testfixture.beans.DerivedTestBean;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2006年7月4日
 */
class DefaultSingletonBeanRegistryTests {

    private final DefaultSingletonBeanRegistry beanRegistry = new DefaultSingletonBeanRegistry();

    @Test
    void singletons() {
        TestBean tb = new TestBean();
        beanRegistry.registerSingleton("tb", tb);
        assertThat(beanRegistry.getSingleton("tb")).isSameAs(tb);
        TestBean tb2 = (TestBean) beanRegistry.getSingleton("tb2", TestBean::new);
        assertThat(beanRegistry.getSingleton("tb2")).isSameAs(tb2);
        assertThat(beanRegistry.getSingleton("tb")).isSameAs(tb);
        assertThat(beanRegistry.getSingleton("tb2")).isSameAs(tb2);
        assertThat(beanRegistry.getSingletonCount()).isEqualTo(2);
        assertThat(beanRegistry.getSingletonNames()).containsExactly("tb", "tb2");
        beanRegistry.destroySingletons();
        assertThat(beanRegistry.getSingletonCount()).isZero();
        assertThat(beanRegistry.getSingletonNames()).isEmpty();
    }

    @Test
    void disposableBean() {
        DerivedTestBean tb = new DerivedTestBean();
        beanRegistry.registerSingleton("tb", tb);
        beanRegistry.registerDisposableBean("tb", tb);
        assertThat(beanRegistry.getSingleton("tb")).isSameAs(tb);
        assertThat(beanRegistry.getSingleton("tb")).isSameAs(tb);
        assertThat(beanRegistry.getSingletonCount()).isEqualTo(1);
        assertThat(beanRegistry.getSingletonNames()).containsExactly("tb");
        assertThat(tb.wasDestroyed()).isFalse();
        beanRegistry.destroySingletons();
        assertThat(beanRegistry.getSingletonCount()).isZero();
        assertThat(beanRegistry.getSingletonNames()).isEmpty();
        assertThat(tb.wasDestroyed()).isTrue();
    }

    @Test
    void dependentRegistration() {
        beanRegistry.registerDependentBean("a", "b");
        beanRegistry.registerDependentBean("b", "c");
        beanRegistry.registerDependentBean("c", "b");
        assertThat(beanRegistry.isDependent("a", "b")).isTrue();
        assertThat(beanRegistry.isDependent("b", "c")).isTrue();
        assertThat(beanRegistry.isDependent("c", "b")).isTrue();
        assertThat(beanRegistry.isDependent("a", "c")).isTrue();
        assertThat(beanRegistry.isDependent("c", "a")).isFalse();
        assertThat(beanRegistry.isDependent("b", "a")).isFalse();
        assertThat(beanRegistry.isDependent("a", "a")).isFalse();
        assertThat(beanRegistry.isDependent("b", "b")).isTrue();
        assertThat(beanRegistry.isDependent("c", "c")).isTrue();
    }
}
