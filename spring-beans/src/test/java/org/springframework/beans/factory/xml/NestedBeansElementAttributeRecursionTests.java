// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试将封装的 beans 元素默认值传播到嵌套的 beans 元素。
 *
 * @author Chris Beams
 */
public class NestedBeansElementAttributeRecursionTests {

    @Test
    public void defaultLazyInit() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new ClassPathResource("NestedBeansElementAttributeRecursionTests-lazy-context.xml", this.getClass()));
        assertLazyInits(bf);
    }

    @Test
    public void defaultLazyInitWithNonValidatingParser() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(bf);
        xmlBeanDefinitionReader.setValidating(false);
        xmlBeanDefinitionReader.loadBeanDefinitions(new ClassPathResource("NestedBeansElementAttributeRecursionTests-lazy-context.xml", this.getClass()));
        assertLazyInits(bf);
    }

    private void assertLazyInits(DefaultListableBeanFactory bf) {
        BeanDefinition foo = bf.getBeanDefinition("foo");
        BeanDefinition bar = bf.getBeanDefinition("bar");
        BeanDefinition baz = bf.getBeanDefinition("baz");
        BeanDefinition biz = bf.getBeanDefinition("biz");
        BeanDefinition buz = bf.getBeanDefinition("buz");
        assertThat(foo.isLazyInit()).isFalse();
        assertThat(bar.isLazyInit()).isTrue();
        assertThat(baz.isLazyInit()).isFalse();
        assertThat(biz.isLazyInit()).isTrue();
        assertThat(buz.isLazyInit()).isTrue();
    }

    @Test
    public void defaultMerge() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new ClassPathResource("NestedBeansElementAttributeRecursionTests-merge-context.xml", this.getClass()));
        assertMerge(bf);
    }

    @Test
    public void defaultMergeWithNonValidatingParser() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(bf);
        xmlBeanDefinitionReader.setValidating(false);
        xmlBeanDefinitionReader.loadBeanDefinitions(new ClassPathResource("NestedBeansElementAttributeRecursionTests-merge-context.xml", this.getClass()));
        assertMerge(bf);
    }

    @SuppressWarnings("unchecked")
    private void assertMerge(DefaultListableBeanFactory bf) {
        TestBean topLevel = bf.getBean("topLevelConcreteTestBean", TestBean.class);
        // 具有具体的子 bean 值
        assertThat((Iterable<String>) topLevel.getSomeList()).contains("charlie", "delta");
        // 但不合并父值
        assertThat((Iterable<String>) topLevel.getSomeList()).doesNotContain("alpha", "bravo");
        TestBean firstLevel = bf.getBean("firstLevelNestedTestBean", TestBean.class);
        // 合并所有值
        assertThat((Iterable<String>) firstLevel.getSomeList()).contains("charlie", "delta", "echo", "foxtrot");
        TestBean secondLevel = bf.getBean("secondLevelNestedTestBean", TestBean.class);
        // 合并所有值
        assertThat((Iterable<String>) secondLevel.getSomeList()).contains("charlie", "delta", "echo", "foxtrot", "golf", "hotel");
    }

    @Test
    public void defaultAutowireCandidates() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new ClassPathResource("NestedBeansElementAttributeRecursionTests-autowire-candidates-context.xml", this.getClass()));
        assertAutowireCandidates(bf);
    }

    @Test
    public void defaultAutowireCandidatesWithNonValidatingParser() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(bf);
        xmlBeanDefinitionReader.setValidating(false);
        xmlBeanDefinitionReader.loadBeanDefinitions(new ClassPathResource("NestedBeansElementAttributeRecursionTests-autowire-candidates-context.xml", this.getClass()));
        assertAutowireCandidates(bf);
    }

    private void assertAutowireCandidates(DefaultListableBeanFactory bf) {
        assertThat(bf.getBeanDefinition("fooService").isAutowireCandidate()).isTrue();
        assertThat(bf.getBeanDefinition("fooRepository").isAutowireCandidate()).isTrue();
        assertThat(bf.getBeanDefinition("other").isAutowireCandidate()).isFalse();
        assertThat(bf.getBeanDefinition("barService").isAutowireCandidate()).isTrue();
        assertThat(bf.getBeanDefinition("fooController").isAutowireCandidate()).isFalse();
        assertThat(bf.getBeanDefinition("bizRepository").isAutowireCandidate()).isTrue();
        assertThat(bf.getBeanDefinition("bizService").isAutowireCandidate()).isFalse();
        assertThat(bf.getBeanDefinition("bazService").isAutowireCandidate()).isTrue();
        assertThat(bf.getBeanDefinition("random").isAutowireCandidate()).isFalse();
        assertThat(bf.getBeanDefinition("fooComponent").isAutowireCandidate()).isFalse();
        assertThat(bf.getBeanDefinition("fRepository").isAutowireCandidate()).isFalse();
        assertThat(bf.getBeanDefinition("aComponent").isAutowireCandidate()).isTrue();
        assertThat(bf.getBeanDefinition("someService").isAutowireCandidate()).isFalse();
    }

    @Test
    public void initMethod() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new ClassPathResource("NestedBeansElementAttributeRecursionTests-init-destroy-context.xml", this.getClass()));
        InitDestroyBean beanA = bf.getBean("beanA", InitDestroyBean.class);
        InitDestroyBean beanB = bf.getBean("beanB", InitDestroyBean.class);
        InitDestroyBean beanC = bf.getBean("beanC", InitDestroyBean.class);
        InitDestroyBean beanD = bf.getBean("beanD", InitDestroyBean.class);
        assertThat(beanA.initMethod1Called).isTrue();
        assertThat(beanB.initMethod2Called).isTrue();
        assertThat(beanC.initMethod3Called).isTrue();
        assertThat(beanD.initMethod2Called).isTrue();
        bf.destroySingletons();
        assertThat(beanA.destroyMethod1Called).isTrue();
        assertThat(beanB.destroyMethod2Called).isTrue();
        assertThat(beanC.destroyMethod3Called).isTrue();
        assertThat(beanD.destroyMethod2Called).isTrue();
    }
}

class InitDestroyBean {

    boolean initMethod1Called;

    boolean initMethod2Called;

    boolean initMethod3Called;

    boolean destroyMethod1Called;

    boolean destroyMethod2Called;

    boolean destroyMethod3Called;

    void initMethod1() {
        this.initMethod1Called = true;
    }

    void initMethod2() {
        this.initMethod2Called = true;
    }

    void initMethod3() {
        this.initMethod3Called = true;
    }

    void destroyMethod1() {
        this.destroyMethod1Called = true;
    }

    void destroyMethod2() {
        this.destroyMethod2Called = true;
    }

    void destroyMethod3() {
        this.destroyMethod3Called = true;
    }
}
