// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据Apache License，版本2.0（以下简称“许可证”）；除非遵守许可证，否则您不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、特定用途的适用性。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * 简单策略，允许工具控制源元数据如何附加到bean定义元数据上。
 *
 * <p>配置解析器<strong>可能</strong>在解析阶段提供附加源元数据的能力。它们将以通用的格式提供此元数据，该元数据在附加到bean定义元数据之前可以被一个{@link SourceExtractor}进一步修改。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.BeanMetadataElement#getSource()
 * @see org.springframework.beans.factory.config.BeanDefinition
 */
@FunctionalInterface
public interface SourceExtractor {

    /**
     * 从配置解析器提供的候选对象中提取源元数据
     * @param sourceCandidate 原始源元数据（永远不会为 {@code null}）
     * @param definingResource 定义给定源对象的资源（可能为 {@code null}）
     * @return 要存储的源元数据对象（可能为 {@code null}）
     */
    @Nullable
    Object extractSource(Object sourceCandidate, @Nullable Resource definingResource);
}
