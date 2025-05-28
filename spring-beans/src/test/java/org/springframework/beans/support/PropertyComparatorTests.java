// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.support;

import java.util.Comparator;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 对 {@link PropertyComparator} 的单元测试。
 *
 * @author Keith Donald
 * @author Chris Beams
 */
public class PropertyComparatorTests {

    @Test
    public void testPropertyComparator() {
        Dog dog = new Dog();
        dog.setNickName("mace");
        Dog dog2 = new Dog();
        dog2.setNickName("biscy");
        PropertyComparator<Dog> c = new PropertyComparator<>("nickName", false, true);
        assertThat(c.compare(dog, dog2)).isGreaterThan(0);
        assertThat(c.compare(dog, dog)).isEqualTo(0);
        assertThat(c.compare(dog2, dog)).isLessThan(0);
    }

    @Test
    public void testPropertyComparatorNulls() {
        Dog dog = new Dog();
        Dog dog2 = new Dog();
        PropertyComparator<Dog> c = new PropertyComparator<>("nickName", false, true);
        assertThat(c.compare(dog, dog2)).isEqualTo(0);
    }

    @Test
    public void testChainedComparators() {
        Comparator<Dog> c = new PropertyComparator<>("lastName", false, true);
        Dog dog1 = new Dog();
        dog1.setFirstName("macy");
        dog1.setLastName("grayspots");
        Dog dog2 = new Dog();
        dog2.setFirstName("biscuit");
        dog2.setLastName("grayspots");
        assertThat(c.compare(dog1, dog2)).isEqualTo(0);
        c = c.thenComparing(new PropertyComparator<>("firstName", false, true));
        assertThat(c.compare(dog1, dog2)).isGreaterThan(0);
        dog2.setLastName("konikk dog");
        assertThat(c.compare(dog2, dog1)).isGreaterThan(0);
    }

    @Test
    public void testChainedComparatorsReversed() {
        Comparator<Dog> c = (new PropertyComparator<Dog>("lastName", false, true)).thenComparing(new PropertyComparator<>("firstName", false, true));
        Dog dog1 = new Dog();
        dog1.setFirstName("macy");
        dog1.setLastName("grayspots");
        Dog dog2 = new Dog();
        dog2.setFirstName("biscuit");
        dog2.setLastName("grayspots");
        assertThat(c.compare(dog1, dog2)).isGreaterThan(0);
        c = c.reversed();
        assertThat(c.compare(dog1, dog2)).isLessThan(0);
    }

    @SuppressWarnings("unused")
    private static class Dog implements Comparable<Object> {

        private String nickName;

        private String firstName;

        private String lastName;

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Override
        public int compareTo(Object o) {
            return this.nickName.compareTo(((Dog) o).nickName);
        }
    }
}
