// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可的具体语言管辖权限和限制，
* 请参阅许可证。*/
package org.springframework.beans.factory.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Karl Pietrzak
 * @author Juergen Hoeller
 *
 * 作者：Karl Pietrzak
 * 作者：Juergen Hoeller
 */
public class LookupMethodTests {

    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(new ClassPathResource("lookupMethodTests.xml", getClass()));
    }

    @Test
    public void testWithoutConstructorArg() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("abstractBean");
        assertThat(bean).isNotNull();
        Object expected = bean.get();
        assertThat(expected.getClass()).isEqualTo(TestBean.class);
    }

    @Test
    public void testWithOverloadedArg() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("abstractBean");
        assertThat(bean).isNotNull();
        TestBean expected = bean.get("haha");
        assertThat(expected.getClass()).isEqualTo(TestBean.class);
        assertThat(expected.getName()).isEqualTo("haha");
    }

    @Test
    public void testWithOneConstructorArg() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("abstractBean");
        assertThat(bean).isNotNull();
        TestBean expected = bean.getOneArgument("haha");
        assertThat(expected.getClass()).isEqualTo(TestBean.class);
        assertThat(expected.getName()).isEqualTo("haha");
    }

    @Test
    public void testWithTwoConstructorArg() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("abstractBean");
        assertThat(bean).isNotNull();
        TestBean expected = bean.getTwoArguments("haha", 72);
        assertThat(expected.getClass()).isEqualTo(TestBean.class);
        assertThat(expected.getName()).isEqualTo("haha");
        assertThat(expected.getAge()).isEqualTo(72);
    }

    @Test
    public void testWithThreeArgsShouldFail() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("abstractBean");
        assertThat(bean).isNotNull();
        assertThatExceptionOfType(AbstractMethodError.class).as("does not have a three arg constructor").isThrownBy(() -> bean.getThreeArguments("name", 1, 2));
    }

    @Test
    public void testWithOverriddenLookupMethod() {
        AbstractBean bean = (AbstractBean) beanFactory.getBean("extendedBean");
        assertThat(bean).isNotNull();
        TestBean expected = bean.getOneArgument("haha");
        assertThat(expected.getClass()).isEqualTo(TestBean.class);
        assertThat(expected.getName()).isEqualTo("haha");
        assertThat(expected.isJedi()).isTrue();
    }

    @Test
    public void testWithGenericBean() {
        RootBeanDefinition bd = new RootBeanDefinition(NumberBean.class);
        bd.getMethodOverrides().addOverride(new LookupOverride("getDoubleStore", null));
        bd.getMethodOverrides().addOverride(new LookupOverride("getFloatStore", null));
        beanFactory.registerBeanDefinition("numberBean", bd);
        beanFactory.registerBeanDefinition("doubleStore", new RootBeanDefinition(DoubleStore.class));
        beanFactory.registerBeanDefinition("floatStore", new RootBeanDefinition(FloatStore.class));
        NumberBean bean = (NumberBean) beanFactory.getBean("numberBean");
        assertThat(bean).isNotNull();
        assertThat(beanFactory.getBean(DoubleStore.class)).isSameAs(bean.getDoubleStore());
        assertThat(beanFactory.getBean(FloatStore.class)).isSameAs(bean.getFloatStore());
    }

    public static abstract class AbstractBean {

        public abstract TestBean get();

        // 重载的
        public abstract TestBean get(String name);

        public abstract TestBean getOneArgument(String name);

        public abstract TestBean getTwoArguments(String name, int age);

        public abstract TestBean getThreeArguments(String name, int age, int anotherArg);
    }

    public static class NumberStore<T extends Number> {
    }

    public static class DoubleStore extends NumberStore<Double> {
    }

    public static class FloatStore extends NumberStore<Float> {
    }

    public static abstract class NumberBean {

        public abstract NumberStore<Double> getDoubleStore();

        public abstract NumberStore<Float> getFloatStore();
    }
}
