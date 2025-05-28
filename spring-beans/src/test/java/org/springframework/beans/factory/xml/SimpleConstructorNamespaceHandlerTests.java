// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 您不得使用此文件除非遵守许可证规定。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.testfixture.beans.DummyBean;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @作者 Costin Leau
 */
public class SimpleConstructorNamespaceHandlerTests {

    @Test
    public void simpleValue() throws Exception {
        DefaultListableBeanFactory beanFactory = createFactory("simpleConstructorNamespaceHandlerTests.xml");
        String name = "simple";
        // beanFactory.getBean("simple1", DummyBean.class); 的中文翻译为：beanFactory.getBean("simple1", DummyBean.class); 请注意，这里的 "beanFactory" 和 "DummyBean" 是类名或变量名，不应翻译，保持原样。这句话的中文意思为：从beanFactory中获取名为"simple1"的Bean实例，其类型为DummyBean。
        DummyBean nameValue = beanFactory.getBean(name, DummyBean.class);
        assertThat(nameValue.getValue()).isEqualTo("simple");
    }

    @Test
    public void simpleRef() throws Exception {
        DefaultListableBeanFactory beanFactory = createFactory("simpleConstructorNamespaceHandlerTests.xml");
        String name = "simple-ref";
        // beanFactory.getBean("name-value1", TestBean.class); 的注释内容翻译成中文可以是：从 beanFactory 中获取名为 "name-value1" 的 bean 实例，其类型为 TestBean。
        DummyBean nameValue = beanFactory.getBean(name, DummyBean.class);
        assertThat(nameValue.getValue()).isEqualTo(beanFactory.getBean("name"));
    }

    @Test
    public void nameValue() throws Exception {
        DefaultListableBeanFactory beanFactory = createFactory("simpleConstructorNamespaceHandlerTests.xml");
        String name = "name-value";
        // beanFactory.getBean("name-value1", TestBean.class); 的中文翻译为：beanFactory.getBean("名称-值1", TestBean.class);
        TestBean nameValue = beanFactory.getBean(name, TestBean.class);
        assertThat(nameValue.getName()).isEqualTo(name);
        assertThat(nameValue.getAge()).isEqualTo(10);
    }

    @Test
    public void nameRef() throws Exception {
        DefaultListableBeanFactory beanFactory = createFactory("simpleConstructorNamespaceHandlerTests.xml");
        TestBean nameValue = beanFactory.getBean("name-value", TestBean.class);
        DummyBean nameRef = beanFactory.getBean("name-ref", DummyBean.class);
        assertThat(nameRef.getName()).isEqualTo("some-name");
        assertThat(nameRef.getSpouse()).isEqualTo(nameValue);
    }

    @Test
    public void typeIndexedValue() throws Exception {
        DefaultListableBeanFactory beanFactory = createFactory("simpleConstructorNamespaceHandlerTests.xml");
        DummyBean typeRef = beanFactory.getBean("indexed-value", DummyBean.class);
        assertThat(typeRef.getName()).isEqualTo("at");
        assertThat(typeRef.getValue()).isEqualTo("austria");
        assertThat(typeRef.getAge()).isEqualTo(10);
    }

    @Test
    public void typeIndexedRef() throws Exception {
        DefaultListableBeanFactory beanFactory = createFactory("simpleConstructorNamespaceHandlerTests.xml");
        DummyBean typeRef = beanFactory.getBean("indexed-ref", DummyBean.class);
        assertThat(typeRef.getName()).isEqualTo("some-name");
        assertThat(typeRef.getSpouse()).isEqualTo(beanFactory.getBean("name-value"));
    }

    @Test
    public void ambiguousConstructor() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        assertThatExceptionOfType(BeanDefinitionStoreException.class).isThrownBy(() -> new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new ClassPathResource("simpleConstructorNamespaceHandlerTestsWithErrors.xml", getClass())));
    }

    @Test
    public void constructorWithNameEndingInRef() throws Exception {
        DefaultListableBeanFactory beanFactory = createFactory("simpleConstructorNamespaceHandlerTests.xml");
        DummyBean derivedBean = beanFactory.getBean("beanWithRefConstructorArg", DummyBean.class);
        assertThat(derivedBean.getAge()).isEqualTo(10);
        assertThat(derivedBean.getName()).isEqualTo("silly name");
    }

    private DefaultListableBeanFactory createFactory(String resourceName) {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new ClassPathResource(resourceName, getClass()));
        return bf;
    }
}
