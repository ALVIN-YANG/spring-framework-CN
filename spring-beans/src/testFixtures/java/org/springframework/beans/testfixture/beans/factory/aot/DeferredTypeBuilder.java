// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.testfixture.beans.factory.aot;

import java.util.function.Consumer;
import org.springframework.javapoet.TypeSpec;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于延迟执行到另一个在稍后设置的消费者中的
 * `TypeSpec.Builder` 和 `Consumer`。
 *
 * @author Phillip Webb
 * @since 6.0
 */
public class DeferredTypeBuilder implements Consumer<TypeSpec.Builder> {

    @Nullable
    private Consumer<TypeSpec.Builder> type;

    @Override
    public void accept(TypeSpec.Builder type) {
        Assert.notNull(this.type, "No type builder set");
        this.type.accept(type);
    }

    public void set(Consumer<TypeSpec.Builder> type) {
        this.type = type;
    }
}
