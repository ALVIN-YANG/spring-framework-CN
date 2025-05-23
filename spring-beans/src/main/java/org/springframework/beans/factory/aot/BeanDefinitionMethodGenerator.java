// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可；
* 除非遵守许可协议，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.springframework.aot.generate.GeneratedClass;
import org.springframework.aot.generate.GeneratedMethod;
import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodParameter;
import org.springframework.javapoet.ClassName;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 生成一个返回要注册的 {@link BeanDefinition} 的方法。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @since 6.0
 * @see BeanDefinitionMethodGeneratorFactory
 */
class BeanDefinitionMethodGenerator {

    private final BeanDefinitionMethodGeneratorFactory methodGeneratorFactory;

    private final RegisteredBean registeredBean;

    private final Executable constructorOrFactoryMethod;

    @Nullable
    private final String currentPropertyName;

    private final List<BeanRegistrationAotContribution> aotContributions;

    /**
     * 创建一个新的 {@link BeanDefinitionMethodGenerator} 实例。
     * @param methodGeneratorFactory 方法生成器工厂
     * @param registeredBean 已注册的bean
     * @param currentPropertyName 当前属性名
     * @param aotContributions AOT（提前优化）贡献
     * @throws IllegalArgumentException 如果bean定义定义了一个实例提供者，因为代码生成不支持这种情况
     */
    BeanDefinitionMethodGenerator(BeanDefinitionMethodGeneratorFactory methodGeneratorFactory, RegisteredBean registeredBean, @Nullable String currentPropertyName, List<BeanRegistrationAotContribution> aotContributions) {
        RootBeanDefinition mbd = registeredBean.getMergedBeanDefinition();
        if (mbd.getInstanceSupplier() != null && aotContributions.isEmpty()) {
            throw new IllegalArgumentException("Code generation is not supported for bean definitions declaring an instance supplier callback : " + mbd);
        }
        this.methodGeneratorFactory = methodGeneratorFactory;
        this.registeredBean = registeredBean;
        this.constructorOrFactoryMethod = registeredBean.resolveConstructorOrFactoryMethod();
        this.currentPropertyName = currentPropertyName;
        this.aotContributions = aotContributions;
    }

    /**
     * 生成一个返回要注册的 {@link BeanDefinition} 的方法。
     * @param generationContext 生成上下文
     * @param beanRegistrationsCode Bean注册代码
     * @return 对生成的方法的引用。
     */
    MethodReference generateBeanDefinitionMethod(GenerationContext generationContext, BeanRegistrationsCode beanRegistrationsCode) {
        registerRuntimeHintsIfNecessary(generationContext.getRuntimeHints());
        BeanRegistrationCodeFragments codeFragments = getCodeFragments(generationContext, beanRegistrationsCode);
        ClassName target = codeFragments.getTarget(this.registeredBean, this.constructorOrFactoryMethod);
        if (isWritablePackageName(target)) {
            GeneratedClass generatedClass = lookupGeneratedClass(generationContext, target);
            GeneratedMethods generatedMethods = generatedClass.getMethods().withPrefix(getName());
            GeneratedMethod generatedMethod = generateBeanDefinitionMethod(generationContext, generatedClass.getName(), generatedMethods, codeFragments, Modifier.PUBLIC);
            return generatedMethod.toMethodReference();
        }
        GeneratedMethods generatedMethods = beanRegistrationsCode.getMethods().withPrefix(getName());
        GeneratedMethod generatedMethod = generateBeanDefinitionMethod(generationContext, beanRegistrationsCode.getClassName(), generatedMethods, codeFragments, Modifier.PRIVATE);
        return generatedMethod.toMethodReference();
    }

    /**
     * 指定 {@link ClassName} 是否属于可写包。
     * @param target 要检查的目标
     * @return 如果该包中允许生成代码，则返回 {@code true}
     */
    private boolean isWritablePackageName(ClassName target) {
        String packageName = target.packageName();
        return (!packageName.startsWith("java.") && !packageName.startsWith("javax."));
    }

    /**
     * 返回用于指定 {@code target} 的 {@link GeneratedClass}。
     * <p>如果目标类是一个内部类，则在原始结构中创建相应的内部类。
     * @param generationContext 要使用的生成上下文
     * @param target 为bean定义所选的目标类名
     * @return 要使用的生成类
     */
    private static GeneratedClass lookupGeneratedClass(GenerationContext generationContext, ClassName target) {
        ClassName topLevelClassName = target.topLevelClassName();
        GeneratedClass generatedClass = generationContext.getGeneratedClasses().getOrAddForFeatureComponent("BeanDefinitions", topLevelClassName, type -> {
            type.addJavadoc("Bean definitions for {@link $T}.", topLevelClassName);
            type.addModifiers(Modifier.PUBLIC);
        });
        List<String> names = target.simpleNames();
        if (names.size() == 1) {
            return generatedClass;
        }
        List<String> namesToProcess = names.subList(1, names.size());
        ClassName currentTargetClassName = topLevelClassName;
        GeneratedClass tmp = generatedClass;
        for (String nameToProcess : namesToProcess) {
            currentTargetClassName = currentTargetClassName.nestedClass(nameToProcess);
            tmp = createInnerClass(tmp, nameToProcess, currentTargetClassName);
        }
        return tmp;
    }

