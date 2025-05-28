// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性或非侵权性。
* 请参阅许可证了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.testfixture.beans.factory.aot;

/**
 * 一个绑定目标类型的 {@link GenericFactoryBean}。
 *
 * @author Stephane Nicoll
 */
public class NumberFactoryBean<T extends Number> extends GenericFactoryBean<T> {

    public NumberFactoryBean(Class<T> beanType) {
        super(beanType);
    }
}
