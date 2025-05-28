// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 您不得使用此文件除非符合许可证规定。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按照“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体规定许可和限制。*/
package org.springframework.scheduling.aspectj;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.annotation.AbstractAsyncConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

/**
 * 用于注册Spring基础设施Bean的{@code @Configuration}类，这些Bean是启用基于AspectJ的异步方法执行所必需的。
 *
 * @author Chris Beams
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 3.1
 * @see EnableAsync
 * @see org.springframework.scheduling.annotation.AsyncConfigurationSelector
 * @see org.springframework.scheduling.annotation.ProxyAsyncConfiguration
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AspectJAsyncConfiguration extends AbstractAsyncConfiguration {

    @Bean(name = TaskManagementConfigUtils.ASYNC_EXECUTION_ASPECT_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public AnnotationAsyncExecutionAspect asyncAdvisor() {
        AnnotationAsyncExecutionAspect asyncAspect = AnnotationAsyncExecutionAspect.aspectOf();
        asyncAspect.configure(this.executor, this.exceptionHandler);
        return asyncAspect;
    }
}
