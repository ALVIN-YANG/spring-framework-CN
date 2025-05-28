// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“许可协议”）许可；
* 您只能在遵守许可协议的情况下使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可协议分发的软件
* 是按“原样”分发的，不提供任何形式的质量保证或条件，无论是明示的还是暗示的。
* 请参阅许可协议了解具体规定许可权限和限制的内容。*/
package org.springframework.beans.factory.aot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.Test;
import org.springframework.aot.generate.ClassNameGenerator;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.aot.generate.MethodReference.ArgumentCodeGenerator;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.test.generate.TestGenerationContext;
import org.springframework.beans.factory.aot.BeanRegistrationsAotContribution.Registration;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.testfixture.beans.GenericBeanWithBounds;
import org.springframework.beans.testfixture.beans.Person;
import org.springframework.beans.testfixture.beans.RecordBean;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.beans.testfixture.beans.factory.aot.MockBeanFactoryInitializationCode;
import org.springframework.core.test.io.support.MockSpringFactoriesLoader;
import org.springframework.core.test.tools.Compiled;
import org.springframework.core.test.tools.SourceFile;
import org.springframework.core.test.tools.TestCompiler;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

/**
 * 测试 {@link BeanRegistrationsAotContribution}。
 *
 * @author Phillip Webb
 * @author Sebastien Deleuze
 * @author Stephane Nicoll
 * @author Brian Clozel
 */
class BeanRegistrationsAotContributionTests {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    private final BeanDefinitionMethodGeneratorFactory methodGeneratorFactory = new BeanDefinitionMethodGeneratorFactory(AotServices.factoriesAndBeans(new MockSpringFactoriesLoader(), this.beanFactory));

    private TestGenerationContext generationContext = new TestGenerationContext();

    private MockBeanFactoryInitializationCode beanFactoryInitializationCode = new MockBeanFactoryInitializationCode(this.generationContext);

    @Test
    void applyToAppliesContribution() {
        RegisteredBean registeredBean = registerBean(new RootBeanDefinition(TestBean.class));
        BeanDefinitionMethodGenerator generator = new BeanDefinitionMethodGenerator(this.methodGeneratorFactory, registeredBean, null, List.of());
        BeanRegistrationsAotContribution contribution = createContribution(TestBean.class, generator);
        contribution.applyTo(this.generationContext, this.beanFactoryInitializationCode);
        compile((consumer, compiled) -> {
            DefaultListableBeanFactory freshBeanFactory = new DefaultListableBeanFactory();
            consumer.accept(freshBeanFactory);
            assertThat(freshBeanFactory.getBean(TestBean.class)).isNotNull();
        });
    }

    @Test
    void applyToAppliesContributionWithAliases() {
        RegisteredBean registeredBean = registerBean(new RootBeanDefinition(TestBean.class));
        BeanDefinitionMethodGenerator generator = new BeanDefinitionMethodGenerator(this.methodGeneratorFactory, registeredBean, null, List.of());
        BeanRegistrationsAotContribution contribution = createContribution(TestBean.class, generator, "testAlias");
        contribution.applyTo(this.generationContext, this.beanFactoryInitializationCode);
        compile((consumer, compiled) -> {
            DefaultListableBeanFactory freshBeanFactory = new DefaultListableBeanFactory();
            consumer.accept(freshBeanFactory);
            assertThat(freshBeanFactory.getAliases("testBean")).containsExactly("testAlias");
        });
    }

    @Test
    void applyToWhenHasNameGeneratesPrefixedFeatureName() {
        this.generationContext = new TestGenerationContext(new ClassNameGenerator(TestGenerationContext.TEST_TARGET, "Management"));
        this.beanFactoryInitializationCode = new MockBeanFactoryInitializationCode(this.generationContext);
        RegisteredBean registeredBean = registerBean(new RootBeanDefinition(TestBean.class));
        BeanDefinitionMethodGenerator generator = new BeanDefinitionMethodGenerator(this.methodGeneratorFactory, registeredBean, null, List.of());
        BeanRegistrationsAotContribution contribution = createContribution(TestBean.class, generator);
        contribution.applyTo(this.generationContext, this.beanFactoryInitializationCode);
        compile((consumer, compiled) -> {
            SourceFile sourceFile = compiled.getSourceFile(".*BeanDefinitions");
            assertThat(sourceFile.getClassName()).endsWith("__ManagementBeanDefinitions");
        });
    }

