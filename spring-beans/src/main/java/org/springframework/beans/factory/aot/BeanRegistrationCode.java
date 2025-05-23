// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用，除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、特定用途适用性或不侵犯权利。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.aot;

import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.MethodReference;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.javapoet.ClassName;
import org.springframework.util.function.ThrowingBiFunction;

/**
 * 可以用于配置生成以执行单个bean注册的代码的接口。
 *
 * @author Phillip Webb
 * @since 6.0
 * @see BeanRegistrationCodeFragments
 */
public interface BeanRegistrationCode {

    /**
     * 返回用于注册的类名。
     * @return 返回类的名称
     */
    ClassName getClassName();

    /**
     * 返回由注册代码使用的 {@link GeneratedMethods}。
     * @return 生成的类
     */
    GeneratedMethods getMethods();

    /**
     * 在注册代码中添加一个实例后处理器方法调用。
     * @param methodReference 要调用的后处理方法的引用。
     * 引用的方法必须与 {@link InstanceSupplier#andThen} 兼容的功能签名。
     * @see InstanceSupplier#andThen(ThrowingBiFunction)
     */
    void addInstancePostProcessor(MethodReference methodReference);
}
