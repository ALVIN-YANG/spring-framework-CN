// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体规定权限和限制。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 对于标准 JDK 的 {@link java.util.ResourceBundle ResourceBundles} 的 {@link java.beans.PropertyEditor} 实现。
 *
 * <p>仅支持从 String 类型进行转换，但不支持转换到 String 类型。
 *
 * 以下是一些使用此类在（正确配置的）基于 XML 元数据的 Spring 容器中的示例：
 *
 * <pre class="code"> &lt;bean id="errorDialog" class="..."&gt;
 *    &lt;!--
 *        'messages' 属性的类型为 java.util.ResourceBundle。
 *        'DialogMessages.properties' 文件位于 CLASSPATH 的根目录下
 *    --&gt;
 *    &lt;property name="messages" value="DialogMessages"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * <pre class="code"> &lt;bean id="errorDialog" class="..."&gt;
 *    &lt;!--
 *        'DialogMessages.properties' 文件位于 'com/messages' 包中
 *    --&gt;
 *    &lt;property name="messages" value="com/messages/DialogMessages"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * <p>一个“正确配置”的 Spring {@link org.springframework.context.ApplicationContext 容器} 可能包含一个 {@link org.springframework.beans.factory.config.CustomEditorConfigurer} 定义，以便透明地实现转换：
 *
 * <pre class="code"> &lt;bean class="org.springframework.beans.factory.config.CustomEditorConfigurer"&gt;
 *    &lt;property name="customEditors"&gt;
 *        &lt;map&gt;
 *            &lt;entry key="java.util.ResourceBundle"&gt;
 *                &lt;bean class="org.springframework.beans.propertyeditors.ResourceBundleEditor"/&gt;
 *            &lt;/entry&gt;
 *        &lt;/map&gt;
 *    &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * <p>请注意，此 {@link java.beans.PropertyEditor} 默认情况下并未注册到任何 Spring 基础设施中。
 *
 * <p>感谢 David Leal Valmana 提出建议和初始原型。
 *
 * @author Rick Evans
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ResourceBundleEditor extends PropertyEditorSupport {

    /**
     * 在从字符串转换时，用于区分基本名称和（如果有）区域设置的分隔符
     */
    public static final String BASE_NAME_SEPARATOR = "_";

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        Assert.hasText(text, "'text' must not be empty");
        String name = text.trim();
        int separator = name.indexOf(BASE_NAME_SEPARATOR);
        if (separator == -1) {
            setValue(ResourceBundle.getBundle(name));
        } else {
            // 该名称可能包含区域信息
            String baseName = name.substring(0, separator);
            if (!StringUtils.hasText(baseName)) {
                throw new IllegalArgumentException("Invalid ResourceBundle name: '" + text + "'");
            }
            String localeString = name.substring(separator + 1);
            Locale locale = StringUtils.parseLocaleString(localeString);
            setValue(locale != null ? ResourceBundle.getBundle(baseName, locale) : ResourceBundle.getBundle(baseName));
        }
    }
}
