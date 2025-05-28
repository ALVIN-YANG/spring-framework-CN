// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class DefaultLifecycleMethodsTests {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    @BeforeEach
    public void setup() throws Exception {
        new XmlBeanDefinitionReader(this.beanFactory).loadBeanDefinitions(new ClassPathResource("defaultLifecycleMethods.xml", getClass()));
    }

    @Test
    public void lifecycleMethodsInvoked() {
        LifecycleAwareBean bean = (LifecycleAwareBean) this.beanFactory.getBean("lifecycleAware");
        assertThat(bean.isInitCalled()).as("Bean not initialized").isTrue();
        assertThat(bean.isCustomInitCalled()).as("Custom init method called incorrectly").isFalse();
        assertThat(bean.isDestroyCalled()).as("Bean destroyed too early").isFalse();
        this.beanFactory.destroySingletons();
        assertThat(bean.isDestroyCalled()).as("Bean not destroyed").isTrue();
        assertThat(bean.isCustomDestroyCalled()).as("Custom destroy method called incorrectly").isFalse();
    }

    @Test
    public void lifecycleMethodsDisabled() throws Exception {
        LifecycleAwareBean bean = (LifecycleAwareBean) this.beanFactory.getBean("lifecycleMethodsDisabled");
        assertThat(bean.isInitCalled()).as("Bean init method called incorrectly").isFalse();
        assertThat(bean.isCustomInitCalled()).as("Custom init method called incorrectly").isFalse();
        this.beanFactory.destroySingletons();
        assertThat(bean.isDestroyCalled()).as("Bean destroy method called incorrectly").isFalse();
        assertThat(bean.isCustomDestroyCalled()).as("Custom destroy method called incorrectly").isFalse();
    }

    @Test
    public void ignoreDefaultLifecycleMethods() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new ClassPathResource("ignoreDefaultLifecycleMethods.xml", getClass()));
        bf.preInstantiateSingletons();
        bf.destroySingletons();
    }

    @Test
    public void overrideDefaultLifecycleMethods() throws Exception {
        LifecycleAwareBean bean = (LifecycleAwareBean) this.beanFactory.getBean("overrideLifecycleMethods");
        assertThat(bean.isInitCalled()).as("Default init method called incorrectly").isFalse();
        assertThat(bean.isCustomInitCalled()).as("Custom init method not called").isTrue();
        this.beanFactory.destroySingletons();
        assertThat(bean.isDestroyCalled()).as("Default destroy method called incorrectly").isFalse();
        assertThat(bean.isCustomDestroyCalled()).as("Custom destroy method not called").isTrue();
    }

    @Test
    public void childWithDefaultLifecycleMethods() throws Exception {
        LifecycleAwareBean bean = (LifecycleAwareBean) this.beanFactory.getBean("childWithDefaultLifecycleMethods");
        assertThat(bean.isInitCalled()).as("Bean not initialized").isTrue();
        assertThat(bean.isCustomInitCalled()).as("Custom init method called incorrectly").isFalse();
        assertThat(bean.isDestroyCalled()).as("Bean destroyed too early").isFalse();
        this.beanFactory.destroySingletons();
        assertThat(bean.isDestroyCalled()).as("Bean not destroyed").isTrue();
        assertThat(bean.isCustomDestroyCalled()).as("Custom destroy method called incorrectly").isFalse();
    }

    @Test
    public void childWithLifecycleMethodsDisabled() throws Exception {
        LifecycleAwareBean bean = (LifecycleAwareBean) this.beanFactory.getBean("childWithLifecycleMethodsDisabled");
        assertThat(bean.isInitCalled()).as("Bean init method called incorrectly").isFalse();
        assertThat(bean.isCustomInitCalled()).as("Custom init method called incorrectly").isFalse();
        this.beanFactory.destroySingletons();
        assertThat(bean.isDestroyCalled()).as("Bean destroy method called incorrectly").isFalse();
        assertThat(bean.isCustomDestroyCalled()).as("Custom destroy method called incorrectly").isFalse();
    }

    public static class LifecycleAwareBean {

        private boolean initCalled;

        private boolean destroyCalled;

        private boolean customInitCalled;

        private boolean customDestroyCalled;

        public void init() {
            this.initCalled = true;
        }

        public void destroy() {
            this.destroyCalled = true;
        }

        public void customInit() {
            this.customInitCalled = true;
        }

        public void customDestroy() {
            this.customDestroyCalled = true;
        }

        public boolean isInitCalled() {
            return initCalled;
        }

        public boolean isDestroyCalled() {
            return destroyCalled;
        }

        public boolean isCustomInitCalled() {
            return customInitCalled;
        }

        public boolean isCustomDestroyCalled() {
            return customDestroyCalled;
        }
    }
}
