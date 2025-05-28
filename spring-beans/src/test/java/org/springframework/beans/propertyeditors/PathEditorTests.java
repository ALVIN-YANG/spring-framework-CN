// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”），除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可的具体语言、权限和限制，
* 请参阅许可证。*/
package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditor;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @作者 Juergen Hoeller
 * @since 4.3.2
 */
class PathEditorTests {

    @Test
    void testClasspathPathName() {
        PropertyEditor pathEditor = new PathEditor();
        pathEditor.setAsText("classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class");
        Object value = pathEditor.getValue();
        assertThat(value).isInstanceOf(Path.class);
        Path path = (Path) value;
        assertThat(path.toFile()).exists();
    }

    @Test
    void testWithNonExistentResource() {
        PropertyEditor pathEditor = new PathEditor();
        assertThatIllegalArgumentException().isThrownBy(() -> pathEditor.setAsText("classpath:/no_way_this_file_is_found.doc"));
    }

    @Test
    void testWithNonExistentPath() {
        PropertyEditor pathEditor = new PathEditor();
        pathEditor.setAsText("file:/no_way_this_file_is_found.doc");
        Object value = pathEditor.getValue();
        assertThat(value).isInstanceOf(Path.class);
        Path path = (Path) value;
        assertThat(path.toFile()).doesNotExist();
    }

    @Test
    void testAbsolutePath() {
        PropertyEditor pathEditor = new PathEditor();
        pathEditor.setAsText("/no_way_this_file_is_found.doc");
        Object value = pathEditor.getValue();
        assertThat(value).isInstanceOf(Path.class);
        Path path = (Path) value;
        assertThat(path.toFile()).doesNotExist();
    }

    @Test
    void testWindowsAbsolutePath() {
        PropertyEditor pathEditor = new PathEditor();
        pathEditor.setAsText("C:\\no_way_this_file_is_found.doc");
        Object value = pathEditor.getValue();
        assertThat(value).isInstanceOf(Path.class);
        Path path = (Path) value;
        assertThat(path.toFile()).doesNotExist();
    }

    @Test
    void testWindowsAbsoluteFilePath() {
        PropertyEditor pathEditor = new PathEditor();
        try {
            pathEditor.setAsText("file://C:\\no_way_this_file_is_found.doc");
            Object value = pathEditor.getValue();
            assertThat(value).isInstanceOf(Path.class);
            Path path = (Path) value;
            assertThat(path.toFile()).doesNotExist();
        } catch (IllegalArgumentException ex) {
            if (File.separatorChar == '\\') {
                // 在 Windows 上，否则静默忽略
                throw ex;
            }
        }
    }

    @Test
    void testCurrentDirectory() {
        PropertyEditor pathEditor = new PathEditor();
        pathEditor.setAsText("file:.");
        Object value = pathEditor.getValue();
        assertThat(value).isInstanceOf(Path.class);
        Path path = (Path) value;
        assertThat(path).isEqualTo(Paths.get("."));
    }

    @Test
    void testUnqualifiedPathNameFound() {
        PropertyEditor pathEditor = new PathEditor();
        String fileName = ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class";
        pathEditor.setAsText(fileName);
        Object value = pathEditor.getValue();
        assertThat(value).isInstanceOf(Path.class);
        Path path = (Path) value;
        File file = path.toFile();
        assertThat(file).exists();
        String absolutePath = file.getAbsolutePath();
        if (File.separatorChar == '\\') {
            absolutePath = absolutePath.replace('\\', '/');
        }
        assertThat(absolutePath).endsWith(fileName);
    }

    @Test
    void testUnqualifiedPathNameNotFound() {
        PropertyEditor pathEditor = new PathEditor();
        String fileName = ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".clazz";
        pathEditor.setAsText(fileName);
        Object value = pathEditor.getValue();
        assertThat(value).isInstanceOf(Path.class);
        Path path = (Path) value;
        File file = path.toFile();
        assertThat(file).doesNotExist();
        String absolutePath = file.getAbsolutePath();
        if (File.separatorChar == '\\') {
            absolutePath = absolutePath.replace('\\', '/');
        }
        assertThat(absolutePath).endsWith(fileName);
    }
}
