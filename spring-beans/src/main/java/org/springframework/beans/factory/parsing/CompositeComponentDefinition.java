// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.beans.factory.parsing;

import java.util.ArrayList;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link ComponentDefinition} 的实现，它包含一个或多个嵌套的
 * {@link ComponentDefinition} 实例，并将它们聚合为一个命名的组件组。
 *
 * @author Juergen Hoeller
 * @since 2.0.1
 * @see #getNestedComponents()
 */
public class CompositeComponentDefinition extends AbstractComponentDefinition {

    private final String name;

    @Nullable
    private final Object source;

    private final List<ComponentDefinition> nestedComponents = new ArrayList<>();

    /**
     * 创建一个新的 CompositeComponentDefinition。
     * @param name 组合组件的名称
     * @param source 定义组合组件根元素的源元素
     */
    public CompositeComponentDefinition(String name, @Nullable Object source) {
        Assert.notNull(name, "Name must not be null");
        this.name = name;
        this.source = source;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    @Nullable
    public Object getSource() {
        return this.source;
    }

    /**
     * 将给定组件添加为该组合组件的嵌套元素。
     * @param component 要添加的嵌套组件
     */
    public void addNestedComponent(ComponentDefinition component) {
        Assert.notNull(component, "ComponentDefinition must not be null");
        this.nestedComponents.add(component);
    }

    /**
     * 返回此组合组件所持有的嵌套组件。
     * @return 返回嵌套组件的数组，如果没有则返回空数组
     */
    public ComponentDefinition[] getNestedComponents() {
        return this.nestedComponents.toArray(new ComponentDefinition[0]);
    }
}
