// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可以使用此文件，但必须遵守许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途的适用性或非侵权性。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.testfixture.beans;

import org.springframework.beans.factory.InitializingBean;

/**
 * 简单的 BeanFactory 初始化测试
 * @author Rod Johnson
 * @since 2003年3月12日
 */
public class MustBeInitialized implements InitializingBean {

    private boolean inited;

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     * 翻译为中文为：
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     * 请参见 org.springframework.beans.factory.InitializingBean 接口的 afterPropertiesSet() 方法。
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.inited = true;
    }

    /**
     * 一个虚拟的商务方法，除非工厂正确管理了该Bean的生命周期，否则会失败
     */
    public void businessMethod() {
        if (!this.inited) {
            throw new RuntimeException("Factory didn't call afterPropertiesSet() on MustBeInitialized object");
        }
    }
}
