// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用，除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体规定权限和限制。*/
package org.springframework.beans.factory.annotation;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.Test;
import org.springframework.aot.generate.MethodReference;
import org.springframework.aot.generate.MethodReference.ArgumentCodeGenerator;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.aot.test.generate.TestGenerationContext;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.testfixture.beans.factory.annotation.PackagePrivateFieldInjectionSample;
import org.springframework.beans.testfixture.beans.factory.annotation.PackagePrivateMethodInjectionSample;
import org.springframework.beans.testfixture.beans.factory.annotation.PrivateFieldInjectionSample;
import org.springframework.beans.testfixture.beans.factory.annotation.PrivateMethodInjectionSample;
import org.springframework.beans.testfixture.beans.factory.annotation.subpkg.PackagePrivateFieldInjectionFromParentSample;
import org.springframework.beans.testfixture.beans.factory.annotation.subpkg.PackagePrivateMethodInjectionFromParentSample;
import org.springframework.beans.testfixture.beans.factory.aot.MockBeanRegistrationCode;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.test.tools.CompileWithForkedClassLoader;
import org.springframework.core.test.tools.Compiled;
import org.springframework.core.test.tools.SourceFile;
import org.springframework.core.test.tools.TestCompiler;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试用于 AOT（Ahead-Of-Time）贡献的 {@link AutowiredAnnotationBeanPostProcessor}。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
class AutowiredAnnotationBeanRegistrationAotContributionTests {

    private final TestGenerationContext generationContext;

    private final MockBeanRegistrationCode beanRegistrationCode;

    private final DefaultListableBeanFactory beanFactory;

    private final AutowiredAnnotationBeanPostProcessor beanPostProcessor;

    AutowiredAnnotationBeanRegistrationAotContributionTests() {
        this.generationContext = new TestGenerationContext();
        this.beanRegistrationCode = new MockBeanRegistrationCode(this.generationContext);
        this.beanFactory = new DefaultListableBeanFactory();
        this.beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
        this.beanPostProcessor.setBeanFactory(this.beanFactory);
    }

    @Test
    void contributeWhenPrivateFieldInjectionInjectsUsingReflection() {
        Environment environment = new StandardEnvironment();
        this.beanFactory.registerSingleton("environment", environment);
        RegisteredBean registeredBean = getAndApplyContribution(PrivateFieldInjectionSample.class);
        assertThat(RuntimeHintsPredicates.reflection().onField(PrivateFieldInjectionSample.class, "environment")).accepts(this.generationContext.getRuntimeHints());
        compile(registeredBean, (postProcessor, compiled) -> {
            PrivateFieldInjectionSample instance = new PrivateFieldInjectionSample();
            postProcessor.apply(registeredBean, instance);
            assertThat(instance).extracting("environment").isSameAs(environment);
            assertThat(getSourceFile(compiled, PrivateFieldInjectionSample.class)).contains("resolveAndSet(");
        });
    }

    @Test
    @CompileWithForkedClassLoader
    void contributeWhenPackagePrivateFieldInjectionInjectsUsingConsumer() {
        Environment environment = new StandardEnvironment();
        this.beanFactory.registerSingleton("environment", environment);
        RegisteredBean registeredBean = getAndApplyContribution(PackagePrivateFieldInjectionSample.class);
        assertThat(RuntimeHintsPredicates.reflection().onField(PackagePrivateFieldInjectionSample.class, "environment")).accepts(this.generationContext.getRuntimeHints());
        compile(registeredBean, (postProcessor, compiled) -> {
            PackagePrivateFieldInjectionSample instance = new PackagePrivateFieldInjectionSample();
            postProcessor.apply(registeredBean, instance);
            assertThat(instance).extracting("environment").isSameAs(environment);
            assertThat(getSourceFile(compiled, PackagePrivateFieldInjectionSample.class)).contains("instance.environment =");
        });
    }

    @Test
    @CompileWithForkedClassLoader
    void contributeWhenPackagePrivateFieldInjectionOnParentClassInjectsUsingReflection() {
        Environment environment = new StandardEnvironment();
        this.beanFactory.registerSingleton("environment", environment);
        RegisteredBean registeredBean = getAndApplyContribution(PackagePrivateFieldInjectionFromParentSample.class);
        assertThat(RuntimeHintsPredicates.reflection().onField(PackagePrivateFieldInjectionSample.class, "environment")).accepts(this.generationContext.getRuntimeHints());
        compile(registeredBean, (postProcessor, compiled) -> {
            PackagePrivateFieldInjectionFromParentSample instance = new PackagePrivateFieldInjectionFromParentSample();
            postProcessor.apply(registeredBean, instance);
            assertThat(instance).extracting("environment").isSameAs(environment);
            assertThat(getSourceFile(compiled, PackagePrivateFieldInjectionFromParentSample.class)).contains("resolveAndSet");
        });
    }

