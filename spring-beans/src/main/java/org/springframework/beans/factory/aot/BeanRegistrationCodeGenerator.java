// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”），除非法律要求或经书面同意，否则您不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、 merchantability 或特定用途的适用性。
* 请参阅许可证了解管理许可权和限制的具体语言。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.util.Assert;

/**
 * 带有代码生成支持的 {@link BeanRegistrationCode} 实现。
 *
 * @author Phillip Webb
 * @since 6.0
 */
class BeanRegistrationCodeGenerator implements BeanRegistrationCode {

    private static final Predicate<String> REJECT_ALL_ATTRIBUTES_FILTER = attribute -> false;

    private final ClassName className;

    private final GeneratedMethods generatedMethods;

    private final List<MethodReference> instancePostProcessors = new ArrayList<>();

    private final RegisteredBean registeredBean;

    private final Executable constructorOrFactoryMethod;

    private final BeanRegistrationCodeFragments codeFragments;

    BeanRegistrationCodeGenerator(ClassName className, GeneratedMethods generatedMethods, RegisteredBean registeredBean, Executable constructorOrFactoryMethod, BeanRegistrationCodeFragments codeFragments) {
        this.className = className;
        this.generatedMethods = generatedMethods;
        this.registeredBean = registeredBean;
        this.constructorOrFactoryMethod = constructorOrFactoryMethod;
        this.codeFragments = codeFragments;
    }

    @Override
    public ClassName getClassName() {
        return this.className;
    }

    @Override
    public GeneratedMethods getMethods() {
        return this.generatedMethods;
    }

    @Override
    public void addInstancePostProcessor(MethodReference methodReference) {
        Assert.notNull(methodReference, "'methodReference' must not be null");
        this.instancePostProcessors.add(methodReference);
    }

    CodeBlock generateCode(GenerationContext generationContext) {
        CodeBlock.Builder code = CodeBlock.builder();
        code.add(this.codeFragments.generateNewBeanDefinitionCode(generationContext, this.registeredBean.getBeanType(), this));
        code.add(this.codeFragments.generateSetBeanDefinitionPropertiesCode(generationContext, this, this.registeredBean.getMergedBeanDefinition(), REJECT_ALL_ATTRIBUTES_FILTER));
        CodeBlock instanceSupplierCode = this.codeFragments.generateInstanceSupplierCode(generationContext, this, this.constructorOrFactoryMethod, this.instancePostProcessors.isEmpty());
        code.add(this.codeFragments.generateSetBeanInstanceSupplierCode(generationContext, this, instanceSupplierCode, this.instancePostProcessors));
        code.add(this.codeFragments.generateReturnCode(generationContext, this));
        return code.build();
    }
}
