// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按"原样"分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.propertyeditors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 对 {@link CustomCollectionEditor} 类的单元测试。
 *
 * @author Rick Evans
 * @author Chris Beams
 */
public class CustomCollectionEditorTests {

    @Test
    public void testCtorWithNullCollectionType() {
        assertThatIllegalArgumentException().isThrownBy(() -> new CustomCollectionEditor(null));
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testCtorWithNonCollectionType() {
        assertThatIllegalArgumentException().isThrownBy(() -> new CustomCollectionEditor((Class) String.class));
    }

    @Test
    public void testWithCollectionTypeThatDoesNotExposeAPublicNoArgCtor() {
        CustomCollectionEditor editor = new CustomCollectionEditor(CollectionTypeWithNoNoArgCtor.class);
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setValue("1"));
    }

    @Test
    public void testSunnyDaySetValue() {
        CustomCollectionEditor editor = new CustomCollectionEditor(ArrayList.class);
        editor.setValue(new int[] { 0, 1, 2 });
        Object value = editor.getValue();
        assertThat(value).isNotNull();
        assertThat(value instanceof ArrayList).isTrue();
        List<?> list = (List<?>) value;
        assertThat(list).as("There must be 3 elements in the converted collection").hasSize(3);
        assertThat(list.get(0)).isEqualTo(0);
        assertThat(list.get(1)).isEqualTo(1);
        assertThat(list.get(2)).isEqualTo(2);
    }

    @Test
    public void testWhenTargetTypeIsExactlyTheCollectionInterfaceUsesFallbackCollectionType() {
        CustomCollectionEditor editor = new CustomCollectionEditor(Collection.class);
        editor.setValue("0, 1, 2");
        Collection<?> value = (Collection<?>) editor.getValue();
        assertThat(value).isNotNull();
        assertThat(value).as("There must be 1 element in the converted collection").hasSize(1);
        assertThat(value.iterator().next()).isEqualTo("0, 1, 2");
    }

    @Test
    public void testSunnyDaySetAsTextYieldsSingleValue() {
        CustomCollectionEditor editor = new CustomCollectionEditor(ArrayList.class);
        editor.setValue("0, 1, 2");
        Object value = editor.getValue();
        assertThat(value).isNotNull();
        assertThat(value instanceof ArrayList).isTrue();
        List<?> list = (List<?>) value;
        assertThat(list).as("There must be 1 element in the converted collection").hasSize(1);
        assertThat(list.get(0)).isEqualTo("0, 1, 2");
    }

    @SuppressWarnings({ "serial", "unused" })
    private static final class CollectionTypeWithNoNoArgCtor extends ArrayList<Object> {

        public CollectionTypeWithNoNoArgCtor(String anArg) {
        }
    }
}
