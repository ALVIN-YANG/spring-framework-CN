// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按"原样"提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定权限和限制。*/
package org.springframework.beans.testfixture.beans.factory.aot;

import org.springframework.aot.generate.GeneratedClass;
import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.beans.factory.aot.BeanRegistrationsCode;
import org.springframework.javapoet.ClassName;

/**
 * 模拟 {@link BeanRegistrationsCode} 实现。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
public class MockBeanRegistrationsCode implements BeanRegistrationsCode {

    private final GeneratedClass generatedClass;

    private final DeferredTypeBuilder typeBuilder = new DeferredTypeBuilder();

    public MockBeanRegistrationsCode(GenerationContext generationContext) {
        this.generatedClass = generationContext.getGeneratedClasses().addForFeature("TestCode", this.typeBuilder);
    }

    public DeferredTypeBuilder getTypeBuilder() {
        return this.typeBuilder;
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
