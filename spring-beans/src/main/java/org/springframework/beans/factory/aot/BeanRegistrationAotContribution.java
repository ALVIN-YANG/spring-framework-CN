// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性或非侵权性。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.aot;

import java.util.function.UnaryOperator;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.util.Assert;

/**
 * 来自于一个用于注册单个bean定义的{@link BeanRegistrationAotProcessor}的AOT（Ahead-of-Time）贡献。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanRegistrationAotProcessor
 */
@FunctionalInterface
public interface BeanRegistrationAotContribution {

    /**
     * 自定义将用于生成bean注册代码的{@link BeanRegistrationCodeFragments}。如果默认代码生成不合适，可以使用自定义代码片段。
     * @param generationContext 生成上下文
     * @param codeFragments 现有的代码片段
     * @return 要使用的代码片段，可能是原始实例或包装器
     */
    default BeanRegistrationCodeFragments customizeBeanRegistrationCodeFragments(GenerationContext generationContext, BeanRegistrationCodeFragments codeFragments) {
        return codeFragments;
    }

    /**
     * 将此贡献应用于给定的 {@link BeanRegistrationCode}。
     * @param generationContext 生成上下文
     * @param beanRegistrationCode 生成的注册信息
     */
    void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode);

    /**
     * 创建一个自定义 {@link BeanRegistrationAotContribution}，用于定制
     * {@link BeanRegistrationCodeFragments}。通常与扩展自
     * {@link BeanRegistrationCodeFragmentsDecorator} 的装饰器一起使用，该装饰器覆盖了特定的回调。
     * @param defaultCodeFragments 默认代码片段
     * @return 一个新的 {@link BeanRegistrationAotContribution} 实例
     * @see BeanRegistrationCodeFragmentsDecorator
     */
    static BeanRegistrationAotContribution withCustomCodeFragments(UnaryOperator<BeanRegistrationCodeFragments> defaultCodeFragments) {
        Assert.notNull(defaultCodeFragments, "'defaultCodeFragments' must not be null");
        return new BeanRegistrationAotContribution() {

            @Override
            public BeanRegistrationCodeFragments customizeBeanRegistrationCodeFragments(GenerationContext generationContext, BeanRegistrationCodeFragments codeFragments) {
                return defaultCodeFragments.apply(codeFragments);
            }

            @Override
            public void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {
            }
        };
    }
}
