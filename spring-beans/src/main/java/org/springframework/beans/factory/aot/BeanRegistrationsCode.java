// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.aot;

import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.javapoet.ClassName;

/**
 * 可用于配置生成注册bean的代码的接口。
 *
 * @author Phillip Webb
 * @since 6.0
 */
public interface BeanRegistrationsCode {

    /**
     * 返回用于注册的类的名称。
     * @return 生成的类名。
     */
    ClassName getClassName();

    /**
     * 返回由注册代码使用的 {@link GeneratedMethods}。
     * @return 方法生成器
     */
    GeneratedMethods getMethods();
}
