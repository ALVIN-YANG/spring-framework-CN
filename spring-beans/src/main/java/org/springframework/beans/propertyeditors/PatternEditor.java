// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或原作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.regex.Pattern;
import org.springframework.lang.Nullable;

/**
 * 用于编辑 `java.util.regex.Pattern` 的编辑器，可以直接填充一个模式属性。
 * 期望与 `Pattern` 的 `compile` 方法的语法相同。
 *
 * @author Juergen Hoeller
 * @since 2.0.1
 * @see java.util.regex.Pattern
 * @see java.util.regex.Pattern#compile(String)
 */
public class PatternEditor extends PropertyEditorSupport {

    private final int flags;

    /**
     * 创建一个新的 PatternEditor，使用默认设置。
     */
    public PatternEditor() {
        this.flags = 0;
    }

    /**
     * 使用给定的设置创建一个新的 PatternEditor。
     * @param flags 要应用的 {@code java.util.regex.Pattern} 标志
     * @see java.util.regex.Pattern#compile(String, int)
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see java.util.regex.Pattern#MULTILINE
     * @see java.util.regex.Pattern#DOTALL
     * @see java.util.regex.Pattern#UNICODE_CASE
     * @see java.util.regex.Pattern#CANON_EQ
     */
    public PatternEditor(int flags) {
        this.flags = flags;
    }

    @Override
    public void setAsText(@Nullable String text) {
        setValue(text != null ? Pattern.compile(text, this.flags) : null);
    }

    @Override
    public String getAsText() {
        Pattern value = (Pattern) getValue();
        return (value != null ? value.pattern() : "");
    }
}
