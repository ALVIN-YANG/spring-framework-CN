// 翻译完成 glm-4-flash
/**
 * SPI（服务提供者接口）包，允许Spring AOP框架处理任意类型的建议。
 *
 * <p>那些只想<i>使用</i>Spring AOP框架而不是扩展其功能的用户，不需要关心这个包。
 *
 * <p>您可能希望使用这些适配器来将Spring特定的建议（如MethodBeforeAdvice）包装在MethodInterceptor中，以便在其他支持AOP Alliance接口的AOP框架中使用。
 *
 * <p>这些适配器不依赖于任何其他Spring框架类，以允许这种使用。
 */
@NonNullApi
@NonNullFields
package org.springframework.aop.framework.adapter;

import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;

