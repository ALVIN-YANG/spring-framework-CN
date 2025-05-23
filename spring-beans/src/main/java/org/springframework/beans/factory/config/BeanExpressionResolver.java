// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非遵守许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、特定用途的适用性或不侵犯第三方权利。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * 通过评估表达式来解析值的策略接口，如果适用的话。
 *
 * <p>原始的 {@link org.springframework.beans.factory.BeanFactory} 并不包含此策略的默认实现。然而，{@link org.springframework.context.ApplicationContext} 的实现将提供开箱即用的表达式支持。
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface BeanExpressionResolver {

    /**
     * 如果适用，评估给定的值作为表达式；
     * 否则，以原样返回该值。
     * @param value 要评估为表达式的值
     * @param beanExpressionContext 评估表达式时使用的bean表达式上下文
     * @return 解析后的值（可能是给定的值原样）
     * @throws BeansException 如果评估失败
     */
    @Nullable
    Object evaluate(@Nullable String value, BeanExpressionContext beanExpressionContext) throws BeansException;
}
