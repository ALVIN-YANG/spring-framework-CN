// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。有关许可的具体语言规定权限和限制，
* 请参阅许可证。*/
package org.springframework.beans.factory.aot;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.aot.BeanRegistrationsAotContribution.Registration;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.lang.Nullable;

/**
 * 用于向注册bean贡献代码的`BeanFactoryInitializationAotProcessor`。
 *
 * @author Phillip Webb
 * @author Sebastien Deleuze
 * @author Stephane Nicoll
 * @author Brian Clozel
 * @since 6.0
 */
class BeanRegistrationsAotProcessor implements BeanFactoryInitializationAotProcessor {

    @Override
    @Nullable
    public BeanRegistrationsAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
        BeanDefinitionMethodGeneratorFactory beanDefinitionMethodGeneratorFactory = new BeanDefinitionMethodGeneratorFactory(beanFactory);
        Map<BeanRegistrationKey, Registration> registrations = new LinkedHashMap<>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            RegisteredBean registeredBean = RegisteredBean.of(beanFactory, beanName);
            BeanDefinitionMethodGenerator beanDefinitionMethodGenerator = beanDefinitionMethodGeneratorFactory.getBeanDefinitionMethodGenerator(registeredBean);
            if (beanDefinitionMethodGenerator != null) {
                registrations.put(new BeanRegistrationKey(beanName, registeredBean.getBeanClass()), new Registration(beanDefinitionMethodGenerator, beanFactory.getAliases(beanName)));
            }
        }
        if (registrations.isEmpty()) {
            return null;
        }
        return new BeanRegistrationsAotContribution(registrations);
    }
}