    @Test
    void contributeWhenPrivateMethodInjectionInjectsUsingReflection() {
        Environment environment = new StandardEnvironment();
        this.beanFactory.registerSingleton("environment", environment);
        RegisteredBean registeredBean = getAndApplyContribution(PrivateMethodInjectionSample.class);
        assertThat(RuntimeHintsPredicates.reflection().onMethod(PrivateMethodInjectionSample.class, "setTestBean").invoke()).accepts(this.generationContext.getRuntimeHints());
        compile(registeredBean, (postProcessor, compiled) -> {
            PrivateMethodInjectionSample instance = new PrivateMethodInjectionSample();
            postProcessor.apply(registeredBean, instance);
            assertThat(instance).extracting("environment").isSameAs(environment);
            assertThat(getSourceFile(compiled, PrivateMethodInjectionSample.class)).contains("resolveAndInvoke(");
        });
    }

    @Test
    @CompileWithForkedClassLoader
    void contributeWhenPackagePrivateMethodInjectionInjectsUsingConsumer() {
        Environment environment = new StandardEnvironment();
        this.beanFactory.registerSingleton("environment", environment);
        RegisteredBean registeredBean = getAndApplyContribution(PackagePrivateMethodInjectionSample.class);
        assertThat(RuntimeHintsPredicates.reflection().onMethod(PackagePrivateMethodInjectionSample.class, "setTestBean").introspect()).accepts(this.generationContext.getRuntimeHints());
        compile(registeredBean, (postProcessor, compiled) -> {
            PackagePrivateMethodInjectionSample instance = new PackagePrivateMethodInjectionSample();
            postProcessor.apply(registeredBean, instance);
            assertThat(instance.environment).isSameAs(environment);
            assertThat(getSourceFile(compiled, PackagePrivateMethodInjectionSample.class)).contains("args -> instance.setTestBean(");
        });
    }

    @Test
    @CompileWithForkedClassLoader
    void contributeWhenPackagePrivateMethodInjectionOnParentClassInjectsUsingReflection() {
        Environment environment = new StandardEnvironment();
        this.beanFactory.registerSingleton("environment", environment);
        RegisteredBean registeredBean = getAndApplyContribution(PackagePrivateMethodInjectionFromParentSample.class);
        assertThat(RuntimeHintsPredicates.reflection().onMethod(PackagePrivateMethodInjectionSample.class, "setTestBean")).accepts(this.generationContext.getRuntimeHints());
        compile(registeredBean, (postProcessor, compiled) -> {
            PackagePrivateMethodInjectionFromParentSample instance = new PackagePrivateMethodInjectionFromParentSample();
            postProcessor.apply(registeredBean, instance);
            assertThat(instance.environment).isSameAs(environment);
            assertThat(getSourceFile(compiled, PackagePrivateMethodInjectionFromParentSample.class)).contains("resolveAndInvoke(");
        });
    }

    @Test
    void contributeWhenMethodInjectionHasMatchingPropertyValue() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(InjectionBean.class);
        beanDefinition.getPropertyValues().addPropertyValue("counter", 42);
        this.beanFactory.registerBeanDefinition("test", beanDefinition);
        BeanRegistrationAotContribution contribution = this.beanPostProcessor.processAheadOfTime(RegisteredBean.of(this.beanFactory, "test"));
        assertThat(contribution).isNull();
    }

    private RegisteredBean getAndApplyContribution(Class<?> beanClass) {
        RegisteredBean registeredBean = registerBean(beanClass);
        BeanRegistrationAotContribution contribution = this.beanPostProcessor.processAheadOfTime(registeredBean);
        assertThat(contribution).isNotNull();
        contribution.applyTo(this.generationContext, this.beanRegistrationCode);
        return registeredBean;
    }

    private RegisteredBean registerBean(Class<?> beanClass) {
        String beanName = "testBean";
        this.beanFactory.registerBeanDefinition(beanName, new RootBeanDefinition(beanClass));
        return RegisteredBean.of(this.beanFactory, beanName);
    }

    private static SourceFile getSourceFile(Compiled compiled, Class<?> sample) {
        return compiled.getSourceFileFromPackage(sample.getPackageName());
    }

    @SuppressWarnings("unchecked")
    private void compile(RegisteredBean registeredBean, BiConsumer<BiFunction<RegisteredBean, Object, Object>, Compiled> result) {
        Class<?> target = registeredBean.getBeanClass();
        MethodReference methodReference = this.beanRegistrationCode.getInstancePostProcessors().get(0);
        this.beanRegistrationCode.getTypeBuilder().set(type -> {
            CodeBlock methodInvocation = methodReference.toInvokeCodeBlock(ArgumentCodeGenerator.of(RegisteredBean.class, "registeredBean").and(target, "instance"), this.beanRegistrationCode.getClassName());
            type.addModifiers(Modifier.PUBLIC);
            type.addSuperinterface(ParameterizedTypeName.get(BiFunction.class, RegisteredBean.class, target, target));
            type.addMethod(MethodSpec.methodBuilder("apply").addModifiers(Modifier.PUBLIC).addParameter(RegisteredBean.class, "registeredBean").addParameter(target, "instance").returns(target).addStatement("return $L", methodInvocation).build());
        });
        this.generationContext.writeGeneratedContent();
        TestCompiler.forSystem().with(this.generationContext).compile(compiled -> result.accept(compiled.getInstance(BiFunction.class), compiled));
    }

    static class InjectionBean {

        @SuppressWarnings("unused")
        private Integer counter;

        @Autowired
        public void setCounter(Integer counter) {
            this.counter = counter;
        }
    }
}
