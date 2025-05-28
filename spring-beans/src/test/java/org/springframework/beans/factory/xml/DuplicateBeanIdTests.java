// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 您不得使用此文件除非符合许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

/**
 * Bean id 属性（以及核心模式中的所有其他 id 属性）不类型化为 xsd:id，而是作为 xsd:string 类型。这允许在嵌套的 `<beans>` 元素中使用相同的 bean id。
 *
 * <p>在同一嵌套级别内的重复 ID 仍然会通过 ProblemReporter 被视为错误，因为这绝不可能是一个有意或有效的情况。
 *
 * @author Chris Beams
 * @since 3.1
 * @see org.springframework.beans.factory.xml.XmlBeanFactoryTests#withDuplicateName
 * @see org.springframework.beans.factory.xml.XmlBeanFactoryTests#withDuplicateNameInAlias
 */
class DuplicateBeanIdTests {

    @Test
    void duplicateBeanIdsWithinSameNestingLevelRaisesError() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        assertThatException().as("duplicate ids in same nesting level").isThrownBy(() -> reader.loadBeanDefinitions(new ClassPathResource("DuplicateBeanIdTests-sameLevel-context.xml", this.getClass())));
    }

    @Test
    void duplicateBeanIdsAcrossNestingLevels() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.loadBeanDefinitions(new ClassPathResource("DuplicateBeanIdTests-multiLevel-context.xml", this.getClass()));
        // 应该只有一个
        TestBean testBean = bf.getBean(TestBean.class);
        assertThat(testBean.getName()).isEqualTo("nested");
    }
}
