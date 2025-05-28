// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证，了解具体的权限和限制。*/
package org.springframework.beans.factory.annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.util.ClassUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * 对{@link ParameterResolutionDelegate}的单元测试。
 *
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @author Loïc Ledoyen
 */
public class ParameterResolutionTests {

    @Test
    public void isAutowirablePreconditions() {
        assertThatIllegalArgumentException().isThrownBy(() -> ParameterResolutionDelegate.isAutowirable(null, 0)).withMessageContaining("Parameter must not be null");
    }

    @Test
    public void annotatedParametersInMethodAreCandidatesForAutowiring() throws Exception {
        Method method = getClass().getDeclaredMethod("autowirableMethod", String.class, String.class, String.class, String.class);
        assertAutowirableParameters(method);
    }

    @Test
    public void annotatedParametersInTopLevelClassConstructorAreCandidatesForAutowiring() throws Exception {
        Constructor<?> constructor = AutowirableClass.class.getConstructor(String.class, String.class, String.class, String.class);
        assertAutowirableParameters(constructor);
    }

    @Test
    public void annotatedParametersInInnerClassConstructorAreCandidatesForAutowiring() throws Exception {
        Class<?> innerClass = AutowirableClass.InnerAutowirableClass.class;
        assertThat(ClassUtils.isInnerClass(innerClass)).isTrue();
        Constructor<?> constructor = innerClass.getConstructor(AutowirableClass.class, String.class, String.class);
        assertAutowirableParameters(constructor);
    }

    private void assertAutowirableParameters(Executable executable) {
        int startIndex = (executable instanceof Constructor) && ClassUtils.isInnerClass(executable.getDeclaringClass()) ? 1 : 0;
        Parameter[] parameters = executable.getParameters();
        for (int parameterIndex = startIndex; parameterIndex < parameters.length; parameterIndex++) {
            Parameter parameter = parameters[parameterIndex];
            assertThat(ParameterResolutionDelegate.isAutowirable(parameter, parameterIndex)).as("Parameter " + parameter + " must be autowirable").isTrue();
        }
    }

    @Test
    public void nonAnnotatedParametersInTopLevelClassConstructorAreNotCandidatesForAutowiring() throws Exception {
        Constructor<?> notAutowirableConstructor = AutowirableClass.class.getConstructor(String.class);
        Parameter[] parameters = notAutowirableConstructor.getParameters();
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            Parameter parameter = parameters[parameterIndex];
            assertThat(ParameterResolutionDelegate.isAutowirable(parameter, parameterIndex)).as("Parameter " + parameter + " must not be autowirable").isFalse();
        }
    }

    @Test
    public void resolveDependencyPreconditionsForParameter() {
        assertThatIllegalArgumentException().isThrownBy(() -> ParameterResolutionDelegate.resolveDependency(null, 0, null, mock())).withMessageContaining("Parameter must not be null");
    }

    @Test
    public void resolveDependencyPreconditionsForContainingClass() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> ParameterResolutionDelegate.resolveDependency(getParameter(), 0, null, null)).withMessageContaining("Containing class must not be null");
    }

    @Test
    public void resolveDependencyPreconditionsForBeanFactory() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> ParameterResolutionDelegate.resolveDependency(getParameter(), 0, getClass(), null)).withMessageContaining("AutowireCapableBeanFactory must not be null");
    }

    private Parameter getParameter() throws NoSuchMethodException {
        Method method = getClass().getDeclaredMethod("autowirableMethod", String.class, String.class, String.class, String.class);
        return method.getParameters()[0];
    }

    @Test
    public void resolveDependencyForAnnotatedParametersInTopLevelClassConstructor() throws Exception {
        Constructor<?> constructor = AutowirableClass.class.getConstructor(String.class, String.class, String.class, String.class);
        AutowireCapableBeanFactory beanFactory = mock();
        // 配置模拟的BeanFactory以返回DependencyDescriptor，以便于使用。
        // 为了避免使用 ArgumentCaptor。
        given(beanFactory.resolveDependency(any(), isNull())).willAnswer(invocation -> invocation.getArgument(0));
        Parameter[] parameters = constructor.getParameters();
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            Parameter parameter = parameters[parameterIndex];
            DependencyDescriptor intermediateDependencyDescriptor = (DependencyDescriptor) ParameterResolutionDelegate.resolveDependency(parameter, parameterIndex, AutowirableClass.class, beanFactory);
            assertThat(intermediateDependencyDescriptor.getAnnotatedElement()).isEqualTo(constructor);
            assertThat(intermediateDependencyDescriptor.getMethodParameter().getParameter()).isEqualTo(parameter);
        }
    }

    void autowirableMethod(@Autowired String firstParameter, @Qualifier("someQualifier") String secondParameter, @Value("${someValue}") String thirdParameter, @Autowired(required = false) String fourthParameter) {
    }

    public static class AutowirableClass {

        public AutowirableClass(@Autowired String firstParameter, @Qualifier("someQualifier") String secondParameter, @Value("${someValue}") String thirdParameter, @Autowired(required = false) String fourthParameter) {
        }

        public AutowirableClass(String notAutowirableParameter) {
        }

        public class InnerAutowirableClass {

            public InnerAutowirableClass(@Autowired String firstParameter, @Qualifier("someQualifier") String secondParameter) {
            }
        }
    }
}
