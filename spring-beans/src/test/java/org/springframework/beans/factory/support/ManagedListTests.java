// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License, Version 2.0（“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的质量保证或条件，
* 无论明示的还是暗示的。请参阅许可证以了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * 对 {@link ManagedList} 的单元测试。
 *
 * @author Rick Evans
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class ManagedListTests {

    @Test
    void mergeSunnyDay() {
        ManagedList parent = ManagedList.of("one", "two");
        ManagedList child = ManagedList.of("three");
        child.setMergeEnabled(true);
        List mergedList = child.merge(parent);
        assertThat(mergedList).as("merge() obviously did not work.").containsExactly("one", "two", "three");
    }

    @Test
    void mergeWithNullParent() {
        ManagedList child = ManagedList.of("one");
        child.setMergeEnabled(true);
        assertThat(child.merge(null)).isSameAs(child);
    }

    @Test
    void mergeNotAllowedWhenMergeNotEnabled() {
        ManagedList child = new ManagedList();
        assertThatIllegalStateException().isThrownBy(() -> child.merge(null));
    }

    @Test
    void mergeWithIncompatibleParentType() {
        ManagedList child = ManagedList.of("one");
        child.setMergeEnabled(true);
        assertThatIllegalArgumentException().isThrownBy(() -> child.merge("hello"));
    }

    @Test
    void mergeEmptyChild() {
        ManagedList parent = ManagedList.of("one", "two");
        ManagedList child = new ManagedList();
        child.setMergeEnabled(true);
        List mergedList = child.merge(parent);
        assertThat(mergedList).as("merge() obviously did not work.").containsExactly("one", "two");
    }

    @Test
    void mergedChildValuesDoNotOverrideTheParents() {
        // 在列表的上下文中没有太多意义...
        ManagedList parent = ManagedList.of("one", "two");
        ManagedList child = ManagedList.of("one");
        child.setMergeEnabled(true);
        List mergedList = child.merge(parent);
        assertThat(mergedList).as("merge() obviously did not work.").containsExactly("one", "two", "one");
    }
}
