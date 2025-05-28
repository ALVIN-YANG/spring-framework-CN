// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按 "原样" 分发，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的的适用性。
* 请参阅许可证以了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory.wiring;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 对 ClassNameBeanWiringInfoResolver 类的单元测试。
 *
 * @author Rick Evans
 */
class ClassNameBeanWiringInfoResolverTests {

    @Test
    void resolveWiringInfoWithNullBeanInstance() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new ClassNameBeanWiringInfoResolver().resolveWiringInfo(null));
    }

    @Test
    void resolveWiringInfo() {
        ClassNameBeanWiringInfoResolver resolver = new ClassNameBeanWiringInfoResolver();
        Long beanInstance = 1L;
        BeanWiringInfo info = resolver.resolveWiringInfo(beanInstance);
        assertThat(info).isNotNull();
        assertThat(info.getBeanName()).as("Not resolving bean name to the class name of the supplied bean instance as per class contract.").isEqualTo(beanInstance.getClass().getName());
    }
}
