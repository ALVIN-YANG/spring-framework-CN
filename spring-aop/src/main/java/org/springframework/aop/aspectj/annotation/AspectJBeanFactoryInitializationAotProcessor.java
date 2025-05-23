// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
有关许可的特定语言管辖权限和限制，请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import java.util.List;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * `BeanFactoryInitializationAotProcessor` 实现类，负责注册 AOP 通知的提示。
 *
 * @author Sébastien Deleuze
 * @author Stéphane Nicoll
 * @since 6.0.11
 */
class AspectJBeanFactoryInitializationAotProcessor implements BeanFactoryInitializationAotProcessor {

    private static final boolean aspectJPresent = ClassUtils.isPresent("org.aspectj.lang.annotation.Pointcut", AspectJBeanFactoryInitializationAotProcessor.class.getClassLoader());

    @Override
    @Nullable
    public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
        if (aspectJPresent) {
            return AspectDelegate.processAheadOfTime(beanFactory);
        }
        return null;
    }

    /**
     * 内部类用于避免在运行时对AspectJ的硬依赖。
     */
    private static class AspectDelegate {

        @Nullable
        private static AspectContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
            BeanFactoryAspectJAdvisorsBuilder builder = new BeanFactoryAspectJAdvisorsBuilder(beanFactory);
            List<Advisor> advisors = builder.buildAspectJAdvisors();
            return (advisors.isEmpty() ? null : new AspectContribution(advisors));
        }
    }

    private static class AspectContribution implements BeanFactoryInitializationAotContribution {

        private final List<Advisor> advisors;

        public AspectContribution(List<Advisor> advisors) {
            this.advisors = advisors;
        }

        @Override
        public void applyTo(GenerationContext generationContext, BeanFactoryInitializationCode beanFactoryInitializationCode) {
            ReflectionHints reflectionHints = generationContext.getRuntimeHints().reflection();
            for (Advisor advisor : this.advisors) {
                if (advisor.getAdvice() instanceof AbstractAspectJAdvice aspectJAdvice) {
                    reflectionHints.registerMethod(aspectJAdvice.getAspectJAdviceMethod(), ExecutableMode.INVOKE);
                }
            }
        }
    }
}
