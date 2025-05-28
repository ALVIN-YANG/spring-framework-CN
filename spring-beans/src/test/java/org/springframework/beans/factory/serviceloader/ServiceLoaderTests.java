// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory.serviceloader;

import java.util.List;
import java.util.ServiceLoader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Juergen Hoeller
 * @author Chris Beams
 *
 * 作者：Juergen Hoeller
 * 作者：Chris Beams
 */
class ServiceLoaderTests {

    @BeforeAll
    static void assumeDocumentBuilderFactoryCanBeLoaded() {
        assumeTrue(ServiceLoader.load(DocumentBuilderFactory.class).iterator().hasNext());
    }

    @Test
    void testServiceLoaderFactoryBean() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(ServiceLoaderFactoryBean.class);
        bd.getPropertyValues().add("serviceType", DocumentBuilderFactory.class.getName());
        bf.registerBeanDefinition("service", bd);
        ServiceLoader<?> serviceLoader = (ServiceLoader<?>) bf.getBean("service");
        assertThat(serviceLoader.iterator().next() instanceof DocumentBuilderFactory).isTrue();
    }

    @Test
    void testServiceFactoryBean() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(ServiceFactoryBean.class);
        bd.getPropertyValues().add("serviceType", DocumentBuilderFactory.class.getName());
        bf.registerBeanDefinition("service", bd);
        assertThat(bf.getBean("service") instanceof DocumentBuilderFactory).isTrue();
    }

    @Test
    void testServiceListFactoryBean() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(ServiceListFactoryBean.class);
        bd.getPropertyValues().add("serviceType", DocumentBuilderFactory.class.getName());
        bf.registerBeanDefinition("service", bd);
        List<?> serviceList = (List<?>) bf.getBean("service");
        assertThat(serviceList.get(0) instanceof DocumentBuilderFactory).isTrue();
    }
}
