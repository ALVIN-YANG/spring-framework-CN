// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是隐含的。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.support;

import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * 对 {@link ManagedSet} 的单元测试。
 *
 * @author Rick Evans
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class ManagedSetTests {

    @Test
    void mergeSunnyDay() {
        ManagedSet parent = ManagedSet.of("one", "two");
        ManagedSet child = ManagedSet.of("three");
        child.add("three");
        child.add("four");
        child.setMergeEnabled(true);
        Set mergedSet = child.merge(parent);
        assertThat(mergedSet).as("merge() obviously did not work.").containsExactly("one", "two", "three", "four");
    }

    @Test
    void mergeWithNullParent() {
        ManagedSet child = ManagedSet.of("one");
        child.setMergeEnabled(true);
        assertThat(child.merge(null)).isSameAs(child);
    }

    @Test
    void mergeNotAllowedWhenMergeNotEnabled() {
        assertThatIllegalStateException().isThrownBy(() -> new ManagedSet().merge(null));
    }

    @Test
    void mergeWithNonCompatibleParentType() {
        ManagedSet child = ManagedSet.of("one");
        child.setMergeEnabled(true);
        assertThatIllegalArgumentException().isThrownBy(() -> child.merge("hello"));
    }

    @Test
    void mergeEmptyChild() {
        ManagedSet parent = ManagedSet.of("one", "two");
        ManagedSet child = new ManagedSet();
        child.setMergeEnabled(true);
        Set mergedSet = child.merge(parent);
        assertThat(mergedSet).as("merge() obviously did not work.").containsExactly("one", "two");
    }

    @Test
    void mergeChildValuesOverrideTheParents() {
        // 断言在 merge() 操作过程中不会违反集合合约...
        ManagedSet parent = ManagedSet.of("one", "two");
        ManagedSet child = ManagedSet.of("one");
        child.setMergeEnabled(true);
        Set mergedSet = child.merge(parent);
        assertThat(mergedSet).as("merge() obviously did not work.").containsExactly("one", "two");
    }
}
