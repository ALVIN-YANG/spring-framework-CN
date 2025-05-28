// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.scheduling.aspectj;

import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.config.TaskManagementConfigUtils;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Stephane Nicoll
 */
public class AnnotationDrivenBeanDefinitionParserTests {

    private ConfigurableApplicationContext context;

    @BeforeEach
    public void setup() {
        this.context = new ClassPathXmlApplicationContext("annotationDrivenContext.xml", AnnotationDrivenBeanDefinitionParserTests.class);
    }

    @AfterEach
    public void after() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void asyncAspectRegistered() {
        assertThat(context.containsBean(TaskManagementConfigUtils.ASYNC_EXECUTION_ASPECT_BEAN_NAME)).isTrue();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void asyncPostProcessorExecutorReference() {
        Object executor = context.getBean("testExecutor");
        Object aspect = context.getBean(TaskManagementConfigUtils.ASYNC_EXECUTION_ASPECT_BEAN_NAME);
        assertThat(((Supplier) new DirectFieldAccessor(aspect).getPropertyValue("defaultExecutor")).get()).isSameAs(executor);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void asyncPostProcessorExceptionHandlerReference() {
        Object exceptionHandler = context.getBean("testExceptionHandler");
        Object aspect = context.getBean(TaskManagementConfigUtils.ASYNC_EXECUTION_ASPECT_BEAN_NAME);
        assertThat(((Supplier) new DirectFieldAccessor(aspect).getPropertyValue("exceptionHandler")).get()).isSameAs(exceptionHandler);
    }
}
