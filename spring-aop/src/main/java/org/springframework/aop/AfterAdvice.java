// 翻译完成 glm-4-flash
/*版权所有 2002-2007 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * 用于after advice的通用标记接口，例如{@link AfterReturningAdvice}和{@link ThrowsAdvice}。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see BeforeAdvice
 */
public interface AfterAdvice extends Advice {
}
