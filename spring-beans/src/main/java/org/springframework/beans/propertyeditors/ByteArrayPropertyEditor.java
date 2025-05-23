// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者们。
*
* 根据Apache License，版本2.0（“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import org.springframework.lang.Nullable;

/**
 * 字节数组编辑器。字符串将被简单地转换为它们对应的字节数组表示形式。
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 * @see java.lang.String#getBytes
 */
public class ByteArrayPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(@Nullable String text) {
        setValue(text != null ? text.getBytes() : null);
    }

    @Override
    public String getAsText() {
        byte[] value = (byte[]) getValue();
        return (value != null ? new String(value) : "");
    }
}
