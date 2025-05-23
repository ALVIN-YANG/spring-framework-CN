// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * 简单的 {@link SourceExtractor} 实现，它只是将候选源元数据对象传递以进行附加。
 *
 * <p>使用此实现意味着工具将获得对由工具提供的底层配置源元数据的原始访问权限。
 *
 * <p>此实现 <strong>不应</strong> 在生产应用程序中使用，因为它很可能会在内存中保留过多的元数据（不必要）。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class PassThroughSourceExtractor implements SourceExtractor {

    /**
     * 简单地以原样返回提供的 {@code sourceCandidate}。
     * @param sourceCandidate 源元数据
     * @return 提供的 {@code sourceCandidate}
     */
    @Override
    public Object extractSource(Object sourceCandidate, @Nullable Resource definingResource) {
        return sourceCandidate;
    }
}
