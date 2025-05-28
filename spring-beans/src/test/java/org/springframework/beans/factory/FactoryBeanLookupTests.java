// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的质量保证或适用性保证；
* 请参阅许可证了解具体语言规范权限和限制。*/
package org.springframework.beans.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 编写此代码的目的是重现SPR-7318问题。
 *
 * @author Chris Beams
 */
public class FactoryBeanLookupTests {

    private BeanFactory beanFactory;

    @BeforeEach
    public void setUp() {
        beanFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader((BeanDefinitionRegistry) beanFactory).loadBeanDefinitions(new ClassPathResource("FactoryBeanLookupTests-context.xml", this.getClass()));
    }

    @Test
    public void factoryBeanLookupByNameDereferencing() {
        Object fooFactory = beanFactory.getBean("&fooFactory");
        assertThat(fooFactory).isInstanceOf(FooFactoryBean.class);
    }

    @Test
    public void factoryBeanLookupByType() {
        FooFactoryBean fooFactory = beanFactory.getBean(FooFactoryBean.class);
        assertThat(fooFactory).isNotNull();
    }

    @Test
    public void factoryBeanLookupByTypeAndNameDereference() {
        FooFactoryBean fooFactory = beanFactory.getBean("&fooFactory", FooFactoryBean.class);
        assertThat(fooFactory).isNotNull();
    }

    @Test
    public void factoryBeanObjectLookupByName() {
        Object fooFactory = beanFactory.getBean("fooFactory");
        assertThat(fooFactory).isInstanceOf(Foo.class);
    }

    @Test
    public void factoryBeanObjectLookupByNameAndType() {
        Foo foo = beanFactory.getBean("fooFactory", Foo.class);
        assertThat(foo).isNotNull();
    }
}

class FooFactoryBean extends AbstractFactoryBean<Foo> {

    @Override
    protected Foo createInstance() throws Exception {
        return new Foo();
    }

    @Override
    public Class<?> getObjectType() {
        return Foo.class;
    }
}

class Foo {
}
