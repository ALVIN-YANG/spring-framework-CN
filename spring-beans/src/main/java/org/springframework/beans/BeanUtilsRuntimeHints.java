// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.ResourceEditor;
import org.springframework.lang.Nullable;

/**
 * 使用 {@link RuntimeHintsRegistrar} 来注册对流行约定的提示，
 * 用于在 {@link BeanUtils#findEditorByConvention(Class)} 中查找。
 *
 * @author Sébastien Deleuze
 * @since 6.0.10
 */
class BeanUtilsRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
        ReflectionHints reflectionHints = hints.reflection();
        reflectionHints.registerType(ResourceEditor.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        reflectionHints.registerTypeIfPresent(classLoader, "org.springframework.http.MediaTypeEditor", MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
    }
}
