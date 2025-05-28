// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.support;

import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * SPR-8954 的单元测试，其中自定义的 {@link InstantiationAwareBeanPostProcessor} 强制了 FactoryBean 的预测类型，从而有效地防止了通过 #getBeansOfType(FactoryBean.class) 调用检索该 Bean。现在，{@link AbstractBeanFactory#isFactoryBean(String, RootBeanDefinition)} 的实现不仅考虑了预测的 Bean 类型，还考虑了原始 Bean 定义中的 beanClass。
 *
 * @author Chris Beams
 * @author Oliver Gierke
 */
public class Spr8954Tests {

    private DefaultListableBeanFactory bf;

    @BeforeEach
    public void setUp() {
        bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("foo", new RootBeanDefinition(FooFactoryBean.class));
        bf.addBeanPostProcessor(new PredictingBPP());
    }

    @Test
    public void repro() {
        assertThat(bf.getBean("foo")).isInstanceOf(Foo.class);
        assertThat(bf.getBean("&foo")).isInstanceOf(FooFactoryBean.class);
        assertThat(bf.isTypeMatch("&foo", FactoryBean.class)).isTrue();
        @SuppressWarnings("rawtypes")
        Map<String, FactoryBean> fbBeans = bf.getBeansOfType(FactoryBean.class);
        assertThat(fbBeans).hasSize(1);
        assertThat(fbBeans.keySet()).contains("&foo");
        Map<String, AnInterface> aiBeans = bf.getBeansOfType(AnInterface.class);
        assertThat(aiBeans).hasSize(1);
        assertThat(aiBeans.keySet()).contains("&foo");
    }

    @Test
    public void findsBeansByTypeIfNotInstantiated() {
        assertThat(bf.isTypeMatch("&foo", FactoryBean.class)).isTrue();
        @SuppressWarnings("rawtypes")
        Map<String, FactoryBean> fbBeans = bf.getBeansOfType(FactoryBean.class);
        assertThat(fbBeans.size()).isEqualTo(1);
        assertThat(fbBeans.keySet().iterator().next()).isEqualTo("&foo");
        Map<String, AnInterface> aiBeans = bf.getBeansOfType(AnInterface.class);
        assertThat(aiBeans).hasSize(1);
        assertThat(aiBeans.keySet()).contains("&foo");
    }

    /**
     *  SPR-10517
     *
     * 请注意，SPR-10517 看起来像是一个Spring框架的bug报告编号。在这种情况下，注释中的 "* SPR-10517" 并不是代码的一部分，而是一个标记或引用，通常用于指出相关的bug报告。因此，它的翻译保持不变，因为这是一个专有名词。以下是注释的翻译：
     *
     *  SPR-10517
     *
     * （这里的翻译没有变化，因为它是一个专有名词，通常不会翻译。）
     */
    @Test
    public void findsFactoryBeanNameByTypeWithoutInstantiation() {
        String[] names = bf.getBeanNamesForType(AnInterface.class, false, false);
        assertThat(Arrays.asList(names)).contains("&foo");
        Map<String, AnInterface> beans = bf.getBeansOfType(AnInterface.class, false, false);
        assertThat(beans).hasSize(1);
        assertThat(beans.keySet()).contains("&foo");
    }

    static class FooFactoryBean implements FactoryBean<Foo>, AnInterface {

        @Override
        public Foo getObject() throws Exception {
            return new Foo();
        }

        @Override
        public Class<?> getObjectType() {
            return Foo.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }

    interface AnInterface {
    }

    static class Foo {
    }

    interface PredictedType {
    }

    static class PredictedTypeImpl implements PredictedType {
    }

    static class PredictingBPP implements SmartInstantiationAwareBeanPostProcessor {

        @Override
        public Class<?> predictBeanType(Class<?> beanClass, String beanName) {
            return FactoryBean.class.isAssignableFrom(beanClass) ? PredictedType.class : null;
        }
    }
}
