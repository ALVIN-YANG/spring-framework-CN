// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 用于Java类（`java.lang.Class`）的属性编辑器，允许直接填充`Class`属性，无需使用字符串类名属性作为桥梁。
 *
 * <p>还支持类似"java.lang.String[]"样式的数组类名，与标准的`Class#forName(String)`方法不同。
 *
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2003年5月13日
 * @see Class#forName
 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
 */
public class ClassEditor extends PropertyEditorSupport {

    @Nullable
    private final ClassLoader classLoader;

    /**
     * 创建一个默认的 ClassEditor，使用线程上下文 ClassLoader。
     */
    public ClassEditor() {
        this(null);
    }

    /**
     * 使用给定的类加载器创建一个默认的 ClassEditor。
     * @param classLoader 要使用的类加载器
     * （或为空，表示使用线程上下文类加载器）
     */
    public ClassEditor(@Nullable ClassLoader classLoader) {
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            setValue(ClassUtils.resolveClassName(text.trim(), this.classLoader));
        } else {
            setValue(null);
        }
    }

    @Override
    public String getAsText() {
        Class<?> clazz = (Class<?>) getValue();
        if (clazz != null) {
            return ClassUtils.getQualifiedName(clazz);
        } else {
            return "";
        }
    }
}
