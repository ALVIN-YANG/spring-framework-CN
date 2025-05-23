// 翻译完成 glm-4-flash
/*版权所有 2002-2007 原作者或作者们。

根据Apache许可证版本2.0（以下简称“许可证”）许可；除非符合许可证，否则您不得使用此文件。
您可以在以下地址获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性或非侵权性的保证。
有关许可证的具体语言管理权限和限制，请参阅许可证。*/
package org.springframework.aop;

/**
 * 所有AOP代理实现标记接口。用于检测对象是否是Spring生成的代理。
 *
 * @author Rob Harrop
 * @since 2.0.1
 * @see org.springframework.aop.support.AopUtils#isAopProxy(Object)
 */
public interface SpringProxy {
}