    private static GeneratedClass createInnerClass(GeneratedClass generatedClass, String name, ClassName target) {
        return generatedClass.getOrAdd(name, type -> {
            type.addJavadoc("Bean definitions for {@link $T}.", target);
            type.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        });
    }

    private BeanRegistrationCodeFragments getCodeFragments(GenerationContext generationContext, BeanRegistrationsCode beanRegistrationsCode) {
        BeanRegistrationCodeFragments codeFragments = new DefaultBeanRegistrationCodeFragments(beanRegistrationsCode, this.registeredBean, this.methodGeneratorFactory);
        for (BeanRegistrationAotContribution aotContribution : this.aotContributions) {
            codeFragments = aotContribution.customizeBeanRegistrationCodeFragments(generationContext, codeFragments);
        }
        return codeFragments;
    }

    private GeneratedMethod generateBeanDefinitionMethod(GenerationContext generationContext, ClassName className, GeneratedMethods generatedMethods, BeanRegistrationCodeFragments codeFragments, Modifier modifier) {
        BeanRegistrationCodeGenerator codeGenerator = new BeanRegistrationCodeGenerator(className, generatedMethods, this.registeredBean, this.constructorOrFactoryMethod, codeFragments);
        this.aotContributions.forEach(aotContribution -> aotContribution.applyTo(generationContext, codeGenerator));
        return generatedMethods.add("getBeanDefinition", method -> {
            method.addJavadoc("Get the $L definition for '$L'.", (this.registeredBean.isInnerBean() ? "inner-bean" : "bean"), getName());
            method.addModifiers(modifier, Modifier.STATIC);
            method.returns(BeanDefinition.class);
            method.addCode(codeGenerator.generateCode(generationContext));
        });
    }

    private String getName() {
        if (this.currentPropertyName != null) {
            return this.currentPropertyName;
        }
        if (!this.registeredBean.isGeneratedBeanName()) {
            return getSimpleBeanName(this.registeredBean.getBeanName());
        }
        RegisteredBean nonGeneratedParent = this.registeredBean;
        while (nonGeneratedParent != null && nonGeneratedParent.isGeneratedBeanName()) {
            nonGeneratedParent = nonGeneratedParent.getParent();
        }
        if (nonGeneratedParent != null) {
            return getSimpleBeanName(nonGeneratedParent.getBeanName()) + "InnerBean";
        }
        return "innerBean";
    }

    private String getSimpleBeanName(String beanName) {
        int lastDot = beanName.lastIndexOf('.');
        beanName = (lastDot != -1 ? beanName.substring(lastDot + 1) : beanName);
        int lastDollar = beanName.lastIndexOf('$');
        beanName = (lastDollar != -1 ? beanName.substring(lastDollar + 1) : beanName);
        return StringUtils.uncapitalize(beanName);
    }

    private void registerRuntimeHintsIfNecessary(RuntimeHints runtimeHints) {
        if (this.registeredBean.getBeanFactory() instanceof DefaultListableBeanFactory dlbf) {
            ProxyRuntimeHintsRegistrar registrar = new ProxyRuntimeHintsRegistrar(dlbf.getAutowireCandidateResolver());
            if (this.constructorOrFactoryMethod instanceof Method method) {
                registrar.registerRuntimeHints(runtimeHints, method);
            } else if (this.constructorOrFactoryMethod instanceof Constructor<?> constructor) {
                registrar.registerRuntimeHints(runtimeHints, constructor);
            }
        }
    }

    private static class ProxyRuntimeHintsRegistrar {

        private final AutowireCandidateResolver candidateResolver;

        public ProxyRuntimeHintsRegistrar(AutowireCandidateResolver candidateResolver) {
            this.candidateResolver = candidateResolver;
        }

        public void registerRuntimeHints(RuntimeHints runtimeHints, Method method) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                MethodParameter methodParam = new MethodParameter(method, i);
                DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(methodParam, true);
                registerProxyIfNecessary(runtimeHints, dependencyDescriptor);
            }
        }

        public void registerRuntimeHints(RuntimeHints runtimeHints, Constructor<?> constructor) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                MethodParameter methodParam = new MethodParameter(constructor, i);
                DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(methodParam, true);
                registerProxyIfNecessary(runtimeHints, dependencyDescriptor);
            }
        }

        private void registerProxyIfNecessary(RuntimeHints runtimeHints, DependencyDescriptor dependencyDescriptor) {
            Class<?> proxyType = this.candidateResolver.getLazyResolutionProxyClass(dependencyDescriptor, null);
            if (proxyType != null && Proxy.isProxyClass(proxyType)) {
                runtimeHints.proxies().registerJdkProxy(proxyType.getInterfaces());
            }
        }
    }
}
