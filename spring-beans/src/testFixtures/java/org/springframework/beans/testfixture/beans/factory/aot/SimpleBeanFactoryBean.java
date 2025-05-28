// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式（明示或暗示）的保证或条件。
* 请参阅许可证以了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.testfixture.beans.factory.aot;

import org.springframework.beans.factory.FactoryBean;

/**
 * 一个公开的 {@link FactoryBean}，为 {@link GenericFactoryBean} 解析了泛型。
 *
 * @author Stephane Nicoll
 */
public class SimpleBeanFactoryBean extends GenericFactoryBean<SimpleBean> {

    public SimpleBeanFactoryBean() {
        super(SimpleBean.class);
    }
}
