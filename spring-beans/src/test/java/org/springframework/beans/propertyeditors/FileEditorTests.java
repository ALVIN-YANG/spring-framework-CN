// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非适用法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditor;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author 托马斯·里斯伯格
 * @author 克里斯·比姆斯
 * @author 于尔根·霍勒
 */
class FileEditorTests {

    @Test
    void testClasspathFileName() {
        PropertyEditor fileEditor = new FileEditor();
        fileEditor.setAsText("classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class");
        Object value = fileEditor.getValue();
        assertThat(value).isInstanceOf(File.class);
        File file = (File) value;
        assertThat(file).exists();
    }

    @Test
    void testWithNonExistentResource() {
        PropertyEditor fileEditor = new FileEditor();
        assertThatIllegalArgumentException().isThrownBy(() -> fileEditor.setAsText("classpath:no_way_this_file_is_found.doc"));
    }

    @Test
    void testWithNonExistentFile() {
        PropertyEditor fileEditor = new FileEditor();
        fileEditor.setAsText("file:no_way_this_file_is_found.doc");
        Object value = fileEditor.getValue();
        assertThat(value).isInstanceOf(File.class);
        File file = (File) value;
        assertThat(file).doesNotExist();
    }

    @Test
    void testAbsoluteFileName() {
        PropertyEditor fileEditor = new FileEditor();
        fileEditor.setAsText("/no_way_this_file_is_found.doc");
        Object value = fileEditor.getValue();
        assertThat(value).isInstanceOf(File.class);
        File file = (File) value;
        assertThat(file).doesNotExist();
    }

    @Test
    void testCurrentDirectory() {
        PropertyEditor fileEditor = new FileEditor();
        fileEditor.setAsText("file:.");
        Object value = fileEditor.getValue();
        assertThat(value).isInstanceOf(File.class);
        File file = (File) value;
        assertThat(file).isEqualTo(new File("."));
    }

    @Test
    void testUnqualifiedFileNameFound() {
        PropertyEditor fileEditor = new FileEditor();
        String fileName = ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class";
        fileEditor.setAsText(fileName);
        Object value = fileEditor.getValue();
        assertThat(value).isInstanceOf(File.class);
        File file = (File) value;
        assertThat(file).exists();
        String absolutePath = file.getAbsolutePath().replace('\\', '/');
        assertThat(absolutePath).endsWith(fileName);
    }

    @Test
    void testUnqualifiedFileNameNotFound() {
        PropertyEditor fileEditor = new FileEditor();
        String fileName = ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".clazz";
        fileEditor.setAsText(fileName);
        Object value = fileEditor.getValue();
        assertThat(value).isInstanceOf(File.class);
        File file = (File) value;
        assertThat(file).doesNotExist();
        String absolutePath = file.getAbsolutePath().replace('\\', '/');
        assertThat(absolutePath).endsWith(fileName);
    }
}
