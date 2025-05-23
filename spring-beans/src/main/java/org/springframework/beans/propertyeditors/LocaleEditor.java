// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或特定用途适用性。
* 请参阅许可证，了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import org.springframework.util.StringUtils;

/**
 * 用于编辑 `java.util.Locale` 的编辑器，可以直接填充Locale属性。
 *
 * <p>期望与Locale的 `toString()` 的语法相同，即语言 + 可选的国家 + 可选的变体，由 "_" 分隔（例如 "en"，"en_US"）。
 * 还接受空格作为分隔符，作为下划线的替代。
 *
 * @author Juergen Hoeller
 * @since 26.05.2003
 * @see java.util.Locale
 * @see org.springframework.util.StringUtils#parseLocaleString
 */
public class LocaleEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        setValue(StringUtils.parseLocaleString(text));
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        return (value != null ? value.toString() : "");
    }
}
