// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Karl Pietrzak
 * @author Juergen Hoeller
 *
 * 作者：Karl Pietrzak
 * 作者：Juergen Hoeller
 */
public class LookupAnnotationTests {

    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        beanFactory = new DefaultListableBeanFactory();
        AutowiredAnnotationBeanPostProcessor aabpp = new AutowiredAnnotationBeanPostProcessor();
        aabpp.setBeanFactory(beanFactory);
        beanFactory.addBeanPostProcessor(aabpp);
        beanFactory.registerBeanDefinition("abstractBean", new RootBeanDefinition(AbstractBean.class));
        beanFactory.registerBeanDefinition("beanConsumer", new RootBeanDefinition(BeanConsumer.class));
        RootBeanDefinition tbd = new RootBeanDefinition(TestBean.class);
        tbd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        beanFactory.registerBeanDefinition("testBean", tbd);
    }

    @Test
    public void testWithoutConstructorArg() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("abstractBean");
        Object expected = bean.get();
        assertThat(expected.getClass()).isEqualTo(TestBean.class);
        assertThat(beanFactory.getBean(BeanConsumer.class).abstractBean).isSameAs(bean);
    }

    @Test
    public void testWithOverloadedArg() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("abstractBean");
        TestBean expected = bean.get("haha");
        assertThat(expected.getClass()).isEqualTo(TestBean.class);
        assertThat(expected.getName()).isEqualTo("haha");
        assertThat(beanFactory.getBean(BeanConsumer.class).abstractBean).isSameAs(bean);
    }

    @Test
    public void testWithOneConstructorArg() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("abstractBean");
        TestBean expected = bean.getOneArgument("haha");
        assertThat(expected.getClass()).isEqualTo(TestBean.class);
        assertThat(expected.getName()).isEqualTo("haha");
        assertThat(beanFactory.getBean(BeanConsumer.class).abstractBean).isSameAs(bean);
    }

    @Test
    public void testWithTwoConstructorArg() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("abstractBean");
        TestBean expected = bean.getTwoArguments("haha", 72);
        assertThat(expected.getClass()).isEqualTo(TestBean.class);
        assertThat(expected.getName()).isEqualTo("haha");
        assertThat(expected.getAge()).isEqualTo(72);
        assertThat(beanFactory.getBean(BeanConsumer.class).abstractBean).isSameAs(bean);
    }

    @Test
    public void testWithThreeArgsShouldFail() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("abstractBean");
        assertThatExceptionOfType(AbstractMethodError.class).as("TestBean has no three arg constructor").isThrownBy(() -> bean.getThreeArguments("name", 1, 2));
        assertThat(beanFactory.getBean(BeanConsumer.class).abstractBean).isSameAs(bean);
    }

    @Test
    public void testWithEarlyInjection() {
        AbstractBean bean = beanFactory.getBean("beanConsumer", BeanConsumer.class).abstractBean;
        Object expected = bean.get();
        assertThat(expected.getClass()).isEqualTo(TestBean.class);
        assertThat(beanFactory.getBean(BeanConsumer.class).abstractBean).isSameAs(bean);
    }

    // gh-25806（此标识通常是 GitHub 上提交号或者 pull request 的标识，翻译成中文为：GitHub-25806）
    @Test
    public void testWithNullBean() {
        RootBeanDefinition tbd = new RootBeanDefinition(TestBean.class, () -> null);
        tbd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        beanFactory.registerBeanDefinition("testBean", tbd);
        AbstractBean bean = beanFactory.getBean("beanConsumer", BeanConsumer.class).abstractBean;
        Object expected = bean.get();
        assertThat(expected).isNull();
        assertThat(beanFactory.getBean(BeanConsumer.class).abstractBean).isSameAs(bean);
    }

    @Test
    public void testWithGenericBean() {
        beanFactory.registerBeanDefinition("numberBean", new RootBeanDefinition(NumberBean.class));
        beanFactory.registerBeanDefinition("doubleStore", new RootBeanDefinition(DoubleStore.class));
        beanFactory.registerBeanDefinition("floatStore", new RootBeanDefinition(FloatStore.class));
        NumberBean bean = (NumberBean) beanFactory.getBean("numberBean");
        assertThat(beanFactory.getBean(DoubleStore.class)).isSameAs(bean.getDoubleStore());
        assertThat(beanFactory.getBean(FloatStore.class)).isSameAs(bean.getFloatStore());
    }

    public static abstract class AbstractBean {

        @Lookup("testBean")
        public abstract TestBean get();

        @Lookup
        public abstract TestBean get(// 重载的
        String name);

        @Lookup
        public abstract TestBean getOneArgument(String name);

        @Lookup
        public abstract TestBean getTwoArguments(String name, int age);

        // 没有 @Lookup 注解
        public abstract TestBean getThreeArguments(String name, int age, int anotherArg);
    }

    public static class BeanConsumer {

        @Autowired
        AbstractBean abstractBean;
    }

    public static class NumberStore<T extends Number> {
    }

    public static class DoubleStore extends NumberStore<Double> {
    }

    public static class FloatStore extends NumberStore<Float> {
    }

    public static abstract class NumberBean {

        @Lookup
        public abstract NumberStore<Double> getDoubleStore();

        @Lookup
        public abstract NumberStore<Float> getFloatStore();
    }
}
