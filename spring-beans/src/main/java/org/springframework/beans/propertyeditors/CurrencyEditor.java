// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0 ("许可协议") 进行许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“现状”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议以了解具体的管理权限和限制。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.Currency;
import org.springframework.util.StringUtils;

/**
 * 用于编辑 `java.util.Currency` 的编辑器，将货币代码转换为 `Currency` 对象。以货币对象的文本表示形式公开货币代码。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see java.util.Currency
 */
public class CurrencyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            text = text.trim();
        }
        setValue(Currency.getInstance(text));
    }

    @Override
    public String getAsText() {
        Currency value = (Currency) getValue();
        return (value != null ? value.getCurrencyCode() : "");
    }
}
