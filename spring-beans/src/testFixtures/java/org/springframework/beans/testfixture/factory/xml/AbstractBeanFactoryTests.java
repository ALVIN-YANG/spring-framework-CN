// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.testfixture.factory.xml;

import java.beans.PropertyEditorSupport;
import java.util.StringTokenizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.testfixture.beans.LifecycleBean;
import org.springframework.beans.testfixture.beans.MustBeInitialized;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.beans.testfixture.beans.factory.DummyFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 子类必须初始化bean工厂以及它们需要的任何其他变量。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
public abstract class AbstractBeanFactoryTests {

    protected abstract BeanFactory getBeanFactory();

    /**
     * Roderick bean 继承自 rod，仅重写 name 属性。
     */
    @Test
    public void inheritance() {
        assertThat(getBeanFactory().containsBean("rod")).isTrue();
        assertThat(getBeanFactory().containsBean("roderick")).isTrue();
        TestBean rod = (TestBean) getBeanFactory().getBean("rod");
        TestBean roderick = (TestBean) getBeanFactory().getBean("roderick");
        assertThat(rod).as("not == ").isNotSameAs(roderick);
        assertThat(rod.getName().equals("Rod")).as("rod.name is Rod").isTrue();
        assertThat(rod.getAge()).as("rod.age is 31").isEqualTo(31);
        assertThat(roderick.getName().equals("Roderick")).as("roderick.name is Roderick").isTrue();
        assertThat(roderick.getAge()).as("roderick.age was inherited").isEqualTo(rod.getAge());
    }

    @Test
    public void getBeanWithNullArg() {
        assertThatIllegalArgumentException().isThrownBy(() -> getBeanFactory().getBean((String) null));
    }

    /**
     * 测试InitializingBean对象是否接收到afterPropertiesSet()回调
     */
    @Test
    public void initializingBeanCallback() {
        MustBeInitialized mbi = (MustBeInitialized) getBeanFactory().getBean("mustBeInitialized");
        // 模拟业务方法将在以下情况下抛出异常：
        // `afterPropertiesSet()` 回调未调用
        mbi.businessMethod();
    }

    /**
     * 测试 InitializingBean/BeanFactoryAware/DisposableBean 对象是否在 BeanFactoryAware 回调之前接收到 afterPropertiesSet() 回调
     */
    @Test
    public void lifecycleCallbacks() {
        LifecycleBean lb = (LifecycleBean) getBeanFactory().getBean("lifecycle");
        assertThat(lb.getBeanName()).isEqualTo("lifecycle");
        // 模拟业务方法如果抛出异常，将执行以下操作：
        // 必要的回调未按正确顺序调用。
        lb.businessMethod();
        boolean condition = !lb.isDestroyed();
        assertThat(condition).as("Not destroyed").isTrue();
    }

    @Test
    public void findsValidInstance() {
        Object o = getBeanFactory().getBean("rod");
        boolean condition = o instanceof TestBean;
        assertThat(condition).as("Rod bean is a TestBean").isTrue();
        TestBean rod = (TestBean) o;
        assertThat(rod.getName().equals("Rod")).as("rod.name is Rod").isTrue();
        assertThat(rod.getAge()).as("rod.age is 31").isEqualTo(31);
    }

    @Test
    public void getInstanceByMatchingClass() {
        Object o = getBeanFactory().getBean("rod", TestBean.class);
        boolean condition = o instanceof TestBean;
        assertThat(condition).as("Rod bean is a TestBean").isTrue();
    }

    @Test
    public void getInstanceByNonmatchingClass() {
        assertThatExceptionOfType(BeanNotOfRequiredTypeException.class).isThrownBy(() -> getBeanFactory().getBean("rod", BeanFactory.class)).satisfies(ex -> {
            assertThat(ex.getBeanName()).isEqualTo("rod");
            assertThat(ex.getRequiredType()).isEqualTo(BeanFactory.class);
            assertThat(ex.getActualType()).isEqualTo(TestBean.class).isEqualTo(getBeanFactory().getBean("rod").getClass());
        });
    }

    @Test
    public void getSharedInstanceByMatchingClass() {
        Object o = getBeanFactory().getBean("rod", TestBean.class);
        boolean condition = o instanceof TestBean;
        assertThat(condition).as("Rod bean is a TestBean").isTrue();
    }

    @Test
    public void getSharedInstanceByMatchingClassNoCatch() {
        Object o = getBeanFactory().getBean("rod", TestBean.class);
        boolean condition = o instanceof TestBean;
        assertThat(condition).as("Rod bean is a TestBean").isTrue();
    }

    @Test
    public void getSharedInstanceByNonmatchingClass() {
        assertThatExceptionOfType(BeanNotOfRequiredTypeException.class).isThrownBy(() -> getBeanFactory().getBean("rod", BeanFactory.class)).satisfies(ex -> {
            assertThat(ex.getBeanName()).isEqualTo("rod");
            assertThat(ex.getRequiredType()).isEqualTo(BeanFactory.class);
            assertThat(ex.getActualType()).isEqualTo(TestBean.class);
        });
    }

    @Test
    public void sharedInstancesAreEqual() {
        Object o = getBeanFactory().getBean("rod");
        boolean condition1 = o instanceof TestBean;
        assertThat(condition1).as("Rod bean1 is a TestBean").isTrue();
        Object o1 = getBeanFactory().getBean("rod");
        boolean condition = o1 instanceof TestBean;
        assertThat(condition).as("Rod bean2 is a TestBean").isTrue();
        assertThat(o).as("Object equals applies").isSameAs(o1);
    }

