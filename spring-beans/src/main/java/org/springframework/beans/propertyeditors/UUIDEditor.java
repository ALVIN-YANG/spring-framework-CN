// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、特定用途的适用性或不侵犯第三方权利。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.UUID;
import org.springframework.util.StringUtils;

/**
 * 用于 {@code java.util.UUID} 的编辑器，将 UUID 字符串表示转换为 UUID 对象及其反向转换。
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 * @see java.util.UUID
 */
public class UUIDEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            setValue(UUID.fromString(text.trim()));
        } else {
            setValue(null);
        }
    }

    @Override
    public String getAsText() {
        UUID value = (UUID) getValue();
        return (value != null ? value.toString() : "");
    }
}
