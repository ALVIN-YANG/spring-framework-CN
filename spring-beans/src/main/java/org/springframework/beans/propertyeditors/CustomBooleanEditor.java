// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Boolean/boolean 属性的属性编辑器。
 *
 * <p>此编辑器不是用于系统属性编辑器，而是作为自定义控制器代码中的特定地区 Boolean 编辑器使用，用于将 UI 造成的布尔字符串解析为 bean 的布尔属性并在 UI 表单中进行检查。
 *
 * <p>在 Web MVC 代码中，此编辑器通常通过调用 {@code binder.registerCustomEditor} 进行注册。
 *
 * @author Juergen Hoeller
 * @since 2003年6月10日
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 */
public class CustomBooleanEditor extends PropertyEditorSupport {

    /**
     * “true”的值。
     */
    public static final String VALUE_TRUE = "true";

    /**
     * “false”的值。
     */
    public static final String VALUE_FALSE = "false";

    /**
     * “on”的值。
     */
    public static final String VALUE_ON = "on";

    /**
     * “off”的值。
     */
    public static final String VALUE_OFF = "off";

    /**
     * “yes”的值。
     */
    public static final String VALUE_YES = "yes";

    /**
     * “no”的值。
     */
    public static final String VALUE_NO = "no";

    /**
     * "1" 的值。
     */
    public static final String VALUE_1 = "1";

    /**
     * “0”的值。
     */
    public static final String VALUE_0 = "0";

    @Nullable
    private final String trueString;

    @Nullable
    private final String falseString;

    private final boolean allowEmpty;

    /**
     * 创建一个新的 CustomBooleanEditor 实例，其中 "true"/"on"/"yes" 和 "false"/"off"/"no" 被识别为有效的字符串值。
     * "allowEmpty" 参数表示是否允许空字符串进行解析，即将其解释为 null 值。
     * 否则，在那种情况下将抛出 IllegalArgumentException 异常。
     * @param allowEmpty 如果应该允许空字符串
     */
    public CustomBooleanEditor(boolean allowEmpty) {
        this(null, null, allowEmpty);
    }

    /**
     * 创建一个新的 CustomBooleanEditor 实例，
     * 配置表示 true 和 false 的可配置的 String 值。
     * <p>"allowEmpty" 参数表示是否允许空字符串进行解析，
     * 即将其解释为 null 值。否则，在这种情况下会抛出 IllegalArgumentException。
     * @param trueString 表示 true 的 String 值：
     * 例如，"true" (VALUE_TRUE)，"on" (VALUE_ON)，"yes" (VALUE_YES) 或某个自定义值
     * @param falseString 表示 false 的 String 值：
     * 例如，"false" (VALUE_FALSE)，"off" (VALUE_OFF)，"no" (VALUE_NO) 或某个自定义值
     * @param allowEmpty 如果应允许空字符串
     * @see #VALUE_TRUE
     * @see #VALUE_FALSE
     * @see #VALUE_ON
     * @see #VALUE_OFF
     * @see #VALUE_YES
     * @see #VALUE_NO
     */
    public CustomBooleanEditor(@Nullable String trueString, @Nullable String falseString, boolean allowEmpty) {
        this.trueString = trueString;
        this.falseString = falseString;
        this.allowEmpty = allowEmpty;
    }

    @Override
    public void setAsText(@Nullable String text) throws IllegalArgumentException {
        String input = (text != null ? text.trim() : null);
        if (this.allowEmpty && !StringUtils.hasLength(input)) {
            // 将空字符串视为null值。
            setValue(null);
        } else if (this.trueString != null && this.trueString.equalsIgnoreCase(input)) {
            setValue(Boolean.TRUE);
        } else if (this.falseString != null && this.falseString.equalsIgnoreCase(input)) {
            setValue(Boolean.FALSE);
        } else if (this.trueString == null && (VALUE_TRUE.equalsIgnoreCase(input) || VALUE_ON.equalsIgnoreCase(input) || VALUE_YES.equalsIgnoreCase(input) || VALUE_1.equals(input))) {
            setValue(Boolean.TRUE);
        } else if (this.falseString == null && (VALUE_FALSE.equalsIgnoreCase(input) || VALUE_OFF.equalsIgnoreCase(input) || VALUE_NO.equalsIgnoreCase(input) || VALUE_0.equals(input))) {
            setValue(Boolean.FALSE);
        } else {
            throw new IllegalArgumentException("Invalid boolean value [" + text + "]");
        }
    }

    @Override
    public String getAsText() {
        if (Boolean.TRUE.equals(getValue())) {
            return (this.trueString != null ? this.trueString : VALUE_TRUE);
        } else if (Boolean.FALSE.equals(getValue())) {
            return (this.falseString != null ? this.falseString : VALUE_FALSE);
        } else {
            return "";
        }
    }
}
