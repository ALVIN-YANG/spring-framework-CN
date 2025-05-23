// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory;

/**
 * 与 {@link BeanNameAware} 的对应接口。返回对象的 Bean 名称。
 *
 * <p>此接口可以用于避免在使用 Spring IoC 和 Spring AOP 的对象中对 Bean 名称的脆弱依赖。
 *
 * @author Rod Johnson
 * @since 2.0
 * @see BeanNameAware
 */
public interface NamedBean {

    /**
     * 如果已知，返回此bean在Spring bean工厂中的名称。
     */
    String getBeanName();
}
