// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.text.NumberFormat;
import org.springframework.lang.Nullable;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

/**
 * 用于任何 Number 子类的属性编辑器，例如 Short、Integer、Long、BigInteger、Float、Double、BigDecimal。可以使用给定的 NumberFormat 进行（区域特定的）解析和渲染，或者使用默认的 {@code decode} / {@code valueOf} / {@code toString} 方法。
 *
 * <p>这并不是作为系统 PropertyEditor 使用，而是作为自定义控制器代码中的区域特定数字编辑器，将用户输入的数字字符串解析为 bean 的 Number 属性，并在 UI 表单中渲染它们。
 *
 * <p>在 Web MVC 代码中，这个编辑器通常通过调用 {@code binder.registerCustomEditor} 进行注册。
 *
 * @author Juergen Hoeller
 * @since 06.06.2003
 * @see Number
 * @see java.text.NumberFormat
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 */
public class CustomNumberEditor extends PropertyEditorSupport {

    private final Class<? extends Number> numberClass;

    @Nullable
    private final NumberFormat numberFormat;

    private final boolean allowEmpty;

    /**
     * 创建一个新的 CustomNumberEditor 实例，使用默认的
     * {@code valueOf} 方法进行解析和 {@code toString}
     * 方法进行渲染。
     * <p>“allowEmpty” 参数表示是否允许解析空字符串，即被解释为 {@code null} 值。
     * 否则，如果遇到空字符串，将抛出 IllegalArgumentException 异常。
     * @param numberClass 要生成的 Number 子类
     * @param allowEmpty 是否允许空字符串
     * @throws IllegalArgumentException 如果指定了无效的 numberClass
     * @see org.springframework.util.NumberUtils#parseNumber(String, Class)
     * @see Integer#valueOf
     * @see Integer#toString
     */
    public CustomNumberEditor(Class<? extends Number> numberClass, boolean allowEmpty) throws IllegalArgumentException {
        this(numberClass, null, allowEmpty);
    }

    /**
     * 创建一个新的 CustomNumberEditor 实例，使用给定的 NumberFormat 用于解析和渲染。
     * <p>allowEmpty 参数表示是否允许空字符串进行解析，即解释为 {@code null} 值。
     * 否则，在这种情况下将抛出 IllegalArgumentException 异常。
     * @param numberClass 要生成的 Number 子类
     * @param numberFormat 用于解析和渲染的 NumberFormat
     * @param allowEmpty 是否允许空字符串
     * @throws IllegalArgumentException 如果指定了无效的 numberClass
     * @see org.springframework.util.NumberUtils#parseNumber(String, Class, java.text.NumberFormat)
     * @see java.text.NumberFormat#parse
     * @see java.text.NumberFormat#format
     */
    public CustomNumberEditor(Class<? extends Number> numberClass, @Nullable NumberFormat numberFormat, boolean allowEmpty) throws IllegalArgumentException {
        if (!Number.class.isAssignableFrom(numberClass)) {
            throw new IllegalArgumentException("Property class must be a subclass of Number");
        }
        this.numberClass = numberClass;
        this.numberFormat = numberFormat;
        this.allowEmpty = allowEmpty;
    }

    /**
     * 使用指定的 NumberFormat 从给定文本中解析数字。
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (this.allowEmpty && !StringUtils.hasText(text)) {
            // 将空字符串视为null值。
            setValue(null);
        } else if (this.numberFormat != null) {
            // 使用给定的 NumberFormat 解析文本。
            setValue(NumberUtils.parseNumber(text, this.numberClass, this.numberFormat));
        } else {
            // 使用默认的valueOf方法进行文本解析。
            setValue(NumberUtils.parseNumber(text, this.numberClass));
        }
    }

    /**
     * 如果需要，将数字值强制转换为所需的目标类。
     */
    @Override
    public void setValue(@Nullable Object value) {
        if (value instanceof Number num) {
            super.setValue(NumberUtils.convertNumberToTargetClass(num, this.numberClass));
        } else {
            super.setValue(value);
        }
    }

    /**
     * 使用指定的NumberFormat将数字格式化为字符串。
     */
    @Override
    public String getAsText() {
        Object value = getValue();
        if (value == null) {
            return "";
        }
        if (this.numberFormat != null) {
            // 使用 NumberFormat 进行值渲染。
            return this.numberFormat.format(value);
        } else {
            // 使用 `toString` 方法来渲染值。
            return value.toString();
        }
    }
}
