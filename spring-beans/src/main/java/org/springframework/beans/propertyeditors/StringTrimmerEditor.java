// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”）进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下网址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件
* 是在“现状”基础上分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 字符串修剪属性编辑器。
 *
 * <p>可选地将空字符串转换为 {@code null} 值。
 * 需要显式注册，例如用于命令绑定。
 *
 * @author Juergen Hoeller
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 */
public class StringTrimmerEditor extends PropertyEditorSupport {

    @Nullable
    private final String charsToDelete;

    private final boolean emptyAsNull;

    /**
     * 创建一个新的 StringTrimmerEditor。
     * @param emptyAsNull 如果一个空字符串要被转换为 null，则设置为 {@code true}
     */
    public StringTrimmerEditor(boolean emptyAsNull) {
        this.charsToDelete = null;
        this.emptyAsNull = emptyAsNull;
    }

    /**
     * 创建一个新的 StringTrimmerEditor。
     * @param charsToDelete 要删除的字符集合，除了修剪输入的字符串。用于删除不需要的换行符很有用：
     * 例如 "\r\n\f" 将删除字符串中的所有换行符和换行符。
     * @param emptyAsNull 如果为 {@code true}，则空字符串将被转换为 {@code null}
     */
    public StringTrimmerEditor(String charsToDelete, boolean emptyAsNull) {
        this.charsToDelete = charsToDelete;
        this.emptyAsNull = emptyAsNull;
    }

    @Override
    public void setAsText(@Nullable String text) {
        if (text == null) {
            setValue(null);
        } else {
            String value = text.trim();
            if (this.charsToDelete != null) {
                value = StringUtils.deleteAny(value, this.charsToDelete);
            }
            if (this.emptyAsNull && value.isEmpty()) {
                setValue(null);
            } else {
                setValue(value);
            }
        }
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        return (value != null ? value.toString() : "");
    }
}
