// 翻译完成 glm-4-flash
/** 版权所有 2002-2013 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”），您可能不得使用此文件除非遵守许可证。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则根据许可证分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途适用性或不侵犯权利。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.xml;

import org.springframework.beans.testfixture.beans.TestBean;

/**
 * 测试类，用于验证Spring通过静态工厂方法而非构造函数创建对象的能力。
 * @author Rod Johnson
 */
public class TestBeanCreator {

    public static TestBean createTestBean(String name, int age) {
        TestBean tb = new TestBean();
        tb.setName(name);
        tb.setAge(age);
        return tb;
    }

    public static TestBean createTestBean() {
        TestBean tb = new TestBean();
        tb.setName("Tristan");
        tb.setAge(2);
        return tb;
    }
}
