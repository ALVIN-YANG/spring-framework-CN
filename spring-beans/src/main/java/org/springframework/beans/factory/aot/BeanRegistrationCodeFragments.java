// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.Executable;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;

/**
 * 生成注册 Bean 所需的各种代码片段。
 *
 * @author Phillip Webb
 * @since 6.0
 */
public interface BeanRegistrationCodeFragments {

    /**
     * 当创建 Bean 定义时使用的变量名。
     */
    String BEAN_DEFINITION_VARIABLE = "beanDefinition";

    /**
     * 创建Bean定义时使用的变量名称。
     */
    String INSTANCE_SUPPLIER_VARIABLE = "instanceSupplier";

    /**
     * 返回注册的目标。用于确定代码写入的位置。
     * @param registeredBean 已注册的bean
     * @param constructorOrFactoryMethod 构造函数或工厂方法
     * @return 目标 {@link ClassName}
     */
    ClassName getTarget(RegisteredBean registeredBean, Executable constructorOrFactoryMethod);

    /**
     * 生成定义新Bean定义实例的代码。
     * @param generationContext 生成上下文
     * @param beanType Bean类型
     * @param beanRegistrationCode Bean注册代码
     * @return 生成的代码
     */
    CodeBlock generateNewBeanDefinitionCode(GenerationContext generationContext, ResolvableType beanType, BeanRegistrationCode beanRegistrationCode);

    /**
     * 生成设置 Bean 定义属性的代码。
     * @param generationContext 生成上下文
     * @param beanRegistrationCode Bean 注册代码
     * @param attributeFilter 应应用的任何属性过滤
     * @return 生成的代码
     */
    CodeBlock generateSetBeanDefinitionPropertiesCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode, RootBeanDefinition beanDefinition, Predicate<String> attributeFilter);

    /**
     * 生成设置实例供应商的代码到bean定义中。
     * @param generationContext 生成上下文
     * @param beanRegistrationCode bean注册代码
     * @param instanceSupplierCode 实例供应商代码供应商代码
     * @param postProcessors 应应用的任何实例后处理器
     * @return 生成的代码
     * @see #generateInstanceSupplierCode
     */
    CodeBlock generateSetBeanInstanceSupplierCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode, CodeBlock instanceSupplierCode, List<MethodReference> postProcessors);

    /**
     * 生成实例供应商代码。
     * @param generationContext 生成上下文
     * @param beanRegistrationCode Bean注册代码
     * @param constructorOrFactoryMethod Bean的构造函数或工厂方法
     * @param allowDirectSupplierShortcut 是否可以使用直接供应商而不是始终需要使用 {@link InstanceSupplier}
     * @return 生成的代码
     */
    CodeBlock generateInstanceSupplierCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode, Executable constructorOrFactoryMethod, boolean allowDirectSupplierShortcut);

    /**
     * 生成返回语句。
     * @param generationContext 生成上下文
     * @param beanRegistrationCode Bean注册代码
     * @return 生成的代码
     */
    CodeBlock generateReturnCode(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode);
}
