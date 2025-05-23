// 翻译完成 glm-4-flash
/*版权所有 2002-2021 原作者或原作者。

根据Apache License 2.0（以下简称“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途的适用性和非侵权性。
有关许可的具体语言和限制，请参阅许可证。*/
package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * AOP联盟Advice的子接口，允许Advice实现额外的接口，并通过使用该拦截器的代理来访问这些接口。这是一个称为<b>引入</b>的AOP基本概念。
 *
 * <p>引入通常被用作<b>混入</b>，使得可以构建能够实现多个继承在Java中许多目标的多重对象。
 *
 * <p>与{@link IntroductionInfo}相比，此接口允许Advice实现一系列接口，这些接口事先不一定已知。因此，可以使用{@link IntroductionAdvisor}来指定在advised对象中暴露哪些接口。
 *
 * @author Rod Johnson
 * @since 1.1.1
 * @see IntroductionInfo
 * @see IntroductionAdvisor
 */
public interface DynamicIntroductionAdvice extends Advice {

    /**
     * 这个介绍建议是否实现了给定的接口？
     * @param intf 要检查的接口
     * @return 是否实现了指定的接口
     */
    boolean implementsInterface(Class<?> intf);
}
