// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）授权；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律强制规定或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体规定权限和限制的内容。*/
package org.springframework.beans.testfixture.beans;

/**
 * @作者 Costin Leau
 */
public class DummyBean {

    private Object value;

    private String name;

    private int age;

    private TestBean spouse;

    public DummyBean(Object value) {
        this.value = value;
    }

    public DummyBean(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public DummyBean(int ageRef, String nameRef) {
        this.name = nameRef;
        this.age = ageRef;
    }

    public DummyBean(String name, TestBean spouse) {
        this.name = name;
        this.spouse = spouse;
    }

    public DummyBean(String name, Object value, int age) {
        this.name = name;
        this.value = value;
        this.age = age;
    }

    public Object getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public TestBean getSpouse() {
        return spouse;
    }
}
