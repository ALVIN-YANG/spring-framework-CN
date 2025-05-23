// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.aot;

import org.springframework.beans.factory.support.RegisteredBean;

/**
 * 可用于排除对 {@link RegisteredBean} 进行 AOT（Ahead-of-Time）处理的过滤器。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
@FunctionalInterface
public interface BeanRegistrationExcludeFilter {

    /**
     * 返回是否应将已注册的 Bean 排除从 AOT（Ahead-of-Time）处理和注册。
     * @param registeredBean 已注册的 Bean
     * @return 如果应排除已注册的 Bean
     */
    boolean isExcludedFromAotProcessing(RegisteredBean registeredBean);
}
