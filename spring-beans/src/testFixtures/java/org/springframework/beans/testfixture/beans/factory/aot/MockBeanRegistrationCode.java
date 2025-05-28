// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权性的保证。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.testfixture.beans.factory.aot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.aot.generate.GeneratedClass;
import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.javapoet.ClassName;

/**
 * 模拟 {@link BeanRegistrationCode} 实现。
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 */
public class MockBeanRegistrationCode implements BeanRegistrationCode {

    private final GeneratedClass generatedClass;

    private final List<MethodReference> instancePostProcessors = new ArrayList<>();

    private final DeferredTypeBuilder typeBuilder = new DeferredTypeBuilder();

    public MockBeanRegistrationCode(GenerationContext generationContext) {
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

    @Override
    public void addInstancePostProcessor(MethodReference methodReference) {
        this.instancePostProcessors.add(methodReference);
    }

    public List<MethodReference> getInstancePostProcessors() {
        return Collections.unmodifiableList(this.instancePostProcessors);
    }
}
