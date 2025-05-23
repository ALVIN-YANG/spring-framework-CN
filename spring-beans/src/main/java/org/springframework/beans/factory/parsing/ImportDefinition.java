// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 您必须遵守许可证才能使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件
* 是在 "按原样" 基础上分发的，不提供任何形式（明示或暗示）的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.parsing;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 解析过程中已处理过的导入表示。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ReaderEventListener#importProcessed(ImportDefinition)
 */
public class ImportDefinition implements BeanMetadataElement {

    private final String importedResource;

    @Nullable
    private final Resource[] actualResources;

    @Nullable
    private final Object source;

    /**
     * 创建一个新的 ImportDefinition 对象。
     * @param importedResource 导入资源的位置
     */
    public ImportDefinition(String importedResource) {
        this(importedResource, null, null);
    }

    /**
     * 创建一个新的 ImportDefinition。
     * @param importedResource 被导入资源的位置
     * @param source 源对象（可能为 {@code null}）
     */
    public ImportDefinition(String importedResource, @Nullable Object source) {
        this(importedResource, null, source);
    }

    /**
     * 创建一个新的 ImportDefinition。
     * @param importedResource 导入资源的位置
     * @param source 源对象（可能为 {@code null}）
     */
    public ImportDefinition(String importedResource, @Nullable Resource[] actualResources, @Nullable Object source) {
        Assert.notNull(importedResource, "Imported resource must not be null");
        this.importedResource = importedResource;
        this.actualResources = actualResources;
        this.source = source;
    }

    /**
     * 返回导入资源的位置。
     */
    public final String getImportedResource() {
        return this.importedResource;
    }

    @Nullable
    public final Resource[] getActualResources() {
        return this.actualResources;
    }

    @Override
    @Nullable
    public final Object getSource() {
        return this.source;
    }
}
