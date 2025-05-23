// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据Apache许可证2.0版本（“许可证”）授权；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 用于编辑一个 {@link Character} 的编辑器，用于从字符串值中填充类型为 {@code Character} 或 {@code char} 的属性。
 *
 * <p>请注意，JDK 不包含默认的
 * {@link java.beans.PropertyEditor 属性编辑器} 用于 {@code char}！
 * {@link org.springframework.beans.BeanWrapperImpl} 将默认注册此编辑器。
 *
 * <p>还支持从 Unicode 字符序列进行转换；例如，{@code u0041} ('A')。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rick Evans
 * @since 1.2
 * @see Character
 * @see org.springframework.beans.BeanWrapperImpl
 */
public class CharacterEditor extends PropertyEditorSupport {

    /**
     * 用于标识字符串为 Unicode 字符序列的前缀。
     */
    private static final String UNICODE_PREFIX = "\\u";

    /**
     * Unicode字符序列的长度。
     */
    private static final int UNICODE_LENGTH = 6;

    private final boolean allowEmpty;

    /**
     * 创建一个新的 CharacterEditor 实例。
     * <p>"allowEmpty" 参数控制是否允许在解析时接受空字符串，即当执行文本转换时，将其解释为 {@code null} 值。如果为 {@code false}，则在此时将抛出 {@link IllegalArgumentException} 异常。
     * @param allowEmpty 如果要允许空字符串
     */
    public CharacterEditor(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    @Override
    public void setAsText(@Nullable String text) throws IllegalArgumentException {
        if (this.allowEmpty && !StringUtils.hasLength(text)) {
            // 将空字符串视为null值。
            setValue(null);
        } else if (text == null) {
            throw new IllegalArgumentException("null String cannot be converted to char type");
        } else if (isUnicodeCharacterSequence(text)) {
            setAsUnicode(text);
        } else if (text.length() == 1) {
            setValue(text.charAt(0));
        } else {
            throw new IllegalArgumentException("String [" + text + "] with length " + text.length() + " cannot be converted to char type: neither Unicode nor single character");
        }
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        return (value != null ? value.toString() : "");
    }

    private boolean isUnicodeCharacterSequence(String sequence) {
        return (sequence.startsWith(UNICODE_PREFIX) && sequence.length() == UNICODE_LENGTH);
    }

    private void setAsUnicode(String text) {
        int code = Integer.parseInt(text.substring(UNICODE_PREFIX.length()), 16);
        setValue((char) code);
    }
}
