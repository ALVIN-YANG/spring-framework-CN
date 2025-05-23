// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体管理权限和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.nio.charset.Charset;
import org.springframework.util.StringUtils;

/**
 * 用于编辑 `java.nio.charset.Charset` 的编辑器，将字符集的字符串表示转换为 `Charset` 对象，以及反向转换。
 *
 * <p>期望使用与 `Charset` 的 `@link java.nio.charset.Charset#name()` 相同的语法，例如 `UTF-8`、`ISO-8859-16` 等。
 *
 * @author Arjen Poutsma
 * @author Sam Brannen
 * @since 2.5.4
 * @see Charset
 */
public class CharsetEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            setValue(Charset.forName(text.trim()));
        } else {
            setValue(null);
        }
    }

    @Override
    public String getAsText() {
        Charset value = (Charset) getValue();
        return (value != null ? value.name() : "");
    }
}
