// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。有关许可的特定语言管理权限和
* 限制，请参阅许可证。*/
package org.springframework.cache.aspectj;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.contextsupport.testfixture.jcache.AbstractJCacheAnnotationTests;

/**
 * @作者 Stephane Nicoll
 * @作者 Sam Brannen
 */
public class JCacheAspectJNamespaceConfigTests extends AbstractJCacheAnnotationTests {

    @Override
    protected ApplicationContext getApplicationContext() {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        // 禁止覆盖 Bean 定义以测试 https://github.com/spring-projects/spring-framework/pull/27499
        context.setAllowBeanDefinitionOverriding(false);
        context.load("/org/springframework/cache/config/annotation-jcache-aspectj.xml");
        context.refresh();
        return context;
    }
}
