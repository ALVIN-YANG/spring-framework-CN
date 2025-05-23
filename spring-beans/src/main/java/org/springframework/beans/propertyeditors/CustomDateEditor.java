// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（“许可协议”）授权；
* 除非遵守许可协议，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是“按原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议以了解具体管理权限和限制的语言。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 用于{@code java.util.Date}的属性编辑器，支持自定义的{@code java.text.DateFormat}。
 *
 * <p>此编辑器不旨在作为系统属性编辑器使用，而应作为特定于区域的日期编辑器在自定义控制器代码中使用，
 * 将用户输入的数字字符串解析为bean的日期属性，并在UI表单中渲染它们。
 *
 * <p>在Web MVC代码中，此编辑器通常将通过{@code binder.registerCustomEditor}进行注册。
 *
 * @author Juergen Hoeller
 * @since 28.04.2003
 * @see java.util.Date
 * @see java.text.DateFormat
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 */
public class CustomDateEditor extends PropertyEditorSupport {

    private final DateFormat dateFormat;

    private final boolean allowEmpty;

    private final int exactDateLength;

    /**
     * 创建一个新的 CustomDateEditor 实例，使用给定的 DateFormat 进行解析和渲染。
     * <p>"allowEmpty" 参数表示是否应允许空字符串进行解析，即被解释为 null 值。
     * 否则，在这种情况下将抛出 IllegalArgumentException 异常。
     * @param dateFormat 用于解析和渲染的 DateFormat
     * @param allowEmpty 是否允许空字符串
     */
    public CustomDateEditor(DateFormat dateFormat, boolean allowEmpty) {
        this.dateFormat = dateFormat;
        this.allowEmpty = allowEmpty;
        this.exactDateLength = -1;
    }

    /**
     * 创建一个新的 CustomDateEditor 实例，使用给定的 DateFormat 进行解析和渲染。
     * <p>"allowEmpty" 参数表示是否允许空字符串进行解析，即将其解释为 null 值。
     * 否则，在那种情况下会抛出 IllegalArgumentException。
     * <p>"exactDateLength" 参数表示如果字符串不与指定的长度完全匹配，则会抛出 IllegalArgumentException。
     * 这很有用，因为 SimpleDateFormat 不强制对年份部分进行严格解析，即使使用 {@code setLenient(false)} 也不行。
     * 如果没有指定 "exactDateLength"，则 "01/01/05" 会被解析为 "01/01/0005"。
     * 然而，即使指定了 "exactDateLength"，日期或月份部分的前置零仍然可能允许年份部分更短，所以可以将这视为只是对目标日期格式的进一步确认。
     * @param dateFormat 用于解析和渲染的 DateFormat
     * @param allowEmpty 是否允许空字符串
     * @param exactDateLength 日期字符串的预期确切长度
     */
    public CustomDateEditor(DateFormat dateFormat, boolean allowEmpty, int exactDateLength) {
        this.dateFormat = dateFormat;
        this.allowEmpty = allowEmpty;
        this.exactDateLength = exactDateLength;
    }

    /**
     * 从给定文本中解析日期，使用指定的日期格式化工具。
     */
    @Override
    public void setAsText(@Nullable String text) throws IllegalArgumentException {
        if (this.allowEmpty && !StringUtils.hasText(text)) {
            // 将空字符串视为null值。
            setValue(null);
        } else if (text != null && this.exactDateLength >= 0 && text.length() != this.exactDateLength) {
            throw new IllegalArgumentException("Could not parse date: it is not exactly" + this.exactDateLength + "characters long");
        } else {
            try {
                setValue(this.dateFormat.parse(text));
            } catch (ParseException ex) {
                throw new IllegalArgumentException("Could not parse date: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * 使用指定的DateFormat格式化日期为字符串。
     */
    @Override
    public String getAsText() {
        Date value = (Date) getValue();
        return (value != null ? this.dateFormat.format(value) : "");
    }
}
