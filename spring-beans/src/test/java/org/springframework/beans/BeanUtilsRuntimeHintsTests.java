// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议以了解具体规定许可协议下的权限和限制。*/
package org.springframework.beans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 {@link BeanUtilsRuntimeHints}。
 *
 * @author Sebastien Deleuze
 * @since 6.0.10
 * @see org.springframework.http.WebBeanUtilsRuntimeHintsTests
 */
class BeanUtilsRuntimeHintsTests {

    private final RuntimeHints hints = new RuntimeHints();

    @BeforeEach
    void setup() {
        SpringFactoriesLoader.forResourceLocation("META-INF/spring/aot.factories").load(RuntimeHintsRegistrar.class).forEach(registrar -> registrar.registerHints(this.hints, ClassUtils.getDefaultClassLoader()));
    }

    @Test
    void resourceEditorHasHints() {
        assertThat(RuntimeHintsPredicates.reflection().onType(ResourceEditor.class).withMemberCategories(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(this.hints);
    }
}
