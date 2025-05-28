// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明示或暗示。请参阅许可证了解具体管理许可权和
* 限制的条款。*/
package org.springframework.beans.testfixture.beans;

import org.springframework.lang.Nullable;

/**
 * 简单嵌套测试 Bean，用于测试 Bean 工厂、AOP 框架等。
 *
 * @author Trevor D. Cook
 * @since 2003年9月30日
 */
public class NestedTestBean implements INestedTestBean {

    private String company = "";

    public NestedTestBean() {
    }

    public NestedTestBean(String company) {
        setCompany(company);
    }

    public void setCompany(String company) {
        this.company = (company != null ? company : "");
    }

    @Override
    public String getCompany() {
        return company;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof NestedTestBean ntb)) {
            return false;
        }
        return this.company.equals(ntb.company);
    }

    @Override
    public int hashCode() {
        return this.company.hashCode();
    }

    @Override
    public String toString() {
        return "NestedTestBean: " + this.company;
    }
}
