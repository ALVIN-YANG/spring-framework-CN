// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权的具体语言和限制，请参阅许可证。*/
package org.springframework.beans;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 对 {@link PropertyAccessorUtils} 的单元测试。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 */
public class PropertyAccessorUtilsTests {

    @Test
    public void getPropertyName() {
        assertThat(PropertyAccessorUtils.getPropertyName("")).isEmpty();
        assertThat(PropertyAccessorUtils.getPropertyName("[user]")).isEmpty();
        assertThat(PropertyAccessorUtils.getPropertyName("user")).isEqualTo("user");
    }

    @Test
    public void isNestedOrIndexedProperty() {
        assertThat(PropertyAccessorUtils.isNestedOrIndexedProperty(null)).isFalse();
        assertThat(PropertyAccessorUtils.isNestedOrIndexedProperty("")).isFalse();
        assertThat(PropertyAccessorUtils.isNestedOrIndexedProperty("user")).isFalse();
        assertThat(PropertyAccessorUtils.isNestedOrIndexedProperty("[user]")).isTrue();
        assertThat(PropertyAccessorUtils.isNestedOrIndexedProperty("user.name")).isTrue();
    }

    @Test
    public void getFirstNestedPropertySeparatorIndex() {
        assertThat(PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex("[user]")).isEqualTo(-1);
        assertThat(PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex("user.name")).isEqualTo(4);
    }

    @Test
    public void getLastNestedPropertySeparatorIndex() {
        assertThat(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex("[user]")).isEqualTo(-1);
        assertThat(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex("user.address.street")).isEqualTo(12);
    }

    @Test
    public void matchesProperty() {
        assertThat(PropertyAccessorUtils.matchesProperty("user", "email")).isFalse();
        assertThat(PropertyAccessorUtils.matchesProperty("username", "user")).isFalse();
        assertThat(PropertyAccessorUtils.matchesProperty("admin[user]", "user")).isFalse();
        assertThat(PropertyAccessorUtils.matchesProperty("user", "user")).isTrue();
        assertThat(PropertyAccessorUtils.matchesProperty("user[name]", "user")).isTrue();
    }

    @Test
    public void canonicalPropertyName() {
        assertThat(PropertyAccessorUtils.canonicalPropertyName(null)).isEmpty();
        assertThat(PropertyAccessorUtils.canonicalPropertyName("map")).isEqualTo("map");
        assertThat(PropertyAccessorUtils.canonicalPropertyName("map[key1]")).isEqualTo("map[key1]");
        assertThat(PropertyAccessorUtils.canonicalPropertyName("map['key1']")).isEqualTo("map[key1]");
        assertThat(PropertyAccessorUtils.canonicalPropertyName("map[\"key1\"]")).isEqualTo("map[key1]");
        assertThat(PropertyAccessorUtils.canonicalPropertyName("map[key1][key2]")).isEqualTo("map[key1][key2]");
        assertThat(PropertyAccessorUtils.canonicalPropertyName("map['key1'][\"key2\"]")).isEqualTo("map[key1][key2]");
        assertThat(PropertyAccessorUtils.canonicalPropertyName("map[key1].name")).isEqualTo("map[key1].name");
        assertThat(PropertyAccessorUtils.canonicalPropertyName("map['key1'].name")).isEqualTo("map[key1].name");
        assertThat(PropertyAccessorUtils.canonicalPropertyName("map[\"key1\"].name")).isEqualTo("map[key1].name");
    }

    @Test
    public void canonicalPropertyNames() {
        assertThat(PropertyAccessorUtils.canonicalPropertyNames(null)).isNull();
        String[] original = new String[] { "map", "map[key1]", "map['key1']", "map[\"key1\"]", "map[key1][key2]", "map['key1'][\"key2\"]", "map[key1].name", "map['key1'].name", "map[\"key1\"].name" };
        String[] canonical = new String[] { "map", "map[key1]", "map[key1]", "map[key1]", "map[key1][key2]", "map[key1][key2]", "map[key1].name", "map[key1].name", "map[key1].name" };
        assertThat(PropertyAccessorUtils.canonicalPropertyNames(original)).isEqualTo(canonical);
    }
}
