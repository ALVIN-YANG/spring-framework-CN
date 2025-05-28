// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律要求或书面同意，否则在许可证下分发的软件按 "原样" 提供分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证以了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.BeanNameAware;

/**
 * @作者 Rob Harrop
 */
public class GeneratedNameBean implements BeanNameAware {

    private String beanName;

    private GeneratedNameBean child;

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setChild(GeneratedNameBean child) {
        this.child = child;
    }

    public GeneratedNameBean getChild() {
        return child;
    }
}
