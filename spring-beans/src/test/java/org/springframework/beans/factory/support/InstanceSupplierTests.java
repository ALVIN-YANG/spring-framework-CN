// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.util.function.ThrowingBiFunction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * 测试 {@link InstanceSupplier}。
 *
 * @author Phillip Webb
 */
class InstanceSupplierTests {

    private final RegisteredBean registeredBean = RegisteredBean.of(new DefaultListableBeanFactory(), "test");

    @Test
    void getWithoutRegisteredBeanThrowsException() {
        InstanceSupplier<String> supplier = registeredBean -> "test";
        assertThatIllegalStateException().isThrownBy(() -> supplier.get()).withMessage("No RegisteredBean parameter provided");
    }

    @Test
    void getWithExceptionWithoutRegisteredBeanThrowsException() {
        InstanceSupplier<String> supplier = registeredBean -> "test";
        assertThatIllegalStateException().isThrownBy(() -> supplier.getWithException()).withMessage("No RegisteredBean parameter provided");
    }

    @Test
    void getReturnsResult() throws Exception {
        InstanceSupplier<String> supplier = registeredBean -> "test";
        assertThat(supplier.get(this.registeredBean)).isEqualTo("test");
    }

    @Test
    void andThenWhenFunctionIsNullThrowsException() {
        InstanceSupplier<String> supplier = registeredBean -> "test";
        ThrowingBiFunction<RegisteredBean, String, String> after = null;
        assertThatIllegalArgumentException().isThrownBy(() -> supplier.andThen(after)).withMessage("'after' function must not be null");
    }

    @Test
    void andThenAppliesFunctionToObtainResult() throws Exception {
        InstanceSupplier<String> supplier = registeredBean -> "bean";
        supplier = supplier.andThen((registeredBean, string) -> registeredBean.getBeanName() + "-" + string);
        assertThat(supplier.get(this.registeredBean)).isEqualTo("test-bean");
    }

    @Test
    void andThenWhenInstanceSupplierHasFactoryMethod() throws Exception {
        Method factoryMethod = getClass().getDeclaredMethod("andThenWhenInstanceSupplierHasFactoryMethod");
        InstanceSupplier<String> supplier = InstanceSupplier.using(factoryMethod, () -> "bean");
        supplier = supplier.andThen((registeredBean, string) -> registeredBean.getBeanName() + "-" + string);
        assertThat(supplier.get(this.registeredBean)).isEqualTo("test-bean");
        assertThat(supplier.getFactoryMethod()).isSameAs(factoryMethod);
    }

    @Test
    void ofSupplierWhenInstanceSupplierReturnsSameInstance() {
        InstanceSupplier<String> supplier = registeredBean -> "test";
        assertThat(InstanceSupplier.of(supplier)).isSameAs(supplier);
    }

    @Test
    void usingSupplierAdaptsToInstanceSupplier() throws Exception {
        InstanceSupplier<String> instanceSupplier = InstanceSupplier.using(() -> "test");
        assertThat(instanceSupplier.get(this.registeredBean)).isEqualTo("test");
    }

    @Test
    void ofInstanceSupplierAdaptsToInstanceSupplier() throws Exception {
        InstanceSupplier<String> instanceSupplier = InstanceSupplier.of(registeredBean -> "test");
        assertThat(instanceSupplier.get(this.registeredBean)).isEqualTo("test");
    }
}