    @Test
    public void prototypeInstancesAreIndependent() {
        TestBean tb1 = (TestBean) getBeanFactory().getBean("kathy");
        TestBean tb2 = (TestBean) getBeanFactory().getBean("kathy");
        assertThat(tb1).as("ref equal DOES NOT apply").isNotSameAs(tb2);
        assertThat(tb1.equals(tb2)).as("object equal true").isTrue();
        tb1.setAge(1);
        tb2.setAge(2);
        assertThat(tb1.getAge()).as("1 age independent = 1").isEqualTo(1);
        assertThat(tb2.getAge()).as("2 age independent = 2").isEqualTo(2);
        boolean condition = !tb1.equals(tb2);
        assertThat(condition).as("object equal now false").isTrue();
    }

    @Test
    public void notThere() {
        assertThat(getBeanFactory().containsBean("Mr Squiggle")).isFalse();
        assertThatExceptionOfType(BeansException.class).isThrownBy(() -> getBeanFactory().getBean("Mr Squiggle"));
    }

    @Test
    public void validEmpty() {
        Object o = getBeanFactory().getBean("validEmpty");
        boolean condition = o instanceof TestBean;
        assertThat(condition).as("validEmpty bean is a TestBean").isTrue();
        TestBean ve = (TestBean) o;
        assertThat(ve.getName() == null && ve.getAge() == 0 && ve.getSpouse() == null).as("Valid empty has defaults").isTrue();
    }

    @Test
    public void typeMismatch() {
        assertThatExceptionOfType(BeanCreationException.class).isThrownBy(() -> getBeanFactory().getBean("typeMismatch")).withCauseInstanceOf(TypeMismatchException.class);
    }

    @Test
    public void grandparentDefinitionFoundInBeanFactory() throws Exception {
        TestBean dad = (TestBean) getBeanFactory().getBean("father");
        assertThat(dad.getName().equals("Albert")).as("Dad has correct name").isTrue();
    }

    @Test
    public void factorySingleton() throws Exception {
        assertThat(getBeanFactory().isSingleton("&singletonFactory")).isTrue();
        assertThat(getBeanFactory().isSingleton("singletonFactory")).isTrue();
        TestBean tb = (TestBean) getBeanFactory().getBean("singletonFactory");
        assertThat(tb.getName().equals(DummyFactory.SINGLETON_NAME)).as("Singleton from factory has correct name, not " + tb.getName()).isTrue();
        DummyFactory factory = (DummyFactory) getBeanFactory().getBean("&singletonFactory");
        TestBean tb2 = (TestBean) getBeanFactory().getBean("singletonFactory");
        assertThat(tb).as("Singleton references ==").isSameAs(tb2);
        assertThat(factory.getBeanFactory()).as("FactoryBean is BeanFactoryAware").isNotNull();
    }

    @Test
    public void factoryPrototype() throws Exception {
        assertThat(getBeanFactory().isSingleton("&prototypeFactory")).isTrue();
        assertThat(getBeanFactory().isSingleton("prototypeFactory")).isFalse();
        TestBean tb = (TestBean) getBeanFactory().getBean("prototypeFactory");
        boolean condition = !tb.getName().equals(DummyFactory.SINGLETON_NAME);
        assertThat(condition).isTrue();
        TestBean tb2 = (TestBean) getBeanFactory().getBean("prototypeFactory");
        assertThat(tb).as("Prototype references !=").isNotSameAs(tb2);
    }

    /**
     * 检查我们是否可以获取工厂bean本身。
     * 这只在处理工厂时才可能实现。
     */
    @Test
    public void getFactoryItself() throws Exception {
        assertThat(getBeanFactory().getBean("&singletonFactory")).isNotNull();
    }

    /**
     * 检查在工厂上调用afterPropertiesSet方法
     */
    @Test
    public void factoryIsInitialized() throws Exception {
        TestBean tb = (TestBean) getBeanFactory().getBean("singletonFactory");
        assertThat(tb).isNotNull();
        DummyFactory factory = (DummyFactory) getBeanFactory().getBean("&singletonFactory");
        assertThat(factory.wasInitialized()).as("Factory was initialized because it implemented InitializingBean").isTrue();
    }

    /**
     * 将普通对象作为工厂来解引用应该是不合法的。
     */
    @Test
    public void rejectsFactoryGetOnNormalBean() {
        assertThatExceptionOfType(BeanIsNotAFactoryException.class).isThrownBy(() -> getBeanFactory().getBean("&rod"));
    }

    // 待办事项：在 AbstractBeanFactory 中重构（为 AbstractBeanFactory 编写测试用例）
    // 将此类重命名
    @Test
    public void aliasing() {
        BeanFactory bf = getBeanFactory();
        if (!(bf instanceof ConfigurableBeanFactory cbf)) {
            return;
        }
        String alias = "rods alias";
        assertThatExceptionOfType(NoSuchBeanDefinitionException.class).isThrownBy(() -> cbf.getBean(alias)).satisfies(ex -> assertThat(ex.getBeanName()).isEqualTo(alias));
        // 创建别名
        cbf.registerAlias("rod", alias);
        Object rod = getBeanFactory().getBean("rod");
        Object aliasRod = getBeanFactory().getBean(alias);
        assertThat(rod).isSameAs(aliasRod);
    }

    public static class TestBeanEditor extends PropertyEditorSupport {

        @Override
        public void setAsText(String text) {
            TestBean tb = new TestBean();
            StringTokenizer st = new StringTokenizer(text, "_");
            tb.setName(st.nextToken());
            tb.setAge(Integer.parseInt(st.nextToken()));
            setValue(tb);
        }
    }
}
