// 翻译完成 glm-4-flash
/** 版权所有 2002-2013 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式的保证或条件，
* 无论是明示的还是暗示的。请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml;

import java.io.Serializable;
import org.springframework.beans.testfixture.beans.IndexedTestBean;
import org.springframework.beans.testfixture.beans.TestBean;

/**
 * 简单的Bean，用于检查构造函数依赖检查。
 *
 * @author Juergen Hoeller
 * @since 09.11.2003
 */
@SuppressWarnings("serial")
public class ConstructorDependenciesBean implements Serializable {

    private int age;

    private String name;

    private TestBean spouse1;

    private TestBean spouse2;

    private IndexedTestBean other;

    public ConstructorDependenciesBean(int age) {
        this.age = age;
    }

    public ConstructorDependenciesBean(String name) {
        this.name = name;
    }

    public ConstructorDependenciesBean(TestBean spouse1) {
        this.spouse1 = spouse1;
    }

    public ConstructorDependenciesBean(TestBean spouse1, TestBean spouse2) {
        this.spouse1 = spouse1;
        this.spouse2 = spouse2;
    }

    public ConstructorDependenciesBean(TestBean spouse1, TestBean spouse2, int age) {
        this.spouse1 = spouse1;
        this.spouse2 = spouse2;
        this.age = age;
    }

    public ConstructorDependenciesBean(TestBean spouse1, TestBean spouse2, IndexedTestBean other) {
        this.spouse1 = spouse1;
        this.spouse2 = spouse2;
        this.other = other;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public TestBean getSpouse1() {
        return spouse1;
    }

    public TestBean getSpouse2() {
        return spouse2;
    }

    public IndexedTestBean getOther() {
        return other;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }
}
