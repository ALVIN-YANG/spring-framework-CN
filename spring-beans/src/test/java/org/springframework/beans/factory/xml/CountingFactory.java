// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权的保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.testfixture.beans.TestBean;

/**
 * @作者 Juergen Hoeller
 */
public class CountingFactory implements FactoryBean<String> {

    private static int factoryBeanInstanceCount = 0;

    /**
     * 清除静态状态。
     */
    public static void reset() {
        factoryBeanInstanceCount = 0;
    }

    public static int getFactoryBeanInstanceCount() {
        return factoryBeanInstanceCount;
    }

    public CountingFactory() {
        factoryBeanInstanceCount++;
    }

    public void setTestBean(TestBean tb) {
        if (tb.getSpouse() == null) {
            throw new IllegalStateException("TestBean needs to have spouse");
        }
    }

    @Override
    public String getObject() {
        return "myString";
    }

    @Override
    public Class<String> getObjectType() {
        return String.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
