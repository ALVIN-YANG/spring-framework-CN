// 翻译完成 glm-4-flash
/** 版权所有 2002-2014 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵犯知识产权。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.Nullable;

/**
 * 扩展了 {@link org.springframework.beans.factory.config.BeanDefinition} 接口，
 * 该接口公开了关于其 bean 类的 {@link org.springframework.core.type.AnnotationMetadata} 信息，
 * 而无需加载类。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see AnnotatedGenericBeanDefinition
 * @see org.springframework.core.type.AnnotationMetadata
 */
public interface AnnotatedBeanDefinition extends BeanDefinition {

    /**
     * 获取此bean定义的bean类的注解元数据（以及基本类元数据）。
     * @return 注解元数据对象（永远不会为{@code null}）
     */
    AnnotationMetadata getMetadata();

    /**
     * 获取此bean定义的工厂方法的元数据（如果有的话）。
     * @return 工厂方法元数据，如果没有则为{@code null}
     * @since 4.1.1
     */
    @Nullable
    MethodMetadata getFactoryMethodMetadata();
}
