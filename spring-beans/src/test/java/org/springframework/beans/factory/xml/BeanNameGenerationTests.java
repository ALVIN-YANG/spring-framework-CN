// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据Apache License，版本2.0（以下简称“许可证”）授权；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“现状”分发的，不提供任何形式，明示或暗示的保证。
* 请参阅许可证了解具体规定权限和限制。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 *
 * 作者：Rob Harrop
 * 作者：Juergen Hoeller
 */
public class BeanNameGenerationTests {

    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    public void setUp() {
        this.beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
        reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
        reader.loadBeanDefinitions(new ClassPathResource("beanNameGeneration.xml", getClass()));
    }

    @Test
    public void naming() {
        String className = GeneratedNameBean.class.getName();
        String targetName = className + BeanDefinitionReaderUtils.GENERATED_BEAN_NAME_SEPARATOR + "0";
        GeneratedNameBean topLevel1 = (GeneratedNameBean) beanFactory.getBean(targetName);
        assertThat(topLevel1).isNotNull();
        targetName = className + BeanDefinitionReaderUtils.GENERATED_BEAN_NAME_SEPARATOR + "1";
        GeneratedNameBean topLevel2 = (GeneratedNameBean) beanFactory.getBean(targetName);
        assertThat(topLevel2).isNotNull();
        GeneratedNameBean child1 = topLevel1.getChild();
        assertThat(child1.getBeanName()).isNotNull();
        assertThat(child1.getBeanName()).startsWith(className);
        GeneratedNameBean child2 = topLevel2.getChild();
        assertThat(child2.getBeanName()).isNotNull();
        assertThat(child2.getBeanName()).startsWith(className);
        assertThat(child1.getBeanName()).isNotEqualTo(child2.getBeanName());
    }
}
