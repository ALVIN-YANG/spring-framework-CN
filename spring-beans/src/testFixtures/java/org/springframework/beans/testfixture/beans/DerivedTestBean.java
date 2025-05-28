// 翻译完成 glm-4-flash
/** 版权所有 2002-2013 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能无法使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.testfixture.beans;

import java.io.Serializable;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;

/**
 * @author Juergen Hoeller
 * @since 2003年8月21日
 */
@SuppressWarnings("serial")
public class DerivedTestBean extends TestBean implements Serializable, BeanNameAware, DisposableBean {

    private String beanName;

    private boolean initialized;

    private boolean destroyed;

    public DerivedTestBean() {
    }

    public DerivedTestBean(String[] names) {
        if (names == null || names.length < 2) {
            throw new IllegalArgumentException("Invalid names array");
        }
        setName(names[0]);
        setBeanName(names[1]);
    }

    public static DerivedTestBean create(String[] names) {
        return new DerivedTestBean(names);
    }

    @Override
    public void setBeanName(String beanName) {
        if (this.beanName == null || beanName == null) {
            this.beanName = beanName;
        }
    }

    @Override
    public String getBeanName() {
        return beanName;
    }

    public void setActualSpouse(TestBean spouse) {
        setSpouse(spouse);
    }

    public void setSpouseRef(String name) {
        setSpouse(new TestBean(name));
    }

    @Override
    public TestBean getSpouse() {
        return (TestBean) super.getSpouse();
    }

    public void initialize() {
        this.initialized = true;
    }

    public boolean wasInitialized() {
        return initialized;
    }

    @Override
    public void destroy() {
        this.destroyed = true;
    }

    @Override
    public boolean wasDestroyed() {
        return destroyed;
    }
}
