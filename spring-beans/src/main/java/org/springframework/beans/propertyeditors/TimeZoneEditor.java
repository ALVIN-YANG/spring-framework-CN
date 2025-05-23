// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（"许可证"）许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按"原样"提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的的适用性。
* 请参阅许可证了解具体管理许可权和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.TimeZone;
import org.springframework.util.StringUtils;

/**
 * 用于编辑 {@code java.util.TimeZone} 的编辑器，将时区ID转换为
 * {@code TimeZone} 对象。以文本形式暴露时区ID。
 *
 * @author Juergen Hoeller
 * @author Nicholas Williams
 * @author Sam Brannen
 * @since 3.0
 * @see java.util.TimeZone
 * @see ZoneIdEditor
 */
public class TimeZoneEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            text = text.trim();
        }
        setValue(StringUtils.parseTimeZoneString(text));
    }

    @Override
    public String getAsText() {
        TimeZone value = (TimeZone) getValue();
        return (value != null ? value.getID() : "");
    }
}
