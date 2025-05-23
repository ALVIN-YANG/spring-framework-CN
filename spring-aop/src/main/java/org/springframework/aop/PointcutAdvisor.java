// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者们。

根据Apache License，版本2.0（以下简称“许可证”）授权；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop;

/**
 * 所有由切入点驱动的顾问的超级接口。
 * 这几乎涵盖了所有顾问，除了介绍型顾问，
 * 对于介绍型顾问，方法级别的匹配不适用。
 *
 * @作者 Rod Johnson
 */
public interface PointcutAdvisor extends Advisor {

    /**
     * 获取驱动此顾问的切点（Pointcut）。
     */
    Pointcut getPointcut();
}
