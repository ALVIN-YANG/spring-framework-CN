// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.aot.generate.AccessControl;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.aot.generate.MethodReference.ArgumentCodeGenerator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 默认使用的内部 {@link BeanRegistrationCodeFragments} 实现。
 *
 * @author Phillip Webb
 */
class DefaultBeanRegistrationCodeFragments implements BeanRegistrationCodeFragments {

    private final BeanRegistrationsCode beanRegistrationsCode;

    private final RegisteredBean registeredBean;

    private final BeanDefinitionMethodGeneratorFactory beanDefinitionMethodGeneratorFactory;

    DefaultBeanRegistrationCodeFragments(BeanRegistrationsCode beanRegistrationsCode, RegisteredBean registeredBean, BeanDefinitionMethodGeneratorFactory beanDefinitionMethodGeneratorFactory) {
        this.beanRegistrationsCode = beanRegistrationsCode;
        this.registeredBean = registeredBean;
        this.beanDefinitionMethodGeneratorFactory = beanDefinitionMethodGeneratorFactory;
    }

    @Override
    public ClassName getTarget(RegisteredBean registeredBean, Executable constructorOrFactoryMethod) {
        Class<?> target = extractDeclaringClass(registeredBean.getBeanType(), constructorOrFactoryMethod);
        while (target.getName().startsWith("java.") && registeredBean.isInnerBean()) {
            RegisteredBean parent = registeredBean.getParent();
            Assert.state(parent != null, "No parent available for inner bean");
            target = parent.getBeanClass();
        }
        return ClassName.get(target);
    }

    private Class<?> extractDeclaringClass(ResolvableType beanType, Executable executable) {
        Class<?> declaringClass = ClassUtils.getUserClass(executable.getDeclaringClass());
        if (executable instanceof Constructor<?> && AccessControl.forMember(executable).isPublic() && FactoryBean.class.isAssignableFrom(declaringClass)) {
            return extractTargetClassFromFactoryBean(declaringClass, beanType);
        }
        return executable.getDeclaringClass();
    }

    /**
     * 根据其构造函数提取一个公开的 {@link FactoryBean} 的目标类。如果实现无法解析目标类因为其自身使用泛型，则尝试从bean类型中提取它。
     * @param factoryBeanType 工厂bean类型
     * @param beanType bean类型
     * @return 要使用的目标类
     */
    private Class<?> extractTargetClassFromFactoryBean(Class<?> factoryBeanType, ResolvableType beanType) {
        ResolvableType target = ResolvableType.forType(factoryBeanType).as(FactoryBean.class).getGeneric(0);
        if (target.getType().equals(Class.class)) {
            return target.toClass();
        } else if (factoryBeanType.isAssignableFrom(beanType.toClass())) {
            return beanType.as(FactoryBean.class).getGeneric(0).toClass();
        }
        return beanType.toClass();
    }

    @Override
    public CodeBlock generateNewBeanDefinitionCode(GenerationContext generationContext, ResolvableType beanType, BeanRegistrationCode beanRegistrationCode) {
        CodeBlock.Builder code = CodeBlock.builder();
        RootBeanDefinition mbd = this.registeredBean.getMergedBeanDefinition();
        Class<?> beanClass = (mbd.hasBeanClass() ? ClassUtils.getUserClass(mbd.getBeanClass()) : null);
        CodeBlock beanClassCode = generateBeanClassCode(beanRegistrationCode.getClassName().packageName(), (beanClass != null ? beanClass : beanType.toClass()));
        code.addStatement("$T $L = new $T($L)", RootBeanDefinition.class, BEAN_DEFINITION_VARIABLE, RootBeanDefinition.class, beanClassCode);
        if (targetTypeNecessary(beanType, beanClass)) {
            code.addStatement("$L.setTargetType($L)", BEAN_DEFINITION_VARIABLE, generateBeanTypeCode(beanType));
        }
        return code.build();
    }

    private CodeBlock generateBeanClassCode(String targetPackage, Class<?> beanClass) {
        if (Modifier.isPublic(beanClass.getModifiers()) || targetPackage.equals(beanClass.getPackageName())) {
            return CodeBlock.of("$T.class", beanClass);
        } else {
            return CodeBlock.of("$S", beanClass.getName());
        }
    }

