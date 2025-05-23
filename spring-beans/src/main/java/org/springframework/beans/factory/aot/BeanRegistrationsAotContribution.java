// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途的适用性或不侵犯权利。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Map;
import javax.lang.model.element.Modifier;
import org.springframework.aot.generate.GeneratedClass;
import org.springframework.aot.generate.GeneratedMethod;
import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.aot.generate.MethodReference.ArgumentCodeGenerator;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.MethodSpec;
import org.springframework.util.ReflectionUtils;

/**
 * 从一个用于注册Bean定义和别名的 {@link BeanRegistrationsAotProcessor} 中贡献的AOT（Ahead-of-Time）内容。
 *
 * @author Phillip Webb
 * @author Sebastien Deleuze
 * @author Stephane Nicoll
 * @author Brian Clozel
 * @since 6.0
 * @see BeanRegistrationsAotProcessor
 */
class BeanRegistrationsAotContribution implements BeanFactoryInitializationAotContribution {

    private static final String BEAN_FACTORY_PARAMETER_NAME = "beanFactory";

    private final Map<BeanRegistrationKey, Registration> registrations;

    BeanRegistrationsAotContribution(Map<BeanRegistrationKey, Registration> registrations) {
        this.registrations = registrations;
    }

    @Override
    public void applyTo(GenerationContext generationContext, BeanFactoryInitializationCode beanFactoryInitializationCode) {
        GeneratedClass generatedClass = generationContext.getGeneratedClasses().addForFeature("BeanFactoryRegistrations", type -> {
            type.addJavadoc("Register bean definitions for the bean factory.");
            type.addModifiers(Modifier.PUBLIC);
        });
        BeanRegistrationsCodeGenerator codeGenerator = new BeanRegistrationsCodeGenerator(generatedClass);
        GeneratedMethod generatedBeanDefinitionsMethod = codeGenerator.getMethods().add("registerBeanDefinitions", method -> generateRegisterBeanDefinitionsMethod(method, generationContext, codeGenerator));
        beanFactoryInitializationCode.addInitializer(generatedBeanDefinitionsMethod.toMethodReference());
        GeneratedMethod generatedAliasesMethod = codeGenerator.getMethods().add("registerAliases", this::generateRegisterAliasesMethod);
        beanFactoryInitializationCode.addInitializer(generatedAliasesMethod.toMethodReference());
        generateRegisterHints(generationContext.getRuntimeHints(), this.registrations);
    }

    private void generateRegisterBeanDefinitionsMethod(MethodSpec.Builder method, GenerationContext generationContext, BeanRegistrationsCode beanRegistrationsCode) {
        method.addJavadoc("Register the bean definitions.");
        method.addModifiers(Modifier.PUBLIC);
        method.addParameter(DefaultListableBeanFactory.class, BEAN_FACTORY_PARAMETER_NAME);
        CodeBlock.Builder code = CodeBlock.builder();
        this.registrations.forEach((registeredBean, registration) -> {
            MethodReference beanDefinitionMethod = registration.methodGenerator.generateBeanDefinitionMethod(generationContext, beanRegistrationsCode);
            CodeBlock methodInvocation = beanDefinitionMethod.toInvokeCodeBlock(ArgumentCodeGenerator.none(), beanRegistrationsCode.getClassName());
            code.addStatement("$L.registerBeanDefinition($S, $L)", BEAN_FACTORY_PARAMETER_NAME, registeredBean.beanName(), methodInvocation);
        });
        method.addCode(code.build());
    }

    private void generateRegisterAliasesMethod(MethodSpec.Builder method) {
        method.addJavadoc("Register the aliases.");
        method.addModifiers(Modifier.PUBLIC);
        method.addParameter(DefaultListableBeanFactory.class, BEAN_FACTORY_PARAMETER_NAME);
        CodeBlock.Builder code = CodeBlock.builder();
        this.registrations.forEach((registeredBean, registration) -> {
            for (String alias : registration.aliases) {
                code.addStatement("$L.registerAlias($S, $S)", BEAN_FACTORY_PARAMETER_NAME, registeredBean.beanName(), alias);
            }
        });
        method.addCode(code.build());
    }

    private void generateRegisterHints(RuntimeHints runtimeHints, Map<BeanRegistrationKey, Registration> registrations) {
        registrations.keySet().forEach(beanRegistrationKey -> {
            ReflectionHints hints = runtimeHints.reflection();
            Class<?> beanClass = beanRegistrationKey.beanClass();
            hints.registerType(beanClass, MemberCategory.INTROSPECT_DECLARED_METHODS);
            // 针对 https://github.com/oracle/graal/issues/6510 的解决方案
            if (beanClass.isRecord()) {
                hints.registerType(beanClass, MemberCategory.INVOKE_DECLARED_METHODS);
            }
            // 针对 https://github.com/oracle/graal/issues/6529 的解决方案
            ReflectionUtils.doWithMethods(beanClass, method -> {
                for (Type type : method.getGenericParameterTypes()) {
                    if (type instanceof GenericArrayType) {
                        Class<?> clazz = ResolvableType.forType(type).resolve();
                        if (clazz != null) {
                            hints.registerType(clazz);
                        }
                    }
                }
            });
        });
    }

    /**
     * 收集注册特定 Bean 所必需的信息。
     * @param methodGenerator 要使用的 {@link BeanDefinitionMethodGenerator}
     * @param aliases 如果有的话，Bean 的别名
     */
    record Registration(BeanDefinitionMethodGenerator methodGenerator, String[] aliases) {
    }

    /**
     * 与生成支持相关的 {@link BeanRegistrationsCode}。
     */
    static class BeanRegistrationsCodeGenerator implements BeanRegistrationsCode {

        private final GeneratedClass generatedClass;

        public BeanRegistrationsCodeGenerator(GeneratedClass generatedClass) {
            this.generatedClass = generatedClass;
        }

        @Override
        public ClassName getClassName() {
            return this.generatedClass.getName();
        }

        @Override
        public GeneratedMethods getMethods() {
            return this.generatedClass.getMethods();
        }
    }
}
