// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.testfixture.beans.factory.aot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.aot.generate.GeneratedClass;
import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.javapoet.ClassName;

/**
 * 模拟实现 {@link BeanFactoryInitializationCode}。
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 */
public class MockBeanFactoryInitializationCode implements BeanFactoryInitializationCode {

    private final GeneratedClass generatedClass;

    private final List<MethodReference> initializers = new ArrayList<>();

    private final DeferredTypeBuilder typeBuilder = new DeferredTypeBuilder();

    public MockBeanFactoryInitializationCode(GenerationContext generationContext) {
        this.generatedClass = generationContext.getGeneratedClasses().addForFeature("TestCode", this.typeBuilder);
    }

    public ClassName getClassName() {
        return this.generatedClass.getName();
    }

    public DeferredTypeBuilder getTypeBuilder() {
        return this.typeBuilder;
    }

    @Override
    public GeneratedMethods getMethods() {
        return this.generatedClass.getMethods();
    }

    @Override
    public void addInitializer(MethodReference methodReference) {
        this.initializers.add(methodReference);
    }

    public List<MethodReference> getInitializers() {
        return Collections.unmodifiableList(this.initializers);
    }
}
