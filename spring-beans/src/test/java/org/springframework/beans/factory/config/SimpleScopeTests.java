// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.testfixture.io.ResourceTestUtils.qualifiedResource;

/**
 * 简单测试，用于说明和验证作用域的使用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 */
public class SimpleScopeTests {

    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        beanFactory = new DefaultListableBeanFactory();
        Scope scope = new NoOpScope() {

            private int index;

            private List<TestBean> objects = new ArrayList<>();

            {
                objects.add(new TestBean());
                objects.add(new TestBean());
            }

            @Override
            public Object get(String name, ObjectFactory<?> objectFactory) {
                if (index >= objects.size()) {
                    index = 0;
                }
                return objects.get(index++);
            }
        };
        beanFactory.registerScope("myScope", scope);
        String[] scopeNames = beanFactory.getRegisteredScopeNames();
        assertThat(scopeNames).hasSize(1);
        assertThat(scopeNames[0]).isEqualTo("myScope");
        assertThat(beanFactory.getRegisteredScope("myScope")).isSameAs(scope);
        new XmlBeanDefinitionReader(beanFactory).loadBeanDefinitions(qualifiedResource(SimpleScopeTests.class, "context.xml"));
    }

    @Test
    public void testCanGetScopedObject() {
        TestBean tb1 = (TestBean) beanFactory.getBean("usesScope");
        TestBean tb2 = (TestBean) beanFactory.getBean("usesScope");
        assertThat(tb2).isNotSameAs(tb1);
        TestBean tb3 = (TestBean) beanFactory.getBean("usesScope");
        assertThat(tb1).isSameAs(tb3);
    }
}
