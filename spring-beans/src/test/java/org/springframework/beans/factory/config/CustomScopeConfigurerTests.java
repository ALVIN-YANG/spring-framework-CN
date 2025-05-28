// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非适用法律要求或经书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途的适用性或非侵权的保证。
* 请参阅许可证，了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.factory.config;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;

/**
 * 对 {@link CustomScopeConfigurer} 的单元测试。
 *
 * @author Rick Evans
 * @author Juergen Hoeller
 * @author Chris Beams
 */
public class CustomScopeConfigurerTests {

    private static final String FOO_SCOPE = "fooScope";

    private final ConfigurableListableBeanFactory factory = new DefaultListableBeanFactory();

    @Test
    public void testWithNoScopes() {
        CustomScopeConfigurer figurer = new CustomScopeConfigurer();
        figurer.postProcessBeanFactory(factory);
    }

    @Test
    public void testSunnyDayWithBonaFideScopeInstance() {
        Scope scope = mock();
        factory.registerScope(FOO_SCOPE, scope);
        Map<String, Object> scopes = new HashMap<>();
        scopes.put(FOO_SCOPE, scope);
        CustomScopeConfigurer figurer = new CustomScopeConfigurer();
        figurer.setScopes(scopes);
        figurer.postProcessBeanFactory(factory);
    }

    @Test
    public void testSunnyDayWithBonaFideScopeClass() {
        Map<String, Object> scopes = new HashMap<>();
        scopes.put(FOO_SCOPE, NoOpScope.class);
        CustomScopeConfigurer figurer = new CustomScopeConfigurer();
        figurer.setScopes(scopes);
        figurer.postProcessBeanFactory(factory);
        assertThat(factory.getRegisteredScope(FOO_SCOPE) instanceof NoOpScope).isTrue();
    }

    @Test
    public void testSunnyDayWithBonaFideScopeClassName() {
        Map<String, Object> scopes = new HashMap<>();
        scopes.put(FOO_SCOPE, NoOpScope.class.getName());
        CustomScopeConfigurer figurer = new CustomScopeConfigurer();
        figurer.setScopes(scopes);
        figurer.postProcessBeanFactory(factory);
        assertThat(factory.getRegisteredScope(FOO_SCOPE) instanceof NoOpScope).isTrue();
    }

    @Test
    public void testWhereScopeMapHasNullScopeValueInEntrySet() {
        Map<String, Object> scopes = new HashMap<>();
        scopes.put(FOO_SCOPE, null);
        CustomScopeConfigurer figurer = new CustomScopeConfigurer();
        figurer.setScopes(scopes);
        assertThatIllegalArgumentException().isThrownBy(() -> figurer.postProcessBeanFactory(factory));
    }

    @Test
    public void testWhereScopeMapHasNonScopeInstanceInEntrySet() {
        Map<String, Object> scopes = new HashMap<>();
        // <!-- 不是一个有效的值... -->
        scopes.put(FOO_SCOPE, this);
        CustomScopeConfigurer figurer = new CustomScopeConfigurer();
        figurer.setScopes(scopes);
        assertThatIllegalArgumentException().isThrownBy(() -> figurer.postProcessBeanFactory(factory));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testWhereScopeMapHasNonStringTypedScopeNameInKeySet() {
        Map scopes = new HashMap();
        // <!-- 不是一个有效的值（键）... -->
        scopes.put(this, new NoOpScope());
        CustomScopeConfigurer figurer = new CustomScopeConfigurer();
        figurer.setScopes(scopes);
        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> figurer.postProcessBeanFactory(factory));
    }
}
