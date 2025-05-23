// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.parsing;

/**
 * 表示一个Bean定义的`ParseState`条目。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class BeanEntry implements ParseState.Entry {

    private final String beanDefinitionName;

    /**
     * 创建一个新的 {@code BeanEntry} 实例。
     * @param beanDefinitionName 相关的 Bean 定义名称
     */
    public BeanEntry(String beanDefinitionName) {
        this.beanDefinitionName = beanDefinitionName;
    }

    @Override
    public String toString() {
        return "Bean '" + this.beanDefinitionName + "'";
    }
}
