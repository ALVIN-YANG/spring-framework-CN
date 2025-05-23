// 翻译完成 glm-4-flash
/*版权所有 2002-2006 原作者或作者们。

根据Apache License, Version 2.0（“许可证”）许可；
除非根据适用法律要求或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下地址获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”提供，
不提供任何形式的质量保证或适用性保证，无论是明示的还是暗示的。
有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.aspectj;

import org.springframework.aop.PointcutAdvisor;

/**
 * 该接口由Spring AOP顾问实现，用于封装可能具有延迟初始化策略的AspectJ方面。例如，如果使用perThis实例化模型，则意味着对通知进行延迟初始化。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public interface InstantiationModelAwarePointcutAdvisor extends PointcutAdvisor {

    /**
     * 返回此顾问是否正在懒加载其底层建议。
     */
    boolean isLazy();

    /**
     * 返回此顾问是否已经实例化了其建议。
     */
    boolean isAdviceInstantiated();
}
