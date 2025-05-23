// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为还是适销性。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.aop.framework;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.MethodMatcher;

/**
 * 内部框架记录，结合一个 {@link MethodInterceptor} 实例与一个 {@link MethodMatcher}，用作顾问链中的元素。
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @param interceptor 方法拦截器实例
 * @param matcher 方法匹配器
 */
record InterceptorAndDynamicMethodMatcher(MethodInterceptor interceptor, MethodMatcher matcher) {
}
