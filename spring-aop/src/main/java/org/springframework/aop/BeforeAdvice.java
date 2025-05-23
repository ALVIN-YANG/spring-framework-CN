// 翻译完成 glm-4-flash
/** 版权所有 2002-2007 原作者或作者们。
*
* 根据Apache许可证第2版（“许可证”）授权；
* 您不得使用此文件除非遵守许可证规定。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“现状”分发，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性还是其特定用途。
* 请参阅许可证了解管理许可权和限制的具体语言。*/
package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * 用于方法前置通知的通用标记接口，例如 {@link MethodBeforeAdvice}。
 *
 * <p>Spring 仅支持方法前置通知。尽管这种情况不太可能改变，
 * 但此API设计为允许在未来如果需要的话添加字段通知。
 *
 * @author Rod Johnson
 * @see AfterAdvice
 */
public interface BeforeAdvice extends Advice {
}
