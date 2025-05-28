// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.cache.config;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * 共享的{@code TestEntity}的副本：由于Gradle测试固定和AspectJ配置在Gradle构建中存在问题，这是必要的。
 *
 * <p>用于缓存测试的简单测试实体。
 *
 * @author Michael Plod
 */
public class TestEntity {

    private Long id;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.id);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        return (obj instanceof TestEntity that && ObjectUtils.nullSafeEquals(this.id, that.id));
    }
}
