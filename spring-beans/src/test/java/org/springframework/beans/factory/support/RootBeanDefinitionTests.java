// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按照“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证以了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 测试{@link RootBeanDefinition}。
 *
 * @author Stephane Nicoll
 */
class RootBeanDefinitionTests {

    @Test
    void setInstanceSetResolvedFactoryMethod() {
        InstanceSupplier<?> instanceSupplier = mock();
        Method method = ReflectionUtils.findMethod(String.class, "toString");
        given(instanceSupplier.getFactoryMethod()).willReturn(method);
        RootBeanDefinition beanDefinition = new RootBeanDefinition(String.class);
        beanDefinition.setInstanceSupplier(instanceSupplier);
        assertThat(beanDefinition.getResolvedFactoryMethod()).isEqualTo(method);
        verify(instanceSupplier).getFactoryMethod();
    }

    @Test
    void setInstanceDoesNotOverrideResolvedFactoryMethodWithNull() {
        InstanceSupplier<?> instanceSupplier = mock();
        given(instanceSupplier.getFactoryMethod()).willReturn(null);
        Method method = ReflectionUtils.findMethod(String.class, "toString");
        RootBeanDefinition beanDefinition = new RootBeanDefinition(String.class);
        beanDefinition.setResolvedFactoryMethod(method);
        beanDefinition.setInstanceSupplier(instanceSupplier);
        assertThat(beanDefinition.getResolvedFactoryMethod()).isEqualTo(method);
        verify(instanceSupplier).getFactoryMethod();
    }

    @Test
    void resolveDestroyMethodWithMatchingCandidateReplacedInferredVaue() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(BeanWithCloseMethod.class);
        beanDefinition.setDestroyMethodName(AbstractBeanDefinition.INFER_METHOD);
        beanDefinition.resolveDestroyMethodIfNecessary();
        assertThat(beanDefinition.getDestroyMethodNames()).containsExactly("close");
    }

    @Test
    void resolveDestroyMethodWithNoCandidateSetDestroyMethodNameToNull() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(BeanWithNoDestroyMethod.class);
        beanDefinition.setDestroyMethodName(AbstractBeanDefinition.INFER_METHOD);
        beanDefinition.resolveDestroyMethodIfNecessary();
        assertThat(beanDefinition.getDestroyMethodNames()).isNull();
    }

    @Test
    void resolveDestroyMethodWithNoResolvableType() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setDestroyMethodName(AbstractBeanDefinition.INFER_METHOD);
        beanDefinition.resolveDestroyMethodIfNecessary();
        assertThat(beanDefinition.getDestroyMethodNames()).isNull();
    }

    static class BeanWithCloseMethod {

        public void close() {
        }
    }

    static class BeanWithNoDestroyMethod {
    }
}
