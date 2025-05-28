// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体规定许可权限和限制。*/
package org.springframework.beans.propertyeditors;

import java.util.ResourceBundle;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 对 {@link ResourceBundleEditor} 类的单元测试。
 *
 * @author Rick Evans
 * @author Chris Beams
 */
public class ResourceBundleEditorTests {

    private static final String BASE_NAME = ResourceBundleEditorTests.class.getName();

    private static final String MESSAGE_KEY = "punk";

    @Test
    public void testSetAsTextWithJustBaseName() {
        ResourceBundleEditor editor = new ResourceBundleEditor();
        editor.setAsText(BASE_NAME);
        Object value = editor.getValue();
        assertThat(value).as("Returned ResourceBundle was null (must not be for valid setAsText(..) call).").isNotNull();
        assertThat(value instanceof ResourceBundle).as("Returned object was not a ResourceBundle (must be for valid setAsText(..) call).").isTrue();
        ResourceBundle bundle = (ResourceBundle) value;
        String string = bundle.getString(MESSAGE_KEY);
        assertThat(string).isEqualTo(MESSAGE_KEY);
    }

    @Test
    public void testSetAsTextWithBaseNameThatEndsInDefaultSeparator() {
        ResourceBundleEditor editor = new ResourceBundleEditor();
        editor.setAsText(BASE_NAME + "_");
        Object value = editor.getValue();
        assertThat(value).as("Returned ResourceBundle was null (must not be for valid setAsText(..) call).").isNotNull();
        assertThat(value instanceof ResourceBundle).as("Returned object was not a ResourceBundle (must be for valid setAsText(..) call).").isTrue();
        ResourceBundle bundle = (ResourceBundle) value;
        String string = bundle.getString(MESSAGE_KEY);
        assertThat(string).isEqualTo(MESSAGE_KEY);
    }

    @Test
    public void testSetAsTextWithBaseNameAndLanguageCode() {
        ResourceBundleEditor editor = new ResourceBundleEditor();
        editor.setAsText(BASE_NAME + "Lang" + "_en");
        Object value = editor.getValue();
        assertThat(value).as("Returned ResourceBundle was null (must not be for valid setAsText(..) call).").isNotNull();
        assertThat(value instanceof ResourceBundle).as("Returned object was not a ResourceBundle (must be for valid setAsText(..) call).").isTrue();
        ResourceBundle bundle = (ResourceBundle) value;
        String string = bundle.getString(MESSAGE_KEY);
        assertThat(string).isEqualTo("yob");
    }

    @Test
    public void testSetAsTextWithBaseNameLanguageAndCountryCode() {
        ResourceBundleEditor editor = new ResourceBundleEditor();
        editor.setAsText(BASE_NAME + "LangCountry" + "_en_GB");
        Object value = editor.getValue();
        assertThat(value).as("Returned ResourceBundle was null (must not be for valid setAsText(..) call).").isNotNull();
        assertThat(value instanceof ResourceBundle).as("Returned object was not a ResourceBundle (must be for valid setAsText(..) call).").isTrue();
        ResourceBundle bundle = (ResourceBundle) value;
        String string = bundle.getString(MESSAGE_KEY);
        assertThat(string).isEqualTo("chav");
    }

    @Test
    public void testSetAsTextWithTheKitchenSink() {
        ResourceBundleEditor editor = new ResourceBundleEditor();
        editor.setAsText(BASE_NAME + "LangCountryDialect" + "_en_GB_GLASGOW");
        Object value = editor.getValue();
        assertThat(value).as("Returned ResourceBundle was null (must not be for valid setAsText(..) call).").isNotNull();
        assertThat(value instanceof ResourceBundle).as("Returned object was not a ResourceBundle (must be for valid setAsText(..) call).").isTrue();
        ResourceBundle bundle = (ResourceBundle) value;
        String string = bundle.getString(MESSAGE_KEY);
        assertThat(string).isEqualTo("ned");
    }

    @Test
    public void testSetAsTextWithNull() {
        ResourceBundleEditor editor = new ResourceBundleEditor();
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setAsText(null));
    }

    @Test
    public void testSetAsTextWithEmptyString() {
        ResourceBundleEditor editor = new ResourceBundleEditor();
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setAsText(""));
    }

    @Test
    public void testSetAsTextWithWhiteSpaceString() {
        ResourceBundleEditor editor = new ResourceBundleEditor();
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setAsText("   "));
    }

    @Test
    public void testSetAsTextWithJustSeparatorString() {
        ResourceBundleEditor editor = new ResourceBundleEditor();
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setAsText("_"));
    }
}
