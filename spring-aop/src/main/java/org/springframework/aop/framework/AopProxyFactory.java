// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者们。
 
根据Apache许可证版本2.0（以下简称“许可证”）许可；除非遵守许可证规定，否则不得使用此文件。
您可以在以下地址获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可证下管理许可和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

/**
 * 接口，由能够基于 {@link AdvisedSupport} 配置对象创建 AOP 代理的工厂实现。
 *
 * <p>代理应遵守以下契约：
 * <ul>
 * <li>它们应实现配置指示应代理的所有接口。
 * <li>它们应实现 {@link Advised} 接口。
 * <li>它们应实现 equals 方法以比较代理接口、通知和目标。
 * <li>如果所有通知和目标都是可序列化的，则它们应该是可序列化的。
 * <li>如果通知和目标都是线程安全的，则它们应该是线程安全的。
 * </ul>
 *
 * <p>代理可能允许或不允许进行通知更改。如果它们不允许通知更改（例如，因为配置已被冻结），则在尝试更改通知时，代理应抛出 {@link AopConfigException} 异常。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface AopProxyFactory {

    /**
     * 为给定的 AOP 配置创建一个 {@link AopProxy}。
     * @param config 以 AdvisedSupport 对象形式存在的 AOP 配置
     * @return 对应的 AOP 代理
     * @throws AopConfigException 如果配置无效，则抛出此异常
     */
    AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;
}
