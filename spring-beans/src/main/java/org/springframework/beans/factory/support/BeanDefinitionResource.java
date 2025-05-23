// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可使用；
* 除非遵守许可协议，否则不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.io.AbstractResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于描述性的包装器，包装了 {@link org.springframework.core.io.Resource}，用于
 * 表示一个 {@link org.springframework.beans.factory.config.BeanDefinition}。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 * @see org.springframework.core.io.DescriptiveResource
 */
class BeanDefinitionResource extends AbstractResource {

    private final BeanDefinition beanDefinition;

    /**
     * 创建一个新的 BeanDefinitionResource。
     * @param beanDefinition 要封装的 BeanDefinition 对象
     */
    public BeanDefinitionResource(BeanDefinition beanDefinition) {
        Assert.notNull(beanDefinition, "BeanDefinition must not be null");
        this.beanDefinition = beanDefinition;
    }

    /**
     * 返回包装的 BeanDefinition 对象。
     */
    public final BeanDefinition getBeanDefinition() {
        return this.beanDefinition;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new FileNotFoundException("Resource cannot be opened because it points to " + getDescription());
    }

    @Override
    public String getDescription() {
        return "BeanDefinition defined in " + this.beanDefinition.getResourceDescription();
    }

    /**
     * 此实现比较底层的BeanDefinition。
     */
    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof BeanDefinitionResource that && this.beanDefinition.equals(that.beanDefinition)));
    }

    /**
     * 此实现返回底层BeanDefinition的哈希码。
     */
    @Override
    public int hashCode() {
        return this.beanDefinition.hashCode();
    }
}
