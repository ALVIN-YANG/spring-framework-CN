// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”），您可能仅在不违反许可证的情况下使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体规定许可范围和限制。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.time.ZoneId;
import org.springframework.util.StringUtils;

/**
 * 用于编辑 `java.time.ZoneId` 的编辑器，将时区 ID 字符串转换为 `ZoneId` 对象。以文本形式暴露 `TimeZone` ID。
 *
 * @author Nicholas Williams
 * @author Sam Brannen
 * @since 4.0
 * @see java.time.ZoneId
 * @see TimeZoneEditor
 */
public class ZoneIdEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            text = text.trim();
        }
        setValue(ZoneId.of(text));
    }

    @Override
    public String getAsText() {
        ZoneId value = (ZoneId) getValue();
        return (value != null ? value.getId() : "");
    }
}
