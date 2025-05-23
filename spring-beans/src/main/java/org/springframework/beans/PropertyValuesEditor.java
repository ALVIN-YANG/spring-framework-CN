// 翻译完成 glm-4-flash
/** 版权所有 2002-2007 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明示或暗示。有关权限和限制的特定语言，请参阅许可证。*/
package org.springframework.beans;

import java.beans.PropertyEditorSupport;
import java.util.Properties;
import org.springframework.beans.propertyeditors.PropertiesEditor;

/**
 * 用于 {@link PropertyValues} 对象的 {@link java.beans.PropertyEditor} 编辑器。
 *
 * <p>所需的格式定义在 {@link java.util.Properties} 文档中。每个属性必须占一行。
 *
 * <p>当前实现依赖于底层的 {@link org.springframework.beans.propertyeditors.PropertiesEditor}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class PropertyValuesEditor extends PropertyEditorSupport {

    private final PropertiesEditor propertiesEditor = new PropertiesEditor();

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.propertiesEditor.setAsText(text);
        Properties props = (Properties) this.propertiesEditor.getValue();
        setValue(new MutablePropertyValues(props));
    }
}
