// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 自定义的 {@link java.beans.PropertyEditor} 用于字符串数组。
 *
 * <p>字符串必须以CSV格式存在，并可以使用自定义的分隔符。
 * 默认情况下，结果中的值会去除空白字符。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Dave Syer
 * @see org.springframework.util.StringUtils#delimitedListToStringArray
 * @see org.springframework.util.StringUtils#arrayToDelimitedString
 */
public class StringArrayPropertyEditor extends PropertyEditorSupport {

    /**
     * 默认的字符串分隔符：逗号（","）。
     */
    public static final String DEFAULT_SEPARATOR = ",";

    private final String separator;

    @Nullable
    private final String charsToDelete;

    private final boolean emptyArrayAsNull;

    private final boolean trimValues;

    /**
     * 创建一个新的具有默认分隔符（逗号）的 {@code StringArrayPropertyEditor}。
     * <p>一个空的文本（没有元素）将被转换为一个空数组。
     */
    public StringArrayPropertyEditor() {
        this(DEFAULT_SEPARATOR, null, false);
    }

    /**
     * 创建一个新的带有指定分隔符的 {@code StringArrayPropertyEditor}。
     * <p>空文本（无元素）将被转换为空数组。
     * @param separator 要用于分割一个 {@link String} 的分隔符
     */
    public StringArrayPropertyEditor(String separator) {
        this(separator, null, false);
    }

    /**
     * 使用给定的分隔符创建一个新的 {@code StringArrayPropertyEditor}。
     * @param separator 用于分割一个 {@link String} 的分隔符
     * @param emptyArrayAsNull 如果一个空字符串数组要被转换为 {@code null}，则为 {@code true}
     */
    public StringArrayPropertyEditor(String separator, boolean emptyArrayAsNull) {
        this(separator, null, emptyArrayAsNull);
    }

    /**
     * 使用给定的分隔符创建一个新的 {@code StringArrayPropertyEditor}。
     * @param separator 用于分割一个 {@link String} 的分隔符
     * @param emptyArrayAsNull 如果空字符串数组将被转换为 {@code null}，则设置为 {@code true}
     * @param trimValues 如果要修剪解析数组中的值以移除空白字符（默认为 {@code true}）
     */
    public StringArrayPropertyEditor(String separator, boolean emptyArrayAsNull, boolean trimValues) {
        this(separator, null, emptyArrayAsNull, trimValues);
    }

    /**
     * 使用给定的分隔符创建一个新的 {@code StringArrayPropertyEditor}。
     * @param separator 用于分割 {@link String} 的分隔符。
     * @param charsToDelete 要删除的一组字符，除了对输入字符串进行修剪外。用于删除不需要的换行符很有用：例如 "\r\n\f" 将删除字符串中的所有新行和换行符。
     * @param emptyArrayAsNull 如果空字符串数组应转换为 {@code null}，则设置为 {@code true}。
     */
    public StringArrayPropertyEditor(String separator, @Nullable String charsToDelete, boolean emptyArrayAsNull) {
        this(separator, charsToDelete, emptyArrayAsNull, true);
    }

    /**
     * 使用给定的分隔符创建一个新的 {@code StringArrayPropertyEditor}。
     * @param separator 要用于拆分 {@link String} 的分隔符。
     * @param charsToDelete 要删除的字符集，除了修剪输入字符串。用于删除不需要的换行符很有用：例如，"\r\n\f" 将删除字符串中的所有新行和换行符。
     * @param emptyArrayAsNull 如果空字符串数组要转换为 {@code null}，则为 {@code true}。
     * @param trimValues 如果解析后的数组中的值要被修剪空格（默认为 {@code true}）。
     */
    public StringArrayPropertyEditor(String separator, @Nullable String charsToDelete, boolean emptyArrayAsNull, boolean trimValues) {
        this.separator = separator;
        this.charsToDelete = charsToDelete;
        this.emptyArrayAsNull = emptyArrayAsNull;
        this.trimValues = trimValues;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String[] array = StringUtils.delimitedListToStringArray(text, this.separator, this.charsToDelete);
        if (this.emptyArrayAsNull && array.length == 0) {
            setValue(null);
        } else {
            if (this.trimValues) {
                array = StringUtils.trimArrayElements(array);
            }
            setValue(array);
        }
    }

    @Override
    public String getAsText() {
        return StringUtils.arrayToDelimitedString(ObjectUtils.toObjectArray(getValue()), this.separator);
    }
}
