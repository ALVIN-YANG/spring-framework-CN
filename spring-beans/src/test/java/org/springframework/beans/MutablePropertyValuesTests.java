// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans;

import java.util.Iterator;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * 测试 {@link MutablePropertyValues}。
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @author Juergen Hoeller
 */
public class MutablePropertyValuesTests extends AbstractPropertyValuesTests {

    @Test
    public void testValid() {
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue(new PropertyValue("forname", "Tony"));
        pvs.addPropertyValue(new PropertyValue("surname", "Blair"));
        pvs.addPropertyValue(new PropertyValue("age", "50"));
        doTestTony(pvs);
        MutablePropertyValues deepCopy = new MutablePropertyValues(pvs);
        doTestTony(deepCopy);
        deepCopy.setPropertyValueAt(new PropertyValue("name", "Gordon"), 0);
        doTestTony(pvs);
        assertThat(deepCopy.getPropertyValue("name").getValue()).isEqualTo("Gordon");
    }

    @Test
    public void testAddOrOverride() {
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue(new PropertyValue("forname", "Tony"));
        pvs.addPropertyValue(new PropertyValue("surname", "Blair"));
        pvs.addPropertyValue(new PropertyValue("age", "50"));
        doTestTony(pvs);
        PropertyValue addedPv = new PropertyValue("rod", "Rod");
        pvs.addPropertyValue(addedPv);
        assertThat(pvs.getPropertyValue("rod").equals(addedPv)).isTrue();
        PropertyValue changedPv = new PropertyValue("forname", "Greg");
        pvs.addPropertyValue(changedPv);
        assertThat(pvs.getPropertyValue("forname").equals(changedPv)).isTrue();
    }

    @Test
    public void testChangesOnEquals() {
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue(new PropertyValue("forname", "Tony"));
        pvs.addPropertyValue(new PropertyValue("surname", "Blair"));
        pvs.addPropertyValue(new PropertyValue("age", "50"));
        MutablePropertyValues pvs2 = pvs;
        PropertyValues changes = pvs2.changesSince(pvs);
        assertThat(changes.getPropertyValues().length).as("changes are empty").isEqualTo(0);
    }

    @Test
    public void testChangeOfOneField() {
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue(new PropertyValue("forname", "Tony"));
        pvs.addPropertyValue(new PropertyValue("surname", "Blair"));
        pvs.addPropertyValue(new PropertyValue("age", "50"));
        MutablePropertyValues pvs2 = new MutablePropertyValues(pvs);
        PropertyValues changes = pvs2.changesSince(pvs);
        assertThat(changes.getPropertyValues().length).as("changes are empty, not of length " + changes.getPropertyValues().length).isEqualTo(0);
        pvs2.addPropertyValue(new PropertyValue("forname", "Gordon"));
        changes = pvs2.changesSince(pvs);
        assertThat(changes.getPropertyValues().length).as("1 change").isEqualTo(1);
        PropertyValue fn = changes.getPropertyValue("forname");
        assertThat(fn).as("change is forname").isNotNull();
        assertThat(fn.getValue().equals("Gordon")).as("new value is gordon").isTrue();
        MutablePropertyValues pvs3 = new MutablePropertyValues(pvs);
        changes = pvs3.changesSince(pvs);
        assertThat(changes.getPropertyValues().length).as("changes are empty, not of length " + changes.getPropertyValues().length).isEqualTo(0);
        // 添加新
        pvs3.addPropertyValue(new PropertyValue("foo", "bar"));
        pvs3.addPropertyValue(new PropertyValue("fi", "fum"));
        changes = pvs3.changesSince(pvs);
        assertThat(changes.getPropertyValues().length).as("2 change").isEqualTo(2);
        fn = changes.getPropertyValue("foo");
        assertThat(fn).as("change in foo").isNotNull();
        assertThat(fn.getValue().equals("bar")).as("new value is bar").isTrue();
    }

    @Test
    public void iteratorContainsPropertyValue() {
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("foo", "bar");
        Iterator<PropertyValue> it = pvs.iterator();
        assertThat(it.hasNext()).isTrue();
        PropertyValue pv = it.next();
        assertThat(pv.getName()).isEqualTo("foo");
        assertThat(pv.getValue()).isEqualTo("bar");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(it::remove);
        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void iteratorIsEmptyForEmptyValues() {
        MutablePropertyValues pvs = new MutablePropertyValues();
        Iterator<PropertyValue> it = pvs.iterator();
        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void streamContainsPropertyValue() {
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("foo", "bar");
        assertThat(pvs.stream()).isNotNull();
        assertThat(pvs.stream().count()).isEqualTo(1L);
        assertThat(pvs.stream().anyMatch(pv -> "foo".equals(pv.getName()) && "bar".equals(pv.getValue()))).isTrue();
        assertThat(pvs.stream().anyMatch(pv -> "bar".equals(pv.getName()) && "foo".equals(pv.getValue()))).isFalse();
    }

    @Test
    public void streamIsEmptyForEmptyValues() {
        MutablePropertyValues pvs = new MutablePropertyValues();
        assertThat(pvs.stream()).isNotNull();
        assertThat(pvs.stream().count()).isEqualTo(0L);
    }
}
