// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.testfixture.beans.factory.generator.lifecycle.Destroy;
import org.springframework.beans.testfixture.beans.factory.generator.lifecycle.InferredDestroyBean;
import org.springframework.beans.testfixture.beans.factory.generator.lifecycle.Init;
import org.springframework.beans.testfixture.beans.factory.generator.lifecycle.InitDestroyBean;
import org.springframework.beans.testfixture.beans.factory.generator.lifecycle.MultiInitDestroyBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * 测试用于 {@link InitDestroyAnnotationBeanPostProcessor}。
 *
 * @since 6.0
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @author Sam Brannen
 */
class InitDestroyAnnotationBeanPostProcessorTests {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    @Test
    void processAheadOfTimeWhenNoCallbackDoesNotMutateRootBeanDefinition() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(NoInitDestroyBean.class);
        processAheadOfTime(beanDefinition);
        RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition();
        assertThat(mergedBeanDefinition.getInitMethodNames()).isNull();
        assertThat(mergedBeanDefinition.getDestroyMethodNames()).isNull();
    }

    @Test
    void processAheadOfTimeWhenHasInitDestroyAnnotationsAddsMethodNames() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(InitDestroyBean.class);
        processAheadOfTime(beanDefinition);
        RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition();
        assertThat(mergedBeanDefinition.getInitMethodNames()).containsExactly("initMethod");
        assertThat(mergedBeanDefinition.getDestroyMethodNames()).containsExactly("destroyMethod");
    }

    @Test
    void processAheadOfTimeWhenHasInitDestroyAnnotationsAndCustomDefinedMethodNamesAddsMethodNames() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(InitDestroyBean.class);
        beanDefinition.setInitMethodName("customInitMethod");
        beanDefinition.setDestroyMethodNames("customDestroyMethod");
        processAheadOfTime(beanDefinition);
        RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition();
        assertThat(mergedBeanDefinition.getInitMethodNames()).containsExactly("initMethod", "customInitMethod");
        assertThat(mergedBeanDefinition.getDestroyMethodNames()).containsExactly("destroyMethod", "customDestroyMethod");
    }

    @Test
    void processAheadOfTimeWhenHasInitDestroyAnnotationsAndOverlappingCustomDefinedMethodNamesFiltersDuplicates() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(InitDestroyBean.class);
        beanDefinition.setInitMethodName("initMethod");
        beanDefinition.setDestroyMethodNames("destroyMethod");
        processAheadOfTime(beanDefinition);
        RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition();
        assertThat(mergedBeanDefinition.getInitMethodNames()).containsExactly("initMethod");
        assertThat(mergedBeanDefinition.getDestroyMethodNames()).containsExactly("destroyMethod");
    }

    @Test
    void processAheadOfTimeWhenHasInferredDestroyMethodAddsDestroyMethodName() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(InferredDestroyBean.class);
        beanDefinition.setDestroyMethodNames(AbstractBeanDefinition.INFER_METHOD);
        processAheadOfTime(beanDefinition);
        RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition();
        assertThat(mergedBeanDefinition.getInitMethodNames()).isNull();
        assertThat(mergedBeanDefinition.getDestroyMethodNames()).containsExactly("close");
    }

    @Test
    void processAheadOfTimeWhenHasInferredDestroyMethodAndNoCandidateDoesNotMutateRootBeanDefinition() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(NoInitDestroyBean.class);
        beanDefinition.setDestroyMethodNames(AbstractBeanDefinition.INFER_METHOD);
        processAheadOfTime(beanDefinition);
        RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition();
        assertThat(mergedBeanDefinition.getInitMethodNames()).isNull();
        assertThat(mergedBeanDefinition.getDestroyMethodNames()).isNull();
    }

    @Test
    void processAheadOfTimeWhenHasMultipleInitDestroyAnnotationsAddsAllMethodNames() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(MultiInitDestroyBean.class);
        processAheadOfTime(beanDefinition);
        RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition();
        assertThat(mergedBeanDefinition.getInitMethodNames()).containsExactly("initMethod", "anotherInitMethod");
        assertThat(mergedBeanDefinition.getDestroyMethodNames()).containsExactly("anotherDestroyMethod", "destroyMethod");
    }

    @Test
    void processAheadOfTimeWithMultipleLevelsOfPublicAndPrivateInitAndDestroyMethods() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(CustomAnnotatedPrivateSameNameInitDestroyBean.class);
        // 我们明确地将“afterPropertiesSet”定义为“自定义初始化方法”。
        // 确保即使它具有相同的，也会将其跟踪为这样的
        // 名称为 InitializingBean#afterPropertiesSet()。
        beanDefinition.setInitMethodNames("afterPropertiesSet", "customInit");
        // 我们明确地将“destroy”定义为“自定义销毁方法”
        // 为了确保即使它与它具有相同的
        // 名称作为 DisposableBean#destroy()。
        beanDefinition.setDestroyMethodNames("destroy", "customDestroy");
        processAheadOfTime(beanDefinition);
        RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition();
        assertSoftly(softly -> {
            softly.assertThat(mergedBeanDefinition.getInitMethodNames()).containsExactly(// 完全限定的私有方法
            CustomAnnotatedPrivateInitDestroyBean.class.getName() + ".privateInit", // 完全限定的私有方法
            CustomAnnotatedPrivateSameNameInitDestroyBean.class.getName() + ".privateInit", "afterPropertiesSet", "customInit");
            softly.assertThat(mergedBeanDefinition.getDestroyMethodNames()).containsExactly(// 完全限定的私有方法
            CustomAnnotatedPrivateSameNameInitDestroyBean.class.getName() + ".privateDestroy", // 完全限定的私有方法
            CustomAnnotatedPrivateInitDestroyBean.class.getName() + ".privateDestroy", "destroy", "customDestroy");
        });
    }

    private void processAheadOfTime(RootBeanDefinition beanDefinition) {
        RegisteredBean registeredBean = registerBean(beanDefinition);
        assertThat(createAotBeanPostProcessor().processAheadOfTime(registeredBean)).isNull();
    }

    private RegisteredBean registerBean(RootBeanDefinition beanDefinition) {
        String beanName = "test";
        this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
        return RegisteredBean.of(this.beanFactory, beanName);
    }

    private RootBeanDefinition getMergedBeanDefinition() {
        return (RootBeanDefinition) this.beanFactory.getMergedBeanDefinition("test");
    }

    private InitDestroyAnnotationBeanPostProcessor createAotBeanPostProcessor() {
        InitDestroyAnnotationBeanPostProcessor beanPostProcessor = new InitDestroyAnnotationBeanPostProcessor();
        beanPostProcessor.setInitAnnotationType(Init.class);
        beanPostProcessor.setDestroyAnnotationType(Destroy.class);
        return beanPostProcessor;
    }

    static class NoInitDestroyBean {
    }

    static class CustomInitDestroyBean {

        public void customInit() {
        }

        public void customDestroy() {
        }
    }

    static class CustomInitializingDisposableBean extends CustomInitDestroyBean implements InitializingBean, DisposableBean {

        @Override
        public void afterPropertiesSet() {
        }

        @Override
        public void destroy() {
        }
    }

    static class CustomAnnotatedPrivateInitDestroyBean extends CustomInitializingDisposableBean {

        @Init
        private void privateInit() {
        }

        @Destroy
        private void privateDestroy() {
        }
    }

    static class CustomAnnotatedPrivateSameNameInitDestroyBean extends CustomAnnotatedPrivateInitDestroyBean {

        @Init
        @SuppressWarnings("unused")
        private void privateInit() {
        }

        @Destroy
        @SuppressWarnings("unused")
        private void privateDestroy() {
        }
    }
}
