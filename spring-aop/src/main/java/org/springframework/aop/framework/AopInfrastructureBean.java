// 翻译完成 glm-4-flash
/*版权所有 2002-2007 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”），除非法律要求或书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

/**
 * 标记接口，表示该Bean是Spring的AOP基础设施的一部分。特别是，这意味着即使切入点匹配，这样的Bean也不会受到自动代理的影响。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator
 * @see org.springframework.aop.scope.ScopedProxyFactoryBean
 */
public interface AopInfrastructureBean {
}
