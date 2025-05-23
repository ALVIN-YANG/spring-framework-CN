// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.Executable;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.util.Assert;

/**
 * 一个 {@link BeanRegistrationCodeFragments} 装饰器实现。通常用于需要自定义默认代码片段的部分，通过扩展此类并作为
 * {@link BeanRegistrationAotContribution#withCustomCodeFragments(UnaryOperator)} 的一部分使用。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
public class BeanRegistrationCodeFragmentsDecorator implements BeanRegistrationCodeFragments {

    private final BeanRegistrationCodeFragments delegate;

    protected BeanRegistrationCodeFragmentsDecorator(BeanRegistrationCodeFragments delegate) {
        Assert.notNull(delegate, "Delegate must not be null");
        this.delegate = delegate;
    }

    @Override
    public ClassName getTarget(RegisteredBean registeredBean, Executable constructorOrFactoryMethod) {
        return this.delegate.getTarget(registeredBean, constructorOrFactoryMethod);
    }

    @Override
    public CodeBlock generateNewBeanDefinitionCode(GenerationContext generationContext, ResolvableType beanType, BeanRegistrationCode beanRegistrationCode) {
        return this.delegate.generateNewBeanDefinitionCode(generationContext, beanType, beanRegistrationCode);
    }

    @Override
    public CodeBlock generateSetBeanDefinitionPropertiesCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode, RootBeanDefinition beanDefinition, Predicate<String> attributeFilter) {
        return this.delegate.generateSetBeanDefinitionPropertiesCode(generationContext, beanRegistrationCode, beanDefinition, attributeFilter);
    }

    @Override
    public CodeBlock generateSetBeanInstanceSupplierCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode, CodeBlock instanceSupplierCode, List<MethodReference> postProcessors) {
        return this.delegate.generateSetBeanInstanceSupplierCode(generationContext, beanRegistrationCode, instanceSupplierCode, postProcessors);
    }

    @Override
    public CodeBlock generateInstanceSupplierCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode, Executable constructorOrFactoryMethod, boolean allowDirectSupplierShortcut) {
        return this.delegate.generateInstanceSupplierCode(generationContext, beanRegistrationCode, constructorOrFactoryMethod, allowDirectSupplierShortcut);
    }

    @Override
    public CodeBlock generateReturnCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {
        return this.delegate.generateReturnCode(generationContext, beanRegistrationCode);
    }
}
