// 翻译完成 glm-4-flash
/*版权所有 2002-2007 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可，您可以使用此文件，但必须遵守许可证条款。
您可以在以下链接获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * AOP Alliance MethodInterceptor 的子接口，允许拦截器实现额外的接口，并通过使用该拦截器的代理来访问这些接口。这是 AOP 中的一个基本概念，称为 <b>引入</b>。
 *
 * <p>引入通常被用作 <b>混入</b>，它能够构建出能够实现多个继承在 Java 中许多目标的复合对象。
 *
 * @author Rod Johnson
 * @see DynamicIntroductionAdvice
 */
public interface IntroductionInterceptor extends MethodInterceptor, DynamicIntroductionAdvice {
}
