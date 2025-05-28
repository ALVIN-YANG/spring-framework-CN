// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件
* 是按“现状”提供的，不提供任何形式，明示或暗示的保证或条件。
* 请参阅许可证以获取关于许可权限和限制的特定语言。*/
package org.springframework.beans.factory.support;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * @作者 Rick Evans
 * @作者 Juergen Hoeller
 * @作者 Sam Brannen
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ManagedMapTests {

    @Test
    public void mergeSunnyDay() {
        ManagedMap parent = ManagedMap.ofEntries(Map.entry("one", "one"), Map.entry("two", "two"));
        ManagedMap child = ManagedMap.ofEntries(Map.entry("tree", "three"));
        child.setMergeEnabled(true);
        Map mergedMap = (Map) child.merge(parent);
        assertThat(mergedMap).as("merge() obviously did not work.").hasSize(3);
    }

    @Test
    public void mergeWithNullParent() {
        ManagedMap child = new ManagedMap();
        child.setMergeEnabled(true);
        assertThat(child.merge(null)).isSameAs(child);
    }

    @Test
    public void mergeWithNonCompatibleParentType() {
        ManagedMap map = new ManagedMap();
        map.setMergeEnabled(true);
        assertThatIllegalArgumentException().isThrownBy(() -> map.merge("hello"));
    }

    @Test
    public void mergeNotAllowedWhenMergeNotEnabled() {
        assertThatIllegalStateException().isThrownBy(() -> new ManagedMap().merge(null));
    }

    @Test
    public void mergeEmptyChild() {
        ManagedMap parent = ManagedMap.ofEntries(Map.entry("one", "one"), Map.entry("two", "two"));
        ManagedMap child = new ManagedMap();
        child.setMergeEnabled(true);
        Map mergedMap = (Map) child.merge(parent);
        assertThat(mergedMap).as("merge() obviously did not work.").hasSize(2);
    }

    @Test
    public void mergeChildValuesOverrideTheParents() {
        ManagedMap parent = ManagedMap.ofEntries(Map.entry("one", "one"), Map.entry("two", "two"));
        ManagedMap child = ManagedMap.ofEntries(Map.entry("one", "fork"));
        child.setMergeEnabled(true);
        Map mergedMap = (Map) child.merge(parent);
        // 子对象中对于 'one' 的值必须覆盖父对象中的值...
        assertThat(mergedMap).as("merge() obviously did not work.").hasSize(2);
        assertThat(mergedMap.get("one")).as("Parent value not being overridden during merge().").isEqualTo("fork");
    }
}
