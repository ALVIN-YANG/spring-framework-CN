// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.wiring.BeanWiringInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author Rick Evans
 * @author Chris Beams
 *
 * 作者：Rick Evans
 * 作者：Chris Beams
 */
public class AnnotationBeanWiringInfoResolverTests {

    @Test
    public void testResolveWiringInfo() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new AnnotationBeanWiringInfoResolver().resolveWiringInfo(null));
    }

    @Test
    public void testResolveWiringInfoWithAnInstanceOfANonAnnotatedClass() {
        AnnotationBeanWiringInfoResolver resolver = new AnnotationBeanWiringInfoResolver();
        BeanWiringInfo info = resolver.resolveWiringInfo("java.lang.String is not @Configurable");
        assertThat(info).as("Must be returning null for a non-@Configurable class instance").isNull();
    }

    @Test
    public void testResolveWiringInfoWithAnInstanceOfAnAnnotatedClass() {
        AnnotationBeanWiringInfoResolver resolver = new AnnotationBeanWiringInfoResolver();
        BeanWiringInfo info = resolver.resolveWiringInfo(new Soap());
        assertThat(info).as("Must *not* be returning null for a non-@Configurable class instance").isNotNull();
    }

    @Test
    public void testResolveWiringInfoWithAnInstanceOfAnAnnotatedClassWithAutowiringTurnedOffExplicitly() {
        AnnotationBeanWiringInfoResolver resolver = new AnnotationBeanWiringInfoResolver();
        BeanWiringInfo info = resolver.resolveWiringInfo(new WirelessSoap());
        assertThat(info).as("Must *not* be returning null for an @Configurable class instance even when autowiring is NO").isNotNull();
        assertThat(info.indicatesAutowiring()).isFalse();
        assertThat(info.getBeanName()).isEqualTo(WirelessSoap.class.getName());
    }

    @Test
    public void testResolveWiringInfoWithAnInstanceOfAnAnnotatedClassWithAutowiringTurnedOffExplicitlyAndCustomBeanName() {
        AnnotationBeanWiringInfoResolver resolver = new AnnotationBeanWiringInfoResolver();
        BeanWiringInfo info = resolver.resolveWiringInfo(new NamedWirelessSoap());
        assertThat(info).as("Must *not* be returning null for an @Configurable class instance even when autowiring is NO").isNotNull();
        assertThat(info.indicatesAutowiring()).isFalse();
        assertThat(info.getBeanName()).isEqualTo("DerBigStick");
    }

    @Configurable()
    private static class Soap {
    }

    @Configurable(autowire = Autowire.NO)
    private static class WirelessSoap {
    }

    @Configurable(autowire = Autowire.NO, value = "DerBigStick")
    private static class NamedWirelessSoap {
    }
}
