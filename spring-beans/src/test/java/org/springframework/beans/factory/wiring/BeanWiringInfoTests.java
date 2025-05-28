// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据Apache License，版本2.0（以下简称“许可证”）；除非遵守许可证规定，否则您不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途的适用性或不侵犯第三方权利。
* 请参阅许可证了解具体管理权限和限制的详细语言。*/
package org.springframework.beans.factory.wiring;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * BeanWiringInfo 类的单元测试。
 *
 * @author Rick Evans
 * @author Sam Brannen
 */
public class BeanWiringInfoTests {

    @Test
    public void ctorWithNullBeanName() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new BeanWiringInfo(null));
    }

    @Test
    public void ctorWithWhitespacedBeanName() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new BeanWiringInfo("   \t"));
    }

    @Test
    public void ctorWithEmptyBeanName() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new BeanWiringInfo(""));
    }

    @Test
    public void ctorWithNegativeIllegalAutowiringValue() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new BeanWiringInfo(-1, true));
    }

    @Test
    public void ctorWithPositiveOutOfRangeAutowiringValue() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new BeanWiringInfo(123871, true));
    }

    @Test
    public void usingAutowireCtorIndicatesAutowiring() throws Exception {
        BeanWiringInfo info = new BeanWiringInfo(BeanWiringInfo.AUTOWIRE_BY_NAME, true);
        assertThat(info.indicatesAutowiring()).isTrue();
    }

    @Test
    public void usingBeanNameCtorDoesNotIndicateAutowiring() throws Exception {
        BeanWiringInfo info = new BeanWiringInfo("fooService");
        assertThat(info.indicatesAutowiring()).isFalse();
    }

    @Test
    public void noDependencyCheckValueIsPreserved() throws Exception {
        BeanWiringInfo info = new BeanWiringInfo(BeanWiringInfo.AUTOWIRE_BY_NAME, true);
        assertThat(info.getDependencyCheck()).isTrue();
    }

    @Test
    public void dependencyCheckValueIsPreserved() throws Exception {
        BeanWiringInfo info = new BeanWiringInfo(BeanWiringInfo.AUTOWIRE_BY_TYPE, false);
        assertThat(info.getDependencyCheck()).isFalse();
    }
}