    private CodeBlock generateBeanTypeCode(ResolvableType beanType) {
        if (!beanType.hasGenerics()) {
            return CodeBlock.of("$T.class", ClassUtils.getUserClass(beanType.toClass()));
        }
        return ResolvableTypeCodeGenerator.generateCode(beanType);
    }

    private boolean targetTypeNecessary(ResolvableType beanType, @Nullable Class<?> beanClass) {
        if (beanType.hasGenerics()) {
            return true;
        }
        if (beanClass != null && this.registeredBean.getMergedBeanDefinition().getFactoryMethodName() != null) {
            return true;
        }
        return (beanClass != null && !beanType.toClass().equals(beanClass));
    }

    @Override
    public CodeBlock generateSetBeanDefinitionPropertiesCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode, RootBeanDefinition beanDefinition, Predicate<String> attributeFilter) {
        return new BeanDefinitionPropertiesCodeGenerator(generationContext.getRuntimeHints(), attributeFilter, beanRegistrationCode.getMethods(), (name, value) -> generateValueCode(generationContext, name, value)).generateCode(beanDefinition);
    }

    @Nullable
    protected CodeBlock generateValueCode(GenerationContext generationContext, String name, Object value) {
        RegisteredBean innerRegisteredBean = getInnerRegisteredBean(value);
        if (innerRegisteredBean != null) {
            BeanDefinitionMethodGenerator methodGenerator = this.beanDefinitionMethodGeneratorFactory.getBeanDefinitionMethodGenerator(innerRegisteredBean, name);
            Assert.state(methodGenerator != null, "Unexpected filtering of inner-bean");
            MethodReference generatedMethod = methodGenerator.generateBeanDefinitionMethod(generationContext, this.beanRegistrationsCode);
            return generatedMethod.toInvokeCodeBlock(ArgumentCodeGenerator.none());
        }
        return null;
    }

    @Nullable
    private RegisteredBean getInnerRegisteredBean(Object value) {
        if (value instanceof BeanDefinitionHolder beanDefinitionHolder) {
            return RegisteredBean.ofInnerBean(this.registeredBean, beanDefinitionHolder);
        }
        if (value instanceof BeanDefinition beanDefinition) {
            return RegisteredBean.ofInnerBean(this.registeredBean, beanDefinition);
        }
        return null;
    }

    @Override
    public CodeBlock generateSetBeanInstanceSupplierCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode, CodeBlock instanceSupplierCode, List<MethodReference> postProcessors) {
        CodeBlock.Builder code = CodeBlock.builder();
        if (postProcessors.isEmpty()) {
            code.addStatement("$L.setInstanceSupplier($L)", BEAN_DEFINITION_VARIABLE, instanceSupplierCode);
            return code.build();
        }
        code.addStatement("$T $L = $L", ParameterizedTypeName.get(InstanceSupplier.class, this.registeredBean.getBeanClass()), INSTANCE_SUPPLIER_VARIABLE, instanceSupplierCode);
        for (MethodReference postProcessor : postProcessors) {
            code.addStatement("$L = $L.andThen($L)", INSTANCE_SUPPLIER_VARIABLE, INSTANCE_SUPPLIER_VARIABLE, postProcessor.toCodeBlock());
        }
        code.addStatement("$L.setInstanceSupplier($L)", BEAN_DEFINITION_VARIABLE, INSTANCE_SUPPLIER_VARIABLE);
        return code.build();
    }

    @Override
    public CodeBlock generateInstanceSupplierCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode, Executable constructorOrFactoryMethod, boolean allowDirectSupplierShortcut) {
        return new InstanceSupplierCodeGenerator(generationContext, beanRegistrationCode.getClassName(), beanRegistrationCode.getMethods(), allowDirectSupplierShortcut).generateCode(this.registeredBean, constructorOrFactoryMethod);
    }

    @Override
    public CodeBlock generateReturnCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("return $L", BEAN_DEFINITION_VARIABLE);
        return code.build();
    }
}
