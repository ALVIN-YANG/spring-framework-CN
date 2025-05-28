// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.testfixture.beans;

import java.util.Collections;
import java.util.List;

/**
 * 测试类，用于验证Spring通过静态工厂方法创建对象的能力，而不是使用构造函数。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class FactoryMethods {

    public static FactoryMethods nullInstance() {
        return null;
    }

    public static FactoryMethods defaultInstance() {
        TestBean tb = new TestBean();
        tb.setName("defaultInstance");
        return new FactoryMethods(tb, "default", 0);
    }

    /**
     * 注意，支持方法重载。
     */
    public static FactoryMethods newInstance(TestBean tb) {
        return new FactoryMethods(tb, "default", 0);
    }

    public static FactoryMethods newInstance(TestBean tb, int num, String name) {
        if (name == null) {
            throw new IllegalStateException("Should never be called with null value");
        }
        return new FactoryMethods(tb, name, num);
    }

    static ExtendedFactoryMethods newInstance(TestBean tb, int num, Integer something) {
        if (something != null) {
            throw new IllegalStateException("Should never be called with non-null value");
        }
        return new ExtendedFactoryMethods(tb, null, num);
    }

    @SuppressWarnings("unused")
    private static List<?> listInstance() {
        return Collections.EMPTY_LIST;
    }

    private int num = 0;

    private String name = "default";

    private TestBean tb;

    private String stringValue;

    /**
     * 构造函数是私有的：不适用于本类外部使用，即使是 IoC 容器也不行。
     */
    private FactoryMethods(TestBean tb, String name, int num) {
        this.tb = tb;
        this.name = name;
        this.num = num;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public TestBean getTestBean() {
        return this.tb;
    }

    protected TestBean protectedGetTestBean() {
        return this.tb;
    }

    @SuppressWarnings("unused")
    private TestBean privateGetTestBean() {
        return this.tb;
    }

    public int getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    /**
     * 在实例创建后通过setter方法进行注入。
     */
    public void setName(String name) {
        this.name = name;
    }

    public static class ExtendedFactoryMethods extends FactoryMethods {

        ExtendedFactoryMethods(TestBean tb, String name, int num) {
            super(tb, name, num);
        }
    }
}
