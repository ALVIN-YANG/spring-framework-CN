// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.StringJoiner;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 用于一组 {@link Class 类} 的属性编辑器，以实现直接填充一个 {@code Class[]} 属性，而无需使用作为桥梁的 {@code String} 类名属性。
 *
 * <p>此外还支持 "java.lang.String[]" 风格的数组类名，与标准的 {@link Class#forName(String)} 方法形成对比。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ClassArrayEditor extends PropertyEditorSupport {

    @Nullable
    private final ClassLoader classLoader;

    /**
     * 创建一个默认的 {@code ClassEditor}，使用线程上下文的 {@code ClassLoader}。
     */
    public ClassArrayEditor() {
        this(null);
    }

    /**
     * 创建一个使用给定 {@code ClassLoader} 的默认 {@code ClassArrayEditor}。
     * @param classLoader 要使用的 {@code ClassLoader}（或者传递 {@code null} 以使用线程上下文的 {@code ClassLoader}）
     */
    public ClassArrayEditor(@Nullable ClassLoader classLoader) {
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            String[] classNames = StringUtils.commaDelimitedListToStringArray(text);
            Class<?>[] classes = new Class<?>[classNames.length];
            for (int i = 0; i < classNames.length; i++) {
                String className = classNames[i].trim();
                classes[i] = ClassUtils.resolveClassName(className, this.classLoader);
            }
            setValue(classes);
        } else {
            setValue(null);
        }
    }

    @Override
    public String getAsText() {
        Class<?>[] classes = (Class[]) getValue();
        if (ObjectUtils.isEmpty(classes)) {
            return "";
        }
        StringJoiner sj = new StringJoiner(",");
        for (Class<?> klass : classes) {
            sj.add(ClassUtils.getQualifiedName(klass));
        }
        return sj.toString();
    }
}
