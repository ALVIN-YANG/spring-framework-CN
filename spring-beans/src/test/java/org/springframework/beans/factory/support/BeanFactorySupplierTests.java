// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.support;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.function.ThrowingSupplier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * 测试对 {@link AbstractAutowireCapableBeanFactory} 实例供应商支持的功能。
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 */
public class BeanFactorySupplierTests {

    @Test
    void getBeanWhenUsingRegularSupplier() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setInstanceSupplier(() -> "I am supplied");
        beanFactory.registerBeanDefinition("test", beanDefinition);
        assertThat(beanFactory.getBean("test")).isEqualTo("I am supplied");
    }

    @Test
    void getBeanWithInnerBeanUsingRegularSupplier() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setInstanceSupplier(() -> "I am supplied");
        RootBeanDefinition outerBean = new RootBeanDefinition(String.class);
        outerBean.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition);
        beanFactory.registerBeanDefinition("test", outerBean);
        assertThat(beanFactory.getBean("test")).asString().startsWith("I am supplied");
    }

    @Test
    void getBeanWhenUsingInstanceSupplier() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        RootBeanDefinition beanDefinition = new RootBeanDefinition(String.class);
        beanDefinition.setInstanceSupplier(InstanceSupplier.of(registeredBean -> "I am bean " + registeredBean.getBeanName() + " of " + registeredBean.getBeanClass()));
        beanFactory.registerBeanDefinition("test", beanDefinition);
        assertThat(beanFactory.getBean("test")).isEqualTo("I am bean test of class java.lang.String");
    }

    @Test
    void getBeanWithInnerBeanUsingInstanceSupplier() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        RootBeanDefinition beanDefinition = new RootBeanDefinition(String.class);
        beanDefinition.setInstanceSupplier(InstanceSupplier.of(registeredBean -> "I am bean " + registeredBean.getBeanName() + " of " + registeredBean.getBeanClass()));
        RootBeanDefinition outerBean = new RootBeanDefinition(String.class);
        outerBean.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition);
        beanFactory.registerBeanDefinition("test", outerBean);
        assertThat(beanFactory.getBean("test")).asString().startsWith("I am bean (inner bean)").endsWith(" of class java.lang.String");
    }

    @Test
    void getBeanWhenUsingThrowableSupplier() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setInstanceSupplier(ThrowingSupplier.of(() -> "I am supplied"));
        beanFactory.registerBeanDefinition("test", beanDefinition);
        assertThat(beanFactory.getBean("test")).isEqualTo("I am supplied");
    }

    @Test
    void getBeanWithInnerBeanUsingThrowableSupplier() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setInstanceSupplier(ThrowingSupplier.of(() -> "I am supplied"));
        RootBeanDefinition outerBean = new RootBeanDefinition(String.class);
        outerBean.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition);
        beanFactory.registerBeanDefinition("test", outerBean);
        assertThat(beanFactory.getBean("test")).asString().startsWith("I am supplied");
    }

    @Test
    void getBeanWhenUsingThrowableSupplierThatThrowsCheckedException() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setInstanceSupplier(ThrowingSupplier.of(() -> {
            throw new IOException("fail");
        }));
        beanFactory.registerBeanDefinition("test", beanDefinition);
        assertThatExceptionOfType(BeanCreationException.class).isThrownBy(() -> beanFactory.getBean("test")).withCauseInstanceOf(IOException.class);
    }

    @Test
    void getBeanWhenUsingThrowableSupplierThatThrowsRuntimeException() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setInstanceSupplier(ThrowingSupplier.of(() -> {
            throw new IllegalStateException("fail");
        }));
        beanFactory.registerBeanDefinition("test", beanDefinition);
        assertThatExceptionOfType(BeanCreationException.class).isThrownBy(() -> beanFactory.getBean("test")).withCauseInstanceOf(IllegalStateException.class);
    }
}
