// 翻译完成 glm-4-flash
/**
 * 核心Spring AOP接口，基于AOP Alliance AOP互操作接口构建。
 *
 * <p>任何AOP Alliance MethodInterceptor都可以在Spring中使用。
 *
 * <br>Spring AOP还提供：
 * <ul>
 * <li>引入支持
 * <li>点切面（Pointcut）抽象，支持“静态”点切面（基于类和方法）和“动态”点切面（也考虑方法参数）。目前没有AOP Alliance接口为点切面。
 * <li>一系列完整的建议（Advice）类型，包括环绕（around）、之前（before）、返回后（after returning）和抛出（throws）建议。
 * <li>可扩展性，允许在不修改核心框架的情况下，插入任意的自定义建议类型。
 * </ul>
 *
 * <p>Spring AOP可以通过编程方式使用，或者（更推荐）与Spring IoC容器集成。
 */
@NonNullApi
@NonNullFields
package org.springframework.aop;

import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;

