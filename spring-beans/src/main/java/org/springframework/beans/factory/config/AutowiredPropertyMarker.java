// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.config;

import java.io.Serializable;
import org.springframework.lang.Nullable;

/**
 * 简单的标记类，用于单独自动装配的属性值，将被添加到特定bean属性的`@link BeanDefinition#getPropertyValues()`.
 *
 * <p>在运行时，这将替换为对应bean属性写入方法的`@link DependencyDescriptor`，最终将通过`@link AutowireCapableBeanFactory#resolveDependency`步骤进行解析。
 *
 * @author Juergen Hoeller
 * @since 5.2
 * @see AutowireCapableBeanFactory#resolveDependency
 * @see BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.support.BeanDefinitionBuilder#addAutowiredProperty
 */
@SuppressWarnings("serial")
public final class AutowiredPropertyMarker implements Serializable {

    /**
     * 自动注入标记值的规范实例。
     */
    public static final Object INSTANCE = new AutowiredPropertyMarker();

    private AutowiredPropertyMarker() {
    }

    private Object readResolve() {
        return INSTANCE;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other);
    }

    @Override
    public int hashCode() {
        return AutowiredPropertyMarker.class.hashCode();
    }

    @Override
    public String toString() {
        return "(autowired)";
    }
}
