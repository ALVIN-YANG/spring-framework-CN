// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）授权；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * @author Rick Evans
 * @author Juergen Hoeller
 * @author Sam Brannen
 *
 * 作者：Rick Evans
 * 作者：Juergen Hoeller
 * 作者：Sam Brannen
 */
@SuppressWarnings("rawtypes")
public class ManagedPropertiesTests {

    @Test
    @SuppressWarnings("unchecked")
    public void mergeSunnyDay() {
        ManagedProperties parent = new ManagedProperties();
        parent.setProperty("one", "one");
        parent.setProperty("two", "two");
        ManagedProperties child = new ManagedProperties();
        child.setProperty("three", "three");
        child.setMergeEnabled(true);
        Map mergedMap = (Map) child.merge(parent);
        assertThat(mergedMap).as("merge() obviously did not work.").hasSize(3);
    }

    @Test
    public void mergeWithNullParent() {
        ManagedProperties child = new ManagedProperties();
        child.setMergeEnabled(true);
        assertThat(child.merge(null)).isSameAs(child);
    }

    @Test
    public void mergeWithNonCompatibleParentType() {
        ManagedProperties map = new ManagedProperties();
        map.setMergeEnabled(true);
        assertThatIllegalArgumentException().isThrownBy(() -> map.merge("hello"));
    }

    @Test
    public void mergeNotAllowedWhenMergeNotEnabled() {
        ManagedProperties map = new ManagedProperties();
        assertThatIllegalStateException().isThrownBy(() -> map.merge(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mergeEmptyChild() {
        ManagedProperties parent = new ManagedProperties();
        parent.setProperty("one", "one");
        parent.setProperty("two", "two");
        ManagedProperties child = new ManagedProperties();
        child.setMergeEnabled(true);
        Map mergedMap = (Map) child.merge(parent);
        assertThat(mergedMap).as("merge() obviously did not work.").hasSize(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mergeChildValuesOverrideTheParents() {
        ManagedProperties parent = new ManagedProperties();
        parent.setProperty("one", "one");
        parent.setProperty("two", "two");
        ManagedProperties child = new ManagedProperties();
        child.setProperty("one", "fork");
        child.setMergeEnabled(true);
        Map mergedMap = (Map) child.merge(parent);
        // 子类对于 'one' 的值必须覆盖父类值...
        assertThat(mergedMap).as("merge() obviously did not work.").hasSize(2);
        assertThat(mergedMap.get("one")).as("Parent value not being overridden during merge().").isEqualTo("fork");
    }
}
