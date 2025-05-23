// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 模拟资源中的任意位置的类。
 *
 * <p>通常用于跟踪XML配置文件中问题或错误元数据的定位。例如，一个
 * {@link #getSource() 源}位置可能是'beans.properties文件第76行定义的bean无效的Class'；另一个源可能是解析的XML
 * {@link org.w3c.dom.Document}中的实际DOM元素；或者源对象可能只是简单地是{@code null}。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class Location {

    private final Resource resource;

    @Nullable
    private final Object source;

    /**
     * 创建一个新的 {@link Location} 类实例。
     * @param resource 与此位置关联的资源
     */
    public Location(Resource resource) {
        this(resource, null);
    }

    /**
     * 创建一个新实例的 {@link Location} 类。
     * @param resource 与此位置关联的资源
     * @param source 相关联资源中的实际位置
     * （可能为 {@code null}）
     */
    public Location(Resource resource, @Nullable Object source) {
        Assert.notNull(resource, "Resource must not be null");
        this.resource = resource;
        this.source = source;
    }

    /**
     * 获取与该位置关联的资源。
     */
    public Resource getResource() {
        return this.resource;
    }

    /**
     * 获取关联的 {@link #getResource() 资源} 内的实际位置（可能为空）。
     * 请参阅该类的 {@link Location 类级别 Javadoc} 以获取返回对象实际类型的示例。
     */
    @Nullable
    public Object getSource() {
        return this.source;
    }
}
