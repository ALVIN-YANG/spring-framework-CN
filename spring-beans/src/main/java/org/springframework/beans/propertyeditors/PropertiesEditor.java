// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“许可协议”）许可；
* 除非遵守许可协议，否则不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可协议下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途的适用性。
* 请参阅许可协议，了解具体规定许可权限和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import org.springframework.lang.Nullable;

/**
 * 自定义的 {@link java.beans.PropertyEditor} 用于处理 {@link Properties} 对象。
 *
 * <p>负责将内容 {@link String} 转换为 {@code Properties} 对象。
 * 同时处理从 {@link Map} 到 {@code Properties} 的转换，以便通过 XML 的 "map" 条目填充一个 {@code Properties} 对象。
 *
 * <p>所需的格式在标准的 {@code Properties} 文档中定义。每个属性必须在新的行上。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.util.Properties#load
 */
public class PropertiesEditor extends PropertyEditorSupport {

    /**
     * 将 {@link String} 转换为 {@link Properties}，将其视为属性内容。
     * @param text 要转换的文本
     */
    @Override
    public void setAsText(@Nullable String text) throws IllegalArgumentException {
        Properties props = new Properties();
        if (text != null) {
            try {
                // 必须使用 ISO-8859-1 编码，因为 Properties.load(stream) 方法期望它。
                props.load(new ByteArrayInputStream(text.getBytes(StandardCharsets.ISO_8859_1)));
            } catch (IOException ex) {
                // 这种情况不应发生。
                throw new IllegalArgumentException("Failed to parse [" + text + "] into Properties", ex);
            }
        }
        setValue(props);
    }

    /**
     * 直接使用 {@link Properties}；将 {@link Map} 转换为 {@code Properties}。
     */
    @Override
    public void setValue(Object value) {
        if (!(value instanceof Properties) && value instanceof Map<?, ?> map) {
            Properties props = new Properties();
            props.putAll(map);
            super.setValue(props);
        } else {
            super.setValue(value);
        }
    }
}
