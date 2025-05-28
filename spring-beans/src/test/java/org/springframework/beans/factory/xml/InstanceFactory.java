// 翻译完成 glm-4-flash
/** 版权所有 2002-2013 原作者或作者。
*
* 根据 Apache 许可协议版本 2.0 ("许可协议") 授权；
* 您必须遵守许可协议才能使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可协议分发的软件
* 是按“现状”分发的，不提供任何形式的明示或暗示保证，
* 包括但不限于对适销性、特定用途适用性或非侵权性的保证。
* 请参阅许可协议，了解管理许可和限制的具体语言。*/
package org.springframework.beans.factory.xml;

import org.springframework.beans.testfixture.beans.FactoryMethods;
import org.springframework.beans.testfixture.beans.TestBean;

/**
 * 测试类，用于验证 Spring 是否能够使用静态工厂方法而不是构造函数来创建对象。
 *
 * @author Rod Johnson
 */
public class InstanceFactory {

    protected static int count = 0;

    private String factoryBeanProperty;

    public InstanceFactory() {
        count++;
    }

    public void setFactoryBeanProperty(String s) {
        this.factoryBeanProperty = s;
    }

    public String getFactoryBeanProperty() {
        return this.factoryBeanProperty;
    }

    public FactoryMethods defaultInstance() {
        TestBean tb = new TestBean();
        tb.setName(this.factoryBeanProperty);
        return FactoryMethods.newInstance(tb);
    }

    /**
     * 注意，支持方法重载。
     */
    public FactoryMethods newInstance(TestBean tb) {
        return FactoryMethods.newInstance(tb);
    }

    public FactoryMethods newInstance(TestBean tb, int num, String name) {
        return FactoryMethods.newInstance(tb, num, name);
    }
}
