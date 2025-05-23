// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.annotation;

import java.util.stream.Stream;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * 对于 Jakarta 注解，使用 {@link RuntimeHintsRegistrar}。
 * <p>只有当 Jakarta Inject 包含在类路径中时，才会注册这些提示。
 *
 * @author Brian Clozel
 */
class JakartaAnnotationsRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
        if (ClassUtils.isPresent("jakarta.inject.Inject", classLoader)) {
            Stream.of("jakarta.inject.Inject", "jakarta.inject.Qualifier").forEach(annotationType -> hints.reflection().registerType(ClassUtils.resolveClassName(annotationType, classLoader)));
        }
    }
}
