// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.aop.aspectj;

import org.springframework.core.Ordered;

/**
 * 该接口由能够提供信息以按照 AspectJ 的优先级规则对建议/顾问进行排序的类型实现。
 *
 * @author Adrian Colyer
 * @since 2.0
 * @see org.springframework.aop.aspectj.autoproxy.AspectJPrecedenceComparator
 */
public interface AspectJPrecedenceInformation extends Ordered {

    // 实现备注：
    // 我们需要这个接口提供的间接层次，否则否则
    // AspectJPrecedenceComparator 必须在所有情况下向 Advisor 请求其 Advice
    // 为了对顾问进行排序。这导致了一些问题。
    // InstantiationModelAwarePointcutAdvisor 需要延迟创建
    // 对于具有非单例实例化模式的方面，它的建议是：
    /**
     * 返回声明通知（增强）的方面（Bean）的名称。
     */
    String getAspectName();

    /**
     * 返回在方面中建议成员的声明顺序。
     */
    int getDeclarationOrder();

    /**
     * 返回这是否是前置通知。
     */
    boolean isBeforeAdvice();

    /**
     * 返回是否这是一个后置通知。
     */
    boolean isAfterAdvice();
}
