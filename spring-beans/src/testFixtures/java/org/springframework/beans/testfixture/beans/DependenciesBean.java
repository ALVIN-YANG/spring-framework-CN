// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（"许可证"），除非法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按"原样"提供，
* 不提供任何明示或暗示的保证或条件。有关许可协议下授权和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.testfixture.beans;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * 简单的Bean，用于测试依赖检查。
 *
 * @author Rod Johnson
 * @since 2003年9月4日
 */
public class DependenciesBean implements BeanFactoryAware {

    private int age;

    private String name;

    private TestBean spouse;

    private BeanFactory beanFactory;

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSpouse(TestBean spouse) {
        this.spouse = spouse;
    }

    public TestBean getSpouse() {
        return spouse;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }
}
