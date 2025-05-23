// 翻译完成 glm-4-flash
/**
 * 用于在ApplicationContext中使用的Bean后处理器，通过自动创建AOP代理来简化AOP的使用，无需使用ProxyFactoryBean。
 *
 * <p>本包中的各种后处理器只需添加到ApplicationContext中（通常在XML bean定义文档中），就可以自动代理所选的Bean。
 *
 * <p><b>注意</b>：对于BeanFactory实现，不支持自动自动代理，因为后处理器Bean仅在应用程序上下文中自动检测。可以在ConfigurableBeanFactory上显式注册后处理器。
 */
@NonNullApi
@NonNullFields
package org.springframework.aop.framework.autoproxy;

import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;

