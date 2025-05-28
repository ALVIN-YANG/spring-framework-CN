// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 您不得使用此文件，除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明。有关许可权限和限制的具体语言，
* 请参阅许可证。*/
package org.springframework.beans.testfixture.factory.xml;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Rod Johnson
 * @作者 Juergen Hoeller
 */
public abstract class AbstractListableBeanFactoryTests extends AbstractBeanFactoryTests {

    /**
     * 子类必须初始化此
     */
    protected ListableBeanFactory getListableBeanFactory() {
        BeanFactory bf = getBeanFactory();
        if (!(bf instanceof ListableBeanFactory)) {
            throw new IllegalStateException("ListableBeanFactory required");
        }
        return (ListableBeanFactory) bf;
    }

    /**
     * 子类可以重写此方法。
     */
    @Test
    public void count() {
        assertCount(13);
    }

    protected final void assertCount(int count) {
        String[] defnames = getListableBeanFactory().getBeanDefinitionNames();
        assertThat(defnames.length).as("We should have " + count + " beans, not " + defnames.length).isEqualTo(count);
    }

    protected void assertTestBeanCount(int count) {
        String[] defNames = getListableBeanFactory().getBeanNamesForType(TestBean.class, true, false);
        assertThat(defNames.length).as("We should have " + count + " beans for class org.springframework.beans.testfixture.beans.TestBean, not " + defNames.length).isEqualTo(count);
        int countIncludingFactoryBeans = count + 2;
        String[] names = getListableBeanFactory().getBeanNamesForType(TestBean.class, true, true);
        assertThat(names.length).as("We should have " + countIncludingFactoryBeans + " beans for class org.springframework.beans.testfixture.beans.TestBean, not " + names.length).isEqualTo(countIncludingFactoryBeans);
    }

    @Test
    public void getDefinitionsForNoSuchClass() {
        String[] defnames = getListableBeanFactory().getBeanNamesForType(String.class);
        assertThat(defnames.length).as("No string definitions").isEqualTo(0);
    }

    /**
     * 检查count是否引用的是工厂类，而不是Bean类。（我们不知道工厂可能返回什么类型，甚至可能随时间改变。）
     */
    @Test
    public void getCountForFactoryClass() {
        assertThat(getListableBeanFactory().getBeanNamesForType(FactoryBean.class).length).as("Should have 2 factories, not " + getListableBeanFactory().getBeanNamesForType(FactoryBean.class).length).isEqualTo(2);
        assertThat(getListableBeanFactory().getBeanNamesForType(FactoryBean.class).length).as("Should have 2 factories, not " + getListableBeanFactory().getBeanNamesForType(FactoryBean.class).length).isEqualTo(2);
    }

    @Test
    public void containsBeanDefinition() {
        assertThat(getListableBeanFactory().containsBeanDefinition("rod")).isTrue();
        assertThat(getListableBeanFactory().containsBeanDefinition("roderick")).isTrue();
    }
}