    @Test
    void applyToCallsRegistrationsWithBeanRegistrationsCode() {
        List<BeanRegistrationsCode> beanRegistrationsCodes = new ArrayList<>();
        RegisteredBean registeredBean = registerBean(new RootBeanDefinition(TestBean.class));
        BeanDefinitionMethodGenerator generator = new BeanDefinitionMethodGenerator(this.methodGeneratorFactory, registeredBean, null, List.of()) {

            @Override
            MethodReference generateBeanDefinitionMethod(GenerationContext generationContext, BeanRegistrationsCode beanRegistrationsCode) {
                beanRegistrationsCodes.add(beanRegistrationsCode);
                return super.generateBeanDefinitionMethod(generationContext, beanRegistrationsCode);
            }
        };
        BeanRegistrationsAotContribution contribution = createContribution(TestBean.class, generator);
        contribution.applyTo(this.generationContext, this.beanFactoryInitializationCode);
        assertThat(beanRegistrationsCodes).hasSize(1);
        BeanRegistrationsCode actual = beanRegistrationsCodes.get(0);
        assertThat(actual.getMethods()).isNotNull();
    }

    @Test
    void applyToRegisterReflectionHints() {
        RegisteredBean registeredBean = registerBean(new RootBeanDefinition(TestBean.class));
        BeanDefinitionMethodGenerator generator = new BeanDefinitionMethodGenerator(this.methodGeneratorFactory, registeredBean, null, List.of());
        BeanRegistrationsAotContribution contribution = createContribution(TestBean.class, generator);
        contribution.applyTo(this.generationContext, this.beanFactoryInitializationCode);
        assertThat(reflection().onType(TestBean.class).withMemberCategory(MemberCategory.INTROSPECT_DECLARED_METHODS)).accepts(this.generationContext.getRuntimeHints());
    }

    @Test
    void applyToRegisterReflectionHintsOnRecordBean() {
        RegisteredBean registeredBean = registerBean(new RootBeanDefinition(RecordBean.class));
        BeanDefinitionMethodGenerator generator = new BeanDefinitionMethodGenerator(this.methodGeneratorFactory, registeredBean, null, List.of());
        BeanRegistrationsAotContribution contribution = createContribution(RecordBean.class, generator);
        contribution.applyTo(this.generationContext, this.beanFactoryInitializationCode);
        assertThat(reflection().onType(RecordBean.class).withMemberCategories(MemberCategory.INTROSPECT_DECLARED_METHODS, MemberCategory.INVOKE_DECLARED_METHODS)).accepts(this.generationContext.getRuntimeHints());
    }

    @Test
    void applyToRegisterReflectionHintsOnGenericBeanWithBounds() {
        RegisteredBean registeredBean = registerBean(new RootBeanDefinition(GenericBeanWithBounds.class));
        BeanDefinitionMethodGenerator generator = new BeanDefinitionMethodGenerator(this.methodGeneratorFactory, registeredBean, null, List.of());
        BeanRegistrationsAotContribution contribution = createContribution(GenericBeanWithBounds.class, generator);
        contribution.applyTo(this.generationContext, this.beanFactoryInitializationCode);
        assertThat(reflection().onType(Person[].class)).accepts(this.generationContext.getRuntimeHints());
    }

    private RegisteredBean registerBean(RootBeanDefinition rootBeanDefinition) {
        String beanName = "testBean";
        this.beanFactory.registerBeanDefinition(beanName, rootBeanDefinition);
        return RegisteredBean.of(this.beanFactory, beanName);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    private void compile(BiConsumer<Consumer<DefaultListableBeanFactory>, Compiled> result) {
        MethodReference beanRegistrationsMethodReference = this.beanFactoryInitializationCode.getInitializers().get(0);
        MethodReference aliasesMethodReference = this.beanFactoryInitializationCode.getInitializers().get(1);
        this.beanFactoryInitializationCode.getTypeBuilder().set(type -> {
            ArgumentCodeGenerator beanFactory = ArgumentCodeGenerator.of(DefaultListableBeanFactory.class, "beanFactory");
            ClassName className = this.beanFactoryInitializationCode.getClassName();
            CodeBlock beanRegistrationsMethodInvocation = beanRegistrationsMethodReference.toInvokeCodeBlock(beanFactory, className);
            CodeBlock aliasesMethodInvocation = aliasesMethodReference.toInvokeCodeBlock(beanFactory, className);
            type.addModifiers(Modifier.PUBLIC);
            type.addSuperinterface(ParameterizedTypeName.get(Consumer.class, DefaultListableBeanFactory.class));
            type.addMethod(MethodSpec.methodBuilder("accept").addModifiers(Modifier.PUBLIC).addParameter(DefaultListableBeanFactory.class, "beanFactory").addStatement(beanRegistrationsMethodInvocation).addStatement(aliasesMethodInvocation).build());
        });
        this.generationContext.writeGeneratedContent();
        TestCompiler.forSystem().with(this.generationContext).compile(compiled -> result.accept(compiled.getInstance(Consumer.class), compiled));
    }

    private BeanRegistrationsAotContribution createContribution(Class<?> beanClass, BeanDefinitionMethodGenerator methodGenerator, String... aliases) {
        return new BeanRegistrationsAotContribution(Map.of(new BeanRegistrationKey("testBean", beanClass), new Registration(methodGenerator, aliases)));
    }
}
