// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用的法律或书面同意，否则在许可协议下分发的软件是按“原样”分发的，
* 不提供任何明示或暗示的保证或条件。有关许可协议具体规定的权限和限制，请参阅许可协议。*/
package org.springframework.beans.factory.xml;

import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.beans.testfixture.beans.factory.DummyFactory;

/**
 * @author Juergen Hoeller
 * @since 2003年7月21日
 */
public class DummyReferencer {

    private TestBean testBean1;

    private TestBean testBean2;

    private DummyFactory dummyFactory;

    public DummyReferencer() {
    }

    public DummyReferencer(DummyFactory dummyFactory) {
        this.dummyFactory = dummyFactory;
    }

    public void setDummyFactory(DummyFactory dummyFactory) {
        this.dummyFactory = dummyFactory;
    }

    public DummyFactory getDummyFactory() {
        return dummyFactory;
    }

    public void setTestBean1(TestBean testBean1) {
        this.testBean1 = testBean1;
    }

    public TestBean getTestBean1() {
        return testBean1;
    }

    public void setTestBean2(TestBean testBean2) {
        this.testBean2 = testBean2;
    }

    public TestBean getTestBean2() {
        return testBean2;
    }
}
