// 翻译完成 glm-4-flash
/*版权所有 2002-2007 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可，您可以使用此文件，但必须遵守许可证条款。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

/**
 * 用于在 {@link ProxyCreatorSupport} 对象上注册的监听器
 * 允许接收激活和通知变更的回调。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ProxyCreatorSupport#addListener
 */
public interface AdvisedSupportListener {

    /**
     * 当创建第一个代理时被调用。
     * @param advised AdvisedSupport 对象
     */
    void activated(AdvisedSupport advised);

    /**
     * 在创建代理之后，当通知（Advice）被修改时调用。
     * @param advised AdvisedSupport 对象
     */
    void adviceChanged(AdvisedSupport advised);
}
