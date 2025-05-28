// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据Apache许可证版本2.0（以下简称“许可证”）授权；
* 除非符合许可证要求，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律强制要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.annotation;

import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;
import org.springframework.util.ClassUtils;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 {@link JakartaAnnotationsRuntimeHints}。
 *
 * @author Brian Clozel
 */
class JakartaAnnotationsRuntimeHintsTests {

    private final RuntimeHints hints = new RuntimeHints();

    @BeforeEach
    void setup() {
        AotServices.factories().load(RuntimeHintsRegistrar.class).forEach(registrar -> registrar.registerHints(this.hints, ClassUtils.getDefaultClassLoader()));
    }

    @Test
    void jakartaInjectAnnotationHasHints() {
        assertThat(RuntimeHintsPredicates.reflection().onType(Inject.class)).accepts(this.hints);
    }

    @Test
    void jakartaQualifierAnnotationHasHints() {
        assertThat(RuntimeHintsPredicates.reflection().onType(Qualifier.class)).accepts(this.hints);
    }
}
